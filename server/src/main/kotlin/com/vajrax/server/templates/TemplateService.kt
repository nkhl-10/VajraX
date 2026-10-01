package com.vajrax.server.templates

import com.vajrax.contract.ContractJson
import com.vajrax.contract.templates.LibraryTemplate
import com.vajrax.contract.templates.TemplateLibrary
import com.vajrax.server.db.Db
import com.vajrax.server.db.query
import com.vajrax.server.db.queryOne
import com.vajrax.server.db.update
import com.vajrax.server.http.ValidationException
import java.time.Clock
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * The public template library. Reads are cached per library version (one tiny query per request);
 * admins edit templates and then publish, which bumps the version every app checks.
 */
class TemplateService(private val db: Db, private val clock: Clock) {
    @Volatile private var cached: TemplateLibrary? = null

    suspend fun version(): String? = db.read { c ->
        c.queryOne(
            "SELECT version FROM library_meta WHERE id = 1"
        ) { it.getString(1) }
    }

    suspend fun library(): TemplateLibrary {
        val version = version() ?: return TemplateLibrary("0", emptyList())
        cached?.takeIf { it.version == version }?.let { return it }
        val templates = db.read { c ->
            c.query("SELECT payload FROM library_templates WHERE published ORDER BY sort_order, id") {
                ContractJson.decodeFromString(LibraryTemplate.serializer(), it.getString(1))
            }
        }
        return TemplateLibrary(version, templates).also { cached = it }
    }

    /** First start: the library the apps ship with (seed/templates.json, exported from the app). */
    suspend fun seedIfEmpty() {
        if (version() != null) return
        val seed = javaClass.getResourceAsStream("/seed/templates.json")?.bufferedReader()?.use { it.readText() } ?: return
        val library = ContractJson.decodeFromString(TemplateLibrary.serializer(), seed)
        db.tx { c ->
            library.templates.forEachIndexed { index, t -> upsert(c, t, index) }
            c.update(
                "INSERT INTO library_meta (id, version) VALUES (1, ?) ON CONFLICT (id) DO NOTHING",
                library.version
            )
        }
    }

    /** Saved as a draft of the next version; apps see it after [publish]. */
    suspend fun save(template: LibraryTemplate, sortOrder: Int?) {
        val errors = buildMap {
            if (template.id.isBlank() || template.id.length > ID_MAX || !ID.matches(template.id)) {
                put(
                    "id",
                    "Use letters, digits, - and _."
                )
            }
            if (template.title.isBlank()) put("title", "Add a title.")
            if (template.habits.isEmpty()) put("habits", "Add at least one habit.")
        }
        if (errors.isNotEmpty()) throw ValidationException(errors)
        db.tx { c -> upsert(c, template, sortOrder ?: nextSortOrder(c)) }
    }

    suspend fun delete(id: String) = db.tx { c -> c.update("DELETE FROM library_templates WHERE id = ?", id) }

    /** Makes the current templates the new library version. */
    suspend fun publish(): String {
        val version = VERSION_FORMAT.format(clock.instant())
        db.tx { c ->
            c.update(
                "INSERT INTO library_meta (id, version, updated_at) VALUES (1, ?, now()) " +
                    "ON CONFLICT (id) DO UPDATE SET version = EXCLUDED.version, updated_at = now()",
                version
            )
        }
        cached = null
        return version
    }

    private fun upsert(c: java.sql.Connection, t: LibraryTemplate, sortOrder: Int) = c.update(
        """
        INSERT INTO library_templates (id, payload, sort_order, published, updated_at) VALUES (?, CAST(? AS JSONB), ?, true, now())
        ON CONFLICT (id) DO UPDATE SET payload = EXCLUDED.payload, sort_order = EXCLUDED.sort_order, updated_at = now()
        """.trimIndent(),
        t.id,
        ContractJson.encodeToString(LibraryTemplate.serializer(), t),
        sortOrder
    )

    private fun nextSortOrder(c: java.sql.Connection): Int =
        c.queryOne("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM library_templates") { it.getInt(1) } ?: 0

    private companion object {
        val ID = Regex("^[A-Za-z0-9_-]+$")
        const val ID_MAX = 64
        val VERSION_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC)
    }
}
