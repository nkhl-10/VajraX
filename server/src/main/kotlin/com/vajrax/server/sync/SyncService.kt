package com.vajrax.server.sync

import com.vajrax.contract.ContractJson
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.Limits
import com.vajrax.contract.sync.AcceptedChange
import com.vajrax.contract.sync.PullResponse
import com.vajrax.contract.sync.PushResponse
import com.vajrax.contract.sync.SyncChange
import com.vajrax.contract.sync.SyncEntity
import com.vajrax.contract.sync.SyncRecord
import com.vajrax.server.db.Db
import com.vajrax.server.db.query
import com.vajrax.server.db.queryOne
import com.vajrax.server.db.update
import com.vajrax.server.http.ApiException
import com.vajrax.server.http.ValidationException
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject
import java.sql.Connection
import java.sql.ResultSet
import java.time.Clock
import java.time.Duration
import java.util.UUID

/**
 * Offline-first sync, last writer wins per record. The server never interprets payloads; it keeps
 * the newest version of each record (by the client's `updatedAt`, ties broken by device id) and
 * hands out changes in the order they were accepted.
 */
class SyncService(private val db: Db, private val clock: Clock) {

    suspend fun push(userId: UUID, deviceId: String, changes: List<SyncChange>): PushResponse {
        validate(deviceId, changes)
        val now = clock.millis()
        return db.tx { c ->
            // One push per account at a time: versions then grow in commit order, so a pull never skips one.
            c.queryOne("SELECT id FROM users WHERE id = ? FOR UPDATE", userId) { true }
            val accepted = ArrayList<AcceptedChange>()
            val rejected = ArrayList<SyncRecord>()
            changes.forEach { change ->
                // A clock far in the future would win every later edit; cap it close to server time.
                val updatedAt = change.updatedAt.coerceAtMost(now + MAX_CLOCK_AHEAD_MS)
                val version = upsertIfNewer(c, userId, deviceId, change, updatedAt)
                if (version != null) {
                    accepted += AcceptedChange(change.entity, change.id, version)
                } else {
                    current(c, userId, change.entity, change.id)?.let { rejected += it }
                }
            }
            PushResponse(accepted, rejected, now)
        }
    }

    suspend fun pull(userId: UUID, since: Long, limit: Int): PullResponse {
        val size = limit.coerceIn(1, Limits.PULL_MAX_CHANGES)
        return db.read { c ->
            val purged = c.queryOne("SELECT sync_purged_version FROM users WHERE id = ?", userId) { it.getLong(1) } ?: 0L
            if (since in 1 until purged) {
                throw ApiException(
                    HttpStatusCode.Gone,
                    ErrorCodes.CURSOR_EXPIRED,
                    "This device was offline too long. Everything will download again."
                )
            }
            val rows = c.query(
                "SELECT $COLUMNS FROM sync_records WHERE user_id = ? AND server_version > ? ORDER BY server_version LIMIT ?",
                userId,
                since,
                size + 1
            ) { it.toRecord() }
            val page = rows.take(size)
            PullResponse(changes = page, next = page.lastOrNull()?.version ?: since, hasMore = rows.size > size)
        }
    }

    /**
     * Drops deletion markers older than [retention] and remembers, per account, up to which version
     * they are gone ([pull] then asks long-offline devices for a full download instead).
     */
    suspend fun purgeTombstones(retention: Duration = TOMBSTONE_RETENTION): Int = db.tx { c ->
        val cutoff = clock.instant().minus(retention)
        val purged = c.query(
            "DELETE FROM sync_records WHERE deleted AND updated_at < ? RETURNING user_id, server_version",
            cutoff
        ) { it.getObject("user_id", UUID::class.java) to it.getLong("server_version") }
        purged.groupBy({ it.first }, { it.second }).forEach { (user, versions) ->
            c.update(
                "UPDATE users SET sync_purged_version = GREATEST(sync_purged_version, ?) WHERE id = ?",
                versions.max(),
                user
            )
        }
        purged.size
    }

    /** Inserts or replaces the record in one statement, only when the incoming change is newer. */
    private fun upsertIfNewer(
        c: Connection,
        userId: UUID,
        deviceId: String,
        change: SyncChange,
        updatedAt: Long
    ): Long? = c.queryOne(
        """
        INSERT INTO sync_records
            (user_id, entity, entity_id, payload, deleted, schema_version, client_updated_at, device_id, server_version, updated_at)
        VALUES (?, ?, ?, CAST(? AS JSONB), ?, ?, ?, ?, nextval('sync_version_seq'), now())
        ON CONFLICT (user_id, entity, entity_id) DO UPDATE SET
            payload = EXCLUDED.payload,
            deleted = EXCLUDED.deleted,
            schema_version = EXCLUDED.schema_version,
            client_updated_at = EXCLUDED.client_updated_at,
            device_id = EXCLUDED.device_id,
            server_version = EXCLUDED.server_version,
            updated_at = now()
        WHERE (EXCLUDED.client_updated_at, EXCLUDED.device_id) > (sync_records.client_updated_at, sync_records.device_id)
        RETURNING server_version
        """.trimIndent(),
        userId, change.entity, change.id,
        if (change.deleted) null else change.payload?.let { ContractJson.encodeToString(JsonObject.serializer(), it) },
        change.deleted, change.schema, updatedAt, deviceId
    ) { it.getLong(1) }

    private fun current(c: Connection, userId: UUID, entity: String, id: String): SyncRecord? = c.queryOne(
        "SELECT $COLUMNS FROM sync_records WHERE user_id = ? AND entity = ? AND entity_id = ?",
        userId,
        entity,
        id
    ) { it.toRecord() }

    private fun ResultSet.toRecord() = SyncRecord(
        entity = getString("entity"),
        id = getString("entity_id"),
        updatedAt = getLong("client_updated_at"),
        deleted = getBoolean("deleted"),
        schema = getInt("schema_version"),
        payload = getString("payload")?.let { ContractJson.decodeFromString(JsonObject.serializer(), it) },
        deviceId = getString("device_id"),
        version = getLong("server_version")
    )

    private fun validate(deviceId: String, changes: List<SyncChange>) {
        if (changes.size > Limits.PUSH_MAX_CHANGES) {
            throw ApiException(
                HttpStatusCode.PayloadTooLarge,
                ErrorCodes.PAYLOAD_TOO_LARGE,
                "Send at most ${Limits.PUSH_MAX_CHANGES} changes at a time."
            )
        }
        val errors = LinkedHashMap<String, String>()
        if (!ID.matches(deviceId) || deviceId.length > DEVICE_ID_MAX) errors["deviceId"] = "Invalid device id."
        changes.forEachIndexed { i, ch ->
            when {
                ch.entity !in SyncEntity.all -> errors["changes[$i].entity"] = "Unknown record type."
                ch.id.isEmpty() || ch.id.length > Limits.ENTITY_ID_MAX || !ID.matches(ch.id) ->
                    errors["changes[$i].id"] = "Invalid record id."
                ch.updatedAt <= 0 -> errors["changes[$i].updatedAt"] = "Missing change time."
                !ch.deleted && ch.payload == null -> errors["changes[$i].payload"] = "Missing record."
                (ch.payload?.toString()?.toByteArray()?.size ?: 0) > Limits.PAYLOAD_MAX_BYTES ->
                    errors["changes[$i].payload"] = "Record too large."
            }
        }
        if (errors.isNotEmpty()) throw ValidationException(errors, "Some changes couldn't be saved.")
    }

    private companion object {
        const val COLUMNS = "entity, entity_id, payload, deleted, schema_version, client_updated_at, device_id, server_version"
        val ID = Regex("^[A-Za-z0-9_.:|-]+$")
        const val DEVICE_ID_MAX = 64
        const val MAX_CLOCK_AHEAD_MS = 5 * 60_000L
        val TOMBSTONE_RETENTION: Duration = Duration.ofDays(180)
    }
}
