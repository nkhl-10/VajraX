@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)

package com.vajrax.ui.features.report

import com.vajrax.core.time.AppClock
import com.vajrax.core.time.Dates
import com.vajrax.core.time.minuteTicks
import com.vajrax.domain.analytics.HabitAnalytics
import com.vajrax.domain.analytics.PeriodStats
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.Occurrence
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.repository.Goal
import com.vajrax.domain.repository.GoalRepository
import com.vajrax.domain.repository.GoalTargetType
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.domain.repository.Reflection
import com.vajrax.domain.repository.ReflectionPeriod
import com.vajrax.domain.repository.ReflectionRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.habit.formatValue
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Weekly / monthly progress (spec 04 Screens 08–10, brief §5). Every number is derived from
 * persisted occurrences by [HabitAnalytics]; nothing is estimated or hard-coded.
 */
class ReportViewModel(
    private val practices: PracticeRepository,
    private val trackers: TrackerRepository,
    private val goals: GoalRepository,
    private val reflections: ReflectionRepository,
    private val clock: AppClock
) : MviViewModel<ReportUiState, ReportIntent, ReportEffect>(ReportUiState()) {

    private val period = MutableStateFlow(ReportPeriod.WEEK)

    private data class Inputs(
        val today: LocalDate,
        val period: ReportPeriod,
        val tracker: Tracker?,
        val habits: List<Habit>,
        val records: List<Occurrence>,
        val goals: List<Goal>,
        val reflection: Reflection?
    )

    init {
        val today = clock.minuteTicks().map { it.first }.distinctUntilChanged()
        viewModelScope.launch {
            combine(today, period) { t, p -> t to p }
                .flatMapLatest { (t, p) ->
                    val from = Dates.startOfWeek(t.minus(HISTORY_DAYS, DateTimeUnit.DAY))
                    val reflectionStart = if (p == ReportPeriod.WEEK) Dates.startOfWeek(t) else Dates.startOfMonth(t)
                    val reflectionType = if (p == ReportPeriod.WEEK) ReflectionPeriod.WEEK else ReflectionPeriod.MONTH
                    combine(
                        combine(trackers.observeActiveTracker(), practices.observeAllTrackedHabits(), practices.observeRange(from, t)) { a, b, c -> Triple(a, b, c) },
                        goals.observeGoals(),
                        reflections.observeReflection(reflectionType, reflectionStart)
                    ) { core, g, r -> Inputs(t, p, core.first, core.second, core.third, g, r) }
                }
                .map { build(it) }
                .flowOn(Dispatchers.Default)
                .collect { built -> updateState { built.copy(openReflection = openReflection) } }
        }
    }

    private fun build(input: Inputs): ReportUiState {
        val today = input.today
        val window = Dates.startOfWeek(today.minus(HISTORY_DAYS, DateTimeUnit.DAY))..today
        val analytics = HabitAnalytics(input.habits, input.records, today, window)
        val week = input.period == ReportPeriod.WEEK
        val start = if (week) Dates.startOfWeek(today) else Dates.startOfMonth(today)
        val end = if (week) Dates.endOfWeek(today) else Dates.endOfMonth(today)
        val comparison = if (week) HabitAnalytics.weekComparison(analytics, today) else HabitAnalytics.monthComparison(analytics, today)
        val overall = comparison.current

        val bars = if (week) {
            Dates.range(start, end).map { d ->
                val s = analytics.day(d)
                Bar(Dates.dayLetter(d.dayOfWeek), s.completed, s.eligible + s.pending, d == today, d > today)
            }
        } else {
            var weekStart = start
            val list = ArrayList<Bar>()
            var index = 1
            while (weekStart <= end) {
                val weekEnd = minOf(weekStart.plus(6 - (weekStart.dayOfWeek.ordinal), DateTimeUnit.DAY), end)
                val s = analytics.period(weekStart, minOf(weekEnd, today))
                list += Bar("W$index", s.completed, s.eligible + s.pending, today in weekStart..weekEnd, weekStart > today)
                weekStart = weekEnd.plus(1, DateTimeUnit.DAY)
                index++
            }
            list
        }

        val periodEnd = minOf(end, today)
        val streak = analytics.dayStreak()
        val most = analytics.mostConsistent(start, periodEnd).map { HabitStatRow(it.habit, it.stats, it.streak.current, it.streakUnit) }
        val attention = analytics.needsAttention(start, periodEnd).map { HabitStatRow(it.habit, it.stats, it.streak.current, it.streakUnit) }
        val categories = analytics.categoryStats(start, periodEnd)
        val areas = categories.mapIndexed { i, c ->
            val tag = when {
                categories.size < 2 -> null
                i == 0 -> "Strongest"
                i == categories.lastIndex && (c.stats.rate ?: 100) < 70 -> "Needs attention"
                else -> null
            }
            AreaRow(c.category, c.stats, tag)
        }

        val headline = when {
            overall.eligible == 0 && overall.pending > 0 -> "Your first results appear as the day goes on"
            overall.eligible == 0 -> "No check-ins in this period yet"
            (comparison.rateDelta ?: 0) > 2 -> "Your consistency is increasing"
            (comparison.rateDelta ?: 0) < -2 -> "A lighter stretch — that's okay"
            else -> "Holding steady"
        }

        val activeHabits = input.habits.filter { it.trackerId == input.tracker?.id && it.archivedAt == null }
        val hasData = input.records.any { it.date <= today }
        return ReportUiState(
            isLoading = false,
            hasData = hasData,
            period = input.period,
            periodLabel = if (week) "${Dates.shortLabel(start)} – ${Dates.shortLabel(end)}" else Dates.monthYearLabel(today),
            overall = overall,
            previous = comparison.previous,
            rateDelta = comparison.rateDelta,
            completedDelta = comparison.completedDelta,
            headline = headline,
            bars = bars,
            barsTitle = if (week) "This Week" else "This Month",
            currentStreak = streak.current,
            bestStreak = streak.longest,
            bestStreakEnd = streak.longestEnd?.let { Dates.shortLabel(it) },
            mostConsistent = most,
            needsAttention = attention,
            areas = areas,
            insight = insight(analytics, today),
            habitsTracked = analytics.habitStats(start, periodEnd).size,
            reflectionPeriod = if (week) ReflectionPeriod.WEEK else ReflectionPeriod.MONTH,
            reflectionStart = start,
            reflection = input.reflection,
            goals = input.goals.map { goalProgress(it, analytics, input.habits, today) },
            activeHabits = activeHabits
        )
    }

    /** Transparent pattern: best vs weakest time of day over the last 30 days, with raw counts. */
    private fun insight(analytics: HabitAnalytics, today: LocalDate): PatternInsight? {
        val buckets = analytics.timeBuckets(today.minus(29, DateTimeUnit.DAY), today)
        val usable = buckets.filter { it.stats.eligible >= 3 }
        if (usable.size < 2) return null
        val best = usable.maxBy { it.stats.rateFraction }
        val worst = usable.minBy { it.stats.rateFraction }
        val gap = (best.stats.rate ?: 0) - (worst.stats.rate ?: 0)
        if (gap < 15) return null
        return PatternInsight(
            headline = "${best.label} habits are completed ${best.stats.rate}% of the time, vs ${worst.stats.rate}% in the ${worst.label.lowercase()}.",
            rawData = "Based on ${best.stats.eligible} ${best.label.lowercase()} and ${worst.stats.eligible} ${worst.label.lowercase()} check-ins in the last 30 days.",
            buckets = buckets
        )
    }

    private fun goalProgress(goal: Goal, analytics: HabitAnalytics, habits: List<Habit>, today: LocalDate): GoalProgress {
        val end = minOf(goal.endDate ?: today, today)
        val stats = goal.habitIds.fold(PeriodStats()) { acc, id -> acc + analytics.habitPeriod(id, goal.startDate, end) }
        val names = habits.filter { it.id in goal.habitIds }.map { it.title }
        return when (goal.targetType) {
            GoalTargetType.COMPLETIONS -> {
                val current = stats.completed.toDouble()
                GoalProgress(goal, current, (current / goal.targetValue).toFloat().coerceIn(0f, 1f), "${stats.completed} of ${formatValue(goal.targetValue)} completions", names)
            }
            GoalTargetType.RATE -> {
                val rate = stats.rate ?: 0
                GoalProgress(goal, rate.toDouble(), (rate / goal.targetValue).toFloat().coerceIn(0f, 1f), "$rate% completion · target ${formatValue(goal.targetValue)}%", names)
            }
        }
    }

    override fun sendIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.SetPeriod -> period.value = intent.period
            ReportIntent.StartWeeklyReview -> {
                period.value = ReportPeriod.WEEK
                updateState { copy(openReflection = true) }
            }
            ReportIntent.ReflectionOpened -> updateState { copy(openReflection = false) }
            is ReportIntent.SaveReflection -> {
                val s = currentState()
                val start = s.reflectionStart ?: return
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        reflections.saveReflection(Reflection(s.reflectionPeriod, start, intent.achievements, intent.obstacles, intent.nextActions), clock.nowIso())
                    }.onSuccess { sendEffect(ReportEffect.ShowMessage("Reflection saved")) }
                        .onFailure { sendEffect(ReportEffect.ShowMessage(userMessage(it))) }
                }
            }
            is ReportIntent.SaveGoal -> {
                if (intent.title.isBlank() || intent.target <= 0 || intent.habitIds.isEmpty()) {
                    trySendEffect(ReportEffect.ShowMessage("Add a title, a target above 0 and at least one habit."))
                    return
                }
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        goals.saveGoal(
                            Goal(
                                id = intent.id ?: ("goal_" + Uuid.random().toString()),
                                title = intent.title,
                                description = null,
                                targetType = intent.targetType,
                                targetValue = if (intent.targetType == GoalTargetType.RATE) intent.target.coerceAtMost(100.0) else intent.target,
                                startDate = clock.today(),
                                endDate = intent.endDate,
                                habitIds = intent.habitIds
                            ),
                            clock.nowIso()
                        )
                    }.onSuccess { sendEffect(ReportEffect.ShowMessage("Goal saved")) }
                        .onFailure { sendEffect(ReportEffect.ShowMessage(userMessage(it))) }
                }
            }
            is ReportIntent.DeleteGoal -> viewModelScope.launch(Dispatchers.IO) {
                runCatching { goals.deleteGoal(intent.goalId) }
                    .onSuccess { sendEffect(ReportEffect.ShowMessage("Goal removed")) }
            }
        }
    }

    companion object {
        const val HISTORY_DAYS = 400
    }
}
