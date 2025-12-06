package com.example.wink.ui.features.profile

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.SocialRepository
import com.example.wink.util.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import kotlin.String



@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val socialRepository: SocialRepository
) : BaseViewModel<ProfileState, ProfileEvent>() {

    override val uiState: StateFlow<ProfileState>
        get() = _uiState

    override fun getInitialState(): ProfileState = ProfileState()

    init{
        userInit()
        loadFriends()

        }
        // load dữ liệu ban đầu


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
    private fun userInit () {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _uiState.value = _uiState.value.copy(
                    username = user?.username ?: "Chưa đăng nhập",
                    avatarUrl = user?.avatarUrl,
                    rizzPoint = user?.rizzPoints ?: 0,
                    // Reset nếu logout
                    friends = if (user == null) emptyList() else _uiState.value.friends
                )

                // SAU KHI CÓ USER -> LẤY BÀI VIẾT
                if (user != null) {
                    loadUserPosts(user.uid)
                }
            }
        }

        Log.i("user info", "user info: ${uiState.value}")
    }
    private fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            socialRepository.getUserPosts(userId).collectLatest { posts ->
                _uiState.value = _uiState.value.copy(posts = posts)
            }
        }
    }
    private fun loadFriends()  {
        viewModelScope.launch {
            val lstFriends = null
//            _uiState.value = _uiState.value.copy(friends = lstFriends, friendCount = lstFriends.size)
        }
    }
}
