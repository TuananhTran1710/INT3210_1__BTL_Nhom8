package com.example.wink.ui.features.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository // Hilt sẽ tự động tiêm AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    // Một "kênh" đặc biệt để gửi tín hiệu điều hướng (navigation) 1 LẦN
//    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
//    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(replay = 1)
    val navigationEvent = _navigationEvent.asSharedFlow()


    init {
        Log.d("TestAuth", "firebaseAuth = $authRepository")
        autoLoginIfPossible()
    }

    private fun autoLoginIfPossible() {
        viewModelScope.launch {
            if (authRepository.hasLoggedInUser()) {
                // ĐÃ có user từ phiên trước → nhảy thẳng Dashboard
                _navigationEvent.emit(NavigationEvent.NavigateToMain)
                // Không cần set isCheckingSession = false vì sẽ navigate đi luôn
            } else {
                // KHÔNG có user → thôi hiện form login
                _uiState.update { it.copy(isCheckingSession = false) }
            }
        }
    }


    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailChanged -> {
                _uiState.update { it.copy(email = event.email) }
            }
            is LoginEvent.OnPasswordChanged -> {
                _uiState.update { it.copy(pass = event.pass) }
            }
            LoginEvent.OnLoginClicked -> {
                loginUser()
            }
        }
    }

    private fun loginUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.login(_uiState.value.email, _uiState.value.pass)

            if (result.isSuccess) {
                // Đăng nhập thành công, gửi tín hiệu để chuyển màn hình
                _navigationEvent.emit(NavigationEvent.NavigateToMain)
            } else {
                // Thất bại
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }

    // Lớp kín (sealed class) cho các sự kiện điều hướng
    sealed class NavigationEvent {
        object NavigateToMain : NavigationEvent()
    }
}