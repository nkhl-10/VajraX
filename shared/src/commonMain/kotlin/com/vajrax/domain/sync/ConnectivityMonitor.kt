package com.vajrax.domain.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Whether the device is online. The offline release has no network access, so it reports
 * offline; the sync phase plugs in a platform monitor (Android: ConnectivityManager).
 */
interface ConnectivityMonitor {
    val isOnline: StateFlow<Boolean>
}

/** Default for the offline release and for tests. */
object OfflineOnly : ConnectivityMonitor {
    override val isOnline: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
}
