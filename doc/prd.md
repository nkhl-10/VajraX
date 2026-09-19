# Project VAJRAX — Product Requirements Document (PRD)

## 1. Core Positioning
> **"Choose the life you want to build. VAJRAX turns it into today's actions."**
> **Build. Discipline. Become.**

VAJRAX is not another habit tracker. Traditional habit trackers ask: *"Track your habits and see your progress."*
VAJRAX asks: *"Who do you want to become, and what is the exact system that gets you there today?"*

The universal product loop is:
```text
Direction → Principles → System → Today → Action → Evidence → Reflection → Adaptation
```

---

## 2. Competitor Insights vs. VAJRAX Differentiation

| Dimension | Competitor Habit Trackers | VAJRAX Life OS |
|---|---|---|
| **Core Promise** | "Don't break the chain" (Gamified streaks) | "Build an unbreakable system" (Consistency & Autonomy) |
| **Cognitive Load** | Checklist of 12+ equal items | Single-Core Focus: **NOW**, **NEXT**, **LATER** |
| **Completion Loop** | Tedious multi-step logging / XP fireworks | 2–3s seamless feedback (Subtle haptic + Progress increment) |
| **Evidence & Journaling** | Forced daily journaling or empty checkboxes | Optional tiered evidence (Normal / Important / Deep Reflection) |
| **Missing a Day** | Streak resets to 0 (Shame & guilt) | Consistency rate preserved (e.g. 82%; missing a day breaks nothing) |
| **Intelligence** | Opaque AI summaries / hallucinations | Transparent Pattern Discovery (Raw Data + Interpretation + "Why?") |
| **Growth Metric** | Arbitrary XP & Level numbers | Principle Strength (% in Physical Discipline, Deep Work, Self-Respect) |
| **Tracking Burden** | 100% manual logging | Automated metrics (Health Connect/UsageStats) → Semi-auto → Manual |
| **Aesthetic** | Bright neon gamification, busy UI | Obsidian dark / Lumina calm, 80% whitespace, 0 visual noise |

---

## 3. The 11 Core Product Rules

### Rule 1: Core Positioning
Always bridge high-level life direction to immediate daily action.
`Direction → Principles → System → Today → Action → Evidence → Reflection → Adaptation`

### Rule 2: The Single-Core Principle
Attention stays on one task: **"This is what matters right now."**
The UI separates daily life into three clear tiers:
- **NOW**: The active focus card. 1-tap start, 1-tap instant completion.
- **NEXT**: The immediate upcoming commitments.
- **LATER**: Later evening routines and background tasks.

### Rule 3: Rewarding Completion (2–3s Feedback)
Completing an action must feel crisp, immediate, and satisfying:
1. Instant visual state change (checkmark circle fills with smooth ease-out).
2. Subtle, tactile haptic pulse.
3. Progress indicator advances immediately.
4. Non-intrusive `+1 Evidence` badge appears with optional tap-to-attach.
5. Visual focus smoothly transitions to the NEXT priority.

### Rule 4: Optional Evidence System
Logging evidence must never feel like homework:
- **Tier 1 (Normal)**: `✓ Complete` (1-tap, done).
- **Tier 2 (Important)**: `✓ Complete` → Optional `+ Add evidence` (photo/screenshot/quick metric).
- **Tier 3 (Reflection-Worthy)**: `✓ Complete` → Note / voice memo / deep reflection rating.
Zero nagging. Zero forced popups.

### Rule 5: Consistency Over Streaks
Streaks encourage fragility, shame, and relapse upon a single missed day.
VAJRAX uses **Consistency Rate** (e.g., 82% over 30 days, 5/7 this week). Missing a single day never breaks the user's momentum or resets their progress.

### Rule 6: Transparent Pattern Discovery Layer
Never present opaque "AI magic". Every insight is transparent:
1. **Raw Data**: "18 sessions analyzed (14 Morning / 4 Afternoon)."
2. **VAJRAX Interpretation**: "Deep Work is 34% more likely to succeed before 11:00 AM."
3. **Interactive "Why?" Drill-down**: Tap to see timestamp distributions, fatigue correlations, and system recommendations.

### Rule 7: Principle Strength Breakdown
Instead of generic habit lists, aggregate progress into the user's underlying core principles:
- **Physical Discipline**: 91% (Strongest)
- **Deep Work**: 84%
- **Self-Respect**: 78%
- **Continuous Learning**: 72%
- **Daily Reflection**: 63% (Needs attention)
Answers the real question: *"Which part of your life system is becoming stronger?"*

### Rule 8: Automated Metrics Strategy
Progressive data collection hierarchy:
1. **Automatic**: Silent background resolution via Health Connect (sleep, steps, workouts) and UsageStats (screen time).
2. **Semi-automatic**: Contextual 1-tap quick confirmation widgets and rich notifications.
3. **Manual Fallback**: 1-tap in-app completion with optional evidence.

### Rule 9: Obsidian Dark & Calm Whitespace
- 80% calm, open whitespace (Obsidian deep dark / Lumina clean slate).
- 20% intentional intensity (Royal indigo / Obsidian gold accents).
- High-legibility typography with negative letter-spacing for premium feel.
- Zero clutter, zero unnecessary decorative elements.

### Rule 10: The 8-Level Product Hierarchy
1. **Level 1: Life Direction** (Who do I want to become?)
2. **Level 2: Principles** (What unshakeable rules guide me?)
3. **Level 3: System** (What recurring routines compose my days?)
4. **Level 4: Today** (What is the timeline of today's commitments?)
5. **Level 5: Now** (What is the single action in front of me right now?)
6. **Level 6: Evidence** (What real proof did I generate?)
7. **Level 7: Reflection** (What did I learn from today's execution?)
8. **Level 8: Adaptation** (How does the system self-tune for tomorrow?)

### Rule 11: "Don't Make Me Think"
When the user opens the app, they should never wonder what to do next. The **NOW** card is front-and-center, actionable in 1 tap, eliminating decision fatigue.

---

## 4. Curated Life Paths (The "Who")
*   **⚡ High Performance**: Deep focus blocks, physical vigor, relentless execution.
*   **🗿 Self-Mastery**: Self-respect, emotional discipline, firm boundaries, autonomy.
*   **🧠 Scholar**: Systematic reading, note synthesis, intellectual depth.
*   **💰 Wealth Builder**: High-leverage skills, career compounding, financial discipline.
*   **🌿 Balanced Life**: Holistic health, deep relationships, mindfulness, calm.
*   **🎨 Creator**: Daily creative output, flow state protection, craft mastery.
*   **🧘 Mindful Life**: Present awareness, emotional regulation, evening review.
*   **🕉 Purpose-Driven**: Core virtues, duty, service, and meaningful impact.
*   **🛠 Custom Life**: User-defined principles and routines.

---

## 5. Technical & Architecture Non-Negotiables
- **Compose Multiplatform**: Shared 100% Kotlin codebase across Android & iOS.
- **Offline-First Single Source of Truth**: SQLite via SQLDelight. UI responds in <16ms.
- **Supabase Cloud Sync**: Background bi-directional sync with Row Level Security (RLS).
- **Zero Gamification Gimmicks**: No fire emojis, cartoon animations, or streak-loss punishment.
