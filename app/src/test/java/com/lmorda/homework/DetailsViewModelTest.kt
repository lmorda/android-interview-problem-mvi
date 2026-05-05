package com.lmorda.homework

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.lmorda.homework.domain.DataRepository
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: DataRepository = mockk()
    private lateinit var viewModel: DetailsViewModel

    @Test
    fun `loading state on init`() = runTest {
        coEvery { repository.getRepo(id = 123L) } returns mockDomainData[0]
        val savedStateHandle = SavedStateHandle(mapOf("id" to 123L))
        viewModel =
            DetailsViewModel(dataRepository = repository, savedStateHandle = savedStateHandle)
        assertEquals(Initial, viewModel.state.value)
    }

    // TODO: Add more unit tests

    @Test
    fun `load details emits loading then loaded state`() = runTest {
        coEvery { repository.getRepo(id = 123L) } returns mockDomainData[0]
        viewModel = DetailsViewModel(
            dataRepository = repository,
            savedStateHandle = SavedStateHandle(mapOf("id" to 123L)),
        )

        viewModel.push(OnLoadDetails)
        assertEquals(Loading, viewModel.state.value)
        advanceUntilIdle()

        assertEquals(Loaded(githubRepo = mockDomainData[0]), viewModel.state.value)
        coVerify(exactly = 1) { repository.getRepo(id = 123L) }
    }

    @Test
    fun `load details error emits load error state`() = runTest {
        coEvery { repository.getRepo(id = 123L) } throws Exception("boom")
        viewModel = DetailsViewModel(
            dataRepository = repository,
            savedStateHandle = SavedStateHandle(mapOf("id" to 123L)),
        )

        viewModel.push(OnLoadDetails)
        advanceUntilIdle()

        assertEquals(LoadError(errorMessage = "boom"), viewModel.state.value)
    }
}
