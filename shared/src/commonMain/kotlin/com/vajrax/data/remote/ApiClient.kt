package com.vajrax.data.remote

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.ContractJson
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.Limits
import com.vajrax.contract.Problem
import com.vajrax.contract.auth.AuthResponse
import com.vajrax.contract.auth.ChangePasswordRequest
import com.vajrax.contract.auth.DeleteAccountRequest
import com.vajrax.contract.auth.ForgotPasswordRequest
import com.vajrax.contract.auth.LoginRequest
import com.vajrax.contract.auth.LogoutRequest
import com.vajrax.contract.auth.RefreshRequest
import com.vajrax.contract.auth.RegisterRequest
import com.vajrax.contract.auth.UpdateMeRequest
import com.vajrax.contract.auth.UserDto
import com.vajrax.contract.sync.PullResponse
import com.vajrax.contract.sync.PushRequest
import com.vajrax.contract.sync.PushResponse
import com.vajrax.contract.templates.TemplateLibrary
import com.vajrax.core.error.AppError
import com.vajrax.domain.account.Account
import com.vajrax.domain.account.AccountGateway
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException

/** Where the API lives; the web app uses its own origin. */
data class ApiConfig(val baseUrl: String, val isWeb: Boolean = false, val deviceName: String? = null)

/** Fetches the public template library; null when the app already has [knownVersion]. */
interface TemplateLibraryApi {
    suspend fun library(knownVersion: String?): TemplateLibrary?
}

/**
 * The VAJRAX API over HTTPS. Keeps the access token in memory and renews it with the refresh
 * token when the server answers 401 — one renewal at a time, shared by every waiting call.
 * Every failure becomes an [AppError] with a sentence for the user.
 */
class ApiClient(
    private val config: ApiConfig,
    private val tokens: TokenStore,
    engine: HttpClientEngine? = null
) : SyncApi, AccountGateway, TemplateLibraryApi {

    private val setup: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = false
        install(ContentNegotiation) { json(ContractJson) }
        install(HttpTimeout) {
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
        }
        defaultRequest {
            url(config.baseUrl.trimEnd('/') + "/")
            if (config.isWeb) header(ApiPaths.CLIENT_HEADER, ApiPaths.CLIENT_WEB)
        }
    }
    private val http: HttpClient = engine?.let { HttpClient(it, setup) } ?: HttpClient(setup)
    private val refreshLock = Mutex()
    private var accessToken: String? = null
    private val _sessionEnded = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val sessionEnded: Flow<Unit> = _sessionEnded.asSharedFlow()

    // ------------------------------------------------------------------ account

    override suspend fun register(email: String, password: String, displayName: String): Account = network {
        val body = RegisterRequest(email.trim(), password, displayName.trim(), config.deviceName)
        startSession(http.post(path(ApiPaths.REGISTER)) { json(body) })
    }

    override suspend fun signIn(email: String, password: String): Account = network {
        val body = LoginRequest(email.trim(), password, config.deviceName)
        startSession(http.post(path(ApiPaths.LOGIN)) { json(body) })
    }

    override suspend fun restore(): Account? {
        if (!tokens.usesCookie && tokens.refreshToken() == null) return null
        return try {
            refresh(failed = null)
            val me = authed { token -> http.get(path(ApiPaths.ME)) { bearerAuth(token) } }.read<UserDto>()
            me.toAccount().also { saveAccount(it) }
        } catch (_: AppError.SignedOut) {
            null
        }
    }

    override suspend fun cachedAccount(): Account? = tokens.cachedAccount()?.let {
        runCatching { ContractJson.decodeFromString(UserDto.serializer(), it).toAccount() }.getOrNull()
    }

    override suspend fun signOut() {
        val token = if (tokens.usesCookie) null else tokens.refreshToken()
        runCatching { network { http.post(path(ApiPaths.LOGOUT)) { json(LogoutRequest(token)) } } }
        endSession(notify = false)
    }

    override suspend fun signOutEverywhere() {
        network { authedPost(ApiPaths.LOGOUT_ALL, body = null).ensureOk() }
        endSession(notify = false)
    }

    override suspend fun updateName(displayName: String): Account = network {
        authedSend(HttpMethod.Patch, ApiPaths.ME, UpdateMeRequest(displayName)).read<UserDto>().toAccount().also { saveAccount(it) }
    }

    override suspend fun changePassword(current: String, new: String) {
        network { startSession(authedPost(ApiPaths.PASSWORD_CHANGE, ChangePasswordRequest(current, new))) }
    }

    override suspend fun requestPasswordReset(email: String) {
        network { http.post(path(ApiPaths.PASSWORD_FORGOT)) { json(ForgotPasswordRequest(email.trim())) }.ensureOk() }
    }

    override suspend fun deleteAccount(password: String) {
        network { authedSend(HttpMethod.Delete, ApiPaths.ACCOUNT, DeleteAccountRequest(password)).ensureOk() }
        endSession(notify = false)
    }

    // ------------------------------------------------------------------ sync + templates

    override suspend fun push(request: PushRequest): PushResponse = network {
        authedPost(ApiPaths.SYNC_PUSH, request).read()
    }

    override suspend fun pull(since: Long, limit: Int): PullResponse = network {
        val size = limit.coerceIn(1, Limits.PULL_MAX_CHANGES)
        authed { token -> http.get(path("${ApiPaths.SYNC_PULL}?since=$since&limit=$size")) { bearerAuth(token) } }.read()
    }

    override suspend fun library(knownVersion: String?): TemplateLibrary? = network {
        val response = http.get(path(ApiPaths.TEMPLATES + (knownVersion?.let { "?since=$it" } ?: "")))
        if (response.status == HttpStatusCode.NotModified) null else response.read<TemplateLibrary>()
    }

    // ------------------------------------------------------------------ session plumbing

    private suspend fun startSession(response: HttpResponse): Account {
        val auth = response.read<AuthResponse>()
        accessToken = auth.accessToken
        val account = auth.user.toAccount()
        tokens.save(auth.refreshToken, ContractJson.encodeToString(UserDto.serializer(), auth.user))
        return account
    }

    private suspend fun saveAccount(account: Account) {
        val dto = UserDto(account.id, account.email, account.displayName, emailVerified = false, createdAt = "")
        tokens.save(
            if (tokens.usesCookie) null else tokens.refreshToken(),
            ContractJson.encodeToString(UserDto.serializer(), dto)
        )
    }

    private suspend fun endSession(notify: Boolean) {
        accessToken = null
        tokens.clear()
        if (notify) _sessionEnded.tryEmit(Unit)
    }

    private suspend fun authedPost(path: String, body: Any?): HttpResponse = authedSend(HttpMethod.Post, path, body)

    private suspend fun authedSend(method: HttpMethod, path: String, body: Any?): HttpResponse = authed { token ->
        http.request(path(path)) {
            this.method = method
            bearerAuth(token)
            body?.let { json(it) }
        }
    }

    /** Calls with the access token; on 401 renews it once and tries again. */
    private suspend fun authed(request: suspend (token: String) -> HttpResponse): HttpResponse {
        val token = accessToken ?: refresh(failed = null)
        val first = request(token)
        if (first.status != HttpStatusCode.Unauthorized) return first
        return request(refresh(failed = token))
    }

    /** Renews the access token, unless another call already did while this one waited. */
    private suspend fun refresh(failed: String?): String = refreshLock.withLock {
        accessToken?.takeIf { it != failed }?.let { return it }
        val stored = if (tokens.usesCookie) null else tokens.refreshToken() ?: throw AppError.SignedOut()
        val response = http.post(path(ApiPaths.REFRESH)) { json(RefreshRequest(stored)) }
        if (response.status == HttpStatusCode.Unauthorized) {
            endSession(notify = true)
            throw AppError.SignedOut()
        }
        val auth = response.read<AuthResponse>()
        accessToken = auth.accessToken
        tokens.save(auth.refreshToken, ContractJson.encodeToString(UserDto.serializer(), auth.user))
        auth.accessToken
    }

    private fun path(p: String) = p.removePrefix("/")

    private fun HttpRequestBuilder.json(body: Any) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    private suspend inline fun <reified T> HttpResponse.read(): T {
        if (!status.isSuccess()) throw problem()
        return try {
            body()
        } catch (e: SerializationException) {
            throw AppError.Unknown(e)
        }
    }

    private suspend fun HttpResponse.ensureOk() {
        if (!status.isSuccess()) throw problem()
    }

    private suspend fun HttpResponse.problem(): AppError {
        val problem = runCatching { body<Problem>() }.getOrNull()
        val code = problem?.code
        return when {
            status.value >= HttpStatusCode.InternalServerError.value -> AppError.Server(status.value)
            status == HttpStatusCode.Unauthorized && code in SESSION_CODES -> AppError.SignedOut()
            problem != null -> AppError.Rejected(problem.code, problem.title, problem.errors, status.value)
            else -> AppError.Rejected(
                "http_${status.value}",
                "Something went wrong. Please try again.",
                status = status.value
            )
        }
    }

    /** Network failures become Offline / Timeout so the app can say "saved on this phone". */
    private suspend fun <T> network(block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: AppError) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        throw AppError.Timeout(e)
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        throw AppError.Offline(e)
    }

    private fun UserDto.toAccount() = Account(id, email, displayName)

    private companion object {
        const val CONNECT_TIMEOUT_MS = 10_000L
        const val REQUEST_TIMEOUT_MS = 30_000L

        /** A 401 with one of these means the session is over (wrong passwords carry their own code). */
        val SESSION_CODES = setOf(null, ErrorCodes.UNAUTHORIZED, ErrorCodes.REFRESH_INVALID)
    }
}
