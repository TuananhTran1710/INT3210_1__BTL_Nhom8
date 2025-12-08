package com.example.wink.ui.features.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Message
import com.example.wink.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
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
            viewModelScope.launch {
                val chat = chatRepository.getChat(chatId)
                // In a real app, you would fetch user profiles to get a better name
                _chatTitle.value = chat?.name ?: "Chat"
                _chatAvatarUrl.value = chat?.avatarUrl
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
