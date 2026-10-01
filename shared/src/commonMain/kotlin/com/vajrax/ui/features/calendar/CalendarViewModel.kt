@file:OptIn(ExperimentalCoroutinesApi::class)

package com.vajrax.ui.features.calendar

import com.vajrax.core.coroutines.AppDispatchers
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.time.AppClock
import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.core.time.minuteTicks
import com.vajrax.domain.analytics.HabitAnalytics
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Calendar history (spec 04 Screen 07): week habit matrix + month heatmap, from persisted logs. */
class CalendarViewModel(
    private val routineManager: RoutineManager,
    private val practices: PracticeRepository,
    private val trackers: TrackerRepository,
    private val clock: AppClock
) : MviViewModel<CalendarUiState, CalendarIntent, CalendarEffect>(CalendarUiState()) {

    private data class Selection(val mode: CalendarMode, val selected: LocalDate?)

    private val selection = MutableStateFlow(Selection(CalendarMode.WEEK, null))

    init {
        val today = clock.minuteTicks().map { it.first }.distinctUntilChanged()
        launchLoad {
            combine(today, selection) { t, s -> t to s.copy(selected = s.selected ?: t) }
                .flatMapLatest { (t, s) ->
                    val sel = s.selected!!
                    val monthStart = Dates.startOfMonth(sel)
                    val from = Dates.startOfWeek(minOf(sel.minus(4, DateTimeUnit.DAY), monthStart))
                    val to = maxOf(sel.plus(4, DateTimeUnit.DAY), Dates.endOfMonth(sel))
                    combine(trackers.observeActiveTracker(), practices.observeAllTrackedHabits(), practices.observeRange(from, to)) { tr, h, r ->
                        build(t, s.mode, sel, tr, h, r, from, to)
                    }
                }
                .flowOn(Dispatchers.Default)
                .whileVisible()
                .collect { built -> updateState { built } }
        }
    }

    private fun build(
        today: LocalDate,
        mode: CalendarMode,
        selected: LocalDate,
        tracker: Tracker?,
        habits: List<Habit>,
        records: List<Occurrence>,
        from: LocalDate,
        to: LocalDate
    ): CalendarUiState {
        val analytics = HabitAnalytics(habits, records, today, from..to)
        val byKey = records.associateBy { "${it.habitId}|${it.date}" }

        // Week matrix: five days centred on the selected date (as in the design).
        val days = Dates.range(selected.minus(2, DateTimeUnit.DAY), selected.plus(2, DateTimeUnit.DAY))
        val activeIds = habits.filter { it.trackerId == tracker?.id && it.archivedAt == null }.map { it.id }.toSet()
        val recordIds = records.filter { it.date in days.first()..days.last() }.map { it.habitId }.toSet()
        val rowHabits = habits.filter { it.id in activeIds || it.id in recordIds }
            .sortedWith(compareBy<Habit> { TimeFormat.toMinutes(it.time) ?: Int.MAX_VALUE }.thenBy { it.sortOrder })
        val rows = rowHabits.map { habit ->
            MatrixRow(
                habit = habit,
                timeLabel = TimeFormat.displayRange(habit.time, habit.durationMinutes),
                cells = days.map { d ->
                    val occ = byKey["${habit.id}|$d"]
                    val kind = when {
                        d > today -> CellKind.FUTURE
                        occ == null -> CellKind.REST
                        occ.isDone -> if (d == today) CellKind.DONE_TODAY else CellKind.DONE_PAST
                        occ.isSkipped -> CellKind.SKIPPED
                        d == today -> CellKind.OPEN_TODAY
                        else -> CellKind.MISSED
                    }
                    MatrixCell(d, occ?.id, kind)
                }
            )
        }
        val weekDays = days.map { d ->
            DayHeader(d, d.day, Dates.shortDayName(d.dayOfWeek), d == today, d == selected)
        }

        // Month heatmap.
        val monthStart = Dates.startOfMonth(selected)
        val monthEnd = Dates.endOfMonth(selected)
        val leading = monthStart.dayOfWeek.isoDayNumber - 1
        val cells = ArrayList<MonthCell>()
        repeat(leading) { cells += MonthCell(null, -1, false, false, false, null) }
        Dates.range(monthStart, monthEnd).forEach { d ->
            val s = analytics.day(d)
            val rate = s.rate
            val level = when {
                d > today -> -1
                s.eligible == 0 -> if (s.pending > 0) 0 else -1
                rate == null -> -1
                rate >= 80 -> 3
                rate >= 50 -> 2
                rate > 0 -> 1
                else -> 0
            }
            cells += MonthCell(d, level, d == today, d == selected, d > today, rate)
        }
        while (cells.size % 7 != 0) cells += MonthCell(null, -1, false, false, false, null)

        val habitsById = habits.associateBy { it.id }
        val dayItems = records.filter { it.date == selected }.mapNotNull { occ ->
            habitsById[occ.habitId]?.let { DayHabit(occ.id, it, occ.status, selected < today, selected == today) }
        }.sortedBy { TimeFormat.toMinutes(it.habit.time) ?: Int.MAX_VALUE }

        return CalendarUiState(
            isLoading = false,
            hasTracker = tracker != null,
            trackerName = tracker?.name ?: "",
            mode = mode,
            today = today,
            selected = selected,
            rangeLabel = "${Dates.shortLabel(days.first())} – ${Dates.shortLabel(days.last())}",
            weekDays = weekDays,
            rows = rows,
            monthLabel = Dates.monthYearLabel(selected),
            monthCells = cells,
            monthStats = analytics.period(monthStart, minOf(monthEnd, today)),
            selectedLabel = "${Dates.fullDayName(selected.dayOfWeek)}, ${Dates.shortLabel(selected)}",
            selectedStats = analytics.day(selected),
            selectedItems = dayItems
        )
    }

    override fun sendIntent(intent: CalendarIntent) {
        when (intent) {
            is CalendarIntent.SetMode -> selection.update { it.copy(mode = intent.mode) }
            is CalendarIntent.Select -> selection.update { it.copy(selected = intent.date) }
            is CalendarIntent.Shift -> selection.update { s ->
                val base = s.selected ?: clock.today()
                s.copy(selected = if (intent.days >= 0) base.plus(intent.days, DateTimeUnit.DAY) else base.minus(-intent.days, DateTimeUnit.DAY))
            }
            is CalendarIntent.ShiftMonth -> selection.update { s ->
                val base = Dates.startOfMonth(s.selected ?: clock.today())
                s.copy(selected = if (intent.months >= 0) base.plus(intent.months, DateTimeUnit.MONTH) else base.minus(-intent.months, DateTimeUnit.MONTH))
            }
            CalendarIntent.GoToday -> selection.update { it.copy(selected = clock.today()) }
            is CalendarIntent.ToggleToday -> write {
                val before = routineManager.toggle(intent.occurrenceId)
                (if (before.isDone || before.isSkipped) "Marked as not done" else "Marked done") to before
            }
            is CalendarIntent.CorrectPast -> write {
                routineManager.correctPastRecord(intent.occurrenceId, intent.done)
                (if (intent.done) "Record corrected: marked done" else "Record corrected: marked not done") to null
            }
            is CalendarIntent.Undo -> write {
                routineManager.restore(intent.previous)
                null
            }
        }
    }

    private fun write(block: suspend () -> Pair<String, com.vajrax.domain.habit.Occurrence?>?) {
        viewModelScope.launch(AppDispatchers.IO) {
            runCatchingCancellable { block() }
                .onSuccess { msg -> msg?.let { sendEffect(CalendarEffect.ShowMessage(it.first, it.second)) } }
                .onFailure { sendEffect(CalendarEffect.ShowMessage(userMessage(it))) }
        }
    }
}
