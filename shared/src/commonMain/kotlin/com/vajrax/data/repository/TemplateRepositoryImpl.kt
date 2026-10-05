@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.vajrax.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.vajrax.data.local.DbDispatcher
import com.vajrax.data.local.TemplateEntity
import com.vajrax.data.local.TemplateHabitEntity
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.local.io
import com.vajrax.data.local.toDb
import com.vajrax.data.local.toDefaultHabit
import com.vajrax.data.local.toDefaultTemplate
import com.vajrax.domain.repository.SettingsRepository
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.domain.template.TemplateCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TemplateRepositoryImpl(
    private val database: VajraDatabase
) : TemplateRepository {
    private val queries = database.vajraDatabaseQueries

    private fun assemble(templates: List<TemplateEntity>, habits: List<TemplateHabitEntity>): List<DefaultTemplate> {
        val byTemplate = habits.groupBy { it.templateId }
        return templates.map { t ->
            t.toDefaultTemplate(byTemplate[t.id].orEmpty().sortedBy { it.sortOrder }.map { it.toDefaultHabit() })
        }
    }

    override suspend fun getAllTemplates(): List<DefaultTemplate> = io {
        assemble(queries.getAllTemplates().awaitAsList(), queries.getAllTemplateHabits().awaitAsList())
    }

    override suspend fun getProvidedTemplates(): List<DefaultTemplate> = io {
        assemble(queries.getProvidedTemplates().awaitAsList(), queries.getAllTemplateHabits().awaitAsList())
    }

    override suspend fun getCustomTemplates(): List<DefaultTemplate> = io {
        assemble(queries.getCustomTemplates().awaitAsList(), queries.getAllTemplateHabits().awaitAsList())
    }

    override suspend fun getTemplateWithHabits(templateId: String): DefaultTemplate? = io {
        val entity = queries.getTemplateById(templateId).awaitAsOneOrNull() ?: return@io null
        entity.toDefaultTemplate(queries.getHabitsForTemplate(templateId).awaitAsList().map { it.toDefaultHabit() })
    }

    override fun observeLibrary(): Flow<List<DefaultTemplate>> = combine(
        queries.getAllTemplates().asFlow().mapToList(DbDispatcher),
        queries.getAllTemplateHabits().asFlow().mapToList(DbDispatcher)
    ) { templates, habits -> assemble(templates, habits) }

    override fun observeCustomTemplates(): Flow<List<DefaultTemplate>> = combine(
        queries.getCustomTemplates().asFlow().mapToList(DbDispatcher),
        queries.getAllTemplateHabits().asFlow().mapToList(DbDispatcher)
    ) { templates, habits -> assemble(templates, habits) }

    private suspend fun writeTemplate(template: DefaultTemplate, isCustom: Boolean, updatedAt: String) {
        queries.upsertTemplate(
            id = template.id,
            title = template.name.trim(),
            description = template.description.trim(),
            taskCount = template.habits.size.toLong(),
            frequency = template.frequencyLabel,
            author = template.author,
            isCommunity = template.isCommunity.toDb(),
            isBookmarked = 0L,
            isCustom = isCustom.toDb(),
            category = template.category,
            durationDays = template.durationDays.toLong(),
            isDraft = template.isDraft.toDb(),
            recommendedFor = template.recommendedFor,
            updatedAt = updatedAt
        )
        queries.deleteTemplateHabits(template.id)
        template.habits.forEachIndexed { index, habit ->
            queries.insertTemplateHabit(
                id = "${template.id}_habit_$index",
                templateId = template.id,
                name = habit.name.trim(),
                startTime = habit.startTime,
                durationMinutes = habit.duration.toLong(),
                trackingMode = habit.trackingType.name,
                target = habit.target,
                repeatDays = habit.repeatDays.joinToString(","),
                reminderEnabled = habit.reminderEnabled.toDb(),
                sortOrder = index.toLong(),
                category = habit.category,
                icon = habit.icon,
                color = habit.color,
                habitType = habit.habitType.name,
                targetValue = habit.targetValue,
                unit = habit.unit,
                scheduleType = habit.scheduleType.name,
                weeklyTarget = habit.weeklyTarget.toLong(),
                intervalDays = habit.intervalDays.toLong()
            )
        }
    }

    override suspend fun saveCustomTemplate(template: DefaultTemplate): Unit = io {
        val now = kotlin.time.Clock.System.now().toString()
        database.transaction { writeTemplate(template.copy(isCustom = true), isCustom = true, updatedAt = now) }
    }

    override suspend fun updateTemplate(template: DefaultTemplate): Unit = io {
        val existing = queries.getTemplateById(template.id).awaitAsOneOrNull()
        // System templates are immutable source definitions.
        if (existing != null && existing.isCustom != 1L) return@io
        val now = kotlin.time.Clock.System.now().toString()
        database.transaction { writeTemplate(template.copy(isCustom = true), isCustom = true, updatedAt = now) }
    }

    override suspend fun deleteTemplate(templateId: String): Unit = io {
        val existing = queries.getTemplateById(templateId).awaitAsOneOrNull() ?: return@io
        if (existing.isCustom != 1L) return@io
        database.transaction {
            queries.deleteTemplateHabits(templateId)
            queries.deleteTemplate(templateId)
        }
    }

    override suspend fun seedSystemTemplatesIfNeeded(): Unit = io {
        com.vajrax.data.local.DatabaseSeeder(database).seedInitialDataIfEmpty()
        val stored = queries.getSetting(SettingsRepository.LIBRARY_VERSION).awaitAsOneOrNull()
        if (stored == TemplateCatalog.VERSION) return@io
        database.transaction {
            queries.deleteSystemTemplateHabits()
            queries.deleteSystemTemplates()
            TemplateCatalog.all.forEach { writeTemplate(it, isCustom = false, updatedAt = "") }
            queries.putSetting(SettingsRepository.LIBRARY_VERSION, TemplateCatalog.VERSION)
            // A new bundled library replaced a downloaded one: download again on the next refresh.
            queries.deleteSetting(REMOTE_LIBRARY_VERSION)
        }
    }

    override suspend fun remoteLibraryVersion(): String? = io {
        queries.getSetting(
            REMOTE_LIBRARY_VERSION
        ).awaitAsOneOrNull()
    }

    override suspend fun replaceSystemTemplates(templates: List<DefaultTemplate>, version: String): Unit = io {
        database.transaction {
            queries.deleteSystemTemplateHabits()
            queries.deleteSystemTemplates()
            templates.filterNot {
                it.id.startsWith(
                    "custom_"
                )
            }.forEach { writeTemplate(it, isCustom = false, updatedAt = "") }
            queries.putSetting(REMOTE_LIBRARY_VERSION, version)
        }
    }

    private companion object {
        const val REMOTE_LIBRARY_VERSION = "template_library_remote_version"
    }
}
