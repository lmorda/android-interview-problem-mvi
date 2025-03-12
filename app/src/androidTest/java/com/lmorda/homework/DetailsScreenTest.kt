package com.lmorda.homework

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.lmorda.homework.domain.model.mockDomainData
import com.lmorda.homework.ui.details.DetailsContract.State.Loaded
import com.lmorda.homework.ui.details.DetailsScreen
import org.junit.Rule
import org.junit.Test

class DetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testExploreScreenList() {
        composeTestRule.setContent {
            DetailsScreen(
                state = Loaded(
                    githubRepo = mockDomainData[0],
                ),
                onBack = {},
            )
        }

        composeTestRule.onNodeWithText("description for google my application 1").assertIsDisplayed()
    }

    // TODO: Add more UI tests
}
