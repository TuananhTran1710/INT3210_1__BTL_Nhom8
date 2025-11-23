package com.example.wink.ui.features.profile

data class FriendUi(
    val id: String,
    val name: String? = null,
    val avatarUrl: String? = null,
    val isFriend: Boolean = true
)
data class ProfileState (
    val username: String = "",
    val avatarUrl: String? = null,
    val rizzPoint: Int = 0,
    val friendCount: Int = 0,
    val friends: List<FriendUi> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val isLoggedOut: Boolean = false
)