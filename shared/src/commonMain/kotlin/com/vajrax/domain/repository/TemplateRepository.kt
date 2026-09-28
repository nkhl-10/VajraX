package com.vajrax.domain.repository

import com.vajrax.domain.template.DefaultTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Template library. System templates are immutable source definitions (spec 01 principle 6);
 * only custom templates (isCustom) can be created, edited or deleted.
 */
interface TemplateRepository {
    suspend fun getAllTemplates(): List<DefaultTemplate>
    suspend fun getProvidedTemplates(): List<DefaultTemplate>
    suspend fun getCustomTemplates(): List<DefaultTemplate>
    suspend fun getTemplateWithHabits(templateId: String): DefaultTemplate?

    /** Published (non-draft) templates, system + custom. */
    fun observeLibrary(): Flow<List<DefaultTemplate>>
    /** User custom templates including drafts. */
    fun observeCustomTemplates(): Flow<List<DefaultTemplate>>

    suspend fun saveCustomTemplate(template: DefaultTemplate)
    suspend fun updateTemplate(template: DefaultTemplate)
    suspend fun deleteTemplate(templateId: String)

    /** Re-seeds system templates when the bundled catalog version changes. */
    suspend fun seedSystemTemplatesIfNeeded()
}
