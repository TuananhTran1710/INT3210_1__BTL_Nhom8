package com.example.wink.ui.features.signup

sealed class SignupEvent {
    data class OnUsernameChanged(val name: String) : SignupEvent()
    data class OnEmailChanged(val email: String) : SignupEvent()
    data class OnPasswordChanged(val pass: String) : SignupEvent()
    data class OnConfirmPasswordChanged(val pass: String) : SignupEvent()
    object OnSignupClicked : SignupEvent()
    object OnLoginNavClicked : SignupEvent() // Để quay lại màn đăng nhập
}