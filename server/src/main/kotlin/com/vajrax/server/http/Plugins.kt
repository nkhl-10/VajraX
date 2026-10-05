package com.vajrax.server.http

import com.vajrax.contract.ContractJson
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.Problem
import com.vajrax.server.config.AppConfig
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import org.slf4j.event.Level
import java.io.File
import java.util.UUID

/** Plugins every response goes through: JSON, errors, request ids, logs, security headers, gzip. */
fun Application.installHttp(config: AppConfig) {
    install(XForwardedHeaders) {
        // Cloud Run's front end appends the real client address last; earlier entries can be forged.
        useLastProxy()
    }
    install(ContentNegotiation) { json(ContractJson) }
    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        generate { UUID.randomUUID().toString() }
        verify { it.length in REQUEST_ID_LENGTH }
        replyToHeader(HttpHeaders.XRequestId)
    }
    install(CallLogging) {
        level = Level.INFO
        callIdMdc("requestId")
        // Health probes would drown the log.
        filter { !it.request.path().startsWith("/healthz") && !it.request.path().startsWith("/readyz") }
        // Method, path and status only: never query strings, bodies or headers (tokens, emails).
        format { call -> "${call.request.httpMethod.value} ${call.request.path()} ${call.response.status()?.value}" }
    }
    install(DefaultHeaders) {
        header("X-Content-Type-Options", "nosniff")
        header("Referrer-Policy", "strict-origin-when-cross-origin")
        header("X-Frame-Options", "DENY")
        header("Permissions-Policy", "camera=(), microphone=(), geolocation=()")
        header("Cross-Origin-Opener-Policy", "same-origin")
        if (config.isProduction) header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
    }
    install(Compression) {
        gzip { minimumSize(MIN_COMPRESSED_BYTES) }
    }
    install(ConditionalHeaders)
    if (config.corsOrigins.isNotEmpty()) {
        install(CORS) {
            config.corsOrigins.forEach { origin ->
                allowHost(origin.substringAfter("://"), schemes = listOf(origin.substringBefore("://")))
            }
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowCredentials = true
        }
    }
    install(StatusPages) {
        exception<ApiException> { call, e -> call.respondProblem(e.status, e.code, e.title, e.detail, e.errors) }
        exception<BadRequestException> { call, _ ->
            call.respondProblem(HttpStatusCode.BadRequest, ErrorCodes.VALIDATION, "That request couldn't be read.")
        }
        exception<Throwable> { call, e ->
            call.application.environment.log.error("Unhandled error on ${call.request.path()}", e)
            call.respondProblem(
                HttpStatusCode.InternalServerError,
                ErrorCodes.SERVER_ERROR,
                "Something went wrong on our side. Please try again."
            )
        }
        status(HttpStatusCode.NotFound) { call, status ->
            val page = config.webDir?.let { File(it, "site/404.html") }?.takeIf { it.isFile }
            when {
                call.request.path().startsWith("/v1/") -> call.respondProblem(status, ErrorCodes.NOT_FOUND, "Not found.")
                // A mistyped website address gets the friendly page, still with a 404 status.
                page != null -> call.respondText(page.readText(), ContentType.Text.Html, status)
            }
        }
        status(HttpStatusCode.TooManyRequests) { call, status ->
            call.respondProblem(
                status,
                ErrorCodes.RATE_LIMITED,
                "Too many attempts. Please wait a minute and try again."
            )
        }
    }
}

suspend fun ApplicationCall.respondProblem(
    status: HttpStatusCode,
    code: String,
    title: String,
    detail: String? = null,
    errors: Map<String, String> = emptyMap()
) = respond(
    status,
    Problem(title = title, status = status.value, code = code, detail = detail, errors = errors, requestId = callId)
)

private val REQUEST_ID_LENGTH = 8..128
private const val MIN_COMPRESSED_BYTES = 1024L
