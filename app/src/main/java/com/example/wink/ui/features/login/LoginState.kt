package com.example.wink.ui.features.login

data class LoginState(
    val email: String = "",
    val pass: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCheckingSession: Boolean = true,
)