package com.vajrax.data

import com.vajrax.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import kotlinx.datetime.Clock

interface TaskRepository {
    fun getTasksForToday(): Flow<List<DailyTask>>
    suspend fun toggleTaskCompletion(taskId: String)
    fun getWeekSchedule(startOfWeek: LocalDate): Flow<WeekSchedule>
}

class TaskRepositoryImpl : TaskRepository {
    
    // Expanded mock data to cover the whole week for the Calendar
    private val allTasksFlow = MutableStateFlow(
        listOf(
            DailyTask("1", "Morning Stoic Reflection", false, LocalDate(2026, 9, 21), 15, TaskCategory.SCHOLAR),
            DailyTask("2", "Deep Work: VajraX Architecture", false, LocalDate(2026, 9, 21), 120, TaskCategory.DEEP_WORK),
            DailyTask("3", "Physical Training", true, LocalDate(2026, 9, 22), 45, TaskCategory.HEALTH),
            DailyTask("4", "Read Marcus Aurelius", false, LocalDate(2026, 9, 23), 30, TaskCategory.SCHOLAR),
            DailyTask("5", "Weekly Review", false, LocalDate(2026, 9, 25), 60, TaskCategory.MAINTENANCE)
        )
    )

    override fun getTasksForToday(): Flow<List<DailyTask>> = allTasksFlow.map { it }

    override suspend fun toggleTaskCompletion(taskId: String) {
        val current = allTasksFlow.value
        allTasksFlow.value = current.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    // NEW: Calendar Implementation
    override fun getWeekSchedule(startOfWeek: LocalDate): Flow<WeekSchedule> {
        return allTasksFlow.map { allTasks ->
            // Generate Mon-Fri (5 days)
            val today = LocalDate(2026, 9, 27)
            val days = (0..4).map { daysToAdd ->
                val currentDate = startOfWeek.plus(daysToAdd, DateTimeUnit.DAY)
                val tasksForDay = allTasks.filter { it.scheduledDate == currentDate }
                
                CalendarDay(
                    date = currentDate,
                    dayOfWeek = currentDate.dayOfWeek,
                    dayOfMonth = currentDate.dayOfMonth,
                    tasks = tasksForDay,
                    isToday = currentDate == today
                )
            }
            WeekSchedule(days)
        }
    }
}
