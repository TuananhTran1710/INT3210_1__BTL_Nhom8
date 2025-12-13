package com.example.wink.ui.features.chat

data class NewChatState(
    val searchQuery: String = "",
    val allFriends: List<NewChatFriendUi> = emptyList(), // Danh sách gốc
    val filteredFriends: List<NewChatFriendUi> = emptyList(), // Danh sách hiển thị (đã lọc)
    val isLoading: Boolean = false,
    val error: String? = null
)

// Model UI riêng cho màn hình này cho gọn
data class NewChatFriendUi(
    val uid: String,
    val username: String,
    val avatarUrl: String?,
    val email: String?
)