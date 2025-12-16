package com.example.wink.ui.features.tarot.name

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.wink.ui.features.tarot.name.results.TarotNameResultScreenContent
import com.example.wink.ui.features.tarot.name.results.TarotNameResultState
import org.junit.Rule
import org.junit.Test

class TarotNameFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNameInput_HienThiDungUI() {
        composeTestRule.setContent {
            TarotNameScreenContent(
                state = TarotNameState(yourName = "", crushName = ""),
                onYourNameChange = {},
                onCrushNameChange = {},
                onAnalyzeClick = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Bói Theo Tên").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tên của bạn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tên người ấy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Xem Tương Hợp").assertIsDisplayed()
    }

    @Test
    fun testNameInput_NhapLieuVaBamNut() {
        var yourName = ""
        var crushName = ""
        var isAnalyzeClicked = false

        composeTestRule.setContent {
            TarotNameScreenContent(
                state = TarotNameState(yourName = yourName, crushName = crushName),
                onYourNameChange = { yourName = it },
                onCrushNameChange = { crushName = it },
                onAnalyzeClick = { isAnalyzeClicked = true },
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Tên của bạn").performTextInput("Romeo")
        composeTestRule.onNodeWithText("Tên người ấy").performTextInput("Juliet")

        composeTestRule.onNodeWithText("Xem Tương Hợp").performClick()
        assert(isAnalyzeClicked)
    }

    @Test
    fun testNameInput_HienThiLoi() {
        composeTestRule.setContent {
            TarotNameScreenContent(
                state = TarotNameState(errorMessage = "Vui lòng nhập tên"),
                onYourNameChange = {},
                onCrushNameChange = {},
                onAnalyzeClick = {},
                onBackClick = {}
            )
        }

        composeTestRule.onNodeWithText("Vui lòng nhập tên").assertIsDisplayed()
    }

    @Test
    fun testNameResult_HienThiKetQua() {
        val fakeState = TarotNameResultState(
            yourName = "Tùng",
            crushName = "Cúc",
            score = 99,
            message = "Hai bạn là định mệnh của nhau!"
        )

        composeTestRule.setContent {
            TarotNameResultScreenContent(
                state = fakeState,
                onBackClick = {},
                onReturnHomeClick = {},
                onRetryClick = {},
                onConfirmRetry = {},
                onDismissDialogs = {},
                onNotEnoughBack = {}
            )
        }

        composeTestRule.onNodeWithText("Tùng", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Cúc", substring = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("99%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hai bạn là định mệnh của nhau!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quay về trang chủ")
            .performScrollTo()
            .assertIsDisplayed()
    }
}