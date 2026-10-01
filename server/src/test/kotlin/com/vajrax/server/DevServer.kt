package com.vajrax.server

import com.vajrax.server.config.AppConfig
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import java.io.File

/**
 * Local run without Docker or a database install: PostgreSQL is started from the embedded binaries
 * (data kept in ~/.vajrax-dev/pg) and the real server runs on it. `./gradlew :server:runDev`
 */
fun main() {
    System.setProperty("logback.configurationFile", "logback-dev.xml")
    val dataDir = File(System.getProperty("user.home"), ".vajrax-dev/pg").apply { mkdirs() }
    val postgres = EmbeddedPostgres.builder()
        .setDataDirectory(dataDir)
        .setCleanDataDirectory(false)
        .setPort(DEV_PG_PORT)
        .start()
    val env = System.getenv() + mapOf(
        "DB_URL" to postgres.getJdbcUrl("postgres", "postgres"),
        "DB_USER" to "postgres",
        "DB_PASSWORD" to ""
    )
    Runtime.getRuntime().addShutdownHook(Thread { postgres.close() })
    startServer(AppConfig.fromEnv(env), wait = true)
}

private const val DEV_PG_PORT = 54329
