package com.example.wink.data.model

import com.google.firebase.firestore.Exclude
data class Question(
    @get:Exclude @Exclude
    val id: String = "",
    val text: String = "",
    val answers: List<Answer> = emptyList(),
    val correctIndex: Int = -1
)