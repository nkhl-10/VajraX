package com.vajrax.ui.features.discover

import com.vajrax.domain.template.DefaultTemplate

enum class DiscoverTab(val label: String) { TEMPLATES("Templates"), ARC("Arc"), MINE("Mine") }

@androidx.compose.runtime.Immutable
data class DiscoverState(
    val isLoading: Boolean = true,
    val provided: List<DefaultTemplate> = emptyList(),
    val community: List<DefaultTemplate> = emptyList(),
    val mine: List<DefaultTemplate> = emptyList(),
    val activeTemplateId: String? = null,
    val searchOpen: Boolean = false,
    val query: String = "",
    val category: String? = null,
    val categories: List<String> = emptyList(),
    val tab: DiscoverTab = DiscoverTab.TEMPLATES
) {
    private fun List<DefaultTemplate>.filtered() = filter { t ->
        (category == null || t.category == category) &&
            (query.isBlank() || t.name.contains(query, true) || t.description.contains(query, true) ||
                t.habits.any { it.name.contains(query, true) })
    }

    private fun isArc(t: DefaultTemplate) = t.category == ARC

    val visibleProvided get() = when (tab) {
        DiscoverTab.TEMPLATES -> provided.filterNot(::isArc).filtered()
        DiscoverTab.ARC -> provided.filter(::isArc).filtered()
        DiscoverTab.MINE -> emptyList()
    }
    val visibleCommunity get() = if (tab == DiscoverTab.TEMPLATES) community.filtered() else emptyList()
    val visibleMine get() = if (tab == DiscoverTab.MINE) mine.filtered() else emptyList()
    /** Category chips only make sense on the main Templates tab. */
    val visibleCategories get() = if (tab == DiscoverTab.TEMPLATES) categories.filter { it != ARC } else emptyList()
    val isFiltering get() = category != null || query.isNotBlank()

    companion object {
        const val ARC = "Arc"
    }
}

sealed interface DiscoverIntent {
    data object ToggleSearch : DiscoverIntent
    data class Search(val query: String) : DiscoverIntent
    data class SelectCategory(val category: String?) : DiscoverIntent
    data class SelectTab(val tab: DiscoverTab) : DiscoverIntent
}
