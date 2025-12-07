package com.example.wink.ui.features.profile

data class UserDetailState(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val rizzPoints: Int = 0,
    val loginStreak: Int = 0,
    val longestStreak: Int = 0,
    val friendCount: Int = 0,
    val isLoading: Boolean = false,
    val userFound: Boolean = false,
    val errorMessage: String? = null
)

sealed class UserDetailEvent {
    object Refresh : UserDetailEvent()
    object SendMessage : UserDetailEvent()
}
