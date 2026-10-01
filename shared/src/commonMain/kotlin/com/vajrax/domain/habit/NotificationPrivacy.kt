package com.vajrax.domain.habit

/** How much a reminder shows on the lock screen. Stored by [name]. */
enum class NotificationPrivacy {
    /** "Time for Morning walk". */
    FULL,
    /** "You have a habit due". */
    GENERIC,
    /** No details. */
    HIDDEN;

    companion object {
        val Default = GENERIC

        fun of(stored: String?): NotificationPrivacy = entries.firstOrNull { it.name == stored } ?: Default
    }
}
