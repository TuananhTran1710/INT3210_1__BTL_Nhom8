package com.example.wink.ui.features.chat

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @RelaxedMockK
    lateinit var navController: NavController

    @RelaxedMockK
    lateinit var viewModel: NewChatViewModel

    private val _state = MutableStateFlow(NewChatState())

    private val mockFriend = NewChatFriendUi(
        uid = "f1", username = "Friend A", avatarUrl = null, email = "a@test.com"
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { viewModel.state } returns _state
        every { viewModel.effect } returns flowOf()
    }

    @Test
    fun displays_friend_list() {
        // GIVEN
        _state.value = NewChatState(
            filteredFriends = listOf(mockFriend),
            isLoading = false
        )

        composeTestRule.setContent {
            NewChatScreen(navController = navController, viewModel = viewModel)
        }

        // THEN
        composeTestRule.onNodeWithText("Gợi ý").assertIsDisplayed()
        composeTestRule.onNodeWithText("Friend A").assertIsDisplayed()
        composeTestRule.onNodeWithText("a@test.com").assertIsDisplayed()
    }

    @Test
    fun search_filters_list() {
        // GIVEN
        _state.value = NewChatState(searchQuery = "")

        composeTestRule.setContent {
            NewChatScreen(navController = navController, viewModel = viewModel)
        }

        // ACTION - Nhập tìm kiếm
        composeTestRule.onNodeWithText("Nhập tên ").performTextInput("Friend")

        // VERIFY - ViewModel nhận query change
        verify { viewModel.onSearchQueryChange("Friend") }
    }

    @Test
    fun click_friend_triggers_creation() {
        // GIVEN
        _state.value = NewChatState(filteredFriends = listOf(mockFriend))

        composeTestRule.setContent {
            NewChatScreen(navController = navController, viewModel = viewModel)
        }

        // ACTION - Chọn bạn bè
        composeTestRule.onNodeWithText("Friend A").performClick()

        // VERIFY - ViewModel xử lý chọn
        verify { viewModel.onFriendSelected("f1") }
    }
}