package com.example.wink.ui.features.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Tip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TipsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TipsState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>() // String là tipId
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(500) // Fake loading

            // Mock Data
            val mockTips = listOf(
                Tip("1", "Eye Contact cơ bản", "Cách giao tiếp bằng mắt không gây sợ hãi", "Nội dung bài 1...", 0, false), // Free
                Tip("2", "Quy tắc 3 ngày", "Có nên nhắn tin ngay sau buổi hẹn?", "Nội dung bài 2...", 0, false), // Free
                Tip("3", "Đọc vị ngôn ngữ cơ thể", "Biết nàng thích bạn qua cử chỉ", "Nội dung bài 3...", 50, true), // Locked
                Tip("4", "Cách bắt chuyện tự nhiên", "Không bao giờ bị 'Seen' không rep", "Nội dung bài 4...", 100, true), // Locked
                Tip("5", "Nghệ thuật khen ngợi", "Khen sao cho tinh tế", "Nội dung bài 5...", 150, true) // Locked
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    tips = mockTips,
                    userRizzPoints = 200 // Giả sử user có 200 điểm
                )
            }
        }
    }

    // Xử lý khi bấm vào Card
    fun onTipClick(tip: Tip) {
        if (tip.isLocked) {
            // Nếu khóa -> Mở dialog xác nhận
            _uiState.update { it.copy(selectedTipToUnlock = tip, unlockError = null) }
        } else {
            // Nếu mở -> Điều hướng sang màn chi tiết
            viewModelScope.launch {
                _navigationEvent.emit(tip.id)
            }
        }
    }

    // Xử lý hành động Mua (Unlock)
    fun confirmUnlock() {
        val tip = _uiState.value.selectedTipToUnlock ?: return
        val currentPoints = _uiState.value.userRizzPoints

        if (currentPoints >= tip.price) {
            // Đủ điểm -> Trừ điểm & Mở khóa
            val updatedTips = _uiState.value.tips.map {
                if (it.id == tip.id) it.copy(isLocked = false) else it
            }

            _uiState.update {
                it.copy(
                    tips = updatedTips,
                    userRizzPoints = currentPoints - tip.price,
                    selectedTipToUnlock = null // Đóng dialog
                )
            }
            // TODO: Gọi Repository để lưu update lên Firestore
        } else {
            // Không đủ điểm
            _uiState.update { it.copy(unlockError = "Bạn thiếu ${tip.price - currentPoints} điểm RIZZ!") }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(selectedTipToUnlock = null) }
    }
}