package com.vajrax.data.local

import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.ReflectionRating
import com.vajrax.domain.model.TrackingMode

/**
 * Global Database Seeder for VAJRAX (International Market).
 * Seeds 9 Global Paths (High Performance, Self-Mastery, Scholar, Wealth, Balanced, Creator, Mindful, Purpose, Custom)
 * with principles from Stoicism, Modern Behavioral Science, Vedic philosophy, Buddhism, and Leadership.
 */
class DatabaseSeeder(
    private val database: VajraDatabase
) {
    private val queries = database.vajraDatabaseQueries

    fun seedInitialDataIfEmpty(todayDate: String = "2026-08-30") {
        val existingUser = queries.getCurrentUser().executeAsOneOrNull()
        if (existingUser != null) return

        database.transaction {
            // 1. Test User Session
            queries.insertOrUpdateUser(
                userId = "user_admin_01",
                email = "admin@gmail.co",
                displayName = "Vajra User",
                isGuest = 0L,
                activePathId = "high_performance",
                createdAt = "2026-08-30T00:00:00Z"
            )

            // 2. Global Life Paths (9 Paths)
            queries.insertLifePath(
                id = "high_performance",
                name = "High Performance",
                description = "Deep focus, physical vigor, competence, and relentless execution.",
                isActive = 1L
            )
            queries.insertLifePath(
                id = "self_mastery",
                name = "Self-Mastery",
                description = "Self-respect, emotional discipline, strong boundaries, and autonomy.",
                isActive = 0L
            )
            queries.insertLifePath(
                id = "scholar",
                name = "Scholar",
                description = "Reading, rigorous learning, intellectual depth, and critical thinking.",
                isActive = 0L
            )
            queries.insertLifePath(
                id = "wealth_builder",
                name = "Wealth Builder",
                description = "High-value skills, career growth, financial discipline, and long-term leverage.",
                isActive = 0L
            )
            queries.insertLifePath(
                id = "balanced_life",
                name = "Balanced Life",
                description = "Holistic health, deep relationships, work-life equilibrium, and calmness.",
                isActive = 0L
            )
            queries.insertLifePath(
                id = "creator",
                name = "Creator",
                description = "Deep craft, consistent creative output, focus blocks, and experimentation.",
                isActive = 0L
            )
            queries.insertLifePath(
                id = "mindful_life",
                name = "Mindful Life",
                description = "Awareness, emotional regulation, presence, and intentional reflection.",
                isActive = 0L
            )
            queries.insertLifePath(
                id = "purpose_driven",
                name = "Purpose-Driven",
                description = "Core values, duty, community contribution, and meaningful impact.",
                isActive = 0L
            )
            queries.insertLifePath(
                id = "custom_life",
                name = "Custom Architecture",
                description = "Fully personalized path designed by you.",
                isActive = 0L
            )

            // 3. Multi-Tradition Principles for High Performance Path
            queries.insertPrinciple(
                id = "p1",
                lifePathId = "high_performance",
                title = "Dichotomy of Control (Stoicism)",
                description = "Direct 100% of your energy to your own actions and decisions. Ignore what is outside your control."
            )
            queries.insertPrinciple(
                id = "p2",
                lifePathId = "high_performance",
                title = "Deep Work Block (Behavioral Science)",
                description = "High output requires uninterrupted focus. Guard your deep work blocks from cognitive fragmentation."
            )
            queries.insertPrinciple(
                id = "p3",
                lifePathId = "high_performance",
                title = "Physical Primacy (Health & Physiology)",
                description = "A sharp mind requires a resilient body. Movement and recovery are non-negotiable fundamentals."
            )

            // 4. Initial 5 System Practices
            queries.insertPractice(
                id = "prac_1",
                principleId = "p3",
                title = "Morning Exercise",
                targetDurationMinutes = 45,
                minimumDurationMinutes = 15,
                preferredTime = "06:30",
                trackingMode = TrackingMode.MANUAL.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_2",
                principleId = "p1",
                title = "Daily Reading",
                targetDurationMinutes = 20,
                minimumDurationMinutes = 5,
                preferredTime = "08:00",
                trackingMode = TrackingMode.MANUAL.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_3",
                principleId = "p2",
                title = "Deep Work",
                targetDurationMinutes = 60,
                minimumDurationMinutes = 15,
                preferredTime = "09:00",
                trackingMode = TrackingMode.TIMER.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_4",
                principleId = "p3",
                title = "Movement Break",
                targetDurationMinutes = 15,
                minimumDurationMinutes = 5,
                preferredTime = "12:30",
                trackingMode = TrackingMode.PASSIVE.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_5",
                principleId = "p1",
                title = "Digital Shutdown",
                targetDurationMinutes = 30,
                minimumDurationMinutes = 10,
                preferredTime = "21:30",
                trackingMode = TrackingMode.MANUAL.name,
                isActive = 1L
            )

            // 5. Initial Action Records
            queries.insertActionRecord(
                id = "act_1",
                practiceId = "prac_1",
                date = todayDate,
                scheduledTime = "06:30",
                status = ActionStatus.COMPLETE.name,
                completedAt = "${todayDate}T07:15:00Z",
                durationMinutes = 45
            )
            queries.insertActionRecord(
                id = "act_2",
                practiceId = "prac_2",
                date = todayDate,
                scheduledTime = "08:00",
                status = ActionStatus.COMPLETE.name,
                completedAt = "${todayDate}T08:20:00Z",
                durationMinutes = 20
            )
            queries.insertActionRecord(
                id = "act_3",
                practiceId = "prac_3",
                date = todayDate,
                scheduledTime = "09:00",
                status = ActionStatus.ONGOING.name,
                completedAt = null,
                durationMinutes = null
            )
            queries.insertActionRecord(
                id = "act_4",
                practiceId = "prac_4",
                date = todayDate,
                scheduledTime = "12:30",
                status = ActionStatus.PENDING.name,
                completedAt = null,
                durationMinutes = null
            )
            queries.insertActionRecord(
                id = "act_5",
                practiceId = "prac_5",
                date = todayDate,
                scheduledTime = "21:30",
                status = ActionStatus.PENDING.name,
                completedAt = null,
                durationMinutes = null
            )

            // 6. Evidence Log
            queries.insertEvidence(
                id = "ev_1",
                actionRecordId = "act_1",
                reflectionRating = ReflectionRating.HARD.name,
                note = "Tired upon waking, but honored commitment. 45 min completed.",
                mediaPath = null,
                createdAt = "${todayDate}T07:16:00Z"
            )
            queries.insertEvidence(
                id = "ev_2",
                actionRecordId = "act_2",
                reflectionRating = ReflectionRating.EASY.name,
                note = "Read Marcus Aurelius. Practiced pause before responding.",
                mediaPath = null,
                createdAt = "${todayDate}T08:21:00Z"
            )

            // 7. Multi-Tradition Book Ideas (LEARN)
            queries.insertBookIdea(
                id = "idea_1",
                bookTitle = "Deep Work",
                author = "Cal Newport (Behavior Science)",
                idea = "High-Quality Output = (Time Spent) x (Intensity of Focus).",
                practicalApplication = "Phone outside workspace during morning deep work block.",
                isExperimentActive = 1L,
                experimentDaysLeft = 4,
                createdAt = "${todayDate}T00:00:00Z"
            )
            queries.insertBookIdea(
                id = "idea_2",
                bookTitle = "Atomic Habits",
                author = "James Clear (Behavior Psychology)",
                idea = "You do not rise to the level of your goals. You fall to the level of your systems.",
                practicalApplication = "Establish a 2-minute minimum viable version for every critical habit.",
                isExperimentActive = 0L,
                experimentDaysLeft = null,
                createdAt = "${todayDate}T00:00:00Z"
            )
            queries.insertBookIdea(
                id = "idea_3",
                bookTitle = "Meditations",
                author = "Marcus Aurelius (Stoic Philosophy)",
                idea = "You have power over your mind - not outside events. Realize this, and you will find strength.",
                practicalApplication = "10-second pause before responding to stressful communication.",
                isExperimentActive = 0L,
                experimentDaysLeft = null,
                createdAt = "${todayDate}T00:00:00Z"
            )

            // 8. Pattern Insight (REVIEW)
            queries.insertPatternInsight(
                id = "pattern_1",
                title = "Circadian Energy Pattern",
                description = "Focus completion is 31% higher before 1:00 PM. Evening focus blocks fail 60% of the time.",
                recommendation = "Shift high-friction work to the morning window.",
                isApplied = 0L,
                createdAt = "${todayDate}T00:00:00Z"
            )
        }
    }
}
