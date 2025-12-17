package com.example.wink.ui.features.signup

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

class SignupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @RelaxedMockK
    lateinit var navController: NavController

    @RelaxedMockK
    lateinit var viewModel: SignupViewModel

    // Fake State để điều khiển UI
    private val _uiState = MutableStateFlow(SignupState())
    private val _navigationEvent = MutableSharedFlow<SignupViewModel.NavigationEvent>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        // Mock Behavior cho ViewModel
        every { viewModel.uiState } returns _uiState.asStateFlow()
        every { viewModel.navigationEvent } returns _navigationEvent.asSharedFlow()
    }

    @Test
    fun signup_screen_displays_all_fields() {
        // GIVEN - Màn hình mặc định
        composeTestRule.setContent {
            SignupScreen(navController = navController, viewModel = viewModel)
        }

        // THEN - Kiểm tra sự tồn tại của các trường nhập liệu
        composeTestRule.onNodeWithText("Tạo tài khoản").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tên hiển thị").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mật khẩu").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nhập lại mật khẩu").assertIsDisplayed()
        composeTestRule.onNodeWithText("Đăng ký ngay").assertIsDisplayed()
    }

    @Test
    fun input_fields_trigger_viewmodel_events() {
        // GIVEN
        composeTestRule.setContent {
            SignupScreen(navController = navController, viewModel = viewModel)
        }

        // ACTION - Nhập liệu vào các ô
        composeTestRule.onNodeWithText("Tên hiển thị").performTextInput("UserTest")
        composeTestRule.onNodeWithText("Email").performTextInput("test@gmail.com")
        composeTestRule.onNodeWithText("Mật khẩu").performTextInput("123456")
        composeTestRule.onNodeWithText("Nhập lại mật khẩu").performTextInput("123456")

        // VERIFY - Kiểm tra ViewModel nhận được sự kiện
        verify { viewModel.onEvent(SignupEvent.OnUsernameChanged("UserTest")) }
        verify { viewModel.onEvent(SignupEvent.OnEmailChanged("test@gmail.com")) }
        verify { viewModel.onEvent(SignupEvent.OnPasswordChanged("123456")) }
        verify { viewModel.onEvent(SignupEvent.OnConfirmPasswordChanged("123456")) }
    }

    @Test
    fun username_loading_state_displays_correctly() {
        // GIVEN - State đang kiểm tra username
        _uiState.value = SignupState(
            username = "User",
            isCheckingUsername = true
        )

        composeTestRule.setContent {
            SignupScreen(navController = navController, viewModel = viewModel)
        }

        // THEN - Kiểm tra text "Đang kiểm tra..." hiện lên
        composeTestRule.onNodeWithText("Đang kiểm tra...").assertIsDisplayed()
    }

    @Test
    fun username_error_state_displays_error_message() {
        // GIVEN - State báo lỗi username trùng
        _uiState.value = SignupState(
            username = "UserDup",
            usernameError = "Tên người dùng đã tồn tại"
        )

        composeTestRule.setContent {
            SignupScreen(navController = navController, viewModel = viewModel)
        }

        // THEN - Kiểm tra thông báo lỗi hiện lên
        composeTestRule.onNodeWithText("Tên người dùng đã tồn tại").assertIsDisplayed()

        // Kiểm tra nút Đăng ký bị vô hiệu hóa (Disabled)
        // Lưu ý: Trong code UI của bạn, nút bị disable khi usernameError != null
        composeTestRule.onNodeWithText("Đăng ký ngay").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun username_valid_state_displays_success_message() {
        // GIVEN - State username hợp lệ
        _uiState.value = SignupState(
            username = "ValidUser",
            isUsernameValid = true
        )

        composeTestRule.setContent {
            SignupScreen(navController = navController, viewModel = viewModel)
        }

        // THEN
        composeTestRule.onNodeWithText("✓ Tên hợp lệ").assertIsDisplayed()
        // Kiểm tra Icon Check (Hợp lệ) hiện lên
        composeTestRule.onNodeWithContentDescription("Hợp lệ").assertIsDisplayed()
    }

    @Test
    fun password_mismatch_shows_error() {
        // GIVEN - Nhập pass không khớp
        _uiState.value = SignupState(
            pass = "123",
            confirmPass = "1234",
            error = "Mật khẩu xác nhận không khớp"
        )

        composeTestRule.setContent {
            SignupScreen(navController = navController, viewModel = viewModel)
        }

        // THEN
        composeTestRule.onNodeWithText("Mật khẩu xác nhận không khớp").assertIsDisplayed()
    }

    @Test
    fun signup_button_click_triggers_event() {
        // GIVEN - State hợp lệ để nút button enable
        _uiState.value = SignupState(
            username = "User",
            email = "email",
            pass = "123",
            confirmPass = "123",
            isUsernameValid = true // Để pass điều kiện enable
        )

        composeTestRule.setContent {
            SignupScreen(navController = navController, viewModel = viewModel)
        }

        // ACTION - Cuộn xuống và Click nút Đăng ký
        composeTestRule.onNodeWithText("Đăng ký ngay")
            .performScrollTo() // Quan trọng vì màn hình có verticalScroll
            .performClick()

        // VERIFY
        verify { viewModel.onEvent(SignupEvent.OnSignupClicked) }
    }

    @Test
    fun login_nav_click_triggers_event() {
        composeTestRule.setContent {
            SignupScreen(navController = navController, viewModel = viewModel)
        }

        // ACTION - Click "Đã có tài khoản? Đăng nhập"
        composeTestRule.onNodeWithText("Đã có tài khoản? Đăng nhập")
            .performScrollTo()
            .performClick()

        // VERIFY
        verify { viewModel.onEvent(SignupEvent.OnLoginNavClicked) }
    }
}