@file:OptIn(ExperimentalTime::class)

package com.vajrax.core.time

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Single source of "now" for the app. Business logic never reads the system clock directly,
 * so tests can inject a fixed clock (spec 08: never rely on the current date in logic tests).
 * All calendar dates are local dates in the user's current time zone.
 */
interface AppClock {
    fun now(): Instant
    fun timeZone(): TimeZone

    fun nowLocal(): LocalDateTime = now().toLocalDateTime(timeZone())
    fun today(): LocalDate = nowLocal().date
    fun nowIso(): String = now().toString()

    /** Minutes since local midnight. */
    fun minuteOfDay(): Int = nowLocal().let { it.hour * 60 + it.minute }
}

object SystemAppClock : AppClock {
    override fun now(): Instant = Clock.System.now()
    override fun timeZone(): TimeZone = TimeZone.currentSystemDefault()
}

/** Emits the current minute-of-day roughly every 30 s; used to move the NOW card and detect day rollover. */
fun AppClock.minuteTicks(): Flow<Pair<LocalDate, Int>> = flow {
    while (true) {
        emit(today() to minuteOfDay())
        delay(30_000)
    }
}.distinctUntilChanged()

object Dates {
    fun parse(value: String?): LocalDate? = value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    fun startOfWeek(date: LocalDate): LocalDate =
        date.minus(date.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)

    fun endOfWeek(date: LocalDate): LocalDate = startOfWeek(date).plus(6, DateTimeUnit.DAY)

    fun startOfMonth(date: LocalDate): LocalDate = LocalDate(date.year, date.month, 1)

    fun endOfMonth(date: LocalDate): LocalDate =
        startOfMonth(date).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

    fun daysBetween(from: LocalDate, to: LocalDate): Int = to.toEpochDays().toInt() - from.toEpochDays().toInt()

    fun range(from: LocalDate, to: LocalDate): List<LocalDate> {
        if (to < from) return emptyList()
        val out = ArrayList<LocalDate>(daysBetween(from, to) + 1)
        var d = from
        while (d <= to) {
            out += d
            d = d.plus(1, DateTimeUnit.DAY)
        }
        return out
    }

    fun shortDayName(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        else -> "Sun"
    }

    fun dayLetter(day: DayOfWeek): String = shortDayName(day).take(1)

    fun fullDayName(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "Monday"
        DayOfWeek.TUESDAY -> "Tuesday"
        DayOfWeek.WEDNESDAY -> "Wednesday"
        DayOfWeek.THURSDAY -> "Thursday"
        DayOfWeek.FRIDAY -> "Friday"
        DayOfWeek.SATURDAY -> "Saturday"
        else -> "Sunday"
    }

    private val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun monthName(date: LocalDate): String = monthNames[date.month.ordinal]
    fun shortMonthName(date: LocalDate): String = monthName(date).take(3)

    /** "Wednesday · September 23" */
    fun headerLabel(date: LocalDate): String = "${fullDayName(date.dayOfWeek)} · ${monthName(date)} ${date.day}"

    /** "Jan 14" */
    fun shortLabel(date: LocalDate): String = "${shortMonthName(date)} ${date.day}"

    /** "Sep 2026" / "September 2026" */
    fun monthYearLabel(date: LocalDate): String = "${monthName(date)} ${date.year}"
}
