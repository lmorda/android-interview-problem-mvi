package com.lmorda.homework.ui.explore

import com.lmorda.homework.domain.model.GithubRepo

interface ExploreContract {

    sealed class State {

        data object Initial : State()

        data class Loaded(
            val githubRepos: List<GithubRepo>,
            val nextPage: Int?,
            val query: String?,
        ) : State()

        data class LoadingPage(
            val githubRepos: List<GithubRepo>,
        ) : State()

        data object LoadingRefresh : State()

        data class LoadError(
            val errorMessage: String?,
        ) : State()
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
                val query: String?,
            ) : Internal()

            data class OnLoadError(
                val errorMessage: String?,
            ) : Internal()
        }
    }
}
