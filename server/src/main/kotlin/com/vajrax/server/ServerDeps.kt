package com.vajrax.server

import com.vajrax.server.auth.AuthService
import com.vajrax.server.auth.LogMailer
import com.vajrax.server.auth.Mailer
import com.vajrax.server.auth.PasswordHasher
import com.vajrax.server.auth.SmtpMailer
import com.vajrax.server.auth.TokenService
import com.vajrax.server.config.AppConfig
import com.vajrax.server.db.Db
import com.vajrax.server.sync.SyncService
import com.vajrax.server.system.Maintenance
import com.vajrax.server.templates.TemplateService
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.time.Clock
import javax.sql.DataSource

/** Everything the routes need, built once (constructor injection; no framework). */
class ServerDeps(
    val config: AppConfig,
    dataSource: DataSource,
    val clock: Clock = Clock.systemUTC(),
    val mailer: Mailer = config.smtp?.let { SmtpMailer(it) } ?: LogMailer(showContent = !config.isProduction)
) {
    val db = Db(dataSource)
    val metrics: PrometheusMeterRegistry? =
        config.metricsToken?.let { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
    val hasher = PasswordHasher(config.passwordHashing)
    val tokens = TokenService(config.jwt, clock)
    val auth = AuthService(db, hasher, tokens, mailer, config, clock)
    val sync = SyncService(db, clock)
    val templates = TemplateService(db, clock)
    val maintenance = Maintenance(db, sync, clock)

    /** One-time start-up work after migrations (seeds the template library on an empty database). */
    suspend fun prepare() {
        templates.seedIfEmpty()
    }
}
