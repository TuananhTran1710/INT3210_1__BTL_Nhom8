package com.example.wink.data.repository

import android.content.Context
import android.net.Uri
import com.example.wink.data.model.Comment
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import com.example.wink.util.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class SocialRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : SocialRepository {

    private val currentUserId get() = auth.currentUser?.uid ?: ""

    // 1. LẤY BẢNG TIN (REALTIME)
    override fun getSocialFeed(): Flow<List<SocialPost>> = callbackFlow {
        val listener = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Sắp xếp mới nhất
            .limit(50) // Lấy 50 bài gần nhất
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Đóng flow nếu lỗi
                    return@addSnapshotListener
                }

                // Launch coroutine để kiểm tra bài gốc
                CoroutineScope(Dispatchers.IO).launch {
                    // Lấy danh sách originalPostId để kiểm tra bài gốc có tồn tại không
                    val originalPostIds = snapshot?.documents
                        ?.filter { it.getBoolean("isRepost") == true }
                        ?.mapNotNull { it.getString("originalPostId") }
                        ?.distinct()
                        ?: emptyList()
                    
                    // Kiểm tra các bài gốc có tồn tại không
                    val deletedOriginalPostIds = mutableSetOf<String>()
                    for (originalPostId in originalPostIds) {
                        try {
                            val originalDoc = firestore.collection("posts").document(originalPostId).get().await()
                            if (!originalDoc.exists()) {
                                deletedOriginalPostIds.add(originalPostId)
                            }
                        } catch (e: Exception) {
                            // Nếu lỗi khi lấy bài gốc, coi như đã bị xóa
                            deletedOriginalPostIds.add(originalPostId)
                        }
                    }

                    val posts = snapshot?.documents?.mapNotNull { doc ->
                        val likedBy = doc.get("likedBy") as? List<String> ?: emptyList()
                        val retweetedBy = doc.get("retweetedBy") as? List<String> ?: emptyList()
                        val images = (doc.get("imageUrls") as? List<*>)?.map { it.toString() } ?: emptyList()

                        val userId = doc.getString("userId") ?: ""
                        val canDelete = userId == currentUserId
                        val canEdit = userId == currentUserId
                        val isRepost = doc.getBoolean("isRepost") ?: false
                        val originalPostId = doc.getString("originalPostId")
                        val isOriginalDeleted = isRepost && originalPostId != null && deletedOriginalPostIds.contains(originalPostId)

                        SocialPost(
                            id = doc.id,
                            userId = userId,
                            username = doc.getString("username") ?: "Ẩn danh",
                            avatarUrl = doc.getString("avatarUrl"),
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            likes = (doc.getLong("likesCount") ?: 0).toInt(),
                            comments = (doc.getLong("commentsCount") ?: 0).toInt(),
                            isLikedByMe = likedBy.contains(currentUserId),
                            imageUrls = images,
                            isRepost = isRepost,
                            originalPostId = originalPostId,
                            originalUserId = doc.getString("originalUserId"),
                            originalUsername = doc.getString("originalUsername"),
                            originalAvatarUrl = doc.getString("originalAvatarUrl"),
                            isOriginalDeleted = isOriginalDeleted,
                            retweetedBy = retweetedBy,
                            retweetCount = (doc.getLong("retweetCount") ?: 0).toInt(),
                            isRetweetedByMe = retweetedBy.contains(currentUserId),
                            canDelete = canDelete,
                            canEdit = canEdit
                        )
                    } ?: emptyList()

                    trySend(posts)
                }
            }
        awaitClose { listener.remove() }
    }

    // 2. ĐĂNG BÀI
    override suspend fun createPost(content: String, imageUrls: List<String>, user: User): Result<Unit> {
        return try {
            val postMap = hashMapOf(
                "userId" to user.uid,
                "username" to user.username,
                "avatarUrl" to user.avatarUrl,
                "content" to content,
                "imageUrls" to imageUrls,
                "timestamp" to System.currentTimeMillis(),
                "likesCount" to 0,
                "commentsCount" to 0,
                "likedBy" to emptyList<String>(),
                "isRepost" to false,
                "retweetedBy" to emptyList<String>(),
                "retweetCount" to 0
            )
            firestore.collection("posts").add(postMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. LIKE / UNLIKE BÀI VIẾT
    override suspend fun toggleLike(postId: String, userId: String, currentLikes: List<String>): Result<Unit> {
        return try {
            val isLiked = currentLikes.contains(userId)
            val postRef = firestore.collection("posts").document(postId)

            if (isLiked) {
                // Unlike: Xóa ID khỏi mảng, giảm count
                postRef.update(
                    mapOf(
                        "likedBy" to FieldValue.arrayRemove(userId),
                        "likesCount" to FieldValue.increment(-1)
                    )
                ).await()
            } else {
                // Like: Thêm ID vào mảng, tăng count
                postRef.update(
                    mapOf(
                        "likedBy" to FieldValue.arrayUnion(userId),
                        "likesCount" to FieldValue.increment(1)
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. LẤY COMMENT (REALTIME)
    override fun getComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = firestore.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Cũ nhất lên đầu
            .addSnapshotListener { snapshot, _ ->
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    val likedBy = doc.get("likedBy") as? List<String> ?: emptyList()
                    val commentUserId = doc.getString("userId") ?: ""
                    Comment(
                        id = doc.id,
                        userId = commentUserId,
                        username = doc.getString("username") ?: "Ẩn danh",
                        avatarUrl = doc.getString("avatarUrl"),
                        content = doc.getString("content") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        likedBy = likedBy,
                        likeCount = (doc.getLong("likeCount") ?: 0).toInt(),
                        isLikedByMe = likedBy.contains(currentUserId),
                        isEdited = doc.getBoolean("isEdited") ?: false,
                        canEdit = commentUserId == currentUserId
                    )
                } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    // 5. GỬI COMMENT
    override suspend fun sendComment(postId: String, content: String, user: User): Result<Unit> {
        return try {
            val commentMap = hashMapOf(
                "userId" to user.uid,
                "username" to user.username,
                "avatarUrl" to user.avatarUrl,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "likedBy" to emptyList<String>(),
                "likeCount" to 0
            )

            // Thêm vào subcollection comments
            firestore.collection("posts").document(postId)
                .collection("comments")
                .add(commentMap)
                .await()

            // Tăng biến đếm comment ở bài post gốc
            firestore.collection("posts").document(postId)
                .update("commentsCount", FieldValue.increment(1))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. LIKE / UNLIKE COMMENT
    override suspend fun toggleCommentLike(postId: String, commentId: String, userId: String, currentLikes: List<String>): Result<Unit> {
        return try {
            val isLiked = currentLikes.contains(userId)
            val commentRef = firestore.collection("posts").document(postId)
                .collection("comments").document(commentId)

            if (isLiked) {
                // Unlike
                commentRef.update(
                    mapOf(
                        "likedBy" to FieldValue.arrayRemove(userId),
                        "likeCount" to FieldValue.increment(-1)
                    )
                ).await()
            } else {
                // Like
                commentRef.update(
                    mapOf(
                        "likedBy" to FieldValue.arrayUnion(userId),
                        "likeCount" to FieldValue.increment(1)
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6.5. SỬA COMMENT
    override suspend fun editComment(postId: String, commentId: String, userId: String, newContent: String): Result<Unit> {
        return try {
            val commentRef = firestore.collection("posts").document(postId)
                .collection("comments").document(commentId)

            // Kiểm tra quyền sở hữu
            val doc = commentRef.get().await()
            val ownerId = doc.getString("userId")

            if (ownerId != userId) {
                return Result.failure(Exception("Bạn không có quyền sửa comment này"))
            }

            commentRef.update(
                mapOf(
                    "content" to newContent,
                    "isEdited" to true,
                    "editedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 7. XÓA BÀI VIẾT
    override suspend fun deletePost(postId: String, userId: String): Result<Unit> {
        return try {
            val postRef = firestore.collection("posts").document(postId)

            // Kiểm tra quyền sở hữu
            val doc = postRef.get().await()
            val ownerId = doc.getString("userId")

            if (ownerId != userId) {
                return Result.failure(Exception("Bạn không có quyền xóa bài viết này"))
            }

            // Xóa tất cả comment của bài này trước
            val commentsSnapshot = postRef.collection("comments").get().await()
            for (commentDoc in commentsSnapshot.documents) {
                commentDoc.reference.delete().await()
            }

            // Xóa bài viết
            postRef.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 8. SỬA BÀI VIẾT
    override suspend fun editPost(postId: String, userId: String, newContent: String, newImageUrls: List<String>): Result<Unit> {
        return try {
            val postRef = firestore.collection("posts").document(postId)

            // Kiểm tra quyền sở hữu
            val doc = postRef.get().await()
            val ownerId = doc.getString("userId")

            if (ownerId != userId) {
                return Result.failure(Exception("Bạn không có quyền sửa bài viết này"))
            }

            postRef.update(
                mapOf(
                    "content" to newContent,
                    "imageUrls" to newImageUrls,
                    "isEdited" to true,
                    "editedAt" to System.currentTimeMillis()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 9. RETWEET / UNTWEET
    override suspend fun toggleRetweet(
        postId: String,
        userId: String,
        userName: String,
        userAvatar: String?,
        currentRetweets: List<String>
    ): Result<Unit> {
        return try {
            val isRetweeted = currentRetweets.contains(userId)
            val postRef = firestore.collection("posts").document(postId)

            if (isRetweeted) {
                // Untweet: Xóa khỏi mảng và giảm count
                postRef.update(
                    mapOf(
                        "retweetedBy" to FieldValue.arrayRemove(userId),
                        "retweetCount" to FieldValue.increment(-1)
                    )
                ).await()
            } else {
                // Retweet: Thêm vào mảng, tăng count, và tạo bài retweet
                postRef.update(
                    mapOf(
                        "retweetedBy" to FieldValue.arrayUnion(userId),
                        "retweetCount" to FieldValue.increment(1)
                    )
                ).await()

                // Tạo bài retweet mới
                val originalPostSnapshot = postRef.get().await()
                
                // Kiểm tra xem bài đang retweet có phải là bài repost không
                // Nếu là repost thì lấy thông tin tác giả gốc từ originalUserId/Username/AvatarUrl
                // Nếu không thì lấy từ userId/username/avatarUrl
                val isRepost = originalPostSnapshot.getBoolean("isRepost") ?: false
                val trueOriginalPostId = if (isRepost) {
                    originalPostSnapshot.getString("originalPostId") ?: postId
                } else {
                    postId
                }
                val trueOriginalUserId = if (isRepost) {
                    originalPostSnapshot.getString("originalUserId") ?: originalPostSnapshot.getString("userId")
                } else {
                    originalPostSnapshot.getString("userId")
                }
                val trueOriginalUsername = if (isRepost) {
                    originalPostSnapshot.getString("originalUsername") ?: originalPostSnapshot.getString("username")
                } else {
                    originalPostSnapshot.getString("username")
                }
                val trueOriginalAvatarUrl = if (isRepost) {
                    originalPostSnapshot.getString("originalAvatarUrl") ?: originalPostSnapshot.getString("avatarUrl")
                } else {
                    originalPostSnapshot.getString("avatarUrl")
                }
                
                val retweetMap = hashMapOf(
                    "userId" to userId,
                    "username" to userName,
                    "avatarUrl" to userAvatar,
                    "content" to (originalPostSnapshot.getString("content") ?: ""),
                    "imageUrls" to (originalPostSnapshot.get("imageUrls") as? List<String> ?: emptyList()),
                    "timestamp" to System.currentTimeMillis(),
                    "likesCount" to 0,
                    "commentsCount" to 0,
                    "likedBy" to emptyList<String>(),
                    "isRepost" to true,
                    "originalPostId" to trueOriginalPostId,
                    "originalUserId" to trueOriginalUserId,
                    "originalUsername" to trueOriginalUsername,
                    "originalAvatarUrl" to trueOriginalAvatarUrl,
                    "retweetedBy" to emptyList<String>(),
                    "retweetCount" to 0
                )
                firestore.collection("posts").add(retweetMap).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 10. LẤY BÀI VIẾT CỦA USER (REALTIME)
    override fun getUserPosts(userId: String): Flow<List<SocialPost>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // Launch coroutine để kiểm tra bài gốc
                CoroutineScope(Dispatchers.IO).launch {
                    // Lấy danh sách originalPostId để kiểm tra bài gốc có tồn tại không
                    val originalPostIds = snapshot?.documents
                        ?.filter { it.getBoolean("isRepost") == true }
                        ?.mapNotNull { it.getString("originalPostId") }
                        ?.distinct()
                        ?: emptyList()
                    
                    // Kiểm tra các bài gốc có tồn tại không
                    val deletedOriginalPostIds = mutableSetOf<String>()
                    for (originalPostId in originalPostIds) {
                        try {
                            val originalDoc = firestore.collection("posts").document(originalPostId).get().await()
                            if (!originalDoc.exists()) {
                                deletedOriginalPostIds.add(originalPostId)
                            }
                        } catch (e: Exception) {
                            // Nếu lỗi khi lấy bài gốc, coi như đã bị xóa
                            deletedOriginalPostIds.add(originalPostId)
                        }
                    }

                    val posts = snapshot?.documents?.mapNotNull { doc ->
                        val likedBy = doc.get("likedBy") as? List<String> ?: emptyList()
                        val retweetedBy = doc.get("retweetedBy") as? List<String> ?: emptyList()
                        val images = (doc.get("imageUrls") as? List<*>)?.map { it.toString() } ?: emptyList()
                        val isRepost = doc.getBoolean("isRepost") ?: false
                        val originalPostId = doc.getString("originalPostId")
                        val isOriginalDeleted = isRepost && originalPostId != null && deletedOriginalPostIds.contains(originalPostId)

                        SocialPost(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            username = doc.getString("username") ?: "Ẩn danh",
                            avatarUrl = doc.getString("avatarUrl"),
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            likes = (doc.getLong("likesCount") ?: 0).toInt(),
                            comments = (doc.getLong("commentsCount") ?: 0).toInt(),
                            isLikedByMe = likedBy.contains(currentUserId),
                            imageUrls = images,
                            isRepost = isRepost,
                            originalPostId = originalPostId,
                            originalUserId = doc.getString("originalUserId"),
                            originalUsername = doc.getString("originalUsername"),
                            originalAvatarUrl = doc.getString("originalAvatarUrl"),
                            isOriginalDeleted = isOriginalDeleted,
                            retweetedBy = retweetedBy,
                            retweetCount = (doc.getLong("retweetCount") ?: 0).toInt(),
                            isRetweetedByMe = retweetedBy.contains(currentUserId),
                            canDelete = userId == currentUserId,
                            canEdit = userId == currentUserId
                        )
                    } ?: emptyList()

                    trySend(posts)
                }
            }
        awaitClose { listener.remove() }
    }

    // 11. LẤY BẢNG XẾP HẠNG
    override suspend fun getLeaderboard(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .orderBy("rizzPoints", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                // Map thủ công để tránh lỗi crash nếu thiếu trường
                User(
                    uid = doc.getString("uid") ?: "",
                    email = doc.getString("email"),
                    username = doc.getString("username") ?: "No Name",
                    gender = doc.getString("gender") ?: "",
                    preference = doc.getString("preference") ?: "",
                    rizzPoints = (doc.getLong("rizzPoints") ?: 0).toInt(),
                    loginStreak = (doc.getLong("loginStreak") ?: 0).toInt(),
                    avatarUrl = doc.getString("avatarUrl") ?: ""
                )
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(uri: Uri): Result<String> {
        return try {
            // --- BƯỚC NÉN ẢNH Ở ĐÂY ---
            // Biến uri gốc (nặng) thành uri đã nén (nhẹ)
            val compressedUri = ImageUtils.compressImage(context, uri)
            // ---------------------------

            val filename = UUID.randomUUID().toString()
            val ref = storage.reference.child("images/$filename.jpg")

            ref.putFile(uri).await()
            // Upload ảnh nén thay vì ảnh gốc
            ref.putFile(compressedUri).await() // Dùng compressedUri

            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
