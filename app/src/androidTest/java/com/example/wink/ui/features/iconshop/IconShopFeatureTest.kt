package com.example.wink.ui.features.iconshop

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.wink.R
import org.junit.Rule
import org.junit.Test

class IconShopFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockIcons = listOf(
        IconItemUi(
            id = "default",
            price = 0,
            isOwned = true,
            isSelected = true,
            iconResId = R.drawable.ic_launcher_foreground
        ),
        IconItemUi(
            id = "premium_1",
            price = 500,
            isOwned = false,
            isSelected = false,
            iconResId = R.drawable.ic_launcher_foreground
        ),
        IconItemUi(
            id = "premium_2",
            price = 1000,
            isOwned = true,
            isSelected = false,
            iconResId = R.drawable.ic_launcher_foreground
        )
    )

    @Test
    fun testIconShopList_DisplayCorrectly() {
        val state = IconShopState(
            rizzPoints = 1250,
            icons = mockIcons,
            isLoading = false
        )

        composeTestRule.setContent {
            IconShopContent(
                state = state,
                onBackClick = {},
                onIconClick = {},
                onConfirmChange = {},
                onCancelChange = {}
            )
        }

        composeTestRule.onNodeWithText("Đổi icon").assertIsDisplayed()
        composeTestRule.onNodeWithText("1250 RIZZ").assertIsDisplayed()

        composeTestRule.onNodeWithText("Đang dùng").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Đang chọn").assertIsDisplayed()

        composeTestRule.onNodeWithText("500 RP").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Bị khóa").assertIsDisplayed()

        composeTestRule.onNodeWithText("Đã mua").assertIsDisplayed()
    }

    @Test
    fun testRestartDialog_ShowAndActions() {
        val state = IconShopState(
            rizzPoints = 1000,
            icons = mockIcons,
            showRestartDialog = true,
            pendingIconId = "premium_1"
        )

        var isConfirmed = false
        var isCancelled = false

        composeTestRule.setContent {
            IconShopContent(
                state = state,
                onBackClick = {},
                onIconClick = {},
                onConfirmChange = { isConfirmed = true },
                onCancelChange = { isCancelled = true }
            )
        }

        composeTestRule.onNodeWithText("Thay đổi biểu tượng").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hiệu ứng sẽ chỉ áp dụng trong lần chạy sau. Bạn có muốn thoát ngay bây giờ không?").assertIsDisplayed()

        composeTestRule.onNodeWithText("Đồng ý").performClick()
        assert(isConfirmed)

        composeTestRule.onNodeWithText("Để sau").performClick()
        assert(isCancelled)
    }

    @Test
    fun testErrorMessage_Display() {
        val state = IconShopState(
            errorMessage = "Không đủ tiền mua icon này"
        )

        composeTestRule.setContent {
            IconShopContent(
                state = state,
                onBackClick = {},
                onIconClick = {},
                onConfirmChange = {},
                onCancelChange = {}
            )
        }

        composeTestRule.onNodeWithText("Không đủ tiền mua icon này").assertIsDisplayed()
    }
}