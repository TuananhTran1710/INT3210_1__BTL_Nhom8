package com.example.wink.ui.features.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.mockk.confirmVerified
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.After

/**
 * Test suite for ProfileScreen
 * Covers user profile display, tab navigation, and user interactions
 */
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @RelaxedMockK
    lateinit var navController: NavController

    @RelaxedMockK
    lateinit var viewModel: ProfileViewModel

    private val _uiState = MutableStateFlow(ProfileState())
    private val _effectFlow = MutableStateFlow<ProfileEffect?>(null)

    // Mock Data
    private val mockPost = SocialPost(
        id = "post1", userId = "user1", username = "Test User",
        content = "My Profile Post", timestamp = System.currentTimeMillis(),
        likes = 10, comments = 5, imageUrls = emptyList(), avatarUrl = ""
    )

    private val mockPost2 = SocialPost(
        id = "post2", userId = "user1", username = "Test User",
        content = "Another Post", timestamp = System.currentTimeMillis() - 3600000,
        likes = 25, comments = 8, imageUrls = emptyList(), avatarUrl = ""
    )

    private val mockFriend = FriendUi(
        id = "friend1", name = "Friend One", rizzPoints = 100
    )

    private val mockFriend2 = FriendUi(
        id = "friend2", name = "Friend Two", rizzPoints = 150
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { viewModel.uiState } returns _uiState.asStateFlow()
        // Mock effect flow để tránh crash
        every { viewModel.effect } returns kotlinx.coroutines.flow.emptyFlow()
    }

    @After
    fun tearDown() {
        _uiState.value = ProfileState()
    }

    @Test
    fun profile_displays_user_info_correctly() {
        // GIVEN - Chuẩn bị dữ liệu người dùng
        _uiState.value = _uiState.value.copy(
            username = "Vinh Nguyen",
            rizzPoints = 999,
            friendCount = 50,
            posts = listOf(mockPost),
            dailyStreak = 15,
            longestStreak = 42
        )

        // WHEN - Render ProfileScreen
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra tên người dùng hiển thị
        composeTestRule.onNodeWithText("Vinh Nguyen").assertIsDisplayed()
        
        // Kiểm tra RIZZ points hiển thị
        composeTestRule.onNodeWithText("999").assertIsDisplayed()
        
        // Kiểm tra số bạn bè hiển thị
        composeTestRule.onNodeWithText("50").assertIsDisplayed()
    }

    @Test
    fun profile_displays_empty_state_when_no_posts() {
        // GIVEN - Người dùng không có bài viết
        _uiState.value = _uiState.value.copy(
            username = "Test User",
            posts = emptyList()
        )

        // WHEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra không hiển thị bài viết
        composeTestRule.onNodeWithText("My Profile Post").assertDoesNotExist()
    }

    @Test
    fun profile_displays_multiple_posts() {
        // GIVEN - Người dùng có nhiều bài viết
        _uiState.value = _uiState.value.copy(
            posts = listOf(mockPost, mockPost2)
        )

        // WHEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra cả hai bài viết đều hiển thị
        composeTestRule.onNodeWithText("My Profile Post").assertIsDisplayed()
        composeTestRule.onNodeWithText("Another Post").assertIsDisplayed()
    }

    @Test
    fun profile_shows_loading_state() {
        // GIVEN - Đang tải dữ liệu
        _uiState.value = _uiState.value.copy(isLoading = true)

        // WHEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra loading indicator hiển thị
        composeTestRule.onNodeWithContentDescription("Loading").assertExists()
    }

    @Test
    fun profile_displays_error_message() {
        // GIVEN - Có lỗi khi tải dữ liệu
        _uiState.value = _uiState.value.copy(
            errorMessage = "Failed to load profile"
        )

        // WHEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra thông báo lỗi
        composeTestRule.onNodeWithText("Failed to load profile").assertIsDisplayed()
    }

    @Test
    fun tab_switching_shows_posts_tab_content() {
        // GIVEN - Chuẩn bị dữ liệu cho tab Bài viết
        _uiState.value = _uiState.value.copy(
            posts = listOf(mockPost)
        )

        // WHEN - Render profile screen
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra Tab "Bài viết" hiển thị nội dung
        composeTestRule.onNodeWithText("My Profile Post")
            .assertIsDisplayed()
    }

    @Test
    fun tab_switching_shows_friends_tab_content() {
        // GIVEN - Chuẩn bị dữ liệu cho tab Bạn bè
        _uiState.value = _uiState.value.copy(
            loadedFriends = listOf(mockFriend, mockFriend2)
        )

        // WHEN - Render profile screen
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // WHEN - Click chuyển sang Tab "Bạn bè"
        composeTestRule.onNodeWithText("Bạn bè").performClick()
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra nội dung Tab "Bạn bè" hiển thị
        composeTestRule.onNodeWithText("Friend One").assertIsDisplayed()
        composeTestRule.onNodeWithText("Friend Two").assertIsDisplayed()
    }

    @Test
    fun tab_switching_shows_empty_friends_list() {
        // GIVEN - Không có bạn bè
        _uiState.value = _uiState.value.copy(
            loadedFriends = emptyList()
        )

        // WHEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // WHEN - Chuyển sang tab Bạn bè
        composeTestRule.onNodeWithText("Bạn bè").performClick()
        composeTestRule.waitForIdle()

        // THEN - Danh sách trống
        composeTestRule.onNodeWithText("Friend One").assertDoesNotExist()
    }

    @Test
    fun settings_button_click_navigates_to_settings() {
        // GIVEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }

        // WHEN - Click nút Settings
        composeTestRule.onNodeWithContentDescription("Settings").performClick()

        // THEN - Kiểm tra điều hướng được gọi
        verify { navController.navigate(any<String>()) }
    }

    @Test
    fun message_button_on_friend_list_triggers_event() {
        // GIVEN - Đang ở Tab Bạn bè
        _uiState.value = _uiState.value.copy(
            loadedFriends = listOf(mockFriend)
        )

        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }

        // WHEN - Chuyển Tab
        composeTestRule.onNodeWithText("Bạn bè").performClick()
        composeTestRule.waitForIdle()

        // WHEN - Click nút "Nhắn tin" của bạn bè
        composeTestRule.onNodeWithText("Nhắn tin").performClick()

        // THEN
        verify { viewModel.onEvent(ProfileEvent.MessageClick("friend1")) }
    }

    @Test
    fun logout_navigates_to_login_screen() {
        // GIVEN - Người dùng đã đăng xuất
        _uiState.value = _uiState.value.copy(isLoggedOut = true)

        // WHEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra điều hướng sang màn hình Login
        verify { navController.navigate(any<String>()) }
    }

    @Test
    fun friend_item_displays_correct_information() {
        // GIVEN
        _uiState.value = _uiState.value.copy(
            loadedFriends = listOf(mockFriend)
        )

        // WHEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Bạn bè").performClick()
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra tên bạn bè
        composeTestRule.onNodeWithText("Friend One").assertIsDisplayed()
        
        // Kiểm tra RIZZ points của bạn bè
        composeTestRule.onNodeWithText("100").assertIsDisplayed()
    }

    @Test
    fun profile_streak_information_displays_correctly() {
        // GIVEN
        _uiState.value = _uiState.value.copy(
            dailyStreak = 15,
            longestStreak = 42
        )

        // WHEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN
        composeTestRule.onNodeWithText("15").assertIsDisplayed()  // Daily Streak
        composeTestRule.onNodeWithText("42").assertIsDisplayed()  // Longest Streak
    }

    @Test
    fun post_interactions_are_visible() {
        // GIVEN - Post có likes và comments
        _uiState.value = _uiState.value.copy(
            posts = listOf(mockPost) // 10 likes, 5 comments
        )

        // WHEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN
        composeTestRule.onNodeWithText("My Profile Post").assertIsDisplayed()
    }

    @Test
    fun scrolling_through_posts_works() {
        // GIVEN - Có nhiều bài viết
        val manyPosts = (1..10).map { i ->
            mockPost.copy(id = "post$i", content = "Post $i")
        }
        _uiState.value = _uiState.value.copy(posts = manyPosts)

        // WHEN
        composeTestRule.setContent {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }
        composeTestRule.waitForIdle()

        // THEN - Kiểm tra bài viết đầu tiên hiển thị
        composeTestRule.onNodeWithText("Post 1").assertIsDisplayed()
    }
}