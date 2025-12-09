package com.example.wink.ui.features.tarot.zodiac.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.UserRepository
import com.example.wink.ui.features.tarot.zodiac.ZodiacResultCache
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val RETRY_COST = 50

@HiltViewModel
class TarotZodiacResultViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TarotZodiacResultState())
    val uiState: StateFlow<TarotZodiacResultState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TarotZodiacResultEvent>()
    val events = _events.asSharedFlow()

    init {
        val cache = ZodiacResultCache.lastResult
        if (cache != null) {
            _uiState.update {
                it.copy(
                    score = cache.score,
                    yourSignName = cache.yourSign.displayName,
                    crushSignName = cache.crushSign.displayName,
                    message = cache.message
                )
            }
        } else {
            // Nếu không có cache (vào màn trực tiếp) thì quay lại hub
            viewModelScope.launch {
                _events.emit(TarotZodiacResultEvent.BackToHub)
            }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _events.emit(TarotZodiacResultEvent.BackToHub)
        }
    }

    fun onRetryClicked() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun onDismissDialogs() {
        _uiState.update { it.copy(showConfirmDialog = false, showNotEnoughDialog = false) }
    }

    fun onConfirmUseRizz() {
        viewModelScope.launch {
            val success = userRepository.spendRizz(RETRY_COST)
            if (success) {
                _uiState.update { it.copy(showConfirmDialog = false) }
                _events.emit(TarotZodiacResultEvent.RetryPaid)
            } else {
                _uiState.update {
                    it.copy(showConfirmDialog = false, showNotEnoughDialog = true)
                }
            }
        }
    }

    fun onNotEnoughOk() {
        viewModelScope.launch {
            _events.emit(TarotZodiacResultEvent.BackToHub)
        }
    }
}
