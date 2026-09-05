package com.vajrax.domain.repository

import com.vajrax.ui.features.learn.BookIdea

interface LearnRepository {
    suspend fun getAllBookIdeas(): List<BookIdea>
    suspend fun saveBookIdea(idea: BookIdea)
    suspend fun launchExperiment(ideaId: String, customPractice: String, days: Int)
}
