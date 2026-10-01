package com.vajrax.server.auth

import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.Limits
import com.vajrax.contract.auth.UserDto
import com.vajrax.server.config.AppConfig
import com.vajrax.server.db.Db
import com.vajrax.server.db.queryOne
import com.vajrax.server.http.ApiException
import com.vajrax.server.http.ValidationException
import com.vajrax.server.http.unauthorized
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

/** Where a request came from: a salted hash of the address (for the audit trail) and the device name. */
data class ClientContext(val ipHash: String?, val deviceName: String?)

/** A signed-in session: the account plus a fresh access token and refresh token. */
data class Session(val user: User, val accessToken: String, val refreshToken: String)

/**
 * Accounts and sessions: register, sign in, refresh (rotating, with reuse detection), sign out,
 * profile, password change and reset. Passwords are only ever compared as Argon2id hashes.
 */
class AuthService(
    private val db: Db,
    private val hasher: PasswordHasher,
    private val tokens: TokenService,
    private val mailer: Mailer,
    private val config: AppConfig,
    private val clock: Clock
) {
    suspend fun register(email: String, password: String, displayName: String, ctx: ClientContext): Session {
        val cleanEmail = email.trim()
        val name = displayName.trim()
        val errors = buildMap {
            emailProblem(cleanEmail)?.let { put("email", it) }
            PasswordPolicy.problem(password, cleanEmail)?.let { put("password", it) }
            if (name.length > Limits.DISPLAY_NAME_MAX) {
                put(
                    "displayName",
                    "Use at most ${Limits.DISPLAY_NAME_MAX} characters."
                )
            }
        }
        if (errors.isNotEmpty()) throw ValidationException(errors)
        val hash = cpu { hasher.hash(password) }
        return db.tx { c ->
            if (UserStore.emailTaken(c, cleanEmail)) {
                throw ApiException(
                    HttpStatusCode.Conflict,
                    ErrorCodes.EMAIL_TAKEN,
                    "An account with this email already exists. Sign in instead."
                )
            }
            val now = clock.instant()
            val user = UserStore.insert(c, UUID.randomUUID(), cleanEmail, hash, name, now)
            AuditLog.record(c, user.id, "register", ctx.ipHash, now)
            newSession(c, user, ctx.deviceName, UUID.randomUUID(), now)
        }
    }

    suspend fun login(email: String, password: String, ctx: ClientContext): Session {
        val now = clock.instant()
        val user = db.read { c -> UserStore.byEmail(c, email) }
        if (user == null) {
            cpu { hasher.burn(password) }
            throw invalidCredentials()
        }
        user.lockedUntil?.takeIf { it.isAfter(now) }?.let { until ->
            val minutes = Duration.between(now, until).toMinutes().coerceAtLeast(1)
            throw ApiException(
                HttpStatusCode.TooManyRequests,
                ErrorCodes.LOCKED,
                "Too many attempts. Try again in $minutes min."
            )
        }
        val ok = cpu { hasher.verify(password, user.passwordHash) }
        if (!ok) {
            db.tx { c ->
                val failures = user.failedLogins + 1
                UserStore.recordFailure(c, user.id, lockUntil(failures, now))
                AuditLog.record(c, user.id, "login_failed", ctx.ipHash, now)
            }
            throw invalidCredentials()
        }
        val upgraded = if (hasher.needsRehash(user.passwordHash)) cpu { hasher.hash(password) } else null
        return db.tx { c ->
            UserStore.clearFailures(c, user.id)
            upgraded?.let { UserStore.upgradeHash(c, user.id, it) }
            AuditLog.record(c, user.id, "login", ctx.ipHash, now)
            newSession(c, user, ctx.deviceName, UUID.randomUUID(), now)
        }
    }

    /**
     * Swaps a refresh token for a new pair. Presenting an already-rotated token means it leaked
     * (or two copies exist): the whole sign-in family is revoked, except within a short grace
     * window where a duplicate request from the same app is the likely cause.
     */
    suspend fun refresh(refreshToken: String, ctx: ClientContext): Session {
        // The revocation of a reused family must be committed, so the failure is raised after the transaction.
        val session = db.tx { c ->
            val now = clock.instant()
            val stored = RefreshTokenStore.byHashForUpdate(c, TokenService.hash(refreshToken)) ?: return@tx null
            if (stored.revokedAt != null) {
                val rotatedJustNow = stored.replacedBy != null && Duration.between(stored.revokedAt, now) < REUSE_GRACE
                if (!rotatedJustNow) {
                    RefreshTokenStore.revokeFamily(c, stored.familyId, now)
                    AuditLog.record(c, stored.userId, "refresh_reuse", ctx.ipHash, now)
                }
                return@tx null
            }
            if (!stored.expiresAt.isAfter(now)) return@tx null
            val user = UserStore.byId(c, stored.userId) ?: return@tx null
            val newId = UUID.randomUUID()
            newSession(c, user, stored.deviceName ?: ctx.deviceName, stored.familyId, now, tokenId = newId)
                .also { RefreshTokenStore.rotate(c, stored.id, newId, now) }
        }
        return session ?: throw unauthorized(ErrorCodes.REFRESH_INVALID)
    }

    suspend fun logout(refreshToken: String) {
        db.tx { c ->
            val now = clock.instant()
            RefreshTokenStore.byHashForUpdate(
                c,
                TokenService.hash(refreshToken)
            )?.let { RefreshTokenStore.revoke(c, it.id, now) }
        }
    }

    /** Ends every session of the account (all devices and browsers). */
    suspend fun logoutEverywhere(userId: UUID, ctx: ClientContext) {
        db.tx { c ->
            val now = clock.instant()
            UserStore.bumpTokenVersion(c, userId, now)
            RefreshTokenStore.revokeAll(c, userId, now)
            AuditLog.record(c, userId, "logout_all", ctx.ipHash, now)
        }
    }

    suspend fun me(userId: UUID): User = db.read { c -> UserStore.byId(c, userId) } ?: throw unauthorized()

    suspend fun updateDisplayName(userId: UUID, displayName: String): User {
        val name = displayName.trim()
        if (name.length > Limits.DISPLAY_NAME_MAX) {
            throw ValidationException(
                mapOf("displayName" to "Use at most ${Limits.DISPLAY_NAME_MAX} characters.")
            )
        }
        return db.tx { c ->
            UserStore.setDisplayName(c, userId, name, clock.instant())
            UserStore.byId(c, userId) ?: throw unauthorized()
        }
    }

    /** Signs out every other device; this one gets a new session. */
    suspend fun changePassword(userId: UUID, current: String, new: String, ctx: ClientContext): Session {
        val user = verifyPassword(userId, current)
        PasswordPolicy.problem(new, user.email)?.let { throw ValidationException(mapOf("newPassword" to it)) }
        val hash = cpu { hasher.hash(new) }
        return db.tx { c ->
            val now = clock.instant()
            UserStore.setPassword(c, userId, hash, now)
            RefreshTokenStore.revokeAll(c, userId, now)
            AuditLog.record(c, userId, "password_changed", ctx.ipHash, now)
            newSession(c, requireNotNull(UserStore.byId(c, userId)), ctx.deviceName, UUID.randomUUID(), now)
        }
    }

    /** Always succeeds from the caller's view, so it never reveals whether an email has an account. */
    suspend fun forgotPassword(email: String, ctx: ClientContext) {
        val token = tokens.newOpaqueToken()
        val user = db.tx { c ->
            val user = UserStore.byEmail(c, email) ?: return@tx null
            val now = clock.instant()
            ResetTokenStore.invalidateFor(c, user.id, now)
            ResetTokenStore.insert(c, TokenService.hash(token), user.id, now, now.plus(RESET_TTL))
            AuditLog.record(c, user.id, "password_reset_requested", ctx.ipHash, now)
            user
        } ?: return
        val link = "${config.publicBaseUrl}/reset-password?token=$token"
        mailer.send(
            to = user.email,
            subject = "Reset your VAJRAX password",
            body = """
                |Someone asked to reset the password of your VAJRAX account.
                |
                |Choose a new password here (the link works once, for 30 minutes):
                |$link
                |
                |If it wasn't you, ignore this email — your password stays the same.
            """.trimMargin()
        )
    }

    suspend fun resetPassword(token: String, newPassword: String, ctx: ClientContext) {
        val now = clock.instant()
        val userId = db.read { c -> peekResetUser(c, token, now) } ?: throw resetInvalid()
        val user = me(userId)
        PasswordPolicy.problem(newPassword, user.email)?.let { throw ValidationException(mapOf("newPassword" to it)) }
        val hash = cpu { hasher.hash(newPassword) }
        db.tx { c ->
            val consumed = ResetTokenStore.consume(c, TokenService.hash(token), now) ?: throw resetInvalid()
            UserStore.setPassword(c, consumed, hash, now)
            RefreshTokenStore.revokeAll(c, consumed, now)
            AuditLog.record(c, consumed, "password_reset", ctx.ipHash, now)
        }
    }

    /** Re-checks the password before sensitive actions (change password, delete account). */
    suspend fun verifyPassword(userId: UUID, password: String): User {
        val user = me(userId)
        if (!cpu { hasher.verify(password, user.passwordHash) }) {
            throw ApiException(HttpStatusCode.Forbidden, ErrorCodes.INVALID_CREDENTIALS, "That password isn't right.")
        }
        return user
    }

    fun toDto(user: User) = UserDto(
        id = user.id.toString(),
        email = user.email,
        displayName = user.displayName,
        emailVerified = user.emailVerifiedAt != null,
        createdAt = user.createdAt.toString()
    )

    private fun newSession(
        c: Connection,
        user: User,
        deviceName: String?,
        familyId: UUID,
        now: Instant,
        tokenId: UUID = UUID.randomUUID()
    ): Session {
        val refresh = tokens.newOpaqueToken()
        RefreshTokenStore.insert(
            c,
            tokenId,
            user.id,
            familyId,
            TokenService.hash(refresh),
            deviceName?.take(DEVICE_NAME_MAX),
            now,
            now.plusSeconds(tokens.refreshTtlSeconds)
        )
        return Session(user, tokens.accessToken(user.id, user.tokenVersion), refresh)
    }

    /** Checks a reset token without using it up (the password is validated first). */
    private fun peekResetUser(c: Connection, token: String, now: Instant): UUID? = c.queryOne(
        "SELECT user_id FROM password_reset_tokens WHERE token_hash = ? AND used_at IS NULL AND expires_at > ?",
        TokenService.hash(token),
        now
    ) { it.getObject("user_id", UUID::class.java) }

    private fun lockUntil(failures: Int, now: Instant): Instant? {
        if (failures < LOCK_AFTER) return null
        // 1, 2, 4, 8, then 15 minutes.
        val minutes = (1L shl (failures - LOCK_AFTER).coerceAtMost(MAX_LOCK_DOUBLINGS)).coerceAtMost(MAX_LOCK_MINUTES)
        return now.plus(Duration.ofMinutes(minutes))
    }

    private fun emailProblem(email: String): String? = when {
        email.isEmpty() -> "Enter your email."
        email.length > Limits.EMAIL_MAX || !EMAIL.matches(email) -> "Enter a valid email address."
        else -> null
    }

    private fun invalidCredentials() =
        ApiException(HttpStatusCode.Unauthorized, ErrorCodes.INVALID_CREDENTIALS, "Email or password is incorrect.")

    private fun resetInvalid() =
        ApiException(
            HttpStatusCode.BadRequest,
            ErrorCodes.RESET_INVALID,
            "This reset link has expired or was already used. Ask for a new one."
        )

    /** Argon2 is CPU-bound: keep it off the IO threads. */
    private suspend fun <T> cpu(block: () -> T): T = withContext(Dispatchers.Default) { block() }

    private companion object {
        val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        val RESET_TTL: Duration = Duration.ofMinutes(30)
        val REUSE_GRACE: Duration = Duration.ofSeconds(30)
        const val LOCK_AFTER = 5
        const val MAX_LOCK_MINUTES = 15L
        const val MAX_LOCK_DOUBLINGS = 4
        const val DEVICE_NAME_MAX = 80
    }
}
