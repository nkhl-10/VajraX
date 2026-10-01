package com.vajrax.contract

import com.vajrax.contract.sync.HabitPayload
import com.vajrax.contract.sync.SyncChange
import com.vajrax.contract.sync.SyncEntity
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContractJsonTest {
    @Test
    fun payloadsRoundTripAndIgnoreUnknownFields() {
        val habit = HabitPayload(title = "Read", targetDurationMinutes = 20, minimumDurationMinutes = 5)
        val element = ContractJson.encodeToJsonElement(HabitPayload.serializer(), habit).jsonObject
        val change = SyncChange(SyncEntity.HABIT, "hab_1", updatedAt = 1L, payload = element)
        val wire = ContractJson.encodeToString(SyncChange.serializer(), change)
            .replace("\"title\"", "\"futureField\":1,\"title\"")
        val back = ContractJson.decodeFromString(SyncChange.serializer(), wire)
        assertEquals(habit, ContractJson.decodeFromJsonElement(HabitPayload.serializer(), back.payload!!))
    }

    @Test
    fun deletionsCarryNoPayload() {
        val wire = ContractJson.encodeToString(
            SyncChange.serializer(),
            SyncChange(SyncEntity.GOAL, "goal_1", 5L, deleted = true)
        )
        assertNull(ContractJson.decodeFromString(SyncChange.serializer(), wire).payload)
    }
}
