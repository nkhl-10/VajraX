package com.vajrax.data.sync

import com.vajrax.domain.sync.CloudSync
import com.vajrax.domain.sync.FirstSync
import com.vajrax.domain.sync.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Builds without a server (tests, iOS for now): the app stays offline-only. */
object NoCloudSync : CloudSync {
    override val status: StateFlow<SyncStatus> = MutableStateFlow<SyncStatus>(SyncStatus.Off).asStateFlow()
    override suspend fun start() = Unit
    override suspend fun syncNow() = Unit
    override fun requestSync() = Unit
    override suspend fun enable(firstSync: FirstSync) = Unit
    override suspend fun disable(removeLocalData: Boolean) = Unit
    override suspend fun isEnabled(): Boolean = false
    override suspend fun hasLocalData(): Boolean = false
    override suspend fun accountHasData(): Boolean = false
}
