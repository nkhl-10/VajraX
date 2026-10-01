package com.vajrax.ui.features.discover

import com.vajrax.domain.repository.TemplateRepository
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.template.TemplateCatalog
import com.vajrax.presentation.mvi.MviViewModel
import kotlinx.coroutines.flow.combine

/** Template library (spec 04 Screens 02 & 11): provided, bundled community picks and the user's own. */
class DiscoverViewModel(
    private val templates: TemplateRepository,
    private val trackers: TrackerRepository
) : MviViewModel<DiscoverState, DiscoverIntent, Unit>(DiscoverState()) {

    private val catalogOrder: Map<String, Int> = TemplateCatalog.all.mapIndexed { i, t -> t.id to i }.toMap()

    init {
        launchLoad {
            combine(templates.observeLibrary(), trackers.observeActiveTracker()) { all, tracker -> all to tracker }
                .collect { (all, tracker) ->
                    val published = all.filter { !it.isDraft }
                    val system = published.filter { !it.isCustom }.sortedBy { catalogOrder[it.id] ?: Int.MAX_VALUE }
                    updateState {
                        copy(
                            isLoading = false,
                            provided = system.filter { !it.isCommunity },
                            community = system.filter { it.isCommunity },
                            mine = published.filter { it.isCustom },
                            activeTemplateId = tracker?.templateId,
                            categories = TemplateCatalog.categories.filter { c -> published.any { it.category == c } } +
                                published.map { it.category }.filter { it !in TemplateCatalog.categories }.distinct()
                        )
                    }
                }
        }
    }

    override fun sendIntent(intent: DiscoverIntent) {
        when (intent) {
            DiscoverIntent.ToggleSearch -> updateState { copy(searchOpen = !searchOpen, query = if (searchOpen) "" else query) }
            is DiscoverIntent.Search -> updateState { copy(query = intent.query.take(40)) }
            is DiscoverIntent.SelectCategory -> updateState { copy(category = intent.category) }
            is DiscoverIntent.SelectTab -> updateState { copy(tab = intent.tab, category = null) }
        }
    }
}
