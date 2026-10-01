package com.vajrax.android.widget

import android.content.Context
import com.vajrax.android.R
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.time.AppClock
import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.core.time.minuteTicks
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.habit.formatValue
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.today.DayPhase
import com.vajrax.domain.today.TodayPlanner
import com.vajrax.domain.usecase.RoutineManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.LocalDate
import org.koin.core.context.GlobalContext

/** One of today's habits as shown on a home-screen widget. */
data class WidgetRow(
    val habit: Habit,
    val occurrence: Occurrence,
    val phase: DayPhase
) {
    val id: String get() = occurrence.id
    val isCurrent: Boolean get() = phase == DayPhase.NOW

    fun title(context: Context, hideNames: Boolean): String =
        if (hideNames) context.getString(R.string.widget_habit_generic, habit.category) else habit.title

    fun time(context: Context): String =
        TimeFormat.display(occurrence.scheduledTime ?: habit.time).ifBlank { context.getString(R.string.widget_anytime) }

    fun subtitle(context: Context): String {
        val progress = when {
            occurrence.isSkipped -> context.getString(R.string.widget_skipped)
            phase == DayPhase.WEEKLY_MET -> context.getString(R.string.widget_weekly_met)
            habit.type != HabitType.BOOLEAN ->
                "${formatValue(occurrence.value ?: 0.0)} / ${formatValue(habit.targetValue)} ${habit.unit ?: ""}".trimEnd()
            occurrence.isDone -> context.getString(R.string.widget_done)
            else -> TimeFormat.duration(habit.durationMinutes)
        }
        return "${time(context)} · $progress"
    }
}

data class WidgetToday(
    val trackerName: String?,
    val rows: List<WidgetRow>,
    val hideNames: Boolean,
    /** Same counts as Home (skipped habits and weekly targets already met don't count). */
    val done: Int = 0,
    val total: Int = 0,
    /** Habit that becomes current once the current one is done (same rule as the app). */
    val nextId: String? = null,
    /** The routine was set to start on a later day. */
    val startsLater: Boolean = false
) {
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

    /** "Now", "Next", "Still open" or "Anytime" for [row], from the shared day phases. */
    fun label(context: Context, row: WidgetRow): String = context.getString(
        when {
            row.phase == DayPhase.NOW -> R.string.widget_label_now
            TimeFormat.toMinutes(row.occurrence.scheduledTime ?: row.habit.time) == null -> R.string.widget_anytime
            row.phase == DayPhase.OPEN_EARLIER -> R.string.widget_label_still_open
            row.phase == DayPhase.WEEKLY_MET -> R.string.widget_label_week_done
            else -> R.string.widget_label_next
        }
    )

    companion object {
        val Empty = WidgetToday(null, emptyList(), false)
    }
}

/**
 * Today's state for widgets, read from the app database through the shared repositories
 * (spec 11: the widget never keeps a second copy of habit state) and planned by the same
 * [TodayPlanner] as Home.
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
                    // The week so far: weekly targets need it to know when they are met.
                    practices.observeRange(Dates.startOfWeek(day), day),
                    settings.observe(SettingsRepository.WIDGET_HIDE_NAMES)
                ) { tracker, habits, records, hide ->
                    build(tracker, habits, records, hide == "true", minute, day, clock.nowIso())
                }
            }
            .onStart { runCatchingCancellable { routine.materialize() }.onFailure { com.vajrax.core.log.VxLog.w("Widget", "Couldn't plan today", it) } }
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
        val plan = TodayPlanner.plan(day, now, tracker, habits, records, reflectionDone = true, nowIso = nowIso)
        return WidgetToday(
            trackerName = tracker.name,
            rows = plan.items.map { WidgetRow(it.habit, it.occurrence, it.phase) },
            hideNames = hideNames,
            done = plan.done,
            total = plan.total,
            nextId = plan.nextId,
            startsLater = plan.startsOn != null
        )
    }
}
