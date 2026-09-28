package com.vajrax.android.widget

import com.vajrax.core.time.AppClock
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
    val nowMinute: Int = 0
) {
    private val counted get() = rows.filter { !it.occurrence.isSkipped }
    val done: Int get() = counted.count { it.occurrence.isDone }
    val total: Int get() = counted.size
    val percent: Int get() = if (total > 0) done * 100 / total else 0
    val fraction: Float get() = if (total > 0) done.toFloat() / total else 0f
    val current: WidgetRow? get() = rows.firstOrNull { it.isCurrent }
    val hasTracker: Boolean get() = trackerName != null

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
                ) { tracker, habits, records, hide -> build(tracker, habits, records, hide == "true", minute) }
            }
            .onStart { runCatching { routine.materialize() } }
            .distinctUntilChanged()
    }

    private fun build(tracker: Tracker?, habits: List<Habit>, records: List<Occurrence>, hideNames: Boolean, now: Int): WidgetToday {
        if (tracker == null) return WidgetToday.Empty
        val byId = habits.associateBy { it.id }
        val pairs = records
            .mapNotNull { occ -> byId[occ.habitId]?.let { it to occ } }
            .sortedWith(compareBy<Pair<Habit, Occurrence>> { TimeFormat.toMinutes(it.second.scheduledTime ?: it.first.time) ?: Int.MAX_VALUE }.thenBy { it.first.sortOrder })
        fun start(p: Pair<Habit, Occurrence>) = TimeFormat.toMinutes(p.second.scheduledTime ?: p.first.time)
        val open = pairs.filter { it.second.isOpen }
        val current = open.firstOrNull { p -> start(p)?.let { it <= now && now < it + p.first.durationMinutes } == true }
            ?: open.firstOrNull { p -> (start(p) ?: 0) > now }
            ?: open.firstOrNull()
        return WidgetToday(
            trackerName = tracker.name,
            rows = pairs.map { (h, o) -> WidgetRow(h, o, isCurrent = current?.second?.id == o.id) },
            hideNames = hideNames,
            nowMinute = now
        )
    }
}
