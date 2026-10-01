package com.vajrax.server

import com.vajrax.server.auth.User
import com.vajrax.server.auth.UserStore
import com.vajrax.server.config.AppConfig
import com.vajrax.server.db.Database
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/**
 * `java -jar vajrax-server.jar` serves. Admin commands (run as a one-off Cloud Run job or locally):
 *   migrate                  apply database migrations and exit
 *   grant-admin <email>      let an account manage the template library
 */
fun main(args: Array<String>) {
    val config = AppConfig.fromEnv(System.getenv())
    when (args.firstOrNull()) {
        null, "serve" -> startServer(config, wait = true)
        "migrate" -> Database.dataSource(config.db).use { Database.migrate(it) }
        "grant-admin" -> {
            val email = args.getOrNull(1) ?: usage()
            val changed = Database.dataSource(config.db).use { ds ->
                ds.connection.use { c -> UserStore.setRole(c, email, User.ROLE_ADMIN) }
            }
            println(if (changed == 1) "$email is now an admin" else "No account with that email")
        }
        else -> usage()
    }
}

private fun usage(): Nothing {
    System.err.println("usage: vajrax-server [serve | migrate | grant-admin <email>]")
    exitProcess(2)
}

/** Opens the pool, migrates, serves on PORT; Cloud Run's SIGTERM stops it gracefully. */
fun startServer(
    config: AppConfig,
    wait: Boolean
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
    val log = LoggerFactory.getLogger("com.vajrax.server")
    val dataSource = Database.dataSource(config.db)
    if (config.db.migrateOnStart) Database.migrate(dataSource)
    val deps = ServerDeps(config, dataSource)
    runBlocking { deps.prepare() }
    val server = embeddedServer(Netty, port = config.port, host = "0.0.0.0") { vajraModule(deps) }
    Runtime.getRuntime().addShutdownHook(
        Thread {
            log.info("Shutting down")
            server.stop(gracePeriodMillis = 1_000, timeoutMillis = 10_000)
            dataSource.close()
        }
    )
    log.info("VAJRAX server ${config.version} (${config.environment}) on port ${config.port}")
    return server.start(wait = wait)
}
