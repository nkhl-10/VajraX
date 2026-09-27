package com.vajrax.ui.features.discover

// Represents a "Life Operating System"
data class DiscoverLifePath(
    val id: String,
    val title: String,
    val subtitle: String,
    val accentColorHex: String
)

// Represents a specific practice/habit tied to a path
data class DiscoverPractice(
    val id: String,
    val pathId: String,
    val title: String,
    val description: String,
    val isActive: Boolean
)
