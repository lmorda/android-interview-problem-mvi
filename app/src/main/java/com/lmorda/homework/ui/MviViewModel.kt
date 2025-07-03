package com.lmorda.homework.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

abstract class MviViewModel<State, Event>(
    initialState: State,
) : ViewModel() {
    private val events = Channel<Event>(Channel.UNLIMITED)
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Unconfined) {
            events.consumeAsFlow()
                .scan(initialState) { state, event ->
                    reduce(state, event)
                }
                .collect { newState ->
                    _state.value = newState
                }
        }
    }

    protected abstract fun reduce(state: State, event: Event): State

    fun push(event: Event) {
        events.trySend(event)
    }
}
