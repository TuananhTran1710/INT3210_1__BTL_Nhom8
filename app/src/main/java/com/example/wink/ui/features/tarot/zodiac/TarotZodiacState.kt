package com.example.wink.ui.features.tarot.zodiac

import kotlin.random.Random

// 12 cung hoàng đạo
enum class ZodiacSign(val displayName: String) {
    ARIES("Bạch Dương"),
    TAURUS("Kim Ngưu"),
    GEMINI("Song Tử"),
    CANCER("Cự Giải"),
    LEO("Sư Tử"),
    VIRGO("Xử Nữ"),
    LIBRA("Thiên Bình"),
    SCORPIO("Bọ Cạp"),
    SAGITTARIUS("Nhân Mã"),
    CAPRICORN("Ma Kết"),
    AQUARIUS("Bảo Bình"),
    PISCES("Song Ngư");

    companion object {
        fun all() = values().toList()
    }
}

/** State của màn chọn cung */
data class TarotZodiacState(
    val yourSign: ZodiacSign = ZodiacSign.ARIES,
    val crushSign: ZodiacSign = ZodiacSign.TAURUS, // nếu muốn đổi default thì đổi ở đây
    val isLoading: Boolean = false
)

/** Kết quả tương hợp để chuyển sang màn Result */
data class ZodiacCompatResult(
    val yourSign: ZodiacSign,
    val crushSign: ZodiacSign,
    val score: Int,
    val message: String
)

/** Cache tạm giữa màn chọn & màn kết quả (giống bói theo tên) */
object ZodiacResultCache {
    var lastResult: ZodiacCompatResult? = null
}

/** Helper sinh điểm random & câu mô tả */
fun randomZodiacResult(
    your: ZodiacSign,
    crush: ZodiacSign
): ZodiacCompatResult {
    // Random điểm 50–100 cho vui
    val score = Random.nextInt(from = 50, until = 101)

    val message = when {
        score >= 85 -> "Sự kết hợp giữa ${your.displayName} và ${crush.displayName} cực kỳ ăn ý, nhiều cảm xúc và thấu hiểu."
        score >= 65 -> "Hai bạn khá hợp nhau, chỉ cần thêm chút lắng nghe và nhường nhịn là mọi thứ sẽ rất ổn."
        else -> "Cặp đôi hơi trái dấu, nhưng nếu cùng cố gắng thì vẫn có thể tạo nên một câu chuyện thú vị."
    }

    return ZodiacCompatResult(
        yourSign = your,
        crushSign = crush,
        score = score,
        message = message
    )
}
