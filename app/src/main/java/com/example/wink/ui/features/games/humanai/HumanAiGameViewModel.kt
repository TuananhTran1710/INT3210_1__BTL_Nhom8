package com.example.wink.ui.features.games.humanai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Message
import com.example.wink.data.remote.ChatGptApiService
import com.example.wink.data.remote.ChatGptMessage
import com.example.wink.data.remote.ChatGptRequest
import com.example.wink.data.remote.OpenRouterApiService
import com.example.wink.data.repository.GameRepository
import com.example.wink.data.repository.GameRepositoryImpl
import com.example.wink.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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
    private val openRouterApiService: OpenRouterApiService,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HumanAiGameState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var searchJob: Job? = null
    private var myUserId: String = ""

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
        viewModelScope.launch {
            myUserId = userRepository.getCurrentUid() ?: UUID.randomUUID().toString()
        }
    }

    private fun loadLobbyData() {
        viewModelScope.launch {
            val rizz = userRepository.loadRizzPoints()
            // Fake số người online cho vui
            val online = Random.nextInt(1200, 5000)
            _uiState.update { it.copy(currentRizz = rizz, onlineUsers = online, stage = GameStage.LOBBY) }
        }
    }

    fun onCancelMatchmaking() {
        searchJob?.cancel() // Dừng tìm
        timerJob?.cancel()  // Dừng đếm giờ

        viewModelScope.launch {
            val uid = userRepository.getCurrentUid()
            if (uid != null) {
                gameRepository.cancelMatchmaking(uid)
            }
            // Quay về sảnh
            _uiState.update { it.copy(stage = GameStage.LOBBY, searchTimeSeconds = 0) }
        }
    }

    fun onStartMatchmaking() {
        _uiState.update { it.copy(stage = GameStage.SEARCHING, searchTimeSeconds = 0) }

        // 1. Random match với AI hoặc người
        val forceAi = Random.nextFloat() < 0.5

        if (forceAi) {
            fakeSearchingDelayThenStartAi()
        } else {
            startRealMatchmaking()
        }
    }


    private fun fakeSearchingDelayThenStartAi() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Fake thời gian chờ
            val wait = Random.nextLong(2000, 5000)
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < wait) {
                delay(1000)
                _uiState.update { it.copy(searchTimeSeconds = it.searchTimeSeconds + 1) }
            }
            startGameAiMode()
        }
    }

    private fun startRealMatchmaking() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val currentUid = userRepository.getCurrentUid() ?: return@launch
            myUserId = currentUid

            // A. Job đếm giờ (Chạy độc lập, không bị Firestore block)
            val timerJob = launch {
                var timeWaited = 0
                while (true) {
                    delay(1000)
                    timeWaited++
                    _uiState.update { it.copy(searchTimeSeconds = timeWaited) }

                    // Timeout 30s -> Chuyển sang AI
                    if (timeWaited > 30) {
                        gameRepository.cancelMatchmaking(currentUid)
                        this@launch.cancel() // Dừng tìm kiếm
                        startGameAiMode()    // Fallback sang AI
                        return@launch
                    }
                }
            }

            // B. Job tìm trận
            // 1. Thử tìm người đang chờ
            val matchedGameId = (gameRepository as GameRepositoryImpl).findOpponentAndCreateGame(currentUid)
            if (matchedGameId != null) {
                timerJob.cancel()
                joinRealGame(matchedGameId, currentUid)
                return@launch
            }

            // 2. Nếu không có, vào hàng chờ lắng nghe
            gameRepository.joinMatchmakingQueue(currentUid).collect { gameId ->
                if (gameId != null) {
                    timerJob.cancel()
                    joinRealGame(gameId, currentUid)
                    return@collect // Thoát flow
                }
                // Flow này chỉ emit khi có thay đổi DB, không block timer nữa
            }
        }
    }

    private fun joinRealGame(gameId: String, uid: String) {
        searchJob?.cancel()

        viewModelScope.launch {
            // 1. Lấy thông tin đối thủ & lượt đi
            val details = gameRepository.getGameDetails(gameId)
            val p1 = details?.get("player1") as? String
            val p2 = details?.get("player2") as? String
            val currentTurn = details?.get("currentTurn") as? String

            val opponentId = if (p1 == uid) p2 else p1
            val isMyTurn = (currentTurn == uid)

            // 2. Tạo tin nhắn hệ thống (Local)
            // Lưu ý: Timestamp = 0 để nó luôn nằm trên cùng (hoặc dưới cùng tùy sort)
            val systemMsg = Message(
                messageId = "sys_init",
                senderId = "system",
                content = if (isMyTurn) "Bạn đi trước! Hãy bắt đầu cuộc trò chuyện." else "Đối phương đi trước.",
                timestamp = 0L
            )

            _uiState.update {
                it.copy(
                    stage = GameStage.CHATTING,
                    isOpponentActuallyAi = false,
                    gameId = gameId,
                    opponentId = opponentId,
                    timeLeft = 60,
                    isMyTurn = isMyTurn,
                    messages = listOf(systemMsg) // Khởi tạo với tin hệ thống
                )
            }

            startTimer()

            // 3. Lắng nghe tin nhắn & MERGE
            launch {
                gameRepository.listenToGameMessages(gameId).collect { serverMsgs ->
                    _uiState.update { s ->
                        // Logic Merge: Giữ tin hệ thống + Tin server mới nhất
                        // Server messages thường đã sort DESC (mới nhất ở đầu)
                        // Ta muốn tin hệ thống ở cuối cùng (cũ nhất)
                        val merged = serverMsgs + listOf(systemMsg)
                        s.copy(messages = merged)
                    }
                }
            }

            // 4. Lắng nghe lượt đi
            launch {
                gameRepository.listenToCurrentTurn(gameId).collect { turnUserId ->
                    val isMine = (turnUserId == uid)
                    _uiState.update {
                        it.copy(
                            isMyTurn = isMine,
                            isOpponentTyping = !isMine
                        )
                    }
                }
            }
        }
    }

    private fun startGameAiMode() {
        val playerStarts = Random.nextBoolean()

        val systemMsg = Message(
            messageId = "sys_init",
            senderId = "system",
            content = if (playerStarts) "Bạn đi trước! Hãy bắt đầu cuộc trò chuyện." else "Đối phương đi trước.",
            timestamp = 0L
        )

        _uiState.update {
            it.copy(
                stage = GameStage.CHATTING,
                isOpponentActuallyAi = true,
                isMyTurn = playerStarts,
                messages = listOf(systemMsg)
            )
        }

        startTimer()

        if (!playerStarts) {
            viewModelScope.launch {
                _uiState.update { it.copy(isOpponentTyping = true) }
                delay(2000)
                val firstMsg = generateAiOpening()

                // AI nói -> Add vào list (giữ tin hệ thống)
                val aiMsg = Message(
                    messageId = UUID.randomUUID().toString(),
                    senderId = "opponent",
                    content = firstMsg,
                    timestamp = System.currentTimeMillis()
                )

                _uiState.update { s ->
                    // Thêm tin mới lên đầu (vì LazyColumn reverseLayout)
                    s.copy(
                        messages = listOf(aiMsg) + s.messages,
                        isOpponentTyping = false,
                        isMyTurn = true
                    )
                }
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
        if (!_uiState.value.isMyTurn) return

        viewModelScope.launch {
            val msg = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = myUserId, // <-- Dùng ID thật
                content = content,
                timestamp = System.currentTimeMillis()
            )

            // Cập nhật UI ngay (Optimistic)
            _uiState.update {
                it.copy(
                    messages = listOf(msg) + it.messages,
                    isMyTurn = false
                )
            }

            if (_uiState.value.isOpponentActuallyAi) {
                simulateOpponentResponse(content)
            } else {
                val gameId = _uiState.value.gameId ?: return@launch
                val opponentId = _uiState.value.opponentId ?: return@launch
                gameRepository.sendGameMessage(gameId, msg, opponentId)
            }
        }
    }

    private fun simulateOpponentResponse(userContent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOpponentTyping = true) }

            val thinkingTime = if (_uiState.value.isOpponentActuallyAi) Random.nextLong(1000, 3000) else Random.nextLong(2000, 5000)
            delay(thinkingTime)

            val reply = callAiToActLikeHuman(userContent)

            receiveMessage(reply)

            // Nhận tin xong -> Đến lượt người chơi (isMyTurn = true)
            _uiState.update { it.copy(isOpponentTyping = false, isMyTurn = true) }
        }
    }

    private suspend fun callAiToActLikeHuman(content: String): String {
        return try {
            // Lấy 6 tin nhắn gần nhất để tiết kiệm token
            val history = _uiState.value.messages
                .filter { it.senderId != "system" }
                .take(6).reversed().map {
                    ChatGptMessage(
                        // So sánh với ID thật
                        role = if (it.senderId == myUserId) "user" else "assistant",
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
    private fun receiveMessage(content: String) {
        val msg = Message(
            messageId = UUID.randomUUID().toString(),
            senderId = "opponent",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        _uiState.update {
            it.copy(messages = listOf(msg) + it.messages)
        }
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

    fun getMyUserId() = myUserId

    fun onPlayAgain() {
        loadLobbyData()
    }
}