package com.vajrax.ui.features.today

import androidx.compose.runtime.Immutable
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.ReflectionRating
import com.vajrax.domain.model.TrackingMode

@Immutable
data class ActionTimelineItem(
    val id: String,
    val practiceId: String,
    val title: String,
    val scheduledTime: String?,
    val targetDurationMinutes: Int,
    val minimumDurationMinutes: Int,
    val status: ActionStatus,
    val trackingMode: TrackingMode,
    val durationMinutes: Int? = null
)

@Immutable
data class TodayUiState(
    val greeting: String = "Good Morning,\nAlex.",
    val userName: String = "Alex",
    val dayProgressFraction: Float = 0.25f,
    val targetPercentage: Int = 75,
    val pacePercentage: Int = 92,
    val consistencyPercentage: Int = 85,
    val consistencyDeltaText: String = "↑ 12% this month",
    val todayCompletedCount: Int = 2,
    val todayTotalCount: Int = 5,
    val focusHoursSummary: String = "12h",
    val activePathName: String = "High Performance",
    val completedItems: List<ActionTimelineItem> = emptyList(),
    val currentFocus: ActionTimelineItem? = null,
    val nextItems: List<ActionTimelineItem> = emptyList(),
    val isLoading: Boolean = false,
    val showEvidenceSheet: Boolean = false,
    val selectedActionIdForEvidence: String? = null,
    val activeTimerItem: ActionTimelineItem? = null,
    val samaInterventionItem: ActionTimelineItem? = null
)

sealed interface TodayIntent {
    data object LoadTodayTimeline : TodayIntent
    data class StartPractice(val actionId: String) : TodayIntent
    data class CompletePractice(val actionId: String) : TodayIntent
    data class MinimumPractice(val actionId: String) : TodayIntent
    data class SelectPracticeAsFocus(val actionId: String) : TodayIntent
    data class RequestSkipPractice(val actionId: String) : TodayIntent
    data class AcceptSamaMinimum(val actionId: String) : TodayIntent
    data class ConfirmSkip(val actionId: String) : TodayIntent
    data object DismissSamaDialog : TodayIntent
    data class FinishTimerSession(val actionId: String, val elapsedMinutes: Int) : TodayIntent
    data object CancelTimer : TodayIntent
    data class SubmitEvidence(
        val actionId: String,
        val note: String,
        val rating: ReflectionRating?
    ) : TodayIntent
    data object DismissEvidenceSheet : TodayIntent
}

sealed interface TodayEffect {
    data class ShowToast(val message: String) : TodayEffect
}

