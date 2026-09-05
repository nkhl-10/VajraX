package com.vajrax.data.repository

import com.vajrax.data.local.VajraDatabase
import com.vajrax.domain.model.ReflectionRating
import com.vajrax.domain.repository.GrowRepository
import com.vajrax.ui.features.grow.EvidenceEntry
import com.vajrax.ui.features.grow.GrowthDimension

class GrowRepositoryImpl(
    private val database: VajraDatabase
) : GrowRepository {
    private val queries = database.vajraDatabaseQueries

    override suspend fun getRecentEvidence(limit: Long): List<EvidenceEntry> {
        return queries.getRecentEvidence(limit).executeAsList().map {
            EvidenceEntry(
                id = it.id,
                practiceTitle = it.practiceTitle,
                date = it.date,
                note = it.note ?: "",
                rating = it.reflectionRating?.let { r -> ReflectionRating.valueOf(r) },
                durationMinutes = it.durationMinutes?.toInt() ?: 0
            )
        }
    }

    override suspend fun getGrowthDimensions(): List<GrowthDimension> {
        val completedCount = queries.countCompletedActionsBetweenDates("2026-08-01", "2026-08-30").executeAsOne()
        val totalCount = queries.countTotalActionsBetweenDates("2026-08-01", "2026-08-30").executeAsOne()
        val consistency = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 80

        return listOf(
            GrowthDimension("Self-Respect", "Commitments kept vs planned", "$consistency%", "alignment"),
            GrowthDimension("Independence", "Self-managed daily routines", "7", "routines"),
            GrowthDimension("Competence", "Deep focus deliberate practice", "32", "hours"),
            GrowthDimension("Boundaries", "Protected focus blocks without distraction", "18", "sessions"),
            GrowthDimension("Calmness", "Digital-free evening shutdowns", "11", "nights")
        )
    }
}
