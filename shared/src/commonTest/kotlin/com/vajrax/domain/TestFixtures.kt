@file:OptIn(ExperimentalTime::class)

package com.vajrax.domain

import com.vajrax.core.time.AppClock
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitSchedule
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.model.ActionStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Deterministic clock for tests (spec 08: never depend on the real date). */
class FixedClock(var current: LocalDateTime, private val zone: TimeZone = TimeZone.UTC) : AppClock {
    override fun now(): Instant = current.toInstant(zone)
    override fun timeZone(): TimeZone = zone
}

fun date(s: String): LocalDate = LocalDate.parse(s)

fun habit(
    id: String = "h1",
    schedule: HabitSchedule = HabitSchedule(),
    start: String? = "2026-09-01",
    archived: String? = null,
    time: String = "07:00",
    category: String = "Health"
) = Habit(
    id = id,
    trackerId = "t1",
    title = "Habit $id",
    category = category,
    time = time,
    schedule = schedule,
    startDate = start?.let(::date),
    archivedAt = archived?.let(::date)
)

fun occ(habitId: String, day: String, status: ActionStatus, time: String? = "07:00") =
    Occurrence(Occurrence.idFor(habitId, date(day)), habitId, date(day), time, status)
