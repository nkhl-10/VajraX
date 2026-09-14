package com.vajrax.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vajrax.data.local.VajraDatabase
import com.vajrax.data.remote.SupabaseSyncManager
import com.vajrax.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "John Doe",
    val email: String = "john@example.com",
    val avatarInitials: String = "JD",
    val activeTemplateTitle: String = "Morning Discipline",
    val activeTemplateDescription: String = "Build a structured morning routine",
    val currentDay: Int = 12,
    val totalDays: Int = 30,
    val progressPercent: Int = 40,
    val isSyncing: Boolean = false,
    val syncStatusMessage: String? = null
)

class ProfileViewModel(
    private val database: VajraDatabase,
    private val authRepository: AuthRepository,
    private val supabaseSyncManager: SupabaseSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val queries = database.vajraDatabaseQueries

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = queries.getCurrentUser().executeAsOneOrNull()
            val activeUserTemplate = queries.getActiveUserTemplate().executeAsOneOrNull()

            _uiState.update { state ->
                state.copy(
                    displayName = user?.displayName ?: "John Doe",
                    email = user?.email ?: "john@example.com",
                    avatarInitials = (user?.displayName ?: "JD")
                        .split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .ifBlank { "JD" },
                    activeTemplateTitle = activeUserTemplate?.title ?: "Morning Discipline",
                    activeTemplateDescription = activeUserTemplate?.description ?: "Build a structured morning routine",
                    currentDay = activeUserTemplate?.currentDay?.toInt() ?: 12,
                    totalDays = activeUserTemplate?.totalDays?.toInt() ?: 30,
                    progressPercent = activeUserTemplate?.progressPercent?.toInt() ?: 40
                )
            }
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSyncing = true, syncStatusMessage = "Syncing with Supabase...") }
            val result = supabaseSyncManager.syncLocalDataToCloud()
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    syncStatusMessage = result.message
                )
            }
        }
    }
}
