package com.example.wink.ui.features.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Message
import com.example.wink.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await // Cần import để fetch user info
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore, // Inject thêm Firestore để lấy thông tin User đối diện
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = savedStateHandle.get<String>("chatId") ?: ""

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _chatTitle = MutableStateFlow("")
    val chatTitle: StateFlow<String> = _chatTitle.asStateFlow()

    private val _chatAvatarUrl = MutableStateFlow<String?>(null)
    val chatAvatarUrl: StateFlow<String?> = _chatAvatarUrl.asStateFlow()

    val currentUserId: String
        get() = auth.currentUser!!.uid

    init {
        if (chatId.isNotBlank()) {
            listenMessages()
            loadChatInfo() // Tách logic load info ra hàm riêng
            markAsRead() // Gọi hàm đánh dấu đã đọc
        }
    }
    private fun markAsRead() {
        if (chatId.isBlank()) return
        viewModelScope.launch {
            // Gọi Repository để update DB
            chatRepository.markMessagesAsRead(chatId, currentUserId)
        }
    }
    private fun loadChatInfo() {
        viewModelScope.launch {
            val chat = chatRepository.getChat(chatId)
            if (chat != null) {
                // LOGIC HIỂN THỊ TÊN:
                // Nếu chat có tên cụ thể (Group chat đặt tên) -> dùng tên đó.
                // Nếu không (Chat 1-1), tìm người còn lại trong danh sách participants.
                if (!chat.name.isNullOrBlank()) {
                    _chatTitle.value = chat.name
                    _chatAvatarUrl.value = chat.avatarUrl
                } else {
                    // Chat 1-1, tìm người kia
                    val otherUserId = chat.participants.find { it != currentUserId }
                    if (otherUserId != null) {
                        try {
                            val userDoc = firestore.collection("users").document(otherUserId).get().await()
                            val username = userDoc.getString("username") ?: "Người dùng"
                            val avatar = userDoc.getString("avatarUrl")

                            _chatTitle.value = username
                            _chatAvatarUrl.value = avatar
                        } catch (e: Exception) {
                            _chatTitle.value = "Chat"
                        }
                    } else {
                        _chatTitle.value = "Chat" // Trường hợp chat với chính mình hoặc lỗi
                    }
                }
            }
        }
    }

    private fun listenMessages() {
        viewModelScope.launch {
            chatRepository.listenMessages(chatId).collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(content: String) {
        if (chatId.isBlank()) return
        viewModelScope.launch {
            val chat = chatRepository.getChat(chatId)
            val receiverId = chat?.participants?.firstOrNull { it != currentUserId }

            val message = Message(
                senderId = currentUserId,
                receiverId = receiverId,
                content = content,
                timestamp = System.currentTimeMillis(),
                readBy = listOf(currentUserId),
            )
            chatRepository.sendMessage(chatId, message)
        }
    }
}