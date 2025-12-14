package com.example.wink.ui.features.profile

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.ChatRepository
import com.example.wink.data.repository.SocialRepository
import com.example.wink.util.BaseViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.TimeZone
import javax.inject.Inject
import kotlin.String


// Định nghĩa Sealed Class cho các sự kiện điều hướng (Side Effects)
sealed class ProfileEffect {
    data class NavigateToChat(val chatId: String) : ProfileEffect()
    data class ShowError(val message: String) : ProfileEffect()

}
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val socialRepository: SocialRepository,
    private val chatRepository: ChatRepository // Inject thêm ChatRepository
) : BaseViewModel<ProfileState, ProfileEvent>() {

    override val uiState: StateFlow<ProfileState>
        get() = _uiState

    // Channel để bắn sự kiện ra UI (chỉ nhận 1 lần)
    private val _effect = Channel<ProfileEffect>()
    val effect = _effect.receiveAsFlow()

    override fun getInitialState(): ProfileState = ProfileState(
        // state ban đầu

    )

    private var currentUserId: String? = null

    init{
        observeUser()
    }
    // load dữ liệu ban đầu
    private fun observeUser() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                val current = _uiState.value
                val friendIds = user?.friendsList ?: emptyList()
                
                _uiState.value = current.copy(
                    userEmail = user?.email ?: "Không tìm thấy Email",
                    username = user?.username ?: user?.email?.substringBefore("@") ?: "Người dùng",
                    rizzPoints = user?.rizzPoints ?: current.rizzPoints,
                    dailyStreak = user?.loginStreak ?: current.dailyStreak,
                    longestStreak = user?.longestStreak?:current.longestStreak,
                    friends = friendIds,
                    avatarUrl = user?.avatarUrl?:current.avatarUrl,
                    isLoading = false
                )

                // Load friends data when user changes (always reload to reflect unfriend changes)
                loadFriends(friendIds)

                // Load user posts
                user?.uid?.let { userId ->
                    if (currentUserId != userId) {
                        currentUserId = userId
                        loadUserPosts(userId)
                    }
                }
            }
        }
    }

    private var postsJob: kotlinx.coroutines.Job? = null

    private fun loadUserPosts(userId: String) {
        // Cancel previous job to avoid multiple listeners
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            try {
                socialRepository.getUserPosts(userId).collectLatest { posts ->
                    _uiState.value = _uiState.value.copy(posts = posts)
                    Log.d("ProfileViewModel", "Loaded ${posts.size} posts")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading posts", e)
            }
        }
    }

    override fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.Refresh -> {
                refresh()
            }
            ProfileEvent.LogoutClick -> {
                logout()
            }
            is ProfileEvent.AddFriendClick -> {
                addFriend(event.friendId)
            }
            is ProfileEvent.MessageClick -> {
                // Xử lý khi ấn nút nhắn tin
                openChat(event.friendId)
            }
        }
    }
    private fun refresh () {

    }
    private fun logout() {
        viewModelScope.launch {
            try {
                // hiển thị loading nếu muốn
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                authRepository.logout()      // <-- gọi repo đăng xuất (Firebase, v.v.)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedOut = true        // báo cho UI biết là đã logout
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Đăng xuất thất bại"
                )
            }
        }
    }

    private fun addFriend (friendId: String) {

    }

    private fun openChat(targetUserId: String) {
        val myId = currentUserId
        if (myId == null) {
            viewModelScope.launch { _effect.send(ProfileEffect.ShowError("Vui lòng đăng nhập lại")) }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Gọi Repo tìm hoặc tạo chat
            val result = chatRepository.findOrCreatePrivateChat(myId, targetUserId)

            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { chatId ->
                // Bắn sự kiện điều hướng kèm chatId
                _effect.send(ProfileEffect.NavigateToChat(chatId))
            }.onFailure { e ->
                _effect.send(ProfileEffect.ShowError("Không thể tạo cuộc trò chuyện: ${e.message}"))
            }
        }
    }

    private fun loadFriends(friendIds: List<String>) {
        viewModelScope.launch {
            try {
                // Handle empty friends list
                if (friendIds.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        loadedFriends = emptyList(),
                        friendCount = 0
                    )
                    Log.d("ProfileViewModel", "Friends list is empty")
                    return@launch
                }

                // Get user data for all friend IDs
                val users = authRepository.getUsersByIds(friendIds)

                // Convert to FriendUi
                val loadedFriends = users.map { user ->
                    FriendUi(
                        id = user.uid,
                        name = user.username,
                        avatarUrl = user.avatarUrl,
                        rizzPoints = user.rizzPoints,
                        isFriend = true
                    )
                }

                _uiState.value = _uiState.value.copy(
                    loadedFriends = loadedFriends,
                    friendCount = loadedFriends.size
                )

                Log.d("ProfileViewModel", "Loaded ${loadedFriends.size} friends")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading friends", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Không thể tải danh sách bạn bè"
                )
            }
        }
    }
}
