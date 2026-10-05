# syntax=docker/dockerfile:1
#
# VAJRAX server image: the API, the website and the web app in one container (Cloud Run).
# Build from the repository root (the build needs contract/, shared/, webApp/, web/ and doc/legal/):
#   docker build -f server/Dockerfile -t vajrax-server .
#   docker build -f server/Dockerfile --target test .      # runs the server tests instead
# Run locally:
#   docker run -p 8080:8080 --env-file deploy/.env vajrax-server
# No Android SDK is needed: only the server, the contract and the browser build of the app are compiled.

ARG BUILD_IMAGE=eclipse-temurin:17-jdk-jammy
ARG RUNTIME_IMAGE=gcr.io/distroless/java17-debian12:nonroot

# ---- build: fat jar + website + web app ----
FROM ${BUILD_IMAGE} AS build
# The embedded PostgreSQL used by the tests refuses to run as root, and nothing here needs root.
RUN useradd --create-home --uid 10001 builder
USER builder
WORKDIR /home/builder/src
COPY --chown=builder:builder . .
# One JVM (Kotlin compiles in-process) keeps the build inside 8 GB.
RUN --mount=type=cache,target=/home/builder/.gradle,uid=10001,gid=10001 \
    bash ./gradlew --no-daemon --no-configuration-cache --console=plain \
      -Pkotlin.compiler.execution.strategy=in-process "-Dorg.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8" \
      :server:stageWeb :server:buildFatJar

# ---- test: server and contract tests on a real (embedded) PostgreSQL ----
FROM build AS test
RUN --mount=type=cache,target=/home/builder/.gradle,uid=10001,gid=10001 \
    bash ./gradlew --no-daemon --no-configuration-cache --console=plain \
      -Pkotlin.compiler.execution.strategy=in-process "-Dorg.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8" \
      :server:test :contract:jvmTest

# ---- runtime: no shell, no package manager, non-root ----
FROM ${RUNTIME_IMAGE}
ARG GIT_SHA=unknown
ENV PORT=8080 \
    WEB_DIR=/app/web \
    GIT_SHA=${GIT_SHA}
COPY --from=build /home/builder/src/server/build/libs/vajrax-server.jar /app/vajrax-server.jar
COPY --from=build /home/builder/src/server/build/web /app/web
USER nonroot
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/vajrax-server.jar"]
