package com.vajrax.data.repository

import com.vajrax.data.local.VajraDatabase
import com.vajrax.domain.model.*
import com.vajrax.domain.repository.ActionTimelineEntityResult
import com.vajrax.domain.repository.PracticeRepository

class PracticeRepositoryImpl(
    private val database: VajraDatabase
) : PracticeRepository {
    private val queries = database.vajraDatabaseQueries

    override suspend fun getActivePractices(): List<Practice> {
        return queries.getActivePractices().executeAsList().map {
            Practice(
                id = it.id,
                principleId = it.principleId,
                title = it.title,
                targetDurationMinutes = it.targetDurationMinutes.toInt(),
                minimumDurationMinutes = it.minimumDurationMinutes.toInt(),
                preferredTime = it.preferredTime,
                trackingMode = TrackingMode.valueOf(it.trackingMode),
                isActive = it.isActive == 1L
            )
        }
    }

    override suspend fun getAllPractices(): List<Practice> {
        return queries.getAllPractices().executeAsList().map {
            Practice(
                id = it.id,
                principleId = it.principleId,
                title = it.title,
                targetDurationMinutes = it.targetDurationMinutes.toInt(),
                minimumDurationMinutes = it.minimumDurationMinutes.toInt(),
                preferredTime = it.preferredTime,
                trackingMode = TrackingMode.valueOf(it.trackingMode),
                isActive = it.isActive == 1L
            )
        }
    }

    override suspend fun insertPractice(practice: Practice) {
        queries.insertPractice(
            id = practice.id,
            principleId = practice.principleId,
            title = practice.title,
            targetDurationMinutes = practice.targetDurationMinutes.toLong(),
            minimumDurationMinutes = practice.minimumDurationMinutes.toLong(),
            preferredTime = practice.preferredTime,
            trackingMode = practice.trackingMode.name,
            isActive = if (practice.isActive) 1L else 0L
        )
    }

    override suspend fun togglePracticeActive(practiceId: String, active: Boolean) {
        queries.togglePracticeActive(if (active) 1L else 0L, practiceId)
    }

    override suspend fun getTodayTimeline(date: String): List<ActionTimelineEntityResult> {
        return queries.getTodayTimeline(date).executeAsList().map {
            ActionTimelineEntityResult(
                id = it.id,
                practiceId = it.practiceId,
                date = it.date,
                scheduledTime = it.scheduledTime,
                status = ActionStatus.valueOf(it.status),
                completedAt = it.completedAt,
                durationMinutes = it.durationMinutes?.toInt(),
                title = it.title,
                targetDurationMinutes = it.targetDurationMinutes.toInt(),
                minimumDurationMinutes = it.minimumDurationMinutes.toInt(),
                trackingMode = TrackingMode.valueOf(it.trackingMode)
            )
        }
    }

    override suspend fun updateActionStatus(actionId: String, status: ActionStatus, durationMinutes: Int?) {
        val completedAt = if (status == ActionStatus.COMPLETE || status == ActionStatus.MINIMUM) "2026-08-30T12:00:00Z" else null
        queries.updateActionStatus(
            status = status.name,
            completedAt = completedAt,
            durationMinutes = durationMinutes?.toLong(),
            id = actionId
        )
    }

    override suspend fun submitEvidence(
        actionId: String,
        note: String?,
        rating: ReflectionRating?,
        mediaPath: String?
    ) {
        val evidenceId = "ev_${actionId}_${kotlin.random.Random.nextInt(10000)}"
        queries.insertEvidence(
            id = evidenceId,
            actionRecordId = actionId,
            reflectionRating = rating?.name,
            note = note,
            mediaPath = mediaPath,
            createdAt = "2026-08-30T12:00:00Z"
        )
    }
}
