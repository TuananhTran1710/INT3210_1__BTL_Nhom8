package com.example.wink.ui.features.dashboard

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import com.example.wink.data.model.DailyTask
import com.example.wink.data.model.Notification
import com.example.wink.data.model.NotificationType
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @RelaxedMockK
    lateinit var navController: NavController

    @RelaxedMockK
    lateinit var viewModel: DashboardViewModel

    // Sử dụng đúng tên class DashboardState dựa trên code bạn cung cấp
    private val _uiState = MutableStateFlow(
        DashboardState(
            userEmail = "test@email.com",
            username = "Test User",
            dailyTasks = emptyList() // Khởi tạo list rỗng để tránh lỗi null
        )
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        // Mock ViewModel trả về StateFlow giả
        every { viewModel.uiState } returns _uiState.asStateFlow()
    }

    @Test
    fun dashboard_displays_user_info_and_points() {
        // GIVEN
        _uiState.value = _uiState.value.copy(
            username = "dat",
            rizzPoints = 999,
            dailyStreak = 15,
            hasDailyCheckIn = true
        )

        // WHEN
        composeTestRule.setContent {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // Đợi animation của AnimatedDashboardItem hoàn tất (item đầu delay 0ms)
        composeTestRule.waitForIdle()

        // THEN
        composeTestRule.onNodeWithText("dat").assertIsDisplayed()
        composeTestRule.onNodeWithText("999").assertIsDisplayed() // Điểm Rizz
        composeTestRule.onNodeWithText("15 ngày").assertIsDisplayed() // Streak
        composeTestRule.onNodeWithText("Đã điểm danh").assertIsDisplayed()
    }

    @Test
    fun dashboard_displays_check_in_button_when_not_checked_in() {
        // GIVEN - Chưa điểm danh
        _uiState.value = _uiState.value.copy(
            hasDailyCheckIn = false,
            dailyStreak = 5
        )

        // WHEN
        composeTestRule.setContent {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN
        composeTestRule.onNodeWithText("Điểm danh ngay").assertIsDisplayed()

        // ACTION - Click điểm danh
        composeTestRule.onNodeWithText("Điểm danh ngay").performClick()

        // VERIFY - ViewModel nhận được sự kiện
        verify { viewModel.onEvent(DashboardEvent.OnDailyCheckIn) }
    }

    @Test
    fun dashboard_displays_daily_tasks_correctly() {
        // GIVEN
        val tasks = listOf(
            DailyTask(
                id = "1",
                title = "Chat với AI",
                target = 1,
                currentProgress = 0,
                reward = 50,
                isCompleted = false
            ),
            DailyTask(
                id = "2",
                title = "Kết bạn mới",
                target = 1,
                currentProgress = 1,
                reward = 100,
                isCompleted = true
            )
        )

        _uiState.value = _uiState.value.copy(dailyTasks = tasks)

        // WHEN
        composeTestRule.setContent {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // Dashboard item task có delay 200ms, cần đợi time trôi qua
        composeTestRule.mainClock.autoAdvance = true // Mặc định là true, nhưng đảm bảo
        composeTestRule.mainClock.advanceTimeBy(300) // Tua nhanh 300ms
        composeTestRule.waitForIdle()

        // THEN
        // Task 1: Chưa xong
        composeTestRule.onNodeWithText("Chat với AI").assertIsDisplayed()
        composeTestRule.onNodeWithText("+50 RIZZ").assertIsDisplayed()

        // Task 2: Đã xong
        composeTestRule.onNodeWithText("Kết bạn mới").assertIsDisplayed()
        composeTestRule.onNodeWithText("Đã xong").assertIsDisplayed()
    }

    @Test
    fun ai_card_navigation_works() {
        // WHEN
        composeTestRule.setContent {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // Item AI card có delay 100ms
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.waitForIdle()

        // ACTION - Click nút "Vào hâm nóng"
        composeTestRule.onNodeWithText("Vào hâm nóng").performClick()

        // VERIFY - NavController được gọi với route đúng
        verify { navController.navigate("message/ai_chat") }
    }

    @Test
    fun notification_dialog_appears_and_displays_data() {
        // GIVEN
        val notification = Notification(
            id = "notif_1",
            type = NotificationType.GENERAL,
            title = "Chào mừng",
            message = "Chào mừng bạn đến với Wink",
            isRead = false,
            // Các trường bắt buộc khác nếu model yêu cầu (dựa trên code bạn thì các trường này có default hoặc nullable)
            fromUserId = "", fromUsername = "", fromAvatarUrl = "", relatedId = ""
        )

        _uiState.value = _uiState.value.copy(
            showNotificationsDialog = true,
            notifications = listOf(notification)
        )

        // WHEN
        composeTestRule.setContent {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN - Dialog title "Thông báo" (từ code NotificationsDialog)
        composeTestRule.onNodeWithText("Thông báo").assertIsDisplayed()

        // Nội dung notification
        composeTestRule.onNodeWithText("Chào mừng").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chào mừng bạn đến với Wink").assertIsDisplayed()
    }

    @Test
    fun click_notification_icon_triggers_event() {
        // GIVEN
        _uiState.value = _uiState.value.copy(notifications = emptyList())

        composeTestRule.setContent {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // ACTION - Click icon chuông (dựa vào contentDescription trong DashboardTopBar)
        composeTestRule.onNodeWithContentDescription("Thông báo").performClick()

        // VERIFY
        verify { viewModel.onEvent(DashboardEvent.OnOpenNotifications) }
    }
    @Test
    fun friend_request_actions_trigger_correct_events() {
        // GIVEN - Giả lập có 1 thông báo lời mời kết bạn đang mở
        val requestNotification = Notification(
            id = "notif_fr_1",
            type = NotificationType.FRIEND_REQUEST,
            title = "Lời mời kết bạn",
            message = "User A muốn kết bạn",
            fromUsername = "User A",
            relatedId = "req_123", // ID của lời mời
            isRead = false
        )

        _uiState.value = _uiState.value.copy(
            showNotificationsDialog = true,
            notifications = listOf(requestNotification)
        )

        // WHEN
        composeTestRule.setContent {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN & ACTION 1: Kiểm tra nút Chấp nhận hoạt động
        composeTestRule.onNodeWithText("Chấp nhận").performClick()
        verify { viewModel.onEvent(DashboardEvent.OnAcceptFriendRequest("req_123")) }

        // ACTION 2: Kiểm tra nút Từ chối hoạt động
        // (Lưu ý: Vì đây là Mock ViewModel nên UI không tự update mất đi, ta có thể click tiếp nút Từ chối để test)
        composeTestRule.onNodeWithText("Từ chối").performClick()
        verify { viewModel.onEvent(DashboardEvent.OnRejectFriendRequest("req_123")) }
    }

    @Test
    fun clear_all_notifications_button_works() {
        // GIVEN - Mở dialog với list thông báo
        val notification = Notification(
            id = "1", type = NotificationType.GENERAL,
            title = "Tin tức", message = "Nội dung", isRead = false
        )
        _uiState.value = _uiState.value.copy(
            showNotificationsDialog = true,
            notifications = listOf(notification)
        )

        composeTestRule.setContent {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // ACTION - Click nút "Xóa tất cả"
        composeTestRule.onNodeWithText("Xóa tất cả").performClick()

        // VERIFY - Kiểm tra event gửi đi
        verify { viewModel.onEvent(DashboardEvent.OnClearAllNotifications) }
    }

    @Test
    fun snackbar_shows_when_friend_request_accepted() {
        // GIVEN - State có thông báo snackbar
        val testMessage = "User B đã chấp nhận lời mời!"
        _uiState.value = _uiState.value.copy(
            acceptedFriendNotification = testMessage
        )

        // WHEN
        composeTestRule.setContent {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // Đợi LaunchedEffect chạy
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra Snackbar hiện lên với nội dung đúng
        composeTestRule.onNodeWithText(testMessage).assertIsDisplayed()
        composeTestRule.mainClock.autoAdvance = false // Tắt chế độ tự động
        composeTestRule.mainClock.advanceTimeBy(5000) // Tua đi 5 giây
        composeTestRule.mainClock.autoAdvance = true // Bật lại
        // Kiểm tra xem ViewModel có được gọi để clear thông báo sau khi hiện không
        verify { viewModel.onEvent(DashboardEvent.OnClearAcceptedNotification) }
    }
}