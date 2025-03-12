package com.lmorda.homework.domain.model

val mockDomainData = listOf(
    GithubRepo(
        id = 0,
        name = "my-application-1",
        owner = Owner("google", "", ""),
        description = "description for google my application 1",
        stargazersCount = 345123,
        language = "Kotlin",
    ),
    GithubRepo(
        id = 1,
        name = "my-application-2",
        owner = Owner("google", "", ""),
        description = "description for google my application 2",
        stargazersCount = 1234,
        language = "Java",
    ),
)
