package com.vajrax.data.repository

import com.vajrax.data.local.VajraDatabase
import com.vajrax.domain.repository.LearnRepository
import com.vajrax.ui.features.learn.BookIdea

class LearnRepositoryImpl(
    private val database: VajraDatabase
) : LearnRepository {
    private val queries = database.vajraDatabaseQueries

    override suspend fun getAllBookIdeas(): List<BookIdea> {
        return queries.getAllBookIdeas().executeAsList().map {
            BookIdea(
                id = it.id,
                bookTitle = it.bookTitle,
                author = it.author,
                idea = it.idea,
                practicalApplication = it.practicalApplication,
                isExperimentActive = it.isExperimentActive == 1L,
                experimentDaysLeft = it.experimentDaysLeft?.toInt()
            )
        }
    }

    override suspend fun saveBookIdea(idea: BookIdea) {
        queries.insertBookIdea(
            id = idea.id,
            bookTitle = idea.bookTitle,
            author = idea.author,
            idea = idea.idea,
            practicalApplication = idea.practicalApplication,
            isExperimentActive = if (idea.isExperimentActive) 1L else 0L,
            experimentDaysLeft = idea.experimentDaysLeft?.toLong(),
            createdAt = "2026-08-30T00:00:00Z"
        )
    }

    override suspend fun launchExperiment(ideaId: String, customPractice: String, days: Int) {
        queries.updateExperimentStatus(
            isExperimentActive = 1L,
            experimentDaysLeft = days.toLong(),
            practicalApplication = customPractice,
            id = ideaId
        )
    }
}
