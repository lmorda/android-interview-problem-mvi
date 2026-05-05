package com.lmorda.homework.data

import com.lmorda.homework.data.api.ApiService
import com.lmorda.homework.data.api.ORDER
import com.lmorda.homework.data.api.REPOS_PER_PAGE
import com.lmorda.homework.data.api.SORT
import com.lmorda.homework.data.api.safeApiCall
import com.lmorda.homework.data.mapper.GithubRepoMapper
import com.lmorda.homework.domain.DataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val mapper: GithubRepoMapper,
) : DataRepository {

    override suspend fun searchRepos(
        page: Int,
        query: String,
    ) = safeApiCall(
        apiCall = {
            require(query.isNotBlank()) { "Search query must not be blank." }
            apiService.searchRepositories(
                page = page,
                perPage = REPOS_PER_PAGE,
                query = query,
                order = ORDER,
                sort = SORT,
            )
        },
        transform = mapper::map,
    )

    override suspend fun getRepo(id: Long) = safeApiCall(
        apiCall = { apiService.getRepo(id = id) },
        transform = mapper::map,
    )
}
