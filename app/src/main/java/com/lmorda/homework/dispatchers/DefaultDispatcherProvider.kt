package com.lmorda.homework.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {

    override fun events(): CoroutineDispatcher = Dispatchers.Main.immediate
}
