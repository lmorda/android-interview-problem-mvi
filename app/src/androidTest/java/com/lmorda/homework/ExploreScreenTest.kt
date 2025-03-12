package com.lmorda.homework

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.lmorda.homework.domain.model.mockDomainData
import com.lmorda.homework.ui.explore.ExploreContract.State.Loaded
import com.lmorda.homework.ui.explore.ExploreScreen
import org.junit.Rule
import org.junit.Test

class ExploreScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testExploreScreenList() {
        composeTestRule.setContent {
            ExploreScreen(
                state = Loaded(
                    githubRepos = mockDomainData,
                    nextPage = null,
                    query = null,
                ),
                push = {},
                onNavigateToDetails = {},
            )
        }

        composeTestRule.onNodeWithText("my-application-1").assertIsDisplayed()
        composeTestRule.onNodeWithText("my-application-2").assertIsDisplayed()
    }

    // TODO: Add more UI tests
}
