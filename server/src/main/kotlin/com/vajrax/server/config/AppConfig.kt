package com.vajrax.server.config

import java.time.Duration

/** Everything the server reads from its environment (12-factor). See deploy/.env.example. */
data class AppConfig(
    val environment: Environment,
    val port: Int,
    /** Public origin, e.g. https://vajrax.app — used in emails and cookies. */
    val publicBaseUrl: String,
    val db: DbConfig,
    val jwt: JwtConfig,
    val smtp: SmtpConfig?,
    /** Extra origins allowed to call the API from a browser (our own site and app need none). */
    val corsOrigins: List<String>,
    /** Bearer token for /internal/metrics; metrics are off when null. */
    val metricsToken: String?,
    /** Bearer token for /internal/jobs/maintenance (Cloud Scheduler); the endpoint is off when null. */
    val jobsToken: String?,
    /** Daily housekeeping inside the server process. */
    val maintenanceEnabled: Boolean,
    /** Folder with site/ and app/ (see :server:stageWeb); static files are off when null. */
    val webDir: String?,
    val version: String,
    val gitSha: String,
    val minAppVersion: String,
    val rateLimits: RateLimits = RateLimits(),
    val passwordHashing: PasswordHashing = PasswordHashing()
) {
    val isProduction: Boolean get() = environment == Environment.PRODUCTION

    enum class Environment { DEVELOPMENT, PRODUCTION }

    data class DbConfig(
        val url: String,
        val user: String,
        val password: String,
        /** Cloud SQL instance ("project:region:instance"); when set the Cloud SQL connector is used. */
        val instanceConnectionName: String?,
        val maxPoolSize: Int,
        val migrateOnStart: Boolean
    )

    data class JwtConfig(
        val secret: String,
        val issuer: String,
        val audience: String,
        val accessTtl: Duration,
        val refreshTtl: Duration
    )

    data class SmtpConfig(
        val host: String,
        val port: Int,
        val user: String,
        val password: String,
        val from: String,
        val startTls: Boolean
    )

    /** Per client address, per server instance. */
    data class RateLimits(
        val loginPerMinute: Int = 10,
        val registerPerHour: Int = 5,
        val passwordEmailPerHour: Int = 3,
        val syncPerMinute: Int = 120
    )

    /** Argon2id cost (OWASP baseline: 19 MiB, 2 passes, 1 lane). */
    data class PasswordHashing(val memoryKb: Int = 19_456, val iterations: Int = 2, val parallelism: Int = 1)

    /** Refuses to start with settings that would be unsafe in production. */
    fun validate(): AppConfig = apply {
        require(port in VALID_PORTS) { "PORT must be 1-65535" }
        if (isProduction) {
            require(
                jwt.secret.toByteArray().size >= MIN_SECRET_BYTES
            ) { "JWT_SECRET must be at least $MIN_SECRET_BYTES bytes in production" }
            require(publicBaseUrl.startsWith("https://")) { "PUBLIC_BASE_URL must use https in production" }
            require(db.password.isNotEmpty() || db.instanceConnectionName != null) { "DB_PASSWORD is required" }
        }
    }

    companion object {
        const val MIN_SECRET_BYTES = 32
        private val VALID_PORTS = 1..65535
        private const val DEV_SECRET = "dev-only-secret-never-use-in-production-0123456789"

        /** Defaults are the documented values of deploy/.env.example. */
        @Suppress("MagicNumber", "CyclomaticComplexMethod", "LongMethod")
        fun fromEnv(env: Map<String, String>): AppConfig {
            fun get(key: String): String? = env[key]?.trim()?.takeIf { it.isNotEmpty() }
            val environment = if (get(
                    "APP_ENV"
                ).equals("production", ignoreCase = true)
            ) {
                Environment.PRODUCTION
            } else {
                Environment.DEVELOPMENT
            }
            val dev = environment == Environment.DEVELOPMENT
            val smtpHost = get("SMTP_HOST")
            return AppConfig(
                environment = environment,
                port = get("PORT")?.toInt() ?: 8080,
                publicBaseUrl = (get("PUBLIC_BASE_URL") ?: "http://localhost:8080").trimEnd('/'),
                db = DbConfig(
                    url = get("DB_URL") ?: "jdbc:postgresql://localhost:5432/vajrax",
                    user = get("DB_USER") ?: "vajrax",
                    password = get("DB_PASSWORD") ?: "",
                    instanceConnectionName = get("DB_INSTANCE"),
                    maxPoolSize = get("DB_POOL_SIZE")?.toInt() ?: 5,
                    migrateOnStart = get("DB_MIGRATE")?.toBooleanStrictOrNull() ?: true
                ),
                jwt = JwtConfig(
                    secret = get("JWT_SECRET") ?: if (dev) DEV_SECRET else "",
                    issuer = get("JWT_ISSUER") ?: "vajrax",
                    audience = get("JWT_AUDIENCE") ?: "vajrax-apps",
                    accessTtl = Duration.ofMinutes(get("ACCESS_TOKEN_MINUTES")?.toLong() ?: 15),
                    refreshTtl = Duration.ofDays(get("REFRESH_TOKEN_DAYS")?.toLong() ?: 60)
                ),
                smtp = smtpHost?.let {
                    SmtpConfig(
                        host = it,
                        port = get("SMTP_PORT")?.toInt() ?: 587,
                        user = get("SMTP_USER") ?: "",
                        password = get("SMTP_PASSWORD") ?: "",
                        from = get("SMTP_FROM") ?: "VAJRAX <no-reply@localhost>",
                        startTls = get("SMTP_STARTTLS")?.toBooleanStrictOrNull() ?: true
                    )
                },
                corsOrigins = get("CORS_ORIGINS")?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty(),
                metricsToken = get("METRICS_TOKEN"),
                jobsToken = get("JOBS_TOKEN"),
                maintenanceEnabled = get("MAINTENANCE")?.equals("off", ignoreCase = true) != true,
                webDir = get("WEB_DIR"),
                version = get("APP_VERSION") ?: "1.0.0",
                gitSha = get("GIT_SHA") ?: "dev",
                minAppVersion = get("MIN_APP_VERSION") ?: "1.0",
                rateLimits = RateLimits(
                    loginPerMinute = get("RATE_LOGIN_PER_MINUTE")?.toInt() ?: 10,
                    registerPerHour = get("RATE_REGISTER_PER_HOUR")?.toInt() ?: 5,
                    passwordEmailPerHour = get("RATE_PASSWORD_EMAIL_PER_HOUR")?.toInt() ?: 3,
                    syncPerMinute = get("RATE_SYNC_PER_MINUTE")?.toInt() ?: 120
                ),
                passwordHashing = PasswordHashing(
                    memoryKb = get("ARGON2_MEMORY_KB")?.toInt() ?: 19_456,
                    iterations = get("ARGON2_ITERATIONS")?.toInt() ?: 2
                )
            ).validate()
        }
    }
}
