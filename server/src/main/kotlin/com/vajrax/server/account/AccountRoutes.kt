package com.vajrax.server.account

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.auth.DeleteAccountRequest
import com.vajrax.server.auth.AUTH_USER
import com.vajrax.server.auth.AuditLog
import com.vajrax.server.auth.AuthService
import com.vajrax.server.auth.RATE_LOGIN
import com.vajrax.server.auth.UserStore
import com.vajrax.server.auth.clientContext
import com.vajrax.server.auth.userId
import com.vajrax.server.config.AppConfig
import com.vajrax.server.db.Db
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import java.time.Clock

/**
 * Deleting an account removes the user, every session and every synced record in one transaction.
 * The audit trail keeps only the account id and the time (no email, no data).
 */
fun Route.accountRoutes(config: AppConfig, auth: AuthService, db: Db, clock: Clock) {
    authenticate(AUTH_USER) {
        rateLimit(RATE_LOGIN) {
            delete(ApiPaths.ACCOUNT) {
                val userId = call.userId
                auth.verifyPassword(userId, call.receive<DeleteAccountRequest>().password)
                val ctx = call.clientContext(config, null)
                db.tx { c ->
                    UserStore.delete(c, userId)
                    AuditLog.record(c, userId, "account_deleted", ctx.ipHash, clock.instant())
                }
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
