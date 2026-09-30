package com.vajrax.android.widget

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.TimeFormat
import com.vajrax.core.time.minuteTicks
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.NowPicker
import com.vajrax.domain.model.ActionStatus
import kotlinx.datetime.LocalDate
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.habit.formatValue
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.usecase.RoutineManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import org.koin.core.context.GlobalContext

/** One of today's habits as shown on a home-screen widget. */
data class WidgetRow(
    val habit: Habit,
    val occurrence: Occurrence,
    val isCurrent: Boolean
) {
    val id: String get() = occurrence.id

    fun title(hideNames: Boolean): String = if (hideNames) "${habit.category} habit" else habit.title

    fun time(): String = TimeFormat.display(occurrence.scheduledTime ?: habit.time).ifBlank { "Anytime" }

    fun subtitle(): String {
        val progress = when {
            occurrence.isSkipped -> "Skipped"
            habit.type != HabitType.BOOLEAN ->
                "${formatValue(occurrence.value ?: 0.0)}/${formatValue(habit.targetValue)} ${habit.unit ?: ""}".trimEnd()
            occurrence.isDone -> "Done"
            else -> TimeFormat.duration(habit.durationMinutes)
        }
        return "${time()} · $progress"
    }
}

data class WidgetToday(
    val trackerName: String?,
    val rows: List<WidgetRow>,
    val hideNames: Boolean,
    val nowMinute: Int = 0,
    /** Habit that becomes current once the current one is done (same rule as the app). */
    val nextId: String? = null,
    /** The routine was set to start on a later day. */
    val startsLater: Boolean = false
) {
    private val counted get() = rows.filter { !it.occurrence.isSkipped }
    val done: Int get() = counted.count { it.occurrence.isDone }
    val total: Int get() = counted.size
    val percent: Int get() = if (total > 0) done * 100 / total else 0
    val fraction: Float get() = if (total > 0) done.toFloat() / total else 0f
    val current: WidgetRow? get() = rows.firstOrNull { it.isCurrent }
    val next: WidgetRow? get() = rows.firstOrNull { it.id == nextId }
    val hasTracker: Boolean get() = trackerName != null

    /**
     * Widgets can't scroll to a row, so the list puts what matters first: the current habit,
     * then the rest still to do, then what's already done or skipped.
     */
    val ordered: List<WidgetRow> get() {
        val current = current
        val open = rows.filter { it.occurrence.isOpen && it != current }
        val resolved = rows.filter { !it.occurrence.isOpen }
        return listOfNotNull(current) + open + resolved
    }

    /** "Now", "Next", "Still open" or "Anytime" for [row], from its time window. */
    fun label(row: WidgetRow): String {
        val start = TimeFormat.toMinutes(row.occurrence.scheduledTime ?: row.habit.time) ?: return "Anytime"
        return when {
            nowMinute < start -> "Next"
            nowMinute < start + row.habit.durationMinutes -> "Now"
            else -> "Still open"
        }
    }

    companion object {
        val Empty = WidgetToday(null, emptyList(), false)
    }
}

/**
 * Today's state for widgets, read from the app database through the shared repositories
 * (spec 11: the widget never keeps a second copy of habit state).
 */
@OptIn(ExperimentalCoroutinesApi::class)
object WidgetData {

    /** Live stream: re-emits whenever a check-in, habit or setting changes, and each minute. */
    fun observe(): Flow<WidgetToday> {
        val koin = GlobalContext.getOrNull() ?: return flowOf(WidgetToday.Empty)
        val routine = koin.get<RoutineManager>()
        val practices = koin.get<PracticeRepository>()
        val trackers = koin.get<TrackerRepository>()
        val settings = koin.get<SettingsRepository>()
        val clock = koin.get<AppClock>()
        return clock.minuteTicks()
            .flatMapLatest { (day, minute) ->
                combine(
                    trackers.observeActiveTracker(),
                    practices.observeAllTrackedHabits(),
                    practices.observeDay(day),
                    settings.observe(SettingsRepository.WIDGET_HIDE_NAMES)
                ) { tracker, habits, records, hide ->
                    build(tracker, habits, records, hide == "true", minute, day, clock.nowIso())
                }
            }
            .onStart { runCatching { routine.materialize() } }
            .distinctUntilChanged()
    }

    fun build(
        tracker: Tracker?,
        habits: List<Habit>,
        records: List<Occurrence>,
        hideNames: Boolean,
        now: Int,
        day: LocalDate,
        nowIso: String
    ): WidgetToday {
        if (tracker == null) return WidgetToday.Empty
        val byId = habits.associateBy { it.id }
        val pairs = records
            .mapNotNull { occ -> byId[occ.habitId]?.let { it to occ } }
            .sortedWith(compareBy<Pair<Habit, Occurrence>> { TimeFormat.toMinutes(it.second.scheduledTime ?: it.first.time) ?: Int.MAX_VALUE }.thenBy { it.first.sortOrder })
        val candidates = pairs.map { (h, o) ->
            NowPicker.Candidate(
                id = o.id,
                start = TimeFormat.toMinutes(o.scheduledTime ?: h.time),
                durationMinutes = h.durationMinutes,
                open = o.isOpen,
                snoozed = o.status == ActionStatus.SNOOZED,
                completedAt = o.completedAt?.takeIf { o.isDone }
            )
        }
        val currentId = NowPicker.pick(candidates, now)
        return WidgetToday(
            trackerName = tracker.name,
            rows = pairs.map { (h, o) -> WidgetRow(h, o, isCurrent = currentId == o.id) },
            hideNames = hideNames,
            nowMinute = now,
            nextId = currentId?.let { NowPicker.next(candidates, now, it, nowIso) },
            startsLater = tracker.startDate > day
        )
    }
}
