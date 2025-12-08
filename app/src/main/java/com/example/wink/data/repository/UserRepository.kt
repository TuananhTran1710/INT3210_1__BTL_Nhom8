// data/repository/UserRepository.kt
package com.example.wink.data.repository

import com.example.wink.data.model.User

interface UserRepository {
    suspend fun saveUserProfile(user: User)
    suspend fun getCurrentUid(): String?
    suspend fun getCurrentUserEmail(): String?

    // --- RIZZ ---
    suspend fun loadRizzPoints(): Int
    suspend fun canSpendRizz(amount: Int): Boolean
    suspend fun spendRizz(amount: Int): Boolean

    // --- Tarot free-usage ---
    suspend fun getTarotFreeUsage(): Map<String, Long>
    suspend fun markTarotFreeUsedToday(featureKey: String, todayEpochDay: Long)

    // --- Icon shop: load & update trạng thái icon ---
    suspend fun loadIconShopState(): Pair<List<String>, String?> // (ownedIds, selectedId)
    suspend fun updateIconShopState(
        ownedIconIds: List<String>,
        selectedIconId: String
    )
}
