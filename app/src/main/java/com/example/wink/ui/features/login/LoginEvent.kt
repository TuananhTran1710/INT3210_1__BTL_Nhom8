package com.example.wink.ui.features.login

sealed class LoginEvent {
    data class OnEmailChanged(val email: String) : LoginEvent()
    data class OnPasswordChanged(val pass: String) : LoginEvent()
    object OnLoginClicked : LoginEvent()
}