package com.lmorda.homework.ui.explore

import androidx.lifecycle.viewModelScope
import com.lmorda.homework.coroutines.DispatcherProvider
import com.lmorda.homework.domain.DataRepository
import com.lmorda.homework.domain.model.GithubRepo
import com.lmorda.homework.ui.MviViewModel
import com.lmorda.homework.ui.explore.ExploreContract.Event
import com.lmorda.homework.ui.explore.ExploreContract.Event.Internal.OnLoadError
import com.lmorda.homework.ui.explore.ExploreContract.Event.Internal.OnLoaded
import com.lmorda.homework.ui.explore.ExploreContract.Event.Internal.OnRateLimitReached
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
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val FIRST_PAGE_NUM = 1
private const val HTTP_FORBIDDEN = 403

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    dispatcherProvider: DispatcherProvider,
) : MviViewModel<State, Event>(
    initialState = State.Initial,
    eventsDispatcher = dispatcherProvider.events(),
) {

    private var searchJob: Job? = null

    override fun reduce(state: State, event: Event): State = when (event) {
        is OnLoadNextPage -> {
            if (state is State.Loaded && state.nextPage != null) {
                getRepoPage(
                    currentRepos = state.githubRepos,
                    nextPage = state.nextPage,
                    searchQuery = state.searchQuery,
                )
                State.LoadingPage(
                    githubRepos = state.githubRepos,
                    searchQuery = state.searchQuery,
                )
            } else state
        }

        is OnLoaded -> State.Loaded(
            githubRepos = event.githubRepos,
            nextPage = event.nextPage,
            searchQuery = event.searchQuery,
        )

        is OnLoadError -> State.LoadError

        is OnRateLimitReached -> State.RateLimitReached

        is OnSearchName -> {
            val searchQuery = event.query.trim()
            if (searchQuery.isBlank()) {
                searchJob?.cancel()
                State.Initial
            } else {
                getFilteredFirstPage(searchQuery = searchQuery, debounce = true)
                state
            }
        }

        is OnRefresh -> if (state is State.Loaded) {
            getFilteredFirstPage(searchQuery = state.searchQuery, debounce = false)
            State.LoadingRefresh(searchQuery = state.searchQuery)
        } else state

        is OnSearchClear -> State.Initial
    }

    private fun getRepoPage(
        currentRepos: List<GithubRepo>?,
        nextPage: Int,
        searchQuery: String,
    ) {
        viewModelScope.launch {
            try {
                val reposPage = dataRepository.searchRepos(
                    page = nextPage,
                    query = searchQuery,
                )
                val githubRepos = currentRepos ?: emptyList()
                val newRepos = githubRepos + reposPage
                push(
                    OnLoaded(
                        githubRepos = newRepos,
                        nextPage = if (reposPage.isEmpty()) null else nextPage + 1,
                        searchQuery = searchQuery,
                    )
                )
            } catch (e: Exception) {
                push(e.toLoadErrorEvent())
            }
        }
    }


    private fun getFilteredFirstPage(searchQuery: String, debounce: Boolean) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounce) {
                delay(EXPLORE_FILTER_DEBOUNCE_MILLIS)
            }
            try {
                val githubRepos = dataRepository.searchRepos(
                    page = FIRST_PAGE_NUM,
                    query = searchQuery,
                )
                push(
                    OnLoaded(
                        githubRepos = githubRepos,
                        nextPage = if (githubRepos.isEmpty()) null else FIRST_PAGE_NUM + 1,
                        searchQuery = searchQuery,
                    )
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                push(e.toLoadErrorEvent())
            }
        }
    }

    private fun Exception.toLoadErrorEvent(): Event.Internal = if (
        this is HttpException && code() == HTTP_FORBIDDEN
    ) {
        OnRateLimitReached
    } else {
        OnLoadError
    }
}
