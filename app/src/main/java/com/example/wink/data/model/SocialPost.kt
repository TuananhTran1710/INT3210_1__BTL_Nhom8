package com.example.wink.data.model

data class SocialPost(
    val id: String,
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val content: String, // Ví dụ: "Đã đạt cấp độ RIZZ Thần Thánh!"
    val timestamp: Long, // Thời gian đăng
    val likes: Int = 0,
    val comments: Int = 0
)