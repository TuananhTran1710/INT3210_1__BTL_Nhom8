package com.example.wink.ui.features.chat

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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
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
    // --- THÊM HÀM MỚI: sendImage ---

    // Tách logic gọi API ra hàm riêng để tái sử dụng cho cả sendMessage và sendImage
    private suspend fun getAiResponse() {
        try {
            val chatHistory = _messages.value.reversed().mapNotNull { msg ->
                if (msg.content.startsWith("Rất tiếc")) null
                else {
                    val role = if (msg.senderId == aiUserId) "assistant" else "user"
                    // Lưu ý: GPT-4o-mini text-only chỉ đọc được text.
                    // Nếu muốn AI "nhìn" thấy ảnh, cần update API request phức tạp hơn.
                    // Ở đây AI sẽ chỉ thấy text "Đã gửi một ảnh" và phản hồi dựa trên đó.
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

    // HÀM CHÍNH: Đã sửa lại logic Batching
    fun sendMessage(content: String, imageUris: List<Uri>) {
        viewModelScope.launch {
            _isSending.value = true
            val sendingJobs = mutableListOf<Job>()

            // 1. BẮN JOB GỬI TEXT (Nếu có)
            // Lưu ý: Chỉ lưu vào DB, KHÔNG gọi AI ở đây
            if (content.isNotBlank()) {
                val textJob = launch {
                    saveTextMessageToDb(content)
                }
                sendingJobs.add(textJob)
            }

            // 2. BẮN CÁC JOB GỬI ẢNH (Nếu có)
            // Mỗi ảnh là 1 luồng upload riêng, chạy song song với nhau và với text
            imageUris.forEach { uri ->
                val imageJob = launch {
                    uploadAndSaveImageToDb(uri)
                }
                sendingJobs.add(imageJob)
            }

            // 3. CHỜ TẤT CẢ HOÀN THÀNH (Magic Step)
            // joinAll sẽ treo hàm này lại cho đến khi tất cả text và ảnh đã được xử lý xong
            // (đã hiện lên UI và đã lưu vào Firebase)
            sendingJobs.joinAll()

            // 4. GỌI AI MỘT LẦN DUY NHẤT
            // Lúc này, _messages.value đã chứa đủ cả Text và Ảnh vừa gửi
            getAiResponse()

            _isSending.value = false
        }
    }
// --- CÁC HÀM CON (Chỉ làm nhiệm vụ lưu DB, không gọi AI) ---

    private suspend fun saveTextMessageToDb(content: String) {
        try {
            val userMessage = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = currentUserId,
                content = content,
                timestamp = System.currentTimeMillis(),
                mediaUrl = null
            )
            // Update UI ngay lập tức
            _messages.value = listOf(userMessage) + _messages.value
            // Lưu Firebase
            chatRepository.sendAiMessage(currentUserId, userMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun uploadAndSaveImageToDb(uri: Uri) {
        try {
            // Upload ảnh
            val uploadResult = chatRepository.uploadImage(uri)

            uploadResult.onSuccess { imageUrl ->
                val userMessage = Message(
                    messageId = UUID.randomUUID().toString(),
                    senderId = currentUserId,
                    content = "Đã gửi một ảnh", // Nội dung để AI nhận biết context
                    timestamp = System.currentTimeMillis(),
                    mediaUrl = listOf(imageUrl) // Tin nhắn riêng cho ảnh
                )
                // Update UI ngay khi ảnh này upload xong (không cần chờ ảnh khác)
                _messages.value = listOf(userMessage) + _messages.value
                // Lưu Firebase
                chatRepository.sendAiMessage(currentUserId, userMessage)
            }.onFailure {
                it.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // Hàm con: Gửi Text
    private suspend fun sendTextMessage(content: String) {
        _isSending.value = true
        try {
            val userMessage = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = currentUserId,
                content = content,
                timestamp = System.currentTimeMillis(),
                mediaUrl = null // Tin nhắn text không có ảnh
            )

            // Update UI & Save DB
            _messages.value = listOf(userMessage) + _messages.value
            chatRepository.sendAiMessage(currentUserId, userMessage)

            // Gọi AI trả lời cho tin nhắn text này
            getAiResponse()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Lưu ý: Logic loading có thể cần tinh chỉnh nếu muốn chờ tất cả xong mới tắt
            // Nhưng với yêu cầu bất đồng bộ, ta có thể tắt loading của input ngay
            _isSending.value = false
        }
    }

    // Hàm con: Gửi Ảnh (Upload -> Gửi tin)
    private suspend fun sendImageMessage(uri: Uri) {
        // Không set _isSending = true ở đây để không chặn UI nhập liệu tiếp

        // 1. Upload ảnh
        val uploadResult = chatRepository.uploadImage(uri)

        uploadResult.onSuccess { imageUrl ->
            val userMessage = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = currentUserId,
                content = "Đã gửi một ảnh", // Nội dung placeholder
                timestamp = System.currentTimeMillis(),
                mediaUrl = listOf(imageUrl) // Tin nhắn ảnh
            )

            // 2. Update UI & Save DB (Độc lập với các tin khác)
            _messages.value = listOf(userMessage) + _messages.value
            chatRepository.sendAiMessage(currentUserId, userMessage)

            // 3. Gọi AI (Tùy chọn: Bạn có thể muốn AI bình luận về bức ảnh này)
            getAiResponse()

        }.onFailure {
            it.printStackTrace()
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
