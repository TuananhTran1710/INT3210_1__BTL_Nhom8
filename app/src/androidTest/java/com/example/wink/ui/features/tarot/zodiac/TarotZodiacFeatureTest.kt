package com.example.wink.ui.features.tarot.zodiac

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.wink.ui.features.tarot.zodiac.results.TarotZodiacResultScreenContent
import com.example.wink.ui.features.tarot.zodiac.results.TarotZodiacResultState
import org.junit.Rule
import org.junit.Test

class TarotZodiacFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testZodiacInput_HienThiDungUI() {
        composeTestRule.setContent {
            TarotZodiacScreenContent(
                state = TarotZodiacState(),
                onYourSignSelected = {},
                onCrushSignSelected = {},
                onAnalyzeClick = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Cung Hoàng Đạo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cung của bạn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cung người ấy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Xem Tương Hợp").assertIsDisplayed()
    }

    @Test
    fun testZodiacInput_BamNutAnalyze() {
        var isAnalyzeClicked = false

        composeTestRule.setContent {
            TarotZodiacScreenContent(
                state = TarotZodiacState(),
                onYourSignSelected = {},
                onCrushSignSelected = {},
                onAnalyzeClick = { isAnalyzeClicked = true },
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Xem Tương Hợp").performClick()
        assert(isAnalyzeClicked)
    }

    @Test
    fun testZodiacResult_HienThiKetQua() {
        val fakeState = TarotZodiacResultState(
            yourSignName = "Bạch Dương",
            crushSignName = "Sư Tử",
            score = 88,
            message = "Lửa gặp Lửa, cháy cực to!"
        )

        composeTestRule.setContent {
            TarotZodiacResultScreenContent(
                state = fakeState,
                onBackClick = {},
                onRetryClick = {},
                onConfirmUseRizz = {},
                onDismissDialogs = {},
                onNotEnoughOk = {}
            )
        }

        composeTestRule.onNodeWithText("Bạch Dương", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Sư Tử", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("88%").assertIsDisplayed()

        composeTestRule.onNodeWithText("Lửa gặp Lửa, cháy cực to!").assertIsDisplayed()

        composeTestRule.onNodeWithText("Quay về trang chủ")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun testZodiacResult_HienThiDialog() {
        composeTestRule.setContent {
            TarotZodiacResultScreenContent(
                state = TarotZodiacResultState(showConfirmDialog = true),
                onBackClick = {},
                onRetryClick = {},
                onConfirmUseRizz = {},
                onDismissDialogs = {},
                onNotEnoughOk = {}
            )
        }

        composeTestRule.onNodeWithText("Thử lại lần nữa?").assertIsDisplayed()
    }
}