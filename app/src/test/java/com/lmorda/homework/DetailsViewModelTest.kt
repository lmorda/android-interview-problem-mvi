package com.lmorda.homework

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.lmorda.homework.domain.DataRepository
import com.lmorda.homework.domain.model.mockDomainData
import com.lmorda.homework.ui.details.DetailsContract.State.Initial
import com.lmorda.homework.ui.details.DetailsViewModel
import io.mockk.coEvery
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
class DetailsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val repository: DataRepository = mockk()
    private lateinit var viewModel: DetailsViewModel

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
        coEvery { repository.getRepo(id = 123L) } returns mockDomainData[0]
        val savedStateHandle = SavedStateHandle(mapOf("id" to 123L))
        viewModel =
            DetailsViewModel(dataRepository = repository, savedStateHandle = savedStateHandle)
        assertEquals(Initial, viewModel.state.value)
    }

    // TODO: Add more unit tests
}
