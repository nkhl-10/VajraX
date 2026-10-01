package com.vajrax.server.sync

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.Limits
import com.vajrax.contract.sync.PushRequest
import com.vajrax.server.auth.AUTH_USER
import com.vajrax.server.auth.RATE_SYNC
import com.vajrax.server.auth.userId
import com.vajrax.server.http.ApiException
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.contentLength
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.syncRoutes(sync: SyncService) {
    authenticate(AUTH_USER) {
        rateLimit(RATE_SYNC) {
            post(ApiPaths.SYNC_PUSH) {
                if ((call.request.contentLength() ?: 0) > Limits.REQUEST_MAX_BYTES) {
                    throw ApiException(
                        HttpStatusCode.PayloadTooLarge,
                        ErrorCodes.PAYLOAD_TOO_LARGE,
                        "Too much at once. The app sends it in smaller parts."
                    )
                }
                val body = call.receive<PushRequest>()
                call.respond(sync.push(call.userId, body.deviceId, body.changes))
            }
            get(ApiPaths.SYNC_PULL) {
                val since = call.request.queryParameters["since"]?.toLongOrNull()?.coerceAtLeast(0) ?: 0
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: Limits.PULL_MAX_CHANGES
                call.respond(sync.pull(call.userId, since, limit))
            }
        }
    }
}
