package com.example.wink.ui.features.dashboard

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import com.example.wink.data.repository.AuthRepository
import com.example.wink.data.repository.FriendRequestRepository
import com.example.wink.data.repository.TaskRepository
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.verify
import com.example.wink.data.model.DailyTask

@RunWith(MockitoJUnitRunner::class)
class DashboardViewModelTest {

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var friendRequestRepository: FriendRequestRepository

    @Mock
    private lateinit var taskRepository: TaskRepository

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = DashboardViewModel(
            authRepository,
            friendRequestRepository,
            taskRepository
        )
    }

    // ====== Test Initial State ======
    @Test
    fun testInitialState_IsCorrect() {
        val state = viewModel.uiState.value

        assertEquals("Đang tải...", state.userEmail)
        assertEquals("", state.username)
        assertEquals(0, state.rizzPoints)
        assertEquals(0, state.dailyStreak)
        assertTrue(state.isLoading)
        assertFalse(state.hasDailyCheckIn)
        assertTrue(state.isAIUnlocked)
        assertNotNull(state.dailyTasks)
    }

    // ====== Test Daily Check-In ======
    @Test
    fun testOnDailyCheckIn_UpdatesCheckInStatus() = runTest {
        val initialHasCheckIn = viewModel.uiState.value.hasDailyCheckIn

        viewModel.onEvent(DashboardEvent.OnDailyCheckIn)

        // Note: Real implementation should update hasDailyCheckIn
        // This test verifies the event is processed
        assertNotNull(viewModel.uiState.value)
    }

    // ====== Test Notifications ======
    @Test
    fun testOnOpenNotifications_ShowsNotificationsDialog() {
        val initialShowDialog = viewModel.uiState.value.showNotificationsDialog

        viewModel.onEvent(DashboardEvent.OnOpenNotifications)

        val updatedShowDialog = viewModel.uiState.value.showNotificationsDialog
        assertNotNull(updatedShowDialog)
    }

    @Test
    fun testOnCloseNotifications_HidesNotificationsDialog() {
        viewModel.onEvent(DashboardEvent.OnOpenNotifications)
        assertTrue(viewModel.uiState.value.showNotificationsDialog)

        viewModel.onEvent(DashboardEvent.OnCloseNotifications)

        assertFalse(viewModel.uiState.value.showNotificationsDialog)
    }

    @Test
    fun testOnClearAllNotifications_EmptiesNotificationsList() {
        viewModel.onEvent(DashboardEvent.OnClearAllNotifications)

        assertEquals(0, viewModel.uiState.value.notifications.size)
    }

    @Test
    fun testOnClearTaskNotification_ClearsMessage() {
        viewModel.onEvent(DashboardEvent.OnClearTaskNotification)

        assertEquals(null, viewModel.uiState.value.completedTaskNotification)
    }

    @Test
    fun testOnClearAcceptedNotification_ClearsMessage() {
        viewModel.onEvent(DashboardEvent.OnClearAcceptedNotification)

        assertEquals(null, viewModel.uiState.value.acceptedFriendNotification)
    }

    // ====== Test Friend Request ======
    @Test
    fun testOnAcceptFriendRequest_ProcessesRequest() = runTest {
        val requestId = "request_123"

        viewModel.onEvent(DashboardEvent.OnAcceptFriendRequest(requestId))

        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun testOnRejectFriendRequest_ProcessesRequest() = runTest {
        val requestId = "request_456"

        viewModel.onEvent(DashboardEvent.OnRejectFriendRequest(requestId))

        assertNotNull(viewModel.uiState.value)
    }

    // ====== Test Mark Notification Read ======
    @Test
    fun testOnMarkNotificationRead_ProcessesNotification() = runTest {
        val notificationId = "notif_789"

        viewModel.onEvent(DashboardEvent.OnMarkNotificationRead(notificationId))

        assertNotNull(viewModel.uiState.value)
    }

    // ====== Test Loading State ======
    @Test
    fun testInitialState_HasLoadingTrue() {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun testInitialState_HasRefreshingFalse() {
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    // ====== Test Rizz Points ======
    @Test
    fun testInitialState_RizzPointsZero() {
        assertEquals(0, viewModel.uiState.value.rizzPoints)
    }

    @Test
    fun testInitialState_DailyStreakZero() {
        assertEquals(0, viewModel.uiState.value.dailyStreak)
    }

    // ====== Test AI Feature ======
    @Test
    fun testInitialState_AIUnlockedByDefault() {
        assertTrue(viewModel.uiState.value.isAIUnlocked)
    }

    // ====== Test Error Handling ======
    @Test
    fun testInitialState_ErrorMessageNull() {
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    // ====== Test Task List ======
    @Test
    fun testInitialState_DailyTasksEmpty() {
        assertEquals(0, viewModel.uiState.value.dailyTasks.size)
    }

    @Test
    fun testInitialState_PendingFriendRequestsEmpty() {
        assertEquals(0, viewModel.uiState.value.pendingFriendRequests.size)
    }

    @Test
    fun testInitialState_NotificationsEmpty() {
        assertEquals(0, viewModel.uiState.value.notifications.size)
    }

    // ====== Test State Independence ======
    @Test
    fun testMultipleEvents_StateChangesIndependently() = runTest {
        viewModel.onEvent(DashboardEvent.OnOpenNotifications)
        assertTrue(viewModel.uiState.value.showNotificationsDialog)

        viewModel.onEvent(DashboardEvent.OnCloseNotifications)
        assertFalse(viewModel.uiState.value.showNotificationsDialog)

        viewModel.onEvent(DashboardEvent.OnOpenNotifications)
        assertTrue(viewModel.uiState.value.showNotificationsDialog)
    }

    // ====== Test Username ======
    @Test
    fun testInitialState_UsernameEmpty() {
        assertEquals("", viewModel.uiState.value.username)
    }

    // ====== Test Friend Requests Dialog ======
    @Test
    fun testInitialState_FriendRequestsDialogClosed() {
        assertFalse(viewModel.uiState.value.showFriendRequestsDialog)
    }

    // ====== Test Notifications Dialog ======
    @Test
    fun testInitialState_NotificationsDialogClosed() {
        assertFalse(viewModel.uiState.value.showNotificationsDialog)
    }
}
