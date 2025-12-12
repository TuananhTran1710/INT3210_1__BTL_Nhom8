package com.example.wink.ui.features.tarot.zodiac.results

data class TarotZodiacResultState(
    val score: Int = 0,
    val yourSignName: String = "",
    val crushSignName: String = "",
    val message: String = "",
    val showConfirmDialog: Boolean = false,
    val showNotEnoughDialog: Boolean = false
)

sealed class TarotZodiacResultEvent {
    object BackToHub : TarotZodiacResultEvent()
    object RetryPaid : TarotZodiacResultEvent()
}
