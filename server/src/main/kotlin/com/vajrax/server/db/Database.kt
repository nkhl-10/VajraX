package com.vajrax.server.db

import com.vajrax.server.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

/** Connection pool, migrations and a small JDBC helper. Stores keep their SQL next to their logic. */
object Database {
    fun dataSource(config: AppConfig.DbConfig): HikariDataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = config.url
            username = config.user
            password = config.password
            maximumPoolSize = config.maxPoolSize
            minimumIdle = 1
            // Cloud Run instances scale to zero; short lifetimes avoid stale connections.
            maxLifetime = MAX_CONNECTION_LIFETIME_MS
            idleTimeout = IDLE_TIMEOUT_MS
            connectionTimeout = CONNECT_TIMEOUT_MS
            poolName = "vajrax"
            config.instanceConnectionName?.let {
                addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory")
                addDataSourceProperty("cloudSqlInstance", it)
            }
        }
    )

    private const val MAX_CONNECTION_LIFETIME_MS = 10 * 60_000L
    private const val IDLE_TIMEOUT_MS = 5 * 60_000L
    private const val CONNECT_TIMEOUT_MS = 10_000L

    /** Applies db/migration/V*.sql in order; safe to call on every start (one instance migrates at a time). */
    fun migrate(dataSource: DataSource) {
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate()
    }
}

/** Runs blocking JDBC on the IO dispatcher. */
class Db(private val dataSource: DataSource) {
    suspend fun <T> tx(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        dataSource.connection.use { c ->
            c.autoCommit = false
            try {
                block(c).also { c.commit() }
            } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
                // Any failure (including cancellation) rolls back, then propagates unchanged.
                c.rollback()
                throw e
            }
        }
    }

    suspend fun <T> read(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        dataSource.connection.use { c -> block(c) }
    }
}

fun Connection.update(sql: String, vararg params: Any?): Int = prepareStatement(
    sql
).use { it.bind(params).executeUpdate() }

fun <T> Connection.query(sql: String, vararg params: Any?, map: (ResultSet) -> T): List<T> =
    prepareStatement(sql).use { st ->
        st.bind(params).executeQuery().use { rs ->
            val out = ArrayList<T>()
            while (rs.next()) out += map(rs)
            out
        }
    }

fun <T> Connection.queryOne(sql: String, vararg params: Any?, map: (ResultSet) -> T): T? = query(
    sql,
    *params,
    map = map
).firstOrNull()

private fun PreparedStatement.bind(params: Array<out Any?>): PreparedStatement = apply {
    params.forEachIndexed { i, p ->
        val index = i + 1
        when (p) {
            null -> setObject(index, null)
            is Instant -> setTimestamp(index, Timestamp.from(p))
            is UUID -> setObject(index, p)
            else -> setObject(index, p)
        }
    }
}

fun ResultSet.instant(column: String): Instant? = getTimestamp(column)?.toInstant()

fun ResultSet.uuid(column: String): UUID = getObject(column, UUID::class.java)
