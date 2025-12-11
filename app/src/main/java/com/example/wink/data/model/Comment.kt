package com.example.wink.data.model

data class Comment(
    val id: String,
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val content: String,
    val timestamp: Long,
    val likedBy: List<String> = emptyList(),
    val likeCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isEdited: Boolean = false,
    val canEdit: Boolean = false
)