package com.example.wink.ui.features.onboarding

data class OnboardingState(
    val currentPage: Int = 0,

    val selectedGender: String? = null,          // "male", "female", "other"
    val selectedPreference: String? = null,      // "female", "male", "both"
    val selectedPersonality: String? = null,

    // UI State
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
