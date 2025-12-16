package com.example.wink.ui.features.tarot.card

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.wink.R
import org.junit.Rule
import org.junit.Test

class TarotCardScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeCard = TarotCardInfo(
        id = 999,
        name = "Lá Bài Test",
        shortMeaning = "Ý nghĩa ngắn test",
        detail = "Chi tiết cực dài để test xem có hiện đủ không",
        imageRes = R.drawable.ic_launcher_foreground
    )

    @Test
    fun testInitialState_HienThiMatSau_VaHuongDan() {
        composeTestRule.setContent {
            TarotCardScreenContent(
                state = TarotCardState(currentCard = null),
                onBackClick = {},
                onReturnHomeClick = {},
                onDrawClick = {},
                onConfirmUseRizz = {},
                onDismissDialogs = {},
                onNotEnoughDialogHandled = {}
            )
        }
        composeTestRule.onNodeWithText("WINK\nTAROT").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chạm vào lá bài").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quay về trang chủ").assertDoesNotExist()
    }

    @Test
    fun testDrawCard_HienThiMatTruoc_VaGiaiNghia() {
        composeTestRule.setContent {
            TarotCardScreenContent(
                state = TarotCardState(currentCard = fakeCard),
                onBackClick = {},
                onReturnHomeClick = {},
                onDrawClick = {},
                onConfirmUseRizz = {},
                onDismissDialogs = {},
                onNotEnoughDialogHandled = {}
            )
        }

        composeTestRule.onNodeWithText("LÁ BÀI TEST").assertIsDisplayed()
        composeTestRule.onNodeWithText("\"Ý nghĩa ngắn test\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chi tiết cực dài để test xem có hiện đủ không").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Quay về trang chủ")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun testDialog_HienThiKhiCanThiet() {
        composeTestRule.setContent {
            TarotCardScreenContent(
                state = TarotCardState(
                    currentCard = fakeCard,
                    showConfirmDialog = true
                ),
                onBackClick = {},
                onReturnHomeClick = {},
                onDrawClick = {},
                onConfirmUseRizz = {},
                onDismissDialogs = {},
                onNotEnoughDialogHandled = {}
            )
        }

        composeTestRule.onNodeWithText("Rút bài lại?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dùng 5 Rizz").assertIsDisplayed()
    }
}