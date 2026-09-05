package com.vajrax.domain.repository

data class UserSession(
    val userId: String,
    val email: String,
    val displayName: String,
    val isGuest: Boolean,
    val activePathId: String?
)

interface AuthRepository {
    suspend fun getCurrentUser(): UserSession?
    suspend fun loginWithEmail(email: String, password: String):Result<UserSession>
    suspend fun continueAsGuest(): UserSession
    suspend fun logout()
}
