# VAJRAX: Technical Specifications (KMP 2026 Stack)

This document defines the exact dependency stack and monorepo structure for building VAJRAX as a cross-platform application.

## 1. Core Tech Stack (Kotlin Multiplatform)
| Layer         | Technology                       | Version / Notes |
| ------------- | -------------------------------- | --------------- |
| **Language**  | Kotlin                           | 2.4.10          |
| **KMP**       | Kotlin Multiplatform             | 2.4.10          |
| **UI**        | Compose Multiplatform            | 1.12.0          |
| **Android UI**| Compose                          | 1.12.0          |
| **iOS UI**    | Compose Multiplatform            | 1.12.0          |
| **Async**     | kotlinx-coroutines               | 1.11.0          |
| **Parsing**   | kotlinx-serialization            | latest stable   |
| **Date/Time** | kotlinx-datetime                 | 0.8.0           |
| **Networking**| Ktor                             | 3.5.2           |
| **DI**        | Koin                             | latest stable   |
| **Database**  | SQLDelight                       | latest stable   |
| **Backend**   | Supabase                         | latest stable   |
| **Push**      | FCM (Android) / APNs (iOS)       | Native specific |
| **Build**     | Gradle Version Catalog (`.toml`) | Latest          |

## 2. Additional KMP Libraries
*   **Material UI**: Compose Material 3 (`1.12.0-alpha03`)
*   **Navigation**: JetBrains Multiplatform Navigation (`2.10.0-alpha02`)
*   **Image loading**: Coil (KMP-compatible stable)
*   **Local Key-Value**: Multiplatform Settings (latest stable)
*   **Logging**: Kermit (latest stable)

## 3. Monorepo Project Structure
VAJRAX uses a unified monorepo structure. The codebase prioritizes sharing logic and UI in `commonMain`, while keeping strictly native capabilities isolated.

```text
vajrax/
├── shared/ (commonMain)
│   ├── Compose Multiplatform (UI, Material 3, Navigation)
│   ├── Architecture (MVVM, Clean Arch use cases)
│   ├── Data Layer (SQLDelight, Supabase API, Ktor)
│   └── Core utilities (Coroutines, DateTime, Serialization, Koin DI)
│
├── androidApp/ (androidMain)
│   ├── Push Notifications (FCM)
│   ├── Android Permissions & Foreground Services
│   ├── Health Connect API (Passive tracking)
│   ├── UsageStatsManager (Distraction OS)
│   └── Android-specific APIs (Widgets, Glance)
│
└── iosApp/ (iosMain)
    ├── Push Notifications (APNs)
    ├── iOS Permissions & Keychain
    ├── HealthKit (Passive tracking for iOS)
    ├── Screen Time API (Distraction OS for iOS)
    └── iOS-specific APIs (Live Activities, Widgets)
```

## 4. KMP Implementation Rules
- **Avoid AndroidX assumptions**: Do not automatically use `androidx.*` dependencies. Prefer JetBrains Compose Multiplatform packages for UI, Foundation, and Material.
- **Dependency Minimization**: Keep the stack tight. Use Ktor + SQLDelight + Koin + Supabase. Avoid adding one-off libraries for trivial features.
