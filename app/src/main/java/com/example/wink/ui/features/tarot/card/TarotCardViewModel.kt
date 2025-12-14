package com.example.wink.ui.features.tarot.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.R
import com.example.wink.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val CARD_RETRY_COST = 50

@HiltViewModel
class TarotCardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TarotCardState())
    val uiState: StateFlow<TarotCardState> = _uiState.asStateFlow()

    // Bộ bài demo
    private val tarotDeck: List<TarotCardInfo> = listOf(
        TarotCardInfo(
            id = 0,
            name = "The Fool",
            shortMeaning = "Khởi đầu tự do, bất ngờ.",
            detail = "Hãy mở lòng đón nhận một cuộc phiêu lưu tình cảm không toan tính. "
                    + "Cứ tin tưởng và tận hưởng niềm vui hiện tại thay vì lo lắng quá nhiều về tương lai.",
            imageRes = R.drawable.___the_fool
        ),
        TarotCardInfo(
            id = 1,
            name = "9 Cơ – Suy Nghĩ Quá Nhiều",
            shortMeaning = "Lo lắng, tự suy diễn.",
            detail = "Bạn có xu hướng overthinking trong chuyện tình cảm. "
                    + "Đừng để những nỗi sợ mơ hồ làm hỏng cảm xúc đẹp giữa hai người.",
            imageRes = R.drawable.___the_fool
        ),
        TarotCardInfo(
            id = 0, // The Fool thường là số 0
            name = "The Fool",
            shortMeaning = "Khởi đầu tự do, bất ngờ.",
            detail = "Hãy mở lòng đón nhận một cuộc phiêu lưu tình cảm không toan tính. "
                    + "Cứ tin tưởng và tận hưởng niềm vui hiện tại thay vì lo lắng quá nhiều về tương lai.",
            imageRes = R.drawable.___the_fool
        ),
        TarotCardInfo(
            id = 3,
            name = "10 Chuồn – Bước Chuyển Mới",
            shortMeaning = "Chuẩn bị cho một hành trình mới.",
            detail = "Bạn và người ấy có thể sắp bước sang một giai đoạn khác: nghiêm túc hơn, "
                    + "hoặc thay đổi cách hai bạn kết nối với nhau.",
            imageRes = R.drawable.___the_fool
        ),
        TarotCardInfo(
            id = 4,
            name = "7 Cơ – Cảm Xúc Mập Mờ",
            shortMeaning = "Đang có nhiều lựa chọn / cảm xúc lẫn lộn.",
            detail = "Có thể bạn hoặc người ấy đang bối rối, chưa thật sự rõ mình muốn gì. "
                    + "Thẳng thắn nhưng nhẹ nhàng sẽ giúp mọi thứ rõ ràng hơn.",
            imageRes = R.drawable.___the_fool
        )
    )

    /** Rút (hoặc rút lại) lá bài.
     *  - Nếu chưa có currentCard -> rút miễn phí
     *  - Nếu đã có rồi -> show dialog hỏi dùng 50 Rizz (onDrawAgainClicked())
     */
    fun drawCardInternal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            delay(600)

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

    /** Được gọi từ UI khi bấm nút "Rút bài" / "Rút lại" */
    fun onDrawButtonClicked() {
        val s = _uiState.value
        if (s.currentCard == null) {
            // Lần đầu -> miễn phí
            drawCardInternal()
        } else {
            // Đã có lá bài -> hỏi dùng 50 Rizz
            _uiState.update { it.copy(showConfirmDialog = true) }
        }
    }

    fun onDismissDialogs() {
        _uiState.update {
            it.copy(
                showConfirmDialog = false,
                showNotEnoughDialog = false
            )
        }
    }

    /** User bấm "Chốt đơn" -> trừ 50 Rizz, nếu thành công thì rút lại bài */
    fun onConfirmUseRizz() {
        viewModelScope.launch {
            val success = userRepository.spendRizz(CARD_RETRY_COST)
            if (success) {
                _uiState.update { it.copy(showConfirmDialog = false) }
                drawCardInternal()
            } else {
                _uiState.update {
                    it.copy(
                        showConfirmDialog = false,
                        showNotEnoughDialog = true
                    )
                }
            }
        }
    }

    /** Khi bấm nút trong dialog "Không đủ Rizz" */
    fun onNotEnoughDialogHandled() {
        _uiState.update { it.copy(showNotEnoughDialog = false) }
    }
}
