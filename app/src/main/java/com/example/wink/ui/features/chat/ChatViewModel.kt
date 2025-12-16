package com.example.wink.ui.features.chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Message
import com.example.wink.data.repository.ChatRepository
import com.example.wink.data.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await // Cần import để fetch user info
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val taskRepository: TaskRepository,
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
    // THÊM HÀM NÀY
    fun sendImage(uri: android.net.Uri) {
        if (chatId.isBlank()) return

        viewModelScope.launch {
            // 1. Upload ảnh trước
            val uploadResult = chatRepository.uploadImage(uri)

            uploadResult.onSuccess { imageUrl ->
                val chat = chatRepository.getChat(chatId)
                val receiverId = chat?.participants?.firstOrNull { it != currentUserId }

                // 2. Tạo message với mediaUrl
                val message = Message(
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = "Đã gửi một ảnh", // Nội dung hiển thị ở danh sách chat (Last Message)
                    timestamp = System.currentTimeMillis(),
                    readBy = listOf(currentUserId),
                    mediaUrl = listOf(imageUrl) // Lưu URL vào list
                )

                // 3. Gửi tin nhắn
                chatRepository.sendMessage(chatId, message)
                taskRepository.updateTaskProgress("CHAT_FRIEND")
            }.onFailure {
                // Xử lý lỗi upload (ví dụ: Toast lỗi)
                it.printStackTrace()
            }
        }
    }
    // SỬA HÀM NÀY
    private fun listenMessages() {
        viewModelScope.launch {
            chatRepository.listenMessages(chatId).collect { newMessages ->
                _messages.value = newMessages

                // --- FIX BUG: Tự động đánh dấu đã đọc khi có tin nhắn mới đến ---
                if (newMessages.isNotEmpty()) {
                    // Vì query trong Repository là DESCENDING (Mới nhất lên đầu)
                    // Nên phần tử đầu tiên (index 0) là tin nhắn mới nhất
                    val newestMessage = newMessages.first()

                    // Kiểm tra: Nếu mình chưa đọc tin nhắn mới nhất này -> Gọi markAsRead ngay
                    val amIRead = newestMessage.readBy?.contains(currentUserId) == true

                    if (!amIRead) {
                        markAsRead()
                    }
                }
            }
        }
    }

    // --- HÀM GỬI TIN NHẮN MỚI (Xử lý Text + Nhiều Ảnh) ---
    fun sendMessage(content: String, imageUris: List<Uri>) {
        if (chatId.isBlank()) return

        // 1. Gửi Text (nếu có) - Chạy luồng riêng
        if (content.isNotBlank()) {
            viewModelScope.launch {
                sendTextMessage(content)
            }
        }

        // 2. Gửi Ảnh (Duyệt list và upload song song)
        if (imageUris.isNotEmpty()) {
            imageUris.forEach { uri ->
                viewModelScope.launch {
                    sendImageMessage(uri)
                }
            }
        }

        // Cập nhật nhiệm vụ (chỉ cần gọi 1 lần)
        viewModelScope.launch {
            taskRepository.updateTaskProgress("CHAT_FRIEND")
        }
    }
    // --- Hàm con: Gửi Text ---
    private suspend fun sendTextMessage(content: String) {
        val chat = chatRepository.getChat(chatId) ?: return
        val receiverId = chat.participants.firstOrNull { it != currentUserId }

        val message = Message(
            messageId = UUID.randomUUID().toString(), // Tạo ID client-side để quản lý UI nếu cần
            senderId = currentUserId,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis(),
            readBy = listOf(currentUserId),
            mediaUrl = null
        )
        chatRepository.sendMessage(chatId, message)
    }

    // --- Hàm con: Upload & Gửi Ảnh ---
    private suspend fun sendImageMessage(uri: Uri) {
        // 1. Upload ảnh
        val uploadResult = chatRepository.uploadImage(uri)

        uploadResult.onSuccess { imageUrl ->
            val chat = chatRepository.getChat(chatId)
            val receiverId = chat?.participants?.firstOrNull { it != currentUserId }

            // 2. Tạo tin nhắn ảnh
            val message = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = currentUserId,
                receiverId = receiverId,
                content = "Đã gửi một ảnh", // Nội dung hiển thị ở danh sách chat (Last Message)
                timestamp = System.currentTimeMillis(),
                readBy = listOf(currentUserId),
                mediaUrl = listOf(imageUrl) // Lưu URL ảnh vào list
            )

            // 3. Gửi tin nhắn
            chatRepository.sendMessage(chatId, message)
        }.onFailure {
            it.printStackTrace()
        }
    }
}