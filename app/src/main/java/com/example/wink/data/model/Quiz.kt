package com.example.wink.data.model

import com.google.firebase.firestore.Exclude

data class Quiz(
    @get:Exclude @Exclude
    val id: String = "",

    val title: String = "",
    val description: String = "",
    val rizzUnlockCost: Int = 0,

    @get:Exclude @Exclude
    val questions: List<Question> = emptyList()
)