package com.vajrax.domain.template

import com.vajrax.domain.model.TrackingMode

data class DefaultTemplate(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val difficulty: String,
    val estimatedDuration: String,
    val habits: List<DefaultHabit>
)

data class DefaultHabit(
    val name: String,
    val startTime: String,
    val duration: Int,
    val trackingType: TrackingMode,
    val target: String,
    val repeatDays: List<Int>,
    val reminderEnabled: Boolean,
    val sortOrder: Int
)

object TemplateLibrary {
    val defaultTemplates = listOf(
        DefaultTemplate(
            id = "balanced_daily_routine",
            name = "Balanced Daily Routine",
            category = "General",
            description = "Curated Balanced Daily Routine",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "05:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Drink Water",
                    startTime = "05:35",
                    duration = 5,
                    trackingType = TrackingMode.PASSIVE,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Meditation / Breathing",
                    startTime = "05:40",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Exercise / Jogging",
                    startTime = "05:50",
                    duration = 45,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Shower & Get Ready",
                    startTime = "06:20",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Make Bed & Organize Room",
                    startTime = "07:00",
                    duration = 5,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Breakfast",
                    startTime = "07:10",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Reading / Affirmations",
                    startTime = "07:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Prepare for Work",
                    startTime = "07:45",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Commute / Podcast",
                    startTime = "08:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
                DefaultHabit(
                    name = "Water + Mindfulness Break",
                    startTime = "10:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 11
                ),
                DefaultHabit(
                    name = "Healthy Lunch",
                    startTime = "12:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 12
                ),
                DefaultHabit(
                    name = "Deep Work / Priority Task",
                    startTime = "13:00",
                    duration = 60,
                    trackingType = TrackingMode.TIMER,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 13
                ),
                DefaultHabit(
                    name = "Walking Break",
                    startTime = "15:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 14
                ),
                DefaultHabit(
                    name = "Plan Tomorrow's Priorities",
                    startTime = "17:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 15
                ),
                DefaultHabit(
                    name = "Commute / Personal Errands",
                    startTime = "18:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 16
                ),
                DefaultHabit(
                    name = "Dinner",
                    startTime = "19:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 17
                ),
                DefaultHabit(
                    name = "Reading / Learning",
                    startTime = "20:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 18
                ),
                DefaultHabit(
                    name = "Stretching",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 19
                ),
                DefaultHabit(
                    name = "Digital Detox / Wind-down",
                    startTime = "21:15",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 20
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:00",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 21
                ),
            )
        ),
        DefaultTemplate(
            id = "simple_daily_routine",
            name = "Simple Daily Routine",
            category = "General",
            description = "Curated Simple Daily Routine",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "06:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Drink Water",
                    startTime = "06:05",
                    duration = 5,
                    trackingType = TrackingMode.PASSIVE,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Exercise",
                    startTime = "06:15",
                    duration = 45,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Shower",
                    startTime = "06:45",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Breakfast",
                    startTime = "07:15",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Reading",
                    startTime = "07:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Start Work / Commute",
                    startTime = "08:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Drink Water",
                    startTime = "10:30",
                    duration = 5,
                    trackingType = TrackingMode.PASSIVE,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Lunch",
                    startTime = "12:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "15-Minute Walk",
                    startTime = "15:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
                DefaultHabit(
                    name = "Finish Work",
                    startTime = "18:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 11
                ),
                DefaultHabit(
                    name = "Dinner",
                    startTime = "19:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 12
                ),
                DefaultHabit(
                    name = "Personal Learning",
                    startTime = "20:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 13
                ),
                DefaultHabit(
                    name = "Stretching",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 14
                ),
                DefaultHabit(
                    name = "No Phone / Wind-down",
                    startTime = "21:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 15
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:00",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 16
                ),
            )
        ),
        DefaultTemplate(
            id = "30_minute_morning_routine",
            name = "30-Minute Morning Routine",
            category = "General",
            description = "Curated 30-Minute Morning Routine",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "06:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Drink Water",
                    startTime = "06:05",
                    duration = 5,
                    trackingType = TrackingMode.PASSIVE,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Quick Exercise",
                    startTime = "06:10",
                    duration = 45,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Stretching",
                    startTime = "06:20",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Plan the Day",
                    startTime = "06:25",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Start Day",
                    startTime = "06:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
            )
        ),
        DefaultTemplate(
            id = "productive_morning_routine",
            name = "Productive Morning Routine",
            category = "General",
            description = "Curated Productive Morning Routine",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "05:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Drink Water",
                    startTime = "05:35",
                    duration = 5,
                    trackingType = TrackingMode.PASSIVE,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Meditation",
                    startTime = "05:40",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Exercise",
                    startTime = "05:50",
                    duration = 45,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Shower",
                    startTime = "06:20",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Journal",
                    startTime = "06:40",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Read",
                    startTime = "06:50",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Breakfast",
                    startTime = "07:10",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Plan Top 3 Priorities",
                    startTime = "07:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Deep Work",
                    startTime = "08:00",
                    duration = 60,
                    trackingType = TrackingMode.TIMER,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
            )
        ),
        DefaultTemplate(
            id = "night_routine",
            name = "Night Routine",
            category = "General",
            description = "Curated Night Routine",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Finish Dinner",
                    startTime = "20:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Prepare for Tomorrow",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Clean / Organize Room",
                    startTime = "21:15",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Digital Detox",
                    startTime = "21:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Reading",
                    startTime = "21:40",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Meditation / Breathing",
                    startTime = "21:55",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:00",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
            )
        ),
        DefaultTemplate(
            id = "fitness_routine",
            name = "Fitness Routine",
            category = "General",
            description = "Curated Fitness Routine",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "06:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Drink Water",
                    startTime = "06:05",
                    duration = 5,
                    trackingType = TrackingMode.PASSIVE,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Warm-up",
                    startTime = "06:15",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Cardio",
                    startTime = "06:25",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Strength Training",
                    startTime = "06:45",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Stretching",
                    startTime = "07:10",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Shower",
                    startTime = "07:20",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Healthy Breakfast",
                    startTime = "07:40",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Healthy Lunch",
                    startTime = "12:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Protein / Healthy Snack",
                    startTime = "16:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
                DefaultHabit(
                    name = "Light Dinner",
                    startTime = "19:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 11
                ),
                DefaultHabit(
                    name = "Recovery Stretch",
                    startTime = "21:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 12
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:00",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 13
                ),
            )
        ),
        DefaultTemplate(
            id = "healthy_lifestyle",
            name = "Healthy Lifestyle",
            category = "General",
            description = "Curated Healthy Lifestyle",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "06:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Drink Water",
                    startTime = "06:05",
                    duration = 5,
                    trackingType = TrackingMode.PASSIVE,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Morning Walk",
                    startTime = "06:15",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Stretching",
                    startTime = "06:45",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Healthy Breakfast",
                    startTime = "07:15",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Water Break",
                    startTime = "10:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Balanced Lunch",
                    startTime = "12:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Fruit / Healthy Snack",
                    startTime = "15:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Short Walk",
                    startTime = "17:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Light Dinner",
                    startTime = "19:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
                DefaultHabit(
                    name = "Wind-down",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 11
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:00",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 12
                ),
            )
        ),
        DefaultTemplate(
            id = "mindfulness_mental_wellness",
            name = "Mindfulness & Mental Wellness",
            category = "General",
            description = "Curated Mindfulness & Mental Wellness",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "06:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Deep Breathing",
                    startTime = "06:10",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Meditation",
                    startTime = "06:20",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Gratitude Journal",
                    startTime = "06:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Morning Walk",
                    startTime = "07:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Mindfulness Break",
                    startTime = "10:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Lunch Without Phone",
                    startTime = "13:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Breathing Break",
                    startTime = "15:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Outdoor Walk",
                    startTime = "18:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Reflection",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
                DefaultHabit(
                    name = "Digital Detox",
                    startTime = "21:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 11
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:00",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 12
                ),
            )
        ),
        DefaultTemplate(
            id = "student_study_routine",
            name = "Student Study Routine",
            category = "General",
            description = "Curated Student Study Routine",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "06:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Exercise",
                    startTime = "06:15",
                    duration = 45,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Shower",
                    startTime = "06:45",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Breakfast",
                    startTime = "07:15",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Review Yesterday's Topics",
                    startTime = "07:45",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Study Session 1",
                    startTime = "09:00",
                    duration = 60,
                    trackingType = TrackingMode.TIMER,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Break",
                    startTime = "11:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Study Session 2",
                    startTime = "11:15",
                    duration = 60,
                    trackingType = TrackingMode.TIMER,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Lunch",
                    startTime = "13:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Study Session 3",
                    startTime = "14:00",
                    duration = 60,
                    trackingType = TrackingMode.TIMER,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
                DefaultHabit(
                    name = "Break / Walk",
                    startTime = "16:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 11
                ),
                DefaultHabit(
                    name = "Practice / Assignments",
                    startTime = "17:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 12
                ),
                DefaultHabit(
                    name = "Dinner",
                    startTime = "19:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 13
                ),
                DefaultHabit(
                    name = "Revision",
                    startTime = "20:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 14
                ),
                DefaultHabit(
                    name = "Plan Tomorrow",
                    startTime = "21:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 15
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:30",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 16
                ),
            )
        ),
        DefaultTemplate(
            id = "work_productivity_routine",
            name = "Work Productivity Routine",
            category = "General",
            description = "Curated Work Productivity Routine",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "06:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Exercise",
                    startTime = "06:15",
                    duration = 45,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Breakfast",
                    startTime = "07:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Plan Work Priorities",
                    startTime = "07:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Deep Work",
                    startTime = "09:00",
                    duration = 60,
                    trackingType = TrackingMode.TIMER,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Email / Communication",
                    startTime = "11:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Deep Work",
                    startTime = "11:30",
                    duration = 60,
                    trackingType = TrackingMode.TIMER,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Lunch",
                    startTime = "13:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Important Tasks",
                    startTime = "14:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Break / Walk",
                    startTime = "16:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
                DefaultHabit(
                    name = "Meetings / Collaboration",
                    startTime = "16:15",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 11
                ),
                DefaultHabit(
                    name = "Review Progress",
                    startTime = "17:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 12
                ),
                DefaultHabit(
                    name = "Plan Tomorrow",
                    startTime = "18:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 13
                ),
                DefaultHabit(
                    name = "Finish Work",
                    startTime = "19:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 14
                ),
            )
        ),
        DefaultTemplate(
            id = "reading_learning",
            name = "Reading & Learning",
            category = "General",
            description = "Curated Reading & Learning",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Read 10 Minutes",
                    startTime = "06:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Learning / Course",
                    startTime = "07:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Read During Lunch",
                    startTime = "12:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Learning Session",
                    startTime = "17:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Read 20 Minutes",
                    startTime = "20:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Write Key Learnings",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
            )
        ),
        DefaultTemplate(
            id = "digital_detox",
            name = "Digital Detox",
            category = "General",
            description = "Curated Digital Detox",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "No Phone for 30 Minutes",
                    startTime = "06:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Phone-Free Breakfast",
                    startTime = "07:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "10-Minute Screen Break",
                    startTime = "10:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Phone-Free Lunch",
                    startTime = "13:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Screen Break",
                    startTime = "15:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "No Social Media",
                    startTime = "19:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Digital Detox",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Reading",
                    startTime = "21:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:00",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
            )
        ),
        DefaultTemplate(
            id = "personal_finance",
            name = "Personal Finance",
            category = "General",
            description = "Curated Personal Finance",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Check Today's Budget",
                    startTime = "08:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Record Expenses",
                    startTime = "13:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Review Spending",
                    startTime = "18:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Update Expense Tracker",
                    startTime = "20:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Review Savings Goal",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
            )
        ),
        DefaultTemplate(
            id = "home_personal_organization",
            name = "Home & Personal Organization",
            category = "General",
            description = "Curated Home & Personal Organization",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Make Bed",
                    startTime = "06:30",
                    duration = 5,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Organize Room",
                    startTime = "06:40",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Prepare Clothes",
                    startTime = "07:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Clean Workspace",
                    startTime = "19:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Organize Personal Items",
                    startTime = "20:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Prepare Tomorrow's Items",
                    startTime = "20:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "10-Minute Home Reset",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
            )
        ),
        DefaultTemplate(
            id = "personal_growth",
            name = "Personal Growth",
            category = "General",
            description = "Curated Personal Growth",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "06:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Meditation",
                    startTime = "06:10",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Exercise",
                    startTime = "06:20",
                    duration = 45,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Read",
                    startTime = "07:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Daily Affirmations",
                    startTime = "07:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Skill Development",
                    startTime = "13:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Learn Something New",
                    startTime = "18:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Personal Reflection",
                    startTime = "20:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Journal",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Plan Tomorrow",
                    startTime = "21:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
            )
        ),
        DefaultTemplate(
            id = "5_am_early_riser_routine",
            name = "5 AM Early-Riser Routine",
            category = "General",
            description = "Curated 5 AM Early-Riser Routine",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "05:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Drink Water",
                    startTime = "05:05",
                    duration = 5,
                    trackingType = TrackingMode.PASSIVE,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Meditation",
                    startTime = "05:10",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Exercise",
                    startTime = "05:20",
                    duration = 45,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Shower",
                    startTime = "05:50",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Reading",
                    startTime = "06:10",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Journal",
                    startTime = "06:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Plan Day",
                    startTime = "06:45",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Breakfast",
                    startTime = "07:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:00",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
            )
        ),
        DefaultTemplate(
            id = "weekend_routine",
            name = "Weekend Routine",
            category = "General",
            description = "Curated Weekend Routine",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "07:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Drink Water",
                    startTime = "07:05",
                    duration = 5,
                    trackingType = TrackingMode.PASSIVE,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Make Bed",
                    startTime = "07:15",
                    duration = 5,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Outdoor Walk",
                    startTime = "07:20",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Breakfast",
                    startTime = "07:45",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Reading",
                    startTime = "08:15",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Personal Project",
                    startTime = "09:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Household Tasks",
                    startTime = "11:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Lunch",
                    startTime = "13:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Hobby / Learning",
                    startTime = "15:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
                DefaultHabit(
                    name = "Outdoor Activity",
                    startTime = "17:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 11
                ),
                DefaultHabit(
                    name = "Dinner",
                    startTime = "19:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 12
                ),
                DefaultHabit(
                    name = "Relax / Entertainment",
                    startTime = "20:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 13
                ),
                DefaultHabit(
                    name = "Stretch + Wind-down",
                    startTime = "21:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 14
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:30",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 15
                ),
            )
        ),
        DefaultTemplate(
            id = "21_day_discipline_challenge",
            name = "21-Day Discipline Challenge",
            category = "General",
            description = "Curated 21-Day Discipline Challenge",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "05:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Make Bed",
                    startTime = "05:35",
                    duration = 5,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Drink Water",
                    startTime = "05:40",
                    duration = 5,
                    trackingType = TrackingMode.PASSIVE,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Exercise",
                    startTime = "05:45",
                    duration = 45,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Meditation",
                    startTime = "06:15",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Cold / Normal Shower",
                    startTime = "06:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Healthy Breakfast",
                    startTime = "07:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Plan Top 3 Tasks",
                    startTime = "08:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Healthy Lunch",
                    startTime = "12:30",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Walk",
                    startTime = "15:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
                DefaultHabit(
                    name = "Reading",
                    startTime = "20:00",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 11
                ),
                DefaultHabit(
                    name = "Journal",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 12
                ),
                DefaultHabit(
                    name = "No Phone",
                    startTime = "21:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 13
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:00",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 14
                ),
            )
        ),
        DefaultTemplate(
            id = "30_day_personal_growth_challenge",
            name = "30-Day Personal Growth Challenge",
            category = "General",
            description = "Curated 30-Day Personal Growth Challenge",
            difficulty = "Medium",
            estimatedDuration = "1 Day",
            habits = listOf(
                DefaultHabit(
                    name = "Wake Up",
                    startTime = "06:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 1
                ),
                DefaultHabit(
                    name = "Meditation",
                    startTime = "06:10",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 2
                ),
                DefaultHabit(
                    name = "Exercise",
                    startTime = "06:20",
                    duration = 45,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 3
                ),
                DefaultHabit(
                    name = "Reading",
                    startTime = "06:50",
                    duration = 30,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 4
                ),
                DefaultHabit(
                    name = "Affirmations",
                    startTime = "07:15",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 5
                ),
                DefaultHabit(
                    name = "Plan Goals",
                    startTime = "07:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 6
                ),
                DefaultHabit(
                    name = "Skill Learning",
                    startTime = "13:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 7
                ),
                DefaultHabit(
                    name = "Practice Skill",
                    startTime = "18:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 8
                ),
                DefaultHabit(
                    name = "Reflection",
                    startTime = "20:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 9
                ),
                DefaultHabit(
                    name = "Journal",
                    startTime = "20:30",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 10
                ),
                DefaultHabit(
                    name = "Tomorrow Planning",
                    startTime = "21:00",
                    duration = 15,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 11
                ),
                DefaultHabit(
                    name = "Sleep",
                    startTime = "22:00",
                    duration = 480,
                    trackingType = TrackingMode.MANUAL,
                    target = "1.0",
                    repeatDays = listOf(1,2,3,4,5,6,7),
                    reminderEnabled = true,
                    sortOrder = 12
                ),
            )
        ),
        DefaultTemplate(
            id = "blank_custom_template",
            name = "Blank Custom Template",
            category = "Custom",
            description = "Create your own personalized routine from scratch.",
            difficulty = "Any",
            estimatedDuration = "Custom",
            habits = listOf(
            )
        ),
    )
}