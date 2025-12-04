// File: app/src/main/java/com/example/wink/ui/features/tarot/TarotViewModel.kt
package com.example.wink.ui.features.tarot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TarotViewModel @Inject constructor(
    // Sau này nếu muốn lấy Tarot từ Firestore/API thì inject repo vào đây
) : ViewModel() {

    private val _uiState = MutableStateFlow(TarotState())
    val uiState: StateFlow<TarotState> = _uiState.asStateFlow()

    // Bộ bài demo – tập trung cho chủ đề tình cảm / crush
    private val tarotDeck: List<TarotCard> = listOf(
        TarotCard(
            id = 0,
            name = "The Fool – Kẻ Khởi Hành",
            shortMeaning = "Khởi đầu mới, liều lĩnh dễ thương, cơ hội bất ngờ.",
            detail = "Trong chuyện tình cảm, lá bài này gợi ý về một mối quan hệ mới hoặc một chương "
                    + "mới trong tình yêu. Hãy mở lòng, nhưng cũng đừng quá ngây thơ."
        ),
        TarotCard(
            id = 1,
            name = "The Lovers – Những Người Yêu",
            shortMeaning = "Lựa chọn trong tình yêu, kết nối sâu sắc, hấp dẫn mạnh mẽ.",
            detail = "Đây là lá bài của sự gắn kết và lựa chọn. Bạn có thể đang đứng giữa những ngã rẽ "
                    + "tình cảm, cần lắng nghe trái tim nhưng đừng bỏ qua lý trí."
        ),
        TarotCard(
            id = 2,
            name = "The Hermit – Ẩn Sĩ",
            shortMeaning = "Cần thời gian một mình, suy nghĩ lại, chữa lành.",
            detail = "Lá bài này khuyên bạn tạm lùi lại để hiểu bản thân muốn gì trong tình yêu. "
                    + "Đừng vội nhảy vào hay duy trì một mối quan hệ chỉ vì sợ cô đơn."
        ),
        TarotCard(
            id = 3,
            name = "The Sun – Mặt Trời",
            shortMeaning = "Niềm vui, sự tích cực, tương lai sáng sủa.",
            detail = "Trong tình yêu, The Sun là điềm lành: sự thấu hiểu, niềm vui chung, và cảm giác "
                    + "an toàn. Đây là lúc cho phép bản thân hạnh phúc hơn."
        ),
        TarotCard(
            id = 4,
            name = "Death – Kết Thúc & Tái Sinh",
            shortMeaning = "Kết thúc một chương cũ, mở ra chương mới.",
            detail = "Đừng hoảng sợ, lá Death thường nói về sự chuyển đổi. Một mối quan hệ, cách nghĩ "
                    + "hay kỳ vọng cũ có thể sắp kết thúc để bạn bước sang giai đoạn tốt hơn."
        )
    )

    fun drawCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Giả vờ có hiệu ứng “vũ trụ đang shuffle bài”
            delay(500)

            val card = tarotDeck.random()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentCard = card,
                    error = null
                )
            }
        }
    }

    fun reset() {
        _uiState.value = TarotState()
    }
}
