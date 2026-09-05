# VAJRAX: Technical Architecture

## 1. Platform & Stack
* **OS**: Android & iOS (Cross-platform)
* **Language**: Kotlin 2.4.10
* **Framework**: Kotlin Multiplatform (KMP) & Compose Multiplatform 1.12.0
* **Architecture Pattern**: Clean Architecture + MVVM to handle complex behavioral states predictably across platforms.
* **Dependency Injection**: Koin
* **Backend**: Supabase (Auth, Cloud Sync)

## 2. Monorepo Structure
VAJRAX is built as a single monorepo:
* **`commonMain`**: Shared Compose UI, Domain logic, ViewModels, Ktor networking, and SQLDelight databases.
* **`androidMain`**: Android-specific hardware/OS integrations (Health Connect, UsageStatsManager, FCM).
* **`iosMain`**: iOS-specific hardware/OS integrations (HealthKit, Screen Time API, APNs).

## 3. Data Layer
* **Local Database**: **SQLDelight** for robust multiplatform offline storage of `Habit`, `ActionRecord`, `LifePath`, and `Evidence` data.
* **Networking**: **Ktor** (3.5.2) for network requests.
* **Serialization**: `kotlinx-serialization`.

## 4. Passive Intelligence & Integrations
To support the "Zero Friction" and "Track what the phone knows" rules natively on each OS:
* **Android**: Health Connect API (Steps, Sleep), UsageStatsManager (Distraction OS), WorkManager.
* **iOS**: HealthKit (Steps, Sleep), Screen Time API (Distractions), BackgroundTasks.

## 5. UI/UX Implementation Details
* **Shared UI**: Built 100% in Compose Multiplatform.
* **Custom Modifiers**: Create custom Compose modifiers for the "Obsidian Glass" (blur + translucent background) and "Vajra Accent" (subtle metallic border rendering). 
* **Navigation**: JetBrains Multiplatform Navigation.

## 6. Production & Performance
* See **[production_standards.md](file:///c:/Users/User/AndroidStudioProjects/VajraX/doc/production_standards.md)** for strict guidelines on App Startup (Baseline Profiles), Memory Management, Coroutine stability, Offline-First rules, and ANR prevention. This ensures VAJRAX ships as a premium, crash-free application.

## 7. Global Internationalization (i18n) & Neutrality
* **Localization-Ready**: English first, multi-language translation ready.
* **Flexible Circadian System**: No hardcoded 6:00 AM routines; fully customizable wake/sleep windows supporting any timezone or shift schedule.
* **Universal Terminology**: Clear separation between **Paths**, **Principles**, and **Practices** across diverse global traditions (Stoic, Behavioral Science, Vedic, Mindful).
