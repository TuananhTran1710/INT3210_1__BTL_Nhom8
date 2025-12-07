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
    val price: Int = 50,
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
            description = "Xem độ hợp nhau qua cái tên"
        ),
        TarotSubFeatureUi(
            type = LoveFortuneType.ZODIAC,
            title = "Cung Hoàng Đạo",
            description = "Bạch Dương, Kim Ngưu,..."
        ),
        TarotSubFeatureUi(
            type = LoveFortuneType.TAROT_CARD,
            title = "Bói Bài Tây",
            description = "Thông điệp bí ẩn từ lá bài"
        )
    ),

    // Dialogs
    val confirmingFor: LoveFortuneType? = null,         // đang hỏi: có dùng 50 Rizz không?
    val showNotEnoughDialogFor: LoveFortuneType? = null // popup "không đủ Rizz"
)
