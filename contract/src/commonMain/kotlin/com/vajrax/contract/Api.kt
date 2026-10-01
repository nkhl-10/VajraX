package com.vajrax.contract

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Paths of the VAJRAX API (all JSON over HTTPS, relative to the server's base URL). */
object ApiPaths {
    const val VERSION = "/v1/version"
    const val REGISTER = "/v1/auth/register"
    const val LOGIN = "/v1/auth/login"
    const val REFRESH = "/v1/auth/refresh"
    const val LOGOUT = "/v1/auth/logout"
    const val LOGOUT_ALL = "/v1/auth/logout-all"
    const val PASSWORD_CHANGE = "/v1/auth/password/change"
    const val PASSWORD_FORGOT = "/v1/auth/password/forgot"
    const val PASSWORD_RESET = "/v1/auth/password/reset"
    const val ME = "/v1/me"
    const val ACCOUNT = "/v1/account"
    const val SYNC_PUSH = "/v1/sync/push"
    const val SYNC_PULL = "/v1/sync/pull"
    const val TEMPLATES = "/v1/templates"

    /** Web clients send this header so the refresh token travels in an HttpOnly cookie instead of the body. */
    const val CLIENT_HEADER = "X-VajraX-Client"
    const val CLIENT_WEB = "web"
    const val REFRESH_COOKIE = "vx_refresh"
}

/** Limits both sides enforce. */
object Limits {
    const val EMAIL_MAX = 254
    const val PASSWORD_MIN = 10
    const val PASSWORD_MAX = 128
    const val DISPLAY_NAME_MAX = 40
    const val ENTITY_ID_MAX = 128
    const val PUSH_MAX_CHANGES = 500
    const val PULL_MAX_CHANGES = 500

    /** Largest single sync payload (one habit, routine, template…), in UTF-8 bytes. */
    const val PAYLOAD_MAX_BYTES = 32 * 1024
    const val REQUEST_MAX_BYTES = 1024 * 1024
}

/** Stable machine-readable error codes in [Problem.code]; clients branch on these, never on text. */
object ErrorCodes {
    const val VALIDATION = "validation_failed"
    const val INVALID_CREDENTIALS = "invalid_credentials"
    const val EMAIL_TAKEN = "email_taken"
    const val WEAK_PASSWORD = "weak_password"
    const val UNAUTHORIZED = "unauthorized"
    const val TOKEN_EXPIRED = "token_expired"
    const val REFRESH_INVALID = "refresh_invalid"
    const val RESET_INVALID = "reset_token_invalid"
    const val FORBIDDEN = "forbidden"
    const val NOT_FOUND = "not_found"
    const val RATE_LIMITED = "rate_limited"
    const val LOCKED = "temporarily_locked"
    const val CURSOR_EXPIRED = "sync_cursor_expired"
    const val PAYLOAD_TOO_LARGE = "payload_too_large"
    const val SERVER_ERROR = "server_error"
}

/** Error body (RFC 9457 problem details) for every 4xx/5xx response. */
@Serializable
data class Problem(
    val type: String = "about:blank",
    val title: String,
    val status: Int,
    val code: String,
    val detail: String? = null,
    /** Field name → message, for [ErrorCodes.VALIDATION]. */
    val errors: Map<String, String> = emptyMap(),
    val requestId: String? = null
)

@Serializable
data class VersionInfo(
    val version: String,
    val gitSha: String,
    /** Apps older than this must update before syncing. */
    val minAppVersion: String,
    val serverTime: String
)

/** JSON settings used on both sides of the API. */
val ContractJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}
