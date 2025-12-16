package com.example.wink.ui.features.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var usernameCheckJob: Job? = null

    fun onEvent(event: SignupEvent) {
        when (event) {
            is SignupEvent.OnUsernameChanged -> {
                _uiState.update { 
                    it.copy(
                        username = event.name,
                        usernameError = null,
                        isUsernameValid = false
                    ) 
                }
                checkUsernameWithDebounce(event.name)
            }
            is SignupEvent.OnEmailChanged -> _uiState.update { it.copy(email = event.email) }
            is SignupEvent.OnPasswordChanged -> _uiState.update { it.copy(pass = event.pass) }
            is SignupEvent.OnConfirmPasswordChanged -> _uiState.update { it.copy(confirmPass = event.pass) }
            SignupEvent.OnSignupClicked -> performSignup()
            SignupEvent.OnLoginNavClicked -> viewModelScope.launch {
                _navigationEvent.emit(NavigationEvent.NavigateBackToLogin("", ""))
            }
        }
    }

    private fun checkUsernameWithDebounce(username: String) {
        usernameCheckJob?.cancel()
        
        if (username.isBlank()) {
            _uiState.update { it.copy(isCheckingUsername = false, usernameError = null, isUsernameValid = false) }
            return
        }

        if (username.length < 3) {
            _uiState.update { it.copy(isCheckingUsername = false, usernameError = "Tên phải có ít nhất 3 ký tự", isUsernameValid = false) }
            return
        }

        usernameCheckJob = viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUsername = true, usernameError = null) }
            delay(500) // Debounce 500ms
            
            val exists = authRepository.checkUsernameExists(username)
            if (exists) {
                _uiState.update { it.copy(isCheckingUsername = false, usernameError = "Tên người dùng đã tồn tại", isUsernameValid = false) }
            } else {
                _uiState.update { it.copy(isCheckingUsername = false, usernameError = null, isUsernameValid = true) }
            }
        }
    }

    private fun performSignup() {
        val state = _uiState.value

        // 1. Validate cơ bản
        if (state.email.isBlank() || state.pass.isBlank() || state.username.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng điền đầy đủ thông tin") }
            return
        }

        // 2. Kiểm tra mật khẩu khớp nhau
        if (state.pass != state.confirmPass) {
            _uiState.update { it.copy(error = "Mật khẩu xác nhận không khớp") }
            return
        }

        // 2.5. Kiểm tra username hợp lệ
        if (state.usernameError != null || !state.isUsernameValid) {
            _uiState.update { it.copy(error = state.usernameError ?: "Vui lòng kiểm tra tên người dùng") }
            return
        }

        // 3. Gọi Repository
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.signup(state.email, state.pass, state.username)

            if (result.isSuccess) {
                // Đăng ký thành công -> Chuyển sang màn Onboarding (chọn giới tính)
                _navigationEvent.emit(NavigationEvent.NavigateToOnboarding)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Đăng ký thất bại") }
            }
        }
    }

    sealed class NavigationEvent {
        object NavigateToOnboarding : NavigationEvent()
        data class NavigateBackToLogin(
            val email: String,
            val pass: String
        ) : NavigationEvent()
    }
}