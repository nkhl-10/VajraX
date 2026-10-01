package com.vajrax.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// The browser has one thread; database work already runs in a web worker.
internal actual val platformIoDispatcher: CoroutineDispatcher = Dispatchers.Default
