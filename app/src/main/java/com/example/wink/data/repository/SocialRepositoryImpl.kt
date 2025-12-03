package com.example.wink.data.repository

import com.example.wink.data.model.Comment
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SocialRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
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

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    val likedBy = doc.get("likedBy") as? List<String> ?: emptyList()
                    SocialPost(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        username = doc.getString("username") ?: "Ẩn danh",
                        avatarUrl = doc.getString("avatarUrl"),
                        content = doc.getString("content") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        likes = (doc.getLong("likesCount") ?: 0).toInt(),
                        comments = (doc.getLong("commentsCount") ?: 0).toInt(),
                        isLikedByMe = likedBy.contains(currentUserId) // Kiểm tra mình đã like chưa
                    )
                } ?: emptyList()

                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    // 2. ĐĂNG BÀI
    override suspend fun createPost(content: String, user: User): Result<Unit> {
        return try {
            val postMap = hashMapOf(
                "userId" to user.uid,
                "username" to user.username,
                "avatarUrl" to user.avatarUrl,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "likesCount" to 0,
                "commentsCount" to 0,
                "likedBy" to emptyList<String>()
            )
            firestore.collection("posts").add(postMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. LIKE / UNLIKE
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
                    Comment(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        username = doc.getString("username") ?: "Ẩn danh",
                        avatarUrl = doc.getString("avatarUrl"),
                        content = doc.getString("content") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
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
                "timestamp" to System.currentTimeMillis()
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

    // 6. LẤY BẢNG XẾP HẠNG
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
}