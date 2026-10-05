package com.vajrax.server.web

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.io.File

/**
 * The website at `/` and the web app at `/app/`, from WEB_DIR (see `:server:stageWeb`).
 * Hashed bundle files are cached forever; HTML always revalidates so a deploy shows at once.
 */
fun Route.staticRoutes(webDir: String) {
    val root = File(webDir)
    val site = File(root, "site")
    val app = File(root, "app")

    if (app.isDirectory) {
        // sql.js inside the web app's database worker loads /sql-wasm.wasm from the site root.
        get("/sql-wasm.wasm") {
            call.response.header(HttpHeaders.CacheControl, "public, max-age=86400")
            call.respondFile(File(app, "sql-wasm.wasm"))
        }
        staticFiles("/app", app) {
            default("index.html")
            contentType { file -> if (file.extension == "wasm") ContentType("application", "wasm") else null }
            cacheControl { file -> listOf(cacheFor(file)) }
            modify { file, call ->
                if (file.extension == "html") call.response.header("Content-Security-Policy", APP_CSP)
            }
        }
    }
    if (site.isDirectory) {
        // The home page is served explicitly: a default file would also answer every unknown address.
        get("/") {
            call.response.header("Content-Security-Policy", SITE_CSP)
            call.response.header(HttpHeaders.CacheControl, "no-cache")
            call.respondFile(File(site, "index.html"))
        }
        staticFiles("/", site) {
            extensions("html")
            cacheControl { file -> listOf(cacheFor(file)) }
            modify { file, call ->
                if (file.extension == "html") call.response.header("Content-Security-Policy", SITE_CSP)
            }
        }
    }
}

private fun cacheFor(file: File): CacheControl = when {
    file.extension == "html" -> CacheControl.NoCache(null)
    HASHED.matches(
        file.name
    ) -> CacheControl.MaxAge(maxAgeSeconds = 31_536_000, visibility = CacheControl.Visibility.Public)
    else -> CacheControl.MaxAge(maxAgeSeconds = 3_600, visibility = CacheControl.Visibility.Public)
}

/** Webpack names bundle files by content hash, e.g. 0c47239810795199.wasm. */
private val HASHED = Regex("^[0-9a-f]{16,}\\.[a-z0-9]+$")

/** Compose for Web needs WebAssembly compilation; everything else stays on our own origin. */
private const val APP_CSP =
    "default-src 'self'; script-src 'self' 'wasm-unsafe-eval'; worker-src 'self' blob:; connect-src 'self'; " +
        "img-src 'self' data: blob:; style-src 'self' 'unsafe-inline'; font-src 'self' data:; frame-ancestors 'none'; base-uri 'self'"

private const val SITE_CSP =
    "default-src 'self'; script-src 'self'; connect-src 'self'; img-src 'self' data:; style-src 'self'; " +
        "font-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'"
