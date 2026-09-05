package com.vajrax.domain.repository

import com.vajrax.domain.model.LifePath
import com.vajrax.domain.model.Principle

interface LifePathRepository {
    suspend fun getAllLifePaths(): List<LifePath>
    suspend fun getActiveLifePath(): LifePath?
    suspend fun setActiveLifePath(pathId: String)
    suspend fun getPrinciplesForPath(pathId: String): List<Principle>
}
