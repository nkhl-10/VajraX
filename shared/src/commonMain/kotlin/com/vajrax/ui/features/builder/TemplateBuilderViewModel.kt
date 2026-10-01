@file:OptIn(ExperimentalUuidApi::class)

package com.vajrax.ui.features.builder

import com.vajrax.core.coroutines.AppDispatchers
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.habit.Habit
import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.template.toHabitCopy
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class BuilderState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "Productivity",
    val habits: List<Habit> = emptyList(),
    val nameError: String? = null,
    val habitsError: String? = null,
    val isSaving: Boolean = false
) {
    val dailyMinutes: Int get() = habits.sumOf { it.durationMinutes }
}

sealed interface BuilderIntent {
    data class SetName(val value: String) : BuilderIntent
    data class SetDescription(val value: String) : BuilderIntent
    data class SetCategory(val value: String) : BuilderIntent
    data class AddHabit(val habit: Habit) : BuilderIntent
    data class UpdateHabit(val index: Int, val habit: Habit) : BuilderIntent
    data class RemoveHabit(val index: Int) : BuilderIntent
    data class MoveHabit(val from: Int, val to: Int) : BuilderIntent
    data object SaveDraft : BuilderIntent
    data object Create : BuilderIntent
}

sealed interface BuilderEffect {
    data class Saved(val templateId: String, val published: Boolean) : BuilderEffect
    data class ShowMessage(val message: String) : BuilderEffect
}

/** "Create new template" (design: Create new template.png). Custom templates are the user's own copies. */
class TemplateBuilderViewModel(
    private val templates: TemplateRepository,
    private val library: com.vajrax.domain.usecase.TemplateLibraryService,
    editTemplateId: String?
) : MviViewModel<BuilderState, BuilderIntent, BuilderEffect>(BuilderState()) {

    init {
        launchLoad(AppDispatchers.IO) {
            val existing = editTemplateId?.let { templates.getTemplateWithHabits(it) }?.takeIf { it.isCustom }
            if (existing != null) {
                updateState {
                    copy(
                        isLoading = false,
                        isEditing = true,
                        id = existing.id,
                        name = existing.name,
                        description = existing.description,
                        category = existing.category,
                        habits = existing.habits.sortedBy { it.sortOrder }.mapIndexed { i, h -> h.toHabitCopy("draft_$i", null, i) }
                    )
                }
            } else {
                updateState { copy(isLoading = false, id = "custom_" + Uuid.random().toString()) }
            }
        }
    }

    override fun sendIntent(intent: BuilderIntent) {
        when (intent) {
            is BuilderIntent.SetName -> updateState { copy(name = intent.value.take(NAME_MAX), nameError = null) }
            is BuilderIntent.SetDescription -> updateState { copy(description = intent.value.take(DESCRIPTION_MAX)) }
            is BuilderIntent.SetCategory -> updateState { copy(category = intent.value) }
            is BuilderIntent.AddHabit -> updateState {
                copy(habits = habits + intent.habit.copy(id = "draft_${Uuid.random()}", sortOrder = habits.size), habitsError = null)
            }
            is BuilderIntent.UpdateHabit -> updateState {
                copy(habits = habits.mapIndexed { i, h -> if (i == intent.index) intent.habit else h })
            }
            is BuilderIntent.RemoveHabit -> updateState { copy(habits = habits.filterIndexed { i, _ -> i != intent.index }) }
            is BuilderIntent.MoveHabit -> updateState {
                if (intent.from !in habits.indices || intent.to !in habits.indices || intent.from == intent.to) this
                else copy(habits = habits.toMutableList().apply { add(intent.to, removeAt(intent.from)) })
            }
            BuilderIntent.SaveDraft -> save(publish = false)
            BuilderIntent.Create -> save(publish = true)
        }
    }

    private fun save(publish: Boolean) {
        val s = currentState()
        if (s.isSaving) return
        val name = s.name.trim()
        var nameError: String? = null
        var habitsError: String? = null
        if (name.isEmpty()) nameError = "Give your template a name."
        if (publish && s.habits.isEmpty()) habitsError = "Add at least one habit."
        if (nameError != null || habitsError != null) {
            updateState { copy(nameError = nameError, habitsError = habitsError) }
            return
        }
        updateState { copy(isSaving = true) }
        viewModelScope.launch(AppDispatchers.IO) {
            runCatchingCancellable { library.saveCustom(s.id, name, s.category, s.description, s.habits, publish) }
                .onSuccess { template ->
                    updateState { copy(isSaving = false) }
                    sendEffect(BuilderEffect.Saved(template.id, publish))
                }
                .onFailure {
                    updateState { copy(isSaving = false) }
                    sendEffect(BuilderEffect.ShowMessage(if (it is com.vajrax.domain.usecase.RoutineException) userMessage(it) else "Couldn't save the template. Please try again."))
                }
        }
    }

    companion object {
        const val NAME_MAX = 40
        const val DESCRIPTION_MAX = 160
        val categories = listOf("Productivity", "Routine", "Wellness", "Personal")

        fun summary(h: Habit): String = listOfNotNull(
            TimeFormat.display(h.time).ifBlank { null },
            TimeFormat.duration(h.durationMinutes),
            h.targetLabel() ?: h.type.label,
            h.schedule.label()
        ).joinToString(" · ")
    }
}
