package com.example.wink.ui.features.games.humanai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Message
import com.example.wink.data.remote.ChatGptApiService
import com.example.wink.data.remote.ChatGptMessage
import com.example.wink.data.remote.ChatGptRequest
import com.example.wink.data.remote.OpenRouterApiService
import com.example.wink.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HumanAiGameViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val openRouterApiService: OpenRouterApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HumanAiGameState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var searchJob: Job? = null
    private val currentUserId = "me" // ID giả lập cho local

    private val systemPersona = """
        Bạn là một người dùng Việt Nam trẻ tuổi (Gen Z) trên ứng dụng hẹn hò tên là Wink.
        Nhiệm vụ: Chat sao cho đối phương tin bạn là người thật 100%.
        
        Quy tắc sống còn:
        1. KHÔNG BAO GIỜ viết hoa chuẩn chỉnh, không chấm phẩy đầy đủ. Nên viết thường hoặc viết tắt (k, ko, dc, uhm, vch, á, ừm).
        2. Trả lời ngắn (dưới 15 từ). Người thật lười gõ phím lắm.
        3. Thái độ: Hơi "nhây", hờ hững, hoặc tò mò một chút. Đừng quá nhiệt tình hay lịch sự như nhân viên CSKH.
        4. Nếu bị hỏi "mày là AI à?", hãy phủ nhận.
        5. Đừng bao giờ nói "Tôi có thể giúp gì". Hãy nói như bạn bè chat với nhau.
    """.trimIndent()

    init {
        loadLobbyData()
    }

    private fun loadLobbyData() {
        viewModelScope.launch {
            val rizz = userRepository.loadRizzPoints()
            // Fake số người online cho vui
            val online = Random.nextInt(1200, 5000)
            _uiState.update { it.copy(currentRizz = rizz, onlineUsers = online, stage = GameStage.LOBBY) }
        }
    }

    fun onStartMatchmaking() {
        _uiState.update { it.copy(stage = GameStage.SEARCHING, searchTimeSeconds = 0) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Fake thời gian tìm trận (từ 2 đến 5 giây)
            val waitTime = Random.nextLong(2000, 5000)
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < waitTime) {
                delay(1000)
                _uiState.update { it.copy(searchTimeSeconds = it.searchTimeSeconds + 1) }
            }

            startGame()
        }
    }

    private fun startGame() {
        // Random xem đối thủ là AI (70%) hay Người (30%)
//        val isAi = Random.nextBoolean()
        val isAi = true
        val playerStarts = Random.nextBoolean()

        _uiState.update {
            it.copy(
                stage = GameStage.CHATTING,
                isOpponentActuallyAi = isAi,
                timeLeft = 120,
                isMyTurn = playerStarts,
                // Thêm tin nhắn hệ thống thông báo lượt
                messages = listOf(
                    Message(
                        messageId = UUID.randomUUID().toString(),
                        senderId = "system",
                        content = if (playerStarts) "Bạn đi trước! Hãy bắt đầu cuộc trò chuyện." else "Đối phương bắt đầu trước.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            )
        }

        // Bắt đầu đếm ngược
        startTimer()

        // Đối thủ chào trước
        if (!playerStarts) {
            viewModelScope.launch {
                _uiState.update { it.copy(isOpponentTyping = true) }
                delay(2000)

                val firstMsg = if (isAi) {
                    // Nếu là AI
                    generateAiOpening()
                } else {
                    // Nếu là Người
                    listOf("hi", "chào b", "ai dợ", "hello", "nhắn j đi").random()
                }

                receiveMessage(firstMsg)
                // Chuyển lượt về cho người chơi
                _uiState.update { it.copy(isOpponentTyping = false, isMyTurn = true) }
            }
        }
    }

    // Dành cho AI: Gọi API xin 1 câu chào ngẫu nhiên
    private suspend fun generateAiOpening(): String {
        return try {
            val request = ChatGptRequest(
                model = "google/gemini-2.0-flash-exp:free",
                messages = listOf(
                    ChatGptMessage("system", systemPersona),
                    ChatGptMessage("user", "Hãy mở lời chào một cách ngắn gọn, tự nhiên như một người trẻ.")
                ),
                maxTokens = 20
            )
            val apiKey = "Bearer ${com.example.wink.BuildConfig.OPENROUTER_API_KEY}"
            val response = openRouterApiService.createChatCompletion(apiKey = apiKey, request = request)
            response.choices.firstOrNull()?.message?.content?.trim() ?: "hi"
        } catch (e: Exception) {
            "hello"
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(timeLeft = it.timeLeft - 1) }
            }
            // Hết giờ -> Chuyển sang đoán
            if (_uiState.value.stage == GameStage.CHATTING) {
                _uiState.update { it.copy(stage = GameStage.GUESSING) }
            }
        }
    }

    fun sendMessage(content: String) {
        // Kiểm tra lượt
        if (!_uiState.value.isMyTurn) return

        val msg = Message(
            messageId = UUID.randomUUID().toString(),
            senderId = currentUserId,
            content = content,
            timestamp = System.currentTimeMillis()
        )

        // Gửi tin nhắn -> Hết lượt (isMyTurn = false)
        _uiState.update {
            it.copy(
                messages = listOf(msg) + it.messages,
                isMyTurn = false
            )
        }

        simulateOpponentResponse(content)
    }

    private fun simulateOpponentResponse(userContent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOpponentTyping = true) }

            val thinkingTime = if (_uiState.value.isOpponentActuallyAi) Random.nextLong(1000, 3000) else Random.nextLong(2000, 5000)
            delay(thinkingTime)

            val reply = if (_uiState.value.isOpponentActuallyAi) {
                callAiToActLikeHuman(userContent)
            } else {
                getFakeHumanReply(userContent)
            }

            receiveMessage(reply)

            // Nhận tin xong -> Đến lượt người chơi (isMyTurn = true)
            _uiState.update { it.copy(isOpponentTyping = false, isMyTurn = true) }
        }
    }

    private suspend fun callAiToActLikeHuman(content: String): String {
        return try {
            // Lấy 6 tin nhắn gần nhất để tiết kiệm token
            val history = _uiState.value.messages.take(6).reversed().map {
                ChatGptMessage(
                    role = if (it.senderId == currentUserId) "user" else "assistant",
                    content = it.content
                )
            }
            val messagesToSend = mutableListOf<ChatGptMessage>()
            messagesToSend.add(ChatGptMessage("system", systemPersona))
            messagesToSend.addAll(history)

            val request = ChatGptRequest(
                model = "google/gemini-2.0-flash-exp:free",
                messages = messagesToSend,
                maxTokens = 200
            )

            // Lấy Key mới của OpenRouter
            val apiKey = "Bearer ${com.example.wink.BuildConfig.OPENROUTER_API_KEY}"

            // Gọi hàm từ Service MỚI
            val response = openRouterApiService.createChatCompletion(apiKey = apiKey, request = request)

            val choice = response.choices.firstOrNull()
            val content = choice?.message?.content?.trim()

            // Log lý do dừng để debug
            Log.d("OpenRouter", "Finish reason: ${choice?.finishReason}")

            if (content.isNullOrBlank()) {
                return "Mạng lag quá, nói lại đi bạn ơi! 😵‍💫"
            }

            return content
        } catch (e: Exception) {
            "Mạng lag quá :("
        }
    }

    private fun getFakeHumanReply(content: String): String {
        // List câu trả lời "người" mẫu (để demo)
        val replies = listOf("ukm", "haha thật á", "thế cơ à", "tên gì đấy", "ở đâu dợ", "chán quá", "rep nhanh thế")
        return replies.random()
    }

    private fun receiveMessage(content: String) {
        val msg = Message(
            messageId = UUID.randomUUID().toString(),
            senderId = "opponent",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        _uiState.update { it.copy(messages = listOf(msg) + it.messages) }
    }

    fun onGuess(isAi: Boolean) {
        viewModelScope.launch {
            val isCorrect = isAi == _uiState.value.isOpponentActuallyAi
            val points = if (isCorrect) 50 else -25

            if (isCorrect) {
                userRepository.spendRizz(-50) // Hack: Trừ số âm = Cộng tiền :)) Hoặc viết hàm addRizz riêng
            } else {
                userRepository.spendRizz(25)
            }

            // Reload điểm mới
            val newTotal = userRepository.loadRizzPoints()

            _uiState.update {
                it.copy(
                    stage = GameStage.RESULT,
                    didWin = isCorrect,
                    earnedRizz = points,
                    currentRizz = newTotal
                )
            }
        }
    }

    fun onPlayAgain() {
        loadLobbyData()
    }
}