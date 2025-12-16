package com.example.wink.ui.features.tarot

import java.time.LocalDate

/** 3 loại mini-feature trong Bói Tình Yêu */
enum class LoveFortuneType {
    BY_NAME,      // Bói theo tên
    ZODIAC,       // Cung hoàng đạo
    TAROT_CARD    // Bói bài tây
}

/** Thông tin 1 item trong list Bói Tình Yêu */
data class TarotSubFeatureUi(
    val type: LoveFortuneType,
    val title: String,
    val description: String,
    val price: Int = 5,
    val usedFreeToday: Boolean = false          // true -> đã hết lượt free hôm nay
)

/** UI state của màn hub Bói Tình Yêu */
data class TarotHubState(
    val rizzPoints: Int = 0,
    val today: LocalDate = LocalDate.now(),
    val features: List<TarotSubFeatureUi> = listOf(
        TarotSubFeatureUi(
            type = LoveFortuneType.BY_NAME,
            title = "Bói Theo Tên",
            description = "Mật mã tên gọi: Liệu đôi ta có nên duyên?"
        ),
        TarotSubFeatureUi(
            type = LoveFortuneType.ZODIAC,
            title = "Cung Hoàng Đạo",
            description = "Hoàng Đạo se duyên: Tình ta có hợp không?"
        ),
        TarotSubFeatureUi(
            type = LoveFortuneType.TAROT_CARD,
            title = "Bài Tarot",
            description = "Nhận thông điệp bí ẩn từ những lá bài Tarot!"
        )
    ),

    val confirmingFor: LoveFortuneType? = null,
    val showNotEnoughDialogFor: LoveFortuneType? = null
)
