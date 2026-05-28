package com.lmorda.homework

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lmorda.homework.domain.DataRepository
import com.lmorda.homework.dispatchers.TestDispatcherProvider
import com.lmorda.homework.domain.model.GithubRepo
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testDispatcherRule = TestDispatcherRule()

    private val repository: DataRepository = mockk()
    private lateinit var viewModel: ExploreViewModel


    @Test
    fun `loading state on init`() = runTest(testDispatcherRule.dispatcher) {
        viewModel = ExploreViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
        )

        assertEquals(ExploreContract.State.Initial, viewModel.state.value)
    }

    @Test
    fun `refresh after search reloads first page with current query`() = runTest(testDispatcherRule.dispatcher) {
        val query = "retrofit"
        coEvery {
            repository.searchRepos(
                page = 1,
                query = query,
            )
        } returns mockDomainData
        viewModel = ExploreViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
        )

        viewModel.push(OnSearchName(query))
        advanceUntilIdle()

        viewModel.push(OnRefresh)

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
    fun `search loads first page with query after debounce`() = runTest(testDispatcherRule.dispatcher) {
        val query = "compose"
        coEvery {
            repository.searchRepos(
                page = 1,
                query = query,
            )
        } returns mockDomainData
        viewModel = ExploreViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
        )

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
    fun `loading next page appends results and advances next page`() = runTest(testDispatcherRule.dispatcher) {
        val query = "android"
        val nextPage = CompletableDeferred<List<GithubRepo>>()
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
        } coAnswers {
            nextPage.await()
        }
        viewModel = ExploreViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
        )

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

        nextPage.complete(listOf(mockDomainData[1]))

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
    fun `empty next page keeps current results and clears next page`() = runTest(testDispatcherRule.dispatcher) {
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
        } returns emptyList()
        viewModel = ExploreViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
        )

        viewModel.push(OnSearchName(query))
        advanceUntilIdle()
        viewModel.push(OnLoadNextPage)

        assertEquals(
            ExploreContract.State.Loaded(
                githubRepos = listOf(mockDomainData[0]),
                nextPage = null,
                searchQuery = query,
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun `empty first page emits terminal loaded state`() = runTest(testDispatcherRule.dispatcher) {
        val query = "android"
        coEvery {
            repository.searchRepos(
                page = 1,
                query = query,
            )
        } returns emptyList()
        viewModel = ExploreViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
        )

        viewModel.push(OnSearchName(query))
        advanceUntilIdle()

        assertEquals(
            ExploreContract.State.Loaded(
                githubRepos = emptyList(),
                nextPage = null,
                searchQuery = query,
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun `search error emits load error`() = runTest(testDispatcherRule.dispatcher) {
        val exception = Exception("boom")
        coEvery {
            repository.searchRepos(
                page = 1,
                query = "compose",
            )
        } throws exception
        viewModel = ExploreViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
        )

        viewModel.push(OnSearchName("compose"))
        advanceUntilIdle()

        assertEquals(
            ExploreContract.State.LoadError,
            viewModel.state.value,
        )
    }

    @Test
    fun `rate limit error emits rate limit state`() = runTest(testDispatcherRule.dispatcher) {
        coEvery {
            repository.searchRepos(
                page = 1,
                query = "compose",
            )
        } throws httpException(code = 403)
        viewModel = ExploreViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
        )

        viewModel.push(OnSearchName("compose"))
        advanceUntilIdle()

        assertEquals(ExploreContract.State.RateLimitReached, viewModel.state.value)
    }

    @Test
    fun `blank search stays initial and does not call repository`() = runTest(testDispatcherRule.dispatcher) {
        viewModel = ExploreViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
        )

        viewModel.push(OnSearchName("   "))

        assertEquals(ExploreContract.State.Initial, viewModel.state.value)
        coVerify(exactly = 0) {
            repository.searchRepos(page = 1, query = "")
        }
    }

    @Test
    fun `search clear returns to initial state`() = runTest(testDispatcherRule.dispatcher) {
        coEvery {
            repository.searchRepos(
                page = 1,
                query = "compose",
            )
        } returns mockDomainData
        viewModel = ExploreViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
        )
        viewModel.push(OnSearchName("compose"))

        viewModel.push(OnSearchClear)

        assertEquals(ExploreContract.State.Initial, viewModel.state.value)
    }

    private fun httpException(code: Int) = HttpException(
        Response.error<Unit>(
            code,
            "".toResponseBody(),
        )
    )
}
