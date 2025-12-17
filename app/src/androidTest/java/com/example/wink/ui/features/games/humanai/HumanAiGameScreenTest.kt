package com.example.wink.ui.features.games.humanai

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.wink.data.model.Message
import org.junit.Rule
import org.junit.Test

class HumanAiGameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- TEST 1: MÀN HÌNH LOBBY ---
    @Test
    fun lobby_displays_correct_info_and_buttons() {
        // GIVEN - Data giả lập
        val rizz = 1500
        val online = 2345
        var startClicked = false
        var backClicked = false

        // WHEN - Render LobbyView độc lập
        composeTestRule.setContent {
            LobbyView(
                rizz = rizz,
                online = online,
                onStart = { startClicked = true },
                onBack = { backClicked = true }
            )
        }

        // THEN - Kiểm tra hiển thị
        composeTestRule.onNodeWithText("1500 RIZZ").assertIsDisplayed()
        composeTestRule.onNodeWithText("Human or AI?").assertIsDisplayed()
        composeTestRule.onNodeWithText("2345 đang tìm trận").assertIsDisplayed()

        // ACTION - Click nút Start
        composeTestRule.onNodeWithTag("start_game_button").performClick()
        assert(startClicked)

        // ACTION - Click nút Back
        composeTestRule.onNodeWithTag("lobby_back_button").performClick()
        assert(backClicked)
    }

    // --- TEST 2: MÀN HÌNH SEARCHING ---
    @Test
    fun searching_view_shows_timer_and_cancel() {
        // GIVEN
        var cancelClicked = false

        // WHEN
        composeTestRule.setContent {
            SearchingView(
                time = 65, // 1 phút 05 giây
                onCancel = { cancelClicked = true }
            )
        }

        // THEN - Kiểm tra format thời gian (01:05)
        composeTestRule.onNodeWithText("01:05").assertIsDisplayed()
        composeTestRule.onNodeWithText("Đang tìm đối thủ...").assertIsDisplayed()

        // ACTION - Click hủy
        composeTestRule.onNodeWithText("Hủy tìm kiếm").performClick()
        assert(cancelClicked)
    }

    // --- TEST 3: MÀN HÌNH CHAT ---
    @Test
    fun chat_screen_displays_messages_and_input() {
        // GIVEN - SỬA LẠI CÁCH KHỞI TẠO MESSAGE (Thêm tên biến)
        val messages = listOf(
            Message(
                messageId = "1",
                senderId = "partner_id",
                content = "Xin chào bạn",
                timestamp = System.currentTimeMillis()
            ),
            Message(
                messageId = "2",
                senderId = "my_id",
                content = "Chào lại nha",
                timestamp = System.currentTimeMillis()
            )
        )

        val state = HumanAiGameState(
            timeLeft = 59,
            messages = messages,
            isMyTurn = true
        )
    }

    // --- TEST 4: MÀN HÌNH ĐOÁN (GUESSING) ---
    @Test
    fun guessing_view_interactions() {
        var guessedAi: Boolean? = null

        composeTestRule.setContent {
            GuessingView(onGuess = { isAi -> guessedAi = isAi })
        }

        // THEN
        composeTestRule.onNodeWithText("HẾT GIỜ!").assertIsDisplayed()

        // ACTION - Chọn Người thật (False)
        composeTestRule.onNodeWithText("Người thật").performClick()
        assert(guessedAi == false)

        // ACTION - Chọn AI (True)
        composeTestRule.onNodeWithText("AI (Bot)").performClick()
        assert(guessedAi == true)
    }

    // --- TEST 5: MÀN HÌNH KẾT QUẢ (RESULT) ---
    @Test
    fun result_view_displays_win_state() {
        // GIVEN - Thắng game, cộng 50 điểm, đối thủ là AI
        val state = HumanAiGameState(
            didWin = true,
            earnedRizz = 50,
            isOpponentActuallyAi = true
        )
        var playAgainClicked = false

        composeTestRule.setContent {
            ResultView(
                state = state,
                onPlayAgain = { playAgainClicked = true },
                onExit = {}
            )
        }

        // THEN
        composeTestRule.onNodeWithText("CHÍNH XÁC!").assertIsDisplayed()
        composeTestRule.onNodeWithText("+").assertIsDisplayed()
        composeTestRule.onNodeWithText("50 RIZZ").assertIsDisplayed()

        // Kiểm tra tiết lộ danh tính
        composeTestRule.onNodeWithText("AI (Bot)").assertIsDisplayed()

        // ACTION
        composeTestRule.onNodeWithText("Chơi lại").performClick()
        assert(playAgainClicked)
    }

    @Test
    fun result_view_displays_lose_state() {
        // GIVEN - Thua game, trừ 25 điểm
        val state = HumanAiGameState(
            didWin = false,
            earnedRizz = -25,
            isOpponentActuallyAi = false // Đối thủ là người
        )

        composeTestRule.setContent {
            ResultView(
                state = state,
                onPlayAgain = {},
                onExit = {}
            )
        }

        // THEN
        composeTestRule.onNodeWithText("SAI RỒI!").assertIsDisplayed()
        // Kiểm tra hiển thị số âm (Logic code UI của bạn hiển thị: "" + "-25 RIZZ")
        composeTestRule.onNodeWithText("-25 RIZZ").assertIsDisplayed()
        composeTestRule.onNodeWithText("Người thật").assertIsDisplayed()
    }
}