package com.lmorda.homework.ui.explore

import androidx.lifecycle.viewModelScope
import com.lmorda.homework.data.api.FIRST_PAGE_NUM
import com.lmorda.homework.domain.DataRepository
import com.lmorda.homework.domain.model.GithubRepo
import com.lmorda.homework.ui.MviViewModel
import com.lmorda.homework.ui.explore.ExploreContract.Event
import com.lmorda.homework.ui.explore.ExploreContract.Event.Internal.OnLoadError
import com.lmorda.homework.ui.explore.ExploreContract.Event.Internal.OnLoaded
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnLoadNextPage
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnRefresh
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnSearchClear
import com.lmorda.homework.ui.explore.ExploreContract.Event.OnSearchName
import com.lmorda.homework.ui.explore.ExploreContract.State
import com.lmorda.homework.ui.shared.EXPLORE_FILTER_DEBOUNCE_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val dataRepository: DataRepository,
) : MviViewModel<State, Event>(
    initialState = State.Initial,
) {

    private var searchJob: Job? = null

    override fun reduce(state: State, event: Event): State = when (event) {
        is OnLoadNextPage -> {
            if (state is State.Loaded && state.nextPage != null) {
                getRepoPage(
                    currentRepos = state.githubRepos,
                    nextPage = state.nextPage,
                    query = state.query,
                )
                State.LoadingPage(
                    githubRepos = state.githubRepos,
                )
            } else state
        }

        is OnLoaded -> State.Loaded(
            githubRepos = event.githubRepos,
            nextPage = event.nextPage,
            query = event.query,
        )

        is OnLoadError -> State.LoadError(
            errorMessage = event.errorMessage,
        )

        is OnSearchName -> {
            getFilteredFirstPage(query = event.query)
            state
        }

        is OnRefresh -> {
            getFirstPage()
            State.LoadingRefresh
        }

        is OnSearchClear -> State.Initial
    }

    private fun getFirstPage() {
        getRepoPage(
            currentRepos = null,
            nextPage = null,
            query = null,
        )
    }

    private fun getRepoPage(
        currentRepos: List<GithubRepo>?,
        nextPage: Int?,
        query: String?,
    ) {
        viewModelScope.launch {
            try {
                val reposPage = dataRepository.getRepos(
                    page = nextPage,
                    query = query,
                )
                val githubRepos = currentRepos ?: emptyList()
                val newRepos = githubRepos + reposPage
                push(
                    OnLoaded(
                        githubRepos = newRepos,
                        nextPage = nextPage ?: FIRST_PAGE_NUM,
                        query = query,
                    )
                )
            } catch (e: Exception) {
                push(OnLoadError(errorMessage = e.message))
            }
        }
    }


    private fun getFilteredFirstPage(query: String?) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (!query.isNullOrBlank()) {
                delay(EXPLORE_FILTER_DEBOUNCE_MILLIS)
            }
            try {
                val githubRepos = dataRepository.getRepos(
                    page = null,
                    query = query,
                )
                push(
                    OnLoaded(
                        githubRepos = githubRepos,
                        nextPage = FIRST_PAGE_NUM + 1,
                        query = query,
                    )
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                push(OnLoadError(errorMessage = e.message))
            }
        }
    }
}
