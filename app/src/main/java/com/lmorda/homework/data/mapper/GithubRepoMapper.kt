package com.lmorda.homework.data.mapper

import com.lmorda.homework.data.model.GithubRepoDto
import com.lmorda.homework.data.model.GithubReposDto
import com.lmorda.homework.domain.model.GithubRepo
import com.lmorda.homework.domain.model.Owner
import javax.inject.Inject

class GithubRepoMapper @Inject constructor() {

    fun map(githubReposDto: GithubReposDto) =
        githubReposDto.items.map {
            map(githubRepoDto = it)
        }

    fun map(githubRepoDto: GithubRepoDto) =
        with(githubRepoDto) {
            GithubRepo(
                id = id,
                name = name,
                owner = Owner(
                    login = owner.login,
                    avatarUrl = owner.avatarUrl,
                    htmlUrl = owner.htmlUrl,
                ),
                description = description,
                stargazersCount = stargazersCount,
                language = language,
            )
        }
}
