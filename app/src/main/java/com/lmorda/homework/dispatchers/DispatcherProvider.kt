package com.lmorda.homework.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    fun events(): CoroutineDispatcher
}
