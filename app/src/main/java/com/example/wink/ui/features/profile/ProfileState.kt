package com.example.wink.ui.features.profile

data class FriendUi(
    val id: String,
    val name: String = "Unknown",
    val avatarUrl: String = "",
    val rizzPoints: Int = 0,
    val isFriend: Boolean = true
)
data class ProfileState (
    val username: String = "",
    val avatarUrl: String? = null,
    val rizzPoint: Int = 0,
    val friendCount: Int = 0,
    val friends: List<String> = emptyList(), // Friend IDs
    val loadedFriends: List<FriendUi> = emptyList(), // Loaded friend data
    val userEmail: String = "Đang tải...",
    val rizzPoints: Int = 0,
    val dailyStreak: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val longestStreak: Int = 0,

    val isLoggedOut: Boolean = false
)