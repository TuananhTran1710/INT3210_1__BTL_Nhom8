package com.example.wink.data.repository

import com.example.wink.data.model.Chat
import com.example.wink.data.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /** ---------------------------
     * Chat Realtime
     * --------------------------- */

    fun listenChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val listener = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Chat::class.java)?.copy(chatId = document.id)
                } ?: emptyList()
                trySend(chats).isSuccess
            }

        awaitClose { listener.remove() }
    }

    suspend fun createChat(chat: Chat): Result<String> = try {
        val chatData = hashMapOf<String, Any?>(
            "name" to chat.name,
            "participants" to chat.participants,
            "updatedAt" to chat.updatedAt,
            "avatarUrl" to chat.avatarUrl,
        )
        val ref = firestore.collection("chats")
            .add(chatData)
            .await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getChat(chatId: String): Chat? {
        return try {
            val document = firestore.collection("chats").document(chatId).get().await()
            document.toObject(Chat::class.java)?.copy(chatId = document.id)
        } catch (e: Exception) {
            null
        }
    }

    /** ---------------------------
     * Messages Realtime
     * --------------------------- */

    fun listenMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Message::class.java)?.copy(messageId = document.id)
                } ?: emptyList()
                trySend(messages).isSuccess
            }

        awaitClose { listener.remove() }
    }

    suspend fun getLastMessage(chatId: String): Message? {
        return try {
            val snapshot = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toObject(Message::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gửi tin nhắn vào chat subcollection
     * - Đồng thời update updatedAt của chat
     */
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit> = try {
        val chatRef = firestore.collection("chats").document(chatId)

        val messageData = hashMapOf<String, Any?>(
            "senderId" to message.senderId,
            "receiverId" to message.receiverId,
            "content" to message.content,
            "timestamp" to message.timestamp,
            "readBy" to message.readBy,
            "mediaUrl" to message.mediaUrl,
        )

        // Thêm message vào subcollection
        chatRef.collection("messages")
            .add(messageData)
            .await()

        // Update updatedAt của chat
        chatRef.update("updatedAt", message.timestamp).await()
        
        // Update lastMessage của chat
        chatRef.update("lastMessage", message.content).await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
