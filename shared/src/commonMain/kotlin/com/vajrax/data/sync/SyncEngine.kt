@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class, kotlin.time.ExperimentalTime::class)

package com.vajrax.data.sync

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.db.SqlDriver
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.sync.PAYLOAD_SCHEMA
import com.vajrax.contract.sync.PushRequest
import com.vajrax.contract.sync.SyncChange
import com.vajrax.contract.sync.SyncRecord
import com.vajrax.core.coroutines.AppDispatchers
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.error.AppError
import com.vajrax.core.log.VxLog
import com.vajrax.core.time.AppClock
import com.vajrax.data.local.SyncTriggers
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.remote.SyncApi
import com.vajrax.domain.sync.CloudSync
import com.vajrax.domain.sync.FirstSync
import com.vajrax.domain.sync.SyncStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.Uuid

/**
 * Two-way sync between the local database and the account (see doc/api/sync-protocol.md).
 *
 * Push: every record noted in SyncOutbox is sent as it is now (or as a deletion when it is gone).
 * Pull: records newer than the cursor are applied unless this device holds a newer unsent change.
 * Downloaded records are written with SyncState.applying = 1, so they are not sent back.
 * Afterwards [onRemoteChanges] lets the app plan days and refresh reminders and widgets.
 */
class SyncEngine(
    private val database: VajraDatabase,
    private val driver: SqlDriver,
    private val api: SyncApi,
    private val clock: AppClock,
    private val onRemoteChanges: suspend () -> Unit,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + AppDispatchers.IO)
) : CloudSync {
    private val q = database.vajraDatabaseQueries
    private val codecs = SyncCodecs(database)
    private val mutex = Mutex()
    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Off)
    override val status: StateFlow<SyncStatus> = _status.asStateFlow()
    private var debounce: Job? = null
    private var watcher: Job? = null
    private var triggersInstalled = false

    override suspend fun start() {
        val state = prepare()
        if (state.enabled == 1L) {
            watchLocalChanges()
            syncNow()
        } else {
            _status.value = SyncStatus.Off
        }
    }

    override fun requestSync() {
        debounce?.cancel()
        debounce = scope.launch {
            delay(DEBOUNCE_MS)
            runCatchingCancellable { syncNow() }
        }
    }

    override suspend fun syncNow() = mutex.withLock {
        val state = q.syncState().awaitAsOneOrNull()
        if (state == null || state.enabled != 1L) {
            _status.value = SyncStatus.Off
            return@withLock
        }
        _status.value = SyncStatus.Syncing
        try {
            val pushedRemote = push(state.deviceId)
            val pulledRemote = pull(state.deviceId)
            if (pushedRemote || pulledRemote) {
                keepOneActiveRoutine()
                onRemoteChanges()
            }
            val now = clock.now().toEpochMilliseconds()
            q.syncSetLastSync(now)
            _status.value = SyncStatus.UpToDate(now)
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            VxLog.w(TAG, "Sync failed", e)
            val last = q.syncState().awaitAsOneOrNull()?.lastSyncAt
            _status.value = when (e) {
                is AppError.Offline, is AppError.Timeout -> SyncStatus.Waiting(last, q.outboxCount().awaitAsOne())
                is AppError.SignedOut -> SyncStatus.Off
                else -> SyncStatus.Failed(e.message ?: "Sync failed", last)
            }
        }
    }

    override suspend fun enable(firstSync: FirstSync) {
        mutex.withLock {
            prepare()
            database.transaction {
                q.outboxClear()
                q.metaClear()
                q.syncSetCursor(0)
                q.syncSetDeviceId(newDeviceId())
                when (firstSync) {
                    // Everything already here goes up; the account's records come down on the pull.
                    FirstSync.MERGE -> q.outboxSnapshot(clock.now().toEpochMilliseconds()).await()
                    FirstSync.USE_ACCOUNT -> q.wipeSyncedData().await()
                }
                q.syncSetEnabled(1)
            }
        }
        watchLocalChanges()
        syncNow()
    }

    override suspend fun disable(removeLocalData: Boolean) {
        debounce?.cancel()
        watcher?.cancel()
        mutex.withLock {
            prepare()
            database.transaction {
                // Capture is off first, so clearing the device doesn't queue deletions.
                q.syncSetEnabled(0)
                q.outboxClear()
                q.metaClear()
                q.syncSetCursor(0)
                if (removeLocalData) q.wipePersonalData().await()
            }
        }
        _status.value = SyncStatus.Off
        if (removeLocalData) onRemoteChanges()
    }

    override suspend fun hasLocalData(): Boolean = q.personalRecordCount().awaitAsOne() > 0

    override suspend fun accountHasData(): Boolean = api.pull(since = 0, limit = 1).changes.isNotEmpty()

    // ------------------------------------------------------------------ push / pull

    /** Returns true when a newer record from the account replaced something here. */
    private suspend fun push(deviceId: String): Boolean {
        var replaced = false
        repeat(MAX_PUSH_ROUNDS) {
            val batch = q.outboxBatch(PUSH_BATCH).awaitAsList()
            if (batch.isEmpty()) return replaced
            val sentAt = batch.associate { (it.entity to it.entityId) to it.changedAt }
            val changes = batch.map { e ->
                val payload = codecs.read(e.entity, e.entityId)
                SyncChange(e.entity, e.entityId, e.changedAt, deleted = payload == null, payload = payload)
            }
            val response = api.push(PushRequest(deviceId, changes))
            database.transaction {
                val handled = HashSet<Pair<String, String>>()
                response.accepted.forEach { a ->
                    val at = sentAt[a.entity to a.id] ?: return@forEach
                    handled += a.entity to a.id
                    q.metaPut(a.entity, a.id, at, a.version)
                    // A change made while uploading stays queued for the next round.
                    q.outboxRemoveIfUnchanged(a.entity, a.id, at)
                }
                q.syncSetApplying(1)
                response.rejected.forEach { r ->
                    // The account already has a newer version of this record: take it.
                    handled += r.entity to r.id
                    if (applySafely(r)) replaced = true
                    sentAt[r.entity to r.id]?.let { q.outboxRemoveIfUnchanged(r.entity, r.id, it) }
                }
                q.syncSetApplying(0)
                // Nothing came back for these (unknown to the server and not stored): don't resend forever.
                sentAt.filterKeys { it !in handled }.forEach { (key, at) ->
                    q.outboxRemoveIfUnchanged(key.first, key.second, at)
                }
            }
        }
        return replaced
    }

    /** Returns true when anything was applied. */
    private suspend fun pull(deviceId: String): Boolean {
        var applied = false
        var cursor = q.syncState().awaitAsOne().cursor
        do {
            val page = fetchPage(cursor)
            database.transaction {
                q.syncSetApplying(1)
                page.changes.forEach { if (applyIfNewer(it, deviceId)) applied = true }
                q.syncSetApplying(0)
                q.syncSetCursor(page.next)
            }
            cursor = page.next
        } while (page.hasMore)
        return applied
    }

    /** Offline for too long: the server asks for a full download (local unsent changes still win). */
    private suspend fun fetchPage(cursor: Long) = try {
        api.pull(cursor, PULL_PAGE)
    } catch (e: AppError.Rejected) {
        if (e.code != ErrorCodes.CURSOR_EXPIRED || cursor == 0L) throw e
        api.pull(0, PULL_PAGE)
    }

    /** Skips records this device already has (its own uploads) or holds a newer unsent change for. */
    private suspend fun applyIfNewer(r: SyncRecord, deviceId: String): Boolean {
        val local = q.outboxChangedAt(r.entity, r.id).awaitAsOneOrNull()
        if (local != null && localWins(local, deviceId, r)) return false
        val known = q.metaUpdatedAt(r.entity, r.id).awaitAsOneOrNull()
        if (known == r.updatedAt && r.deviceId == deviceId) {
            q.metaPut(r.entity, r.id, r.updatedAt, r.version)
            return false
        }
        val applied = applySafely(r)
        if (applied) q.outboxRemove(r.entity, r.id)
        return applied
    }

    /** Same rule as the server: the newer change wins, ties go to the higher device id. */
    private fun localWins(localChangedAt: Long, deviceId: String, remote: SyncRecord): Boolean =
        localChangedAt > remote.updatedAt || (localChangedAt == remote.updatedAt && deviceId >= remote.deviceId)

    /** One bad or too-new record never blocks the rest of the sync. */
    private suspend fun applySafely(r: SyncRecord): Boolean {
        if (r.schema > PAYLOAD_SCHEMA) return false
        return runCatchingCancellable {
            val payload = r.payload
            if (r.deleted || payload == null) {
                codecs.delete(r.entity, r.id)
            } else {
                codecs.write(r.entity, r.id, payload, r.updatedAt)
            }
            q.metaPut(r.entity, r.id, r.updatedAt, r.version)
        }.onFailure { VxLog.w(TAG, "Skipped ${r.entity} ${r.id}", it) }.isSuccess
    }

    /**
     * Two devices can each start a routine while offline. The most recently changed one stays
     * active; the others end like a switched routine (history kept). The change syncs back, so
     * every device ends with the same active routine.
     */
    private suspend fun keepOneActiveRoutine() {
        val active = q.getActiveTrackers().awaitAsList()
        if (active.size <= 1) return
        // Decided from what the account agrees on (synced change time, then id), so every device keeps the same one.
        val ranked = active.map { (q.metaUpdatedAt("tracker", it.id).awaitAsOneOrNull() ?: 0L) to it.id }
        val keep = ranked.maxWith(compareBy<Pair<Long, String>> { it.first }.thenBy { it.second }).second
        val today = clock.today().toString()
        database.transaction {
            active.filter { it.id != keep }.forEach { t ->
                q.deletePendingOccurrencesForTracker(today, t.id)
                q.archiveHabitsForTracker(today, t.id)
                q.archiveTracker(today, t.id)
            }
        }
    }

    // ------------------------------------------------------------------ setup

    private suspend fun prepare() = run {
        if (!triggersInstalled) {
            SyncTriggers.install(driver)
            triggersInstalled = true
        }
        q.syncStateInit(newDeviceId())
        q.syncState().awaitAsOne()
    }

    private fun newDeviceId() = "dev_" + Uuid.random().toString().take(DEVICE_ID_CHARS)

    private fun watchLocalChanges() {
        if (watcher?.isActive == true) return
        watcher = scope.launch {
            q.outboxCount().asFlow().mapToOne(AppDispatchers.IO).drop(1).collect { if (it > 0) requestSync() }
        }
    }

    private companion object {
        const val TAG = "Sync"
        const val DEBOUNCE_MS = 5_000L
        const val PUSH_BATCH = 200L
        const val PULL_PAGE = 500
        const val MAX_PUSH_ROUNDS = 50
        const val DEVICE_ID_CHARS = 23
    }
}
