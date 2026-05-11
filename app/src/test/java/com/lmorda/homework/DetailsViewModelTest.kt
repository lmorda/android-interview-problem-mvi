package com.lmorda.homework

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.lmorda.homework.coroutines.TestDispatcherProvider
import com.lmorda.homework.domain.DataRepository
import com.lmorda.homework.domain.model.GithubRepo
import com.lmorda.homework.domain.model.mockDomainData
import com.lmorda.homework.ui.details.DetailsContract.Event.OnLoadDetails
import com.lmorda.homework.ui.details.DetailsContract.State.Initial
import com.lmorda.homework.ui.details.DetailsContract.State.LoadError
import com.lmorda.homework.ui.details.DetailsContract.State.Loaded
import com.lmorda.homework.ui.details.DetailsContract.State.Loading
import com.lmorda.homework.ui.details.DetailsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testDispatcherRule = TestDispatcherRule()

    private val repository: DataRepository = mockk()
    private lateinit var viewModel: DetailsViewModel

    @Test
    fun `loading state on init`() = runTest(testDispatcherRule.dispatcher) {
        coEvery { repository.getRepo(id = 123L) } returns mockDomainData[0]
        val savedStateHandle = SavedStateHandle(mapOf("id" to 123L))
        viewModel =
            DetailsViewModel(
                dataRepository = repository,
                dispatcherProvider = TestDispatcherProvider(),
                savedStateHandle = savedStateHandle,
            )
        assertEquals(Initial, viewModel.state.value)
    }

    @Test
    fun `load details emits loading then loaded state`() = runTest(testDispatcherRule.dispatcher) {
        val repo = CompletableDeferred<GithubRepo>()
        coEvery { repository.getRepo(id = 123L) } coAnswers {
            repo.await()
        }
        viewModel = DetailsViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
            savedStateHandle = SavedStateHandle(mapOf("id" to 123L)),
        )

        viewModel.push(OnLoadDetails)
        assertEquals(Loading, viewModel.state.value)

        repo.complete(mockDomainData[0])

        assertEquals(Loaded(githubRepo = mockDomainData[0]), viewModel.state.value)
        coVerify(exactly = 1) { repository.getRepo(id = 123L) }
    }

    @Test
    fun `load details error emits load error state`() = runTest(testDispatcherRule.dispatcher) {
        coEvery { repository.getRepo(id = 123L) } throws Exception("boom")
        viewModel = DetailsViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
            savedStateHandle = SavedStateHandle(mapOf("id" to 123L)),
        )

        viewModel.push(OnLoadDetails)

        assertEquals(LoadError(errorMessage = "boom"), viewModel.state.value)
    }

    @Test
    fun `missing details id emits load error without calling repository`() = runTest(testDispatcherRule.dispatcher) {
        viewModel = DetailsViewModel(
            dataRepository = repository,
            dispatcherProvider = TestDispatcherProvider(),
            savedStateHandle = SavedStateHandle(),
        )

        viewModel.push(OnLoadDetails)

        assertEquals(LoadError(errorMessage = "Repository id is missing."), viewModel.state.value)
        coVerify(exactly = 0) { repository.getRepo(id = any()) }
    }
}
