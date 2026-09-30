package com.vajrax.core.time

/**
 * Habit times are stored as 24h "HH:mm" strings so they sort correctly in SQL.
 * Legacy rows used "06:30 AM" or ranges like "09:00 - 10:00 AM"; [normalize] accepts all of them.
 */
object TimeFormat {

    /**
     * Follows the device's 12/24-hour setting; the platform updates it at start-up and whenever
     * the setting may have changed. Stored values are always 24h "HH:mm", only display changes.
     */
    @kotlin.concurrent.Volatile
    var use24Hour: Boolean = false

    /** Parses "HH:mm", "H:mm", "hh:mm AM", or the start of a range into minutes since midnight. */
    fun toMinutes(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val text = raw.trim().uppercase()
        val first = text.split("-").first().trim()
        val meridiem = when {
            first.endsWith("AM") -> "AM"
            first.endsWith("PM") -> "PM"
            // Range like "09:00 - 10:00 AM" carries the meridiem only at the end.
            text.endsWith("AM") && text.contains("-") -> "AM"
            text.endsWith("PM") && text.contains("-") -> "PM"
            else -> null
        }
        val digits = first.removeSuffix("AM").removeSuffix("PM").trim()
        val parts = digits.split(":")
        var hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
        if (hour !in 0..23 || minute !in 0..59) return null
        if (meridiem == "PM" && hour < 12) hour += 12
        if (meridiem == "AM" && hour == 12) hour = 0
        return hour * 60 + minute
    }

    fun fromMinutes(minutes: Int): String {
        val m = ((minutes % 1440) + 1440) % 1440
        val h = m / 60
        val mm = m % 60
        return "${h.toString().padStart(2, '0')}:${mm.toString().padStart(2, '0')}"
    }

    fun normalize(raw: String?): String? = toMinutes(raw)?.let { fromMinutes(it) }

    /** "6:30 AM", or "06:30" on 24-hour devices */
    fun display(hhmm: String?): String {
        val minutes = toMinutes(hhmm) ?: return ""
        return displayMinutes(minutes)
    }

    fun displayMinutes(minutes: Int): String {
        val m = ((minutes % 1440) + 1440) % 1440
        if (use24Hour) return fromMinutes(m)
        val h24 = m / 60
        val mm = m % 60
        val suffix = if (h24 < 12) "AM" else "PM"
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return "$h12:${mm.toString().padStart(2, '0')} $suffix"
    }

    /** "09:00 AM" style used by the calendar matrix. */
    fun displayPadded(hhmm: String?): String {
        val minutes = toMinutes(hhmm) ?: return ""
        if (use24Hour) return fromMinutes(minutes)
        val h24 = minutes / 60
        val mm = minutes % 60
        val suffix = if (h24 < 12) "AM" else "PM"
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return "${h12.toString().padStart(2, '0')}:${mm.toString().padStart(2, '0')} $suffix"
    }

    /** "09:00 - 10:00 AM" when the habit spans 30 minutes or more, otherwise "06:30 AM". */
    fun displayRange(hhmm: String?, durationMinutes: Int): String {
        val start = toMinutes(hhmm) ?: return ""
        if (durationMinutes < 30) return displayPadded(hhmm)
        val end = start + durationMinutes
        val startText = displayPadded(fromMinutes(start))
        val endText = displayPadded(fromMinutes(end))
        if (use24Hour) return "$startText - $endText"
        val sameMeridiem = startText.takeLast(2) == endText.takeLast(2)
        return if (sameMeridiem) "${startText.dropLast(3)} - $endText" else "$startText - $endText"
    }

    /** "45 min", "1 h", "1 h 30 min" */
    fun duration(minutes: Int): String = when {
        minutes <= 0 -> "—"
        minutes < 60 -> "$minutes min"
        minutes % 60 == 0 -> "${minutes / 60} h"
        else -> "${minutes / 60} h ${minutes % 60} min"
    }
}
