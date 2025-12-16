package com.example.wink.ui.features.social

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Comment
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.GameRepository
import com.example.wink.data.repository.SocialRepository
import com.example.wink.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialState())
    val uiState = _uiState.asStateFlow()

    private var currentUser: User? = null
    private var latestPostTimestamp: Long = 0L

    init {
        getCurrentUserInfo()
        loadFeed(isRefresh = false)
        loadLeaderboard()
    }

    private fun getCurrentUserInfo() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                currentUser = user
                _uiState.update {
                    it.copy(currentUserAvatarUrl = user?.avatarUrl ?: "")
                }
            }
        }
    }

    // Hàm load feed chính
    fun loadFeed(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }

            val result = socialRepository.getSocialFeed()

            result.onSuccess { posts ->
                // Cập nhật timestamp của bài mới nhất để lắng nghe
                val firstPostTime = posts.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()

                // Nếu refresh thì cập nhật timestamp mới, nếu không thì giữ nguyên (để tránh miss tin)
                latestPostTimestamp = firstPostTime

                // Bắt đầu lắng nghe tin mới từ mốc này
                startListeningForNewPosts()

                _uiState.update {
                    it.copy(
                        feedList = posts,
                        isLoading = false,
                        isRefreshing = false,
                        hasNewPosts = false // Reset nút báo bài mới
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
            }
        }
    }

    private var newPostsJob: Job? = null

    private fun startListeningForNewPosts() {
        newPostsJob?.cancel()
        newPostsJob = viewModelScope.launch {
            socialRepository.listenForNewPosts(latestPostTimestamp).collectLatest { hasNew ->
                if (hasNew) {
                    _uiState.update { it.copy(hasNewPosts = true) }
                }
            }
        }
    }

    // Khi người dùng ấn nút "Bài viết mới"
    fun onRefreshFeed() {
        loadFeed(isRefresh = true)
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            val result = socialRepository.getLeaderboard()
            if (result.isSuccess) {
                _uiState.update { it.copy(leaderboardList = result.getOrDefault(emptyList())) }
            }
        }
    }
    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    // --- LOGIC BÀI ĐĂNG MỚI ---

    fun onFabClick() {
        _uiState.update { it.copy(isCreatingPost = true) }
    }

    fun onDismissPostDialog() {
        _uiState.update { it.copy(isCreatingPost = false, newPostContent = "") }
    }

    fun onPostContentChange(text: String) {
        _uiState.update { it.copy(newPostContent = text) }
    }
    fun onSendPost() {
        val content = _uiState.value.newPostContent
        val selectedImages = _uiState.value.selectedImageUris
        val user = currentUser
        if ((content.isBlank() && selectedImages.isEmpty()) || user == null) return

        viewModelScope.launch {
            // A. Bật trạng thái đang đăng (để hiện loading xoay xoay)
            _uiState.update { it.copy(isPosting = true) }

            val uploadedImageUrls = mutableListOf<String>()

            // Upload ảnh (nếu có)
            for (uri in selectedImages) {
                val result = socialRepository.uploadImage(uri)
                result.onSuccess { url ->
                    uploadedImageUrls.add(url)
                }
            }

            // Gọi Repository tạo bài
            val result = socialRepository.createPost(content, uploadedImageUrls, user)

            // B. Xử lý kết quả
            if (result.isSuccess) {
                // 1. Đóng Dialog và Reset form ngay lập tức
                _uiState.update {
                    it.copy(
                        isPosting = false,
                        isCreatingPost = false,
                        newPostContent = "",
                        selectedImageUris = emptyList()
                    )
                }

                // 2. QUAN TRỌNG: Gọi tải lại Feed ngay lập tức để bài mới hiện lên đầu
                loadFeed(isRefresh = true)
                taskRepository.updateTaskProgress("POST_FEED")

            } else {
                // Nếu lỗi thì giữ nguyên Dialog để user thử lại
                _uiState.update { it.copy(isPosting = false) }
            }
        }
    }

    fun onLikeClick(postId: String) {
        val user = currentUser ?: return
        val post = _uiState.value.feedList.find { it.id == postId } ?: return

        // Update UI ngay lập tức (Optimistic Update)
        _uiState.update { state ->
            val updatedList = state.feedList.map { post ->
                if (post.id == postId) {
                    val newIsLiked = !post.isLikedByMe
                    val newCount = if (newIsLiked) post.likes + 1 else post.likes - 1
                    post.copy(isLikedByMe = newIsLiked, likes = newCount)
                } else {
                    post
                }
            }
            state.copy(feedList = updatedList)
        }

        viewModelScope.launch {
            // Gọi API thật, truyền vào mảng likedBy hiện tại để biết đường thêm/xóa
            // Lưu ý: Logic toggleLike trong Repo cần danh sách likedBy hoặc userId
            // Ở đây ta truyền rỗng tạm, nhưng đúng ra nên truyền list like cũ
            // Để đơn giản, repository sẽ tự xử lý logic arrayUnion/arrayRemove
            socialRepository.toggleLike(postId, user.uid, if(post.isLikedByMe) listOf(user.uid) else emptyList())
            taskRepository.updateTaskProgress("LIKE_POST")
        }
    }

    fun onOpenCommentSheet(postId: String) {
        _uiState.update { it.copy(activePostId = postId, newCommentContent = "") }

        // Lắng nghe Realtime Comment của bài viết này
        viewModelScope.launch {
            socialRepository.getComments(postId).collectLatest { comments ->
                // Chỉ update nếu đang mở đúng bài post đó
                if (_uiState.value.activePostId == postId) {
                    _uiState.update { it.copy(commentsForActivePost = comments) }
                }
            }
        }
    }

    fun onDismissCommentSheet() {
        _uiState.update { it.copy(activePostId = null) }
    }

    // 3. XỬ LÝ GỬI COMMENT
    fun onCommentContentChange(text: String) {
        _uiState.update { it.copy(newCommentContent = text) }
    }

    fun onSendComment() {
        val content = _uiState.value.newCommentContent
        val postId = _uiState.value.activePostId
        val user = currentUser
        if (content.isBlank() || postId == null || user == null) return

        viewModelScope.launch {
            val result = socialRepository.sendComment(postId, content, user)
            result.onSuccess {
                val fetchResult = socialRepository.getPostById(postId)
                Log.d("SocialViewModel", fetchResult.toString())

                fetchResult.onSuccess { freshPost ->
                    // CẬP NHẬT LẠI LIST VỚI DỮ LIỆU THẬT
                    _uiState.update { state ->
                        state.copy(
                            feedList = state.feedList.updateItem(postId) { freshPost },
                            newCommentContent = ""
                        )
                    }
                }
                taskRepository.updateTaskProgress("COMMENT_POST")
            }.onFailure {
                _uiState.update { state ->
                    state.copy(feedList = state.feedList.updateItem(postId) { it.copy(comments = it.comments - 1) })
                }
            }
        }
    }

    fun List<SocialPost>.updateItem(id: String, block: (SocialPost) -> SocialPost): List<SocialPost> {
        return map { if (it.id == id) block(it) else it }
    }

    fun onImagesSelected(uris: List<Uri>) {
        // Thêm ảnh mới vào danh sách đã có
        _uiState.update { it.copy(selectedImageUris = it.selectedImageUris + uris) }
    }

    fun onRemoveSelectedImage(uri: Uri) {
        _uiState.update { state ->
            state.copy(selectedImageUris = state.selectedImageUris - uri)
        }
    }

    // 4. LIKE / UNLIKE COMMENT
    fun onCommentLikeClick(commentId: String) {
        val postId = _uiState.value.activePostId ?: return
        val user = currentUser ?: return
        val comment = _uiState.value.commentsForActivePost.find { it.id == commentId } ?: return

        // Optimistic update
        _uiState.update { state ->
            val updatedComments = state.commentsForActivePost.map {
                if (it.id == commentId) {
                    val newIsLiked = !it.isLikedByMe
                    val newCount = if (newIsLiked) it.likeCount + 1 else it.likeCount - 1
                    it.copy(isLikedByMe = newIsLiked, likeCount = newCount)
                } else {
                    it
                }
            }
            state.copy(commentsForActivePost = updatedComments)
        }

        viewModelScope.launch {
            socialRepository.toggleCommentLike(postId, commentId, user.uid, if (comment.isLikedByMe) listOf(user.uid) else emptyList())
        }
    }

    // 5. DELETE POST
    fun onDeletePost(postId: String) {
        val user = currentUser ?: return

        // Cập nhật UI NGAY LẬP TỨC (Xóa bài khỏi list đang hiển thị)
        _uiState.update { state ->
            state.copy(feedList = state.feedList.filter { it.id != postId })
        }

        viewModelScope.launch {
            // Sau đó mới gọi Server xóa thật (chạy ngầm)
            socialRepository.deletePost(postId, user.uid)
        }
    }

    // 6. EDIT POST
    fun onEditPost(postId: String, newContent: String, newImageUrls: List<String>) {
        val user = currentUser ?: return

        // Cập nhật UI NGAY LẬP TỨC (Tìm bài đó và thay nội dung mới)
        _uiState.update { state ->
            val updatedList = state.feedList.map { post ->
                if (post.id == postId) {
                    post.copy(content = newContent, imageUrls = newImageUrls)
                } else {
                    post
                }
            }
            state.copy(feedList = updatedList)
        }

        viewModelScope.launch {
            // Gọi Server cập nhật thật (chạy ngầm)
            socialRepository.editPost(postId, user.uid, newContent, newImageUrls)
        }
    }

    // 6.5. EDIT COMMENT
    fun onEditComment(commentId: String, newContent: String) {
        val postId = _uiState.value.activePostId ?: return
        val user = currentUser ?: return

        // Optimistic update
        _uiState.update { state ->
            val updatedComments = state.commentsForActivePost.map {
                if (it.id == commentId) {
                    it.copy(content = newContent, isEdited = true)
                } else {
                    it
                }
            }
            state.copy(commentsForActivePost = updatedComments)
        }

        viewModelScope.launch {
            socialRepository.editComment(postId, commentId, user.uid, newContent)
        }
    }

    // 7. RETWEET / UNTWEET
    fun onRetweetClick(postId: String) {
        val user = currentUser ?: return
        val post = _uiState.value.feedList.find { it.id == postId } ?: return

        // Optimistic update
        _uiState.update { state ->
            val updatedList = state.feedList.map {
                if (it.id == postId) {
                    val newIsRetweeted = !it.isRetweetedByMe
                    val newCount = if (newIsRetweeted) it.retweetCount + 1 else it.retweetCount - 1
                    it.copy(isRetweetedByMe = newIsRetweeted, retweetCount = newCount)
                } else {
                    it
                }
            }
            state.copy(feedList = updatedList)
        }

        viewModelScope.launch {
            socialRepository.toggleRetweet(
                postId,
                user.uid,
                user.username,
                user.avatarUrl,
                if (post.isRetweetedByMe) listOf(user.uid) else emptyList()
            )
        }
    }
}