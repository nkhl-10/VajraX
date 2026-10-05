package com.vajrax.core.error

/**
 * Failures a future online layer (sync, accounts) can raise, each with a message a person can act
 * on. Local validation keeps using [com.vajrax.domain.usecase.RoutineException].
 */
sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** No connection. Local changes are kept and sent later. */
    class Offline(cause: Throwable? = null) : AppError("You're offline. Your changes are saved on this phone.", cause)

    /** The server didn't answer in time. */
    class Timeout(cause: Throwable? = null) : AppError("That took too long. Check your connection and try again.", cause)

    /** The server answered with an error ([status] is the HTTP status, if any). */
    class Server(val status: Int? = null) : AppError("Something went wrong on our side. Please try again in a moment.")

    /** Signed out or the session expired. */
    class SignedOut : AppError("Please sign in again to keep syncing.")

    /**
     * The server refused the request with a reason the user can act on ([message] is its sentence,
     * [code] its stable error code, [fieldErrors] per-field messages for forms).
     */
    class Rejected(
        val code: String,
        message: String,
        val fieldErrors: Map<String, String> = emptyMap(),
        val status: Int = 0
    ) :
        AppError(message)

    /** Anything else; [cause] is kept for logs. */
    class Unknown(cause: Throwable? = null) : AppError("Something went wrong. Please try again.", cause)
}
