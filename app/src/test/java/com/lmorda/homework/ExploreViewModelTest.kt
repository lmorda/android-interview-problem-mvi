package com.lmorda.homework

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lmorda.homework.domain.DataRepository
import com.lmorda.homework.domain.model.mockDomainData
import com.lmorda.homework.ui.explore.ExploreContract
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnRefresh
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnSearchName
import com.lmorda.homework.ui.explore.ExploreViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val repository: DataRepository = mockk()
    private lateinit var viewModel: ExploreViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `loading state on init`() = runTest {
        coEvery {
            repository.getRepos(
                page = null,
                query = null,
            )
        } returns mockDomainData
        viewModel = ExploreViewModel(dataRepository = repository)
        assertEquals(ExploreContract.State.Initial, viewModel.state.value)
    }

    // TODO: Add more unit tests

    @Test
    fun `refresh after search reloads first page with current query`() = runTest {
        val query = "retrofit"
        coEvery {
            repository.getRepos(
                page = null,
                query = query,
            )
        } returns mockDomainData
        viewModel = ExploreViewModel(dataRepository = repository)

        viewModel.push(OnSearchName(query))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.push(OnRefresh)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            ExploreContract.State.Loaded(
                githubRepos = mockDomainData,
                nextPage = 2,
                query = query,
            ),
            viewModel.state.value,
        )
        coVerify(exactly = 2) {
            repository.getRepos(
                page = null,
                query = query,
            )
        }
        coVerify(exactly = 0) {
            repository.getRepos(
                page = null,
                query = null,
            )
        }
    }
}
