package com.example.wink.data.model

data class Comment(
    val id: String,
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val content: String,
    val timestamp: Long
)