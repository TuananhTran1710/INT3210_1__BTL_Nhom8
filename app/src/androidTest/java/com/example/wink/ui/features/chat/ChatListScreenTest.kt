package com.example.wink.ui.features.chat

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import com.example.wink.data.model.Chat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @RelaxedMockK
    lateinit var navController: NavController

    @RelaxedMockK
    lateinit var viewModel: ChatListViewModel

    private val _chats = MutableStateFlow<List<UiChat>>(emptyList())
    private val _friends = MutableStateFlow<List<SearchFriendUi>>(emptyList())
    private val _isLoading = MutableStateFlow(false)

    // Mock Data
    private val mockChat = Chat(
        chatId = "chat1",
        name = "Test Group",
        participants = listOf("user1", "user2"),
        lastMessage = "Hello",
        updatedAt = System.currentTimeMillis()
    )

    private val mockUiChat = UiChat(
        chat = mockChat,
        lastMessage = "Hello World",
        displayName = "User Test",
        displayAvatarUrl = null,
        isPinned = false,
        isUnread = true
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { viewModel.chats } returns _chats
        every { viewModel.friends } returns _friends
        every { viewModel.isLoading } returns _isLoading
        every { viewModel.effect } returns flowOf()
    }

    @Test
    fun chat_list_displays_items() {
        // GIVEN
        _chats.value = listOf(mockUiChat)
        _isLoading.value = false

        composeTestRule.setContent {
            ChatListScreen(navController = navController, viewModel = viewModel)
        }

        // THEN
        composeTestRule.onNodeWithText("Tin nhắn").assertIsDisplayed()
        composeTestRule.onNodeWithText("ĐẶC BIỆT").assertIsDisplayed()

        // --- SỬA Ở ĐÂY: UI hiển thị "Lan Anh" chứ không phải "Wink AI" ---
        composeTestRule.onNodeWithText("Lan Anh").assertIsDisplayed()

        // Kiểm tra item chat thường
        composeTestRule.onNodeWithText("User Test").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hello World").assertIsDisplayed()
    }

    // ... Các test khác giữ nguyên
}