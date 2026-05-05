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
import org.junit.Test

class DataRepositoryImplTest {

    private val apiService = mockk<ApiService>()
    private val dataRepository = DataRepositoryImpl(
        apiService = apiService,
        mapper = GithubRepoMapper(),
    )

    @Test
    fun `getRepositories should map filter query and return mapped repositories`() = runTest {
        coEvery {
            apiService.searchRepositories(
                page = 2,
                perPage = REPOS_PER_PAGE,
                query = "compose",
                order = ORDER,
                sort = SORT,
            )
        } returns mockApiData

        val repos = dataRepository.getRepos(page = 2, query = "compose")

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
    fun `getRepositories should default to first page`() = runTest {
        coEvery {
            apiService.searchRepositories(
                page = 1,
                perPage = REPOS_PER_PAGE,
                query = "android",
                order = ORDER,
                sort = SORT,
            )
        } returns mockApiData

        dataRepository.getRepos(page = null, query = "android")

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
