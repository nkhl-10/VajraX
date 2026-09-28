package com.vajrax.data.local

/**
 * Seeds only immutable reference data. No demo profile, habits or history are ever inserted:
 * every number the app shows comes from what the user actually recorded.
 * The template library is seeded by [com.vajrax.data.repository.TemplateRepositoryImpl.seedSystemTemplatesIfNeeded].
 */
class DatabaseSeeder(
    private val database: VajraDatabase
) {
    private val queries = database.vajraDatabaseQueries

    fun seedInitialDataIfEmpty() {
        if (queries.getAllLifePaths().executeAsList().isNotEmpty()) return
        database.transaction {
            lifePaths.forEach { (id, name, description) ->
                queries.insertLifePath(id = id, name = name, description = description, isActive = 0L)
            }
        }
    }

    private val lifePaths = listOf(
        Triple("high_performance", "High Performance", "Deep focus, physical vigor, competence, and relentless execution."),
        Triple("self_mastery", "Self-Mastery", "Self-respect, emotional discipline, strong boundaries, and autonomy."),
        Triple("scholar", "Scholar", "Reading, rigorous learning, intellectual depth, and critical thinking."),
        Triple("wealth_builder", "Wealth Builder", "High-value skills, career growth, financial discipline, and long-term leverage."),
        Triple("balanced_life", "Balanced Life", "Holistic health, deep relationships, work-life equilibrium, and calmness."),
        Triple("creator", "Creator", "Deep craft, consistent creative output, focus blocks, and experimentation."),
        Triple("mindful_life", "Mindful Life", "Awareness, emotional regulation, presence, and intentional reflection."),
        Triple("purpose_driven", "Purpose-Driven", "Core values, duty, community contribution, and meaningful impact."),
        Triple("custom_life", "Custom Architecture", "Fully personalized path designed by you.")
    )
}
