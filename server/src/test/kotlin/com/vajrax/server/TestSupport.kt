package com.vajrax.server

import com.vajrax.contract.ContractJson
import com.vajrax.server.auth.Mailer
import com.vajrax.server.config.AppConfig
import com.vajrax.server.db.Database
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import javax.sql.DataSource

/** One real PostgreSQL for the whole test run, migrated once and emptied before each test. */
object TestDb {
    private val postgres: EmbeddedPostgres by lazy { EmbeddedPostgres.builder().start() }

    val dataSource: DataSource by lazy { postgres.postgresDatabase.also { Database.migrate(it) } }

    fun reset() {
        dataSource.connection.use {
            it.createStatement().execute(
                "TRUNCATE users, refresh_tokens, password_reset_tokens, sync_records, library_templates, library_meta, audit_log RESTART IDENTITY CASCADE"
            )
        }
    }
}

/** Cheap password hashing and generous rate limits keep tests fast; individual tests tighten them. */
fun testConfig(extra: Map<String, String> = emptyMap()): AppConfig = AppConfig.fromEnv(
    mapOf(
        "APP_ENV" to "development",
        "PUBLIC_BASE_URL" to "http://localhost",
        "ARGON2_MEMORY_KB" to "1024",
        "ARGON2_ITERATIONS" to "1",
        "RATE_LOGIN_PER_MINUTE" to "1000",
        "RATE_REGISTER_PER_HOUR" to "1000",
        "RATE_PASSWORD_EMAIL_PER_HOUR" to "1000",
        "RATE_SYNC_PER_MINUTE" to "1000",
        "MAINTENANCE" to "off"
    ) + extra
)

/** A clock tests can move forward. */
class TestClock(var now: Instant = Instant.parse("2026-10-01T09:00:00Z")) : Clock() {
    override fun instant(): Instant = now
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId?): Clock = this
    fun advance(duration: Duration) { now = now.plus(duration) }
}

/** Keeps sent emails for assertions. */
class RecordingMailer : Mailer {
    data class Mail(val to: String, val subject: String, val body: String)
    val sent = mutableListOf<Mail>()
    override suspend fun send(to: String, subject: String, body: String) { sent += Mail(to, subject, body) }
}

/** Runs [block] against the full application on the test database. */
fun withServer(
    config: AppConfig = testConfig(),
    deps: (AppConfig) -> ServerDeps = { ServerDeps(it, TestDb.dataSource) },
    block: suspend ApplicationTestBuilder.(HttpClient) -> Unit
) {
    TestDb.reset()
    testApplication {
        val serverDeps = deps(config)
        kotlinx.coroutines.runBlocking { serverDeps.prepare() }
        application { vajraModule(serverDeps) }
        val client = createClient { install(ContentNegotiation) { json(ContractJson) } }
        block(client)
    }
}
