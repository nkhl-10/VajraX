package com.vajrax.data.remote

/**
 * Phase 17: Supabase Project Configuration.
 * Connected to live project.
 */
object SupabaseConfig {
    // 1. Supabase Project URL
    var PROJECT_URL: String = "https://qxjtxjuxpucsgcpjamfi.supabase.co"

    // 2. Supabase Anon / Publishable Key
    var ANON_KEY: String = "sb_publishable_ZqpVXKC7iU6OnjPkZgKuYw_IMRcZLPY"

    fun isConfigured(): Boolean {
        return PROJECT_URL.isNotBlank() && ANON_KEY.isNotBlank()
    }
}
