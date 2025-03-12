package com.lmorda.homework.data.model

val mockApiData = GithubReposDto(
    items = listOf(
        GithubRepoDto(
            id = 0,
            name = "my-application-1",
            owner = OwnerDto("google", "", ""),
            description = "description for google my application 1",
            stargazersCount = 345123,
            language = "Kotlin",
        ),
        GithubRepoDto(
            id = 1,
            name = "my-application-2",
            owner = OwnerDto("google", "", ""),
            description = "description for google my application 2",
            stargazersCount = 1234,
            language = "Java",
        ),
    ),
)
