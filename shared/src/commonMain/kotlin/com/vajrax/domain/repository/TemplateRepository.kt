package com.vajrax.domain.repository

import com.vajrax.domain.template.DefaultTemplate

interface TemplateRepository {
    suspend fun getAllTemplates(): List<DefaultTemplate>
    suspend fun getProvidedTemplates(): List<DefaultTemplate>
    suspend fun getCustomTemplates(): List<DefaultTemplate>
    
    suspend fun getTemplateWithHabits(templateId: String): DefaultTemplate?
    
    suspend fun saveCustomTemplate(template: DefaultTemplate)
    suspend fun updateTemplate(template: DefaultTemplate)
    suspend fun deleteTemplate(templateId: String)
}
