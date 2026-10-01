package com.vajrax.server.auth

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.auth.AuthResponse
import com.vajrax.contract.auth.ChangePasswordRequest
import com.vajrax.contract.auth.ForgotPasswordRequest
import com.vajrax.contract.auth.LoginRequest
import com.vajrax.contract.auth.LogoutRequest
import com.vajrax.contract.auth.RefreshRequest
import com.vajrax.contract.auth.RegisterRequest
import com.vajrax.contract.auth.ResetPasswordRequest
import com.vajrax.contract.auth.UpdateMeRequest
import com.vajrax.server.config.AppConfig
import com.vajrax.server.http.respondProblem
import com.vajrax.server.http.unauthorized
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import java.security.MessageDigest
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/** The signed-in account on an authenticated route. */
data class UserPrincipal(val id: UUID, val isAdmin: Boolean)

const val AUTH_USER = "user"

val RATE_LOGIN = RateLimitName("login")
val RATE_REGISTER = RateLimitName("register")
val RATE_PASSWORD_EMAIL = RateLimitName("password-email")
val RATE_SYNC = RateLimitName("sync")

/** JWT check on every authenticated call, plus per-address rate limits. */
fun Application.installAuth(config: AppConfig, tokens: TokenService, auth: AuthService) {
    install(Authentication) {
        jwt(AUTH_USER) {
            realm = "vajrax"
            verifier(tokens.verifier)
            validate { credential ->
                val id = credential.payload.subject?.let { runCatching { UUID.fromString(it) }.getOrNull() } ?: return@validate null
                val version = credential.payload.getClaim(TokenService.CLAIM_VERSION).asInt() ?: return@validate null
                // Signing out everywhere, a password change or a deleted account end older tokens at once.
                val user = runCatching { auth.me(id) }.getOrNull() ?: return@validate null
                if (user.tokenVersion != version) null else UserPrincipal(user.id, user.isAdmin)
            }
            challenge { _, _ ->
                call.respondProblem(HttpStatusCode.Unauthorized, ErrorCodes.UNAUTHORIZED, "Please sign in again.")
            }
        }
    }
    install(RateLimit) {
        val byAddress: suspend (ApplicationCall) -> Any = { it.request.origin.remoteHost }
        register(RATE_LOGIN) {
            rateLimiter(limit = config.rateLimits.loginPerMinute, refillPeriod = 1.minutes)
            requestKey(byAddress)
        }
        register(RATE_REGISTER) {
            rateLimiter(limit = config.rateLimits.registerPerHour, refillPeriod = 1.hours)
            requestKey(byAddress)
        }
        register(RATE_PASSWORD_EMAIL) {
            rateLimiter(limit = config.rateLimits.passwordEmailPerHour, refillPeriod = 1.hours)
            requestKey(byAddress)
        }
        register(RATE_SYNC) {
            rateLimiter(limit = config.rateLimits.syncPerMinute, refillPeriod = 1.minutes)
            requestKey(byAddress)
        }
    }
}

fun Route.authRoutes(config: AppConfig, auth: AuthService, tokens: TokenService) {
    val cookies = RefreshCookie(config, tokens.refreshTtlSeconds)

    rateLimit(RATE_REGISTER) {
        post(ApiPaths.REGISTER) {
            val body = call.receive<RegisterRequest>()
            val session = auth.register(
                body.email,
                body.password,
                body.displayName,
                call.clientContext(config, body.deviceName)
            )
            call.respondSession(HttpStatusCode.Created, session, auth, tokens, cookies)
        }
    }
    rateLimit(RATE_LOGIN) {
        post(ApiPaths.LOGIN) {
            val body = call.receive<LoginRequest>()
            val session = auth.login(body.email, body.password, call.clientContext(config, body.deviceName))
            call.respondSession(HttpStatusCode.OK, session, auth, tokens, cookies)
        }
        post(ApiPaths.REFRESH) {
            val token = call.refreshTokenFrom(call.receive<RefreshRequest>().refreshToken, cookies)
                ?: throw unauthorized(ErrorCodes.REFRESH_INVALID)
            val session = auth.refresh(token, call.clientContext(config, null))
            call.respondSession(HttpStatusCode.OK, session, auth, tokens, cookies)
        }
        post(ApiPaths.LOGOUT) {
            call.refreshTokenFrom(call.receive<LogoutRequest>().refreshToken, cookies)?.let { auth.logout(it) }
            if (call.isWebClient) cookies.clear(call)
            call.respond(HttpStatusCode.NoContent)
        }
    }
    rateLimit(RATE_PASSWORD_EMAIL) {
        post(ApiPaths.PASSWORD_FORGOT) {
            auth.forgotPassword(call.receive<ForgotPasswordRequest>().email, call.clientContext(config, null))
            call.respond(HttpStatusCode.Accepted)
        }
        post(ApiPaths.PASSWORD_RESET) {
            val body = call.receive<ResetPasswordRequest>()
            auth.resetPassword(body.token, body.newPassword, call.clientContext(config, null))
            call.respond(HttpStatusCode.NoContent)
        }
    }
    authenticate(AUTH_USER) {
        get(ApiPaths.ME) { call.respond(auth.toDto(auth.me(call.userId))) }
        patch(ApiPaths.ME) {
            call.respond(auth.toDto(auth.updateDisplayName(call.userId, call.receive<UpdateMeRequest>().displayName)))
        }
        post(ApiPaths.LOGOUT_ALL) {
            auth.logoutEverywhere(call.userId, call.clientContext(config, null))
            if (call.isWebClient) cookies.clear(call)
            call.respond(HttpStatusCode.NoContent)
        }
        rateLimit(RATE_LOGIN) {
            post(ApiPaths.PASSWORD_CHANGE) {
                val body = call.receive<ChangePasswordRequest>()
                val session = auth.changePassword(
                    call.userId,
                    body.currentPassword,
                    body.newPassword,
                    call.clientContext(config, null)
                )
                call.respondSession(HttpStatusCode.OK, session, auth, tokens, cookies)
            }
        }
    }
}

val ApplicationCall.userId: UUID get() = requireNotNull(principal<UserPrincipal>()).id

val ApplicationCall.isWebClient: Boolean get() = request.header(ApiPaths.CLIENT_HEADER) == ApiPaths.CLIENT_WEB

/** Salted hash of the client address for the audit trail (the address itself is never stored). */
fun ApplicationCall.clientContext(config: AppConfig, deviceName: String?): ClientContext {
    val digest = MessageDigest.getInstance(
        "SHA-256"
    ).digest((config.jwt.secret + request.origin.remoteHost).toByteArray())
    val ipHash = digest.take(IP_HASH_BYTES).joinToString("") { "%02x".format(it) }
    return ClientContext(
        ipHash,
        deviceName?.trim()?.takeIf { it.isNotEmpty() } ?: request.header("User-Agent")?.take(DEVICE_NAME_MAX)
    )
}

private suspend fun ApplicationCall.respondSession(
    status: HttpStatusCode,
    session: Session,
    auth: AuthService,
    tokens: TokenService,
    cookies: RefreshCookie
) {
    // Web clients keep the refresh token in an HttpOnly cookie that page scripts can't read.
    val web = isWebClient
    if (web) cookies.set(this, session.refreshToken)
    respond(
        status,
        AuthResponse(
            user = auth.toDto(session.user),
            accessToken = session.accessToken,
            accessExpiresIn = tokens.accessTtlSeconds,
            refreshToken = if (web) null else session.refreshToken,
            refreshExpiresIn = tokens.refreshTtlSeconds
        )
    )
}

private fun ApplicationCall.refreshTokenFrom(body: String?, cookies: RefreshCookie): String? =
    body?.takeIf { it.isNotBlank() } ?: if (isWebClient) cookies.read(this) else null

/** `HttpOnly; SameSite=Strict; Path=/v1/auth` (+ `Secure` on https), so only the auth endpoints receive it. */
private class RefreshCookie(config: AppConfig, private val maxAgeSeconds: Long) {
    private val secure = config.publicBaseUrl.startsWith("https://")

    fun set(call: ApplicationCall, token: String) = call.response.cookies.append(cookie(token, maxAgeSeconds.toInt()))

    fun clear(call: ApplicationCall) = call.response.cookies.append(cookie("", 0))

    fun read(call: ApplicationCall): String? = call.request.cookies[ApiPaths.REFRESH_COOKIE]?.takeIf { it.isNotBlank() }

    private fun cookie(value: String, maxAge: Int) = Cookie(
        name = ApiPaths.REFRESH_COOKIE,
        value = value,
        maxAge = maxAge,
        path = "/v1/auth",
        secure = secure,
        httpOnly = true,
        extensions = mapOf("SameSite" to "Strict")
    )
}

private const val IP_HASH_BYTES = 8
private const val DEVICE_NAME_MAX = 80
