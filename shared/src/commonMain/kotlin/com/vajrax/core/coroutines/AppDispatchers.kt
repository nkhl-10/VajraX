package com.vajrax.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Dispatchers shared code uses instead of `Dispatchers.IO`, which browsers don't have.
 * [IO] is `Dispatchers.IO` on Android and iOS and the default dispatcher on the web, where the
 * database already runs in a worker.
 */
object AppDispatchers {
    val IO: CoroutineDispatcher get() = platformIoDispatcher
}

internal expect val platformIoDispatcher: CoroutineDispatcher
