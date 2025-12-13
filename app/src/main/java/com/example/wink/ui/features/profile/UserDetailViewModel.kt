package com.example.wink.ui.features.profile

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.ChatRepository
import com.example.wink.data.repository.FriendRequestRepository
import com.example.wink.data.repository.FriendRequestStatus
import com.example.wink.data.repository.SocialRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
// 2. Định nghĩa sự kiện (Side Effect) để điều hướng
sealed class UserDetailEffect {
    data class NavigateToChat(val chatId: String) : UserDetailEffect()
    data class ShowError(val message: String) : UserDetailEffect()
}
data class UserDetailState(
    val user: User? = null,
    val userPosts: List<SocialPost> = emptyList(),
    val isLoading: Boolean = true,
    val friendRequestStatus: FriendRequestStatus = FriendRequestStatus.NOT_SENT,
    val isSendingRequest: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isOwnProfile: Boolean = false

)

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firestore: FirebaseFirestore,
    private val socialRepository: SocialRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository // 3
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(UserDetailState())
    val uiState = _uiState.asStateFlow()
    // 4. Channel để gửi sự kiện điều hướng ra UI
    private val _effect = Channel<UserDetailEffect>()
    val effect = _effect.receiveAsFlow()
    private var currentUser: User? = null

    init {
        loadCurrentUser()
        loadUserProfile()
        loadUserPosts()
        checkFriendRequestStatus()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                currentUser = user
                // Check if viewing own profile
                val isOwn = user?.uid == userId
                _uiState.update { it.copy(isOwnProfile = isOwn) }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Lấy thông tin user từ Firestore
                val snapshot = firestore.collection("users").document(userId).get().await()
                if (snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)?.copy(uid = snapshot.id)
                    _uiState.update { it.copy(user = user, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private var postsJob: kotlinx.coroutines.Job? = null

    private fun loadUserPosts() {
        // Cancel previous job to avoid multiple listeners
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            try {
                socialRepository.getUserPosts(userId).collectLatest { posts ->
                    _uiState.update { it.copy(userPosts = posts) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkFriendRequestStatus() {
        viewModelScope.launch {
            try {
                val status = friendRequestRepository.checkRequestStatus(userId)
                _uiState.update { it.copy(friendRequestStatus = status) }
                Log.d("UserDetailViewModel", "Friend request status: $status")
            } catch (e: Exception) {
                Log.e("UserDetailViewModel", "Error checking friend request status", e)
            }
        }
    }

    fun sendFriendRequest() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isSendingRequest = true,
                    errorMessage = null
                )
            }

            try {
                friendRequestRepository.sendFriendRequest(
                    toUserId = userId,
                    fromUsername = user.username,
                    fromAvatarUrl = user.avatarUrl
                ).getOrThrow()

                _uiState.update {
                    it.copy(
                        isSendingRequest = false,
                        friendRequestStatus = FriendRequestStatus.REQUEST_SENT,
                        successMessage = "Đã gửi lời mời kết bạn"
                    )
                }
                Log.d("UserDetailViewModel", "Friend request sent successfully")
            } catch (e: Exception) {
                Log.e("UserDetailViewModel", "Error sending friend request", e)
                _uiState.update {
                    it.copy(
                        isSendingRequest = false,
                        errorMessage = e.message ?: "Không thể gửi lời mời kết bạn"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { 
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    // 5. Cập nhật logic hàm sendMessage
    fun sendMessage() {
        val myUser = currentUser ?: return
        val targetUserId = userId

        viewModelScope.launch {
            // Có thể hiển thị loading nếu muốn (tùy chọn)
            // _uiState.update { it.copy(isLoading = true) }

            // Gọi Repository để tìm hoặc tạo chat 1-1
            val result = chatRepository.findOrCreatePrivateChat(myUser.uid, targetUserId)

            // Tắt loading (nếu có bật)
            // _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { chatId ->
                // Thành công -> Bắn sự kiện điều hướng kèm chatId
                _effect.send(UserDetailEffect.NavigateToChat(chatId))
            }.onFailure { e ->
                // Thất bại -> Báo lỗi
                _effect.send(UserDetailEffect.ShowError("Lỗi tạo chat: ${e.message}"))
            }
        }
    }
}