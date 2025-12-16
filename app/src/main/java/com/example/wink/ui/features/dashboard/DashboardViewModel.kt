package com.example.wink.ui.features.dashboard

import android.util.Log
import androidx.compose.animation.core.snap
import androidx.lifecycle.viewModelScope
import com.example.wink.data.model.Notification
import com.example.wink.data.model.NotificationType
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.FriendRequestRepository
import com.example.wink.data.repository.TaskRepository
import com.example.wink.util.BaseViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val taskRepository: TaskRepository
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
        observeFriendRequests()
        observeAcceptedFriendRequests()
        initializeDailyTasks()
    }

    private fun initializeDailyTasks() {
        viewModelScope.launch {
            taskRepository.checkAndGenerateDailyTasks()
            taskRepository.getDailyTasks().collectLatest { newTasks ->
                _uiState.update { state ->
                    state.copy(
                        dailyTasks = newTasks,
                    )
                }
            }
        }
    }

    // Nhận event từ UI
    override fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.OnDailyCheckIn -> handleDailyCheckIn()
            is DashboardEvent.OnStartAIChat -> { /* điều hướng màn AI chat */ }
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
            // Friend request events
            is DashboardEvent.OnOpenFriendRequests -> {
                _uiState.value = _uiState.value.copy(showFriendRequestsDialog = true)
            }
            is DashboardEvent.OnCloseFriendRequests -> {
                _uiState.value = _uiState.value.copy(showFriendRequestsDialog = false)
            }
            is DashboardEvent.OnAcceptFriendRequest -> handleAcceptFriendRequest(event.requestId)
            is DashboardEvent.OnRejectFriendRequest -> handleRejectFriendRequest(event.requestId)
            is DashboardEvent.OnClearAcceptedNotification -> {
                _uiState.value = _uiState.value.copy(acceptedFriendNotification = null)
            }
            // Notification events
            is DashboardEvent.OnOpenNotifications -> {
                _uiState.value = _uiState.value.copy(showNotificationsDialog = true)
            }
            is DashboardEvent.OnCloseNotifications -> {
                _uiState.value = _uiState.value.copy(showNotificationsDialog = false)
            }
            is DashboardEvent.OnMarkNotificationRead -> handleMarkNotificationRead(event.notificationId)
            is DashboardEvent.OnClearAllNotifications -> handleClearAllNotifications()
            is DashboardEvent.OnClearTaskNotification -> {
                _uiState.update { it.copy(completedTaskNotification = null) }
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
                    username = user?.username ?: "Người dùng",
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

    /** Lắng nghe danh sách lời mời kết bạn realtime */
    private fun observeFriendRequests() {
        viewModelScope.launch {
            friendRequestRepository.listenPendingRequests().collectLatest { requests ->
                // Cập nhật pending friend requests
                _uiState.value = _uiState.value.copy(
                    pendingFriendRequests = requests
                )
                
                // Xử lý tạo notifications trên background thread để tránh block main thread
                val mergedNotifications = withContext(Dispatchers.Default) {
                    // Tạo notifications từ friend requests
                    val friendRequestNotifications = requests.map { request ->
                        Notification(
                            id = "fr_${request.id}",
                            type = NotificationType.FRIEND_REQUEST,
                            title = "Lời mời kết bạn",
                            message = "${request.fromUsername.ifBlank { "Người dùng" }} muốn kết bạn với bạn",
                            fromUserId = request.fromUserId,
                            fromUsername = request.fromUsername,
                            fromAvatarUrl = request.fromAvatarUrl,
                            relatedId = request.id,
                            isRead = false,
                            createdAt = request.createdAt
                        )
                    }
                    
                    // Merge với các notification khác (không phải friend request)
                    val otherNotifications = _uiState.value.notifications.filter { 
                        it.type != NotificationType.FRIEND_REQUEST 
                    }
                    
                    (friendRequestNotifications + otherNotifications)
                        .sortedByDescending { it.createdAt }
                }
                
                _uiState.value = _uiState.value.copy(
                    notifications = mergedNotifications
                )
                
                Log.d("DashboardViewModel", "Received ${requests.size} pending friend requests")
            }
        }
    }

    /** Chấp nhận lời mời kết bạn */
    private fun handleAcceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            try {
                friendRequestRepository.acceptFriendRequest(requestId).getOrThrow()
                Log.d("DashboardViewModel", "Accepted friend request: $requestId")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to accept friend request", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Không thể chấp nhận lời mời: ${e.message}"
                )
            }
        }
    }

    /** Từ chối lời mời kết bạn */
    private fun handleRejectFriendRequest(requestId: String) {
        viewModelScope.launch {
            try {
                friendRequestRepository.rejectFriendRequest(requestId).getOrThrow()
                Log.d("DashboardViewModel", "Rejected friend request: $requestId")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to reject friend request", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Không thể từ chối lời mời: ${e.message}"
                )
            }
        }
    }

    /** Lắng nghe lời mời kết bạn đã được chấp nhận (để hiển thị thông báo) */
    private val notifiedRequestIds = mutableSetOf<String>()
    
    private fun observeAcceptedFriendRequests() {
        viewModelScope.launch {
            friendRequestRepository.listenAcceptedRequests().collectLatest { acceptedRequests ->
                // Xử lý trên IO thread để tránh block main thread
                withContext(Dispatchers.IO) {
                    // Chỉ hiển thị thông báo cho các request mới được accept
                    for (request in acceptedRequests) {
                        if (!notifiedRequestIds.contains(request.id)) {
                            notifiedRequestIds.add(request.id)
                            
                            // Lấy thông tin user đã accept (IO operation)
                            val acceptedUser = friendRequestRepository.getUserById(request.toUserId)
                            val userName = acceptedUser?.username ?: "Người dùng"
                            
                            // Thêm notification vào danh sách
                            val newNotification = Notification(
                                id = "fra_${request.id}",
                                type = NotificationType.FRIEND_REQUEST_ACCEPTED,
                                title = "Lời mời kết bạn được chấp nhận",
                                message = "$userName đã chấp nhận lời mời kết bạn của bạn!",
                                fromUserId = request.toUserId,
                                fromUsername = userName,
                                fromAvatarUrl = acceptedUser?.avatarUrl ?: "",
                                relatedId = request.id,
                                isRead = false,
                                createdAt = Timestamp.now()
                            )
                            
                            val updatedNotifications = listOf(newNotification) + _uiState.value.notifications
                            
                            // Update UI state on main thread
                            withContext(Dispatchers.Main) {
                                _uiState.value = _uiState.value.copy(
                                    notifications = updatedNotifications,
                                    acceptedFriendNotification = "$userName đã chấp nhận lời mời kết bạn của bạn!"
                                )
                            }
                            
                            // Xóa request sau khi đã thông báo
                            friendRequestRepository.markRequestAsNotified(request.id)
                            
                            Log.d("DashboardViewModel", "Friend request accepted by: $userName")
                        }
                    }
                }
            }
        }
    }
    
    /** Đánh dấu thông báo đã đọc */
    private fun handleMarkNotificationRead(notificationId: String) {
        val updatedNotifications = _uiState.value.notifications.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }
        _uiState.value = _uiState.value.copy(notifications = updatedNotifications)
    }
    
    /** Xóa tất cả thông báo đã đọc */
    private fun handleClearAllNotifications() {
        // Chỉ xóa các thông báo đã đọc, giữ lại friend requests chưa xử lý
        val remainingNotifications = _uiState.value.notifications.filter { notification ->
            notification.type == NotificationType.FRIEND_REQUEST && !notification.isRead
        }
        _uiState.value = _uiState.value.copy(notifications = remainingNotifications)
    }
    
    /** Thêm thông báo mới (có thể gọi từ các nơi khác) */
    fun addNotification(
        type: NotificationType,
        title: String,
        message: String,
        fromUserId: String = "",
        fromUsername: String = "",
        fromAvatarUrl: String = "",
        relatedId: String = ""
    ) {
        val newNotification = Notification(
            id = UUID.randomUUID().toString(),
            type = type,
            title = title,
            message = message,
            fromUserId = fromUserId,
            fromUsername = fromUsername,
            fromAvatarUrl = fromAvatarUrl,
            relatedId = relatedId,
            isRead = false,
            createdAt = Timestamp.now()
        )
        
        val updatedNotifications = listOf(newNotification) + _uiState.value.notifications
        _uiState.value = _uiState.value.copy(notifications = updatedNotifications)
    }
}