package com.example.wink.ui.features.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    // Đã xóa bio
    val currentAvatarUrl: String = "",
    val selectedAvatarUri: Uri? = null,

    val gender: String = "",
    val preference: String = "",

    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            uid = it.uid,
                            username = it.username,
                            email = it.email ?: "",
                            currentAvatarUrl = it.avatarUrl,
                            gender = it.gender,
                            preference = it.preference
                        )
                    }
                }
            }
        }
    }

    fun onUsernameChange(v: String) = _uiState.update { it.copy(username = v) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v) }
    fun onAvatarSelected(uri: Uri) = _uiState.update { it.copy(selectedAvatarUri = uri) }
    fun clearMessage() = _uiState.update { it.copy(successMessage = null, errorMessage = null) }

    fun onSaveProfile() {
        val state = _uiState.value
        if (state.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Tên không được để trống") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // 1. Upload ảnh (Nếu có chọn ảnh mới)
                var finalAvatarUrl = state.currentAvatarUrl
                if (state.selectedAvatarUri != null) {
                    val uploadResult = authRepository.uploadAvatar(state.selectedAvatarUri)
                    uploadResult.onSuccess { url -> finalAvatarUrl = url }
                    uploadResult.onFailure { throw it }
                }

                // 2. Cập nhật thông tin (username, avatar)
                authRepository.updateUserProfile(
                    uid = state.uid,
                    username = state.username,
                    avatarUrl = finalAvatarUrl
                ).getOrThrow()

//                // 3. Cập nhật Email (Nếu thay đổi)
//                if (state.email.isNotBlank() && state.email != authRepository.currentUser.toString()) {
//                    // Logic check email thay đổi ở đây chỉ là ví dụ, thực tế cần so sánh với email gốc
//                    authRepository.updateEmail(state.email).onFailure {
//                        _uiState.update { s -> s.copy(errorMessage = "Lưu thông tin OK, nhưng lỗi đổi email: ${it.message}") }
//                        // return@launch // Không return để vẫn báo thành công phần kia
//                    }
//                }

                _uiState.update { it.copy(isLoading = false, successMessage = "Cập nhật thành công!", selectedAvatarUri = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi: ${e.message}") }
            }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}