package com.example.wink.ui.features.iconshop

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.R
import com.example.wink.data.repository.UserRepository
import com.example.wink.util.AppIconManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class IconMeta(
    val id: String,
    val price: Int,
    val iconResId: Int
)

@HiltViewModel
class IconShopViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(IconShopState())
    val uiState: StateFlow<IconShopState> = _uiState.asStateFlow()

    private val baseIcons = listOf(
        IconMeta("default_logo", 0,   R.drawable.default_logo),
        IconMeta("lost_streak", 200, R.drawable.lost_streak),
        IconMeta("day3", 50, R.drawable.day3),
        IconMeta("day7", 100, R.drawable.day7),
        IconMeta("day14", 200, R.drawable.day14),
        IconMeta("day30_plus", 500, R.drawable.day30_plus),
        IconMeta("day100", 1500, R.drawable.day100),
        IconMeta("day200", 5000, R.drawable.day200),
        IconMeta("day365", 25000, R.drawable.day365),
        IconMeta("day500", 100000, R.drawable.day500),
        IconMeta("day1000", 1000000, R.drawable.day1000),
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
                    iconResId = meta.iconResId
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
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }

    // --- LOGIC MỚI: Xử lý MUA và CHỌN ngay lập tức ---
    fun onIconClicked(iconId: String) {
        val current = _uiState.value
        if (current.isLoading) return

        // Nếu icon này đang được chọn rồi thì không làm gì cả
        if (current.icons.find { it.id == iconId }?.isSelected == true) return

        val clicked = current.icons.firstOrNull { it.id == iconId } ?: return

        viewModelScope.launch {
            // Trường hợp 1: Đã sở hữu -> Chỉ cần chọn lại
            if (clicked.isOwned) {
                val ownedIds = current.icons.filter { it.isOwned }.map { it.id }

                // 1. Cập nhật Server + UI NGAY LẬP TỨC
                updateIconStateOnServer(current.rizzPoints, ownedIds, iconId)

                // 2. Sau khi UI đã đổi sang "Đang dùng", mới hiện Dialog hỏi restart
                _uiState.update { it.copy(showRestartDialog = true, pendingIconId = iconId) }
                return@launch
            }

            // Trường hợp 2: Chưa sở hữu -> Mua rồi chọn
            if (current.rizzPoints < clicked.price) {
                _uiState.update { it.copy(errorMessage = "Bạn không đủ RIZZ.") }
                return@launch
            }

            val spendOk = userRepository.spendRizz(clicked.price)
            if (!spendOk) {
                _uiState.update { it.copy(errorMessage = "Lỗi trừ tiền.") }
                return@launch
            }

            val newRizz = current.rizzPoints - clicked.price
            val newOwnedIds = current.icons.filter { it.isOwned }.map { it.id } + iconId

            // 1. Cập nhật Server + UI NGAY LẬP TỨC
            updateIconStateOnServer(newRizz, newOwnedIds, iconId)

            // 2. Sau khi UI đã đổi sang "Đang dùng", mới hiện Dialog hỏi restart
            _uiState.update { it.copy(showRestartDialog = true, pendingIconId = iconId) }
        }
    }

    // --- LOGIC MỚI: Chỉ xử lý việc Restart ---
    fun confirmChangeIcon() {
        val current = _uiState.value
        val iconId = current.pendingIconId ?: return

        // Ẩn dialog
        _uiState.update { it.copy(showRestartDialog = false) }

        // Thực hiện đổi Alias hệ thống và Restart App
        // Vì UI và Server đã được cập nhật ở hàm onIconClicked rồi, không cần làm lại nữa
        applyChangeAndRestart(iconId)
    }

    // --- LOGIC MỚI: Chỉ ẩn dialog ---
    fun cancelChangeIcon() {
        // Người dùng chọn "Để sau": Ẩn dialog.
        // Trạng thái trong App vẫn là "Đang dùng" icon mới (vì đã update server rồi).
        // Icon ngoài màn hình chính vẫn là cũ cho đến khi App được khởi động lại lần sau.
        _uiState.update {
            it.copy(
                showRestartDialog = false,
                pendingIconId = null
            )
        }
    }

    private fun applyChangeAndRestart(iconId: String) {
        try {
            AppIconManager.changeAppIcon(context, iconId)
//            AppIconManager.restartApp(context, iconId)
        } catch (e: Exception) {
            e.printStackTrace()
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

            // Cập nhật list icons để UI đổi trạng thái ngay lập tức
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
            _uiState.update { it.copy(errorMessage = "Có lỗi khi cập nhật icon.") }
        }
    }
}