package com.lmorda.homework.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherProvider(
    private val eventsDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(),
) : DispatcherProvider {

    override fun events(): CoroutineDispatcher = eventsDispatcher
}
