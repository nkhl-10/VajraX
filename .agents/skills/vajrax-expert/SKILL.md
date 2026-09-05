---
name: vajrax-expert
description: >-
  Use this skill when the user asks to build, update, or maintain the VAJRAX Life OS Android application, or install new features. It provides the necessary architectural context, design rules, and behavioral philosophy required to write code for VAJRAX.
---

# VAJRAX Expert Skill

You are working on **VAJRAX**, a premium behavioral Life OS for Android. Before making any significant code changes, architecture decisions, or UI implementations, you must align with the project's core documentation.

## Required Reading (Context Initialization)
If you are starting a new task or need to understand the project architecture, read these files using `view_file` relative to the workspace root:

1. **[Project Memory & State](../../doc/memory.md)**: Current phase and core tenets.
2. **[Architecture](../../doc/architecture.md)**: The Android tech stack (Kotlin, Compose, Room, Health Connect, WorkManager).
3. **[Design System](../../doc/design.md)**: "VAJRAX Morphism" rules (Obsidian dark mode, 80% calm/20% intensity).
4. **[Product Rules](../../doc/rule.md)**: Progressive Autonomy, Zero Nagging, 3-Second Interaction, Proof over Points.
5. **[App Flow](../../doc/app_flow.md)**: The user journeys and the Intervention Engine (Sāma, Dāma, Daṇḍa, Bheda).

## Implementation Guidelines

### 1. UI/UX (Jetpack Compose)
- Always use the VAJRAX Morphism design language. Avoid heavy gamification (no confetti, no XP bars).
- Keep screens minimal ("Whitespace is a feature").
- For interactions, prioritize the "3-Second Rule" (quick actions, Rich Notifications).
- Never use aggressive red colors for failures. Missed habits are neutral.

### 2. Backend & Data (Room + WorkManager)
- Track passive behavior first (Health Connect, UsageStats) before asking the user.
- Store logic for the Intervention Engine locally. The app should not rely heavily on cloud APIs for behavioral decisions to ensure privacy and speed.

### 3. Progressive Autonomy
- Remember that VAJRAX's ultimate goal is to make itself unnecessary. Do not build features that make the user dependent on the app.

## Updating & Installing Skills / Features
When asked to update the app or install new features, follow the **Clean Architecture + MVI** pattern. 
1. Always check `doc/phases.md` to see which phase the feature belongs to.
2. Ensure new UI components are added gracefully without violating the "Zero Nagging" rule.
3. If introducing external libraries (e.g., Dagger Hilt, Retrofit, Room), ensure they are cleanly modularized.
