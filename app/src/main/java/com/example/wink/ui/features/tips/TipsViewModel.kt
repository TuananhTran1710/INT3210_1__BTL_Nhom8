package com.example.wink.ui.features.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Tip
import com.example.wink.data.model.User
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.TaskRepository
import com.example.wink.data.repository.TipsRepository // Import mới
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TipsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tipsRepository: TipsRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TipsState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    // Lưu trữ danh sách gốc tải từ Server
    private var rawTips: List<Tip> = emptyList()
    private var currentUser: User? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Tải danh sách Tips từ Firestore
            val tipsResult = tipsRepository.getTips()
            if (tipsResult.isSuccess) {
                rawTips = tipsResult.getOrDefault(emptyList())
            }

            // 2. Lắng nghe User để cập nhật trạng thái Lock/Unlock realtime
            authRepository.currentUser.collectLatest { user ->
                currentUser = user

                // Logic tính toán trạng thái khóa
                val updatedTips = rawTips.map { tip ->
                    // Mở khóa nếu: Giá = 0 HOẶC User đã mua
                    val isUnlocked = tip.price == 0 || (user?.unlockedTips?.contains(tip.id) == true)
                    tip.copy(isLocked = !isUnlocked)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tips = updatedTips,
                        userRizzPoints = user?.rizzPoints ?: 0
                    )
                }
            }
        }
    }

    // Xử lý Unlock (Mua)
    fun confirmUnlock() {
        val tip = _uiState.value.selectedTipToUnlock ?: return
        val user = currentUser ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Gọi Repository thực hiện giao dịch trừ tiền
            val result = tipsRepository.unlockTip(user.uid, tip.id, tip.price)

            if (result.isSuccess) {
                // Thành công: Đóng dialog, tắt loading
                // (UI sẽ tự update nhờ cái collectLatest ở trên lắng nghe Firestore thay đổi)
                _uiState.update { it.copy(isLoading = false, selectedTipToUnlock = null) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        unlockError = result.exceptionOrNull()?.message ?: "Lỗi giao dịch"
                    )
                }
            }
        }
    }

    // Các hàm onTipClick, dismissDialog GIỮ NGUYÊN như cũ
    fun onTipClick(tip: Tip) {
        if (tip.isLocked) {
            _uiState.update { it.copy(selectedTipToUnlock = tip, unlockError = null) }
        } else {
            viewModelScope.launch {
                _navigationEvent.emit(tip.id)
                taskRepository.updateTaskProgress("READ_TIP")
            }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(selectedTipToUnlock = null) }
    }
}