package com.example.wink.data.model

data class User(
    val uid: String,
    val email: String?,
    val username: String,
    val gender: String, // "male", "female", "other"
    val preference: String, // "female"
    val rizzPoints: Int = 0,
    val loginStreak: Int = 0
)