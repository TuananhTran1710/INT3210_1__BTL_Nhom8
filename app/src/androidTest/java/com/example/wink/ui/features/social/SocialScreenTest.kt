package com.example.wink.ui.features.social

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import com.example.wink.data.model.SocialPost
import com.example.wink.data.model.User
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SocialScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @RelaxedMockK
    lateinit var navController: NavController

    @RelaxedMockK
    lateinit var viewModel: SocialViewModel

    private val _uiState = MutableStateFlow(SocialState())

    // 1. FIXED: Initialize User with ALL required fields
    private val mockUser = User(
        uid = "user1",
        username = "Test User",
        email = "test@email.com",
        avatarUrl = "",
        rizzPoints = 100,
        loginStreak = 5,
        lastCheckInDate = null

    )

    // 2. FIXED: Initialize SocialPost with ALL required fields
    private val fullMockPost = SocialPost(
        id = "post1",
        userId = "userA",
        username = "Nguyễn Văn A",
        avatarUrl = "",
        content = "Bài viết test",
        timestamp = System.currentTimeMillis(),
        likes = 10,
        comments = 2,
        imageUrls = emptyList(),
        isLikedByMe = false,
        isRetweetedByMe = false,
        retweetCount = 0,
        originalPostId = null,
        originalUserId = null,
        originalUsername = null,
        originalAvatarUrl = null
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { viewModel.uiState } returns _uiState.asStateFlow()
    }

    @Test
    fun social_feed_displays_posts_correctly() {
        // GIVEN
        _uiState.value = _uiState.value.copy(
            feedList = listOf(fullMockPost),
            isLoading = false
        )

        // WHEN
        composeTestRule.setContent {
            SocialScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.waitForIdle()

        // THEN
        composeTestRule.onNodeWithText("Nguyễn Văn A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bài viết test").assertIsDisplayed()
    }

    @Test
    fun loading_state_shows_progress_indicator() {
        // GIVEN
        _uiState.value = _uiState.value.copy(isLoading = true)

        // WHEN
        composeTestRule.setContent {
            SocialScreen(navController = navController, viewModel = viewModel)
        }

        // THEN - Requires Step 2 (adding testTag) to be done
        composeTestRule.onNodeWithTag("loading_indicator").assertExists()
    }

    @Test
    fun create_post_input_click_triggers_viewmodel() {
        // GIVEN
        _uiState.value = _uiState.value.copy(isCreatingPost = false)

        composeTestRule.setContent {
            SocialScreen(navController = navController, viewModel = viewModel)
        }

        // ACTION - FIXED "Failed to inject touch input"
        // performScrollTo ensures the element is visible on screen before clicking
        composeTestRule.onNodeWithContentDescription("Create post")
            .performScrollTo()
            .performClick()

        // VERIFY
        verify { viewModel.onFabClick() }
    }

    @Test
    fun like_button_click_triggers_viewmodel() {
        // GIVEN
        _uiState.value = _uiState.value.copy(feedList = listOf(fullMockPost))

        composeTestRule.setContent {
            SocialScreen(navController = navController, viewModel = viewModel)
        }

        composeTestRule.waitForIdle()

        // ACTION
        composeTestRule.onAllNodesWithContentDescription("Like button")
            .onFirst()
            .performClick()

        // VERIFY
        verify { viewModel.onLikeClick("post1") }
    }

    // New Test: Tab Switching
    // FIX LỖI 1: Bỏ performScrollTo() vì TabRow không cuộn được
    @Test
    fun tab_switching_works() {
        _uiState.value = _uiState.value.copy(selectedTab = 0)

        composeTestRule.setContent {
            SocialScreen(navController = navController, viewModel = viewModel)
        }

        // CHỈ CLICK, KHÔNG SCROLL
        composeTestRule.onNodeWithText("Xếp hạng")
            .performClick()

        verify { viewModel.onTabSelected(1) }
    }

    @Test
    fun create_post_dialog_interaction() {
        _uiState.value = _uiState.value.copy(
            isCreatingPost = true,
            newPostContent = ""
        )

        composeTestRule.setContent {
            SocialScreen(navController = navController, viewModel = viewModel)
        }

        // 1. Kiểm tra Dialog hiện lên
        composeTestRule.onNodeWithText("Tạo bài viết").assertIsDisplayed()

        // 2. Nhập liệu (DÙNG TAG THAY VÌ TEXT)
        // Lý do: Tag gắn vào TextField, còn Text gắn vào Placeholder. Bấm vào TextField an toàn hơn.
        val inputNode = composeTestRule.onNodeWithTag("post_input")

        inputNode.performClick()
        inputNode.performTextInput("Hello World")

        // 3. Verify
        verify { viewModel.onPostContentChange("Hello World") }
    }

    // New Test: Create Post Interaction
//    @Test
//    fun create_post_dialog_interaction() {
//        _uiState.value = _uiState.value.copy(
//            isCreatingPost = true,
//            newPostContent = ""
//        )
//
//        composeTestRule.setContent {
//            SocialScreen(navController = navController, viewModel = viewModel)
//        }
//
//        // 1. Verify Dialog Title exists
//        composeTestRule.onNodeWithText("Tạo bài viết").assertIsDisplayed()
//
//        // 2. Input text
//        composeTestRule.onNodeWithText("Bạn đang nghĩ gì?")
//            .performTextInput("Hello World")
//
//        // 3. Verify ViewModel call
//        verify { viewModel.onPostContentChange("Hello World") }
//    }
}