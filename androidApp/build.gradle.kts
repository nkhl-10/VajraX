import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        // Same bytecode level as compileOptions below (BuildConfig is compiled by javac).
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.core)
            implementation(libs.coroutines.core)
            implementation(libs.datetime)
            implementation(libs.glance.appwidget)
        }
    }
}

// Local, untracked settings (local.properties): keys never live in source control.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

fun localSetting(key: String): String = (localProperties.getProperty(key) ?: "").replace("\"", "")

android {
    namespace = "com.vajrax.android"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.vajrax.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SUPABASE_URL", "\"${localSetting("vajrax.supabase.url")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localSetting("vajrax.supabase.key")}\"")
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/main/res")
            java.srcDirs("src/main/java")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // Existing warnings live in lint-baseline.xml; new errors fail `lintDebug`. Refresh: `updateLintBaseline`.
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = true
        checkReleaseBuilds = false
        htmlReport = true
        xmlReport = true
    }
}

tasks.whenTaskAdded {
    if (name.contains("AarMetadata", ignoreCase = true)) {
        enabled = false
    }
}