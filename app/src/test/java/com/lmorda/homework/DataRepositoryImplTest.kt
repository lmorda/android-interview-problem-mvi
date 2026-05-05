package com.lmorda.homework

import com.lmorda.homework.data.DataRepositoryImpl
import com.lmorda.homework.data.api.ApiService
import com.lmorda.homework.data.api.ORDER
import com.lmorda.homework.data.api.REPOS_PER_PAGE
import com.lmorda.homework.data.api.SORT
import com.lmorda.homework.data.mapper.GithubRepoMapper
import com.lmorda.homework.data.model.mockApiData
import com.lmorda.homework.domain.model.mockDomainData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class DataRepositoryImplTest {

    private val apiService = mockk<ApiService>()
    private val dataRepository = DataRepositoryImpl(
        apiService = apiService,
        mapper = GithubRepoMapper(),
    )

    @Test
    fun `searchRepositories should map query and return mapped repositories`() = runTest {
        coEvery {
            apiService.searchRepositories(
                page = 2,
                perPage = REPOS_PER_PAGE,
                query = "compose",
                order = ORDER,
                sort = SORT,
            )
        } returns mockApiData

        val repos = dataRepository.searchRepos(page = 2, query = "compose")

        assertEquals(mockDomainData, repos)
        coVerify(exactly = 1) {
            apiService.searchRepositories(
                page = 2,
                perPage = REPOS_PER_PAGE,
                query = "compose",
                order = ORDER,
                sort = SORT,
            )
        }
    }

    @Test
    fun `getRepository should return mapped repository`() = runTest {
        coEvery { apiService.getRepo(id = 0) } returns mockApiData.items[0]

        val repo = dataRepository.getRepo(id = 0)

        assertEquals(mockDomainData[0], repo)
    }

    @Test
    fun `searchRepositories should require non blank query`() = runTest {
        try {
            dataRepository.searchRepos(page = 1, query = " ")
            fail("Expected blank query to throw IllegalArgumentException.")
        } catch (_: IllegalArgumentException) {
        }

        coVerify(exactly = 0) {
            apiService.searchRepositories(
                page = 1,
                perPage = REPOS_PER_PAGE,
                query = " ",
                order = ORDER,
                sort = SORT,
            )
        }
    }

    @Test
    fun `searchRepositories should use requested page`() = runTest {
        coEvery {
            apiService.searchRepositories(
                page = 1,
                perPage = REPOS_PER_PAGE,
                query = "android",
                order = ORDER,
                sort = SORT,
            )
        } returns mockApiData

        dataRepository.searchRepos(page = 1, query = "android")

        coVerify(exactly = 1) {
            apiService.searchRepositories(
                page = 1,
                perPage = REPOS_PER_PAGE,
                query = "android",
                order = ORDER,
                sort = SORT,
            )
        }
    }
}
