package com.lmorda.homework.ui.details

import com.lmorda.homework.domain.model.GithubRepo

interface DetailsContract {

    sealed class State {

        data object Initial : State()

        data object Loading : State()

        data class Loaded(
            val githubRepo: GithubRepo
        ) : State()

        data class LoadError(
            val errorMessage: String?
        ) : State()
    }

    sealed class Event {

        data object OnLoadDetails : Event()

        sealed class Internal : Event() {
            data class OnLoaded(
                val githubRepo: GithubRepo
            ) : Event()

            data class OnLoadError(
                val errorMessage: String?
            ) : Internal()
        }
    }
}
