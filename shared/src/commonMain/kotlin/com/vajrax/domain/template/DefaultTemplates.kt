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
        // ==========================================
        // 15-ARC DAILY ROUTINE TEMPLATES
        // ==========================================

        DefaultTemplate(
            id = "arc_master_daily",
            name = "15-Arc Master Daily Routine",
            category = "Arc",
            description = "Complete self-improvement system covering all 15 life arcs: Winter, Gym, Study, Career, Money, Monk, Spiritual, Health, Knowledge, Discipline, Glow-Up, Reset, Build, Peace, Transformation.",
            difficulty = "Hard",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up + Make Bed + Drink Water", "05:30", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Meditation / Prayer / Breathing", "05:40", 20, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Workout / Running / Mobility", "06:00", 45, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Shower + Grooming", "06:45", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Breakfast", "07:15", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Daily Planning + Top 3 Priorities", "07:35", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Deep Study / Technical Learning", "07:50", 60, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Prepare for Work", "08:50", 25, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Travel / Commute (Knowledge/No Scroll)", "09:15", 45, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), false, 9),
                DefaultHabit("Deep Professional Work", "10:00", 180, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 10),
                DefaultHabit("Lunch + Short Break", "13:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 11),
                DefaultHabit("Focused Work", "13:30", 150, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), false, 12),
                DefaultHabit("Break / Walk / Reset", "16:00", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 13),
                DefaultHabit("Work + Project Execution", "16:15", 165, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), false, 14),
                DefaultHabit("Travel / Decompression", "19:00", 60, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), false, 15),
                DefaultHabit("Dinner", "20:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 16),
                DefaultHabit("Walk / Family / Personal Time", "20:30", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 17),
                DefaultHabit("Side Project / Portfolio (Build Arc)", "21:00", 30, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 18),
                DefaultHabit("Money Check / Expense Tracking", "21:30", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 19),
                DefaultHabit("Reading (Knowledge Arc)", "21:45", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 20),
                DefaultHabit("Daily Review + Tomorrow Plan", "22:00", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 21),
                DefaultHabit("No-Screen Wind-Down (Monk Arc)", "22:15", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 22),
                DefaultHabit("Sleep", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 23),
            )
        ),

        DefaultTemplate(
            id = "arc_winter",
            name = "❄️ Winter Arc — Discipline & Consistency",
            category = "Arc",
            description = "Build iron discipline and consistency. 5:30 AM wake up, workout daily, complete Top 3 priorities, study, sleep by 10:30 PM. Never miss twice.",
            difficulty = "Hard",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up 5:30 AM", "05:30", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Make Bed Immediately", "05:32", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Drink Water", "05:37", 5, TrackingMode.PASSIVE, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Workout (Never Skip)", "06:00", 45, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Top 3 Priorities Planning", "07:35", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Deep Learning Session", "07:50", 60, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Complete Top 3 Priorities", "10:00", 180, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Daily Review — What Did I Execute?", "22:00", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Sleep by 10:30 PM", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 9),
            )
        ),

        DefaultTemplate(
            id = "arc_gym",
            name = "💪 Gym Arc — Strength & Fitness",
            category = "Arc",
            description = "Build strength, fitness and physical consistency. Structured weekly split: upper/lower/cardio. 20–30 min movement even on rest days.",
            difficulty = "Medium",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up + Drink Water", "06:00", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Workout (Upper/Lower/Cardio per Day)", "06:00", 45, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Stretch / Mobility 5–10 Min", "06:45", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Shower + Grooming", "06:55", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Protein / Healthy Breakfast", "07:15", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Healthy Lunch", "13:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Break / Short Walk", "16:00", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Protein / Healthy Dinner", "20:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Sleep (Recovery)", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 9),
            )
        ),

        DefaultTemplate(
            id = "arc_study",
            name = "🧠 Study Arc — Focused Learning",
            category = "Arc",
            description = "Structured academic or technical learning. 45 min focused study + 10 min practice + 5 min review each session. Mon–Thu: new concepts. Fri: revision. Sat: practice. Sun: review.",
            difficulty = "Medium",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up + Morning Routine", "06:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Breakfast", "07:15", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Review Yesterday's Topics (5 min)", "07:45", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Deep Study — 45 Min Focused", "07:50", 45, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Practice / Apply Concepts (10 min)", "08:35", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Notes & Review (5 min)", "08:45", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Study Session 2 (Afternoon)", "13:30", 60, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), false, 7),
                DefaultHabit("Evening Study / Revision", "20:00", 45, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Write Key Learnings", "21:00", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 9),
                DefaultHabit("Sleep", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 10),
            )
        ),

        DefaultTemplate(
            id = "arc_career",
            name = "💻 Career Arc — Professional Growth",
            category = "Arc",
            description = "Accelerate professional capability. Deep work blocks, skill learning, architecture, performance, AI/ML, documentation. Choose one career skill as weekly focus.",
            difficulty = "Medium",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up + Morning Routine", "06:00", 75, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Career Skill Study (Technical)", "07:50", 60, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Deep Professional Work — Block 1", "09:00", 120, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Email / Communication (Batch)", "11:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Deep Professional Work — Block 2", "11:30", 90, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), false, 5),
                DefaultHabit("Lunch Break", "13:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Important Tasks / Meetings", "14:00", 120, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), false, 7),
                DefaultHabit("Break / Walk", "16:00", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Work + Project", "16:15", 165, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), false, 9),
                DefaultHabit("Review Progress + Plan Tomorrow", "17:30", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 10),
                DefaultHabit("Sleep", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 11),
            )
        ),

        DefaultTemplate(
            id = "arc_money",
            name = "💰 Money Arc — Finance & Wealth",
            category = "Arc",
            description = "Control personal finances daily. Record expenses, track spending, review savings. Weekly: spending review + savings rate. Monthly: net-worth update, budget review.",
            difficulty = "Easy",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Check Today's Budget (Morning)", "08:00", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Record Lunch Expenses", "13:30", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), false, 2),
                DefaultHabit("Check Unnecessary Spending", "18:30", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Money Check / Record All Expenses", "21:30", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Review Savings Goal", "21:45", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Review Upcoming Payments", "21:55", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
            )
        ),

        DefaultTemplate(
            id = "arc_monk",
            name = "📵 Monk Arc — Focus & No Distraction",
            category = "Arc",
            description = "Reduce distractions and reclaim attention. No social media at wake-up. No phone during deep work. Batch notifications. Digital shutdown after 10 PM.",
            difficulty = "Medium",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up — No Phone for 30 Min", "05:30", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Morning Routine (Phone Free)", "06:00", 75, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Deep Work Block — Phone Away", "07:50", 60, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Phone-Free Lunch", "13:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Screen Break — Eyes Rest", "15:30", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("No Social Media After 7 PM", "19:00", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Digital Shutdown — No Screens", "22:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Sleep", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
            )
        ),

        DefaultTemplate(
            id = "arc_spiritual",
            name = "🧘 Spiritual Arc — Meditation & Reflection",
            category = "Arc",
            description = "Develop reflection, gratitude and inner discipline. Morning meditation/prayer/breathing. Evening quiet reflection. Choose your practice.",
            difficulty = "Easy",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up + Drink Water", "05:30", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Meditation / Prayer / Breathing", "05:40", 20, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Gratitude Journal (3 Things)", "06:00", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Morning Walk (Mindful)", "06:10", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), false, 4),
                DefaultHabit("Mindfulness Break (Mid-Day)", "13:00", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), false, 5),
                DefaultHabit("Breathing Break (Afternoon)", "15:30", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Walk Without Phone", "20:30", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Quiet Reflection / Prayer", "22:00", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Sleep", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 9),
            )
        ),

        DefaultTemplate(
            id = "arc_health",
            name = "🥗 Health Arc — Nutrition, Sleep & Recovery",
            category = "Arc",
            description = "Support energy, recovery and long-term health. Balanced meals, adequate protein, hydration, daily movement, consistent sleep, limit processed food.",
            difficulty = "Easy",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up + Drink Water (500ml)", "05:30", 5, TrackingMode.PASSIVE, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Morning Movement / Workout", "06:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Healthy Balanced Breakfast", "07:15", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Hydration Check (Mid-Morning)", "10:00", 5, TrackingMode.PASSIVE, "1.0", listOf(1,2,3,4,5,6,7), false, 4),
                DefaultHabit("Balanced Lunch", "13:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Walk / Movement Break", "16:00", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Healthy Dinner", "20:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Walk After Dinner", "20:30", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Recovery Stretch", "22:00", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 9),
                DefaultHabit("Sleep 7–8 Hours", "22:30", 450, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 10),
            )
        ),

        DefaultTemplate(
            id = "arc_knowledge",
            name = "📚 Knowledge Arc — Reading & Broad Learning",
            category = "Arc",
            description = "Become broadly knowledgeable. Read consistently in technology, science, business, history, philosophy. Focus on consistency over page counts.",
            difficulty = "Easy",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Morning Read (10 min)", "06:30", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Commute Learning (Podcast/Article)", "09:15", 45, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), false, 2),
                DefaultHabit("Lunch Break Reading (10 min)", "13:15", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), false, 3),
                DefaultHabit("Evening Learning Session", "20:30", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Night Reading (Tech/Science/Business)", "21:45", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Note Key Insight of the Day", "22:05", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
            )
        ),

        DefaultTemplate(
            id = "arc_discipline",
            name = "🎯 Discipline Arc — Execution & Time Management",
            category = "Arc",
            description = "Execute what you plan. Morning: write Top 3 priorities. Night: review what you completed, what you avoided, what caused distraction.",
            difficulty = "Easy",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up on Schedule", "05:30", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Write Top 3 Priorities", "07:35", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Time-Block Deep Work", "07:50", 60, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Execute Priority #1", "10:00", 90, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Execute Priority #2", "13:30", 90, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), false, 5),
                DefaultHabit("Execute Priority #3", "15:30", 60, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), false, 6),
                DefaultHabit("Daily Review (What Did I Complete?)", "22:00", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Plan Tomorrow", "22:10", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Sleep on Time", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 9),
            )
        ),

        DefaultTemplate(
            id = "arc_glowup",
            name = "✨ Glow-Up Arc — Grooming & Confidence",
            category = "Arc",
            description = "Improve presentation, grooming and confidence. Morning: shower, skincare, hair, clean clothes. Weekly: hair/beard maintenance, nail care, clothing organization.",
            difficulty = "Easy",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up + Drink Water", "06:00", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Workout / Exercise", "06:05", 45, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Shower", "06:50", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Skincare Routine", "07:05", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Hair + Grooming", "07:10", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Wear Clean / Planned Outfit", "07:15", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Healthy Breakfast", "07:20", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Posture Check (Mid-Day)", "12:00", 2, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), false, 8),
                DefaultHabit("Evening Walk (Confidence + Fitness)", "20:30", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 9),
                DefaultHabit("Night Skincare", "22:00", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 10),
                DefaultHabit("Sleep (Beauty Recovery)", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 11),
            )
        ),

        DefaultTemplate(
            id = "arc_reset",
            name = "🌱 Reset Arc — Remove Bad Habits",
            category = "Arc",
            description = "Remove what's holding you back and rebuild. Daily: identify one bad habit to reduce and one good habit to strengthen. Sunday: full weekly reset.",
            difficulty = "Easy",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up + No Phone", "06:00", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Morning Routine (Clean Start)", "06:10", 60, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Identify Today's Bad Habit to Reduce", "07:35", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Identify Good Habit to Strengthen", "07:40", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Avoid Identified Trigger (Track)", "09:00", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), false, 5),
                DefaultHabit("Workspace / Room Tidy", "20:00", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Daily Reset Review", "22:00", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Plan One Fix for Tomorrow", "22:10", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Sleep", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 9),
            )
        ),

        DefaultTemplate(
            id = "arc_build",
            name = "🚀 Build Arc — Projects & Portfolio",
            category = "Arc",
            description = "Build something tangible every week. Android apps, open-source, portfolio, AI tools, automation, side projects. Build > consume. Aim for visible output weekly.",
            difficulty = "Hard",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up + Morning Routine", "06:00", 90, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Technical Learning (Career/Build)", "07:50", 60, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Deep Work Block (Professional)", "09:00", 240, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Lunch Break", "13:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Build Block — Project / Code", "21:00", 30, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Commit / Document Progress", "21:30", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Review Build Output of the Week (Sun)", "21:45", 15, TrackingMode.MANUAL, "1.0", listOf(6,7), true, 7),
                DefaultHabit("Sleep", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
            )
        ),

        DefaultTemplate(
            id = "arc_peace",
            name = "🧘‍♂️ Peace Arc — Mental Space & Calm",
            category = "Arc",
            description = "Protect mental space and reduce unnecessary stress. Quiet time daily, phone-free walks, meaningful time with family/friends, organized workspace.",
            difficulty = "Easy",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up — Slow Start (No Urgency)", "06:00", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("Meditation / Breathing (5–10 min)", "06:05", 10, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Organized Workspace Check", "07:30", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Phone-Free Lunch", "13:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Break / Walk Without Phone", "16:00", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Decompression After Work", "19:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), false, 6),
                DefaultHabit("Walk / Family / Personal Time", "20:30", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Quiet Evening (Low Stimulation)", "22:00", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Sleep (Protect Recovery)", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 9),
            )
        ),

        DefaultTemplate(
            id = "arc_transformation",
            name = "🔥 Transformation Arc — Full Life System",
            category = "Arc",
            description = "Combine the strongest elements of all 15 arcs into a sustainable lifestyle. Transformation is not a separate routine — it is the result of executing the other arcs consistently.",
            difficulty = "Hard",
            estimatedDuration = "Full Day",
            habits = listOf(
                DefaultHabit("Wake Up on Schedule", "05:30", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 1),
                DefaultHabit("No Phone — Make Bed + Water", "05:32", 8, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 2),
                DefaultHabit("Meditation / Breathing", "05:40", 20, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 3),
                DefaultHabit("Workout (Never Skip)", "06:00", 45, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 4),
                DefaultHabit("Shower + Grooming", "06:45", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 5),
                DefaultHabit("Healthy Breakfast", "07:15", 20, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 6),
                DefaultHabit("Top 3 Priorities + Daily Plan", "07:35", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 7),
                DefaultHabit("Deep Learning Session", "07:50", 60, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 8),
                DefaultHabit("Professional Deep Work", "09:00", 240, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 9),
                DefaultHabit("Healthy Lunch", "13:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 10),
                DefaultHabit("Deep Work / Career Priorities", "13:30", 150, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), false, 11),
                DefaultHabit("Movement Break", "16:00", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 12),
                DefaultHabit("Eat Reasonably Well (Dinner)", "20:00", 30, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 13),
                DefaultHabit("Build — Side Project / Portfolio", "21:00", 30, TrackingMode.TIMER, "1.0", listOf(1,2,3,4,5,6,7), true, 14),
                DefaultHabit("Track Money", "21:30", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 15),
                DefaultHabit("Read (Knowledge Arc)", "21:45", 15, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 16),
                DefaultHabit("Limit Distractions Check", "22:00", 5, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 17),
                DefaultHabit("Reflect + Tomorrow Plan", "22:05", 10, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 18),
                DefaultHabit("Sleep on Time", "22:30", 420, TrackingMode.MANUAL, "1.0", listOf(1,2,3,4,5,6,7), true, 19),
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