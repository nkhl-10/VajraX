package com.vajrax.domain.usecase

import com.vajrax.core.time.AppClock
import com.vajrax.domain.repository.Reflection
import com.vajrax.domain.repository.ReflectionPeriod
import com.vajrax.domain.repository.ReflectionRepository
import kotlinx.datetime.LocalDate

/** Weekly / monthly reflections, one per period. */
class ReflectionService(
    private val reflections: ReflectionRepository,
    private val clock: AppClock
) {
    suspend fun save(period: ReflectionPeriod, start: LocalDate, wentWell: String, gotInTheWay: String, next: String) {
        if (wentWell.isBlank() && gotInTheWay.isBlank() && next.isBlank()) {
            throw RoutineException("Write at least one answer to save the reflection.")
        }
        reflections.saveReflection(Reflection(period, start, wentWell.trim(), gotInTheWay.trim(), next.trim()), clock.nowIso())
    }
}
