package com.vajrax.ui.features.today

import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.ReflectionRating
import com.vajrax.domain.model.TrackingMode
import com.vajrax.domain.repository.PracticeRepository
import com.vajrax.platform.WidgetController
import com.vajrax.platform.WidgetTimelineSnapshot
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class TodayViewModel(

    private val practiceRepository: PracticeRepository,
    private val widgetController: WidgetController? = null
) : MviViewModel<TodayUiState, TodayIntent, TodayEffect>(TodayUiState()) {

    init {
        sendIntent(TodayIntent.LoadTodayTimeline)
    }

    override fun sendIntent(intent: TodayIntent) {
        when (intent) {
            is TodayIntent.LoadTodayTimeline -> loadTimeline()
            is TodayIntent.StartPractice -> startPractice(intent.actionId)
            is TodayIntent.QuickCompletePractice -> quickCompletePractice(intent.actionId)
            is TodayIntent.SelectPracticeAsFocus -> selectFocusPractice(intent.actionId)
            is TodayIntent.CompletePractice -> openEvidenceSheet(intent.actionId, isMinimum = false)
            is TodayIntent.MinimumPractice -> openEvidenceSheet(intent.actionId, isMinimum = true)
            is TodayIntent.RequestSkipPractice -> triggerSamaDialog(intent.actionId)
            is TodayIntent.AcceptSamaMinimum -> acceptSama(intent.actionId)
            is TodayIntent.ConfirmSkip -> confirmSkip(intent.actionId)
            is TodayIntent.DismissSamaDialog -> updateState { copy(samaInterventionItem = null) }
            is TodayIntent.FinishTimerSession -> finishTimer(intent.actionId, intent.elapsedMinutes)
            is TodayIntent.CancelTimer -> updateState { copy(activeTimerItem = null) }
            is TodayIntent.OpenEvidenceForAction -> updateState { copy(showEvidenceSheet = true, selectedActionIdForEvidence = intent.actionId) }
            is TodayIntent.DismissOptionalEvidencePrompt -> updateState { copy(lastCompletedActionId = null, lastCompletedActionTitle = null) }
            is TodayIntent.SubmitEvidence -> submitEvidence(intent.actionId, intent.note, intent.rating)
            is TodayIntent.DismissEvidenceSheet -> updateState { copy(showEvidenceSheet = false, selectedActionIdForEvidence = null) }
        }
    }

    private fun loadTimeline() {
        viewModelScope.launch(Dispatchers.IO) {
            updateState { copy(isLoading = true) }
            
            val timeline = practiceRepository.getTodayTimeline("2026-08-30")
            
            val completed = timeline.filter { it.status == ActionStatus.COMPLETE || it.status == ActionStatus.MINIMUM }
                .map {
                    ActionTimelineItem(
                        id = it.id,
                        practiceId = it.practiceId,
                        title = it.title,
                        scheduledTime = it.scheduledTime,
                        targetDurationMinutes = it.targetDurationMinutes,
                        minimumDurationMinutes = it.minimumDurationMinutes,
                        status = it.status,
                        trackingMode = it.trackingMode,
                        durationMinutes = it.durationMinutes
                    )
                }

            val ongoingOrPending = timeline.filter { it.status == ActionStatus.ONGOING || it.status == ActionStatus.PENDING }
            val nowItem = ongoingOrPending.firstOrNull()?.let {
                ActionTimelineItem(
                    id = it.id,
                    practiceId = it.practiceId,
                    title = it.title,
                    scheduledTime = it.scheduledTime,
                    targetDurationMinutes = it.targetDurationMinutes,
                    minimumDurationMinutes = it.minimumDurationMinutes,
                    status = it.status,
                    trackingMode = it.trackingMode,
                    durationMinutes = it.durationMinutes
                )
            }

            val nextItems = if (ongoingOrPending.size > 1) ongoingOrPending.drop(1).take(2).map {
                ActionTimelineItem(
                    id = it.id,
                    practiceId = it.practiceId,
                    title = it.title,
                    scheduledTime = it.scheduledTime,
                    targetDurationMinutes = it.targetDurationMinutes,
                    minimumDurationMinutes = it.minimumDurationMinutes,
                    status = it.status,
                    trackingMode = it.trackingMode,
                    durationMinutes = it.durationMinutes
                )
            } else emptyList()

            val laterItems = if (ongoingOrPending.size > 3) ongoingOrPending.drop(3).map {
                ActionTimelineItem(
                    id = it.id,
                    practiceId = it.practiceId,
                    title = it.title,
                    scheduledTime = it.scheduledTime,
                    targetDurationMinutes = it.targetDurationMinutes,
                    minimumDurationMinutes = it.minimumDurationMinutes,
                    status = it.status,
                    trackingMode = it.trackingMode,
                    durationMinutes = it.durationMinutes
                )
            } else emptyList()

            val totalCount = timeline.size
            val completedCount = completed.size

            // Circadian pulse calculations
            val hour = 9
            val minute = 30

            val greetingText = when (hour) {
                in 5..11 -> "Good Morning,\nAlex."
                in 12..16 -> "Good Afternoon,\nAlex."
                in 17..21 -> "Good Evening,\nAlex."
                else -> "Good Night,\nAlex."
            }

            // Circadian active day fraction (9:30 AM = 25% through the 16h active window)
            val dayFraction = ((hour + minute / 60.0f - 6f) / 16.0f).coerceIn(0.05f, 1.0f)
            val targetPercent = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 75
            val consistency = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 85
            val expectedCount = (totalCount * dayFraction).coerceAtLeast(0.5f)
            val pace = ((completedCount.toFloat() / expectedCount).coerceIn(0.5f, 1.4f) * 100).toInt()

            updateState {
                copy(
                    isLoading = false,
                    greeting = greetingText,
                    dayProgressFraction = dayFraction,
                    targetPercentage = targetPercent,
                    pacePercentage = pace,
                    consistencyPercentage = consistency,
                    completedItems = completed,
                    currentFocus = nowItem,
                    nextItems = nextItems,
                    laterItems = laterItems,
                    todayCompletedCount = completedCount,
                    todayTotalCount = if (totalCount > 0) totalCount else 5
                )
            }

            // Sync with Home Screen Widget
            if (nowItem != null) {
                widgetController?.updateWidget(
                    WidgetTimelineSnapshot(
                        currentFocusTitle = nowItem.title,
                        currentFocusDuration = nowItem.targetDurationMinutes,
                        currentFocusScheduledTime = nowItem.scheduledTime,
                        currentActionId = nowItem.id,
                        completedCount = completedCount,
                        totalCount = if (totalCount > 0) totalCount else 5,
                        nextFocusTitle = nextItems.firstOrNull()?.title
                    )
                )
            }
        }
    }

    private fun quickCompletePractice(actionId: String) {
        val currentFocusTitle = uiState.value.currentFocus?.takeIf { it.id == actionId }?.title
            ?: uiState.value.nextItems.firstOrNull { it.id == actionId }?.title
            ?: uiState.value.laterItems.firstOrNull { it.id == actionId }?.title
            ?: "Practice"

        viewModelScope.launch(Dispatchers.IO) {
            practiceRepository.updateActionStatus(actionId, ActionStatus.COMPLETE, 60)
            updateState {
                copy(
                    lastCompletedActionId = actionId,
                    lastCompletedActionTitle = currentFocusTitle
                )
            }
            sendEffect(TodayEffect.ShowToast("✓ Completed. Optional evidence available."))
            loadTimeline()
        }
    }

    private fun selectFocusPractice(actionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            practiceRepository.updateActionStatus(actionId, ActionStatus.ONGOING, null)
            loadTimeline()
        }
    }

    private fun startPractice(actionId: String) {
        val current = uiState.value.currentFocus
        if (current != null && current.id == actionId) {
            if (current.trackingMode == TrackingMode.TIMER) {
                updateState { copy(activeTimerItem = current) }
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    practiceRepository.updateActionStatus(actionId, ActionStatus.ONGOING, null)
                    sendEffect(TodayEffect.ShowToast("Practice started."))
                    loadTimeline()
                }
            }
        }
    }

    private fun finishTimer(actionId: String, elapsedMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            practiceRepository.updateActionStatus(actionId, ActionStatus.COMPLETE, elapsedMinutes)
            updateState {
                copy(
                    activeTimerItem = null,
                    lastCompletedActionId = actionId,
                    lastCompletedActionTitle = "Practice"
                )
            }
            sendEffect(TodayEffect.ShowToast("Timer session complete. Momentum preserved."))
            loadTimeline()
        }
    }

    private fun triggerSamaDialog(actionId: String) {
        val current = uiState.value.currentFocus
        if (current != null && current.id == actionId) {
            updateState { copy(samaInterventionItem = current) }
        }
    }

    private fun acceptSama(actionId: String) {
        updateState {
            copy(
                samaInterventionItem = null,
                showEvidenceSheet = true,
                selectedActionIdForEvidence = actionId
            )
        }
    }

    private fun confirmSkip(actionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            practiceRepository.updateActionStatus(actionId, ActionStatus.SKIPPED, 0)
            updateState { copy(samaInterventionItem = null) }
            sendEffect(TodayEffect.ShowToast("Action skipped. Resetting focus."))
            loadTimeline()
        }
    }

    private fun openEvidenceSheet(actionId: String, isMinimum: Boolean) {
        updateState {
            copy(
                showEvidenceSheet = true,
                selectedActionIdForEvidence = actionId
            )
        }
    }

    private fun submitEvidence(
        actionId: String,
        note: String,
        rating: ReflectionRating?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            practiceRepository.updateActionStatus(actionId, ActionStatus.COMPLETE, 60)
            practiceRepository.submitEvidence(actionId, note.ifBlank { null }, rating, null)
            
            updateState {
                copy(
                    showEvidenceSheet = false,
                    selectedActionIdForEvidence = null,
                    lastCompletedActionId = null,
                    lastCompletedActionTitle = null
                )
            }
            sendEffect(TodayEffect.ShowToast("Evidence saved. Momentum preserved."))
            loadTimeline()
        }
    }
}
