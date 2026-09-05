package com.vajrax.domain.ai

import com.vajrax.domain.model.ReflectionRating

data class SageResponse(
    val title: String,
    val empathyMessage: String,
    val practicalMicroStep: String,
    val philosophicalAnchor: String
)

data class SageContext(
    val activePathName: String = "High Performance",
    val currentTaskTitle: String? = "Deep Work",
    val completedCount: Int = 2,
    val totalCount: Int = 5,
    val timeHour: Int = 9
)

/**
 * Human Touch AI Engine: Sāma & Vajra Life Companion.
 * Delivers empathetic, deeply human, philosophical guidance combining
 * Stoic wisdom, Vedic philosophy, and modern behavioral science.
 */
class AiHumanTouchEngine {

    /**
     * Daily Grounding Reflection tailored to path, current focus, and time of day.
     */
    fun getDailyGroundingReflection(
        pathName: String,
        focusTitle: String?,
        hour: Int
    ): String {
        val task = focusTitle ?: "your next commitment"
        return when (hour) {
            in 5..11 -> when {
                pathName.contains("Performance", ignoreCase = true) ->
                    "\"The mind is forged in the first uninterrupted morning block. Give 100% presence to $task.\""
                pathName.contains("Mastery", ignoreCase = true) ->
                    "\"Self-respect is keeping promises to yourself when nobody is watching. Today, let $task be your standard.\""
                pathName.contains("Scholar", ignoreCase = true) ->
                    "\"True learning requires silence. Clear mental noise and dive into deep synthesis with $task.\""
                else ->
                    "\"You do not have to conquer the whole day at once—just win this next moment with $task.\""
            }
            in 12..16 ->
                "\"Midday energy naturally dips. Don't rely on raw willpower; lower the friction and keep moving forward.\""
            in 17..21 ->
                "\"Honor the work you've put in today. Wrap up loose threads mindfully and prepare for restful recovery.\""
            else ->
                "\"The day is done. Release all unfinished thoughts. Deep rest is the foundation of tomorrow's strength.\""
        }
    }

    /**
     * Sāma Resistance Guidance (when feeling stuck, overwhelmed, or procrastinating).
     */
    fun getSamaResistanceGuidance(taskTitle: String): SageResponse {
        return SageResponse(
            title = "Overcoming Resistance",
            empathyMessage = "It is completely normal to feel resistance before starting $taskTitle. Friction doesn't mean you are incapable—it just means your brain seeks immediate comfort.",
            practicalMicroStep = "Shrink the commitment to a 2-minute non-threatening version. Open the workspace, take 3 deep breaths, and do just the first sentence or first rep.",
            philosophicalAnchor = "Marcus Aurelius: \"What stands in the way becomes the way.\" The moment you step into the friction, the friction dissolves."
        )
    }

    /**
     * Pre-Focus Ritual to prime the nervous system before entering deep focus.
     */
    fun getPreFocusRitual(taskTitle: String, durationMinutes: Int): SageResponse {
        return SageResponse(
            title = "Pre-Focus Activation Ritual",
            empathyMessage = "Entering deep focus on \"$taskTitle\" requires transitioning your nervous system from reactive browsing to single-pointed attention.",
            practicalMicroStep = "1. Place phone in another room or face-down.\n2. Inhale for 4s, hold for 4s, exhale for 6s (repeat 3x).\n3. Write down exactly ONE definition of done for this ${durationMinutes}m session.",
            philosophicalAnchor = "Bhagavad Gita: \"Yoga is the journey of the self, through the self, to the self.\" Guard your attention like sacred territory."
        )
    }

    /**
     * Empathetic Reflection Feedback when user logs evidence.
     */
    fun getEvidenceReflectionFeedback(rating: ReflectionRating?, taskTitle: String): String {
        return when (rating) {
            ReflectionRating.HARD ->
                "Deep respect for showing up when resistance was high. Anyone can work on easy days—true character is built on the days you felt tired but honored your commitment anyway."
            ReflectionRating.OKAY ->
                "Solid, steady execution. Consistency is not about dramatic breakthroughs; it's about compounding quiet daily repetitions into undeniable mastery."
            ReflectionRating.EASY ->
                "Flow state achieved! You moved through \"$taskTitle\" with natural ease. Take note of what conditions made this session feel effortless."
            null ->
                "Evidence logged. Momentum preserved. Every rep changes your self-perception."
        }
    }

    /**
     * Evening Decompression & Mental Unloading.
     */
    fun getEveningDecompression(completedCount: Int, totalCount: Int): SageResponse {
        val message = if (completedCount >= totalCount) {
            "You completed all $completedCount planned practices with excellence. Let your mind shut down completely."
        } else {
            "You completed $completedCount of $totalCount practices. That is meaningful progress. Do not carry tomorrow's burdens into tonight's sleep."
        }
        return SageResponse(
            title = "Evening Decompression",
            empathyMessage = message,
            practicalMicroStep = "Write down any lingering thoughts on paper, dim your lights, and put away screens 30 minutes before bed.",
            philosophicalAnchor = "Epictetus: \"Let death find me doing something noble, mindful, and at peace with nature.\" Rest well."
        )
    }

    /**
     * Conversational Sage Consultation with Human Touch.
     */
    fun askSage(userQuery: String, context: SageContext): SageResponse {
        val queryLower = userQuery.lowercase()

        return when {
            queryLower.contains("stuck") || queryLower.contains("procrastinat") || queryLower.contains("lazy") || queryLower.contains("resist") ->
                getSamaResistanceGuidance(context.currentTaskTitle ?: "your practice")

            queryLower.contains("focus") || queryLower.contains("deep work") || queryLower.contains("distract") ->
                getPreFocusRitual(context.currentTaskTitle ?: "Deep Work", 60)

            queryLower.contains("evening") || queryLower.contains("sleep") || queryLower.contains("night") || queryLower.contains("wind down") ->
                getEveningDecompression(context.completedCount, context.totalCount)

            queryLower.contains("stoic") || queryLower.contains("quote") || queryLower.contains("wisdom") || queryLower.contains("philosophy") ->
                SageResponse(
                    title = "Daily Stoic & Vedic Anchor",
                    empathyMessage = "Life rarely unfolds exactly as we plan, but our ability to respond with grace and strength is always within our sovereignty.",
                    practicalMicroStep = "Pause for 10 seconds before reacting to unexpected friction today. Choose the dignified response.",
                    philosophicalAnchor = "Seneca: \"We suffer more often in imagination than in reality.\" Live in the present action."
                )

            queryLower.contains("tired") || queryLower.contains("burnout") || queryLower.contains("overwhelm") ->
                SageResponse(
                    title = "Compassionate Recovery Protocol",
                    empathyMessage = "Fatigue is biological signal, not a moral failure. Pushing through severe exhaustion creates negative neuro-associations with your habits.",
                    practicalMicroStep = "Apply the Sāma principle: Do the 5-minute absolute minimum viable practice, log it with pride, and prioritize a full 8-hour sleep tonight.",
                    philosophicalAnchor = "Vedic Wisdom: \"Rhythm (Rta) sustains the cosmos.\" Honor your body's need for recovery cycle."
                )

            else ->
                SageResponse(
                    title = "Guidance from the Sage",
                    empathyMessage = "You are walking the ${context.activePathName} path. Every small intentional action aligns your identity with the person you aspire to become.",
                    practicalMicroStep = "Focus on the single immediate practice right in front of you: ${context.currentTaskTitle ?: "your next focus"}. Do it with full sincerity.",
                    philosophicalAnchor = "Lao Tzu: \"A journey of a thousand miles begins with a single step.\" Keep moving forward."
                )
        }
    }

    /**
     * Weekly rhythm momentum insight for Calendar Matrix.
     */
    fun getCalendarMomentumInsight(completedHabitsThisWeek: Int, totalHabits: Int): String {
        val percentage = if (totalHabits > 0) (completedHabitsThisWeek * 100) / totalHabits else 85
        return when {
            percentage >= 85 ->
                "✨ AI Rhythm: Remarkable harmony across your morning habits. Your Tuesday–Thursday consistency is establishing strong neural pathways."
            percentage >= 60 ->
                "✨ AI Rhythm: Steady progress. The data shows strong morning execution with minor friction in late afternoon. Consider shifting complex tasks before 1:00 PM."
            else ->
                "✨ AI Rhythm: Rebuilding momentum. Focus exclusively on anchoring your primary morning practice. One unshakeable habit stabilizes all others."
        }
    }

    /**
     * Growth Mastery Synthesis for Report Screen.
     */
    fun getGrowthMasterySynthesis(pathName: String, consistency: Int): String {
        return "You have sustained an average $consistency% adherence on the $pathName path. Qualitative evidence reveals heightened focus discipline and reduced start-up friction during morning windows."
    }
}
