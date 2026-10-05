import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

buildscript {
    dependencies {
        // Renders doc/legal/*.md into the website's legal pages at build time (one source of truth).
        classpath("org.commonmark:commonmark:0.24.0")
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

group = "com.vajrax"
version = "1.0.0"

application {
    mainClass.set("com.vajrax.server.MainKt")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":contract"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.rate.limit)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.ktor.server.conditional.headers)
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.prometheus)

    implementation(libs.postgresql)
    implementation(libs.hikari)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    // Connects to Cloud SQL through its connector when DB_INSTANCE is set (no public IP needed).
    implementation(libs.cloudsql.postgres.socket.factory)

    implementation(libs.bouncycastle)
    implementation(libs.angus.mail)
    implementation(libs.logback.classic)
    implementation(libs.logstash.encoder)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
    // Real PostgreSQL for tests and local runs, without Docker.
    testImplementation(libs.embedded.postgres)
    testImplementation(enforcedPlatform(libs.embedded.postgres.binaries.bom))
}

ktor {
    fatJar {
        archiveFileName.set("vajrax-server.jar")
    }
    docker {
        jreVersion.set(JavaVersion.VERSION_17)
        // Same runtime as server/Dockerfile: small, no shell, runs as a non-root user.
        customBaseImage.set("gcr.io/distroless/java17-debian12:nonroot")
        localImageName.set("vajrax-server")
        imageTag.set(version.toString())
        environmentVariable("WEB_DIR", "/app/web")
    }
}

/*
 * Jib builds the image without Docker (:server:buildImage → build/jib-image.tar, :server:publishImage).
 * The Dockerfile is the main path for Cloud Build; this one proves the image on machines without Docker.
 */
jib {
    container {
        user = "65532"
        ports = listOf("8080")
        jvmFlags = listOf("-XX:MaxRAMPercentage=70", "-XX:+ExitOnOutOfMemoryError")
    }
    extraDirectories {
        paths {
            path {
                setFrom(layout.buildDirectory.dir("web").get().asFile)
                into = "/app/web"
            }
        }
    }
}

tasks.matching { it.name.startsWith("jib") }.configureEach { dependsOn("stageWeb") }

tasks.test {
    useJUnitPlatform()
    // Embedded PostgreSQL starts once per test JVM.
    maxParallelForks = 1
}

/** doc/legal/<name>.md → <name>.html inside web/site/legal-template.html. */
val generateLegalPages by tasks.registering {
    group = "web"
    description = "Builds the website's privacy and terms pages from doc/legal."
    val sources = rootProject.fileTree("doc/legal") { include("*.md") }
    val template = rootProject.file("web/site/legal-template.html")
    val out = layout.buildDirectory.dir("generated/legal")
    inputs.files(sources, template)
    outputs.dir(out)
    doLast {
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()
        val dir = out.get().asFile.apply { deleteRecursively(); mkdirs() }
        sources.forEach { md ->
            val text = md.readText().replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "").trim()
            val title = text.lineSequence().firstOrNull { it.startsWith("# ") }?.removePrefix("# ")?.trim() ?: md.nameWithoutExtension
            val html = renderer.render(parser.parse(text))
            val page = template.readText().replace("{{title}}", title).replace("{{content}}", html)
            File(dir, md.nameWithoutExtension + ".html").writeText(page)
        }
    }
}

/**
 * Website + web app in one folder, the layout the server expects in WEB_DIR:
 *   site/  static website (web/site + legal pages generated from doc/legal)
 *   app/   the browser app (:webApp production bundle)
 * The Dockerfile copies the same folder into the image.
 */
val stageWeb by tasks.registering(Sync::class) {
    group = "web"
    description = "Collects the website and the web app into build/web for the server."
    dependsOn(":webApp:wasmJsBrowserDistribution", generateLegalPages)
    into(layout.buildDirectory.dir("web"))
    from(rootProject.file("web/site")) {
        into("site")
        exclude("legal-template.html")
    }
    from(generateLegalPages) { into("site") }
    from(project(":webApp").layout.buildDirectory.dir("dist/wasmJs/productionExecutable")) { into("app") }
}

/** Local run on embedded PostgreSQL (data kept in ~/.vajrax-dev/pg): `./gradlew :server:runDev`. */
val runDev by tasks.registering(JavaExec::class) {
    group = "application"
    description = "Runs the server with an embedded PostgreSQL for local development."
    dependsOn("testClasses")
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.vajrax.server.DevServerKt")
    environment("WEB_DIR", layout.buildDirectory.dir("web").get().asFile.absolutePath)
}
