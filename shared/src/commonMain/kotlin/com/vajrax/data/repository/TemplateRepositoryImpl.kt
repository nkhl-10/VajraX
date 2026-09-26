package com.vajrax.data.repository

import com.vajrax.data.local.VajraDatabase
import com.vajrax.domain.model.TrackingMode
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.template.DefaultHabit
import com.vajrax.domain.template.DefaultTemplate

class TemplateRepositoryImpl(
    private val database: VajraDatabase
) : TemplateRepository {
    private val queries = database.vajraDatabaseQueries

    override suspend fun getAllTemplates(): List<DefaultTemplate> {
        return queries.getAllTemplates().executeAsList().map { entity ->
            getTemplateWithHabits(entity.id) ?: throw IllegalStateException("Template not found after fetch")
        }
    }

    override suspend fun getProvidedTemplates(): List<DefaultTemplate> {
        return queries.getProvidedTemplates().executeAsList().map { entity ->
            getTemplateWithHabits(entity.id) ?: throw IllegalStateException("Template not found after fetch")
        }
    }

    override suspend fun getCustomTemplates(): List<DefaultTemplate> {
        return queries.getCustomTemplates().executeAsList().map { entity ->
            getTemplateWithHabits(entity.id) ?: throw IllegalStateException("Template not found after fetch")
        }
    }

    override suspend fun getTemplateWithHabits(templateId: String): DefaultTemplate? {
        val entity = queries.getTemplateById(templateId).executeAsOneOrNull() ?: return null
        
        val habits = queries.getHabitsForTemplate(templateId).executeAsList().map { habitEntity ->
            DefaultHabit(
                name = habitEntity.name,
                startTime = habitEntity.startTime,
                duration = habitEntity.durationMinutes.toInt(),
                trackingType = TrackingMode.valueOf(habitEntity.trackingMode),
                target = habitEntity.target,
                repeatDays = habitEntity.repeatDays.split(",").mapNotNull { it.toIntOrNull() },
                reminderEnabled = habitEntity.reminderEnabled == 1L,
                sortOrder = habitEntity.sortOrder.toInt()
            )
        }

        return DefaultTemplate(
            id = entity.id,
            name = entity.title,
            category = "General",
            description = entity.description,
            difficulty = "Medium",
            estimatedDuration = "Variable",
            habits = habits
        )
    }

    override suspend fun saveCustomTemplate(template: DefaultTemplate) {
        database.transaction {
            queries.insertTemplate(
                id = template.id,
                title = template.name,
                description = template.description,
                taskCount = template.habits.size.toLong(),
                frequency = "Custom",
                author = "User",
                isCommunity = 0L,
                isBookmarked = 0L,
                isCustom = 1L
            )
            
            template.habits.forEachIndexed { index, habit ->
                queries.insertTemplateHabit(
                    id = "${template.id}_habit_$index",
                    templateId = template.id,
                    name = habit.name,
                    startTime = habit.startTime,
                    durationMinutes = habit.duration.toLong(),
                    trackingMode = habit.trackingType.name,
                    target = habit.target,
                    repeatDays = habit.repeatDays.joinToString(","),
                    reminderEnabled = if (habit.reminderEnabled) 1L else 0L,
                    sortOrder = habit.sortOrder.toLong()
                )
            }
        }
    }

    override suspend fun updateTemplate(template: DefaultTemplate) {
        database.transaction {
            queries.updateTemplate(
                title = template.name,
                description = template.description,
                taskCount = template.habits.size.toLong(),
                frequency = "Custom",
                id = template.id
            )
            
            // Re-insert habits
            queries.deleteTemplateHabits(template.id)
            template.habits.forEachIndexed { index, habit ->
                queries.insertTemplateHabit(
                    id = "${template.id}_habit_$index",
                    templateId = template.id,
                    name = habit.name,
                    startTime = habit.startTime,
                    durationMinutes = habit.duration.toLong(),
                    trackingMode = habit.trackingType.name,
                    target = habit.target,
                    repeatDays = habit.repeatDays.joinToString(","),
                    reminderEnabled = if (habit.reminderEnabled) 1L else 0L,
                    sortOrder = habit.sortOrder.toLong()
                )
            }
        }
    }

    override suspend fun deleteTemplate(templateId: String) {
        // FK constraint with ON DELETE CASCADE will handle TemplateHabitEntity deletion
        queries.deleteTemplate(templateId)
    }
}
