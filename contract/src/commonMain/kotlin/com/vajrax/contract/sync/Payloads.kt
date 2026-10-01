package com.vajrax.contract.sync

import kotlinx.serialization.Serializable

/*
 * Payloads of synced records. Field names follow the app's local tables so a record converts
 * one-to-one. Dates are "YYYY-MM-DD", times "HH:mm", instants ISO-8601.
 */

/** A routine the user follows (entity [SyncEntity.TRACKER]). */
@Serializable
data class TrackerPayload(
    val templateId: String,
    val name: String? = null,
    val startDate: String? = null,
    val totalDays: Int = 30,
    val currentDay: Int = 1,
    val progressPercent: Int = 0,
    val isActive: Boolean = true,
    val status: String = "ACTIVE",
    val endedAt: String? = null
)

/** One habit of a routine (entity [SyncEntity.HABIT]). */
@Serializable
data class HabitPayload(
    val trackerId: String? = null,
    val title: String,
    val targetDurationMinutes: Int,
    val minimumDurationMinutes: Int,
    val preferredTime: String? = null,
    val trackingMode: String = "MANUAL",
    val isActive: Boolean = true,
    val category: String = "General",
    val icon: String = "check",
    val color: String = "indigo",
    val habitType: String = "BOOLEAN",
    val targetValue: Double = 1.0,
    val unit: String? = null,
    val scheduleType: String = "DAILY",
    val scheduleDays: String = "1,2,3,4,5,6,7",
    val weeklyTarget: Int = 0,
    val intervalDays: Int = 1,
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null,
    val startDate: String? = null,
    val archivedAt: String? = null,
    val sortOrder: Int = 0,
    val createdAt: String? = null
)

/** A habit's record for one day (entity [SyncEntity.OCCURRENCE]); id is `occ_<habitId>_<date>`. */
@Serializable
data class OccurrencePayload(
    val habitId: String,
    val date: String,
    val scheduledTime: String? = null,
    val status: String,
    val completedAt: String? = null,
    val durationMinutes: Int? = null,
    val value: Double? = null,
    val note: String? = null,
    val skipReason: String? = null
)

/** A goal and the habits that count towards it (entity [SyncEntity.GOAL]). */
@Serializable
data class GoalPayload(
    val title: String,
    val description: String? = null,
    val targetType: String = "COMPLETIONS",
    val targetValue: Double,
    val startDate: String,
    val endDate: String? = null,
    val status: String = "ACTIVE",
    val createdAt: String,
    val habitIds: List<String> = emptyList()
)

/** A weekly or monthly reflection (entity [SyncEntity.REFLECTION]). */
@Serializable
data class ReflectionPayload(
    val periodType: String,
    val periodStart: String,
    val achievements: String = "",
    val obstacles: String = "",
    val nextActions: String = "",
    val createdAt: String,
    val updatedAt: String
)

/** A template the user made, with its habits (entity [SyncEntity.TEMPLATE]). */
@Serializable
data class TemplatePayload(
    val title: String,
    val description: String,
    val category: String = "General",
    val frequency: String = "Daily",
    val author: String? = null,
    val durationDays: Int = 30,
    val isDraft: Boolean = false,
    val recommendedFor: String? = null,
    val updatedAt: String? = null,
    val habits: List<TemplateHabitPayload> = emptyList()
)

@Serializable
data class TemplateHabitPayload(
    val name: String,
    val startTime: String,
    val durationMinutes: Int,
    val trackingMode: String = "MANUAL",
    val target: String = "",
    val repeatDays: String = "",
    val reminderEnabled: Boolean = true,
    val sortOrder: Int = 0,
    val category: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val habitType: String = "BOOLEAN",
    val targetValue: Double = 1.0,
    val unit: String? = null,
    val scheduleType: String = "DAILY",
    val weeklyTarget: Int = 0,
    val intervalDays: Int = 1
)

/** One per-account preference (entity [SyncEntity.SETTING]); id is the setting key. */
@Serializable
data class SettingPayload(val value: String)
