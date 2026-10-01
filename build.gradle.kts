import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    // This declares the plugins to be used across the project in submodules
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktor) apply false
}

// Static analysis for every module: detekt rules plus ktlint formatting (detekt-formatting),
// both reading /.editorconfig. Existing findings live in each module's detekt-baseline.xml, so
// `./gradlew detekt` fails only on new issues; regenerate with `./gradlew detektBaseline`.
val detektFormatting = libs.detekt.formatting
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        baseline = file("detekt-baseline.xml")
        source.setFrom(
            files(
                "src/commonMain/kotlin", "src/androidMain/kotlin", "src/iosMain/kotlin",
                "src/commonTest/kotlin", "src/androidUnitTest/kotlin",
                "src/main/java", "src/main/kotlin"
            )
        )
        parallel = true
        // `-PdetektAutoCorrect` lets the ktlint rules fix formatting in place (use per module).
        autoCorrect = project.hasProperty("detektAutoCorrect")
    }
    dependencies { add("detektPlugins", detektFormatting) }
    // detekt 1.23 runs on its own Kotlin compiler; keep the project's newer Kotlin out of its classpath.
    configurations.matching { it.name == "detekt" }.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") useVersion("2.0.21")
        }
    }
    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        exclude("**/build/**", "**/generated/**")
        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
}
