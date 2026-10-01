package com.vajrax.core.error

/**
 * Failures a future online layer (sync, accounts) can raise, each with a message a person can act
 * on. Local validation keeps using [com.vajrax.domain.usecase.RoutineException].
 */
sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** No connection. Local changes are kept and sent later. */
    class Offline : AppError("You're offline. Your changes are saved on this phone.")

    /** The server didn't answer in time. */
    class Timeout : AppError("That took too long. Check your connection and try again.")

    /** The server answered with an error ([status] is the HTTP status, if any). */
    class Server(val status: Int? = null) : AppError("Something went wrong on our side. Please try again in a moment.")

    /** Signed out or the session expired. */
    class SignedOut : AppError("Please sign in again to keep syncing.")

    /** Anything else; [cause] is kept for logs. */
    class Unknown(cause: Throwable? = null) : AppError("Something went wrong. Please try again.", cause)
}
