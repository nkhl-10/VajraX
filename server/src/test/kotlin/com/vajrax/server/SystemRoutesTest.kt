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

    @Test
    fun theWebsiteAndWebAppAreServedWithTheRightCachingAndSecurity() {
        val dir = kotlin.io.path.createTempDirectory("web").toFile()
        java.io.File(dir, "site").mkdirs()
        java.io.File(dir, "app").mkdirs()
        java.io.File(dir, "site/index.html").writeText("<h1>VAJRAX</h1>")
        java.io.File(dir, "site/privacy-policy.html").writeText("<h1>Privacy</h1>")
        java.io.File(dir, "site/404.html").writeText("<h1>Not here</h1>")
        java.io.File(dir, "app/index.html").writeText("<div>app</div>")
        java.io.File(dir, "app/0123456789abcdef0123.wasm").writeBytes(byteArrayOf(0, 97, 115, 109))
        java.io.File(dir, "app/sql-wasm.wasm").writeBytes(byteArrayOf(0, 97, 115, 109))
        withServer(testConfig(mapOf("WEB_DIR" to dir.absolutePath))) { client ->
            val home = client.get("/")
            assertEquals(HttpStatusCode.OK, home.status)
            assertEquals(true, home.headers["Content-Security-Policy"]?.contains("default-src 'self'"))
            assertEquals(HttpStatusCode.OK, client.get("/privacy-policy").status)
            val app = client.get("/app/")
            assertEquals(true, app.headers["Content-Security-Policy"]?.contains("'wasm-unsafe-eval'"))
            assertEquals(true, app.headers["Cache-Control"]?.contains("no-cache"))
            val wasm = client.get("/app/0123456789abcdef0123.wasm")
            assertEquals("application/wasm", wasm.headers["Content-Type"]?.substringBefore(';'))
            assertEquals(true, wasm.headers["Cache-Control"]?.contains("max-age=31536000"))
            assertEquals(HttpStatusCode.OK, client.get("/sql-wasm.wasm").status)
            val missing = client.get("/no-such-page")
            assertEquals(HttpStatusCode.NotFound, missing.status)
            assertEquals(true, missing.bodyAsText().contains("Not here"))
        }
    }
}
