package com.example.wink.ui.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Chat
import com.example.wink.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Cập nhật UiChat để chứa thông tin hiển thị đã xử lý
data class UiChat(
    val chat: Chat,
    val lastMessage: String,
    val displayName: String,
    val displayAvatarUrl: String?
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore // Inject thêm Firestore để lấy info user
) : ViewModel() {

    private val _chats = MutableStateFlow<List<UiChat>>(emptyList())
    val chats: StateFlow<List<UiChat>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        loadChats()
    }

    private fun loadChats() {
        val myId = currentUserId ?: return

        viewModelScope.launch {
            _isLoading.value = true

            // Lắng nghe thay đổi từ danh sách chat
            chatRepository.listenChats(myId).collect { rawChats ->
                // Xử lý song song từng đoạn chat để lấy thông tin người dùng
                val processedChats = rawChats.map { chat ->
                    async {
                        // 1. Lấy Last Message
                        val lastMessage = chatRepository.getLastMessage(chat.chatId)?.content ?: ""

                        // 2. Xử lý Tên và Avatar
                        var displayName = chat.name
                        var displayAvatar = chat.avatarUrl

                        // Nếu tên chat rỗng (chat 1-1 chưa đặt tên nhóm), tìm người kia
                        if (chat.name.isBlank()) {
                            val otherUserId = chat.participants.find { it != myId }
                            if (otherUserId != null) {
                                try {
                                    val userSnapshot = firestore.collection("users")
                                        .document(otherUserId)
                                        .get()
                                        .await()

                                    displayName = userSnapshot.getString("username") ?: "Người dùng"
                                    displayAvatar = userSnapshot.getString("avatarUrl")
                                } catch (e: Exception) {
                                    displayName = "Người dùng"
                                }
                            } else {
                                displayName = "Chat riêng"
                            }
                        }

                        // Trả về object UiChat hoàn chỉnh
                        UiChat(
                            chat = chat,
                            lastMessage = lastMessage,
                            displayName = displayName,
                            displayAvatarUrl = displayAvatar
                        )
                    }
                }.awaitAll() // Chờ tất cả các coroutine con chạy xong

                _chats.value = processedChats
                _isLoading.value = false
            }
        }
    }
}