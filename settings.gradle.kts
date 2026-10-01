pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VajraX"
include(":contract")
include(":shared")
include(":androidApp")
include(":webApp")
include(":server")