// File: app/src/main/java/com/example/wink/ui/features/tarot/TarotState.kt
package com.example.wink.ui.features.tarot

/**
 * Mô tả 1 lá bài tarot (bản đơn giản, tập trung vào tình cảm).
 */
data class TarotCard(
    val id: Int,
    val name: String,
    val shortMeaning: String,
    val detail: String
)

/**
 * UI state cho màn hình Tarot.
 */
data class TarotState(
    val isLoading: Boolean = false,
    val currentCard: TarotCard? = null,
    val error: String? = null
)
