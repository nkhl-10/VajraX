package com.vajrax.domain.learn

import com.vajrax.domain.model.Practice
import com.vajrax.domain.model.TrackingMode
import com.vajrax.domain.repository.LearnRepository
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.ui.features.learn.BookIdea

/**
 * Phase 15: Book → Life Knowledge-to-Action Manager.
 * Converts theoretical ideas from books/philosophy into concrete 7-day experiments.
 */
class BookToLifeManager(
    private val learnRepository: LearnRepository,
    private val practiceRepository: PracticeRepository
) {

    val curatedLibrary: List<BookIdea> = listOf(
        BookIdea(
            id = "lib_1",
            bookTitle = "Deep Work",
            author = "Cal Newport (Behavior Science)",
            idea = "High-Quality Output = (Time Spent) x (Intensity of Focus).",
            practicalApplication = "Phone outside workspace during 60 min morning focus block.",
            isExperimentActive = true,
            experimentDaysLeft = 4
        ),
        BookIdea(
            id = "lib_2",
            bookTitle = "Atomic Habits",
            author = "James Clear (Behavioral Psychology)",
            idea = "You do not rise to the level of your goals. You fall to the level of your systems.",
            practicalApplication = "Establish a 2-minute minimum viable version for every critical habit.",
            isExperimentActive = false
        ),
        BookIdea(
            id = "lib_3",
            bookTitle = "Meditations",
            author = "Marcus Aurelius (Stoic Philosophy)",
            idea = "You have power over your mind - not outside events. Realize this, and you will find strength.",
            practicalApplication = "10-second mental pause before responding to reactive communication.",
            isExperimentActive = false
        ),
        BookIdea(
            id = "lib_4",
            bookTitle = "Essentialism",
            author = "Greg McKeown (Focus & Execution)",
            idea = "If it isn't a clear yes, then it's a clear no.",
            practicalApplication = "Audit daily schedule and eliminate one non-essential obligation.",
            isExperimentActive = false
        ),
        BookIdea(
            id = "lib_5",
            bookTitle = "The Psychology of Money",
            author = "Morgan Housel (Financial Behavior)",
            idea = "Controlling your time is the highest dividend money pays.",
            practicalApplication = "Weekly 15-minute conscious cashflow review.",
            isExperimentActive = false
        ),
        BookIdea(
            id = "lib_6",
            bookTitle = "Man's Search for Meaning",
            author = "Viktor Frankl (Existential Psychology)",
            idea = "Between stimulus and response there is a space. In that space is our power to choose our response.",
            practicalApplication = "Evening 5-minute micro-reflection on personal responses to friction.",
            isExperimentActive = false
        )
    )

    /**
     * Converts a book idea into an active 7-day experiment and injects it as a temporary practice.
     */
    suspend fun launch7DayExperiment(
        ideaId: String,
        customActionText: String,
        targetDurationMinutes: Int = 20
    ) {
        val minimumMinutes = if (targetDurationMinutes >= 15) targetDurationMinutes / 3 else targetDurationMinutes

        // 1. Create executable practice in user's active system
        val experimentPractice = Practice(
            id = "exp_${ideaId}_${kotlin.random.Random.nextInt(1000)}",
            principleId = null,
            title = "[7-Day Exp] $customActionText",
            targetDurationMinutes = targetDurationMinutes,
            minimumDurationMinutes = minimumMinutes,
            preferredTime = "08:30",
            trackingMode = TrackingMode.MANUAL,
            isActive = true
        )
        practiceRepository.insertPractice(experimentPractice)

        // 2. Update Book Idea state in database
        learnRepository.launchExperiment(ideaId, customActionText, 7)
    }

    /**
     * Concludes a 7-day experiment. If kept, converts it into a permanent practice.
     */
    suspend fun concludeExperiment(ideaId: String, practiceId: String, keepPermanently: Boolean) {
        if (!keepPermanently) {
            practiceRepository.togglePracticeActive(practiceId, false)
        }
        learnRepository.launchExperiment(ideaId, "", 0)
    }
}
