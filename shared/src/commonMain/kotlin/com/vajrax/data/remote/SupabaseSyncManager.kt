package com.vajrax.data.remote

import com.vajrax.data.local.VajraDatabase
import com.vajrax.domain.repository.AuthRepository
import io.ktor.client.HttpClient
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

data class SyncResult(
    val success: Boolean,
    val syncedPracticesCount: Int,
    val message: String
)

/**
 * Phase 17: Supabase Multiplatform Cloud Sync Manager.
 * Principle: Offline-First. Syncs local SQLite deltas to Supabase REST endpoints.
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
                message = "Supabase credentials not set. Running in 100% offline local mode."
            )
        }

        return try {
            val user = authRepository.getCurrentUser()
            if (user == null || user.isGuest) {
                return SyncResult(false, 0, "Guest mode active. Cloud sync skipped.")
            }

            val localPractices = queries.getAllPractices().executeAsList()
            val payloads = localPractices.map {
                RemotePracticePayload(
                    id = it.id,
                    title = it.title,
                    target_duration_minutes = it.targetDurationMinutes.toInt(),
                    minimum_duration_minutes = it.minimumDurationMinutes.toInt(),
                    tracking_mode = it.trackingMode,
                    is_active = it.isActive == 1L
                )
            }

            // Sync to Supabase PostgREST table /rest/v1/practices
            val url = "${SupabaseConfig.PROJECT_URL}/rest/v1/practices"
            httpClient.post(url) {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(payloads)
            }

            SyncResult(
                success = true,
                syncedPracticesCount = payloads.size,
                message = "Successfully backed up ${payloads.size} practices to Supabase."
            )
        } catch (e: Exception) {
            SyncResult(
                success = false,
                syncedPracticesCount = 0,
                message = "Sync failed: ${e.message}. Data safely stored locally in SQLite."
            )
        }
    }
}
