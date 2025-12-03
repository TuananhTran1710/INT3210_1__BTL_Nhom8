package com.example.wink.data.model

data class Quiz(
    val id: String,
    val title: String,
    val description: String,
    val questions: List<Question>
)