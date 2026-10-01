package com.vajrax.data

import com.vajrax.contract.ContractJson
import com.vajrax.contract.templates.TemplateLibrary
import com.vajrax.data.remote.toDefaultTemplate
import com.vajrax.data.remote.toLibraryTemplate
import com.vajrax.domain.template.TemplateCatalog
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The server's starting template library is the app's built-in one. This test keeps the two in step:
 * after changing [TemplateCatalog], run it with UPDATE_TEMPLATE_SEED=1 to rewrite the server seed.
 */
class TemplateSeedExportTest {
    private val seed = File("../server/src/main/resources/seed/templates.json")
    private val json = Json(ContractJson) { prettyPrint = true }

    @Test
    fun serverSeedMatchesTheBundledLibrary() {
        val expected = json.encodeToString(TemplateLibrary.serializer(), library()) + "\n"
        if (System.getenv("UPDATE_TEMPLATE_SEED") == "1" || !seed.exists()) {
            seed.parentFile.mkdirs()
            seed.writeText(expected)
        }
        assertEquals(expected, seed.readText(), "Template library changed: run this test with UPDATE_TEMPLATE_SEED=1")
    }

    @Test
    fun libraryTemplatesConvertBackWithoutLoss() {
        TemplateCatalog.all.forEach { template ->
            val back = template.toLibraryTemplate().toDefaultTemplate()
            assertEquals(
                template.habits.map { it.copy(sortOrder = 0) },
                back.habits.map { it.copy(sortOrder = 0) },
                template.id
            )
            assertEquals(template.name, back.name)
            assertEquals(template.durationDays, back.durationDays)
        }
    }

    private fun library() = TemplateLibrary(TemplateCatalog.VERSION, TemplateCatalog.all.map { it.toLibraryTemplate() })
}
