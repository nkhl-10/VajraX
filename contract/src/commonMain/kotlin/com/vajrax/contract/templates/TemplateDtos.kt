package com.vajrax.contract.templates

import com.vajrax.contract.sync.TemplateHabitPayload
import kotlinx.serialization.Serializable

/** The public template library. Clients keep the bundled copy and replace it when [version] changes. */
@Serializable
data class TemplateLibrary(
    val version: String,
    val templates: List<LibraryTemplate>
)

@Serializable
data class LibraryTemplate(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val frequency: String = "Daily",
    val author: String? = null,
    val isCommunity: Boolean = false,
    val durationDays: Int = 30,
    val recommendedFor: String? = null,
    val habits: List<TemplateHabitPayload>
)
