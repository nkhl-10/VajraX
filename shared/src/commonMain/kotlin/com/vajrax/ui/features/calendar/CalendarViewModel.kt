package com.vajrax.ui.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vajrax.domain.repository.PracticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val practiceRepository: PracticeRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadPractices()
    }

    private fun loadPractices() {
        if (practiceRepository == null) return
        viewModelScope.launch {
            try {
                val practices = practiceRepository.getActivePractices()
                val mappedRows = practices.map { practice ->
                    CalendarMatrixRow(
                        id = practice.id,
                        title = practice.title,
                        timeSubtitle = practice.preferredTime ?: "",
                        iconEmoji = "✓",
                        mondayCompleted = false,
                        tuesdayCompleted = false,
                        wednesdayCompleted = false,
                        thursdayCompleted = false,
                        fridayCompleted = false
                    )
                }
                
                // If practices are loaded, overwrite the default mock rows
                if (mappedRows.isNotEmpty()) {
                    _uiState.update { it.copy(tasks = mappedRows) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendIntent(intent: CalendarIntent) {
        when (intent) {
            is CalendarIntent.SelectDay -> {
                _uiState.update { state ->
                    val day = state.days.getOrNull(intent.dayIndex)
                    val dateStr = if (day != null) "${day.dayName}, ${day.dateNumber} Jan" else state.selectedDateText
                    state.copy(
                        selectedDayOfWeek = intent.dayIndex,
                        selectedDateText = dateStr
                    )
                }
            }
            is CalendarIntent.ToggleTaskDay -> {
                _uiState.update { state ->
                    if (intent.dayIndex != state.currentDayOfWeek) {
                        return@update state // Disallow editing past/future dates
                    }
                    val updated = state.tasks.map { row ->
                        if (row.id == intent.taskId) {
                            when (intent.dayIndex) {
                                0 -> row.copy(mondayCompleted = !row.mondayCompleted)
                                1 -> row.copy(tuesdayCompleted = !row.tuesdayCompleted)
                                2 -> row.copy(wednesdayCompleted = !row.wednesdayCompleted)
                                3 -> row.copy(thursdayCompleted = !row.thursdayCompleted)
                                4 -> row.copy(fridayCompleted = !row.fridayCompleted)
                                else -> row
                            }
                        } else row
                    }
                    state.copy(tasks = updated)
                }
            }
        }
    }
}
