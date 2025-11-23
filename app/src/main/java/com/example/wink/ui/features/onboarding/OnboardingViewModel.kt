package com.example.wink.ui.features.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    var state by mutableStateOf(OnboardingState())
        private set

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.SelectGender -> {
                state = state.copy(selectedGender = event.gender)
            }

            is OnboardingEvent.SelectPreference -> {
                state = state.copy(selectedPreference = event.preference)
            }

            OnboardingEvent.NextPage -> {
                state = state.copy(currentPage = state.currentPage + 1)
            }

            OnboardingEvent.PreviousPage -> {
                state = state.copy(currentPage = state.currentPage - 1)
            }

            OnboardingEvent.FinishOnboarding -> {
                saveOnboardingResult()
            }
        }
    }

    private fun saveOnboardingResult() {
        viewModelScope.launch {
            // repository.saveUserPreference()
        }
    }
}
