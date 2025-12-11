package com.example.wink.data.model

import com.google.firebase.Timestamp

data class FriendRequest(
    val uid: String = "",         // ID người gửi
    val displayName: String = "", // Tên người gửi
    val avatarUrl: String = "",   // Ảnh người gửi
    val status: String = "",      // "pending"
    val timestamp: Timestamp? = null
)
