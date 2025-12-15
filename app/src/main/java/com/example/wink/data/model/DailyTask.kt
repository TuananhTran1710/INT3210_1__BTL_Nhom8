package com.example.wink.data.model
import com.google.firebase.firestore.PropertyName
data class DailyTask(
    val id: String = "",         // Dùng tên Enum làm ID (VD: "CHAT_AI")
    val type: String = "",       // Lưu tên Enum
    val title: String = "",
    val description: String = "",
    val currentProgress: Int = 0,
    val target: Int = 1,
    val reward: Int = 0,
    @get:PropertyName("isCompleted")
    val isCompleted: Boolean = false,
    @get:PropertyName("isClaimed")
    val isClaimed: Boolean = false,
)