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
    // MỚI: Map lưu thời gian pin của từng user. Key = userId, Value = timestamp
    val pinnedBy: Map<String, Long> = emptyMap(),

    // --- CÁC TRƯỜNG MỚI (Denormalization) ---
    val lastMessage: String = "",         // Nội dung tin nhắn cuối
    val lastSenderId: String = "",        // ID người gửi cuối (để hiện "Bạn: ...")
    val lastReadBy: List<String> = emptyList(), // Danh sách người đã đọc tin cuối

    // Map lưu cache thông tin user: Key = UserId, Value = { "name": "...", "avatar": "..." }
    // Giúp hiển thị tên/avatar người kia mà KHÔNG cần query bảng User
    val participantInfo: Map<String, Map<String, String>> = emptyMap()
)
