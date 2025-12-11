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

        _uiState.update {
            it.copy(
                stage = GameStage.CHATTING,
                isOpponentActuallyAi = isAi,
                messages = emptyList(),
                timeLeft = 60
            )
        }

        // Bắt đầu đếm ngược 60s
        startTimer()

        // Đối thủ chào trước
        viewModelScope.launch {
            delay(1500)
            receiveMessage(if (isAi) "Hi, chào đằng ấy 👋" else "hello, ai đấy?")
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
        val msg = Message(
            messageId = UUID.randomUUID().toString(),
            senderId = currentUserId,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        _uiState.update { it.copy(messages = listOf(msg) + it.messages) }

        // Trigger đối thủ trả lời
        simulateOpponentResponse(content)
    }

    private fun simulateOpponentResponse(userContent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOpponentTyping = true) }

            // Thời gian suy nghĩ: AI nhanh hơn, Người chậm hơn
            val thinkingTime = if (_uiState.value.isOpponentActuallyAi) Random.nextLong(1000, 3000) else Random.nextLong(2000, 5000)
            delay(thinkingTime)

            val reply = if (_uiState.value.isOpponentActuallyAi) {
                // Gọi ChatGPT thật (Prompt đóng vai người)
                callAiToActLikeHuman(userContent)
            } else {
                // Fake Script người thật (hoặc dùng AI nhưng prompt ngu ngơ hơn)
                getFakeHumanReply(userContent)
            }

            receiveMessage(reply)
            _uiState.update { it.copy(isOpponentTyping = false) }
        }
    }

    private suspend fun callAiToActLikeHuman(content: String): String {
        return try {
            val prompt = ChatGptMessage("system", "Bạn đang chơi trò chơi Turing Test. Hãy đóng vai một người dùng mạng xã hội bình thường, trẻ trung (GenZ). Trả lời ngắn gọn, dùng teencode nhẹ, không quá formal, đôi khi viết thường không dấu. Đừng tỏ ra là AI.")
            val userMsg = ChatGptMessage("user", content)

            val request = ChatGptRequest(
                model = "google/gemini-2.0-flash-exp:free",
                messages = listOf(prompt, userMsg),
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