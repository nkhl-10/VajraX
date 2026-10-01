package com.vajrax.server

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.Problem
import com.vajrax.contract.auth.DeleteAccountRequest
import com.vajrax.contract.auth.RefreshRequest
import com.vajrax.contract.sync.PullResponse
import com.vajrax.contract.sync.PushRequest
import com.vajrax.contract.sync.SyncChange
import com.vajrax.contract.sync.SyncEntity
import com.vajrax.contract.templates.LibraryTemplate
import com.vajrax.contract.templates.TemplateLibrary
import com.vajrax.server.auth.AuditLog
import com.vajrax.server.auth.User
import com.vajrax.server.auth.UserStore
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemplatesAndAccountTest {
    @Test
    fun theLibraryStartsAsTheAppsBuiltInOneAndIsCacheable() = withServer { client ->
        val response = client.get(ApiPaths.TEMPLATES)
        assertEquals(HttpStatusCode.OK, response.status)
        val library = response.body<TemplateLibrary>()
        assertTrue(library.templates.size >= 10, "seeded ${library.templates.size}")
        val etag = response.headers[HttpHeaders.ETag]!!
        assertTrue(response.headers[HttpHeaders.CacheControl]!!.contains("public"))
        assertEquals(HttpStatusCode.NotModified, client.get(ApiPaths.TEMPLATES) { header(HttpHeaders.IfNoneMatch, etag) }.status)
        assertEquals(HttpStatusCode.NotModified, client.get("${ApiPaths.TEMPLATES}?since=${library.version}").status)
    }

    @Test
    fun onlyAdminsChangeTheLibraryAndAppsSeeItAfterPublishing() = withServer { client ->
        val user = client.register()
        val template = client.get(ApiPaths.TEMPLATES).body<TemplateLibrary>().templates.first().copy(id = "evening_reset", title = "Evening reset")
        assertEquals(HttpStatusCode.Forbidden, client.put("/v1/admin/templates/evening_reset") { auth(user); json(template) }.status)

        TestDb.dataSource.connection.use { UserStore.setRole(it, "sam@example.com", User.ROLE_ADMIN) }
        assertEquals(HttpStatusCode.NoContent, client.put("/v1/admin/templates/evening_reset") { auth(user); json(template) }.status)
        val before = client.get(ApiPaths.TEMPLATES).body<TemplateLibrary>()
        val published = client.post("/v1/admin/templates/publish") { auth(user) }
        assertEquals(HttpStatusCode.OK, published.status)
        val after = client.get(ApiPaths.TEMPLATES).body<TemplateLibrary>()
        assertTrue(after.version != before.version)
        assertTrue(after.templates.any { it.id == "evening_reset" })
        val bad = client.put("/v1/admin/templates/x") { auth(user); json(LibraryTemplate("x", "", "", "General", habits = emptyList())) }
        assertEquals(HttpStatusCode.UnprocessableEntity, bad.status)
    }

    @Test
    fun deletingTheAccountRemovesEverythingAndNeedsThePassword() = withServer { client ->
        val session = client.register()
        val habit = buildJsonObject { put("title", "Read") }
        client.post(ApiPaths.SYNC_PUSH) { auth(session); json(PushRequest("phone", listOf(SyncChange(SyncEntity.HABIT, "hab_1", 1L, payload = habit)))) }

        val wrong = client.delete(ApiPaths.ACCOUNT) { auth(session); json(DeleteAccountRequest("not my password")) }
        assertEquals(HttpStatusCode.Forbidden, wrong.status)
        assertEquals(ErrorCodes.INVALID_CREDENTIALS, wrong.body<Problem>().code)

        assertEquals(HttpStatusCode.NoContent, client.delete(ApiPaths.ACCOUNT) { auth(session); json(DeleteAccountRequest(GOOD_PASSWORD)) }.status)
        assertEquals(HttpStatusCode.Unauthorized, client.get(ApiPaths.SYNC_PULL) { auth(session) }.status)
        assertEquals(HttpStatusCode.Unauthorized, client.post(ApiPaths.REFRESH) { json(RefreshRequest(session.refreshToken)) }.status)
        assertEquals(HttpStatusCode.Unauthorized, client.login().status)
        TestDb.dataSource.connection.use { c ->
            val left = c.createStatement().executeQuery("SELECT (SELECT count(*) FROM sync_records) + (SELECT count(*) FROM refresh_tokens) + (SELECT count(*) FROM users)").use { it.next(); it.getInt(1) }
            assertEquals(0, left)
            // The trail keeps that it happened, by id only.
            assertTrue("account_deleted" in AuditLog.actionsFor(c, UUID.fromString(session.user.id)))
        }
        // The same email can start over.
        client.register()
        assertEquals(0, client.get(ApiPaths.SYNC_PULL) { auth(client.login().body()) }.body<PullResponse>().changes.size)
    }

    @Test
    fun maintenanceJobIsProtectedByItsToken() = withServer(testConfig(mapOf("JOBS_TOKEN" to "job-secret"))) { client ->
        assertEquals(HttpStatusCode.NotFound, client.post("/internal/jobs/maintenance").status)
        assertEquals(HttpStatusCode.OK, client.post("/internal/jobs/maintenance") { header(HttpHeaders.Authorization, "Bearer job-secret") }.status)
    }
}
