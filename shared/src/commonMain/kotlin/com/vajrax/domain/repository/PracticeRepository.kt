package com.vajrax.domain.repository

import com.vajrax.domain.model.ActionRecord
import com.vajrax.domain.model.ActionStatus
import com.vajrax.domain.model.Practice
import com.vajrax.domain.model.ReflectionRating

interface PracticeRepository {
    suspend fun getActivePractices(): List<Practice>
    suspend fun getAllPractices(): List<Practice>
    suspend fun insertPractice(practice: Practice)
    suspend fun togglePracticeActive(practiceId: String, active: Boolean)
    
    // Living Timeline
    suspend fun getTodayTimeline(date: String): List<ActionTimelineEntityResult>
    suspend fun updateActionStatus(actionId: String, status: ActionStatus, durationMinutes: Int?)
    
    // Evidence
    suspend fun submitEvidence(
        actionId: String,
        note: String?,
        rating: ReflectionRating?,
        mediaPath: String?
    )
}

data class ActionTimelineEntityResult(
    val id: String,
    val practiceId: String,
    val date: String,
    val scheduledTime: String?,
    val status: ActionStatus,
    val completedAt: String?,
    val durationMinutes: Int?,
    val title: String,
    val targetDurationMinutes: Int,
    val minimumDurationMinutes: Int,
    val trackingMode: com.vajrax.domain.model.TrackingMode
)
