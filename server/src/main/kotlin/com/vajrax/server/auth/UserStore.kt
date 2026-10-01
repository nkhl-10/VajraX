package com.vajrax.server.auth

import com.vajrax.server.db.instant
import com.vajrax.server.db.query
import com.vajrax.server.db.queryOne
import com.vajrax.server.db.update
import com.vajrax.server.db.uuid
import java.sql.Connection
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val displayName: String,
    val role: String,
    val tokenVersion: Int,
    val emailVerifiedAt: Instant?,
    val failedLogins: Int,
    val lockedUntil: Instant?,
    val syncPurgedVersion: Long,
    val createdAt: Instant
) {
    val isAdmin: Boolean get() = role == ROLE_ADMIN

    companion object {
        const val ROLE_ADMIN = "admin"
        fun normalize(email: String): String = email.trim().lowercase()
    }
}

/** Accounts table. Every function runs inside the caller's transaction ([Connection]). */
object UserStore {
    private const val COLUMNS =
        "id, email, password_hash, display_name, role, token_version, email_verified_at, " +
            "failed_logins, locked_until, sync_purged_version, created_at"

    fun insert(c: Connection, id: UUID, email: String, passwordHash: String, displayName: String, now: Instant): User {
        c.update(
            "INSERT INTO users (id, email, email_normalized, password_hash, display_name, " +
                "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
            id,
            email.trim(),
            User.normalize(email),
            passwordHash,
            displayName,
            now,
            now
        )
        return requireNotNull(byId(c, id))
    }

    fun byId(c: Connection, id: UUID): User? = c.queryOne("SELECT $COLUMNS FROM users WHERE id = ?", id) { it.toUser() }

    fun byEmail(c: Connection, email: String): User? =
        c.queryOne("SELECT $COLUMNS FROM users WHERE email_normalized = ?", User.normalize(email)) { it.toUser() }

    fun emailTaken(c: Connection, email: String): Boolean =
        c.query("SELECT 1 FROM users WHERE email_normalized = ?", User.normalize(email)) { true }.isNotEmpty()

    fun setDisplayName(c: Connection, id: UUID, name: String, now: Instant) =
        c.update("UPDATE users SET display_name = ?, updated_at = ? WHERE id = ?", name, now, id)

    /** New password: older access tokens stop working. */
    fun setPassword(c: Connection, id: UUID, hash: String, now: Instant) = c.update(
        "UPDATE users SET password_hash = ?, token_version = token_version + 1, failed_logins = " +
            "0, locked_until = NULL, updated_at = ? WHERE id = ?",
        hash,
        now,
        id
    )

    /** Same password, stronger hash parameters. */
    fun upgradeHash(c: Connection, id: UUID, hash: String) = c.update(
        "UPDATE users SET password_hash = ? WHERE id = ?",
        hash,
        id
    )

    fun bumpTokenVersion(c: Connection, id: UUID, now: Instant) =
        c.update("UPDATE users SET token_version = token_version + 1, updated_at = ? WHERE id = ?", now, id)

    fun recordFailure(c: Connection, id: UUID, lockedUntil: Instant?) =
        c.update("UPDATE users SET failed_logins = failed_logins + 1, locked_until = ? WHERE id = ?", lockedUntil, id)

    fun clearFailures(c: Connection, id: UUID) = c.update(
        "UPDATE users SET failed_logins = 0, locked_until = NULL WHERE id = ?",
        id
    )

    fun setRole(c: Connection, email: String, role: String): Int =
        c.update("UPDATE users SET role = ? WHERE email_normalized = ?", role, User.normalize(email))

    /** Removes the account; sessions, reset tokens and synced records go with it (ON DELETE CASCADE). */
    fun delete(c: Connection, id: UUID) = c.update("DELETE FROM users WHERE id = ?", id)

    private fun ResultSet.toUser() = User(
        id = uuid("id"),
        email = getString("email"),
        passwordHash = getString("password_hash"),
        displayName = getString("display_name"),
        role = getString("role"),
        tokenVersion = getInt("token_version"),
        emailVerifiedAt = instant("email_verified_at"),
        failedLogins = getInt("failed_logins"),
        lockedUntil = instant("locked_until"),
        syncPurgedVersion = getLong("sync_purged_version"),
        createdAt = requireNotNull(instant("created_at"))
    )
}

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val familyId: UUID,
    val deviceName: String?,
    val expiresAt: Instant,
    val revokedAt: Instant?,
    val replacedBy: UUID?
)

/** Refresh tokens, stored as SHA-256 hashes. */
object RefreshTokenStore {
    @Suppress("LongParameterList") // one argument per column
    fun insert(
        c: Connection,
        id: UUID,
        userId: UUID,
        familyId: UUID,
        hash: ByteArray,
        deviceName: String?,
        now: Instant,
        expiresAt: Instant
    ) =
        c.update(
            "INSERT INTO refresh_tokens (id, user_id, family_id, token_hash, device_name, " +
                "created_at, expires_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
            id,
            userId,
            familyId,
            hash,
            deviceName,
            now,
            expiresAt
        )

    /** Locks the row so two refreshes with the same token can't both succeed. */
    fun byHashForUpdate(c: Connection, hash: ByteArray): RefreshToken? = c.queryOne(
        "SELECT id, user_id, family_id, device_name, expires_at, revoked_at, replaced_by FROM " +
            "refresh_tokens WHERE token_hash = ? FOR UPDATE",
        hash
    ) {
        RefreshToken(
            id = it.uuid("id"),
            userId = it.uuid("user_id"),
            familyId = it.uuid("family_id"),
            deviceName = it.getString("device_name"),
            expiresAt = requireNotNull(it.instant("expires_at")),
            revokedAt = it.instant("revoked_at"),
            replacedBy = it.getObject("replaced_by", UUID::class.java)
        )
    }

    fun rotate(c: Connection, oldId: UUID, newId: UUID, now: Instant) =
        c.update("UPDATE refresh_tokens SET revoked_at = ?, replaced_by = ? WHERE id = ?", now, newId, oldId)

    fun revoke(c: Connection, id: UUID, now: Instant) = c.update(
        "UPDATE refresh_tokens SET revoked_at = ? WHERE id = ? AND revoked_at IS NULL",
        now,
        id
    )

    fun revokeFamily(c: Connection, familyId: UUID, now: Instant) =
        c.update("UPDATE refresh_tokens SET revoked_at = ? WHERE family_id = ? AND revoked_at IS NULL", now, familyId)

    fun revokeAll(c: Connection, userId: UUID, now: Instant) =
        c.update("UPDATE refresh_tokens SET revoked_at = ? WHERE user_id = ? AND revoked_at IS NULL", now, userId)

    /** Expired or long-revoked rows are only kept for reuse detection; drop them after a while. */
    fun purge(c: Connection, before: Instant) = c.update("DELETE FROM refresh_tokens WHERE expires_at < ?", before)
}

/** Single-use password reset tokens, stored as hashes. */
object ResetTokenStore {
    fun insert(c: Connection, hash: ByteArray, userId: UUID, now: Instant, expiresAt: Instant) =
        c.update(
            "INSERT INTO password_reset_tokens (token_hash, user_id, created_at, expires_at) VALUES (?, ?, ?, ?)",
            hash,
            userId,
            now,
            expiresAt
        )

    /** Marks the token used and returns its account, or null when unknown, used or expired. */
    fun consume(c: Connection, hash: ByteArray, now: Instant): UUID? {
        val userId = c.queryOne(
            "SELECT user_id FROM password_reset_tokens WHERE token_hash = ? AND used_at IS NULL AND expires_at > ? FOR UPDATE",
            hash, now
        ) { it.uuid("user_id") } ?: return null
        c.update("UPDATE password_reset_tokens SET used_at = ? WHERE token_hash = ?", now, hash)
        return userId
    }

    /** A new reset invalidates the earlier links of the account. */
    fun invalidateFor(c: Connection, userId: UUID, now: Instant) =
        c.update("UPDATE password_reset_tokens SET used_at = ? WHERE user_id = ? AND used_at IS NULL", now, userId)
}

/** Important account events: who (id only), what, when, and a salted hash of the address. */
object AuditLog {
    fun record(
        c: Connection,
        userId: UUID?,
        action: String,
        ipHash: String?,
        now: Instant,
        detailJson: String? = null
    ) = c.update(
        "INSERT INTO audit_log (user_id, action, ip_hash, detail, at) VALUES (?, ?, ?, CAST(? AS JSONB), ?)",
        userId,
        action,
        ipHash,
        detailJson,
        now
    )

    fun actionsFor(c: Connection, userId: UUID): List<String> =
        c.query("SELECT action FROM audit_log WHERE user_id = ? ORDER BY id", userId) { it.getString("action") }
}
