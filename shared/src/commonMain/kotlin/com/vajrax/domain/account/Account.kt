package com.vajrax.domain.account

import kotlinx.coroutines.flow.Flow

data class Account(val id: String, val email: String, val displayName: String)

sealed interface AccountState {
    data object SignedOut : AccountState
    data class SignedIn(val account: Account) : AccountState
}

/** The account side of the server. Failures are [com.vajrax.core.error.AppError]s. */
interface AccountGateway {
    /** Emits when the server ends the session (signed out elsewhere, password changed, account deleted). */
    val sessionEnded: Flow<Unit>

    suspend fun register(email: String, password: String, displayName: String): Account
    suspend fun signIn(email: String, password: String): Account

    /** The stored session, refreshed; null when there is none or it ended. */
    suspend fun restore(): Account?
    suspend fun cachedAccount(): Account?

    suspend fun signOut()
    suspend fun signOutEverywhere()
    suspend fun updateName(displayName: String): Account
    suspend fun changePassword(current: String, new: String)
    suspend fun requestPasswordReset(email: String)
    suspend fun deleteAccount(password: String)
}
