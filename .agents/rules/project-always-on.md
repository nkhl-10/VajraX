---
trigger: always_on
---
# VAJRAX Project Information

## Overview
VAJRAX is a premium behavioral Life OS for Android & iOS. It is not a gamified habit tracker; it is a self-directed growth system designed around the principles of Progressive Autonomy, Zero Nagging, and 3-Second Execution.

## Tech Stack (KMP Monorepo)
- **Language**: Kotlin 2.4.10
- **Framework**: Kotlin Multiplatform (KMP)
- **UI Framework**: Compose Multiplatform 1.12.0 (using "VAJRAX Morphism" - Obsidian Dark Mode, Glassmorphism).
- **Architecture**: Clean Architecture with MVVM. Shared logic in `commonMain`.
- **Database**: SQLDelight (Multiplatform offline storage).
- **Networking**: Ktor 3.5.2 & Supabase (Backend).
- **Dependency Injection**: Koin.
- **Platform Specifics**: Health Connect/UsageStats (Android) and HealthKit/Screen Time (iOS).

## Core Rules
- **Whitespace is a feature**: Keep UI minimal. "Don't make me think."
- **No gamification**: Use evidence (photos/notes) instead of XP points.
- **Zero Nagging**: Silent by default. Never punish the user visually.
