package com.vajrax.domain

/** Product rules that appear in more than one place; change them here, not at the call sites. */
object BusinessRules {
    /** Snooze from Home, the action sheet, notifications and widgets. */
    const val SNOOZE_MINUTES = 15

    /** A day counts towards the day streak when at least this share of its habits is done. */
    const val STREAK_DAY_THRESHOLD = 0.8f

    /** [STREAK_DAY_THRESHOLD] as a whole percentage, for copy ("80%+ done"). */
    const val STREAK_DAY_PERCENT = 80
}
