package com.example.wink.ui.features.onboarding

sealed class OnboardingEvent {
    data class SelectGender(val gender: String) : OnboardingEvent()
    data class SelectPreference(val preference: String) : OnboardingEvent()
    data class TogglePersonality(val personality: String) : OnboardingEvent()
    data object NextPage : OnboardingEvent()
    data object PreviousPage : OnboardingEvent()
    data object FinishOnboarding : OnboardingEvent()
}