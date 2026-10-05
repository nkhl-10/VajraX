package com.vajrax.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.vajrax.app.VajraApp
import com.vajrax.core.log.VxLog
import com.vajrax.core.time.TimeFormat
import com.vajrax.data.local.DatabaseDriverFactory
import com.vajrax.data.local.createWebDatabaseDriver
import com.vajrax.data.remote.ApiConfig
import com.vajrax.di.initKoin
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.dsl.module

@JsModule("@js-joda/timezone")
external object JsJodaTimeZoneModule

/** Referencing the module loads the time-zone database (kotlinx-datetime on the web). */
@Suppress("unused")
private val timeZones = JsJodaTimeZoneModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    VxLog.sink = { tag, message, error -> consoleLog("$tag: $message ${error?.stackTraceToString().orEmpty()}") }
    TimeFormat.use24Hour = uses24HourClock()
    MainScope().launch {
        // The browser database is created asynchronously before anything reads it.
        val driver = try {
            createWebDatabaseDriver()
        } catch (e: Throwable) {
            VxLog.w("Web", "Couldn't start the database", e)
            document.getElementById("loading")?.textContent = "VAJRAX couldn't start in this browser. Please reload the page."
            return@launch
        }
        initKoin {
            modules(
                module {
                    single { DatabaseDriverFactory(driver) }
                    // Same origin as the page: the server serves the app at /app/ and the API at /v1/.
                    single { ApiConfig(baseUrl = window.location.origin, isWeb = true, deviceName = "Web browser") }
                }
            )
        }
        document.getElementById("loading")?.remove()
        ComposeViewport(document.body!!) { VajraApp(WebPlatformActions) }
    }
}

private fun uses24HourClock(): Boolean =
    js("new Intl.DateTimeFormat(undefined, { hour: 'numeric' }).resolvedOptions().hour12 !== true")

private fun consoleLog(message: String): Unit = js("console.log(message)")
