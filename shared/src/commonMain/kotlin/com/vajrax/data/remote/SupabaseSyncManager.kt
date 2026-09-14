package com.vajrax.data.remote

import com.vajrax.data.local.VajraDatabase
import com.vajrax.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class RemotePracticePayload(
    val id: String,
    val title: String,
    val target_duration_minutes: Int,
    val minimum_duration_minutes: Int,
    val tracking_mode: String,
    val is_active: Boolean
)

@Serializable
data class RemoteActionRecordPayload(
    val id: String,
    val practice_id: String,
    val date: String,
    val scheduled_time: String?,
    val status: String,
    val completed_at: String?,
    val duration_minutes: Int?
)

@Serializable
data class RemoteTemplatePayload(
    val id: String,
    val title: String,
    val description: String,
    val task_count: Int,
    val frequency: String,
    val author: String? = null,
    val is_community: Boolean = false,
    val is_bookmarked: Boolean = false
)

data class SyncResult(
    val success: Boolean,
    val syncedPracticesCount: Int,
    val syncedActionsCount: Int,
    val message: String
)

/**
 * Phase 17: Supabase Multiplatform Cloud Sync Manager.
 * Principle: Offline-First. Syncs local SQLite deltas to Supabase REST endpoints
 * and pulls remote updates idempotently.
 */
class SupabaseSyncManager(
    private val httpClient: HttpClient,
    private val database: VajraDatabase,
    private val authRepository: AuthRepository
) {
    private val queries = database.vajraDatabaseQueries

    suspend fun syncLocalDataToCloud(): SyncResult {
        if (!SupabaseConfig.isConfigured()) {
            return SyncResult(
                success = false,
                syncedPracticesCount = 0,
                syncedActionsCount = 0,
                message = "Supabase credentials not set. Running in 100% offline local mode."
            )
        }

        return try {
            val user = authRepository.getCurrentUser()
            if (user == null || user.isGuest) {
                return SyncResult(false, 0, 0, "Guest mode active. Cloud sync skipped.")
            }

            // 1. Sync Practices
            val localPractices = queries.getAllPractices().executeAsList()
            val practicePayloads = localPractices.map {
                RemotePracticePayload(
                    id = it.id,
                    title = it.title,
                    target_duration_minutes = it.targetDurationMinutes.toInt(),
                    minimum_duration_minutes = it.minimumDurationMinutes.toInt(),
                    tracking_mode = it.trackingMode,
                    is_active = it.isActive == 1L
                )
            }

            val practiceUrl = "${SupabaseConfig.PROJECT_URL}/rest/v1/practices"
            httpClient.post(practiceUrl) {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(practicePayloads)
            }

            // 2. Sync Action Records (Today & Week)
            val localActions = queries.getActionRecordsBetweenDates("2020-01-01", "2099-12-31").executeAsList()
            val actionPayloads = localActions.map {
                RemoteActionRecordPayload(
                    id = it.id,
                    practice_id = it.practiceId,
                    date = it.date,
                    scheduled_time = it.scheduledTime,
                    status = it.status,
                    completed_at = it.completedAt,
                    duration_minutes = it.durationMinutes?.toInt()
                )
            }

            val actionsUrl = "${SupabaseConfig.PROJECT_URL}/rest/v1/action_records"
            httpClient.post(actionsUrl) {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(actionPayloads)
            }

            SyncResult(
                success = true,
                syncedPracticesCount = practicePayloads.size,
                syncedActionsCount = actionPayloads.size,
                message = "Successfully synchronized ${practicePayloads.size} practices & ${actionPayloads.size} actions with Supabase."
            )
        } catch (e: Exception) {
            SyncResult(
                success = false,
                syncedPracticesCount = 0,
                syncedActionsCount = 0,
                message = "Sync failed: ${e.message}. Data safely stored locally in SQLite."
            )
        }
    }

    suspend fun pullRemoteTemplates(): List<RemoteTemplatePayload> {
        if (!SupabaseConfig.isConfigured()) return emptyList()
        return try {
            val url = "${SupabaseConfig.PROJECT_URL}/rest/v1/templates?select=*"
            val response = httpClient.get(url) {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                contentType(ContentType.Application.Json)
            }
            val remoteTemplates: List<RemoteTemplatePayload> = response.body()
            
            // Upsert into local SQLite
            database.transaction {
                remoteTemplates.forEach { t ->
                    queries.insertTemplate(
                        id = t.id,
                        title = t.title,
                        description = t.description,
                        taskCount = t.task_count.toLong(),
                        frequency = t.frequency,
                        author = t.author,
                        isCommunity = if (t.is_community) 1L else 0L,
                        isBookmarked = if (t.is_bookmarked) 1L else 0L
                    )
                }
            }
            remoteTemplates
        } catch (e: Exception) {
            emptyList()
        }
    }
}
