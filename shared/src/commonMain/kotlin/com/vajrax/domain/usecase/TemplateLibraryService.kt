@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.vajrax.domain.usecase

import com.vajrax.domain.habit.Habit
import com.vajrax.domain.habit.Tracker
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.domain.template.toTemplateHabit
import kotlin.uuid.Uuid

/** The user's own templates: build, duplicate, delete, and save the current routine as one. */
class TemplateLibraryService(private val templates: TemplateRepository) {

    /** Saves a template from the builder; [publish] = false keeps it as a draft. */
    suspend fun saveCustom(
        id: String,
        name: String,
        category: String,
        description: String,
        habits: List<Habit>,
        publish: Boolean
    ): DefaultTemplate {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) throw RoutineException("Give your template a name.")
        if (publish && habits.isEmpty()) throw RoutineException("Add at least one habit.")
        val template = DefaultTemplate(
            id = id,
            name = cleanName.take(NAME_MAX),
            category = category,
            description = description.trim().ifBlank { "My custom routine" },
            difficulty = "Custom",
            estimatedDuration = "Daily",
            habits = habits.mapIndexed { i, h -> h.toTemplateHabit(i) },
            isCustom = true,
            isDraft = !publish,
            frequencyLabel = "Daily",
            durationDays = DEFAULT_CYCLE_DAYS,
            recommendedFor = "Created by you"
        )
        templates.saveCustomTemplate(template)
        return template
    }

    /** Copies any template into "My templates". Returns null when it no longer exists. */
    suspend fun duplicate(templateId: String): DefaultTemplate? {
        val t = templates.getTemplateWithHabits(templateId) ?: return null
        val copy = t.copy(
            id = newId(),
            name = "${t.name} (copy)".take(NAME_MAX),
            isCustom = true,
            isDraft = false,
            isCommunity = false,
            author = null
        )
        templates.saveCustomTemplate(copy)
        return copy
    }

    suspend fun delete(templateId: String) = templates.deleteTemplate(templateId)

    /** "Save as template" from My routine: the habits as they are now, as a reusable template. */
    suspend fun saveRoutine(tracker: Tracker, habits: List<Habit>): DefaultTemplate {
        val template = DefaultTemplate(
            id = newId(),
            name = tracker.name.take(NAME_MAX),
            category = "Routine",
            description = "Saved from my routine",
            difficulty = "Custom",
            estimatedDuration = "Daily",
            habits = habits.mapIndexed { i, h -> h.toTemplateHabit(i) },
            isCustom = true,
            durationDays = tracker.totalDays,
            recommendedFor = "Created by you"
        )
        templates.saveCustomTemplate(template)
        return template
    }

    companion object {
        const val NAME_MAX = 40
        const val DEFAULT_CYCLE_DAYS = 30

        fun newId(): String = "custom_" + Uuid.random().toString()
    }
}
