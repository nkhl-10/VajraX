@file:OptIn(ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)

package com.vajrax.ui.features.today

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.core.time.minuteTicks
import com.vajrax.domain.analytics.HabitAnalytics
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.ScheduleType
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.habit.formatValue
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.repository.ReflectionPeriod
import com.vajrax.domain.repository.ReflectionRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.repository.UserProfile
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus

/**
 * Today's plan (spec 04 Screen 05): progress, NOW card, the rest of the day in time order,
 * and one-tap check-ins that persist immediately through [RoutineManager].
 */
class TodayViewModel(
    private val routineManager: RoutineManager,
    private val practices: PracticeRepository,
    private val trackers: TrackerRepository,
    private val profiles: ProfileRepository,
    private val reflections: ReflectionRepository,
    private val settings: SettingsRepository,
    private val clock: AppClock
) : MviViewModel<TodayUiState, TodayIntent, TodayEffect>(TodayUiState()) {

    /** Persisted focus session: "occurrenceId|startedAtMs|accumulatedMs|running". */
    private var persistedTimer: String? = null

    private data class Inputs(
        val day: LocalDate,
        val minute: Int,
        val tracker: Tracker?,
        val profile: UserProfile?,
        val habits: List<Habit>,
        val records: List<Occurrence>,
        val reflectionDone: Boolean
    )

    init {
        val ticks = clock.minuteTicks()
        val dayFlow = ticks.map { it.first }.distinctUntilChanged()
        val data = dayFlow.flatMapLatest { day ->
            val from = Dates.startOfWeek(day.minus(29, DateTimeUnit.DAY))
            combine(
                practices.observeAllTrackedHabits(),
                practices.observeRange(from, day),
                reflections.observeReflection(ReflectionPeriod.WEEK, Dates.startOfWeek(day))
            ) { h, r, refl -> DayData(day, h, r, refl != null) }
        }
        viewModelScope.launch {
            persistedTimer = runCatching { settings.get(KEY_TIMER) }.getOrNull()?.takeIf { it.isNotBlank() }
            combine(ticks, trackers.observeActiveTracker(), profiles.observeProfile(), data) { tick, tracker, profile, d ->
                Inputs(d.day, tick.second, tracker, profile, d.habits, d.records, d.reflectionDone)
            }
                .map { build(it) }
                .flowOn(Dispatchers.Default)
                .collect { built ->
                    // Restore outside the reducer: the reducer may be re-run and must stay side-effect free.
                    val restored = if (currentState().timer == null) restoreTimer(built.items) else null
                    updateState {
                        val current = timer ?: restored
                        built.copy(timer = current?.let { t -> built.items.firstOrNull { it.id == t.item.id }?.let { t.copy(item = it) } ?: t })
                    }
                }
        }
    }

    private data class DayData(val day: LocalDate, val habits: List<Habit>, val records: List<Occurrence>, val reflectionDone: Boolean)

    /** Restores a focus session saved before the app was closed, if its habit is still open today. */
    private fun restoreTimer(items: List<TodayItem>): TimerState? {
        val raw = persistedTimer ?: return null
        persistedTimer = null
        val parts = raw.split("|")
        if (parts.size != 4) return null
        val item = items.firstOrNull { it.id == parts[0] && it.occurrence.isOpen } ?: return null
        return TimerState(item, parts[1].toLongOrNull() ?: return null, parts[2].toLongOrNull() ?: 0L, parts[3] == "true")
    }

    private fun saveTimer(t: TimerState?) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                settings.put(KEY_TIMER, t?.let { "${it.item.id}|${it.startedAtMs}|${it.accumulatedMs}|${it.running}" } ?: "")
            }
        }
    }

    private fun nowMs(): Long = clock.now().toEpochMilliseconds()

    private fun build(input: Inputs): TodayUiState {
        val day = input.day
        val habitsById = input.habits.associateBy { it.id }
        val todays = input.records.filter { it.date == day }.mapNotNull { occ -> habitsById[occ.habitId]?.let { it to occ } }
        val from = Dates.startOfWeek(day.minus(29, DateTimeUnit.DAY))
        val analytics = HabitAnalytics(input.habits, input.records, day, from..day)

        val weekStart = Dates.startOfWeek(day)
        val weekDone = input.records.filter { it.date >= weekStart && it.isDone }.groupBy { it.habitId }.mapValues { it.value.size }

        val now = input.minute
        val items = todays.map { (habit, occ) ->
            val start = TimeFormat.toMinutes(occ.scheduledTime ?: habit.time)
            val weekly = habit.schedule.type == ScheduleType.WEEKLY_TARGET
            val doneThisWeek = weekDone[habit.id] ?: 0
            val phase = when {
                occ.isDone -> TodayPhase.DONE
                occ.isSkipped -> TodayPhase.SKIPPED
                weekly && doneThisWeek >= habit.schedule.weeklyTarget -> TodayPhase.WEEKLY_MET
                start != null && start + habit.durationMinutes <= now -> TodayPhase.OPEN_EARLIER
                else -> TodayPhase.UPCOMING
            }
            val (label, fraction) = when {
                weekly -> "${minOf(doneThisWeek, habit.schedule.weeklyTarget)} / ${habit.schedule.weeklyTarget} this week" to
                    (doneThisWeek.toFloat() / habit.schedule.weeklyTarget).coerceIn(0f, 1f)
                habit.type != HabitType.BOOLEAN -> {
                    val v = occ.value ?: 0.0
                    "${formatValue(v)} / ${formatValue(habit.targetValue)} ${habit.unit ?: ""}".trim() to
                        (v / habit.targetValue).toFloat().coerceIn(0f, 1f)
                }
                else -> null to null
            }
            TodayItem(
                habit = habit,
                occurrence = occ,
                phase = phase,
                timeLabel = TimeFormat.display(occ.scheduledTime ?: habit.time).ifBlank { "Anytime" },
                progressLabel = label,
                progressFraction = fraction
            )
        }.sortedWith(compareBy<TodayItem> { TimeFormat.toMinutes(it.occurrence.scheduledTime ?: it.habit.time) ?: Int.MAX_VALUE }.thenBy { it.habit.sortOrder })

        val nowId = pickNow(items, now)
        val finalItems = items.map { if (it.id == nowId) it.copy(phase = TodayPhase.NOW) else it }

        val counted = finalItems.filter { it.phase != TodayPhase.SKIPPED && !(it.phase == TodayPhase.WEEKLY_MET) }
        val done = counted.count { it.phase == TodayPhase.DONE }
        val total = counted.size
        val openEarlier = finalItems.count { it.phase == TodayPhase.OPEN_EARLIER }

        val name = input.profile?.displayName?.trim().orEmpty().split(" ").firstOrNull().orEmpty()
        val hour = now / 60
        val salutation = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
        val status = when {
            input.tracker == null -> "Choose a routine to get started."
            total == 0 -> "Nothing scheduled today — rest well."
            done == total -> "Everything done today. Well kept."
            openEarlier > 0 -> "A few are still open — there's time."
            done == 0 -> "Small steps. Consistent progress."
            else -> "You're on track this day."
        }
        // End of day: everything done, or the last habit has started and had up to an hour
        // (long overnight habits such as Sleep would otherwise end "tomorrow").
        val lastEnd = counted.mapNotNull { i ->
            TimeFormat.toMinutes(i.occurrence.scheduledTime ?: i.habit.time)?.let { it + minOf(i.habit.durationMinutes, 60) }
        }.maxOrNull()?.coerceAtMost(23 * 60 + 30)
        val allDone = total > 0 && done == total
        val wrap = if (total > 0 && (allDone || (lastEnd != null && now >= lastEnd))) {
            DayWrap(done, total, finalItems.count { it.phase == TodayPhase.SKIPPED }, allDone)
        } else null

        // Weekly review: Saturday evening and Sunday, when the week has records and no reflection yet.
        val iso = day.dayOfWeek.isoDayNumber
        val reviewWindow = iso == 7 || (iso == 6 && now >= 18 * 60)
        val weekHasRecords = input.records.any { it.date >= weekStart && it.date <= day && (it.isDone || it.isSkipped) }
        val reviewDue = input.tracker != null && reviewWindow && weekHasRecords && !input.reflectionDone

        return TodayUiState(
            isLoading = false,
            dayWrap = wrap,
            weeklyReviewDue = reviewDue,
            hasTracker = input.tracker != null,
            trackerName = input.tracker?.name ?: "",
            greeting = if (name.isBlank()) "$salutation." else "$salutation, $name.",
            dateLabel = Dates.headerLabel(day).uppercase(),
            statusLine = status,
            items = finalItems,
            nowId = nowId,
            doneCount = done,
            totalCount = total,
            weekRate = analytics.period(weekStart, day).rate,
            consistencyRate = analytics.period(day.minus(29, DateTimeUnit.DAY), day).rate
        )
    }

    /**
     * The single current habit: one whose time window contains now; otherwise the next one due
     * within the hour; otherwise the most recent still-open one; otherwise the next upcoming.
     */
    private fun pickNow(items: List<TodayItem>, now: Int): String? {
        val open = items.filter { it.phase == TodayPhase.UPCOMING || it.phase == TodayPhase.OPEN_EARLIER }
        fun start(i: TodayItem) = TimeFormat.toMinutes(i.occurrence.scheduledTime ?: i.habit.time)
        open.filter { i -> start(i)?.let { it <= now && now < it + i.habit.durationMinutes } == true }
            .maxByOrNull { start(it) ?: 0 }?.let { return it.id }
        open.filter { i -> start(i)?.let { it > now && it - now <= 60 } == true }
            .minByOrNull { start(it) ?: 0 }?.let { return it.id }
        open.filter { it.phase == TodayPhase.OPEN_EARLIER }.maxByOrNull { start(it) ?: 0 }?.let { return it.id }
        return open.minByOrNull { start(it) ?: Int.MAX_VALUE }?.id
    }

    override fun sendIntent(intent: TodayIntent) {
        when (intent) {
            is TodayIntent.Toggle -> perform {
                val message = doneMessage(intent.occurrenceId)
                val before = routineManager.toggle(intent.occurrenceId)
                if (!before.isDone && !before.isSkipped) message to before else null
            }
            is TodayIntent.Complete -> perform {
                val message = doneMessage(intent.occurrenceId)
                val before = snapshot(intent.occurrenceId)
                routineManager.complete(intent.occurrenceId)
                message to before
            }
            is TodayIntent.CompleteMinimum -> perform {
                val before = snapshot(intent.occurrenceId)
                routineManager.completeMinimum(intent.occurrenceId)
                "Minimum version logged — momentum kept." to before
            }
            is TodayIntent.Increment -> perform {
                routineManager.increment(intent.occurrenceId)
                null
            }
            is TodayIntent.RecordValue -> perform {
                routineManager.recordValue(intent.occurrenceId, intent.value)
                "Saved" to null
            }
            is TodayIntent.Skip -> perform {
                val before = snapshot(intent.occurrenceId)
                routineManager.skip(intent.occurrenceId, intent.reason)
                "Skipped — it won't count against you." to before
            }
            is TodayIntent.Snooze -> perform {
                routineManager.snooze(intent.occurrenceId, 15)
                "Snoozed for 15 minutes" to null
            }
            is TodayIntent.Move -> perform {
                routineManager.move(intent.occurrenceId, intent.time)
                "Moved to ${TimeFormat.display(intent.time)} for today" to null
            }
            is TodayIntent.SaveNote -> perform {
                routineManager.setNote(intent.occurrenceId, intent.note)
                "Note saved" to null
            }
            is TodayIntent.Reopen -> perform {
                routineManager.reopen(intent.occurrenceId)
                null
            }
            is TodayIntent.Undo -> perform {
                routineManager.restore(intent.previous)
                null
            }
            is TodayIntent.StartTimer -> {
                val item = currentState().items.firstOrNull { it.id == intent.occurrenceId } ?: return
                val t = TimerState(item, nowMs(), 0L, running = true)
                updateState { copy(timer = t) }
                saveTimer(t)
            }
            TodayIntent.PauseTimer -> {
                val t = currentState().timer?.takeIf { it.running } ?: return
                val paused = t.copy(accumulatedMs = t.elapsedMs(nowMs()), running = false)
                updateState { copy(timer = paused) }
                saveTimer(paused)
            }
            TodayIntent.ResumeTimer -> {
                val t = currentState().timer?.takeIf { !it.running } ?: return
                val resumed = t.copy(startedAtMs = nowMs(), running = true)
                updateState { copy(timer = resumed) }
                saveTimer(resumed)
            }
            TodayIntent.FinishTimer -> {
                val t = currentState().timer ?: return
                val minutes = (t.elapsedMs(nowMs()) / 60_000L).toInt().coerceAtLeast(1)
                updateState { copy(timer = null) }
                saveTimer(null)
                val before = t.item.occurrence
                perform {
                    routineManager.finishTimer(t.item.id, minutes)
                    "Session saved: ${TimeFormat.duration(minutes)}" to before
                }
            }
            TodayIntent.CancelTimer -> {
                updateState { copy(timer = null) }
                saveTimer(null)
            }
        }
    }

    private fun title(id: String) = currentState().items.firstOrNull { it.id == id }?.habit?.title ?: "Done"

    private fun snapshot(id: String): Occurrence? = currentState().items.firstOrNull { it.id == id }?.occurrence

    /** "✓ Wake up · Next: Drink water, 6:35 AM" — keeps the loop moving to the next habit. */
    private fun doneMessage(id: String): String {
        val items = currentState().items
        val current = items.firstOrNull { it.id == id } ?: return "✓ Done"
        val open = items.filter { it.id != id && (it.phase == TodayPhase.UPCOMING || it.phase == TodayPhase.OPEN_EARLIER || it.phase == TodayPhase.NOW) }
        val start = TimeFormat.toMinutes(current.occurrence.scheduledTime ?: current.habit.time) ?: -1
        val next = open.firstOrNull { (TimeFormat.toMinutes(it.occurrence.scheduledTime ?: it.habit.time) ?: Int.MAX_VALUE) >= start }
            ?: open.firstOrNull()
        return if (next == null) "✓ ${current.habit.title} · That's everything for today"
        else "✓ ${current.habit.title} · Next: ${next.habit.title}, ${next.timeLabel}"
    }

    companion object {
        private const val KEY_TIMER = "active_focus_timer"
    }

    private fun perform(block: suspend () -> Pair<String, Occurrence?>?) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { block() }
                .onSuccess { msg -> if (msg != null) sendEffect(TodayEffect.ShowMessage(msg.first, msg.second)) }
                .onFailure { t -> sendEffect(TodayEffect.ShowMessage(userMessage(t))) }
        }
    }
}
