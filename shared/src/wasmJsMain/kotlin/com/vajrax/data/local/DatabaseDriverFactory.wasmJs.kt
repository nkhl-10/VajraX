package com.vajrax.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

/**
 * The web database is SQLite running in a web worker. Its schema has to be created
 * asynchronously, so the web entry point calls [createWebDatabaseDriver] first and hands the
 * ready driver to this factory.
 */
actual class DatabaseDriverFactory(private val driver: SqlDriver) {
    actual fun createDriver(): SqlDriver = driver
}

/** Starts the SQLite worker and creates the schema. */
suspend fun createWebDatabaseDriver(): SqlDriver {
    val driver = WebWorkerDriver(sqliteWorker())
    VajraDatabase.Schema.create(driver).await()
    return driver
}

private fun sqliteWorker(): Worker =
    js("""new Worker(new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")
