package com.vajrax.domain.repository

import com.vajrax.domain.model.ReflectionRating
import com.vajrax.ui.features.grow.EvidenceEntry
import com.vajrax.ui.features.grow.GrowthDimension

interface GrowRepository {
    suspend fun getRecentEvidence(limit: Long = 10): List<EvidenceEntry>
    suspend fun getGrowthDimensions(): List<GrowthDimension>
}
