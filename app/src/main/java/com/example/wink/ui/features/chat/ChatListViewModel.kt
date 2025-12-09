package com.example.wink.ui.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Chat
import com.example.wink.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiChat(
    val chat: Chat,
    val lastMessage: String
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth
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
        currentUserId?.let {
            viewModelScope.launch {
                _isLoading.value = true
                chatRepository.listenChats(it)
                    .map { chats ->
                        chats.map {  chat ->
                            val lastMessage = chatRepository.getLastMessage(chat.chatId)?.content ?: ""
                            UiChat(chat, lastMessage)
                        }
                    }
                    .collect {
                        _chats.value = it
                        _isLoading.value = false
                    }
            }
        }
    }
}
