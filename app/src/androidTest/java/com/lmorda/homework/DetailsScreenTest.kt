package com.lmorda.homework

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lmorda.homework.domain.model.mockDomainData
import com.lmorda.homework.ui.details.DetailsContract.State
import com.lmorda.homework.ui.details.DetailsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testExploreScreenList() {
        composeTestRule.setContent {
            DetailsScreen(
                state = State.Loaded(
                    githubRepo = mockDomainData[0],
                ),
                onBack = {},
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("description for google my application 1").assertIsDisplayed()
    }

    @Test
    fun testDetailsErrorState() {
        composeTestRule.setContent {
            DetailsScreen(
                state = State.LoadError(errorMessage = "boom"),
                onBack = {},
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Having a bit of trouble finding this repository!")
            .assertIsDisplayed()
    }

    @Test
    fun testDetailsBackClick() {
        var backClicks = 0
        composeTestRule.setContent {
            DetailsScreen(
                state = State.Loaded(
                    githubRepo = mockDomainData[0],
                ),
                onBack = { backClicks++ },
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backClicks)
    }
}
