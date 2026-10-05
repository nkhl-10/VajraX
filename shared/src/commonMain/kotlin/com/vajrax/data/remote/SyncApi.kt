package com.vajrax.data.remote

import com.vajrax.contract.sync.PullResponse
import com.vajrax.contract.sync.PushRequest
import com.vajrax.contract.sync.PushResponse

/** The server's sync endpoints (see doc/api/sync-protocol.md). */
interface SyncApi {
    suspend fun push(request: PushRequest): PushResponse

    /** Throws [com.vajrax.core.error.AppError.Rejected] with code `sync_cursor_expired` when [since] is too old. */
    suspend fun pull(since: Long, limit: Int): PullResponse
}
