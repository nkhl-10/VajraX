package com.vajrax.data.remote

import com.vajrax.domain.repository.TemplateRepository

/**
 * Keeps the built-in template library current from the server, so new templates reach every app
 * without a release. The bundled library stays as the offline fallback.
 */
class TemplateLibrarySync(private val api: TemplateLibraryApi, private val templates: TemplateRepository) {
    suspend fun refresh() {
        val library = api.library(templates.remoteLibraryVersion()) ?: return
        if (library.templates.isEmpty()) return
        templates.replaceSystemTemplates(library.templates.map { it.toDefaultTemplate() }, library.version)
    }
}
