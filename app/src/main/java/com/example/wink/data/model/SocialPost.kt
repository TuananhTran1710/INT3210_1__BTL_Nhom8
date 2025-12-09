package com.example.wink.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class SocialPost(
    val id: String,
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val content: String,
    val timestamp: Long,
    val likes: Int = 0,
    val comments: Int = 0,
    val isLikedByMe: Boolean = false,
    val imageUrls: List<String> = emptyList(),
)