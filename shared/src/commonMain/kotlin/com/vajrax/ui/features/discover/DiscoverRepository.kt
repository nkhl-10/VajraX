package com.vajrax.ui.features.discover

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface DiscoverRepository {
    fun getLifePaths(): Flow<List<DiscoverLifePath>>
    fun getPracticesForPath(pathId: String): Flow<List<DiscoverPractice>>
    suspend fun togglePractice(practiceId: String)
}

class DiscoverRepositoryImpl : DiscoverRepository {
    
    private val pathsFlow = MutableStateFlow(
        listOf(
            DiscoverLifePath("stoic", "Stoicism", "The Art of Unshakeable Focus", "#4A90E2"),
            DiscoverLifePath("high_perf", "High Performance", "Biological & Psychological Optimization", "#E24A4A"),
            DiscoverLifePath("deep_work", "Deep Work", "Mastery in a Distracted World", "#8E44AD")
        )
    )

    private val practicesFlow = MutableStateFlow(
        listOf(
            DiscoverPractice("p1", "stoic", "Morning Reflection", "Review your principles and set intentions.", true),
            DiscoverPractice("p2", "stoic", "Negative Visualization", "Premeditatio malorum to build resilience.", false),
            DiscoverPractice("p3", "stoic", "Evening Review", "Journal your actions and reactions today.", true),
            
            DiscoverPractice("p4", "high_perf", "Zone 2 Cardio", "45 mins to build mitochondrial density.", false),
            DiscoverPractice("p5", "high_perf", "Cold Exposure", "2 mins cold shower for dopamine baseline.", true),
            DiscoverPractice("p6", "high_perf", "Sleep Protocol", "No screens 60 mins before bed.", true),

            DiscoverPractice("p7", "deep_work", "Time Blocking", "Schedule 90-min deep work blocks.", true),
            DiscoverPractice("p8", "deep_work", "Digital Minimalism", "Delete social media from phone.", false)
        )
    )

    override fun getLifePaths(): Flow<List<DiscoverLifePath>> = pathsFlow

    override fun getPracticesForPath(pathId: String): Flow<List<DiscoverPractice>> {
        return practicesFlow.map { list -> list.filter { it.pathId == pathId } }
    }

    override suspend fun togglePractice(practiceId: String) {
        val current = practicesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == practiceId }
        if (index != -1) {
            val practice = current[index]
            current[index] = practice.copy(isActive = !practice.isActive)
            practicesFlow.value = current
        }
    }
}
