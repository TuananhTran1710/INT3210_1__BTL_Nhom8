package com.example.wink.ui.features.tarot.zodiac

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class TarotZodiacEvent {
    object NavigateToResult : TarotZodiacEvent()
}

@HiltViewModel
class TarotZodiacViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(TarotZodiacState())
    val uiState: StateFlow<TarotZodiacState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TarotZodiacEvent>()
    val events = _events.asSharedFlow()

    fun onYourSignSelected(sign: ZodiacSign) {
        _uiState.update { it.copy(yourSign = sign) }
    }

    fun onCrushSignSelected(sign: ZodiacSign) {
        _uiState.update { it.copy(crushSign = sign) }
    }

    fun onAnalyze() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Giả lập loading
            delay(600)

            val result = randomZodiacResult(s.yourSign, s.crushSign)
            ZodiacResultCache.lastResult = result

            _uiState.update { it.copy(isLoading = false) }

            _events.emit(TarotZodiacEvent.NavigateToResult)
        }
    }
}
