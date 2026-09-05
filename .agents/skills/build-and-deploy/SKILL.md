---
name: build-and-deploy
description: >-
  Use this skill to build the Android APK, compile the app, or deploy it to a connected device via ADB.
---
# Build & Deploy Workflow

## Steps
1. **Clean Project**: Run `./gradlew clean` in the terminal to clear old builds.
2. **Build Debug APK**: Run `./gradlew assembleDebug`
3. **Install on Device**: Run `adb install -r app/build/outputs/apk/debug/app-debug.apk` (if an emulator or device is connected).
4. **Launch App**: Use ADB to launch the main activity.
