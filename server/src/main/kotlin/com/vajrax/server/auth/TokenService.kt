package com.vajrax.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.vajrax.server.config.AppConfig
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.util.Base64
import java.util.Date
import java.util.UUID

/** Short-lived JWT access tokens and long-lived opaque refresh tokens (stored only as hashes). */
class TokenService(private val config: AppConfig.JwtConfig, private val clock: Clock) {
    private val algorithm = Algorithm.HMAC256(config.secret)
    private val random = SecureRandom()

    // Expiry is checked against the same clock that issued the token.
    val verifier: JWTVerifier = (
        JWT.require(algorithm)
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .acceptLeeway(LEEWAY_SECONDS) as JWTVerifier.BaseVerification
        ).build(clock)

    val accessTtlSeconds: Long get() = config.accessTtl.seconds
    val refreshTtlSeconds: Long get() = config.refreshTtl.seconds

    fun accessToken(userId: UUID, tokenVersion: Int): String {
        val now = clock.instant()
        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId.toString())
            .withClaim(CLAIM_VERSION, tokenVersion)
            .withJWTId(UUID.randomUUID().toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plus(config.accessTtl)))
            .sign(algorithm)
    }

    /** 256 random bits, URL-safe. */
    fun newOpaqueToken(): String = Base64.getUrlEncoder().withoutPadding().encodeToString(
        ByteArray(TOKEN_BYTES).also(random::nextBytes)
    )

    companion object {
        const val CLAIM_VERSION = "ver"
        private const val TOKEN_BYTES = 32
        private const val LEEWAY_SECONDS = 5L

        fun hash(token: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
    }
}
