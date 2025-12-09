package com.example.wink.data.repository

import com.example.wink.data.model.Tip
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TipsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TipsRepository {

    override suspend fun getTips(): Result<List<Tip>> {
        return try {
            val snapshot = firestore.collection("tips")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            val tips = snapshot.documents.map { doc ->
                val tip = doc.toObject(Tip::class.java)!!
                tip.copy(id = doc.id) // Gán ID từ document ID
            }
            Result.success(tips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Dùng Transaction để đảm bảo an toàn: Trừ tiền và Mở khóa phải cùng thành công
    override suspend fun unlockTip(userId: String, tipId: String, price: Int): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentRizz = snapshot.getLong("rizzPoints") ?: 0L

                if (currentRizz < price) {
                    throw Exception("Không đủ điểm RIZZ!")
                }

                // 1. Trừ điểm
                transaction.update(userRef, "rizzPoints", currentRizz - price)

                // 2. Thêm vào danh sách đã mở
                transaction.update(userRef, "unlockedTips", FieldValue.arrayUnion(tipId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}