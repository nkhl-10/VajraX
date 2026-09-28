package com.vajrax.ui.features.discover

data class DiscoverLifePath(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val accentColorHex: String,
    val practices: List<DiscoverPractice>
)

data class DiscoverPractice(
    val id: String,
    val title: String,
    val time: String?,
    val duration: String,
    val icon: String,
    val isRequired: Boolean
)
