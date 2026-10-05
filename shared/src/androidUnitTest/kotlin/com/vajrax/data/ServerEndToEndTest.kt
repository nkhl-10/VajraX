package com.vajrax.data

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.vajrax.core.error.AppError
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.remote.ApiClient
import com.vajrax.data.remote.ApiConfig
import com.vajrax.data.remote.MemoryTokenStore
import com.vajrax.data.remote.TemplateLibrarySync
import com.vajrax.data.repository.PracticeRepositoryImpl
import com.vajrax.data.repository.ProfileRepositoryImpl
import com.vajrax.data.repository.SettingsRepositoryImpl
import com.vajrax.data.repository.TemplateRepositoryImpl
import com.vajrax.data.repository.TrackerRepositoryImpl
import com.vajrax.data.sync.SyncEngine
import com.vajrax.domain.FixedClock
import com.vajrax.domain.account.AccountService
import com.vajrax.domain.account.AccountState
import com.vajrax.domain.account.SignInOutcome
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.sync.FirstSync
import com.vajrax.domain.usecase.RoutineManager
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.AfterClass
import org.junit.BeforeClass
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** The app's account and sync code against the real server over HTTP (embedded PostgreSQL, no mocks). */
class ServerEndToEndTest {

    private class Device(port: Int) {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { VajraDatabase.Schema.synchronous().create(it) }
        val db = VajraDatabase(driver)
        val clock = FixedClock(LocalDateTime(LocalDate.parse("2026-09-28"), LocalTime(9, 0)))
        val practices = PracticeRepositoryImpl(db)
        val trackers = TrackerRepositoryImpl(db, practices)
        val templates = TemplateRepositoryImpl(db)
        val settings = SettingsRepositoryImpl(db)
        val profiles = ProfileRepositoryImpl(db)
        val routine = RoutineManager(practices, trackers, templates, settings, profiles, clock)
        val api = ApiClient(ApiConfig("http://127.0.0.1:$port", deviceName = "test"), MemoryTokenStore())
        private val idle = CoroutineScope(Job().apply { cancel() })
        val sync = SyncEngine(db, driver, api, clock, onRemoteChanges = { routine.materialize() }, scope = idle)
        val account = AccountService(api, sync, profiles, clock, CoroutineScope(Job()))

        suspend fun activate() {
            routine.startup()
            val template = assertNotNull(templates.getTemplateWithHabits("morning_discipline"))
            routine.activateTemplate(template, routine.draftHabits(template))
        }
    }

    @Test
    fun registerOnThePhoneRestoreOnTheLaptopAndKeepBothInStep() = runBlocking<Unit> {
        val phone = Device(port)
        phone.activate()
        phone.profiles.saveProfile("Sam", "", "now")
        assertEquals(SignInOutcome.Done, phone.account.register("sam@example.com", "river stone lamp", ""))
        assertIs<AccountState.SignedIn>(phone.account.state.value)
        // The phone's name went to the account.
        assertEquals("Sam", (phone.account.state.value as AccountState.SignedIn).account.displayName)

        val laptop = Device(port)
        laptop.routine.startup()
        assertEquals(SignInOutcome.Done, laptop.account.signIn("SAM@example.com", "river stone lamp"))
        val routine = assertNotNull(laptop.trackers.getActiveTracker())
        assertEquals(phone.trackers.getActiveTracker()!!.id, routine.id)
        assertEquals("Sam", laptop.profiles.getProfile()?.displayName)

        val today = phone.practices.getRange(phone.clock.today(), phone.clock.today()).first()
        Thread.sleep(3)
        phone.routine.complete(today.id)
        phone.sync.syncNow()
        laptop.sync.syncNow()
        assertEquals(ActionStatus.COMPLETE, laptop.practices.getOccurrence(today.id)?.status)
    }

    @Test
    fun aDeviceWithItsOwnDataAsksBeforeMixing() = runBlocking<Unit> {
        val phone = Device(port)
        phone.activate()
        phone.account.register("ana@example.com", "river stone lamp", "Ana")
        val tablet = Device(port)
        tablet.activate()
        assertEquals(SignInOutcome.ChooseFirstSync, tablet.account.signIn("ana@example.com", "river stone lamp"))
        tablet.account.chooseFirstSync(FirstSync.USE_ACCOUNT)
        assertEquals(phone.trackers.getActiveTracker()!!.id, tablet.trackers.getActiveTracker()!!.id)
    }

    @Test
    fun signingOutEverywhereEndsTheOtherDevicesSession() = runBlocking<Unit> {
        val phone = Device(port)
        phone.activate()
        phone.account.register("lee@example.com", "river stone lamp", "Lee")
        val laptop = Device(port)
        laptop.routine.startup()
        laptop.account.signIn("lee@example.com", "river stone lamp")

        laptop.account.signOutEverywhere()
        // The phone's access token is now rejected and its refresh token revoked.
        assertFailsWith<AppError.SignedOut> { phone.api.pull(0, 1) }
        // Its data stays on the device.
        assertNotNull(phone.trackers.getActiveTracker())
    }

    @Test
    fun deletingTheAccountRemovesItsCloudData() = runBlocking<Unit> {
        val phone = Device(port)
        phone.activate()
        phone.account.register("kim@example.com", "river stone lamp", "Kim")
        phone.account.deleteAccount("river stone lamp", removeLocalData = false)
        assertIs<AccountState.SignedOut>(phone.account.state.value)
        val rejected = assertFailsWith<AppError.Rejected> { phone.api.signIn("kim@example.com", "river stone lamp") }
        assertEquals("invalid_credentials", rejected.code)
        assertNotNull(phone.trackers.getActiveTracker(), "kept on the device")
    }

    @Test
    fun theTemplateLibraryComesFromTheServer() = runBlocking<Unit> {
        val phone = Device(port)
        phone.routine.startup()
        TemplateLibrarySync(phone.api, phone.templates).refresh()
        assertEquals("4", phone.templates.remoteLibraryVersion())
        assertTrue(phone.templates.getProvidedTemplates().size >= 10)
        // Asking again with the same version is a cheap "not modified".
        assertEquals(null, phone.api.library("4"))
    }

    companion object {
        private lateinit var postgres: EmbeddedPostgres
        private lateinit var server: Process
        private var port = 0

        @JvmStatic
        @BeforeClass
        fun startServer() {
            postgres = EmbeddedPostgres.builder().start()
            port = java.net.ServerSocket(0).use { it.localPort }
            val jar = File(requireNotNull(System.getProperty("vajrax.serverJar")) { "run through Gradle" })
            server = ProcessBuilder("java", "-jar", jar.absolutePath)
                .redirectErrorStream(true)
                .redirectOutput(File(jar.parentFile, "e2e-server.log"))
                .apply {
                    environment().putAll(
                        mapOf(
                            "APP_ENV" to "development",
                            "PORT" to port.toString(),
                            "DB_URL" to postgres.getJdbcUrl("postgres", "postgres"),
                            "DB_USER" to "postgres",
                            "DB_PASSWORD" to "",
                            "ARGON2_MEMORY_KB" to "1024",
                            "ARGON2_ITERATIONS" to "1",
                            "RATE_LOGIN_PER_MINUTE" to "1000",
                            "RATE_REGISTER_PER_HOUR" to "1000",
                            "MAINTENANCE" to "off"
                        )
                    )
                }
                .start()
            val deadline = System.currentTimeMillis() + STARTUP_TIMEOUT_MS
            while (!healthy()) {
                check(server.isAlive) { "server exited: see ${jar.parentFile}/e2e-server.log" }
                check(System.currentTimeMillis() < deadline) { "server did not start" }
                Thread.sleep(200)
            }
        }

        private fun healthy(): Boolean = runCatching {
            (java.net.URI("http://127.0.0.1:$port/readyz").toURL().openConnection() as java.net.HttpURLConnection).run {
                connectTimeout = 500
                readTimeout = 500
                responseCode == 200
            }
        }.getOrDefault(false)

        @JvmStatic
        @AfterClass
        fun stopServer() {
            server.destroy()
            server.waitFor()
            postgres.close()
        }

        private const val STARTUP_TIMEOUT_MS = 60_000L
    }
}
