package com.example.wink.ui.features.signup

data class SignupState(
    val username: String = "",
    val email: String = "",
    val pass: String = "",
    val confirmPass: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCheckingUsername: Boolean = false,
    val usernameError: String? = null,
    val isUsernameValid: Boolean = false
)