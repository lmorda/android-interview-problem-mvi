package com.lmorda.homework.data.api

import timber.log.Timber

suspend fun <T, R> safeApiCall(
    apiCall: suspend () -> T,
    transform: (T) -> R
): R {
    return try {
        val response = apiCall()
        transform(response)
    } catch (e: Exception) {
        Timber.e(e)
        throw e
    }
}
