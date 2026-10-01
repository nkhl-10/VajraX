package com.vajrax.contract.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Kinds of records that sync. Payload shapes are in Payloads.kt. */
object SyncEntity {
    const val TRACKER = "tracker"
    const val HABIT = "habit"
    const val OCCURRENCE = "occurrence"
    const val GOAL = "goal"
    const val REFLECTION = "reflection"
    const val TEMPLATE = "template"
    const val SETTING = "setting"

    val all: Set<String> = setOf(TRACKER, HABIT, OCCURRENCE, GOAL, REFLECTION, TEMPLATE, SETTING)
}

/**
 * One record as a client changed it. [updatedAt] is the client's clock (epoch ms) when the change
 * was made; the newer `(updatedAt, deviceId)` wins. [payload] is null for deletions.
 */
@Serializable
data class SyncChange(
    val entity: String,
    val id: String,
    val updatedAt: Long,
    val deleted: Boolean = false,
    val schema: Int = PAYLOAD_SCHEMA,
    val payload: JsonObject? = null
)

@Serializable
data class PushRequest(val deviceId: String, val changes: List<SyncChange>)

@Serializable
data class AcceptedChange(val entity: String, val id: String, val version: Long)

@Serializable
data class PushResponse(
    val accepted: List<AcceptedChange>,
    /** Changes that lost to a newer record; the current server record is returned for each. */
    val rejected: List<SyncRecord>,
    val serverTime: Long
)

/** A record as the server stores it. [version] grows with every accepted change for the account. */
@Serializable
data class SyncRecord(
    val entity: String,
    val id: String,
    val updatedAt: Long,
    val deleted: Boolean,
    val schema: Int,
    val payload: JsonObject? = null,
    val deviceId: String,
    val version: Long
)

@Serializable
data class PullResponse(
    val changes: List<SyncRecord>,
    /** Pass as `since` on the next pull. */
    val next: Long,
    val hasMore: Boolean
)

/** Current payload schema; bump when a payload shape changes incompatibly. */
const val PAYLOAD_SCHEMA = 1
