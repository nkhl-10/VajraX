package com.vajrax.data.local

import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.ReflectionRating
import com.vajrax.domain.model.TrackingMode

/**
 * Global Database Seeder for Lumina Life OS (VAJRAX).
 * Seeds User profile, Provided/Community Templates, Active User Template progress,
 * and multi-tradition Practices and Action records.
 */
class DatabaseSeeder(
    private val database: VajraDatabase
) {
    private val queries = database.vajraDatabaseQueries

    fun seedInitialDataIfEmpty(todayDate: String = "2026-08-30") {
        val existingUser = queries.getCurrentUser().executeAsOneOrNull()
        if (existingUser != null) return

        database.transaction {
            // 1. Initial User Session (matching Figma profile)
            queries.insertOrUpdateUser(
                userId = "user_lumina_01",
                email = "john@example.com",
                displayName = "John Doe",
                isGuest = 0L,
                activePathId = "morning_discipline",
                createdAt = "2026-08-30T00:00:00Z"
            )

            // 2. Templates (Provided & Community from Figma)
            queries.insertTemplate(
                id = "morning_discipline",
                title = "Morning Discipline",
                description = "Build a structured morning routine",
                taskCount = 6L,
                frequency = "Daily",
                author = null,
                isCommunity = 0L,
                isBookmarked = 0L
            )
            queries.insertTemplate(
                id = "deep_work_block",
                title = "Deep Work Block",
                description = "Lock in focus and maximize high-output hours",
                taskCount = 4L,
                frequency = "Daily",
                author = null,
                isCommunity = 0L,
                isBookmarked = 0L
            )
            queries.insertTemplate(
                id = "30_day_challenge",
                title = "30-Day Challenge",
                description = "A rigorous month-long discipline protocol",
                taskCount = 8L,
                frequency = "30 days",
                author = null,
                isCommunity = 0L,
                isBookmarked = 0L
            )
            queries.insertTemplate(
                id = "6am_routine",
                title = "6 AM Routine",
                description = "Wake up early and win the morning",
                taskCount = 5L,
                frequency = "Daily",
                author = null,
                isCommunity = 0L,
                isBookmarked = 0L
            )
            queries.insertTemplate(
                id = "evening_wind_down",
                title = "Evening Wind Down",
                description = "Slow down your mind for deep, restful recovery",
                taskCount = 4L,
                frequency = "Daily",
                author = "sarah",
                isCommunity = 1L,
                isBookmarked = 1L
            )
            queries.insertTemplate(
                id = "fitness_starter_pack",
                title = "Fitness Starter Pack",
                description = "Essential daily habits for athletic consistency",
                taskCount = 5L,
                frequency = "Daily",
                author = "mike",
                isCommunity = 1L,
                isBookmarked = 1L
            )

            // 3. User Enrolled Template Progress (Figma Profile: Morning Discipline, Day 12 of 30, 40%)
            queries.insertOrUpdateUserTemplate(
                id = "ut_01",
                userId = "user_lumina_01",
                templateId = "morning_discipline",
                currentDay = 12L,
                totalDays = 30L,
                progressPercent = 40L,
                isActive = 1L
            )

            // 4. Global Life Paths (9 Paths)
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

            // 5. Multi-Tradition Principles for High Performance Path
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

            // 6. 8 System Practices (Matching Calendar & Timeline)
            queries.insertPractice(
                id = "prac_1",
                principleId = "p3",
                title = "Daily Review",
                targetDurationMinutes = 15,
                minimumDurationMinutes = 5,
                preferredTime = "06:30 AM",
                trackingMode = TrackingMode.MANUAL.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_2",
                principleId = "p2",
                title = "Deep Work",
                targetDurationMinutes = 60,
                minimumDurationMinutes = 15,
                preferredTime = "09:00 - 10:00 AM",
                trackingMode = TrackingMode.TIMER.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_3",
                principleId = "p3",
                title = "Lunch Walk",
                targetDurationMinutes = 30,
                minimumDurationMinutes = 10,
                preferredTime = "12:30 - 01:00 PM",
                trackingMode = TrackingMode.PASSIVE.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_4",
                principleId = "p1",
                title = "Learning",
                targetDurationMinutes = 60,
                minimumDurationMinutes = 15,
                preferredTime = "03:00 - 04:00 PM",
                trackingMode = TrackingMode.MANUAL.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_5",
                principleId = "p3",
                title = "Workout",
                targetDurationMinutes = 60,
                minimumDurationMinutes = 20,
                preferredTime = "06:00 - 07:00 PM",
                trackingMode = TrackingMode.MANUAL.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_6",
                principleId = "p1",
                title = "Reading",
                targetDurationMinutes = 30,
                minimumDurationMinutes = 10,
                preferredTime = "08:30 - 09:00 PM",
                trackingMode = TrackingMode.MANUAL.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_7",
                principleId = "p1",
                title = "Plan Tomorrow",
                targetDurationMinutes = 15,
                minimumDurationMinutes = 5,
                preferredTime = "09:30 - 09:45 PM",
                trackingMode = TrackingMode.MANUAL.name,
                isActive = 1L
            )
            queries.insertPractice(
                id = "prac_8",
                principleId = "p3",
                title = "Sleep",
                targetDurationMinutes = 480,
                minimumDurationMinutes = 360,
                preferredTime = "10:30 PM",
                trackingMode = TrackingMode.PASSIVE.name,
                isActive = 1L
            )

            // 7. Initial Action Records for Today
            queries.insertActionRecord(
                id = "act_1",
                practiceId = "prac_1",
                date = todayDate,
                scheduledTime = "06:30 AM",
                status = ActionStatus.COMPLETE.name,
                completedAt = "${todayDate}T06:45:00Z",
                durationMinutes = 15
            )
            queries.insertActionRecord(
                id = "act_2",
                practiceId = "prac_2",
                date = todayDate,
                scheduledTime = "09:00 - 10:00 AM",
                status = ActionStatus.COMPLETE.name,
                completedAt = "${todayDate}T10:00:00Z",
                durationMinutes = 60
            )
            queries.insertActionRecord(
                id = "act_3",
                practiceId = "prac_3",
                date = todayDate,
                scheduledTime = "12:30 - 01:00 PM",
                status = ActionStatus.COMPLETE.name,
                completedAt = "${todayDate}T01:00:00Z",
                durationMinutes = 30
            )
            queries.insertActionRecord(
                id = "act_4",
                practiceId = "prac_4",
                date = todayDate,
                scheduledTime = "03:00 - 04:00 PM",
                status = ActionStatus.COMPLETE.name,
                completedAt = "${todayDate}T04:00:00Z",
                durationMinutes = 60
            )
            queries.insertActionRecord(
                id = "act_5",
                practiceId = "prac_5",
                date = todayDate,
                scheduledTime = "06:00 - 07:00 PM",
                status = ActionStatus.COMPLETE.name,
                completedAt = "${todayDate}T07:00:00Z",
                durationMinutes = 60
            )
            queries.insertActionRecord(
                id = "act_6",
                practiceId = "prac_6",
                date = todayDate,
                scheduledTime = "08:30 - 09:00 PM",
                status = ActionStatus.COMPLETE.name,
                completedAt = "${todayDate}T09:00:00Z",
                durationMinutes = 30
            )
            queries.insertActionRecord(
                id = "act_7",
                practiceId = "prac_7",
                date = todayDate,
                scheduledTime = "09:30 - 09:45 PM",
                status = ActionStatus.COMPLETE.name,
                completedAt = "${todayDate}T09:45:00Z",
                durationMinutes = 15
            )
            queries.insertActionRecord(
                id = "act_8",
                practiceId = "prac_8",
                date = todayDate,
                scheduledTime = "10:30 PM",
                status = ActionStatus.COMPLETE.name,
                completedAt = "${todayDate}T10:30:00Z",
                durationMinutes = 480
            )

            // 8. Evidence Log
            queries.insertEvidence(
                id = "ev_1",
                actionRecordId = "act_1",
                reflectionRating = ReflectionRating.EASY.name,
                note = "Clear mind, ready for high output today.",
                mediaPath = null,
                createdAt = "${todayDate}T06:46:00Z"
            )
            queries.insertEvidence(
                id = "ev_2",
                actionRecordId = "act_2",
                reflectionRating = ReflectionRating.HARD.name,
                note = "Deep work block completed without interruptions.",
                mediaPath = null,
                createdAt = "${todayDate}T10:01:00Z"
            )

            // 9. Book Ideas (LEARN)
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

            // 10. Pattern Insight (REVIEW)
            queries.insertPatternInsight(
                id = "pattern_1",
                title = "Circadian Energy Pattern",
                description = "Focus completion is 31% higher before 1:00 PM.",
                recommendation = "Shift high-friction work to the morning window.",
                isApplied = 0L,
                createdAt = "${todayDate}T00:00:00Z"
            )
        }
    }
}
