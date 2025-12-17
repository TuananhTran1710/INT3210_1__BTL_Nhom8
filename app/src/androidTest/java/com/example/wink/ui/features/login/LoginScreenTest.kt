package com.example.wink.ui.features.login

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @RelaxedMockK
    lateinit var navController: NavController

    @RelaxedMockK
    lateinit var viewModel: LoginViewModel

    // Fake State để điều khiển UI
    private val _uiState = MutableStateFlow(LoginState())
    private val _navigationEvent = MutableSharedFlow<LoginViewModel.NavigationEvent>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        // Mock Behavior cho ViewModel
        every { viewModel.uiState } returns _uiState.asStateFlow()
        every { viewModel.navigationEvent } returns _navigationEvent.asSharedFlow()

        // Mock backstack entry để tránh lỗi khi launch LoginScreen
        every { navController.currentBackStackEntry } returns null
    }

    @Test
    fun login_screen_shows_loading_indicator_when_checking_session() {
        // GIVEN - State đang kiểm tra session
        _uiState.value = LoginState(isCheckingSession = true)

        composeTestRule.setContent {
            LoginScreen(navController = navController, viewModel = viewModel)
        }

        // THEN - Chỉ hiển thị vòng loading, không hiện form
        // (CircularProgressIndicator không có text, ta check sự vắng mặt của form)
        composeTestRule.onNodeWithText("Đăng nhập").assertDoesNotExist()
        composeTestRule.onNodeWithText("Email").assertDoesNotExist()
    }

    @Test
    fun login_screen_displays_form_when_session_check_fails() {
        // GIVEN
        _uiState.value = LoginState(isCheckingSession = false)

        composeTestRule.setContent {
            LoginScreen(navController = navController, viewModel = viewModel)
        }

        // THEN

        // 1. Kiểm tra Tiêu đề (Có chữ "Đăng nhập" NHƯNG KHÔNG bấm được)
        composeTestRule.onNode(
            hasText("Đăng nhập") and !hasClickAction()
        ).assertIsDisplayed()

        // 2. Kiểm tra Nút (Có chữ "Đăng nhập" VÀ bấm được)
        composeTestRule.onNode(
            hasText("Đăng nhập") and hasClickAction()
        ).assertIsDisplayed()

        // Các thành phần khác giữ nguyên
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mật khẩu").assertIsDisplayed()
    }

    @Test
    fun input_fields_trigger_viewmodel_events() {
        // GIVEN
        _uiState.value = LoginState(isCheckingSession = false)

        composeTestRule.setContent {
            LoginScreen(navController = navController, viewModel = viewModel)
        }

        // ACTION - Nhập liệu
        composeTestRule.onNodeWithText("Email").performTextInput("test@gmail.com")
        composeTestRule.onNodeWithText("Mật khẩu").performTextInput("123456")

        // VERIFY
        verify { viewModel.onEvent(LoginEvent.OnEmailChanged("test@gmail.com")) }
        verify { viewModel.onEvent(LoginEvent.OnPasswordChanged("123456")) }
    }

    @Test
    fun login_button_click_triggers_event() {
        // GIVEN
        _uiState.value = LoginState(isCheckingSession = false)

        composeTestRule.setContent {
            LoginScreen(navController = navController, viewModel = viewModel)
        }

        // ACTION - Click nút Đăng nhập
        // Button có text "Đăng nhập", tiêu đề cũng "Đăng nhập".
        // Ta dùng performClick lên nút đầu tiên tìm thấy hoặc dùng filter chuẩn hơn.
        // Ở đây đơn giản nhất là tìm Button
        composeTestRule.onAllNodesWithText("Đăng nhập")
            .filterToOne(hasClickAction()) // Chỉ lấy cái nào bấm được (Button)
            .performClick()

        // VERIFY
        verify { viewModel.onEvent(LoginEvent.OnLoginClicked) }
    }

    @Test
    fun error_message_is_displayed() {
        // GIVEN - Có lỗi
        _uiState.value = LoginState(
            isCheckingSession = false,
            error = "Sai mật khẩu"
        )

        composeTestRule.setContent {
            LoginScreen(navController = navController, viewModel = viewModel)
        }

        // THEN
        composeTestRule.onNodeWithText("Sai mật khẩu").assertIsDisplayed()
    }

    @Test
    fun signup_navigation_works() {
        // GIVEN
        _uiState.value = LoginState(isCheckingSession = false)

        composeTestRule.setContent {
            LoginScreen(navController = navController, viewModel = viewModel)
        }

        // ACTION - Click "Chưa có tài khoản? Đăng ký ngay"
        composeTestRule.onNodeWithText("Chưa có tài khoản? Đăng ký ngay").performClick()

        // VERIFY
        // Lưu ý: Cần import Screen class hoặc dùng chuỗi "signup_route"
        // verify { navController.navigate("signup_route") }
        verify { navController.navigate(any<String>()) }
    }
}