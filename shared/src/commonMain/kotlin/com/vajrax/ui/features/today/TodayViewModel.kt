@file:OptIn(ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)

package com.vajrax.ui.features.today

import com.vajrax.core.coroutines.AppDispatchers
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.time.AppClock
import com.vajrax.core.time.Dates
import com.vajrax.core.time.TimeFormat
import com.vajrax.core.time.minuteTicks
import com.vajrax.domain.analytics.HabitAnalytics
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.habit.NowPicker
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.habit.formatValue
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.repository.ReflectionPeriod
import com.vajrax.domain.repository.ReflectionRepository
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.repository.UserProfile
import com.vajrax.domain.today.TodayPlanner
import com.vajrax.domain.usecase.RoutineManager
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
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
    private val clock: AppClock,
    private val preferences: com.vajrax.domain.usecase.PreferencesService
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
        val reflectionDone: Boolean,
        val cycleSeenFor: String?
    )

    init {
        // A 12/24-hour switch rebuilds the labels right away, not at the next minute.
        val ticks = combine(clock.minuteTicks(), TimeFormat.use24HourChanges) { tick, _ -> tick }
        val dayFlow = ticks.map { it.first }.distinctUntilChanged()
        val data = dayFlow.flatMapLatest { day ->
            val from = Dates.startOfWeek(day.minus(29, DateTimeUnit.DAY))
            combine(
                practices.observeAllTrackedHabits(),
                practices.observeRange(from, day),
                reflections.observeReflection(ReflectionPeriod.WEEK, Dates.startOfWeek(day))
            ) { h, r, refl -> DayData(day, h, r, refl != null) }
        }
        launchLoad {
            persistedTimer = runCatchingCancellable { settings.get(KEY_TIMER) }.getOrNull()?.takeIf { it.isNotBlank() }
            combine(
                ticks,
                trackers.observeActiveTracker(),
                profiles.observeProfile(),
                data,
                settings.observe(KEY_CYCLE_SEEN)
            ) { tick, tracker, profile, d, cycleSeen ->
                Inputs(d.day, tick.second, tracker, profile, d.habits, d.records, d.reflectionDone, cycleSeen)
            }
                .map { build(it) }
                .flowOn(Dispatchers.Default)
                // Stops recomputing when no screen shows Today (app in the background).
                .whileVisible()
                .collect { built ->
                    // Restore outside the reducer: the reducer may be re-run and must stay side-effect free.
                    val restored = if (currentState().timer == null) restoreTimer(built.items) else null
                    updateState {
                        val current = timer ?: restored
                        built.copy(
                            timer = current?.let { t -> built.items.firstOrNull { it.id == t.item.id }?.let { t.copy(item = it) } ?: t },
                            dialView = dialView
                        )
                    }
                }
        }
    }

    init {
        viewModelScope.launch {
            settings.observe(SettingsRepository.HOME_VIEW).collect { v -> updateState { copy(dialView = v != "LIST") } }
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
        viewModelScope.launch(AppDispatchers.IO) {
            runCatchingCancellable {
                settings.put(KEY_TIMER, t?.let { "${it.item.id}|${it.startedAtMs}|${it.accumulatedMs}|${it.running}" } ?: "")
            }
        }
    }

    private fun nowMs(): Long = clock.now().toEpochMilliseconds()

    private fun build(input: Inputs): TodayUiState {
        val day = input.day
        val now = input.minute
        val plan = TodayPlanner.plan(day, now, input.tracker, input.habits, input.records, input.reflectionDone, clock.nowIso())
        val from = Dates.startOfWeek(day.minus(29, DateTimeUnit.DAY))
        val analytics = HabitAnalytics(input.habits, input.records, day, from..day)
        val weekStart = Dates.startOfWeek(day)

        val items = plan.items.map { p ->
            val habit = p.habit
            val occ = p.occurrence
            val (label, fraction) = when {
                p.weeklyDone != null -> "${minOf(p.weeklyDone, habit.schedule.weeklyTarget)} / ${habit.schedule.weeklyTarget} this week" to
                    (p.weeklyDone.toFloat() / habit.schedule.weeklyTarget).coerceIn(0f, 1f)
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
                phase = p.phase,
                timeLabel = TimeFormat.display(occ.scheduledTime ?: habit.time).ifBlank { "Anytime" },
                progressLabel = label,
                progressFraction = fraction
            )
        }

        val name = input.profile?.displayName?.trim().orEmpty().split(" ").firstOrNull().orEmpty()
        val salutation = when (now / 60) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
        // A routine started "tomorrow" has no occurrences yet: say when it begins, not "Rest day".
        val startsLabel = plan.startsOn?.let {
            if (Dates.daysBetween(day, it) == 1) "tomorrow" else "on ${Dates.shortLabel(it)}"
        }
        val firstUp = plan.firstHabit?.let { h ->
            listOf(h.title, TimeFormat.display(h.time)).filter { it.isNotBlank() }.joinToString(" · ")
        }
        val status = when {
            input.tracker == null -> "Choose a routine."
            startsLabel != null -> "Starts $startsLabel."
            plan.total == 0 -> "Rest day."
            plan.done == plan.total -> "All done today."
            plan.openEarlier > 0 -> "A few still open."
            plan.done == 0 -> "Small steps. Consistent progress."
            else -> "You're on track today."
        }

        return TodayUiState(
            isLoading = false,
            dayWrap = plan.wrapUp?.let { DayWrap(it.done, it.total, it.skipped, it.allDone) },
            weeklyReviewDue = plan.weeklyReviewDue,
            startsLabel = startsLabel,
            cycleCompleteDays = input.tracker
                ?.takeIf { plan.cycleComplete && input.cycleSeenFor != it.id }
                ?.totalDays,
            firstUp = firstUp,
            hasTracker = input.tracker != null,
            trackerName = input.tracker?.name ?: "",
            greeting = if (name.isBlank()) "$salutation." else "$salutation, $name.",
            dateLabel = Dates.headerLabel(day).uppercase(),
            statusLine = status,
            items = items,
            nowId = plan.nowId,
            doneCount = plan.done,
            totalCount = plan.total,
            weekRate = analytics.period(weekStart, day).rate,
            consistencyRate = analytics.period(day.minus(29, DateTimeUnit.DAY), day).rate
        )
    }

    private fun candidates(items: List<TodayItem>) = items.map { TodayPlanner.candidate(it.habit, it.occurrence, it.phase) }

    override fun sendIntent(intent: TodayIntent) {
        when (intent) {
            is TodayIntent.Toggle -> perform {
                val message = doneMessage(intent.occurrenceId)
                val before = routineManager.toggle(intent.occurrenceId)
                // Un-checking is undoable too: a mis-tap shouldn't cost a completion.
                if (!before.isDone && !before.isSkipped) message to before else "Marked as not done" to before
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
                "Minimum done" to before
            }
            is TodayIntent.Increment -> perform {
                val before = snapshot(intent.occurrenceId)
                routineManager.increment(intent.occurrenceId)
                "+1 · ${title(intent.occurrenceId)}" to before
            }
            is TodayIntent.RecordValue -> perform {
                val before = snapshot(intent.occurrenceId)
                routineManager.recordValue(intent.occurrenceId, intent.value)
                "Saved" to before
            }
            is TodayIntent.Skip -> perform {
                val before = snapshot(intent.occurrenceId)
                routineManager.skip(intent.occurrenceId, intent.reason)
                "Skipped" to before
            }
            is TodayIntent.Snooze -> perform {
                val before = snapshot(intent.occurrenceId)
                routineManager.snooze(intent.occurrenceId, com.vajrax.domain.BusinessRules.SNOOZE_MINUTES)
                "Snoozed ${com.vajrax.domain.BusinessRules.SNOOZE_MINUTES} min" to before
            }
            is TodayIntent.Move -> perform {
                val before = snapshot(intent.occurrenceId)
                routineManager.move(intent.occurrenceId, intent.time)
                "Moved to ${TimeFormat.display(intent.time)}" to before
            }
            is TodayIntent.SaveNote -> perform {
                routineManager.setNote(intent.occurrenceId, intent.note)
                "Note saved" to null
            }
            is TodayIntent.Reopen -> perform {
                val before = snapshot(intent.occurrenceId)
                routineManager.reopen(intent.occurrenceId)
                "Marked as not done" to before
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
            is TodayIntent.SetDialView -> {
                updateState { copy(dialView = intent.dial) }
                viewModelScope.launch(AppDispatchers.IO) {
                    runCatchingCancellable { preferences.setHomeDial(intent.dial) }
                        .onFailure { com.vajrax.core.log.VxLog.w("Today", "Couldn't save home view", it) }
                }
            }
            TodayIntent.DismissCycleComplete -> viewModelScope.launch(AppDispatchers.IO) {
                runCatchingCancellable { trackers.getActiveTracker()?.let { settings.put(KEY_CYCLE_SEEN, it.id) } }
                    .onFailure { sendEffect(TodayEffect.ShowMessage(userMessage(it))) }
            }
            TodayIntent.StartToday -> perform {
                routineManager.startToday()
                "Started today" to null
            }
        }
    }

    private fun title(id: String) = currentState().items.firstOrNull { it.id == id }?.habit?.title ?: "Done"

    private fun snapshot(id: String): Occurrence? = currentState().items.firstOrNull { it.id == id }?.occurrence

    /** "✓ Wake up · Next: Drink water, 6:35 AM", naming the habit that becomes current next. */
    private fun doneMessage(id: String): String {
        val items = currentState().items
        val current = items.firstOrNull { it.id == id } ?: return "✓ Done"
        val nextId = NowPicker.next(candidates(items), clock.minuteOfDay(), id, clock.nowIso())
        val next = items.firstOrNull { it.id == nextId }
        return if (next == null) "✓ ${current.habit.title} · All done"
        else "✓ ${current.habit.title} · Next: ${next.habit.title}, ${next.timeLabel}"
    }

    companion object {
        private const val KEY_TIMER = "active_focus_timer"
        /** Tracker id whose cycle-complete note was dismissed. */
        private const val KEY_CYCLE_SEEN = "cycle_complete_seen"
    }

    private fun perform(block: suspend () -> Pair<String, Occurrence?>?) {
        viewModelScope.launch(AppDispatchers.IO) {
            runCatchingCancellable { block() }
                .onSuccess { msg -> if (msg != null) sendEffect(TodayEffect.ShowMessage(msg.first, msg.second)) }
                .onFailure { t -> sendEffect(TodayEffect.ShowMessage(userMessage(t))) }
        }
    }
}
