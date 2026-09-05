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

    override suspend fun loginWithEmail(email: String, password: String): Result<UserSession> {
        // Test account verification
        if (email.trim().equals("admin@gmail.co", ignoreCase = true) && password == "Test@123") {
            val user = UserSession(
                userId = "user_admin_01",
                email = "admin@gmail.co",
                displayName = "Vajra Admin",
                isGuest = false,
                activePathId = "high_performance"
            )
            queries.insertOrUpdateUser(
                userId = user.userId,
                email = user.email,
                displayName = user.displayName,
                isGuest = if (user.isGuest) 1L else 0L,
                activePathId = user.activePathId,
                createdAt = "2026-08-30T00:00:00Z"
            )
            return Result.success(user)
        }

        // Generic email login fallback for offline/development mode
        if (email.contains("@") && password.length >= 6) {
            val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            val user = UserSession(
                userId = "user_${email.hashCode()}",
                email = email,
                displayName = name,
                isGuest = false,
                activePathId = "high_performance"
            )
            queries.insertOrUpdateUser(
                userId = user.userId,
                email = user.email,
                displayName = user.displayName,
                isGuest = if (user.isGuest) 1L else 0L,
                activePathId = user.activePathId,
                createdAt = "2026-08-30T00:00:00Z"
            )
            return Result.success(user)
        }

        return Result.failure(IllegalArgumentException("Invalid credentials. For testing use admin@gmail.co / Test@123"))
    }

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
}
