package com.vajrax.domain.sync

import kotlinx.coroutines.flow.StateFlow

/** Where cloud backup stands, for the Profile account card. */
sealed interface SyncStatus {
    /** Signed out: everything stays on this device only. */
    data object Off : SyncStatus
    data object Syncing : SyncStatus
    data class UpToDate(val lastSyncAt: Long?) : SyncStatus

    /** Changes are kept and sent when the connection is back. */
    data class Waiting(val lastSyncAt: Long?, val pendingChanges: Long) : SyncStatus
    data class Failed(val message: String, val lastSyncAt: Long?) : SyncStatus
}

/** How the first sign-in on a device treats data already on it. */
enum class FirstSync {
    /** Keep this device's data and add it to the account (both sides end up with everything). */
    MERGE,

    /** Replace this device's data with the account's. */
    USE_ACCOUNT
}

/**
 * Offline-first cloud backup and sync. The app keeps working from its local database; signed-in
 * changes are uploaded in the background and changes from other devices are applied locally.
 */
interface CloudSync {
    val status: StateFlow<SyncStatus>

    /** App start: prepares change capture and syncs if signed in. */
    suspend fun start()

    /** Uploads pending changes and downloads new ones now. */
    suspend fun syncNow()

    /** Asks for a sync soon (debounced); safe to call after every change. */
    fun requestSync()

    /** After sign-in. */
    suspend fun enable(firstSync: FirstSync)

    /** After sign-out or account deletion; [removeLocalData] also clears this device. */
    suspend fun disable(removeLocalData: Boolean)

    suspend fun isEnabled(): Boolean

    suspend fun hasLocalData(): Boolean

    /** Whether the signed-in account already holds synced data (decides the first-sync question). */
    suspend fun accountHasData(): Boolean
}
