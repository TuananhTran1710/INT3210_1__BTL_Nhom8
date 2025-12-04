package com.example.wink.ui.features.iconshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.graphics.Color

@HiltViewModel
class IconShopViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IconShopState())
    val uiState: StateFlow<IconShopState> = _uiState.asStateFlow()

    init {
        observeUserRizz()
        initIcons()
    }

    // Lấy điểm RIZZ hiện tại từ currentUser
    private fun observeUserRizz() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _uiState.update { state ->
                    state.copy(
                        rizzPoints = user?.rizzPoints ?: 0,
                        isLoading = false
                    )
                }
            }
        }
    }

    // Khởi tạo bộ icon: 1 icon free, còn lại phải mua
    private fun initIcons() {
        val defaultIcons = listOf(
            IconItemUi("icon_1", 0,  true,  true,  Color(0xFFFFC1CC)), // free + đang dùng
            IconItemUi("icon_2", 200, false, false, Color(0xFFBCAAA4)),
            IconItemUi("icon_3", 200, false, false, Color(0xFFF8BBD0)),
            IconItemUi("icon_4", 200, false, false, Color(0xFFE1BEE7)),
            IconItemUi("icon_5", 300, false, false, Color(0xFFFFF59D)),
            IconItemUi("icon_6", 300, false, false, Color(0xFFC5E1A5)),
            IconItemUi("icon_7", 300, false, false, Color(0xFFB3E5FC)),
            IconItemUi("icon_8", 400, false, false, Color(0xFFFFCDD2)),
            IconItemUi("icon_9", 400, false, false, Color(0xFFE3F2FD)),
            IconItemUi("icon_10",500, false, false, Color(0xFFF48FB1)),
        )
        _uiState.update { it.copy(icons = defaultIcons) }
    }

    fun onIconClicked(iconId: String) {
        val current = _uiState.value
        val clicked = current.icons.firstOrNull { it.id == iconId } ?: return

        // Nếu đã sở hữu → chỉ set selected
        if (clicked.isOwned) {
            val updated = current.icons.map {
                if (it.id == iconId) it.copy(isSelected = true)
                else it.copy(isSelected = false)
            }
            _uiState.value = current.copy(icons = updated, errorMessage = null)
            return
        }

        // Chưa sở hữu → kiểm tra đủ RIZZ chưa
        if (current.rizzPoints < clicked.price) {
            _uiState.value = current.copy(
                errorMessage = "Bạn không đủ RIZZ để mua icon này."
            )
            return
        }

        val newRizz = current.rizzPoints - clicked.price
        val updatedIcons = current.icons.map {
            when {
                it.id == iconId -> it.copy(isOwned = true, isSelected = true)
                else -> it.copy(isSelected = false)
            }
        }

        _uiState.value = current.copy(
            rizzPoints = newRizz,
            icons = updatedIcons,
            errorMessage = null
        )

        // TODO: nếu muốn lưu xuống Firestore → gọi hàm update RIZZ + icon ở AuthRepository
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
