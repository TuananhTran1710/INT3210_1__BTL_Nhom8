package com.example.wink.ui.features.tips

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.wink.data.model.Tip
import org.junit.Rule
import org.junit.Test

class TipsFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockTips = listOf(
        Tip(
            id = "1",
            title = "Bí kíp tán gái cơ bản",
            description = "Dành cho người mới bắt đầu",
            content = "Nội dung chi tiết...",
            price = 0,
            isLocked = false
        ),
        Tip(
            id = "2",
            title = "Nghệ thuật nhắn tin",
            description = "Cách nhắn tin không bị nhạt",
            content = "Nội dung chi tiết...",
            price = 50,
            isLocked = true
        )
    )

    @Test
    fun testTipsList_Display() {
        val state = TipsState(
            tips = mockTips,
            userRizzPoints = 100
        )

        composeTestRule.setContent {
            TipsContent(
                state = state,
                onTipClick = {},
                onConfirmUnlock = {},
                onDismissDialog = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("100 RIZZ").assertIsDisplayed()

        composeTestRule.onNodeWithText("Bí kíp tán gái cơ bản").assertIsDisplayed()
        composeTestRule.onNodeWithText("Đọc ngay").assertIsDisplayed()

        composeTestRule.onNodeWithText("Nghệ thuật nhắn tin").assertIsDisplayed()
        composeTestRule.onNodeWithText("50 RIZZ").assertIsDisplayed()
    }

    @Test
    fun testUnlockDialog_Show() {
        val lockedTip = mockTips[1]
        val state = TipsState(
            tips = mockTips,
            userRizzPoints = 100,
            selectedTipToUnlock = lockedTip
        )

        composeTestRule.setContent {
            TipsContent(
                state = state,
                onTipClick = {},
                onConfirmUnlock = {},
                onDismissDialog = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Mở khóa Bí kíp?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bạn có muốn dùng 50 điểm RIZZ để mở khóa bài học: \"Nghệ thuật nhắn tin\" không?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mở khóa ngay").assertIsDisplayed()
    }

    @Test
    fun testTipDetail_Display() {
        val tip = mockTips[0]

        composeTestRule.setContent {
            TipDetailScreen(
                tip = tip,
                navController = androidx.navigation.compose.rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Bí kíp tán gái cơ bản").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dành cho người mới bắt đầu").assertIsDisplayed()
    }
}