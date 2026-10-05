package com.vajrax.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Change capture for sync: an AFTER INSERT/UPDATE/DELETE trigger on every synced table records
 * the changed record in SyncOutbox while SyncState.enabled = 1 (signed in) and nothing is being
 * applied from the server (SyncState.applying = 0). Rules worth knowing:
 *  - planned (PENDING) days don't travel: every device plans them itself, and an auto-planned row
 *    must never overwrite a check-in made elsewhere;
 *  - only templates the user made (`custom_…`) and per-account settings travel.
 *
 * Created at runtime because SQLDelight can't compile trigger bodies that use NEW/OLD.
 * [install] drops and re-creates them, so a changed definition applies on the next start.
 */
object SyncTriggers {
    private const val NOW = "CAST((julianday('now') - 2440587.5) * 86400000 AS INTEGER)"
    private const val CAPTURING =
        "(SELECT enabled FROM SyncState WHERE id = 1) = 1 AND (SELECT applying FROM SyncState WHERE id = 1) = 0"
    private const val SYNCED_SETTINGS = "('onboarding_goals', 'wake_time', 'morning_minutes')"

    /** What a user-visible change to a day's record looks like (planning a time alone is not one). */
    private const val OCCURRENCE_CHANGED =
        "NEW.status IS NOT OLD.status OR NEW.completedAt IS NOT OLD.completedAt OR " +
            "NEW.durationMinutes IS NOT OLD.durationMinutes OR NEW.value IS NOT OLD.value OR " +
            "NEW.note IS NOT OLD.note OR NEW.skipReason IS NOT OLD.skipReason OR " +
            "(NEW.scheduledTime IS NOT OLD.scheduledTime AND NEW.status <> 'PENDING')"

    private class Capture(
        val name: String,
        val event: String,
        val table: String,
        val entity: String,
        val id: String,
        val condition: String?
    )

    private val captures: List<Capture> = buildList {
        fun all(
            prefix: String,
            table: String,
            entity: String,
            newId: String,
            oldId: String,
            newIf: String? = null,
            oldIf: String? = null
        ) {
            add(Capture("sync_${prefix}_insert", "INSERT", table, entity, newId, newIf))
            add(Capture("sync_${prefix}_update", "UPDATE", table, entity, newId, newIf))
            add(Capture("sync_${prefix}_delete", "DELETE", table, entity, oldId, oldIf))
        }
        all(
            "habit",
            "PracticeEntity",
            "habit",
            "NEW.id",
            "OLD.id",
            "NEW.trackerId IS NOT NULL",
            "OLD.trackerId IS NOT NULL"
        )
        all("tracker", "UserTemplateEntity", "tracker", "NEW.id", "OLD.id")
        add(
            Capture(
                "sync_occurrence_insert",
                "INSERT",
                "ActionRecordEntity",
                "occurrence",
                "NEW.id",
                "NEW.status <> 'PENDING'"
            )
        )
        add(
            Capture(
                "sync_occurrence_update",
                "UPDATE",
                "ActionRecordEntity",
                "occurrence",
                "NEW.id",
                "($OCCURRENCE_CHANGED)"
            )
        )
        add(
            Capture(
                "sync_occurrence_delete",
                "DELETE",
                "ActionRecordEntity",
                "occurrence",
                "OLD.id",
                "OLD.status <> 'PENDING'"
            )
        )
        all("goal", "GoalEntity", "goal", "NEW.id", "OLD.id")
        add(Capture("sync_goal_link_insert", "INSERT", "GoalHabitEntity", "goal", "NEW.goalId", null))
        add(Capture("sync_goal_link_delete", "DELETE", "GoalHabitEntity", "goal", "OLD.goalId", null))
        all("reflection", "ReflectionEntity", "reflection", "NEW.id", "OLD.id")
        all(
            "template",
            "TemplateEntity",
            "template",
            "NEW.id",
            "OLD.id",
            "NEW.id LIKE 'custom_%'",
            "OLD.id LIKE 'custom_%'"
        )
        all(
            "template_habit",
            "TemplateHabitEntity",
            "template",
            "NEW.templateId",
            "OLD.templateId",
            "NEW.templateId LIKE 'custom_%'",
            "OLD.templateId LIKE 'custom_%'"
        )
        all(
            "setting",
            "SettingEntity",
            "setting",
            "NEW.key",
            "OLD.key",
            "NEW.key IN $SYNCED_SETTINGS",
            "OLD.key IN $SYNCED_SETTINGS"
        )
    }

    private fun Capture.sql(): String = """
        CREATE TRIGGER $name AFTER $event ON $table
        BEGIN
            INSERT OR REPLACE INTO SyncOutbox(entity, entityId, changedAt)
            SELECT '$entity', $id, $NOW
            WHERE $CAPTURING${condition?.let { " AND $it" }.orEmpty()};
        END
    """.trimIndent()

    suspend fun install(driver: SqlDriver) {
        captures.forEach {
            driver.execute(null, "DROP TRIGGER IF EXISTS ${it.name}", 0).await()
            driver.execute(null, it.sql(), 0).await()
        }
    }
}
