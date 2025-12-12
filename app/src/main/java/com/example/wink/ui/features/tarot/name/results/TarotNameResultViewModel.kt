package com.example.wink.ui.features.tarot.name.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.UserRepository
import com.example.wink.ui.features.tarot.name.NameResultCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.random.Random
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class TarotNameResultNav {
    object GoBackToHub : TarotNameResultNav()
    object GoBackToInput : TarotNameResultNav()
}

@HiltViewModel
class TarotNameResultViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val PRICE_RIZZ = 50
    }

    private val _uiState = MutableStateFlow(TarotNameResultState())
    val uiState: StateFlow<TarotNameResultState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<TarotNameResultNav>()
    val navEvents = _navEvents.asSharedFlow()

    /** Được gọi lần đầu từ Screen để khởi tạo dữ liệu */
    fun init(yourName: String, crushName: String) {
        if (_uiState.value.initialized) return
        val cached = NameResultCache.lastResult

        if (cached != null) {
            _uiState.update {
                it.copy(
                    yourName = cached.yourName,
                    crushName = cached.crushName,
                    score = cached.score,
                    message = cached.message,
                    initialized = true
                )
            }
        } else {
            val score = Random.nextInt(0, 101)
            val msg = when {
                score >= 80 -> "Trời sinh một cặp! Hai bạn cực kỳ hợp nhau."
                score >= 50 -> "Có tiềm năng, hãy chủ động nói chuyện nhiều hơn nhé."
                else -> "Hợp nhau kiểu bạn thân hơi nhiều hơn là người yêu."
            }

            _uiState.update {
                it.copy(
                    yourName = yourName,
                    crushName = crushName,
                    score = score,
                    message = msg,
                    initialized = true
                )
            }
        }
    }

    fun onRetryClick() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun dismissDialogs() {
        _uiState.update {
            it.copy(
                showConfirmDialog = false,
                showNotEnoughDialog = false
            )
        }
    }

    /** Người dùng bấm "Chơi tiếp" trong dialog */
    fun confirmRetry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val ok = try {
                userRepository.spendRizz(PRICE_RIZZ)
            } catch (e: Exception) {
                false
            }

            if (ok) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        showConfirmDialog = false
                    )
                }
                _navEvents.emit(TarotNameResultNav.GoBackToInput)
            } else {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        showConfirmDialog = false,
                        showNotEnoughDialog = true
                    )
                }
            }
        }
    }

    /** Ấn nút trong dialog "không đủ điểm" */
    fun backToHubFromNotEnough() {
        viewModelScope.launch {
            _navEvents.emit(TarotNameResultNav.GoBackToHub)
        }
    }
}
