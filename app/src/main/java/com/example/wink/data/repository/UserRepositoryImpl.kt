// File: data/repository/UserRepositoryImpl.kt
package com.example.wink.data.repository

import com.example.wink.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun saveUserProfile(user: User) {
        val uid = user.uid
        firestore.collection("users")
            .document(uid)
            .set(user)
            .await()
    }

    override suspend fun getCurrentUid(): String? = auth.currentUser?.uid
    override suspend fun getCurrentUserEmail(): String? = auth.currentUser?.email

    // --- RIZZ (ví dụ) ---
    override suspend fun loadRizzPoints(): Int {
        val uid = getCurrentUid() ?: return 0
        val snap = firestore.collection("users").document(uid).get().await()
        return (snap.getLong("rizzPoints") ?: 0L).toInt()
    }

    override suspend fun canSpendRizz(amount: Int): Boolean {
        return loadRizzPoints() >= amount
    }

    override suspend fun spendRizz(amount: Int): Boolean {
        val uid = getCurrentUid() ?: return false
        val docRef = firestore.collection("users").document(uid)

        return try {
            firestore.runTransaction { tx ->
                val snap = tx.get(docRef)
                val current = (snap.getLong("rizzPoints") ?: 0L).toInt()
                if (current < amount) {
                    throw IllegalStateException("Not enough Rizz")
                }
                tx.update(docRef, "rizzPoints", current - amount)
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // --------- TAROT FREE-USAGE --------------

    override suspend fun getTarotFreeUsage(): Map<String, Long> {
        val uid = getCurrentUid() ?: return emptyMap()
        val snap = firestore.collection("users").document(uid).get().await()

        // Trong user doc lưu 1 map "tarotFreeUsage": { "BY_NAME": 20000, "ZODIAC": 20001, ... }
        val raw = snap.get("tarotFreeUsage") as? Map<*, *> ?: emptyMap<Any, Any>()
        val result = mutableMapOf<String, Long>()
        raw.forEach { (k, v) ->
            val key = k as? String ?: return@forEach
            val value = when (v) {
                is Long -> v
                is Double -> v.toLong()
                else -> null
            } ?: return@forEach
            result[key] = value
        }
        return result
    }

    override suspend fun markTarotFreeUsedToday(featureKey: String, todayEpochDay: Long) {
        val uid = getCurrentUid() ?: return
        val docRef = firestore.collection("users").document(uid)

        // cập nhật field "tarotFreeUsage.FEATURE_KEY" = todayEpochDay
        docRef.update("tarotFreeUsage.$featureKey", todayEpochDay).await()
    }
}
