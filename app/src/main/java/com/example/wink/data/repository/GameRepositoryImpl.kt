package com.example.wink.data.repository

import android.util.Log
import com.example.wink.data.model.Message
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : GameRepository {

    private val queueRef = firestore.collection("matchmaking_queue")
    private val gamesRef = firestore.collection("human_ai_games")

    override suspend fun joinMatchmakingQueue(userId: String): Flow<String?> = callbackFlow {
        // 1. Tìm xem có ai đang đợi không (mà không phải chính mình)
        val waitingQuery = queueRef
            .whereNotEqualTo("userId", userId)
            .limit(1)
            .get()
            .await()

        if (!waitingQuery.isEmpty) {
            // ---> TÌM THẤY ĐỐI THỦ
            val opponentDoc = waitingQuery.documents[0]
            val opponentId = opponentDoc.getString("userId") ?: ""

            // Xóa đối thủ khỏi hàng chờ (để không ai match trúng nữa)
            queueRef.document(opponentDoc.id).delete().await()

            // Tạo trận đấu mới
            val newGameId = gamesRef.document().id
            val firstTurn = if (Random.nextBoolean()) userId else opponentId

            val gameData = hashMapOf(
                "player1" to userId,
                "player2" to opponentId,
                "currentTurn" to firstTurn,
                "createdAt" to System.currentTimeMillis(),
                "status" to "active"
            )
            gamesRef.document(newGameId).set(gameData).await()

            // Thông báo cho người kia biết là đã match (bằng cách update doc hàng chờ của họ - trick nhỏ)

            trySend(newGameId)
            close()
        } else {
            // ---> KHÔNG CÓ AI, VÀO HÀNG CHỜ
            val myQueueDoc = queueRef.document(userId)
            val queueData = hashMapOf(
                "userId" to userId,
                "timestamp" to System.currentTimeMillis(),
                "matchedGameId" to null // Chưa có game
            )
            myQueueDoc.set(queueData).await()

            // Lắng nghe xem có ai match mình không
            val listener = myQueueDoc.addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }

                if (snapshot != null && !snapshot.exists()) {
                    // Document bị xóa -> Có nghĩa là ai đó đã bốc mình đi và tạo game
                    // Nhưng làm sao biết gameId? -> Cách thiết kế "matchedGameId" tốt hơn.
                    // Sửa lại logic: Người kia update field "matchedGameId", mình thấy có ID thì mình xóa doc và vào game.
                    close() // Tạm thời close, logic chi tiết ở dưới
                } else {
                    val gameId = snapshot?.getString("matchedGameId")
                    if (gameId != null) {
                        // Đã được ghép cặp!
                        trySend(gameId)
                        // Tự xóa mình khỏi queue
                        myQueueDoc.delete()
                        close()
                    } else {
                        trySend(null) // Vẫn đang đợi
                    }
                }
            }
            awaitClose { listener.remove() }
        }
    }
    suspend fun findOpponentAndCreateGame(myUserId: String): String? {
        val waitingQuery = queueRef
            .whereNotEqualTo("userId", myUserId)
            .orderBy("timestamp", Query.Direction.ASCENDING) // Ưu tiên người đợi lâu
            .limit(1)
            .get()
            .await()

        if (waitingQuery.isEmpty) return null

        val opponentDoc = waitingQuery.documents[0]
        val opponentId = opponentDoc.getString("userId") ?: return null

        // Tạo game
        val newGameId = gamesRef.document().id
        val firstTurn = if (Random.nextBoolean()) myUserId else opponentId

        val gameData = hashMapOf(
            "player1" to myUserId,
            "player2" to opponentId,
            "currentTurn" to firstTurn,
            "createdAt" to System.currentTimeMillis()
        )
        gamesRef.document(newGameId).set(gameData).await()

        // Báo cho đối thủ (Update doc queue của họ)
        queueRef.document(opponentId).update("matchedGameId", newGameId).await()

        return newGameId
    }

    override suspend fun getGameDetails(gameId: String): Map<String, Any>? {
        return try {
            val snapshot = gamesRef.document(gameId).get().await()
            snapshot.data
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun cancelMatchmaking(userId: String) {
        try {
            queueRef.document(userId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun listenToGameMessages(gameId: String): Flow<List<Message>> = callbackFlow {
        val listener = gamesRef.document(gameId).collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val msgs = value?.toObjects(Message::class.java) ?: emptyList()
                trySend(msgs)
            }
        awaitClose { listener.remove() }
    }

    override fun listenToCurrentTurn(gameId: String): Flow<String> = callbackFlow {
        val listener = gamesRef.document(gameId).addSnapshotListener { value, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val turn = value?.getString("currentTurn") ?: ""
            trySend(turn)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun sendGameMessage(gameId: String, message: Message, nextTurnUserId: String) {
        val batch = firestore.batch()

        // 1. Thêm tin nhắn
        val msgRef = gamesRef.document(gameId).collection("messages").document(message.messageId)
        batch.set(msgRef, message)

        // 2. Chuyển lượt (Update document cha)
        val gameRef = gamesRef.document(gameId)
        batch.update(gameRef, "currentTurn", nextTurnUserId)

        batch.commit().await()
    }

    override suspend fun getQueueCount(): Long {
        return try {
            val snapshot = queueRef.count().get(AggregateSource.SERVER).await()
            snapshot.count
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun finishGame(gameId: String) {
        // Có thể xóa game hoặc đánh dấu ended
        // gamesRef.document(gameId).update("status", "ended").await()
    }
}