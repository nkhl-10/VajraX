package com.vajrax.server

import com.vajrax.contract.ApiPaths
import com.vajrax.contract.ErrorCodes
import com.vajrax.contract.Problem
import com.vajrax.contract.VersionInfo
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SystemRoutesTest {
    @Test
    fun liveAndReadyWhenTheDatabaseAnswers() = withServer { client ->
        assertEquals(HttpStatusCode.OK, client.get("/healthz").status)
        val ready = client.get("/readyz")
        assertEquals(HttpStatusCode.OK, ready.status)
        assertEquals("ready", ready.bodyAsText())
    }

    @Test
    fun versionTellsAppsTheMinimumVersion() = withServer { client ->
        val info = client.get(ApiPaths.VERSION).body<VersionInfo>()
        assertEquals("1.0", info.minAppVersion)
        assertNotNull(info.serverTime)
    }

    @Test
    fun unknownApiPathIsAProblemWithTheRequestId() = withServer { client ->
        val response = client.get("/v1/nothing-here") { header("X-Request-Id", "test-request-1") }
        assertEquals(HttpStatusCode.NotFound, response.status)
        val problem = response.body<Problem>()
        assertEquals(ErrorCodes.NOT_FOUND, problem.code)
        assertEquals("test-request-1", problem.requestId)
        assertEquals("test-request-1", response.headers["X-Request-Id"])
        assertEquals("nosniff", response.headers["X-Content-Type-Options"])
    }

    @Test
    fun metricsStayHiddenWithoutTheToken() = withServer(testConfig(mapOf("METRICS_TOKEN" to "secret-token"))) { client ->
        assertEquals(HttpStatusCode.NotFound, client.get("/internal/metrics").status)
        val ok = client.get("/internal/metrics") { header("Authorization", "Bearer secret-token") }
        assertEquals(HttpStatusCode.OK, ok.status)
    }
}
