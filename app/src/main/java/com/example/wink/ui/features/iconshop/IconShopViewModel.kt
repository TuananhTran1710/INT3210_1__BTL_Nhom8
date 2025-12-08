package com.example.wink.ui.features.iconshop

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class IconMeta(
    val id: String,
    val price: Int,
    val color: Color
)

@HiltViewModel
class IconShopViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IconShopState())
    val uiState: StateFlow<IconShopState> = _uiState.asStateFlow()

    // cấu hình bộ icon gốc
    private val baseIcons = listOf(
        IconMeta("icon_1", 0,   Color(0xFFFFC1CC)),  // free
        IconMeta("icon_2", 200, Color(0xFFBCAAA4)),
        IconMeta("icon_3", 200, Color(0xFFF8BBD0)),
        IconMeta("icon_4", 200, Color(0xFFE1BEE7)),
        IconMeta("icon_5", 300, Color(0xFFFFF59D)),
        IconMeta("icon_6", 300, Color(0xFFC5E1A5)),
        IconMeta("icon_7", 300, Color(0xFFB3E5FC)),
        IconMeta("icon_8", 400, Color(0xFFFFCDD2)),
        IconMeta("icon_9", 400, Color(0xFFE3F2FD)),
        IconMeta("icon_10",500, Color(0xFFF48FB1))
    )

    init {
        viewModelScope.launch {
            loadFromDatabase()
        }
    }

    private suspend fun loadFromDatabase() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val rizz = userRepository.loadRizzPoints()
            val (ownedIds, selectedId) = userRepository.loadIconShopState()

            val icons = baseIcons.mapIndexed { index, meta ->
                val isOwned = meta.price == 0 || ownedIds.contains(meta.id)
                val isSelected = when {
                    selectedId != null -> meta.id == selectedId
                    selectedId == null && meta.price == 0 && index == 0 -> true
                    else -> false
                }
                IconItemUi(
                    id = meta.id,
                    price = meta.price,
                    isOwned = isOwned,
                    isSelected = isSelected,
                    color = meta.color
                )
            }

            _uiState.update {
                it.copy(
                    rizzPoints = rizz,
                    icons = icons,
                    isLoading = false,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Không tải được dữ liệu icon."
                )
            }
        }
    }

    fun onIconClicked(iconId: String) {
        val current = _uiState.value
        if (current.isLoading) return

        val clicked = current.icons.firstOrNull { it.id == iconId } ?: return

        viewModelScope.launch {
            // 1. Nếu đã sở hữu -> chỉ đổi icon đang dùng, không trừ Rizz
            if (clicked.isOwned) {
                val ownedIds = current.icons.filter { it.isOwned }.map { it.id }
                updateIconStateOnServer(
                    newRizz = current.rizzPoints,
                    newOwnedIds = ownedIds,
                    newSelectedId = iconId
                )
                return@launch
            }

            // 2. Nếu chưa sở hữu -> kiểm tra đủ Rizz và mua
            if (current.rizzPoints < clicked.price) {
                _uiState.update {
                    it.copy(errorMessage = "Bạn không đủ RIZZ để mua icon này.")
                }
                return@launch
            }

            // Trừ RIZZ trên database (transaction trong UserRepository)
            val spendOk = userRepository.spendRizz(clicked.price)
            if (!spendOk) {
                _uiState.update {
                    it.copy(errorMessage = "Không thể trừ RIZZ. Vui lòng thử lại.")
                }
                return@launch
            }

            val newRizz = current.rizzPoints - clicked.price
            val newOwnedIds =
                current.icons.filter { it.isOwned }.map { it.id } + iconId

            updateIconStateOnServer(
                newRizz = newRizz,
                newOwnedIds = newOwnedIds,
                newSelectedId = iconId
            )
        }
    }

    private suspend fun updateIconStateOnServer(
        newRizz: Int,
        newOwnedIds: List<String>,
        newSelectedId: String
    ) {
        try {
            userRepository.updateIconShopState(
                ownedIconIds = newOwnedIds,
                selectedIconId = newSelectedId
            )

            val updatedIcons = _uiState.value.icons.map { item ->
                when {
                    item.id == newSelectedId ->
                        item.copy(isOwned = true, isSelected = true)
                    newOwnedIds.contains(item.id) ->
                        item.copy(isOwned = true, isSelected = false)
                    else ->
                        item.copy(isSelected = false)
                }
            }

            _uiState.update {
                it.copy(
                    rizzPoints = newRizz,
                    icons = updatedIcons,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = "Có lỗi khi cập nhật icon. Thử lại sau nhé."
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
