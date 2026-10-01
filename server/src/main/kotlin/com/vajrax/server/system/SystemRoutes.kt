package com.vajrax.server.system

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.VersionInfo
import com.vajrax.server.config.AppConfig
import com.vajrax.server.db.Db
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.coroutines.withTimeoutOrNull
import java.security.MessageDigest
import java.time.Instant

/** Liveness, readiness (database reachable), version for apps, and protected metrics. */
fun Route.systemRoutes(config: AppConfig, db: Db, metrics: PrometheusMeterRegistry?) {
    get("/healthz") { call.respondText("ok") }

    get("/readyz") {
        val ok = withTimeoutOrNull(READY_TIMEOUT_MS) {
            runCatching { db.read { c -> c.createStatement().use { it.execute("SELECT 1") } } }.isSuccess
        } == true
        if (ok) {
            call.respondText("ready")
        } else {
            call.respondText("database unavailable", status = HttpStatusCode.ServiceUnavailable)
        }
    }

    get(ApiPaths.VERSION) {
        call.respond(VersionInfo(config.version, config.gitSha, config.minAppVersion, Instant.now().toString()))
    }

    if (metrics != null && config.metricsToken != null) {
        get("/internal/metrics") {
            val given = call.request.header(HttpHeaders.Authorization)?.removePrefix("Bearer ")?.trim().orEmpty()
            val expected = config.metricsToken
            if (!MessageDigest.isEqual(given.toByteArray(), expected.toByteArray())) {
                call.respondText("", status = HttpStatusCode.NotFound)
            } else {
                call.respondText(metrics.scrape(), ContentType.parse("text/plain; version=0.0.4"))
            }
        }
    }
}

private const val READY_TIMEOUT_MS = 2_000L
