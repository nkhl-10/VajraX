package com.vajrax.domain.usecase

import com.vajrax.core.time.AppClock
import com.vajrax.domain.repository.ProfileRepository

/** The on-device profile (name, optional email). */
class ProfileService(
    private val profiles: ProfileRepository,
    private val clock: AppClock
) {
    /** Saves name and email; an invalid email is rejected with a message the user can act on. */
    suspend fun save(name: String, email: String) {
        val cleanEmail = email.trim()
        ProfileRules.emailError(cleanEmail)?.let { throw RoutineException(it) }
        profiles.saveProfile(name.trim().take(NAME_MAX), cleanEmail, clock.nowIso())
    }

    /** Onboarding: keeps an email added earlier and only updates the name. */
    suspend fun saveName(name: String) {
        val existing = profiles.getProfile()
        profiles.saveProfile(name.trim().take(NAME_MAX), existing?.email ?: "", clock.nowIso())
    }

    companion object {
        const val NAME_MAX = 40
    }
}
