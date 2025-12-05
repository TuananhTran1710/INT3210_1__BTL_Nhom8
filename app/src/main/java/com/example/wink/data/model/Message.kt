package com.example.wink.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Message(
    @DocumentId
    @get:Exclude
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String? = null,
    val content: String = "",
    val timestamp: Long = 0,
    val readBy: List<String>? = null,
    val mediaUrl: List<String>? = null
)
