package com.example.wink.ui.features.setting

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private val sharedPreferences = application.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(AiSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAiSettings()
    }

    private fun loadAiSettings() {
        viewModelScope.launch {
            val aiName = sharedPreferences.getString("ai_name", "Lan Anh") ?: "Lan Anh"
            val aiAvatarUri = sharedPreferences.getString("ai_avatar_uri", null)
            _uiState.value = AiSettingsUiState(
                aiName = aiName,
                aiAvatarUri = aiAvatarUri?.let { Uri.parse(it) }
            )
        }
    }

    fun onAiNameChange(name: String) {
        _uiState.value = _uiState.value.copy(aiName = name)
    }

    fun onAvatarSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(aiAvatarUri = uri)
    }

    fun saveAiSettings() {
        viewModelScope.launch {
            sharedPreferences.edit()
                .putString("ai_name", _uiState.value.aiName)
                .putString("ai_avatar_uri", _uiState.value.aiAvatarUri?.toString())
                .apply()
        }
    }
}

data class AiSettingsUiState(
    val aiName: String = "Lan Anh",
    val aiAvatarUri: Uri? = null,
)
