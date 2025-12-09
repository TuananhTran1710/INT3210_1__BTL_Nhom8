// File: data/repository/FakeUserRepositoryImpl.kt
package com.example.wink.data.repository

import com.example.wink.data.model.User
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserRepositoryImpl @Inject constructor() : UserRepository {

    // Fake database
    private var last: User? = User(
        uid = "fake-uid",
        email = "fake@example.com",
        rizzPoints = 200,
        ownedIconIds = listOf("icon_1"),   // free icon
        selectedIconId = "icon_1"
    )

    private val tarotFreeMap = mutableMapOf<String, Long>()

    override suspend fun saveUserProfile(user: User) {
        delay(100)
        last = user
    }

    override suspend fun getCurrentUid(): String? = last?.uid
    override suspend fun getCurrentUserEmail(): String? = last?.email

    // ----------- RIZZ -------------
    override suspend fun loadRizzPoints(): Int {
        return last?.rizzPoints ?: 0
    }

    override suspend fun canSpendRizz(amount: Int): Boolean {
        return (last?.rizzPoints ?: 0) >= amount
    }

    override suspend fun spendRizz(amount: Int): Boolean {
        val current = last ?: return false
        if (current.rizzPoints < amount) return false
        last = current.copy(rizzPoints = current.rizzPoints - amount)
        return true
    }

    // ----------- TAROT FREE USAGE -------------
    override suspend fun getTarotFreeUsage(): Map<String, Long> = tarotFreeMap.toMap()

    override suspend fun markTarotFreeUsedToday(featureKey: String, todayEpochDay: Long) {
        tarotFreeMap[featureKey] = todayEpochDay
    }

    // ----------- ICON SHOP -------------
    override suspend fun loadIconShopState(): Pair<List<String>, String?> {
        val user = last ?: return emptyList<String>() to null
        return user.ownedIconIds to user.selectedIconId
    }

    override suspend fun updateIconShopState(
        ownedIconIds: List<String>,
        selectedIconId: String
    ) {
        val cur = last ?: return
        last = cur.copy(
            ownedIconIds = ownedIconIds,
            selectedIconId = selectedIconId
        )
    }
}
