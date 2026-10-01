import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
}

// The VAJRAX app in the browser: same shared UI and logic as Android, data synced through the
// server. `:webApp:wasmJsBrowserDistribution` builds the static bundle the server serves at /app/.
kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("vajrax")
        browser {
            commonWebpackConfig {
                outputFileName = "vajrax.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation(libs.koin.core)
            implementation(libs.coroutines.core)
            implementation(libs.sqldelight.runtime)
            // Named time zones for kotlinx-datetime in the browser.
            implementation(npm("@js-joda/timezone", "2.3.0"))
            // Ships sql.js's sql-wasm.wasm next to the bundle (see webpack.config.d/sqljs.js).
            implementation(devNpm("copy-webpack-plugin", "14.0.0"))
        }
    }
}
