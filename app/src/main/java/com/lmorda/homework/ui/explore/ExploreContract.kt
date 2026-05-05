package com.lmorda.homework.ui.explore

import com.lmorda.homework.domain.model.GithubRepo

interface ExploreContract {

    sealed class State {

        data object Initial : State()

        data class Loaded(
            val githubRepos: List<GithubRepo>,
            val nextPage: Int?,
            val searchQuery: String,
        ) : State()

        data class LoadingPage(
            val githubRepos: List<GithubRepo>,
            val searchQuery: String,
        ) : State()

        data class LoadingRefresh(
            val searchQuery: String,
        ) : State()

        data object LoadError : State()

        data object RateLimitReached : State()
    }

    sealed class Event {

        data object OnRefresh : Event()
        data object OnLoadNextPage : Event()

        data class OnSearchName(
            val query: String,
        ) : Event()

        data object OnSearchClear : Event()

        sealed class Internal : Event() {
            data class OnLoaded(
                val githubRepos: List<GithubRepo>,
                val nextPage: Int?,
                val searchQuery: String,
            ) : Internal()

            data object OnLoadError : Internal()

            data object OnRateLimitReached : Internal()
        }
    }
}
