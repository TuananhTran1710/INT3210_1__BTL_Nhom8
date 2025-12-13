package com.example.wink.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        observeUnreadCount()
    }

    private fun observeUnreadCount() {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Lắng nghe danh sách chat thay đổi
            chatRepository.listenChats(currentUserId).collect { chats ->
                // Mỗi khi có chat thay đổi (tin mới, tạo chat mới...), tính lại tổng số chưa đọc
                val countDeferred = chats.map { chat ->
                    async {
                        // Lấy tin nhắn cuối cùng
                        val lastMessage = chatRepository.getLastMessage(chat.chatId)

                        // Logic check chưa đọc y hệt ChatListViewModel
                        if (lastMessage != null) {
                            val readBy = lastMessage.readBy ?: emptyList()
                            // Nếu mình chưa đọc -> trả về 1, ngược lại 0
                            if (!readBy.contains(currentUserId)) 1 else 0
                        } else {
                            0
                        }
                    }
                }

                // Cộng tổng lại
                val totalUnread = countDeferred.awaitAll().sum()
                _unreadCount.value = totalUnread
            }
        }
    }
}