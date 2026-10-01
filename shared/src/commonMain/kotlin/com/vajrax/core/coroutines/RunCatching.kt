package com.vajrax.core.coroutines

import kotlin.coroutines.cancellation.CancellationException

/**
 * [runCatching] for coroutine code: failures become a [Result], but cancellation is rethrown so
 * a closed screen or finished receiver stops instead of carrying on as if nothing happened.
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
