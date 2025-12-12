// File: ui/features/tarot/TarotHubViewModel.kt
package com.example.wink.ui.features.tarot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class TarotHubNav {
    data class OpenFeature(val type: LoveFortuneType) : TarotHubNav()
}

@HiltViewModel
class TarotHubViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TarotHubState())
    val uiState: StateFlow<TarotHubState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<TarotHubNav>()
    val navEvents = _navEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            val todayEpochDay = LocalDate.now().toEpochDay()

            // 1. load điểm Rizz
            val rizz = userRepository.loadRizzPoints()

            // 2. load map free usage từ Firestore
            val usageMap = userRepository.getTarotFreeUsage()  // key = "BY_NAME", ...

            _uiState.update { old ->
                old.copy(
                    rizzPoints = rizz,
                    features = old.features.map { f ->
                        val lastEpoch = usageMap[f.type.name]  // lấy theo enum name
                        val usedToday = (lastEpoch != null && lastEpoch == todayEpochDay)
                        f.copy(usedFreeToday = usedToday)
                    }
                )
            }
        }
    }

    fun onFeatureClick(type: LoveFortuneType) {
        viewModelScope.launch {
            val state = _uiState.value
            val feature = state.features.first { it.type == type }
            val todayEpochDay = LocalDate.now().toEpochDay()

            if (!feature.usedFreeToday) {
                // CHƯA dùng free hôm nay -> đánh dấu vào Firestore
                userRepository.markTarotFreeUsedToday(type.name, todayEpochDay)

                _uiState.update { s ->
                    s.copy(
                        features = s.features.map {
                            if (it.type == type) it.copy(usedFreeToday = true) else it
                        }
                    )
                }
                navigateTo(type)
            } else {
                // ĐÃ dùng free hôm nay -> xử lý Rizz như cũ
                val enough = userRepository.canSpendRizz(feature.price)
                if (!enough) {
                    _uiState.update { it.copy(showNotEnoughDialogFor = type) }
                } else {
                    _uiState.update { it.copy(confirmingFor = type) }
                }
            }
        }
    }

    fun confirmSpendRizz() {
        val type = _uiState.value.confirmingFor ?: return

        viewModelScope.launch {
            val feature = _uiState.value.features.first { it.type == type }
            val ok = userRepository.spendRizz(feature.price)

            if (!ok) {
                // có thể trong lúc show dialog điểm rizz đã thay đổi
                _uiState.update {
                    it.copy(
                        confirmingFor = null,
                        showNotEnoughDialogFor = type
                    )
                }
                return@launch
            }

            // local state - trừ rizz
            _uiState.update { st ->
                st.copy(
                    rizzPoints = st.rizzPoints - feature.price,
                    confirmingFor = null
                )
            }

            navigateTo(type)
        }
    }

    fun dismissDialogs() {
        _uiState.update { it.copy(confirmingFor = null, showNotEnoughDialogFor = null) }
    }

    private fun navigateTo(type: LoveFortuneType) {
        viewModelScope.launch {
            _navEvents.emit(TarotHubNav.OpenFeature(type))
        }
    }
}
