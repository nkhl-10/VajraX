package com.vajrax.data

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.sync.AcceptedChange
import com.vajrax.contract.sync.PullResponse
import com.vajrax.contract.sync.PushRequest
import com.vajrax.contract.sync.PushResponse
import com.vajrax.contract.sync.SyncRecord
import com.vajrax.core.error.AppError
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.remote.SyncApi
import com.vajrax.data.repository.GoalRepositoryImpl
import com.vajrax.data.repository.PracticeRepositoryImpl
import com.vajrax.data.repository.ProfileRepositoryImpl
import com.vajrax.data.repository.SettingsRepositoryImpl
import com.vajrax.data.repository.TemplateRepositoryImpl
import com.vajrax.data.repository.TrackerRepositoryImpl
import com.vajrax.data.sync.SyncEngine
import com.vajrax.domain.FixedClock
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.repository.Goal
import com.vajrax.domain.repository.GoalTargetType
import com.vajrax.domain.sync.FirstSync
import com.vajrax.domain.sync.SyncStatus
import com.vajrax.domain.usecase.RoutineManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Two devices with their own real databases, syncing through an in-memory server with the server's rules. */
class SyncEngineTest {

    /** Last writer wins per record, versions in acceptance order — the same rules as :server. */
    private class FakeServer : SyncApi {
        val records = LinkedHashMap<Pair<String, String>, SyncRecord>()
        private var version = 0L
        var offline = false
        var purgedBefore = 0L

        override suspend fun push(request: PushRequest): PushResponse {
            if (offline) throw AppError.Offline()
            val accepted = mutableListOf<AcceptedChange>()
            val rejected = mutableListOf<SyncRecord>()
            request.changes.forEach { c ->
                val key = c.entity to c.id
                val current = records[key]
                val newer = current == null || c.updatedAt > current.updatedAt ||
                    (c.updatedAt == current.updatedAt && request.deviceId > current.deviceId)
                if (newer) {
                    version++
                    records[key] = SyncRecord(c.entity, c.id, c.updatedAt, c.deleted, c.schema, c.payload, request.deviceId, version)
                    accepted += AcceptedChange(c.entity, c.id, version)
                } else {
                    rejected += current!!
                }
            }
            return PushResponse(accepted, rejected, 0)
        }

        override suspend fun pull(since: Long, limit: Int): PullResponse {
            if (offline) throw AppError.Offline()
            if (since in 1 until purgedBefore) throw AppError.Rejected(ErrorCodes.CURSOR_EXPIRED, "expired")
            val all = records.values.filter { it.version > since }.sortedBy { it.version }
            val page = all.take(limit)
            return PullResponse(page, page.lastOrNull()?.version ?: since, all.size > limit)
        }
    }

    private class Device(val server: FakeServer) {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { VajraDatabase.Schema.synchronous().create(it) }
        val db = VajraDatabase(driver)
        val clock = FixedClock(LocalDateTime(LocalDate.parse("2026-09-28"), LocalTime(9, 0)))
        val practices = PracticeRepositoryImpl(db)
        val trackers = TrackerRepositoryImpl(db, practices)
        val templates = TemplateRepositoryImpl(db)
        val settings = SettingsRepositoryImpl(db)
        val profiles = ProfileRepositoryImpl(db)
        val goals = GoalRepositoryImpl(db)
        val routine = RoutineManager(practices, trackers, templates, settings, profiles, clock)

        // Background auto-sync is off in tests (a cancelled scope); tests call syncNow().
        val sync = SyncEngine(
            db,
            driver,
            server,
            clock,
            onRemoteChanges = { routine.materialize() },
            scope = CoroutineScope(Job().apply { cancel() })
        )

        suspend fun activate(templateId: String = "morning_discipline") {
            routine.startup()
            val template = assertNotNull(templates.getTemplateWithHabits(templateId))
            routine.activateTemplate(template, routine.draftHabits(template))
        }

        suspend fun outbox() = db.vajraDatabaseQueries.outboxBatch(1000).awaitAsList()
        suspend fun today() = practices.getRange(clock.today(), clock.today())
    }

    /** Trigger timestamps come from SQLite's clock (ms); keep edits on different milliseconds. */
    private fun tick() = Thread.sleep(3)

    @Test
    fun nothingIsQueuedWhileSignedOut() = runTest {
        val phone = Device(FakeServer())
        phone.activate()
        phone.sync.start()
        assertTrue(phone.outbox().isEmpty())
        assertEquals(SyncStatus.Off, phone.sync.status.value)
    }

    @Test
    fun aRoutineAndItsCheckInsReachTheSecondDevice() = runTest {
        val server = FakeServer()
        val phone = Device(server)
        phone.activate()
        val first = phone.today().first()
        phone.routine.complete(first.id)
        phone.sync.enable(FirstSync.MERGE)
        assertTrue(phone.outbox().isEmpty(), "everything uploaded")
        // Planned days stay local: only the check-in travels.
        assertEquals(1, server.records.keys.count { it.first == "occurrence" })

        val laptop = Device(server)
        laptop.routine.startup()
        laptop.sync.enable(FirstSync.MERGE)
        val routine = assertNotNull(laptop.trackers.getActiveTracker())
        assertEquals(phone.trackers.getActiveTracker()!!.id, routine.id)
        assertEquals(
            phone.practices.getTrackerHabits(routine.id).map { it.id },
            laptop.practices.getTrackerHabits(routine.id).map { it.id }
        )
        assertEquals(ActionStatus.COMPLETE, laptop.practices.getOccurrence(first.id)?.status)
        // Downloaded records are not sent back.
        assertTrue(laptop.outbox().isEmpty())
        assertTrue(laptop.sync.status.value is SyncStatus.UpToDate)
    }

    @Test
    fun aDayPlannedOfflineNeverUndoesACheckInFromElsewhere() = runTest {
        val server = FakeServer()
        val phone = Device(server)
        phone.activate()
        phone.sync.enable(FirstSync.MERGE)
        val laptop = Device(server)
        laptop.routine.startup()
        laptop.sync.enable(FirstSync.MERGE)

        val occ = phone.today().first()
        tick()
        phone.routine.complete(occ.id)
        phone.sync.syncNow()
        // The laptop plans the same day again (as it does every morning) before it syncs.
        tick()
        laptop.routine.materialize()
        laptop.sync.syncNow()
        assertEquals(ActionStatus.COMPLETE, laptop.practices.getOccurrence(occ.id)?.status)
        phone.sync.syncNow()
        assertEquals(ActionStatus.COMPLETE, phone.practices.getOccurrence(occ.id)?.status)
    }

    @Test
    fun theLaterEditWinsOnBothDevices() = runTest {
        val server = FakeServer()
        val phone = Device(server)
        phone.activate()
        phone.sync.enable(FirstSync.MERGE)
        val laptop = Device(server)
        laptop.routine.startup()
        laptop.sync.enable(FirstSync.MERGE)

        val habit = phone.practices.getAllTrackedHabits().first()
        tick()
        phone.routine.updateHabit(habit.copy(title = "Phone title"))
        tick()
        laptop.routine.updateHabit(laptop.practices.getHabit(habit.id)!!.copy(title = "Laptop title"))
        phone.sync.syncNow()
        laptop.sync.syncNow()
        phone.sync.syncNow()
        assertEquals("Laptop title", phone.practices.getHabit(habit.id)?.title)
        assertEquals("Laptop title", laptop.practices.getHabit(habit.id)?.title)
    }

    @Test
    fun deletionsTravel() = runTest {
        val server = FakeServer()
        val phone = Device(server)
        phone.activate()
        val habitId = phone.practices.getAllTrackedHabits().first().id
        phone.goals.saveGoal(
            Goal(
                "goal_1",
                "Read more",
                null,
                GoalTargetType.COMPLETIONS,
                10.0,
                LocalDate(2026, 9, 28),
                null,
                listOf(habitId)
            ),
            "now"
        )
        phone.sync.enable(FirstSync.MERGE)
        val laptop = Device(server)
        laptop.routine.startup()
        laptop.sync.enable(FirstSync.MERGE)
        assertEquals(listOf(habitId), laptop.db.vajraDatabaseQueries.getGoalHabitIds("goal_1").awaitAsList())

        tick()
        phone.goals.deleteGoal("goal_1")
        phone.sync.syncNow()
        laptop.sync.syncNow()
        assertEquals(0, laptop.db.vajraDatabaseQueries.getGoals().awaitAsList().size)
    }

    @Test
    fun offlineChangesWaitAndGoUpLater() = runTest {
        val server = FakeServer()
        val phone = Device(server)
        phone.activate()
        phone.sync.enable(FirstSync.MERGE)
        server.offline = true
        tick()
        phone.routine.complete(phone.today().first().id)
        phone.sync.syncNow()
        val waiting = phone.sync.status.value
        assertTrue(waiting is SyncStatus.Waiting && waiting.pendingChanges == 1L, "$waiting")
        server.offline = false
        phone.sync.syncNow()
        assertTrue(phone.outbox().isEmpty())
    }

    @Test
    fun twoRoutinesStartedOfflineEndAsOne() = runTest {
        val server = FakeServer()
        val phone = Device(server)
        phone.activate("morning_discipline")
        phone.sync.enable(FirstSync.MERGE)
        val laptop = Device(server)
        tick()
        laptop.activate("deep_work_block")
        laptop.sync.enable(FirstSync.MERGE)
        phone.sync.syncNow()
        laptop.sync.syncNow()
        val onPhone = phone.db.vajraDatabaseQueries.getActiveTrackers().awaitAsList()
        val onLaptop = laptop.db.vajraDatabaseQueries.getActiveTrackers().awaitAsList()
        assertEquals(1, onPhone.size)
        assertEquals(onPhone.map { it.id }, onLaptop.map { it.id })
    }

    @Test
    fun usingTheAccountReplacesThisDevicesData() = runTest {
        val server = FakeServer()
        val phone = Device(server)
        phone.activate("morning_discipline")
        phone.sync.enable(FirstSync.MERGE)
        val laptop = Device(server)
        laptop.activate("deep_work_block")
        assertTrue(laptop.sync.hasLocalData())
        assertTrue(laptop.sync.accountHasData())
        laptop.sync.enable(FirstSync.USE_ACCOUNT)
        assertEquals(phone.trackers.getActiveTracker()!!.id, laptop.trackers.getActiveTracker()!!.id)
        assertEquals(1, laptop.trackers.getAllTrackers().size)
    }

    @Test
    fun signingOutAndClearingTheDeviceLeavesTheAccountUntouched() = runTest {
        val server = FakeServer()
        val phone = Device(server)
        phone.activate()
        phone.sync.enable(FirstSync.MERGE)
        val before = server.records.size
        phone.sync.disable(removeLocalData = true)
        assertEquals(null, phone.trackers.getActiveTracker())
        phone.sync.syncNow()
        assertEquals(before, server.records.size)
        assertTrue(server.records.values.none { it.deleted })
        assertEquals(0L, phone.db.vajraDatabaseQueries.outboxCount().awaitAsOne())
    }

    @Test
    fun anExpiredCursorDownloadsEverythingAgain() = runTest {
        val server = FakeServer()
        val phone = Device(server)
        phone.activate()
        phone.sync.enable(FirstSync.MERGE)
        val laptop = Device(server)
        laptop.routine.startup()
        laptop.sync.enable(FirstSync.MERGE)
        server.purgedBefore = Long.MAX_VALUE
        laptop.sync.syncNow()
        assertTrue(laptop.sync.status.value is SyncStatus.UpToDate, "${laptop.sync.status.value}")
    }
}
