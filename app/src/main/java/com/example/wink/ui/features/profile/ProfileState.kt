package com.example.wink.ui.features.profile

import com.example.wink.data.model.SocialPost // Import SocialPost thật

data class FriendUi(
    val id: String,
    val name: String? = null,
    val avatarUrl: String? = null,
    val isFriend: Boolean = true,
    val rizzPoints: Int = 0
)

data class ProfileState(
    val username: String = "",
    val avatarUrl: String? = null,
    val rizzPoints: Int = 0,
    val friendCount: Int = 0,
    val friends: List<String> = emptyList(),
    val posts: List<SocialPost> = emptyList(),
    val loadedFriends: List<FriendUi> = emptyList(), // Loaded friend data
    val userEmail: String = "Đang tải...",
    val dailyStreak: Int = 0,
    val longestStreak: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedOut: Boolean = false
)