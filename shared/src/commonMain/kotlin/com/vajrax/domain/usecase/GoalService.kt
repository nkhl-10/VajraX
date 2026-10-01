@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.vajrax.domain.usecase

import com.vajrax.core.time.AppClock
import com.vajrax.domain.repository.Goal
import com.vajrax.domain.repository.GoalRepository
import com.vajrax.domain.repository.GoalTargetType
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

/** Goals linked to habits; progress itself is computed from check-ins by the analytics. */
class GoalService(
    private val goals: GoalRepository,
    private val clock: AppClock
) {
    suspend fun save(
        id: String?,
        title: String,
        targetType: GoalTargetType,
        target: Double,
        endDate: LocalDate?,
        habitIds: List<String>
    ) {
        if (title.isBlank() || target <= 0 || habitIds.isEmpty()) {
            throw RoutineException("Add a title, a target above 0 and at least one habit.")
        }
        goals.saveGoal(
            Goal(
                id = id ?: ("goal_" + Uuid.random().toString()),
                title = title.trim(),
                description = null,
                targetType = targetType,
                targetValue = if (targetType == GoalTargetType.RATE) target.coerceAtMost(100.0) else target,
                startDate = clock.today(),
                endDate = endDate,
                habitIds = habitIds
            ),
            clock.nowIso()
        )
    }

    suspend fun delete(goalId: String) = goals.deleteGoal(goalId)
}
