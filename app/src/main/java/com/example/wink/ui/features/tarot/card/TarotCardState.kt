package com.example.wink.ui.features.tarot.card

/** Mô tả 1 lá bài (demo) */
data class TarotCardInfo(
    val id: Int,
    val name: String,
    val shortMeaning: String,
    val detail: String
)

/** UI state của màn bói bài tây */
data class TarotCardState(
    val isLoading: Boolean = false,
    val currentCard: TarotCardInfo? = null,
    val error: String? = null,

    // Dialog dùng lại 50 Rizz để rút tiếp
    val showConfirmDialog: Boolean = false,
    val showNotEnoughDialog: Boolean = false
)
