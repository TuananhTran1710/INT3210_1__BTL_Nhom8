package com.example.wink.ui.features.profile

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.User
import com.example.wink.data.repository.AuthRepository
import com.example.wink.util.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<UserDetailState, UserDetailEvent>() {

    private val userId: String = savedStateHandle.get<String>("userId") ?: ""

    override val uiState: StateFlow<UserDetailState>
        get() = _uiState

    override fun getInitialState(): UserDetailState = UserDetailState()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val user = authRepository.getUserById(userId)

                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        userId = user.uid,
                        username = user.username,
                        email = user.email ?: "",
                        avatarUrl = user.avatarUrl,
                        rizzPoints = user.rizzPoints,
                        loginStreak = user.loginStreak,
                        longestStreak = user.longestStreak,
                        friendCount = user.friendsList.size,
                        isLoading = false,
                        userFound = true
                    )
                    Log.d("UserDetailViewModel", "Loaded user: ${user.username}")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userFound = false,
                        errorMessage = "Không tìm thấy người dùng"
                    )
                    Log.e("UserDetailViewModel", "User not found: $userId")
                }
            } catch (e: Exception) {
                Log.e("UserDetailViewModel", "Error loading user data", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi khi tải thông tin người dùng: ${e.message}"
                )
            }
        }
    }

    override fun onEvent(event: UserDetailEvent) {
        when (event) {
            UserDetailEvent.Refresh -> loadUserData()
            UserDetailEvent.SendMessage -> {
                // TODO: Navigate to chat
            }
        }
    }
}
