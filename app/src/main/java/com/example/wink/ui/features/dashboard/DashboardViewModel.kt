package com.example.wink.ui.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wink.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    authRepository: AuthRepository // 1. Tiêm (inject) AuthRepository
) : ViewModel() {

    // 2. Lắng nghe `currentUser` và biến nó thành `DashboardState`
    val uiState = authRepository.currentUser
        .map { user ->
            DashboardState(
                userEmail = user?.email ?: "Không tìm thấy Email", // Lấy email
                isLoading = false
            )
        }
        .stateIn(
            // 3. Biến Flow thành StateFlow để UI có thể dùng
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DashboardState(isLoading = true) // Trạng thái ban đầu
        )
}