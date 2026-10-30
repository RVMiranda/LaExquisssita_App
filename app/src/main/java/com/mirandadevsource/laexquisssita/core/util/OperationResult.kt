package com.mirandadevsource.laexquisssita.core.util

import kotlinx.coroutines.CancellationException

/** Cancellation must reach the coroutine owner, never become a UI error. */
suspend fun <T> operationResult(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: Exception) {
    Result.failure(error)
}

fun Throwable.customerMessage(): String = when (this) {
    is IllegalArgumentException -> "Revisa los datos e inténtalo de nuevo."
    else -> "No pudimos actualizar la información. Comprueba tu conexión e inténtalo de nuevo."
}
