package com.vajrax.server

import com.vajrax.server.account.accountRoutes
import com.vajrax.server.auth.authRoutes
import com.vajrax.server.auth.installAuth
import com.vajrax.server.http.installHttp
import com.vajrax.server.sync.syncRoutes
import com.vajrax.server.system.maintenanceRoute
import com.vajrax.server.system.scheduleMaintenance
import com.vajrax.server.system.systemRoutes
import com.vajrax.server.templates.templateRoutes
import com.vajrax.server.web.staticRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.routing.routing

/** Wires the HTTP stack and every route. Tests call this with their own [ServerDeps]. */
fun Application.vajraModule(deps: ServerDeps) {
    installHttp(deps.config)
    installAuth(deps.config, deps.tokens, deps.auth)
    deps.metrics?.let { registry -> install(MicrometerMetrics) { this.registry = registry } }
    routing {
        systemRoutes(deps.config, deps.db, deps.metrics)
        maintenanceRoute(deps.maintenance, deps.config.jobsToken)
        authRoutes(deps.config, deps.auth, deps.tokens)
        accountRoutes(deps.config, deps.auth, deps.db, deps.clock)
        syncRoutes(deps.sync)
        templateRoutes(deps.templates)
        deps.config.webDir?.let { staticRoutes(it) }
    }
    if (deps.config.maintenanceEnabled) scheduleMaintenance(deps.maintenance)
}
