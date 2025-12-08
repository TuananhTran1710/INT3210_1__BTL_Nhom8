package com.example.wink.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Chat(
    @DocumentId
    @get:Exclude
    val chatId: String = "",
    val name: String = "",
    val participants: List<String> = emptyList(),
    val updatedAt : Long = 0,
    val avatarUrl: String? = null,
)
