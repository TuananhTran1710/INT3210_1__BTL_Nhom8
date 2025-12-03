package com.example.wink.data.model

data class Question(
    val id: String,
    val text: String,
    val answers: List<Answer>,
    val correctIndex: Int
)