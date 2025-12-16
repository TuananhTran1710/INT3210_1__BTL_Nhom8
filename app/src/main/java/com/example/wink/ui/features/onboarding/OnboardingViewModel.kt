package com.example.wink.ui.features.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.User
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    var state by mutableStateOf(OnboardingState())
        private set

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.SelectGender -> state = state.copy(selectedGender = event.gender)
            is OnboardingEvent.SelectPreference -> state = state.copy(selectedPreference = event.preference)

            // LOGIC CHỌN NHIỀU (TOGGLE)
            is OnboardingEvent.TogglePersonality -> {
                val currentList = state.selectedPersonalities.toMutableList()
                if (currentList.contains(event.personality)) {
                    currentList.remove(event.personality) // Bỏ chọn
                } else {
                    currentList.add(event.personality)    // Chọn thêm
                }
                state = state.copy(selectedPersonalities = currentList)
            }

            OnboardingEvent.NextPage -> state = state.copy(currentPage = state.currentPage + 1)
            OnboardingEvent.PreviousPage -> state = state.copy(currentPage = state.currentPage - 1)
            OnboardingEvent.FinishOnboarding -> saveOnboardingResult()
        }
    }

    private fun saveOnboardingResult() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            try {
                // 1. Tạo chuỗi mô tả giới tính quan tâm
                val prefGenderText = when (state.selectedPreference) {
                    "male" -> "Thích Nam"
                    "female" -> "Thích Nữ"
                    "both" -> "Thích cả hai"
                    else -> ""
                }

                // 2. Gộp danh sách tính cách thành chuỗi: "Vui vẻ, Hòa đồng, ..."
                val personalitiesText = state.selectedPersonalities.joinToString(", ")

                // 3. TẠO CHUỖI PREFERENCE DÀI
                // Kết quả ví dụ: "Thích Nữ. Gu: Hài hước, Thông minh"
                val finalPreferenceString = if (personalitiesText.isNotEmpty()) {
                    "$prefGenderText. Gu: $personalitiesText"
                } else {
                    prefGenderText
                }

                // Chỉ cập nhật gender và preference, giữ nguyên username đã đăng ký
                val result = authRepo.updateUserPreferences(
                    gender = state.selectedGender,
                    preference = finalPreferenceString,
                    personalities = state.selectedPersonalities
                )

                if (result.isSuccess) {
                    state = state.copy(isLoading = false, isSavedSuccess = true)
                } else {
                    state = state.copy(isLoading = false, errorMessage = "Lưu thất bại")
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}