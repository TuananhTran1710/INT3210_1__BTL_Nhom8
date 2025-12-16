package com.example.wink.ui.features.onboarding

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class OnboardingFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testIntroPage_DisplayCorrectly() {
        val state = OnboardingState(currentPage = 0)

        composeTestRule.setContent {
            OnboardingContent(state = state, onEvent = {})
        }

        composeTestRule.onNodeWithText("Welcome to Wink!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiếp theo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quay lại").assertDoesNotExist()
    }

    @Test
    fun testGenderPage_Selection() {
        val state = OnboardingState(currentPage = 1, selectedGender = "male")
        var selectedGender: String? = null

        composeTestRule.setContent {
            OnboardingContent(
                state = state,
                onEvent = { event ->
                    if (event is OnboardingEvent.SelectGender) {
                        selectedGender = event.gender
                    }
                }
            )
        }

        composeTestRule.onNodeWithText("Giới tính của bạn?").assertIsDisplayed()

        composeTestRule.onNodeWithText("Nữ").performClick()

        assert(selectedGender == "female")
    }

    @Test
    fun testPreferencePage_Display() {
        val state = OnboardingState(currentPage = 2)

        composeTestRule.setContent {
            OnboardingContent(state = state, onEvent = {})
        }

        composeTestRule.onNodeWithText("Bạn muốn tìm?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bạn Trai (Male)").assertIsDisplayed()
    }

    @Test
    fun testPersonalityPage_MultiSelection() {
        val state = OnboardingState(
            currentPage = 3,
            selectedPersonalities = listOf("Hài hước")
        )
        var toggledItem: String? = null

        composeTestRule.setContent {
            OnboardingContent(
                state = state,
                onEvent = { event ->
                    if (event is OnboardingEvent.TogglePersonality) {
                        toggledItem = event.personality
                    }
                }
            )
        }

        composeTestRule.onNodeWithText("Điểm nổi bật ở bạn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hài hước").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lãng mạn").performClick()
        assert(toggledItem == "Lãng mạn")
    }

    @Test
    fun testNavigationButtons_LastPage() {
        val state = OnboardingState(currentPage = 3)
        var nextClicked = false

        composeTestRule.setContent {
            OnboardingContent(
                state = state,
                onEvent = { event ->
                    if (event is OnboardingEvent.FinishOnboarding) {
                        nextClicked = true
                    }
                }
            )
        }

        composeTestRule.onNodeWithText("Hoàn thành").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiếp theo").assertDoesNotExist()

        composeTestRule.onNodeWithText("Hoàn thành").performClick()
        assert(nextClicked)
    }
}