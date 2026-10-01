package com.vajrax.server.system

import com.vajrax.server.auth.RefreshTokenStore
import com.vajrax.server.db.Db
import com.vajrax.server.db.update
import com.vajrax.server.sync.SyncService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.time.Clock
import java.time.Duration

/** Housekeeping: old deletion markers, expired sessions and reset links, old audit entries. */
class Maintenance(private val db: Db, private val sync: SyncService, private val clock: Clock) {
    private val log = LoggerFactory.getLogger(Maintenance::class.java)

    suspend fun run(): String {
        val now = clock.instant()
        val tombstones = sync.purgeTombstones()
        val sessions = db.tx { c -> RefreshTokenStore.purge(c, now.minus(Duration.ofDays(SESSION_GRACE_DAYS))) }
        val resets = db.tx { c ->
            c.update(
                "DELETE FROM password_reset_tokens WHERE expires_at < ?",
                now.minus(Duration.ofDays(1))
            )
        }
        val audit = db.tx { c ->
            c.update(
                "DELETE FROM audit_log WHERE at < ?",
                now.minus(Duration.ofDays(AUDIT_DAYS))
            )
        }
        return "tombstones=$tombstones sessions=$sessions resets=$resets audit=$audit".also {
            log.info(
                "Maintenance: {}",
                it
            )
        }
    }

    private companion object {
        const val SESSION_GRACE_DAYS = 30L
        const val AUDIT_DAYS = 400L
    }
}

/**
 * Runs once a day while an instance is up. Cloud Run may pause idle instances, so production also
 * calls POST /internal/jobs/maintenance from Cloud Scheduler (see deploy/DEPLOY.md).
 */
fun Application.scheduleMaintenance(maintenance: Maintenance) {
    launch {
        delay(Duration.ofMinutes(1).toMillis())
        while (true) {
            runCatching { maintenance.run() }.onFailure { environment.log.warn("Maintenance failed", it) }
            delay(Duration.ofDays(1).toMillis())
        }
    }
}

fun Route.maintenanceRoute(maintenance: Maintenance, jobsToken: String?) {
    if (jobsToken == null) return
    post("/internal/jobs/maintenance") {
        val given = call.request.header(HttpHeaders.Authorization)?.removePrefix("Bearer ")?.trim().orEmpty()
        if (!MessageDigest.isEqual(given.toByteArray(), jobsToken.toByteArray())) {
            call.respondText("", status = HttpStatusCode.NotFound)
        } else {
            call.respondText(maintenance.run())
        }
    }
}
