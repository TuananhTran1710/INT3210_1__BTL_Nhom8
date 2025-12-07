package com.example.wink.ui.features.profile

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import com.example.wink.util.BaseViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.util.TimeZone
import javax.inject.Inject
import kotlin.String



@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<ProfileState, ProfileEvent>() {

    override val uiState: StateFlow<ProfileState>
        get() = _uiState

    override fun getInitialState(): ProfileState = ProfileState(
        // state ban đầu

    )

    init{
        observeUser()
        }
        // load dữ liệu ban đầu
        private fun observeUser() {
            viewModelScope.launch {
                authRepository.currentUser.collectLatest { user ->
                    val current = _uiState.value
                    _uiState.value = current.copy(
                        userEmail = user?.email ?: "Không tìm thấy Email",
                        username = user?.username ?: user?.email?.substringBefore("@") ?: "Người dùng",
                        rizzPoints = user?.rizzPoints ?: current.rizzPoints,
                        dailyStreak = user?.loginStreak ?: current.dailyStreak,
                        longestStreak = user?.longestStreak?:current.longestStreak,
                        friends = user?.friendsList?:current.friends,
                        avatarUrl = user?.avatarUrl?:current.avatarUrl,
                        isLoading = false
                    )
                    
                    // Load friends data when user changes
                    user?.friendsList?.let { friendIds ->
                        if (friendIds.isNotEmpty()) {
                            loadFriends(friendIds)
                        }
                    }
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

    private fun openChat (friendId: String) {

    }

    private fun loadFriends(friendIds: List<String>) {
        viewModelScope.launch {
            try {
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
