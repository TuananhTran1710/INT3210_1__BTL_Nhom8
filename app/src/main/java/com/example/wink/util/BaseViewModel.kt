package com.example.wink.util

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel<S, E> : ViewModel() {

    // S = State (trạng thái của UI)
    // E = Event (hành động của người dùng)

    protected val _uiState: MutableStateFlow<S>
    abstract val uiState: StateFlow<S>

    init {
        _uiState = MutableStateFlow(getInitialState())
    }

    abstract fun getInitialState(): S

    abstract fun onEvent(event: E)
}