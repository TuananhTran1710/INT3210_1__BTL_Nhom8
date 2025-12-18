package com.example.wink.ui.features.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val uid: String = "",
    val username: String = "",
    val originalUsername: String = "", // Tên hiện tại để so sánh
    val email: String = "",
    val currentAvatarUrl: String = "",
    val selectedAvatarUri: Uri? = null,

    // --- Trạng thái kiểm tra Username ---
    val isCheckingUsername: Boolean = false,
    val usernameError: String? = null,
    val isUsernameValid: Boolean = true, // Mặc định true vì ban đầu là tên cũ hợp lệ

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

    private var usernameCheckJob: Job? = null

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
                            originalUsername = it.username, // Gán để biết tên cũ là gì
                            email = it.email ?: "",
                            currentAvatarUrl = it.avatarUrl,
                            gender = it.gender,
                            preference = it.preference,
                            isUsernameValid = true
                        )
                    }
                }
            }
        }
    }

    fun onUsernameChange(v: String) {
        _uiState.update { it.copy(username = v, usernameError = null, isUsernameValid = false) }
        checkUsernameWithDebounce(v)
    }

    private fun checkUsernameWithDebounce(username: String) {
        usernameCheckJob?.cancel() // Hủy bỏ lần kiểm tra trước đó nếu người dùng vẫn đang gõ

        // 1. Kiểm tra trống
        if (username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Tên không được để trống", isUsernameValid = false) }
            return
        }

        // 2. Nếu trùng với tên cũ của chính mình -> Hợp lệ ngay lập tức
        if (username == _uiState.value.originalUsername) {
            _uiState.update { it.copy(usernameError = null, isUsernameValid = true, isCheckingUsername = false) }
            return
        }

        // 3. Kiểm tra độ dài
        if (username.length < 3) {
            _uiState.update { it.copy(usernameError = "Tên phải có ít nhất 3 ký tự", isUsernameValid = false) }
            return
        }

        // 4. Kiểm tra trên server sau một khoảng thời gian chờ (Debounce)
        usernameCheckJob = viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUsername = true) }
            delay(500) // Chờ 500ms

            val exists = authRepository.checkUsernameExists(username)
            if (exists) {
                _uiState.update { it.copy(isCheckingUsername = false, usernameError = "Tên người dùng đã tồn tại", isUsernameValid = false) }
            } else {
                _uiState.update { it.copy(isCheckingUsername = false, usernameError = null, isUsernameValid = true) }
            }
        }
    }

    fun onAvatarSelected(uri: Uri) = _uiState.update { it.copy(selectedAvatarUri = uri) }

    fun clearMessage() = _uiState.update { it.copy(successMessage = null, errorMessage = null) }

    fun onSaveProfile() {
        val state = _uiState.value

        // Ngăn lưu nếu dữ liệu không hợp lệ
        if (!state.isUsernameValid || state.isCheckingUsername) {
            _uiState.update { it.copy(errorMessage = state.usernameError ?: "Vui lòng kiểm tra lại tên hiển thị") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // 1. Xử lý ảnh đại diện
                var finalAvatarUrl = state.currentAvatarUrl
                if (state.selectedAvatarUri != null) {
                    val uploadResult = authRepository.uploadAvatar(state.selectedAvatarUri)
                    uploadResult.onSuccess { url -> finalAvatarUrl = url }
                    uploadResult.onFailure { throw it }
                }

                // 2. Gọi Repository để cập nhật Profile
                authRepository.updateUserProfile(
                    uid = state.uid,
                    username = state.username,
                    avatarUrl = finalAvatarUrl
                ).getOrThrow()

                // 3. Cập nhật State khi thành công
                _uiState.update { s ->
                    s.copy(
                        isLoading = false,
                        successMessage = "Cập nhật thành công!",
                        originalUsername = state.username, // Quan trọng: Cập nhật tên mới làm mốc so sánh
                        currentAvatarUrl = finalAvatarUrl,
                        selectedAvatarUri = null,
                        isUsernameValid = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi khi lưu: ${e.message}") }
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