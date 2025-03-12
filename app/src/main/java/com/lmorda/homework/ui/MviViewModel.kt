package com.lmorda.homework.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

abstract class MviViewModel<State, Event>(
    initialState: State,
) : ViewModel() {
    private val events = Channel<Event>(Channel.UNLIMITED)
    private val _state = MutableLiveData(initialState)
    val state = _state as LiveData<State>

    init {
        viewModelScope.launch(Dispatchers.Unconfined) {
            events.consumeAsFlow()
                .scan(initialState) { state, event ->
                    reduce(state, event)
                }
                .collect {
                    launch(Dispatchers.Main) {
                        _state.value = it
                    }
                }
        }
    }

    protected abstract fun reduce(state: State, event: Event): State

    fun push(event: Event) {
        events.trySend(event)
    }
}
