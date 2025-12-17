package com.example.wink.ui.features.explore

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ExploreScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockCategories = listOf(
        CategoryItem("cat1", "Category 1", "Sub 1", Icons.Default.Home, Color.Red),
        CategoryItem("cat2", "Category 2", "Sub 2", Icons.Default.Home, Color.Blue)
    )

    @Test
    fun testExploreContent_DisplaysCorrectly() {
        composeTestRule.setContent {
            ExploreContent(
                categories = mockCategories,
                onGameClick = {},
                onCategoryClick = {}
            )
        }

        composeTestRule.onNodeWithText("Khám phá").assertIsDisplayed()
        composeTestRule.onNodeWithText("Human or AI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiện ích khác").assertIsDisplayed()

        composeTestRule.onNodeWithText("Category 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Category 2").assertIsDisplayed()
    }

    @Test
    fun testHeroGameCard_ClickAction() {
        var isGameClicked = false

        composeTestRule.setContent {
            ExploreContent(
                categories = mockCategories,
                onGameClick = { isGameClicked = true },
                onCategoryClick = {}
            )
        }

        composeTestRule.onNodeWithText("Human or AI").performClick()

        assert(isGameClicked)
    }

    @Test
    fun testCategoryCard_ClickAction() {
        var clickedCategoryId: String? = null

        composeTestRule.setContent {
            ExploreContent(
                categories = mockCategories,
                onGameClick = {},
                onCategoryClick = { id -> clickedCategoryId = id }
            )
        }

        composeTestRule.onNodeWithText("Category 1").performClick()

        assert(clickedCategoryId == "cat1")
    }
}