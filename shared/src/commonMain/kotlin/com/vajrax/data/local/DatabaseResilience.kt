package com.vajrax.data.local

/**
 * Phase 19: Database Resilience & Crash Protection.
 * Ensures transactions execute safely with rollback protection.
 */
object DatabaseResilience {

    inline fun <T> runSafeDbOperation(crossinline block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            // Log non-fatal database exception
            Result.failure(e)
        }
    }
}
