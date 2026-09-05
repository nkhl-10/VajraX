package com.vajrax.domain.template

import com.vajrax.domain.model.LifePath
import com.vajrax.domain.model.Practice
import com.vajrax.domain.model.Principle
import com.vajrax.domain.model.TrackingMode
import com.vajrax.domain.repository.LifePathRepository
import com.vajrax.domain.repository.PracticeRepository

/**
 * Phase 14: Life Path Template Engine.
 * Provides pre-configured, evidence-based systems across 9 global life directions.
 */
class LifePathTemplateEngine(
    private val lifePathRepository: LifePathRepository,
    private val practiceRepository: PracticeRepository
) {

    val availableTemplates: List<PathTemplate> = listOf(
        // 1. High Performance
        PathTemplate(
            path = LifePath("high_performance", "⚡ High Performance", "Deep focus, physical vigor, competence, and relentless execution.", isActive = true),
            primaryLifeArea = "Execution & Health",
            recommendedWakeTime = "06:00",
            recommendedSleepTime = "22:30",
            principles = listOf(
                Principle("hp_p1", "high_performance", "Dichotomy of Control (Stoicism)", "Direct 100% of your energy to your own actions. Ignore what is outside your control."),
                Principle("hp_p2", "high_performance", "Deep Work Block (Behavioral Science)", "Guard your core analytical hours from cognitive fragmentation."),
                Principle("hp_p3", "high_performance", "Physical Primacy (Physiology)", "A sharp mind requires a resilient body. Movement is non-negotiable.")
            ),
            defaultPractices = listOf(
                Practice("hp_pr1", "hp_p3", "Morning Training", 45, 15, "06:30", TrackingMode.MANUAL, true),
                Practice("hp_pr2", "hp_p1", "Daily Reading", 20, 5, "08:00", TrackingMode.MANUAL, true),
                Practice("hp_pr3", "hp_p2", "Deep Work", 60, 15, "09:00", TrackingMode.TIMER, true),
                Practice("hp_pr4", "hp_p3", "Movement Break", 15, 5, "12:30", TrackingMode.PASSIVE, true),
                Practice("hp_pr5", "hp_p1", "Digital Shutdown", 30, 10, "21:30", TrackingMode.MANUAL, true)
            )
        ),

        // 2. Self-Mastery
        PathTemplate(
            path = LifePath("self_mastery", "🗿 Self-Mastery", "Self-respect, emotional discipline, strong boundaries, and autonomy.", isActive = false),
            primaryLifeArea = "Mind & Character",
            recommendedWakeTime = "06:30",
            recommendedSleepTime = "23:00",
            principles = listOf(
                Principle("sm_p1", "self_mastery", "Honor Your Word", "Self-respect is built by keeping the promises you make to yourself in private."),
                Principle("sm_p2", "self_mastery", "Impenetrable Boundaries", "Say no to low-value demands so you can say yes to your essential mission.")
            ),
            defaultPractices = listOf(
                Practice("sm_pr1", "sm_p1", "Focused Morning Routine", 20, 5, "07:00", TrackingMode.MANUAL, true),
                Practice("sm_pr2", "sm_p2", "Protected Deep Work", 90, 20, "09:30", TrackingMode.TIMER, true),
                Practice("sm_pr3", "sm_p1", "Strength & Discipline Training", 45, 15, "17:30", TrackingMode.MANUAL, true),
                Practice("sm_pr4", "sm_p1", "Evening Micro-Reflection", 10, 3, "22:00", TrackingMode.MANUAL, true)
            )
        ),

        // 3. Scholar
        PathTemplate(
            path = LifePath("scholar", "🧠 Scholar", "Reading, rigorous learning, intellectual depth, and critical thinking.", isActive = false),
            primaryLifeArea = "Learning & Intellect",
            recommendedWakeTime = "07:00",
            recommendedSleepTime = "23:30",
            principles = listOf(
                Principle("sc_p1", "scholar", "Knowledge to Action", "Reading without application is mere entertainment. Synthesize what you learn."),
                Principle("sc_p2", "scholar", "First-Principles Thinking", "Break complex ideas down to their fundamental truths before building up.")
            ),
            defaultPractices = listOf(
                Practice("sc_pr1", "sc_p1", "Deep Study & Research", 90, 20, "09:00", TrackingMode.TIMER, true),
                Practice("sc_pr2", "sc_p1", "Book Reading", 45, 10, "14:00", TrackingMode.MANUAL, true),
                Practice("sc_pr3", "sc_p2", "Writing & Synthesis", 30, 10, "19:00", TrackingMode.MANUAL, true),
                Practice("sc_pr4", "sc_p1", "Nature Walk / Contemplation", 20, 5, "17:30", TrackingMode.PASSIVE, true)
            )
        ),

        // 4. Wealth Builder
        PathTemplate(
            path = LifePath("wealth_builder", "💰 Wealth Builder", "High-value skills, career growth, financial discipline, and long-term leverage.", isActive = false),
            primaryLifeArea = "Career & Wealth",
            recommendedWakeTime = "06:30",
            recommendedSleepTime = "23:00",
            principles = listOf(
                Principle("wb_p1", "wealth_builder", "High-Leverage Execution", "Focus on asymmetric opportunities that produce outsized compounding results."),
                Principle("wb_p2", "wealth_builder", "Financial Awareness", "Track cashflow, eliminate emotional spending, and invest with patience.")
            ),
            defaultPractices = listOf(
                Practice("wb_pr1", "wb_p1", "Core Skill Deep Work", 60, 15, "09:00", TrackingMode.TIMER, true),
                Practice("wb_pr2", "wb_p1", "Industry & Market Research", 30, 10, "14:00", TrackingMode.MANUAL, true),
                Practice("wb_pr3", "wb_p2", "Weekly Budget & Investment Audit", 20, 5, "18:00", TrackingMode.MANUAL, true)
            )
        ),

        // 5. Balanced Life
        PathTemplate(
            path = LifePath("balanced_life", "🌿 Balanced Life", "Holistic health, deep relationships, work-life equilibrium, and calmness.", isActive = false),
            primaryLifeArea = "Holistic Wellbeing",
            recommendedWakeTime = "07:00",
            recommendedSleepTime = "22:30",
            principles = listOf(
                Principle("bl_p1", "balanced_life", "Harmony Over Hustle", "Sustainable consistency outperforms frantic burnout every time."),
                Principle("bl_p2", "balanced_life", "Presence in Relationships", "Give undivided attention to the people who matter most.")
            ),
            defaultPractices = listOf(
                Practice("bl_pr1", "bl_p1", "Mindful Morning Walk", 30, 10, "07:30", TrackingMode.PASSIVE, true),
                Practice("bl_pr2", "bl_p1", "Focused Work Block", 60, 15, "09:30", TrackingMode.TIMER, true),
                Practice("bl_pr3", "bl_p2", "Family / Relationship Time", 45, 15, "19:00", TrackingMode.MANUAL, true),
                Practice("bl_pr4", "bl_p1", "Breathwork & Calm Meditation", 15, 5, "21:30", TrackingMode.MANUAL, true)
            )
        ),

        // 6. Creator
        PathTemplate(
            path = LifePath("creator", "🎨 Creator", "Deep craft, consistent creative output, focus blocks, and experimentation.", isActive = false),
            primaryLifeArea = "Creativity & Craft",
            recommendedWakeTime = "07:30",
            recommendedSleepTime = "23:30",
            principles = listOf(
                Principle("cr_p1", "creator", "Output Over Perfection", "Ship daily. Mastery is the byproduct of voluminous creative output."),
                Principle("cr_p2", "creator", "Curate Your Inputs", "Protect your mind from digital clutter to generate original ideas.")
            ),
            defaultPractices = listOf(
                Practice("cr_pr1", "cr_p1", "Studio / Creative Output Block", 90, 20, "09:00", TrackingMode.TIMER, true),
                Practice("cr_pr2", "cr_p2", "Curated Input & Inspiration", 30, 10, "15:00", TrackingMode.MANUAL, true),
                Practice("cr_pr3", "cr_p1", "Publish / Share Progress", 20, 5, "18:00", TrackingMode.MANUAL, true)
            )
        ),

        // 7. Mindful Life
        PathTemplate(
            path = LifePath("mindful_life", "🧘 Mindful Life", "Awareness, emotional regulation, presence, and intentional reflection.", isActive = false),
            primaryLifeArea = "Inner Peace & Awareness",
            recommendedWakeTime = "06:00",
            recommendedSleepTime = "22:00",
            principles = listOf(
                Principle("ml_p1", "mindful_life", "Present Moment Primacy", "The only moment where life actually exists is right now."),
                Principle("ml_p2", "mindful_life", "Non-Reactive Awareness", "Observe your thoughts without being hijacked by them.")
            ),
            defaultPractices = listOf(
                Practice("ml_pr1", "ml_p1", "Morning Stillness & Meditation", 20, 5, "06:30", TrackingMode.MANUAL, true),
                Practice("ml_pr2", "ml_p1", "Mindful Movement & Walking", 20, 5, "12:30", TrackingMode.PASSIVE, true),
                Practice("ml_pr3", "ml_p2", "Evening Digital Sunset", 60, 15, "21:00", TrackingMode.MANUAL, true)
            )
        ),

        // 8. Purpose-Driven
        PathTemplate(
            path = LifePath("purpose_driven", "🕉 Purpose-Driven", "Core values, duty, community contribution, and meaningful impact.", isActive = false),
            primaryLifeArea = "Meaning & Contribution",
            recommendedWakeTime = "06:00",
            recommendedSleepTime = "22:30",
            principles = listOf(
                Principle("pd_p1", "purpose_driven", "Dharma & Duty", "Execute your responsibilities with excellence without obsessing over immediate praise."),
                Principle("pd_p2", "purpose_driven", "Service to Others", "Your true wealth is the positive impact you create in the lives of others.")
            ),
            defaultPractices = listOf(
                Practice("pd_pr1", "pd_p1", "Mission Focus Block", 60, 15, "09:00", TrackingMode.TIMER, true),
                Practice("pd_pr2", "pd_p2", "Contribution / Mentorship", 30, 10, "16:00", TrackingMode.MANUAL, true),
                Practice("pd_pr3", "pd_p1", "Evening Gratitude & Reflection", 10, 3, "21:30", TrackingMode.MANUAL, true)
            )
        ),

        // 9. Custom Life
        PathTemplate(
            path = LifePath("custom_life", "🛠 Custom Life", "Fully personalized path designed by you.", isActive = false),
            primaryLifeArea = "Personalized",
            recommendedWakeTime = "07:00",
            recommendedSleepTime = "23:00",
            principles = listOf(
                Principle("cu_p1", "custom_life", "Self-Determination", "You are the architect of your own habits, principles, and destiny.")
            ),
            defaultPractices = listOf(
                Practice("cu_pr1", "cu_p1", "Custom Focus Session", 45, 15, "09:00", TrackingMode.TIMER, true)
            )
        )
    )

    /**
     * Applies a chosen path template: updates active path, seeds its principles and practices.
     */
    suspend fun applyTemplate(pathId: String) {
        val template = availableTemplates.firstOrNull { it.path.id == pathId } ?: return

        // 1. Set active path in DB
        lifePathRepository.setActiveLifePath(pathId)

        // 2. Clear / Insert default practices for this path
        template.defaultPractices.forEach { practice ->
            practiceRepository.insertPractice(practice)
        }
    }
}
