package com.example.wink.data.model

import com.google.firebase.firestore.DocumentId

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String? = null,
    val username: String = "",
    val gender: String = "", // "male", "female", "other"
    val preference: String = "", // "female"
    val rizzPoints: Int = 0,
    val loginStreak: Int = 0,
    val avatarUrl: String = "",
    val lastCheckInDate: Timestamp?= null,
    var longestStreak: Int = 0,
    val friends: List<String> = emptyList(),

    val friendsList: List<String> = emptyList(),      // list uid bạn bè
    val quizzesFinished: List<String> = emptyList(),   // list quizId đã xong

    val unlockedTips: List<String> = emptyList()
)
