package com.vajrax.domain.habit

/**
 * Picks the single "current" habit of the day. Home, the widgets and the "Next: …" message all
 * use this, so they always agree on what comes next.
 *
 * Order of preference:
 *  1. an open habit whose time window contains now (the latest-starting one);
 *  2. a pending habit starting within [SOON_MINUTES];
 *  3. the first open habit after the one completed most recently, so marking the current habit
 *     done moves on to the next one in the list;
 *  4. the first open habit of the day.
 * Snoozed habits only come back through rule 1 (when their new time arrives), so snoozing
 * always moves focus on.
 */
object NowPicker {
    const val SOON_MINUTES = 30

    /** One row of today's list, in display (time) order. */
    data class Candidate(
        val id: String,
        /** Minute of day, or null for "anytime". */
        val start: Int?,
        val durationMinutes: Int,
        /** Still needs doing today (weekly targets already met count as not open). */
        val open: Boolean,
        val snoozed: Boolean = false,
        /** ISO timestamp of completion; used to find where the user is in the list. */
        val completedAt: String? = null
    )

    fun pick(items: List<Candidate>, now: Int): String? {
        val open = items.filter { it.open }
        if (open.isEmpty()) return null
        open.filter { c -> c.start != null && c.start <= now && now < c.start + c.durationMinutes }
            .maxByOrNull { it.start ?: 0 }?.let { return it.id }
        val active = open.filterNot { it.snoozed }
        active.filter { c -> c.start != null && c.start > now && c.start - now <= SOON_MINUTES }
            .minByOrNull { it.start ?: 0 }?.let { return it.id }
        val lastDone = items.withIndex().filter { it.value.completedAt != null }.maxByOrNull { it.value.completedAt!! }?.index
        if (lastDone != null) {
            items.drop(lastDone + 1).firstOrNull { it.open && !it.snoozed }?.let { return it.id }
        }
        return (active.firstOrNull() ?: open.first()).id
    }

    /** What becomes current once [currentId] is marked done at [completedAt]. */
    fun next(items: List<Candidate>, now: Int, currentId: String, completedAt: String): String? =
        pick(items.map { if (it.id == currentId) it.copy(open = false, completedAt = completedAt) else it }, now)
}
