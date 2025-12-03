package com.example.wink.ui.features.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Comment
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialState())
    val uiState = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        getCurrentUserInfo()
        loadFeed()
        loadLeaderboard()
    }

    private fun getCurrentUserInfo() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                currentUser = user
            }
        }
    }

    private fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Lắng nghe Realtime Feed
            socialRepository.getSocialFeed().collectLatest { posts ->
                _uiState.update {
                    it.copy(feedList = posts, isLoading = false)
                }
            }
        }
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
        val user = currentUser
        if (content.isBlank() || user == null) return

        viewModelScope.launch {
            // Gọi Repo tạo post
            socialRepository.createPost(content, user)

            // Reset UI
            _uiState.update { it.copy(isCreatingPost = false, newPostContent = "") }
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
            socialRepository.sendComment(postId, content, user)
            _uiState.update { it.copy(newCommentContent = "") }
        }
    }
}