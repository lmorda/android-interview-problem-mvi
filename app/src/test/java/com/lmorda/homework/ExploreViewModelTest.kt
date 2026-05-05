package com.lmorda.homework

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lmorda.homework.domain.DataRepository
import com.lmorda.homework.domain.model.mockDomainData
import com.lmorda.homework.ui.explore.ExploreContract
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnLoadNextPage
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnRefresh
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnSearchClear
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnSearchName
import com.lmorda.homework.ui.explore.ExploreViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: DataRepository = mockk()
    private lateinit var viewModel: ExploreViewModel


    @Test
    fun `loading state on init`() = runTest {
        viewModel = ExploreViewModel(dataRepository = repository)

        assertEquals(ExploreContract.State.Initial, viewModel.state.value)
    }

    @Test
    fun `refresh after search reloads first page with current query`() = runTest {
        val query = "retrofit"
        coEvery {
            repository.searchRepos(
                page = 1,
                query = query,
            )
        } returns mockDomainData
        viewModel = ExploreViewModel(dataRepository = repository)

        viewModel.push(OnSearchName(query))
        advanceUntilIdle()

        viewModel.push(OnRefresh)
        advanceUntilIdle()

        assertEquals(
            ExploreContract.State.Loaded(
                githubRepos = mockDomainData,
                nextPage = 2,
                searchQuery = query,
            ),
            viewModel.state.value,
        )
        coVerify(exactly = 2) {
            repository.searchRepos(
                page = 1,
                query = query,
            )
        }
        coVerify(exactly = 0) {
            repository.searchRepos(
                page = 1,
                query = "",
            )
        }
    }

    @Test
    fun `search loads first page with query after debounce`() = runTest {
        val query = "compose"
        coEvery {
            repository.searchRepos(
                page = 1,
                query = query,
            )
        } returns mockDomainData
        viewModel = ExploreViewModel(dataRepository = repository)

        viewModel.push(OnSearchName(query))
        advanceUntilIdle()

        assertEquals(
            ExploreContract.State.Loaded(
                githubRepos = mockDomainData,
                nextPage = 2,
                searchQuery = query,
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun `loading next page appends results and advances next page`() = runTest {
        val query = "android"
        coEvery {
            repository.searchRepos(
                page = 1,
                query = query,
            )
        } returns listOf(mockDomainData[0])
        coEvery {
            repository.searchRepos(
                page = 2,
                query = query,
            )
        } returns listOf(mockDomainData[1])
        viewModel = ExploreViewModel(dataRepository = repository)

        viewModel.push(OnSearchName(query))
        advanceUntilIdle()
        viewModel.push(OnLoadNextPage)
        assertEquals(
            ExploreContract.State.LoadingPage(
                githubRepos = listOf(mockDomainData[0]),
                searchQuery = query,
            ),
            viewModel.state.value,
        )
        advanceUntilIdle()

        assertEquals(
            ExploreContract.State.Loaded(
                githubRepos = mockDomainData,
                nextPage = 3,
                searchQuery = query,
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun `search error emits load error`() = runTest {
        val exception = Exception("boom")
        coEvery {
            repository.searchRepos(
                page = 1,
                query = "compose",
            )
        } throws exception
        viewModel = ExploreViewModel(dataRepository = repository)

        viewModel.push(OnSearchName("compose"))
        advanceUntilIdle()

        assertEquals(
            ExploreContract.State.LoadError(errorMessage = "boom"),
            viewModel.state.value,
        )
    }

    @Test
    fun `blank search stays initial and does not call repository`() = runTest {
        viewModel = ExploreViewModel(dataRepository = repository)

        viewModel.push(OnSearchName("   "))
        advanceUntilIdle()

        assertEquals(ExploreContract.State.Initial, viewModel.state.value)
        coVerify(exactly = 0) {
            repository.searchRepos(page = 1, query = "")
        }
    }

    @Test
    fun `search clear returns to initial state`() = runTest {
        coEvery {
            repository.searchRepos(
                page = 1,
                query = "compose",
            )
        } returns mockDomainData
        viewModel = ExploreViewModel(dataRepository = repository)
        viewModel.push(OnSearchName("compose"))
        advanceUntilIdle()

        viewModel.push(OnSearchClear)
        advanceUntilIdle()

        assertEquals(ExploreContract.State.Initial, viewModel.state.value)
    }
}
