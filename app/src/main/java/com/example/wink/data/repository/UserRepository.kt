// File: data/repository/UserRepository.kt
package com.example.wink.data.repository

import com.example.wink.data.model.User

interface UserRepository {
    suspend fun saveUserProfile(user: User)
    suspend fun getCurrentUid(): String?
    suspend fun getCurrentUserEmail(): String?

    // --- RIZZ (cậu đã thêm) ---
    suspend fun loadRizzPoints(): Int
    suspend fun canSpendRizz(amount: Int): Boolean
    suspend fun spendRizz(amount: Int): Boolean

    // --- Tarot free-usage ---
    /**
     * Lấy map ngày cuối cùng dùng free cho từng mini-feature.
     * key: tên feature (vd: "BY_NAME", "ZODIAC", "TAROT_CARD")
     * value: epochDay (Long) của ngày đó.
     */
    suspend fun getTarotFreeUsage(): Map<String, Long>

    /**
     * Ghi nhận hôm nay đã dùng free cho featureKey.
     */
    suspend fun markTarotFreeUsedToday(featureKey: String, todayEpochDay: Long)
}
