package com.example.wink.ui.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.BuildConfig
import com.example.wink.data.model.Message
import com.example.wink.data.remote.ChatGptApiService
import com.example.wink.data.remote.ChatGptMessage
import com.example.wink.data.remote.ChatGptRequest
import com.example.wink.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MessageViewModelForAI @Inject constructor(
    private val chatGptApiService: ChatGptApiService,
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    private val aiUserId = "wink-ai-assistant"
    private val currentUserId: String
        get() = auth.currentUser!!.uid

    private val systemPrompt = ChatGptMessage(
        role = "system",
        content = "Bạn là một trợ lý AI hữu ích tên là Wink, được tích hợp trong một ứng dụng mạng xã hội. Hãy trả lời một cách thân thiện, ngắn gọn và hữu ích."
    )

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            val initialMessages = chatRepository.listenAiMessages(currentUserId).first()
            if (initialMessages.isEmpty()) {
                val welcomeMessage = Message(
                    messageId = UUID.randomUUID().toString(),
                    senderId = aiUserId,
                    content = "Xin chào! Tôi là Trợ lý AI của Wink. Bạn cần giúp gì nào?",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = listOf(welcomeMessage)
            } else {
                _messages.value = initialMessages
            }
            _isLoading.value = false
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            _isSending.value = true
            val userMessage = Message(
                senderId = currentUserId,
                content = content,
                timestamp = System.currentTimeMillis(),
            )
            // Add user message to UI immediately
            _messages.value = listOf(userMessage) + _messages.value
            // Save user message to Firebase
            chatRepository.sendAiMessage(currentUserId, userMessage)


            try {
                val chatHistory = _messages.value.reversed().mapNotNull { msg ->
                    // Don't include error messages in the history sent to the API
                    if (msg.content.startsWith("Rất tiếc")) null
                    else {
                        val role = if (msg.senderId == aiUserId) "assistant" else "user"
                        ChatGptMessage(role = role, content = msg.content)
                    }
                }

                val request = ChatGptRequest(
                    model = "gpt-4o-mini", // Explicitly set the model
                    messages = listOf(systemPrompt) + chatHistory
                )

                val apiKey = "Bearer ${BuildConfig.OPENAI_API_KEY}"
                val response = chatGptApiService.createChatCompletion(apiKey, request)

                response.choices.firstOrNull()?.message?.content?.let { aiContent ->
                    val aiResponseMessage = Message(
                        senderId = aiUserId,
                        content = aiContent.trim(),
                        timestamp = System.currentTimeMillis(),
                    )
                    // Add AI response to UI
                    _messages.value = listOf(aiResponseMessage) + _messages.value
                    // Save AI response to Firebase
                    chatRepository.sendAiMessage(currentUserId, aiResponseMessage)
                }

            } catch (e: Exception) {
                val errorMessage = Message(
                    senderId = aiUserId,
                    content = "Rất tiếc, đã có lỗi xảy ra. Vui lòng thử lại sau.",
                    timestamp = System.currentTimeMillis(),
                )
                _messages.value = listOf(errorMessage) + _messages.value
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }
}
