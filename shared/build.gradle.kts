plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        // Match compileOptions (Java 17) so the app module can inline shared code.
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    // The same app in the browser (webApp module); see doc/server.md.
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // API wire format shared with the server.
            implementation(project(":contract"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            // UI text lives in composeResources/values/strings.xml (Res.string.*), not in code.
            implementation(libs.compose.components.resources)
            implementation(libs.navigation.compose)
            
            // Core Architecture
            implementation(libs.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.datetime)
            
            // Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.serialization.json)
            
            // Database
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.async)
        }
        
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            // CIO lives on the platforms that have sockets (it was in commonMain before the web target).
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.activity.compose)
        }

        wasmJsMain.dependencies {
            implementation(libs.sqldelight.web.worker.driver)
            implementation(libs.ktor.client.js)
            implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
            implementation(npm("sql.js", "1.14.2"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
        }

        getByName("androidUnitTest").dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            // End-to-end tests start the real server jar on an embedded PostgreSQL (ServerEndToEndTest).
            implementation(libs.embedded.postgres)
            implementation(project.dependencies.enforcedPlatform(libs.embedded.postgres.binaries.bom))
        }
        
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.vajrax.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = true
        htmlReport = true
        xmlReport = true
    }
}

sqldelight {
    databases {
        create("VajraDatabase") {
            packageName.set("com.vajrax.data.local")
            // Async queries so the same database code also runs on the browser's worker-based SQLite.
            // Android and iOS drivers stay synchronous through Schema.synchronous().
            generateAsync.set(true)
        }
    }
}

// ServerEndToEndTest runs the server exactly as deployed: `java -jar vajrax-server.jar`.
tasks.withType<Test>().configureEach {
    dependsOn(":server:buildFatJar")
    systemProperty("vajrax.serverJar", project(":server").layout.buildDirectory.file("libs/vajrax-server.jar").get().asFile.absolutePath)
}

tasks.whenTaskAdded {
    if (name.contains("AarMetadata", ignoreCase = true)) {
        enabled = false
    }
}

compose.resources {
    packageOfResClass = "com.vajrax.resources"
    publicResClass = false
    generateResClass = always
}
