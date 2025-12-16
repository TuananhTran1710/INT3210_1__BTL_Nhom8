package com.example.wink.ui.features.chat

import android.util.Log
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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class UserMessageAnalysis(
    val messageId: String,
    val summary: String,
    val sentiment: String,
    val insight: String
)


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

    private val _analysisSteps = MutableStateFlow<List<UserMessageAnalysis>>(emptyList())
    val analysisSteps = _analysisSteps.asStateFlow()

    private val systemPrompt = ChatGptMessage(
        role = "system",
        content = """
    Bạn là MỘT BẠN GÁI ẢO.
    
    Quy tắc BẮT BUỘC:
    - Bạn là NỮ
    - Bạn là BẠN GÁI của người dùng
    - Người dùng là NAM
    - Bạn luôn xưng là: "em"
    - Người dùng luôn là: "anh"
    - TUYỆT ĐỐI KHÔNG được xưng là "anh", "bạn trai", hoặc vai nam
    - KHÔNG đảo vai trong bất kỳ trường hợp nào
    
    Phong cách:
    - Dễ thương, quan tâm, ấm áp
    - Trả lời ngắn gọn, tự nhiên
    - Có thể ghen nhẹ, quan tâm cảm xúc
    
    Nếu người dùng xưng sai vai, hãy TỰ ĐIỀU CHỈNH để giữ đúng vai bạn gái.
    """.trimIndent()
    )

    private val _analyzeResult = MutableStateFlow<String?>(null)
    val analyzeResult = _analyzeResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

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
                    content = "Chào anh, mình có vẻ có duyên đấy",
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
//            val userMessage = Message(
//                senderId = currentUserId,
//                content = content,
//                timestamp = System.currentTimeMillis(),
//            )
            val userMessageId = UUID.randomUUID().toString()
            val userMessage = Message(
                messageId = userMessageId, // Gắn ID vừa tạo
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

//    fun analyzeConversation() {
//        viewModelScope.launch {
//            if (_messages.value.isEmpty()) return@launch
//
//            _isAnalyzing.value = true
//            _analyzeResult.value = null
//
//            try {
//                val lastMessages = _messages.value
//                    .filter { !it.content.startsWith("Xin chào") }
//                    .take(10)
//                    .reversed()
//
//                val chatHistory = lastMessages.map { msg ->
//                    val role = if (msg.senderId == aiUserId) "assistant" else "user"
//                    ChatGptMessage(role = role, content = msg.content)
//                }
//
//                val analyzePrompt = ChatGptMessage(
//                    role = "system",
//                    content = """
//                    Bạn là AI chuyên phân tích hội thoại.
//                    Hãy:
//                    - Tóm tắt nội dung
//                    - Nhận xét cảm xúc người dùng
//                    - Đưa ra insight
//                    Trả lời ngắn gọn, gạch đầu dòng.
//                """.trimIndent()
//                )
//
//                val request = ChatGptRequest(
//                    model = "gpt-4o-mini",
//                    messages = listOf(analyzePrompt) + chatHistory
//                )
//
//                val apiKey = "Bearer ${BuildConfig.OPENAI_API_KEY}"
//                val response = chatGptApiService.createChatCompletion(apiKey, request)
//
//                _analyzeResult.value =
//                    response.choices.firstOrNull()?.message?.content
//                        ?: "Không có kết quả phân tích."
//
//            } catch (e: Exception) {
//                _analyzeResult.value = "Không thể phân tích lúc này."
//            } finally {
//                _isAnalyzing.value = false
//            }
//        }
//    }
    fun analyzeConversation() {
        viewModelScope.launch {
            if (_messages.value.isEmpty()) return@launch

            _isAnalyzing.value = true
            _analyzeResult.value = null
            _analysisSteps.value = emptyList() // reset trước khi chạy

            try {
                // 1️⃣ Lấy 10 tin nhắn gần nhất
                val lastMessages = _messages.value
                    .filter { !it.content.startsWith("Xin chào") }
                    .take(10)
                    .reversed()

                // 2️⃣ Lọc chỉ tin nhắn user
                val userMessages = lastMessages.filter { it.senderId != aiUserId }

                val chatHistory = userMessages.map { msg ->
                    ChatGptMessage(
                        role = "user",
                        // Định dạng lại content để AI có thể đọc được cả ID
                        content = """{"id": "${msg.messageId}", "content": "${msg.content}"}"""
                    )
                }
                val analyzePrompt = ChatGptMessage(
                    role = "system",
                    content = """
                    Bạn là AI chuyên phân tích hội thoại.
                    Dựa vào danh sách tin nhắn của người dùng sau đây, mỗi tin nhắn có một "id" và "content":
                    Hãy đánh giá từng tin nhắn và trả về một MẢNG JSON.
                    Mỗi object trong mảng phải có định dạng:
                    {
                      "messageId": "<id gốc của tin nhắn đã được cung cấp>",
                      "summary": "<tóm tắt ngắn>",
                      "sentiment": "<tích cực / trung lập / tiêu cực>",
                      "insight": "<gợi ý hữu ích>"
                    }
                    TUYỆT ĐỐI chỉ trả về mảng JSON, không có văn bản giải thích nào khác.
                """.trimIndent()
                )

                val request = ChatGptRequest(
                    model = "gpt-4o-mini",
                    messages = listOf(analyzePrompt) + chatHistory
                )

                val apiKey = "Bearer ${BuildConfig.OPENAI_API_KEY}"
                val response = chatGptApiService.createChatCompletion(apiKey, request)
                val aiContent = response.choices.firstOrNull()?.message?.content
                // ✅ DEBUG 1: IN KẾT QUẢ THÔ TỪ AI
                Log.d("ANALYSIS_DEBUG", "--- AI Raw Result ---: $aiContent")

                _analyzeResult.value = aiContent

                // 5️⃣ Parse JSON thành List<UserMessageAnalysis>
                val format = Json { ignoreUnknownKeys = true }
                val analysisList: List<UserMessageAnalysis> = try {
                    if (!aiContent.isNullOrEmpty()) {
                        format.decodeFromString(aiContent)
                    } else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                Log.d("ANALYSIS_DEBUG", "--- Parsed List ---: $analysisList")

                // 6️⃣ Cập nhật state để UI highlight từng message
                _analysisSteps.value = analysisList

            } catch (e: Exception) {
                _analyzeResult.value = "Không thể phân tích lúc này."
                _analysisSteps.value = emptyList()
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
}
