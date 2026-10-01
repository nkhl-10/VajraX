package com.vajrax.data.remote

/**
 * Supabase project settings for the (future) cloud sync. Not stored in source: the Android app
 * fills them at start-up from BuildConfig, which reads `vajrax.supabase.url` and
 * `vajrax.supabase.key` from the untracked local.properties. Empty means sync stays off.
 */
object SupabaseConfig {
    var PROJECT_URL: String = ""
        private set

    var ANON_KEY: String = ""
        private set

    fun configure(projectUrl: String, anonKey: String) {
        PROJECT_URL = projectUrl.trim()
        ANON_KEY = anonKey.trim()
    }

    fun isConfigured(): Boolean {
        return PROJECT_URL.isNotBlank() && ANON_KEY.isNotBlank()
    }
}
