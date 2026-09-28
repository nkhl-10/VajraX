package com.vajrax.ui.features.discover

import com.vajrax.domain.template.DefaultTemplate

data class DiscoverState(
    val isLoading: Boolean = true,
    val provided: List<DefaultTemplate> = emptyList(),
    val community: List<DefaultTemplate> = emptyList(),
    val mine: List<DefaultTemplate> = emptyList(),
    val activeTemplateId: String? = null,
    val searchOpen: Boolean = false,
    val query: String = "",
    val category: String? = null,
    val categories: List<String> = emptyList()
) {
    private fun List<DefaultTemplate>.filtered() = filter { t ->
        (category == null || t.category == category) &&
            (query.isBlank() || t.name.contains(query, true) || t.description.contains(query, true) ||
                t.habits.any { it.name.contains(query, true) })
    }

    val visibleProvided get() = provided.filtered()
    val visibleCommunity get() = community.filtered()
    val visibleMine get() = mine.filtered()
    val isFiltering get() = category != null || query.isNotBlank()
}

sealed interface DiscoverIntent {
    data object ToggleSearch : DiscoverIntent
    data class Search(val query: String) : DiscoverIntent
    data class SelectCategory(val category: String?) : DiscoverIntent
}
