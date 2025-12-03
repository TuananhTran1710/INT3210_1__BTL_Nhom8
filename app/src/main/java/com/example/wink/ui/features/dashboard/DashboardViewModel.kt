package com.example.wink.ui.features.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(
        DashboardState(
            rizzPoints = 0,
            dailyStreak = 0,
            hasDailyCheckIn = false,
            isAIUnlocked = true,
            isLoading = false
        )
    )


    // Combine user data from auth repository with dashboard state
    val uiState: StateFlow<DashboardState> = combine(
        authRepository.currentUser,
        _dashboardState
    ) { user, dashboardState ->
        dashboardState.copy(
            userEmail = user?.email ?: "Không tìm thấy Email",
            username = user?.username ?: user?.email?.substringBefore("@") ?: "Người dùng",
            rizzPoints = user?.rizzPoints ?: dashboardState.rizzPoints,
            dailyStreak = user?.loginStreak ?: dashboardState.dailyStreak,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DashboardState(isLoading = true)
    )

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.OnDailyCheckIn -> {
                handleDailyCheckIn()
            }

            is DashboardEvent.OnStartAIChat -> {
                // Handle AI chat start
                // Could navigate to AI chat screen
            }

            is DashboardEvent.OnCompleteTask -> {
                handleTaskCompletion()
            }

            is DashboardEvent.OnClaimTaskReward -> {
                handleClaimTaskReward()
            }

            is DashboardEvent.OnPlayGame -> {
                // Handle game start
            }

            is DashboardEvent.OnClaimReward -> {
                handleClaimReward()
            }

            is DashboardEvent.OnRefresh -> {
                refreshDashboard()
            }

            is DashboardEvent.OnShowDetails -> {
                // Handle showing details for specific item
            }

            else -> {
                // Handle navigation events in the UI layer
            }
        }
    }

    private fun handleDailyCheckIn() {
        viewModelScope.launch {
            val currentState = _dashboardState.value
            if (currentState.hasDailyCheckIn) return@launch

            _dashboardState.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                // Đảm bảo user doc tồn tại nếu anh còn dùng hàm này
                // authRepository.ensureUserDocumentExists().getOrThrow()

                authRepository.performDailyCheckIn().getOrThrow()

                _dashboardState.value = _dashboardState.value.copy(
                    hasDailyCheckIn = true,
                    isLoading = false
                    // Không cần cộng thêm điểm / streak ở đây,
                    // vì combine(...) sẽ nhận user mới từ Firestore.
                )

                Log.d("DashboardViewModel", "Daily check-in completed successfully")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Daily check-in failed: ${e.message}", e)
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    errorMessage = "Check-in thất bại: ${e.message}"
                )
            }
        }
    }


    private fun handleTaskCompletion() {
        viewModelScope.launch {
            val currentState = _dashboardState.value
            val updatedTasks = currentState.dailyTasks.map { task ->
                if (!task.isCompleted) {
                    task.copy(isCompleted = true)
                } else task
            }

            val completedTasksReward = updatedTasks.sumOf { if (it.isCompleted) it.reward else 0 }

            _dashboardState.value = currentState.copy(
                dailyTasks = updatedTasks,
                rizzPoints = currentState.rizzPoints + completedTasksReward
            )
        }
    }

    private fun handleClaimTaskReward() {
        viewModelScope.launch {
            val currentState = _dashboardState.value
            val rewardAmount = 50 // Example reward amount

            _dashboardState.value = currentState.copy(
                rizzPoints = currentState.rizzPoints + rewardAmount
            )
        }
    }

    private fun handleClaimReward() {
        viewModelScope.launch {
            // Handle reward claiming logic
            val currentState = _dashboardState.value
            _dashboardState.value = currentState.copy(
                rizzPoints = currentState.rizzPoints + 100
            )
        }
    }

    private fun refreshDashboard() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isRefreshing = true)

            // Simulate refresh delay
            kotlinx.coroutines.delay(1000)

            _dashboardState.value = _dashboardState.value.copy(
                isRefreshing = false,
                errorMessage = null
            )
        }
    }
}