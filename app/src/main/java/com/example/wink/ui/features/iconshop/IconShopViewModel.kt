package com.example.wink.ui.features.iconshop

import android.content.Context
import androidx.compose.ui.graphics.Color
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

// Cập nhật class nội bộ này
private data class IconMeta(
    val id: String,
    val price: Int,
    val iconResId: Int // Đổi từ Color sang Int
)
@HiltViewModel
class IconShopViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(IconShopState())
    val uiState: StateFlow<IconShopState> = _uiState.asStateFlow()

    // cấu hình bộ icon gốc
    // CẤU HÌNH BỘ ICON MỚI TẠI ĐÂY
    // Hãy thay R.drawable.icon_fire, v.v. bằng tên file ảnh thực tế bạn đã đặt ở Bước 1
    private val baseIcons = listOf(
        IconMeta("default_logo", 0,   R.drawable.default_logo),   // Ví dụ: Icon mặc định (Miễn phí)
        IconMeta("lost_streak", 200, R.drawable.lost_streak),    // Ví dụ: Icon băng
        IconMeta("day3", 50, R.drawable.day3), // Ví dụ: Icon vũ trụ
        IconMeta("day7", 100, R.drawable.day7),   // Ví dụ: Icon vàng
        IconMeta("day14", 200, R.drawable.day14),   // Ví dụ: Icon vàng
        IconMeta("day30_plus", 500, R.drawable.day30_plus),   // Ví dụ: Icon vàng
        IconMeta("day100", 1500, R.drawable.day100),   // Ví dụ: Icon vàng
        IconMeta("day200", 5000, R.drawable.day200),   // Ví dụ: Icon vàng
        IconMeta("day365", 25000, R.drawable.day365),   // Ví dụ: Icon vàng
        IconMeta("day500", 100000, R.drawable.day500),   // Ví dụ: Icon vàng
        IconMeta("day1000", 1000000, R.drawable.day1000),   // Ví dụ: Icon vàng
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
                // Map sang UI model mới
                IconItemUi(
                    id = meta.id,
                    price = meta.price,
                    isOwned = isOwned,
                    isSelected = isSelected,
                    iconResId = meta.iconResId // Truyền ID ảnh vào đây
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

    // CẬP NHẬT HÀM NÀY
    fun onIconClicked(iconId: String) {
        val current = _uiState.value
        if (current.isLoading) return

        val clicked = current.icons.firstOrNull { it.id == iconId } ?: return

        viewModelScope.launch {
            // 1. Nếu đã sở hữu -> Đổi icon server VÀ Đổi icon app
            if (clicked.isOwned) {
                val ownedIds = current.icons.filter { it.isOwned }.map { it.id }

                // Gọi server
                updateIconStateOnServer(current.rizzPoints, ownedIds, iconId)

                // GỌI HÀM ĐỔI ICON APP
                changeLauncherIcon(iconId)

                return@launch
            }

            // 2. Nếu chưa sở hữu -> Mua -> Đổi icon server -> Đổi icon app
            if (current.rizzPoints < clicked.price) {
                _uiState.update { it.copy(errorMessage = "Bạn không đủ RIZZ để mua icon này.") }
                return@launch
            }

            val spendOk = userRepository.spendRizz(clicked.price)
            if (!spendOk) {
                _uiState.update { it.copy(errorMessage = "Không thể trừ RIZZ. Vui lòng thử lại.") }
                return@launch
            }

            val newRizz = current.rizzPoints - clicked.price
            val newOwnedIds = current.icons.filter { it.isOwned }.map { it.id } + iconId

            // Gọi server
            updateIconStateOnServer(newRizz, newOwnedIds, iconId)

            // GỌI HÀM ĐỔI ICON APP
            changeLauncherIcon(iconId)
        }
    }

    // Hàm phụ trợ gọi AppIconManager
    private fun changeLauncherIcon(iconId: String) {
        try {
            AppIconManager.changeAppIcon(context, iconId)
        } catch (e: Exception) {
            e.printStackTrace()
            // Có thể hiện thông báo lỗi nhẹ, nhưng thường ko cần thiết chặn UI
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
