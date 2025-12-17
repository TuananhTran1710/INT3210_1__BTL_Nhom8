package com.example.wink.ui.features.chat

import android.app.Application
import android.content.Context
import android.net.Uri
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class LoveAnalysisResponse(
    val score: Int,
    val comment: String
)

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
    private val application: Application,
) : ViewModel() {

    private val sharedPreferences = application.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    private val _aiName = MutableStateFlow("Lan Anh")
    val aiName = _aiName.asStateFlow()

    private val _aiAvatarUrl = MutableStateFlow<String?>(null)
    val aiAvatarUrl = _aiAvatarUrl.asStateFlow()

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

    private val _analyzeResult = MutableStateFlow<LoveAnalysisResponse?>(null)
    val analyzeResult = _analyzeResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    init {
        loadAiSettings()
        loadMessages()
    }

    private fun loadAiSettings() {
        _aiName.value = sharedPreferences.getString("ai_name", "Lan Anh") ?: "Lan Anh"
        _aiAvatarUrl.value = sharedPreferences.getString("ai_avatar_uri", null)
    }

    private suspend fun getAiResponse() {
        try {
            val chatHistory = _messages.value.reversed().mapNotNull { msg ->
                if (msg.content.startsWith("Rất tiếc")) null
                else {
                    val role = if (msg.senderId == aiUserId) "assistant" else "user"
                    ChatGptMessage(role = role, content = msg.content)
                }
            }

            val request = ChatGptRequest(
                model = "gpt-4o-mini",
                messages = listOf(systemPrompt) + chatHistory
            )

            val apiKey = "Bearer ${BuildConfig.OPENAI_API_KEY}"
            val response = chatGptApiService.createChatCompletion(apiKey, request)

            response.choices.firstOrNull()?.message?.content?.let { aiContent ->
                val aiResponseMessage = Message(
                    messageId = UUID.randomUUID().toString(),
                    senderId = aiUserId,
                    content = aiContent.trim(),
                    timestamp = System.currentTimeMillis(),
                )
                _messages.value = listOf(aiResponseMessage) + _messages.value
                chatRepository.sendAiMessage(currentUserId, aiResponseMessage)
            }

        } catch (e: Exception) {
            val errorMessage = Message(
                senderId = aiUserId,
                content = "Rất tiếc, em đang bận xíu (Lỗi mạng).",
                timestamp = System.currentTimeMillis(),
            )
            _messages.value = listOf(errorMessage) + _messages.value
            e.printStackTrace()
        } finally {
            _isSending.value = false
        }
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

    fun sendMessage(content: String, imageUris: List<Uri>) {
        viewModelScope.launch {
            _isSending.value = true
            val sendingJobs = mutableListOf<Job>()

            if (content.isNotBlank()) {
                val textJob = launch {
                    saveTextMessageToDb(content)
                }
                sendingJobs.add(textJob)
            }

            imageUris.forEach { uri ->
                val imageJob = launch {
                    uploadAndSaveImageToDb(uri)
                }
                sendingJobs.add(imageJob)
            }

            sendingJobs.joinAll()
            getAiResponse()
            _isSending.value = false
        }
    }

    private suspend fun saveTextMessageToDb(content: String) {
        try {
            val userMessage = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = currentUserId,
                content = content,
                timestamp = System.currentTimeMillis(),
                mediaUrl = null
            )
            _messages.value = listOf(userMessage) + _messages.value
            chatRepository.sendAiMessage(currentUserId, userMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun uploadAndSaveImageToDb(uri: Uri) {
        try {
            val uploadResult = chatRepository.uploadImage(uri)

            uploadResult.onSuccess { imageUrl ->
                val userMessage = Message(
                    messageId = UUID.randomUUID().toString(),
                    senderId = currentUserId,
                    content = "Đã gửi một ảnh",
                    timestamp = System.currentTimeMillis(),
                    mediaUrl = listOf(imageUrl)
                )
                _messages.value = listOf(userMessage) + _messages.value
                chatRepository.sendAiMessage(currentUserId, userMessage)
            }.onFailure {
                it.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun analyzeConversation() {
        viewModelScope.launch {
            if (_messages.value.isEmpty()) return@launch

            _isAnalyzing.value = true
            _analyzeResult.value = null
            _analysisSteps.value = emptyList() // reset

            val jsonFormat = Json { ignoreUnknownKeys = true }

            try {
                val last10Messages = _messages.value
                    .filter { !it.content.startsWith("Xin chào") }
                    .take(10)
                    .reversed()

                coroutineScope {
                    val stepsAnalysisJob = async {
                        val userMessagesWithId = last10Messages.filter { it.senderId != aiUserId }
                        if (userMessagesWithId.isEmpty()) return@async emptyList<UserMessageAnalysis>()

                        val chatHistoryForSteps = userMessagesWithId.map {
                            ChatGptMessage(
                                role = "user",
                                content = """{"id": "${it.messageId}", "content": "${it.content}"}"""
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
                        val request = ChatGptRequest("gpt-4o-mini", listOf(analyzePrompt) + chatHistoryForSteps)
                        val response = chatGptApiService.createChatCompletion("Bearer ${BuildConfig.OPENAI_API_KEY}", request)
                        val rawJson = response.choices.firstOrNull()?.message?.content ?: "[]"
                        Log.d("ANALYSIS_DEBUG", "--- AI Raw Steps Result ---: $rawJson")
                        jsonFormat.decodeFromString<List<UserMessageAnalysis>>(rawJson)
                    }

                    val scoreJob = async {
                        val conversationText = last10Messages.joinToString("\n") {
                            val prefix = if (it.senderId == aiUserId) "Em: " else "Anh: "
                            prefix + it.content
                        }
                        if (conversationText.isBlank()) return@async null

                        val scorePrompt = ChatGptMessage(
                            role = "system",
                            content = """
                            Bạn là AI chuyên gia phân tích tình cảm.
                            Dựa vào đoạn hội thoại sau, hãy đưa ra đánh giá tổng quan về mức độ thấu hiểu giữa hai người.
                            Hãy trả về một object JSON DUY NHẤT với định dạng sau:
                            {
                              "score": <một số nguyên từ 0 đến 100>,
                              "comment": "<một bình luận ngắn gọn, hài hước về điểm số này>"
                            }
                            TUYỆT ĐỐI chỉ trả về object JSON, không có văn bản giải thích nào khác.
                            """.trimIndent()
                        )
                        val request = ChatGptRequest("gpt-4o-mini", listOf(scorePrompt, ChatGptMessage("user", conversationText)))
                        val response = chatGptApiService.createChatCompletion("Bearer ${BuildConfig.OPENAI_API_KEY}", request)
                        val rawJson = response.choices.firstOrNull()?.message?.content
                        Log.d("ANALYSIS_DEBUG", "--- AI Raw Score Result ---: $rawJson")
                        rawJson?.let { jsonFormat.decodeFromString<LoveAnalysisResponse>(it) }
                    }

                    _analysisSteps.value = stepsAnalysisJob.await()
                    _analyzeResult.value = scoreJob.await()

                    Log.d("ANALYSIS_DEBUG", "--- Finished ---: Steps=${_analysisSteps.value.size}, Score=${_analyzeResult.value?.score}")
                }
            } catch (e: Exception) {
                Log.e("ANALYSIS_DEBUG", "Phân tích thất bại", e)
                _analyzeResult.value = null
                _analysisSteps.value = emptyList()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
}

