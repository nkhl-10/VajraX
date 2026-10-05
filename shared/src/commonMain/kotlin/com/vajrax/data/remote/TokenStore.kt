package com.vajrax.data.remote

/**
 * Where a signed-in session survives app restarts. Android keeps the refresh token encrypted with
 * a Keystore key; the web keeps it in an HttpOnly cookie the page can't read ([usesCookie]), so only
 * the cached account details are stored there.
 */
interface TokenStore {
    val usesCookie: Boolean get() = false

    suspend fun refreshToken(): String?

    /** The signed-in account as JSON, to show it before the network answers. */
    suspend fun cachedAccount(): String?

    suspend fun save(refreshToken: String?, accountJson: String)

    suspend fun clear()
}

/** Keeps nothing across restarts; for tests and platforms without secure storage yet. */
class MemoryTokenStore(override val usesCookie: Boolean = false) : TokenStore {
    private var refresh: String? = null
    private var account: String? = null

    override suspend fun refreshToken(): String? = refresh

    override suspend fun cachedAccount(): String? = account

    override suspend fun save(refreshToken: String?, accountJson: String) {
        if (!usesCookie) refresh = refreshToken
        account = accountJson
    }

    override suspend fun clear() {
        refresh = null
        account = null
    }
}
