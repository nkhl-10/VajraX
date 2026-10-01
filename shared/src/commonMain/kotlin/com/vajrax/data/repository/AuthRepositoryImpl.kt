package com.vajrax.data.repository

import com.vajrax.data.local.VajraDatabase
import com.vajrax.domain.repository.AuthRepository
import com.vajrax.domain.repository.UserSession

class AuthRepositoryImpl(
    private val database: VajraDatabase
) : AuthRepository {
    private val queries = database.vajraDatabaseQueries

    override suspend fun getCurrentUser(): UserSession? {
        return queries.getCurrentUser().executeAsOneOrNull()?.let {
            UserSession(
                userId = it.userId,
                email = it.email,
                displayName = it.displayName,
                isGuest = it.isGuest == 1L,
                activePathId = it.activePathId
            )
        }
    }

    /**
     * There is no account server yet, so email sign-in always fails with a plain message. It used
     * to accept a built-in test account and any address offline; credentials must never ship in
     * the app. The online phase replaces this with the real auth API.
     */
    override suspend fun loginWithEmail(email: String, password: String): Result<UserSession> =
        Result.failure(IllegalStateException(SIGN_IN_UNAVAILABLE))

    override suspend fun continueAsGuest(): UserSession {
        val guest = UserSession(
            userId = "guest_temp_01",
            email = "guest@vajrax.local",
            displayName = "Vajra Guest",
            isGuest = true,
            activePathId = "high_performance"
        )
        queries.insertOrUpdateUser(
            userId = guest.userId,
            email = guest.email,
            displayName = guest.displayName,
            isGuest = 1L,
            activePathId = guest.activePathId,
            createdAt = "2026-08-30T00:00:00Z"
        )
        return guest
    }

    override suspend fun logout() {
        queries.clearUserSession()
    }

    companion object {
        const val SIGN_IN_UNAVAILABLE = "Sign-in isn't available yet. VAJRAX works fully offline on this phone."
    }
}
