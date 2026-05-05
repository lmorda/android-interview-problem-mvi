package com.lmorda.homework

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lmorda.homework.domain.model.mockDomainData
import com.lmorda.homework.ui.explore.ExploreContract.State
import com.lmorda.homework.ui.explore.ExploreScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ExploreScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testExploreScreenList() {
        composeTestRule.setContent {
            ExploreScreen(
                state = State.Loaded(
                    githubRepos = mockDomainData,
                    nextPage = null,
                    searchQuery = "android",
                ),
                push = {},
                onNavigateToDetails = {},
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("my-application-1").assertIsDisplayed()
        composeTestRule.onNodeWithText("my-application-2").assertIsDisplayed()
    }

    @Test
    fun testExploreInitialState() {
        composeTestRule.setContent {
            ExploreScreen(
                state = State.Initial,
                push = {},
                onNavigateToDetails = {},
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Enter your search query to get started!").assertIsDisplayed()
    }

    @Test
    fun testExploreEmptyState() {
        composeTestRule.setContent {
            ExploreScreen(
                state = State.Loaded(
                    githubRepos = emptyList(),
                    nextPage = null,
                    searchQuery = "missing",
                ),
                push = {},
                onNavigateToDetails = {},
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("No results found").assertIsDisplayed()
    }

    @Test
    fun testExploreErrorState() {
        composeTestRule.setContent {
            ExploreScreen(
                state = State.LoadError(errorMessage = "boom"),
                push = {},
                onNavigateToDetails = {},
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Having a bit of trouble finding those repositories!")
            .assertIsDisplayed()
    }

    @Test
    fun testExploreLoadingNextPageState() {
        composeTestRule.setContent {
            ExploreScreen(
                state = State.LoadingPage(
                    githubRepos = listOf(mockDomainData[0]),
                    searchQuery = "compose",
                ),
                push = {},
                onNavigateToDetails = {},
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("my-application-1").assertIsDisplayed()
    }

    @Test
    fun testExploreItemClickNavigatesToDetails() {
        var navigatedId: Long? = null
        composeTestRule.setContent {
            ExploreScreen(
                state = State.Loaded(
                    githubRepos = mockDomainData,
                    nextPage = null,
                    searchQuery = "android",
                ),
                push = {},
                onNavigateToDetails = { navigatedId = it },
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("my-application-1").performClick()

        assertEquals(0L, navigatedId)
    }

}
