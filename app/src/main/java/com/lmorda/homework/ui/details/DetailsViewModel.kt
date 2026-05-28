package com.lmorda.homework.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lmorda.homework.dispatchers.DispatcherProvider
import com.lmorda.homework.domain.DataRepository
import com.lmorda.homework.ui.MviViewModel
import com.lmorda.homework.ui.details.DetailsContract.Event
import com.lmorda.homework.ui.details.DetailsContract.Event.Internal.OnLoadError
import com.lmorda.homework.ui.details.DetailsContract.Event.Internal.OnLoaded
import com.lmorda.homework.ui.details.DetailsContract.Event.OnLoadDetails
import com.lmorda.homework.ui.details.DetailsContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

const val SAVED_ID_KEY = "id"

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository,
    dispatcherProvider: DispatcherProvider,
) : MviViewModel<State, Event>(
    initialState = State.Initial,
    eventsDispatcher = dispatcherProvider.events(),
) {

    private val id: Long? = savedStateHandle[SAVED_ID_KEY]

    override fun reduce(state: State, event: Event): State = when (event) {
        is OnLoadDetails -> id?.let {
            getRepo(id = it)
            State.Loading
        } ?: State.LoadError(
            errorMessage = "Repository id is missing.",
        )

        is OnLoaded -> State.Loaded(githubRepo = event.githubRepo)
        is OnLoadError -> State.LoadError(errorMessage = event.errorMessage)
    }

    private fun getRepo(id: Long) {
        viewModelScope.launch {
            try {
                push(OnLoaded(githubRepo = dataRepository.getRepo(id = id)))
            } catch (e: Exception) {
                push(OnLoadError(errorMessage = e.message))
            }
        }
    }
}
