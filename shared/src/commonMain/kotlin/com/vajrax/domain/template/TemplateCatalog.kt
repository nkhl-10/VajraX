package com.vajrax.domain.template

import com.vajrax.domain.habit.HabitType
import com.vajrax.domain.model.TrackingMode

/**
 * The immutable default template library shipped with the app (spec 01 §6, principle 6).
 * Combines the six templates from the Discover design with the curated [TemplateLibrary],
 * adding display category, a real description, recommended-for text and cycle length.
 * Bump [VERSION] whenever this content changes so the seeder refreshes the stored copies.
 */
object TemplateCatalog {

    // 4: bundled "community" templates no longer credit invented authors.
    const val VERSION = "4"
    const val BLANK_ID = "blank_custom_template"

    /** Category chips shown in the template library, in display order. */
    val categories = listOf(
        "Morning", "Daily", "Fitness", "Health", "Mind", "Study", "Work",
        "Growth", "Evening", "Detox", "Finance", "Challenge", "Arc"
    )

    /** Onboarding goal chip -> template categories it recommends. */
    val goalToCategories: Map<String, Set<String>> = mapOf(
        "Better mornings" to setOf("Morning"),
        "Fitness" to setOf("Fitness"),
        "Healthy lifestyle" to setOf("Health", "Fitness"),
        "Focus & productivity" to setOf("Work", "Daily"),
        "Study & learning" to setOf("Study"),
        "Mindfulness" to setOf("Mind", "Evening"),
        "Better sleep" to setOf("Evening", "Mind"),
        "Less screen time" to setOf("Detox"),
        "Money habits" to setOf("Finance"),
        "Self-discipline" to setOf("Challenge", "Growth", "Arc")
    )

    private data class Meta(
        val category: String,
        val description: String,
        val recommendedFor: String,
        val durationDays: Int = 30,
        val weekendOnly: Boolean = false
    )

    private val meta: Map<String, Meta> = mapOf(
        "balanced_daily_routine" to Meta("Daily", "A complete day that balances health, focused work and rest.", "Anyone who wants a full daily rhythm"),
        "simple_daily_routine" to Meta("Daily", "Only the essentials, so the routine survives busy days.", "Beginners and busy schedules"),
        "30_minute_morning_routine" to Meta("Morning", "Wake, hydrate, move and plan in about 30 minutes.", "People with short mornings"),
        "productive_morning_routine" to Meta("Morning", "A longer morning that front-loads your most important work.", "Professionals and early starters"),
        "night_routine" to Meta("Evening", "Wind down, prepare for tomorrow and protect your sleep.", "Anyone who sleeps poorly"),
        "fitness_routine" to Meta("Fitness", "Training, recovery and fuel for steady physical progress.", "Gym-goers and athletes"),
        "healthy_lifestyle" to Meta("Health", "Hydration, movement, meals and sleep in one routine.", "A healthier everyday life"),
        "mindfulness_mental_wellness" to Meta("Mind", "Short practices for calm, clarity and emotional balance.", "Stress relief and focus"),
        "student_study_routine" to Meta("Study", "Structured study blocks with revision and breaks.", "Students and exam preparation"),
        "work_productivity_routine" to Meta("Work", "Deep work, planning and shutdown for high-output days.", "Knowledge workers"),
        "reading_learning" to Meta("Study", "Daily reading and note-taking that compounds over time.", "Lifelong learners"),
        "digital_detox" to Meta("Detox", "Reclaim attention with phone-free blocks and evenings.", "Heavy phone users"),
        "personal_finance" to Meta("Finance", "Small daily money checks that build financial control.", "Budgeting and saving"),
        "home_personal_organization" to Meta("Daily", "Keep your space and admin tidy with a few minutes a day.", "A calmer, organised home"),
        "personal_growth" to Meta("Growth", "Reflection, learning and goals for steady self-improvement.", "Personal development"),
        "5_am_early_riser_routine" to Meta("Morning", "An early start with quiet time before the world wakes.", "Early risers"),
        "weekend_routine" to Meta("Daily", "A relaxed structure for Saturdays and Sundays.", "Restful, productive weekends", weekendOnly = true),
        "21_day_discipline_challenge" to Meta("Challenge", "Three weeks of non-negotiables to build self-discipline.", "A focused reset", durationDays = 21),
        "30_day_personal_growth_challenge" to Meta("Challenge", "A month-long program across body, mind and skills.", "Committed self-improvement", durationDays = 30)
    )

    private fun h(
        name: String,
        time: String,
        minutes: Int,
        order: Int,
        mode: TrackingMode = TrackingMode.MANUAL,
        type: HabitType = HabitType.BOOLEAN,
        target: Double = 1.0,
        unit: String? = null
    ) = DefaultHabit(
        name = name,
        startTime = time,
        duration = minutes,
        trackingType = mode,
        target = target.toString(),
        repeatDays = listOf(1, 2, 3, 4, 5, 6, 7),
        reminderEnabled = true,
        sortOrder = order,
        habitType = type,
        targetValue = target,
        unit = unit
    )

    /** Templates from the Discover design (provided + community picks bundled for offline use). */
    private val designTemplates = listOf(
        DefaultTemplate(
            id = "morning_discipline",
            name = "Morning Discipline",
            category = "Morning",
            description = "Build a structured morning routine",
            difficulty = "Medium",
            estimatedDuration = "Daily",
            recommendedFor = "Anyone who wants a calm, consistent start",
            habits = listOf(
                h("Wake up", "06:00", 5, 1),
                h("Drink a glass of water", "06:05", 5, 2),
                h("Make your bed", "06:10", 5, 3),
                h("Meditation / breathing", "06:15", 10, 4, TrackingMode.TIMER),
                h("Light stretching / mobility", "06:30", 15, 5),
                h("Plan the day", "06:45", 10, 6)
            )
        ),
        DefaultTemplate(
            id = "deep_work_block",
            name = "Deep Work Block",
            category = "Work",
            description = "Lock in focus and maximize high-output hours",
            difficulty = "Hard",
            estimatedDuration = "Daily",
            recommendedFor = "Developers, writers and makers",
            habits = listOf(
                h("Phone away, clear workspace", "08:45", 5, 1),
                h("Deep work session", "09:00", 90, 2, TrackingMode.TIMER),
                h("Recovery break", "10:30", 15, 3),
                h("Second deep work session", "10:45", 75, 4, TrackingMode.TIMER)
            )
        ),
        DefaultTemplate(
            id = "30_day_challenge",
            name = "30-Day Challenge",
            category = "Challenge",
            description = "A rigorous month-long discipline protocol",
            difficulty = "Hard",
            estimatedDuration = "30 days",
            frequencyLabel = "30 days",
            durationDays = 30,
            recommendedFor = "A committed month of discipline",
            habits = listOf(
                h("Daily Review", "06:30", 15, 1),
                h("Deep Work", "09:00", 60, 2, TrackingMode.TIMER),
                h("Lunch Walk", "12:30", 30, 3),
                h("Learning", "15:00", 60, 4),
                h("Workout", "18:00", 60, 5),
                h("Reading", "20:30", 30, 6),
                h("Plan Tomorrow", "21:30", 15, 7),
                h("Sleep", "22:30", 480, 8)
            )
        ),
        DefaultTemplate(
            id = "6am_routine",
            name = "6 AM Routine",
            category = "Morning",
            description = "Wake up early and win the morning",
            difficulty = "Medium",
            estimatedDuration = "Daily",
            recommendedFor = "Early risers and night owls who want to change",
            habits = listOf(
                h("Wake up at 6 AM", "06:00", 5, 1),
                h("Hydrate", "06:05", 5, 2, type = HabitType.COUNT, target = 2.0, unit = "glasses"),
                h("Light exercise", "06:15", 20, 3),
                h("Cold shower", "06:40", 10, 4),
                h("Journal", "06:55", 10, 5)
            )
        ),
        DefaultTemplate(
            id = "evening_wind_down",
            name = "Evening Wind Down",
            category = "Evening",
            description = "Slow down your mind for deep, restful recovery",
            difficulty = "Easy",
            estimatedDuration = "Daily",
            isCommunity = true,
            recommendedFor = "Better sleep and a calmer evening",
            habits = listOf(
                h("Digital sunset", "21:00", 5, 1),
                h("Tidy up your space", "21:10", 10, 2),
                h("Read fiction", "21:30", 20, 3, type = HabitType.DURATION, target = 20.0, unit = "min"),
                h("Gratitude journal", "22:00", 5, 4)
            )
        ),
        DefaultTemplate(
            id = "fitness_starter_pack",
            name = "Fitness Starter Pack",
            category = "Fitness",
            description = "Essential daily habits for athletic consistency",
            difficulty = "Medium",
            estimatedDuration = "Daily",
            isCommunity = true,
            recommendedFor = "Getting fit without overthinking it",
            habits = listOf(
                h("20-minute workout", "07:00", 20, 1, TrackingMode.TIMER),
                h("Drink water", "08:00", 5, 2, type = HabitType.COUNT, target = 8.0, unit = "glasses"),
                h("Protein with every meal", "13:00", 5, 3),
                h("Walk 8,000 steps", "18:00", 60, 4, type = HabitType.VALUE, target = 8000.0, unit = "steps"),
                h("Stretch before bed", "21:30", 10, 5)
            )
        )
    )

    private fun cleanName(name: String): String =
        name.dropWhile { !it.isLetterOrDigit() }.trim().ifBlank { name }

    private fun enrich(t: DefaultTemplate): DefaultTemplate {
        val m = meta[t.id]
        val durationDays = m?.durationDays ?: t.durationDays
        val habits = if (m?.weekendOnly == true) t.habits.map { it.copy(repeatDays = listOf(6, 7)) } else t.habits
        return t.copy(
            name = cleanName(t.name),
            category = m?.category ?: if (t.category == "General") "Daily" else t.category,
            description = m?.description ?: t.description,
            recommendedFor = m?.recommendedFor ?: t.recommendedFor.ifBlank { "Building a steady routine" },
            durationDays = durationDays,
            frequencyLabel = when {
                m?.weekendOnly == true -> "Weekends"
                m?.category == "Challenge" -> "$durationDays days"
                else -> t.frequencyLabel
            },
            habits = habits
        )
    }

    /** Every system template (design + library), excluding the blank tracker. */
    val all: List<DefaultTemplate> by lazy {
        val designIds = designTemplates.map { it.id }.toSet()
        designTemplates + TemplateLibrary.defaultTemplates
            .filter { it.id != BLANK_ID && it.id !in designIds }
            .map(::enrich)
    }
}
