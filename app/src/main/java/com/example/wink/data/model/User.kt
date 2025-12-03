package com.example.wink.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val uid: String = "",
    val email: String? = null,
    val username: String = "",
    val gender: String = "", // "male", "female", "other"
    val preference: String = "", // "female"
    val rizzPoints: Int = 0,
    val loginStreak: Int = 0,
    val avatarUrl: String = "",

    val friendsList: List<String> = emptyList(),      // list uid bạn bè
    val quizzesFinished: List<String> = emptyList()   // list quizId đã xong
)
