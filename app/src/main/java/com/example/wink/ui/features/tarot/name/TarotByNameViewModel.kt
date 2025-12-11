package com.example.wink.ui.features.tarot.name

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

sealed class TarotNameNav {
    data class ShowResult(val yourName: String, val crushName: String) : TarotNameNav()
}

@HiltViewModel
class TarotNameViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(TarotNameState())
    val uiState: StateFlow<TarotNameState> = _uiState

    private val _navEvents = MutableSharedFlow<TarotNameNav>()
    val navEvents = _navEvents.asSharedFlow()

    fun onYourNameChange(value: String) {
        _uiState.update { it.copy(yourName = value, errorMessage = null) }
    }

    fun onCrushNameChange(value: String) {
        _uiState.update { it.copy(crushName = value, errorMessage = null) }
    }

    fun onAnalyze() {
        val s = _uiState.value
        if (s.yourName.isBlank() || s.crushName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập đầy đủ cả 2 tên.") }
            return
        }

        viewModelScope.launch {
            _navEvents.emit(
                TarotNameNav.ShowResult(
                    yourName = s.yourName.trim(),
                    crushName = s.crushName.trim()
                )
            )
        }
    }
}
