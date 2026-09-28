package com.vajrax.domain.habit

/**
 * Deterministic category / icon / color for habits that come from the template library,
 * derived from the habit name. Users can override all three when customizing.
 */
object HabitIconResolver {

    data class Look(val category: String, val icon: String, val color: String)

    val categories = listOf(
        "Health", "Fitness", "Mind", "Learning", "Work", "Sleep",
        "Nutrition", "Finance", "Home", "Social", "Growth", "Routine"
    )

    val icons = listOf(
        "check", "sunrise", "droplet", "leaf", "dumbbell", "activity", "utensils", "book",
        "briefcase", "pen", "moon", "clock", "smartphone", "wallet", "home", "heart",
        "zap", "mapPin", "target", "sparkles", "sun", "users"
    )

    val colors = listOf("indigo", "violet", "sky", "teal", "emerald", "amber", "orange", "rose", "slate")

    private data class Rule(val keywords: List<String>, val look: Look)

    private val rules = listOf(
        Rule(listOf("sleep", "bed on time", "lights out"), Look("Sleep", "moon", "indigo")),
        Rule(listOf("water", "hydrat", "drink"), Look("Health", "droplet", "sky")),
        Rule(listOf("wake", "rise", "sunlight", "morning light"), Look("Routine", "sunrise", "amber")),
        Rule(listOf("meditat", "breath", "prayer", "mindful", "gratitude", "stillness", "calm"), Look("Mind", "leaf", "teal")),
        Rule(listOf("journal", "plan", "review", "reflect", "priorit", "write", "writing"), Look("Growth", "pen", "violet")),
        Rule(listOf("workout", "exercise", "gym", "strength", "training", "hiit", "yoga", "cardio", "push"), Look("Fitness", "dumbbell", "orange")),
        Rule(listOf("walk", "run", "jog", "stretch", "mobility", "movement", "steps", "cool down"), Look("Fitness", "activity", "emerald")),
        Rule(listOf("breakfast", "lunch", "dinner", "meal", "protein", "eat", "fruit", "vegetable", "nutrition", "snack"), Look("Nutrition", "utensils", "emerald")),
        Rule(listOf("read", "book", "study", "learn", "course", "revision", "research", "knowledge", "language"), Look("Learning", "book", "indigo")),
        Rule(listOf("deep work", "work", "focus", "project", "email", "career", "meeting", "office", "build"), Look("Work", "briefcase", "slate")),
        Rule(listOf("phone", "screen", "social media", "digital", "detox", "no-screen", "offline"), Look("Mind", "smartphone", "rose")),
        Rule(listOf("money", "expense", "budget", "saving", "invest", "finance", "spend"), Look("Finance", "wallet", "amber")),
        Rule(listOf("clean", "tidy", "organi", "laundry", "room", "declutter", "chores", "home"), Look("Home", "home", "sky")),
        Rule(listOf("family", "friend", "call", "social", "relationship", "partner"), Look("Social", "users", "rose")),
        Rule(listOf("shower", "skincare", "grooming", "self-care", "bath"), Look("Health", "heart", "rose")),
        Rule(listOf("commute", "travel", "podcast"), Look("Routine", "mapPin", "slate")),
        Rule(listOf("challenge", "discipline", "cold"), Look("Growth", "zap", "violet"))
    )

    fun resolve(name: String): Look {
        val lower = name.lowercase()
        return rules.firstOrNull { rule -> rule.keywords.any { lower.contains(it) } }?.look
            ?: Look("Routine", "check", "indigo")
    }
}
