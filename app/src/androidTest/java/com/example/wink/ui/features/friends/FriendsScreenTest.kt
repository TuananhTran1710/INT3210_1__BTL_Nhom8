package com.example.wink.ui.features.friends

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavController
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FriendsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock NavController vì Composable yêu cầu tham số này
    @RelaxedMockK
    lateinit var navController: NavController

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun friends_screen_displays_placeholder_text() {
        // GIVEN - Khởi tạo màn hình
        composeTestRule.setContent {
            FriendsScreen(navController = navController)
        }

        // THEN - Kiểm tra dòng chữ "Friends Screen" có hiện lên không
        composeTestRule.onNodeWithText("Friends Screen").assertIsDisplayed()
    }
}