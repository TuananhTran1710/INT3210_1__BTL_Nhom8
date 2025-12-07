package com.example.wink.ui.features.onboarding

data class OnboardingState(
    val currentPage: Int = 0,

    val selectedGender: String = "",
    val selectedPreference: String = "",

    // SỬA: Dùng List để lưu nhiều tính cách
    val selectedPersonalities: List<String> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false
)