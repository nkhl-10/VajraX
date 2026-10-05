package com.vajrax.server

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.ContractJson
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.Problem
import com.vajrax.contract.auth.AuthResponse
import com.vajrax.contract.sync.HabitPayload
import com.vajrax.contract.sync.PullResponse
import com.vajrax.contract.sync.PushRequest
import com.vajrax.contract.sync.PushResponse
import com.vajrax.contract.sync.SyncChange
import com.vajrax.contract.sync.SyncEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.jsonObject
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SyncTest {
    private val clock = TestClock()
    private fun server(block: suspend io.ktor.server.testing.ApplicationTestBuilder.(HttpClient) -> Unit) =
        withServer(testConfig(), deps = { ServerDeps(it, TestDb.dataSource, clock, RecordingMailer()) }, block = block)

    private fun habit(title: String) =
        ContractJson.encodeToJsonElement(HabitPayload.serializer(), HabitPayload(title = title, targetDurationMinutes = 10, minimumDurationMinutes = 2)).jsonObject

    private fun change(id: String, title: String, at: Long, deleted: Boolean = false) =
        SyncChange(SyncEntity.HABIT, id, updatedAt = at, deleted = deleted, payload = if (deleted) null else habit(title))

    private suspend fun HttpClient.push(session: AuthResponse, device: String, vararg changes: SyncChange) =
        post(ApiPaths.SYNC_PUSH) { auth(session); json(PushRequest(device, changes.toList())) }

    private suspend fun HttpClient.pull(session: AuthResponse, since: Long = 0, limit: Int = 500) =
        get("${ApiPaths.SYNC_PULL}?since=$since&limit=$limit") { auth(session) }

    private val t0 = Instant0

    @Test
    fun pushedChangesComeBackOnAnotherDevice() = server { client ->
        val s = client.register()
        val pushed = client.push(s, "phone", change("hab_1", "Read", t0), change("hab_2", "Walk", t0)).body<PushResponse>()
        assertEquals(2, pushed.accepted.size)
        val pulled = client.pull(s).body<PullResponse>()
        assertEquals(listOf("hab_1", "hab_2"), pulled.changes.map { it.id })
        assertEquals("Read", pulled.changes.first().payload!!["title"].toString().trim('"'))
        assertEquals(pulled.changes.last().version, pulled.next)
        assertTrue(client.pull(s, since = pulled.next).body<PullResponse>().changes.isEmpty())
    }

    @Test
    fun theNewerEditWinsAndTheLoserLearnsTheCurrentRecord() = server { client ->
        val s = client.register()
        client.push(s, "phone", change("hab_1", "Read 20 pages", t0 + 2_000))
        val stale = client.push(s, "laptop", change("hab_1", "Read 5 pages", t0 + 1_000)).body<PushResponse>()
        assertTrue(stale.accepted.isEmpty())
        assertEquals("phone", stale.rejected.single().deviceId)

        val newer = client.push(s, "laptop", change("hab_1", "Read 30 pages", t0 + 3_000)).body<PushResponse>()
        assertEquals(1, newer.accepted.size)
        val record = client.pull(s).body<PullResponse>().changes.single()
        assertEquals("laptop", record.deviceId)
    }

    @Test
    fun sameTimeIsDecidedTheSameWayEverywhere() = server { client ->
        val s = client.register()
        client.push(s, "device-b", change("hab_1", "B", t0))
        assertEquals(0, client.push(s, "device-a", change("hab_1", "A", t0)).body<PushResponse>().accepted.size)
        assertEquals(1, client.push(s, "device-c", change("hab_1", "C", t0)).body<PushResponse>().accepted.size)

        // Bytes decide, as on the devices (not the database's language rules): "dev_a" > "dev_B".
        client.push(s, "dev_B", change("hab_2", "upper", t0))
        assertEquals(1, client.push(s, "dev_a", change("hab_2", "lower", t0)).body<PushResponse>().accepted.size)
    }

    @Test
    fun deletionsTravelAsTombstones() = server { client ->
        val s = client.register()
        client.push(s, "phone", change("hab_1", "Read", t0))
        client.push(s, "phone", change("hab_1", "", t0 + 1, deleted = true))
        val record = client.pull(s).body<PullResponse>().changes.single()
        assertTrue(record.deleted)
        assertNull(record.payload)
    }

    @Test
    fun pullPagesThroughLargeHistories() = server { client ->
        val s = client.register()
        client.push(s, "phone", *(1..7).map { change("hab_$it", "H$it", t0 + it) }.toTypedArray())
        val first = client.pull(s, limit = 3).body<PullResponse>()
        assertEquals(3, first.changes.size)
        assertTrue(first.hasMore)
        val second = client.pull(s, since = first.next, limit = 3).body<PullResponse>()
        val third = client.pull(s, since = second.next, limit = 3).body<PullResponse>()
        assertEquals(7, (first.changes + second.changes + third.changes).map { it.id }.toSet().size)
        assertTrue(!third.hasMore)
    }

    @Test
    fun accountsNeverSeeEachOthersData() = server { client ->
        val sam = client.register("sam@example.com")
        val ana = client.register("ana@example.com")
        client.push(sam, "phone", change("hab_1", "Sam's habit", t0))
        client.push(ana, "phone", change("hab_1", "Ana's habit", t0 + 5))
        val samRecords = client.pull(sam).body<PullResponse>().changes
        assertEquals(1, samRecords.size)
        assertTrue(samRecords.single().payload.toString().contains("Sam's habit"))
        assertEquals(HttpStatusCode.Unauthorized, client.get(ApiPaths.SYNC_PULL).status)
    }

    @Test
    fun badChangesAreRejectedWithReasons() = server { client ->
        val s = client.register()
        val response = client.push(
            s, "phone",
            SyncChange("spaceship", "x", t0, payload = habit("x")),
            SyncChange(SyncEntity.HABIT, "has spaces", t0, payload = habit("x")),
            SyncChange(SyncEntity.HABIT, "hab_9", t0, payload = null)
        )
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(3, response.body<Problem>().errors.size)
        val tooMany = client.push(s, "phone", *(1..501).map { change("h$it", "x", t0) }.toTypedArray())
        assertEquals(HttpStatusCode.PayloadTooLarge, tooMany.status)
    }

    @Test
    fun longOfflineDevicesAreToldToDownloadEverything() = server { client ->
        val s = client.register()
        client.push(s, "phone", change("hab_1", "Read", t0), change("hab_2", "Walk", t0))
        client.push(s, "phone", change("hab_1", "", t0 + 1, deleted = true))
        val oldCursor = 1L
        clock.advance(Duration.ofDays(200))
        TestDb.dataSource.connection.use { it.createStatement().execute("UPDATE sync_records SET updated_at = now() - interval '200 days' WHERE deleted") }
        assertEquals(1, SyncService_purge(clock))
        val fresh = client.login().body<AuthResponse>()
        val response = client.pull(fresh, since = oldCursor)
        assertEquals(HttpStatusCode.Gone, response.status)
        assertEquals(ErrorCodes.CURSOR_EXPIRED, response.body<Problem>().code)
        assertEquals(1, client.pull(fresh).body<PullResponse>().changes.size)
    }

    @Test
    fun aClockFarAheadCannotWinForever() = server { client ->
        val s = client.register()
        val farFuture = clock.millis() + Duration.ofDays(365).toMillis()
        client.push(s, "broken-clock", change("hab_1", "Future", farFuture))
        clock.advance(Duration.ofHours(1))
        val fresh = client.login().body<AuthResponse>()
        val fix = client.push(fresh, "phone", change("hab_1", "Now", clock.millis())).body<PushResponse>()
        assertEquals(1, fix.accepted.size)
    }

    private suspend fun SyncService_purge(clock: TestClock): Int =
        com.vajrax.server.sync.SyncService(com.vajrax.server.db.Db(TestDb.dataSource), clock).purgeTombstones(Duration.ofDays(180))

    private companion object {
        const val Instant0 = 1_790_000_000_000L
    }
}
