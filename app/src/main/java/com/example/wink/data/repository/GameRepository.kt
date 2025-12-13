package com.example.wink.data.repository

import com.example.wink.data.model.Message
import com.example.wink.data.model.User
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    // Tìm trận: Trả về gameId nếu tìm thấy/tạo thành công, null nếu đang đợi
    suspend fun joinMatchmakingQueue(userId: String): Flow<String?>

    // Hủy tìm trận
    suspend fun cancelMatchmaking(userId: String)

    // Lắng nghe tin nhắn và trạng thái lượt đi của trận đấu
    fun listenToGameMessages(gameId: String): Flow<List<Message>>

    // Lắng nghe xem lượt hiện tại là của ai
    fun listenToCurrentTurn(gameId: String): Flow<String> // Trả về userId của người được đi

    // Gửi tin nhắn và chuyển lượt
    suspend fun sendGameMessage(gameId: String, message: Message, nextTurnUserId: String)

    // Kết thúc game (dọn dẹp)
    suspend fun finishGame(gameId: String)
    suspend fun getGameDetails(gameId: String): Map<String, Any>?
    suspend fun getQueueCount(): Long
}