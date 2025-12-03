package com.example.wink.ui.features.dashboard

import android.util.Log
import androidx.compose.animation.core.snap
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import com.example.wink.util.BaseViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<DashboardState, DashboardEvent>() {

    // Cho UI dùng
    override val uiState: StateFlow<DashboardState>
        get() = _uiState

    // State khởi tạo
    override fun getInitialState(): DashboardState = DashboardState(
        userEmail = "",
        username = "",
        rizzPoints = 0,
        dailyStreak = 0,
        hasDailyCheckIn = false,
        isAIUnlocked = true,
        isLoading = true,
        isRefreshing = false,
        errorMessage = null,
        dailyTasks = emptyList()
    )

    init {
        getInitialState()
        observeUser()
    }

    // Nhận event từ UI
    override fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.OnDailyCheckIn -> handleDailyCheckIn()
            is DashboardEvent.OnStartAIChat -> { /* điều hướng màn AI chat */ }
            is DashboardEvent.OnCompleteTask -> handleTaskCompletion()
            is DashboardEvent.OnClaimTaskReward -> handleClaimTaskReward()
            is DashboardEvent.OnPlayGame -> { /* điều hướng game */ }
            is DashboardEvent.OnClaimReward -> handleClaimReward()
            is DashboardEvent.OnRefresh -> refreshDashboard()
            is DashboardEvent.OnShowDetails -> { /* show chi tiết */ }
            is DashboardEvent.OnNavigateToLeaderboard -> {
                // Navigation, để UI xử lý (hoặc emit side-effect nếu anh có)
            }
            is DashboardEvent.OnNavigateToProfile -> {
                // Navigation
            }
            is DashboardEvent.OnNavigateToSettings -> {
                // Navigation
            }
            is DashboardEvent.OnNavigateToShop -> {
                // Navigation
            }
        }
    }

    /** Lắng nghe user từ AuthRepository và merge vào DashboardState */
    private fun observeUser() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                val current = _uiState.value
                val now = com.google.firebase.Timestamp.now()

                val offsetSeconds = TimeZone.getDefault().rawOffset.toLong() / 1000L
                fun dayNumber(ts: com.google.firebase.Timestamp?): Long? {
                    return ts?.seconds?.let { (it + offsetSeconds) / 86400L }
                }
                val todayDay = (now.seconds + offsetSeconds) / 86400L
                val lastAny = user?.lastCheckInDate?:null
                val lastTs = lastAny as? Timestamp
                val lastDay = dayNumber(lastTs)
                _uiState.value = current.copy(
                    userEmail = user?.email ?: "Không tìm thấy Email",
                    username = user?.username ?: user?.email?.substringBefore("@") ?: "Người dùng",
                    rizzPoints = user?.rizzPoints ?: current.rizzPoints,
                    dailyStreak = user?.loginStreak ?: current.dailyStreak,
                    hasDailyCheckIn = (lastDay == todayDay),
                    isLoading = false
                )
                Log.d("dvm","$lastDay:$todayDay:$lastAny")
            }
        }
    }

    private fun handleDailyCheckIn() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.hasDailyCheckIn) return@launch

            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                authRepository.performDailyCheckIn().getOrThrow()

                _uiState.value = _uiState.value.copy(
                    hasDailyCheckIn = true,
                    isLoading = false
                )

                Log.d("DashboardViewModel", "Daily check-in completed successfully")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Daily check-in failed: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Check-in thất bại: ${e.message}"
                )
            }
        }
    }

    private fun handleTaskCompletion() {
        viewModelScope.launch {
            val current = _uiState.value
            val updatedTasks = current.dailyTasks.map { task ->
                if (!task.isCompleted) task.copy(isCompleted = true) else task
            }

            val completedTasksReward = updatedTasks.sumOf { if (it.isCompleted) it.reward else 0 }

            _uiState.value = current.copy(
                dailyTasks = updatedTasks,
                rizzPoints = current.rizzPoints + completedTasksReward
            )
        }
    }

    private fun handleClaimTaskReward() {
        viewModelScope.launch {
            val current = _uiState.value
            val rewardAmount = 50 // Example

            _uiState.value = current.copy(
                rizzPoints = current.rizzPoints + rewardAmount
            )
        }
    }

    private fun handleClaimReward() {
        viewModelScope.launch {
            val current = _uiState.value
            _uiState.value = current.copy(
                rizzPoints = current.rizzPoints + 100
            )
        }
    }

    private fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            kotlinx.coroutines.delay(1000)

            _uiState.value = _uiState.value.copy(
                isRefreshing = false,
                errorMessage = null
            )
        }
    }
}
