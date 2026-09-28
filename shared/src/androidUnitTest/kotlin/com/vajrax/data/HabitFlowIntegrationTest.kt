package com.vajrax.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.repository.DataRepositoryImpl
import com.vajrax.data.repository.PracticeRepositoryImpl
import com.vajrax.data.repository.ProfileRepositoryImpl
import com.vajrax.data.repository.SettingsRepositoryImpl
import com.vajrax.data.repository.TemplateRepositoryImpl
import com.vajrax.data.repository.TrackerRepositoryImpl
import com.vajrax.domain.FixedClock
import com.vajrax.domain.habit.HabitSchedule
import com.vajrax.domain.habit.ScheduleType
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.usecase.RoutineManager
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.vajrax.domain.analytics.HabitAnalytics
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.usecase.RoutineException
import kotlin.test.assertFailsWith
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Repository + domain integration against a real SQLite database (spec 08: database tests,
 * historical data preservation, duplicate prevention, migrations).
 */
class HabitFlowIntegrationTest {

    private class Env(day: String) {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { VajraDatabase.Schema.create(it) }
        val db = VajraDatabase(driver)
        val clock = FixedClock(LocalDateTime(LocalDate.parse(day), LocalTime(9, 0)))
        val practices = PracticeRepositoryImpl(db)
        val trackers = TrackerRepositoryImpl(db, practices)
        val templates = TemplateRepositoryImpl(db)
        val settings = SettingsRepositoryImpl(db)
        val profiles = ProfileRepositoryImpl(db)
        val routine = RoutineManager(practices, trackers, templates, settings, profiles, clock)

        fun setDay(day: String) {
            clock.current = LocalDateTime(LocalDate.parse(day), LocalTime(9, 0))
        }
    }

    private suspend fun Env.activate(templateId: String = "morning_discipline") {
        routine.startup()
        val template = assertNotNull(templates.getTemplateWithHabits(templateId))
        routine.activateTemplate(template, routine.draftHabits(template))
    }

    @Test
    fun activationCreatesAPersonalCopyAndLeavesTheTemplateUntouched() = runTest {
        val env = Env("2026-09-28")
        env.activate()
        val tracker = assertNotNull(env.trackers.getActiveTracker())
        val habits = env.practices.getTrackerHabits(tracker.id)
        assertEquals(6, habits.size)
        assertEquals(6, env.practices.getRange(LocalDate.parse("2026-09-28"), LocalDate.parse("2026-09-28")).size)

        env.routine.updateHabit(habits.first().copy(title = "Renamed by me"))
        val template = assertNotNull(env.templates.getTemplateWithHabits("morning_discipline"))
        assertEquals("Wake up", template.habits.first().name)
        assertEquals("Renamed by me", env.practices.getHabit(habits.first().id)?.title)
    }

    @Test
    fun checkInsPersistAndCanBeUndone() = runTest {
        val env = Env("2026-09-28")
        env.activate()
        val occ = env.practices.getRange(LocalDate.parse("2026-09-28"), LocalDate.parse("2026-09-28")).first()
        env.routine.complete(occ.id)
        assertEquals(ActionStatus.COMPLETE, env.practices.getOccurrence(occ.id)?.status)
        env.routine.reopen(occ.id)
        assertEquals(ActionStatus.PENDING, env.practices.getOccurrence(occ.id)?.status)
        env.routine.skip(occ.id, "Travelling")
        val skipped = assertNotNull(env.practices.getOccurrence(occ.id))
        assertEquals(ActionStatus.SKIPPED, skipped.status)
        assertEquals("Travelling", skipped.skipReason)
    }

    @Test
    fun scheduleEditKeepsPastDaysAndOnlyAdjustsToday() = runTest {
        val env = Env("2026-09-28") // Monday
        env.activate()
        val habit = env.practices.getTrackerHabits(env.trackers.getActiveTracker()!!.id).first()
        val monday = env.practices.getOccurrence(habit.id, LocalDate.parse("2026-09-28"))!!
        env.routine.complete(monday.id)

        // App not opened on Tuesday; on Wednesday the habit is changed to weekends only.
        env.setDay("2026-09-30")
        env.routine.updateHabit(habit.copy(schedule = HabitSchedule(ScheduleType.WEEKDAYS, days = setOf(6, 7))))

        assertEquals(ActionStatus.COMPLETE, env.practices.getOccurrence(habit.id, LocalDate.parse("2026-09-28"))?.status)
        // Tuesday was frozen with the old daily schedule before the edit.
        assertNotNull(env.practices.getOccurrence(habit.id, LocalDate.parse("2026-09-29")))
        // Wednesday is no longer scheduled, so its open occurrence is removed.
        assertNull(env.practices.getOccurrence(habit.id, LocalDate.parse("2026-09-30")))
    }

    @Test
    fun materializingTwiceNeverDuplicatesOccurrences() = runTest {
        val env = Env("2026-09-28")
        env.activate()
        env.setDay("2026-10-02")
        env.routine.materialize()
        env.routine.materialize()
        val rows = env.practices.getRange(LocalDate.parse("2026-09-28"), LocalDate.parse("2026-10-02"))
        assertEquals(6 * 5, rows.size)
        assertEquals(rows.size, rows.map { it.habitId to it.date }.toSet().size)
    }

    @Test
    fun archivingKeepsHistoryAndStopsFutureOccurrences() = runTest {
        val env = Env("2026-09-28")
        env.activate()
        val habit = env.practices.getTrackerHabits(env.trackers.getActiveTracker()!!.id).first()
        val today = env.practices.getOccurrence(habit.id, LocalDate.parse("2026-09-28"))!!
        env.routine.complete(today.id)
        env.routine.archiveHabit(habit.id)
        env.setDay("2026-09-29")
        env.routine.materialize()
        assertEquals(ActionStatus.COMPLETE, env.practices.getOccurrence(today.id)?.status)
        assertNull(env.practices.getOccurrence(habit.id, LocalDate.parse("2026-09-29")))
        assertEquals(5, env.practices.getTrackerHabits(env.trackers.getActiveTracker()!!.id).size)
    }

    @Test
    fun switchingTemplatesArchivesTheOldRoutineButKeepsItsHistory() = runTest {
        val env = Env("2026-09-28")
        env.activate("morning_discipline")
        val first = env.practices.getRange(LocalDate.parse("2026-09-28"), LocalDate.parse("2026-09-28")).first()
        env.routine.complete(first.id)
        env.activate("deep_work_block")
        val trackers = env.trackers.getAllTrackers()
        assertEquals(2, trackers.size)
        assertEquals("Deep Work Block", env.trackers.getActiveTracker()?.name)
        val todays = env.practices.getRange(LocalDate.parse("2026-09-28"), LocalDate.parse("2026-09-28"))
        // 4 new open habits + the 1 completed from the old routine; the old routine's open ones are gone.
        assertEquals(5, todays.size)
        assertEquals(ActionStatus.COMPLETE, env.practices.getOccurrence(first.id)?.status)
    }

    @Test
    fun deleteAllDataRemovesPersonalDataButKeepsTheLibrary() = runTest {
        val env = Env("2026-09-28")
        env.activate()
        val data = DataRepositoryImpl(env.db)
        assertTrue(data.exportJson("2026-09-28T09:00:00Z").contains("\"habits\""))
        data.wipePersonalData()
        assertNull(env.trackers.getActiveTracker())
        assertTrue(env.practices.getAllTrackedHabits().isEmpty())
        assertNotNull(env.templates.getTemplateWithHabits("morning_discipline"))
    }

    @Test
    fun migrationFromVersionOneRemovesDemoDataAndAddsNewColumns() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val v1 = javaClass.classLoader!!.getResource("schema_v1.sql")!!.readText()
        v1.lines().filter { it.isNotBlank() }.forEach { driver.execute(null, it, 0) }
        listOf(
            "INSERT INTO UserSessionEntity VALUES ('user_lumina_01','john@example.com','John Doe',0,'morning_discipline','2026-08-30')",
            "INSERT INTO LifePathEntity VALUES ('high_performance','High Performance',NULL,1)",
            "INSERT INTO PracticeEntity VALUES ('prac_1',NULL,'Daily Review',15,5,'06:30 AM','MANUAL',1)",
            "INSERT INTO PracticeEntity VALUES ('old_user_habit',NULL,'Read',15,5,'07:00','MANUAL',1)",
            "INSERT INTO ActionRecordEntity VALUES ('act_1','prac_1','2026-08-30','06:30 AM','COMPLETE',NULL,15)",
            "INSERT INTO TemplateEntity VALUES ('morning_discipline','Morning Discipline','Build',6,'Daily',NULL,0,0,0)",
            "INSERT INTO UserTemplateEntity VALUES ('ut_01','user_lumina_01','morning_discipline',12,30,40,1)"
        ).forEach { driver.execute(null, it, 0) }

        VajraDatabase.Schema.migrate(driver, 1, VajraDatabase.Schema.version)
        val q = VajraDatabase(driver).vajraDatabaseQueries

        assertNull(q.getCurrentUser().executeAsOneOrNull())
        assertNull(q.getActiveTracker().executeAsOneOrNull())
        assertNull(q.getActiveLifePath().executeAsOneOrNull())
        assertNull(q.getHabitById("prac_1").executeAsOneOrNull())
        // A habit created by the v1 flow is kept, but no longer scheduled.
        assertEquals(0L, q.getHabitById("old_user_habit").executeAsOne().isActive)
        q.putSetting("k", "v")
        assertEquals("v", q.getSetting("k").executeAsOne())
        assertEquals("General", q.getTemplateById("morning_discipline").executeAsOne().category)
    }

    // ------------------------------------------------------------ regression tests (code review)

    @Test
    fun staleSnoozeNeverOverwritesACompletion() = runTest {
        val env = Env("2026-09-28")
        env.activate()
        val occ = env.practices.getRange(LocalDate.parse("2026-09-28"), LocalDate.parse("2026-09-28")).first()
        env.routine.complete(occ.id)
        assertFailsWith<RoutineException> { env.routine.snooze(occ.id) }
        assertEquals(ActionStatus.COMPLETE, env.practices.getOccurrence(occ.id)?.status)
    }

    @Test
    fun changingAWeeklyTargetStartsANewVersionAndKeepsPastScores() = runTest {
        val env = Env("2026-09-21") // Monday
        env.activate()
        val tracker = env.trackers.getActiveTracker()!!
        val original = env.practices.getTrackerHabits(tracker.id).first()
        env.routine.updateHabit(original.copy(schedule = HabitSchedule(ScheduleType.WEEKLY_TARGET, weeklyTarget = 2)))
        val weekly = env.practices.getTrackerHabits(tracker.id).first { it.schedule.type == ScheduleType.WEEKLY_TARGET }
        listOf("2026-09-22", "2026-09-24").forEach { d ->
            env.setDay(d)
            env.routine.materialize()
            env.routine.complete(env.practices.getOccurrence(weekly.id, LocalDate.parse(d))!!.id)
        }
        env.setDay("2026-09-30")
        env.routine.updateHabit(weekly.copy(schedule = HabitSchedule(ScheduleType.WEEKLY_TARGET, weeklyTarget = 5)))

        val all = env.practices.getAllTrackedHabits()
        val records = env.practices.getRange(LocalDate.parse("2026-09-21"), LocalDate.parse("2026-09-30"))
        val analytics = HabitAnalytics(all, records, LocalDate.parse("2026-09-30"), LocalDate.parse("2026-09-21")..LocalDate.parse("2026-09-30"))
        val lastWeek = analytics.habitPeriod(weekly.id, LocalDate.parse("2026-09-21"), LocalDate.parse("2026-09-27"))
        assertEquals(2, lastWeek.completed)
        assertEquals(2, lastWeek.eligible) // still scored against the old target of 2
        assertTrue(env.practices.getHabit(weekly.id)!!.isArchived)
        assertEquals(5, env.practices.getTrackerHabits(tracker.id).first { it.schedule.type == ScheduleType.WEEKLY_TARGET }.schedule.weeklyTarget)
    }

    @Test
    fun editingAHabitKeepsTodaysMovedTimeUnlessTheTimeChanged() = runTest {
        val env = Env("2026-09-28")
        env.activate()
        val habit = env.practices.getTrackerHabits(env.trackers.getActiveTracker()!!.id).first()
        val occ = env.practices.getOccurrence(habit.id, LocalDate.parse("2026-09-28"))!!
        env.routine.move(occ.id, "18:00")
        env.routine.updateHabit(habit.copy(color = "rose"))
        assertEquals("18:00", env.practices.getOccurrence(occ.id)?.scheduledTime)
        env.routine.updateHabit(habit.copy(color = "rose", time = "07:45"))
        assertEquals("07:45", env.practices.getOccurrence(occ.id)?.scheduledTime)
    }

    @Test
    fun concurrentIncrementsNeverLoseASteps() = runTest {
        val env = Env("2026-09-28")
        env.routine.startup()
        val template = env.templates.getTemplateWithHabits("fitness_starter_pack")!!
        env.routine.activateTemplate(template, env.routine.draftHabits(template))
        val water = env.practices.getTrackerHabits(env.trackers.getActiveTracker()!!.id).first { it.type == HabitType.COUNT }
        val occ = env.practices.getOccurrence(water.id, LocalDate.parse("2026-09-28"))!!
        coroutineScope { repeat(5) { launch(Dispatchers.IO) { env.routine.increment(occ.id) } } }
        assertEquals(5.0, env.practices.getOccurrence(occ.id)?.value)
    }

    @Test
    fun undoRestoresThePreviousValue() = runTest {
        val env = Env("2026-09-28")
        env.routine.startup()
        val template = env.templates.getTemplateWithHabits("fitness_starter_pack")!!
        env.routine.activateTemplate(template, env.routine.draftHabits(template))
        val water = env.practices.getTrackerHabits(env.trackers.getActiveTracker()!!.id).first { it.type == HabitType.COUNT }
        val id = env.practices.getOccurrence(water.id, LocalDate.parse("2026-09-28"))!!.id
        env.routine.recordValue(id, 5.0)
        val before = env.practices.getOccurrence(id)!!
        env.routine.complete(id)
        env.routine.restore(before)
        val restored = env.practices.getOccurrence(id)!!
        assertEquals(ActionStatus.PENDING, restored.status)
        assertEquals(5.0, restored.value)
    }

    @Test
    fun wakeTimeShiftNeverWrapsPastMidnight() = runTest {
        val env = Env("2026-09-28")
        env.routine.startup()
        val template = env.templates.getTemplateWithHabits("balanced_daily_routine")!!
        fun order(list: List<com.vajrax.domain.habit.Habit>) =
            list.sortedBy { com.vajrax.core.time.TimeFormat.toMinutes(it.time) }.map { it.title }
        // A wrap would move late-evening habits (e.g. Sleep) to the start of the day.
        assertEquals(order(env.routine.draftHabits(template)), order(env.routine.draftHabits(template, wakeTime = "07:30")))
        assertEquals(order(env.routine.draftHabits(template)), order(env.routine.draftHabits(template, wakeTime = "05:00")))
    }
}
