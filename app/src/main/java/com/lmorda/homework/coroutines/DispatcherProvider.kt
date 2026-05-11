package com.lmorda.homework.coroutines

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    fun events(): CoroutineDispatcher
}
