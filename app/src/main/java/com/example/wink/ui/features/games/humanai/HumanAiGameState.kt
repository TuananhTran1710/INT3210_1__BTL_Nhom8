package com.example.wink.ui.features.games.humanai
import com.example.wink.data.model.Message

// Các trạng thái của Game
enum class GameStage {
    LOBBY, SEARCHING, CHATTING, GUESSING, RESULT
}

data class HumanAiGameState(
    val stage: GameStage = GameStage.LOBBY,
    val currentRizz: Int = 0,
    val onlineUsers: Int = 0,
    val searchTimeSeconds: Int = 0,

    // Chat state
    val messages: List<Message> = emptyList(),
    val timeLeft: Int = 120, // giây chat
    val isOpponentTyping: Boolean = false,

    // Logic game
    val isOpponentActuallyAi: Boolean = false, // True = AI, False = Human
    val didWin: Boolean = false,
    val earnedRizz: Int = 0,
    val isMyTurn: Boolean = false,

    val gameId: String? = null, // ID trận đấu nếu là người thật
    val opponentId: String? = null // ID đối thủ
)