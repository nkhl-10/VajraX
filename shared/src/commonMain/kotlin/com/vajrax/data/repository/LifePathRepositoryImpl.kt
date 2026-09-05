package com.vajrax.data.repository

import com.vajrax.data.local.VajraDatabase
import com.vajrax.domain.model.LifePath
import com.vajrax.domain.model.Principle
import com.vajrax.domain.repository.LifePathRepository

class LifePathRepositoryImpl(
    private val database: VajraDatabase
) : LifePathRepository {
    private val queries = database.vajraDatabaseQueries

    override suspend fun getAllLifePaths(): List<LifePath> {
        return queries.getAllLifePaths().executeAsList().map {
            LifePath(
                id = it.id,
                name = it.name,
                description = it.description,
                isActive = it.isActive == 1L
            )
        }
    }

    override suspend fun getActiveLifePath(): LifePath? {
        return queries.getActiveLifePath().executeAsOneOrNull()?.let {
            LifePath(
                id = it.id,
                name = it.name,
                description = it.description,
                isActive = it.isActive == 1L
            )
        }
    }

    override suspend fun setActiveLifePath(pathId: String) {
        queries.setActiveLifePath(pathId)
    }

    override suspend fun getPrinciplesForPath(pathId: String): List<Principle> {
        return queries.getPrinciplesForPath(pathId).executeAsList().map {
            Principle(
                id = it.id,
                lifePathId = it.lifePathId,
                title = it.title,
                description = it.description
            )
        }
    }
}
