# VAJRAX / HabitFlow — App Progress & Context

_Last updated: 2026-10-01 (quality pass: architecture, error handling, strings, accessibility, quality gates) · Branch `feature/habitflow-offline-mvp` (latest commit `4058a42`; the quality pass in section 4f is uncommitted)._

---

## 1. Status at a glance

| Area | Status |
|---|---|
| Offline-first app, complete flow (splash → onboarding → template → customize → daily use) | ✅ Implemented, runs on device |
| 6 designed screens (`doc/VajraXScreen/*.png`) | ✅ Implemented with real data |
| Local database (SQLDelight) + migration v1 → v2 | ✅ Implemented, migration verified on an upgraded install |
| Analytics (daily / weekly / monthly, streaks, consistency) | ✅ Implemented, unit-tested |
| Reminders (local notifications) | ✅ Implemented · ⏳ not yet verified on device |
| Home-screen widgets (Jetpack Glance): Today list + Now card | ✅ Implemented, builds · ⏳ not yet verified on device |
| Dark mode / landscape | ✅ Implemented · ⏳ not yet verified on device |
| Daily-loop & "less effort" polish (section 4a) | ✅ Implemented, builds · ⏳ not yet verified on device (device busy) |
| Automated tests | ✅ 64 tests, 0 failures (`:shared:testDebugUnitTest`) |
| Quality gates | ✅ detekt + ktlint formatting, Android lint (both with baselines), CI runs tests + detekt + lint (section 13) |
| UI text in resources | ✅ All visible text of the live screens, widgets, notifications and nav (section 4f) |
| Accessibility | ✅ 48 dp targets, dial TalkBack actions, contrast, system animation setting · ⏳ TalkBack / font-scale check on device |
| Debug APK | ✅ `:androidApp:assembleDebug` BUILD SUCCESSFUL |
| Release APK (R8) | ✅ `:androidApp:assembleRelease` BUILD SUCCESSFUL, unsigned · ⚠ R8 Kotlin-metadata warnings (AGP 8.5.2 predates Kotlin 2.4) |
| Global release readiness | ✅ In-app items done · ⏳ Play Console + signing + device QA open — see `doc/release/global-release.md` |
| Online / Supabase sync | ⏸ Out of scope for this phase (code untouched) |
| iOS | ⏸ Not built or verified (the iOS entry point `MainViewController` was already missing) |

---

## 2. Goal of this phase

Make the app **fully functional offline** before any online work:

- a complete user flow: splash → onboarding where the user picks a template → personal routine → daily check-ins → reports;
- the screens in `doc/VajraXScreen/` (Home, Calendar, Discover, Report, Profile, Create new template);
- the requirements in `doc/HabitFlow_DocumentAI_Specs/01–12` and the HabitFlow product brief (template → customize → daily check-in → accurate weekly/monthly analytics → review/adjust).

### Problems found in the previous version (and fixed)

| Problem | Cause | Fix |
|---|---|---|
| Onboarding never appeared | `DatabaseSeeder` inserted a fake "John Doe" user and an active life path, so the app always opened Home | Seeder now inserts reference data only; migration deletes the v1 demo rows |
| All numbers were fake | Hard-coded date `2026-08-30` in view models/repositories; mock values in UI states (e.g. "Week 140%", "78%", "12 days") | Every number is derived from persisted records by `HabitAnalytics` |
| "Use template" gave an empty Home | Habits were inserted but no daily occurrences were created; the 6 design templates had no habits | `RoutineManager` creates today's occurrences; design templates now ship with habits |
| Wrong ordering | Times stored as mixed strings ("06:30 AM", "09:00 - 10:00 AM") sorted as text | Times normalized to `HH:mm` |
| Dead UI | Calendar toggles only in memory, Month view, Discover search, Profile buttons did nothing; widget showed fixed text | All wired to real data and actions |
| Emoji / glyph icons, black status bar, nav bar covering content | — | Vector icon set, edge-to-edge, bottom clearance |

---

## 3. Key decisions

1. **Stack kept:** Kotlin Multiplatform + Compose Multiplatform 1.12, SQLDelight 2.0.2, Koin 4, kotlinx-datetime 0.8, MVI view models. (The brief's Flutter/Bloc/sqflite suggestion is marked "not required"; spec 06 says follow existing sound conventions.)
2. **Domain mapping (spec 10):** Habit = `PracticeEntity`, HabitLog/occurrence = `ActionRecordEntity`, Tracker = `UserTemplateEntity`, habit repository = `PracticeRepository`.
3. **Navigation follows the designs:** Home · Calendar · Discover · Report · Profile.
4. **No fake data anywhere.** Only the immutable template library and life paths are seeded.
5. **Community templates (offline):** two bundled read-only templates tagged "by @author".
6. **Past days are read-only**; a long-press in Calendar asks for confirmation before correcting a record (spec: "immutable except explicit correction"). Future days are disabled.
7. **No login offline:** a local profile (name, optional email).
8. **Weekly-target edits create a new habit version** (old version archived with its history) so past weeks keep their original scores.
9. **Accessibility over pixel-matching:** secondary text uses `#6B7280` (4.8:1) instead of the design's `#9CA3AF` (2.5:1).
10. Legacy VAJRAX features (Learn, Path, Grow/Review, AI companion, Sāma/Dāma/Daṇḍa/Bheda engines) remain in the code but are not reachable from navigation.

---

## 4. App flow

```
Splash (brand, prepares DB)
  ├─ no active routine ─▶ Onboarding
  │     Welcome → About you (name, up to 3 goals) → Your day (wake time, morning time, reminder style; skippable)
  │     → Choose your tracker (recommended for goals, categories, search, Blank tracker)
  │     → Template detail (preview) → Customize (toggle/edit/add habits, name, start today/tomorrow)
  │     → Start my routine ─▶ Home
  └─ active routine ─▶ Home

Home ⇄ Calendar ⇄ Discover ⇄ Report ⇄ Profile   (floating bottom nav)
Discover → Template detail → Customize → "Switch routine?" confirm → Home
Profile → My routine / Create template / Edit-duplicate-delete my templates / settings / export / delete data
Delete all data → back to Onboarding
```

### 4a. Daily loop & "less effort" polish

```
Open app → Home scrolls to NOW → one tap Done ("✓ Wake up · Next: Drink water, 6:35 AM", Undo)
        → focus moves to the next habit → end of day: "Day complete / Wrapping up" card → See progress (Report)
        → Saturday evening / Sunday: "Weekly review" card → Report opens the weekly reflection
```

| Item | Implementation |
|---|---|
| NOW always visible | Home layout: the dashboard card is **pinned at the top**; the task list scrolls in the fixed area below it, down to the device bottom (clipped under the card, soft edge when scrolled). The **active task is centred** in the visible list area on open and whenever focus moves to the next habit. On short / landscape screens the dashboard scrolls with the list so tasks keep enough room |
| Status bar | Opaque status-bar area on Home; Discover and Profile pad outside their scroll containers, so content never slides under the clock and icons (Calendar, Report, onboarding and top-bar screens already did) |
| One-tap quick actions | NOW card: **Mark done / +1 / Start / Log** (primary), **Snooze**, **Skip** (one tap, no reason needed, Undo), **Notes** (full action sheet) |
| Next habit feedback | Completion snackbar names the next habit and its time, with Undo that restores the exact previous state |
| End of day → Progress | Card appears when everything is done, or once the last habit has started (max 1 h, so overnight Sleep doesn't hide it); opens Report |
| Weekly Review | Card on Saturday ≥ 18:00 and Sunday while the week has check-ins and no reflection yet; opens Report (This Week) with the reflection sheet already open |
| Widget without opening app | **Jetpack Glance** widgets. *VAJRAX · Today* (resizable): every habit of today — tap a circle or name to mark done (tap again to undo), **+1** on count habits, **Later** (snooze 15 min) on the current habit; small size shows the current habit with **Done / Snooze**. *VAJRAX · Now*: compact current-habit card with **Done (+1) / Snooze** |
| Minimal reminders | Default reminders only on 3 habits; no reminder if already done; a reminder notification is removed automatically when the habit is completed or skipped in the app or widget (notifications are tagged per habit) |
| Auto-created occurrences | On launch, day change (while open), widget refresh, reminder and notification actions |
| Preserve state after restart | Focus timer start/pause state persisted (`active_focus_timer` setting) and restored after rotation, backgrounding or process death; nav bar hidden while the timer is open |
| Profile additions | **Home-screen widget** card with **Today list** / **Now card** buttons (system "add widget" dialog) and **Send a test reminder** (posts a reminder for the current habit immediately) |
| App-icon long-press | Static launcher shortcuts **Today widget** / **Now widget** (`res/xml/shortcuts.xml`) open the app and show the system "add widget" dialog |

### 4b. UI polish (latest)

| Item | Implementation |
|---|---|
| Portrait only | `android:screenOrientation="portrait"` on `MainActivity` |
| Arc filter tab | Discover: **Templates / Arc / Mine** tabs (category chips only on Templates). Onboarding template step: **Templates / Arc** |
| Glass bottom nav | Same material as TelepMaster's `GlassBar`: live blur of the screen behind (Android 12+, via a recorded `GraphicsLayer` + `BlurEffect`), translucent tint, sheen along the top edge, hairline border, 28 dp radius, selected tab on its own pill. Android 11 and older: same look with a more opaque tint |
| Less text | Helper paragraphs removed or cut to a few words across Home, onboarding, template detail/customize, Discover, Calendar, Report, Profile, habit editor, My routine and builder; snackbars shortened ("Skipped", "Snoozed 15 min", "Saved"). Destructive confirmations stay explicit but short |
| Smoother | Report and Calendar only recompute while visible (`whileVisible()` in `MviViewModel`), so a check-in no longer triggers 400-day analytics in the background; remaining screen states marked `@Immutable`; faster tab fades (180 / 120 ms) |

### 4c. Storyboard pass

Flows were drawn as UX storyboards (one frame per step: action → next destination), which surfaced these stall points:

| Stall | Fix |
|---|---|
| "Blank tracker" card pushed recommendations below the fold | Blank tracker moved to the end of the template list |
| Starting a routine late in the day opened day one with most habits "still open" | Customize preselects **Tomorrow** when fewer than half the habits are still ahead; chip reads "Today · N left" |
| A routine starting tomorrow showed "Rest day" | Home shows "Starts tomorrow" + "First up: habit · time" |
| Day-wrap / weekly-review cards scrolled away when the current habit was centred | Now a compact strip inside the pinned progress card (weekly review wins) |
| Drill-in screens faded like tabs | Template, Customize, Builder, Routine, Habit and legal pages push in from the right (260 ms) and pop back right; tabs keep the crossfade |
| "Complete daily routine" filler under the tracker name | Replaced with "N left" / "All done" |

### 4d. Global release items

Privacy policy, terms, health notice and open-source licenses in Profile › About (and Privacy/Terms links on Welcome); public copies in `doc/legal/`. `INTERNET` and `FOREGROUND_SERVICE` removed from the merged manifest; explicit backup rules; times follow the device 12/24-hour setting; directional icons mirror in RTL; content capped at 640 dp on tablets/foldables (API 36 ignores the portrait lock there); denied notifications offer a Settings shortcut; bundled templates no longer credit invented community authors ("Featured templates"). Full rule-by-rule status: `doc/release/global-release.md`.

### 4e. Current habit, Start today, widget

| Item | Implementation |
|---|---|
| One rule for "current habit" | `domain/habit/NowPicker`: in its time window → pending and starting within 30 min → first open habit after the one completed most recently → first open habit. Snoozed habits return only when their new time arrives. Used by Home, both widgets and the "Next: …" toast, so Done always moves to the same next habit everywhere |
| Start today | A routine set to start tomorrow shows **Start today** on Home; `RoutineManager.startToday()` moves the tracker and its habits to today and plans today's occurrences |
| Rotary dial Home | Default Home view (`ui/features/today/HabitDial.kt`): today's habits sit in holes on a half-circle dial like an old telephone's finger wheel, in time order. The habit under the finger stop (12 o'clock) is selected; its name, time and state show in the hub, with Snooze / Done (+1, Start, Log) / Skip or Undo below. Drag sideways to turn (haptic click per habit, spring snap, fling), tap a hole to turn to it, tap the selected hole to check in. When NOW changes (e.g. after Done) the dial turns to it. Header button switches dial ↔ list (`home_view` setting); short screens scroll |
| Moving card shadows | Every `VxCard` and the glass nav cast a soft top-lit shadow (`tiltShadow`, Compose `dropShadow`) whose offset follows the phone's movement: `TiltSensor` integrates gyroscope rotation into an angle that eases back to centre (±12°, ~2.5 s), exposed as `DeviceTilt` and read at draw time (no recomposition). Registered only while the activity is resumed; off without a gyroscope or when animations are disabled |
| Haptics | One vocabulary in `designsystem/Haptics.kt` (`VxHaptic`): Tap (buttons, rows, icon buttons, cards), Select (tabs, chips, segments, options, calendar days), ToggleOn/Off (check circles, switches, Undo), Confirm (Done on a plain habit), Reject (destructive confirms), Tick (dial steps, steppers, +1), DragStart (dial). Built into the shared components plus `hapticClickable` / `hapticSelectable`. Verified on the moto in `dumpsys vibrator_manager`: tab and button feedback play; ticks use SegmentTick because basic motors drop SegmentFrequentTick / TextHandleMove |
| Profile header | Hero card: gradient band with soft rings, avatar overlapping its edge, name + email ("On this device · offline"), edit button (also on the name), and Day x/N · Streak · This week stats from `HabitAnalytics` |
| Widgets | Current habit first, then what's still to do, then a "Done" group (widgets can't scroll to a row). Current card: "NOW / NEXT / STILL OPEN · time", Snooze + Done (icon, not a glyph), "Then · next habit" on taller Now widgets. Only the circle ticks a habit; tapping text opens the app. Launcher corner radius and `appWidgetBackground()` on Android 12+. Generated picker previews with sample habits on Android 15+ (published once per install/update). "Starts tomorrow" and "Day complete" states |

### 4f. Quality pass (2026-10-01)

Checklist audit (architecture, code quality, "human-designed" UX, edge cases, online readiness). Only gaps were changed; nothing already working was redone and no feature was removed.

| Area | What changed |
|---|---|
| Startup & loading errors | Startup failure stays on the splash with **Retry** (a returning user is never sent to onboarding). `MviViewModel.launchLoad` turns a failed data pipeline into `loadError` + Retry instead of an endless skeleton; `onUnhandledError` logs |
| Use cases | `domain/usecase/`: `PreferencesService`, `ProfileService` (+ `ProfileRules`), `GoalService`, `ReflectionService`, `TemplateLibraryService`, `CheckInService`, `OccurrenceMaterializer` (behind the unchanged `RoutineManager` API). Profile / Report / Builder / Onboarding / Routine view models no longer write repositories directly, so every write reaches `AppHooks` (widgets now, sync later) |
| One rule set for app + widget | `domain/today/TodayPlanner` (phases, counts, wrap-up, weekly-review window) used by Home and `WidgetData`; `domain/habit/CheckInAction` decides Done / +1 / Start / Log everywhere. Widget Done on a timed or measured habit opens the app on that habit instead of logging the full target |
| Forgiving UX | Discard-changes prompt on edited sheets (`VxBottomSheet`), inline email validation, Undo for dial check-in / snooze / move / +1 / value, confirm for goal delete and discarding a timer session, back on the timer pauses + asks, when the first cycle ends Home shows "N days done — keep it going or pick the next template" (opens Discover) and Profile shows "N-day cycle complete" while the day count keeps going, skipping "Your day" saves explicit defaults and the editor offers **Turn on** for reminders |
| Coroutines & lifetime | `runCatchingCancellable` (never swallows cancellation), idempotent `startup()`, DB open on IO, `collectAsStateWithLifecycle`, Builder / Routine / Habit-detail view models as Koin factories, onboarding progress survives process death |
| Config & secrets | `domain/BusinessRules` (snooze minutes, streak threshold, windows), `NotificationPrivacy` enum (stored values unchanged), Supabase URL/key from untracked `local.properties` → `BuildConfig`, hard-coded test login removed from `AuthRepositoryImpl` (sign-in now says it isn't available yet), `VxLog` for every former silent catch |
| Strings | Compose resources (`shared/src/commonMain/composeResources/values/strings.xml`, ~380 keys, `Res.string.*`) for all live screens and the nav bar; widgets, notifications and the pin-widget toast use Android `res/values/strings.xml` (with a plural for the snooze description). Copy pass: sentence case, one noun set (routine / template / habit) |
| Accessibility | 48 dp touch targets (chips, segments, swatches, day picker, calendar cells), dial TalkBack actions (Check in / Previous / Next) and auto list view at font scale ≥ 1.5, timer announces per minute, `textTertiary` ≥ 4.5:1, system "remove animations" honoured (Compose follows the animator scale; tilt shadows switch off), long names ellipsize |
| Design system | `VxShape.card` / `VxShape.control` radius tokens, shared prompt / add-box components, distinct Missed vs Upcoming calendar cells, clock icon on the Snooze notification action |
| Online-ready groundwork | `core/error/AppError` (Offline, Timeout, Server, SignedOut, Unknown) mapped to plain copy by `userMessage()`; `domain/sync/ConnectivityMonitor` (offline-only implementation for now). Remote data sources, auth, sync columns and the INTERNET permission wait for the API |
| Quality gates | detekt 1.23.8 + `detekt-formatting` (ktlint rules) with `config/detekt/detekt.yml` and per-module `detekt-baseline.xml`; Android lint with per-module `lint-baseline.xml`; `.editorconfig`; CI runs tests, detekt and lint and uploads the reports. Unused imports removed from the live files |

### Screens

| Screen | Design | What it does |
|---|---|---|
| Splash | — | V·X brand mark, "Build yourself."; waits for DB migration, template seeding and today's occurrences |
| Onboarding | spec 03 §1, 04 Screen 01 | 4 steps with step dots, system back support, notification permission request if reminders chosen |
| Template detail | spec 04 Screen 03 | Category, author, description, recommended-for, habits / minutes per day / cycle, habit timeline |
| Customize | spec 04 Screen 04 | Include/exclude habits, edit any habit, add own habit, tracker name, start today or tomorrow; times suggested around wake-up time |
| Home (Today) | `Home.png` | Date, greeting, completion ring, Today x/y · Week % · Consistency %, routine name, timeline sorted by time, **NOW card** (Mark done / +1 / Log / Start timer, Notes), one-tap circle with **Undo** snackbar, action sheet (done, value, minimum version, timer, snooze 15 min, move time, skip with reason, note, details), rest-day and empty states |
| Calendar | `Calender.png` | Week/Month toggle. Week: 5-day matrix centred on the selected day (arrows, swipe, Today), today editable, past read-only with long-press correction. Month: heatmap (High/Medium/Low/None), month completion, selected-day list |
| Discover | `Discover.png` | Search, category chips, Provided / Community / My templates, "Active" badge, create-template box, blank tracker |
| Report | `Report.png` | This Week / This Month; overall completion ring + delta chip + "N more tasks than last week"; bar chart (days or weeks); current & best streak; most consistent; needs attention; areas (category strength); pattern discovery with **Why?**; summary counts; **"How this is calculated"** sheet; weekly/monthly reflection; goals |
| Profile | `Profile.png` | Avatar/initials, Edit Profile, current template (Day X of N, % complete, View / Change), My templates (use/edit/duplicate/delete, drafts), Notifications (on/off, lock-screen privacy, widget name hiding), Appearance (System/Light/Dark), Language (English), Export JSON, Delete all data |
| Create / edit template | `Create new template.png` | Name (helper + counter), description (x/160), category chips, drag-to-reorder habit list (long-press handle; accessibility move up/down), "Add a habit", preview sheet, Save draft / Create template / Cancel with discard confirmation, validation |
| My routine | spec 04 Screen 04 | Rename, edit/archive/add habits, save routine as template, switch template, past routines |
| Habit detail | spec 04 Screen 06 | Current/longest streak, last-30-days rate, total completed, schedule/target/reminder, 5-week history grid, recent notes, edit/archive |

---

## 5. Architecture

```
UI (Compose screens) → MVI ViewModels → RoutineManager (use cases) / HabitAnalytics (pure) → Repositories → SQLDelight (SQLite)
Android widget ─────────────────────▶ RoutineManager / repositories (same DB, no second store)
Reminder receiver / notification actions ▶ RoutineManager / repositories
```

- **`RoutineManager`** (`domain/usecase/RoutineManager.kt`) — the only place that writes schedules and check-ins: activation, materialization, habit add/update/archive, complete, minimum, value/+1, timer, skip, snooze, move, note, undo/restore, past-day correction. Check-in writes are serialized with a mutex. Internally it delegates to `CheckInService` and `OccurrenceMaterializer`.
- **Other use cases** — `PreferencesService`, `ProfileService`, `GoalService`, `ReflectionService`, `TemplateLibraryService`: every non-routine write (settings, profile, goals, reflections, templates) goes through one of them, so `AppHooks` always fire.
- **`TodayPlanner`** (`domain/today`) — today's phases, counts and current habit for Home and the widgets.
- **`HabitAnalytics`** (`domain/analytics/HabitAnalytics.kt`) — pure functions for every rate, streak and breakdown.
- **Repositories** expose SQLDelight `Flow`s, so every screen and the widget update automatically after any write.
- **`AppHooks`** — platform side effects after changes (Android: reschedule reminders, refresh widget).
- **`AppClock`** — injected clock; business logic never reads the system time directly.
- **DI:** `di/DataModule.kt` (shared) + Android module in `VajraApplication`.

### Package map (shared/src/commonMain/kotlin/com/vajrax)

| Package | Contents |
|---|---|
| `core/time` | `AppClock`, `Dates`, `TimeFormat` |
| `core/log`, `core/error`, `core/coroutines` | `VxLog`, `AppError`, `runCatchingCancellable` |
| `domain/habit` | `Habit`, `HabitSchedule`, `HabitType`, `Occurrence`, `Tracker`, `ScheduleRules`, `OccurrencePlanner`, `HabitIconResolver` |
| `domain/analytics` | `HabitAnalytics`, `PeriodStats`, streaks, comparisons |
| `domain/usecase` | `RoutineManager`, `AppHooks`, `CheckInService`, `OccurrenceMaterializer`, `PreferencesService`, `ProfileService` / `ProfileRules`, `GoalService`, `ReflectionService`, `TemplateLibraryService` |
| `domain/today` | `TodayPlanner`, `DayPhase` |
| `domain/sync` | `ConnectivityMonitor` |
| `domain/template` | `TemplateCatalog` (design + library templates), `DefaultTemplates` (library), template↔habit mapping |
| `domain/repository` | repository interfaces |
| `data/local` | `.sq` schema, `1.sqm` migration, mappers, seeder |
| `data/repository` | repository implementations |
| `ui/designsystem` | `VxIcons`, components, `HabitEditorSheet`, brand mark, snackbar host |
| `ui/features/*` | splash, onboarding, templates, today, calendar, discover, report, profile, builder, routine |
| `ui/navigation` | type-safe routes, `MainNavigation` |
| `platform` | `PlatformActions` (export, permission, system bars), `WidgetController` |

---

## 6. Data model (schema v2)

| Table | Role | Notable columns |
|---|---|---|
| `TemplateEntity` / `TemplateHabitEntity` | Template library (system = immutable; custom = user) | `category`, `durationDays`, `isDraft`, `recommendedFor`; habit `habitType`, `targetValue`, `unit`, `scheduleType`, `weeklyTarget`, `intervalDays` |
| `UserTemplateEntity` | Tracker (personal copy) | `name`, `startDate`, `status` (ACTIVE/ARCHIVED), `endedAt` |
| `PracticeEntity` | Habit | `trackerId`, `category`, `icon`, `color`, `habitType`, `targetValue`, `unit`, `scheduleType`, `scheduleDays`, `weeklyTarget`, `intervalDays`, `reminderEnabled`, `reminderTime`, `startDate`, `archivedAt`, `sortOrder` |
| `ActionRecordEntity` | Occurrence / HabitLog, **unique (practiceId, date)** | `status` (PENDING, ONGOING, COMPLETE, MINIMUM, SKIPPED, MISSED, SNOOZED), `scheduledTime`, `value`, `note`, `skipReason`, `completedAt` |
| `SettingEntity` | Key/value preferences | theme, reminders, privacy, wake time, goals, last materialized date, library version |
| `GoalEntity` / `GoalHabitEntity` | Goals linked to habits | target type COMPLETIONS or RATE |
| `ReflectionEntity` | Weekly / monthly reflection (unique per period) | achievements, obstacles, next actions |
| `UserSessionEntity` | Local profile | display name, email |

**Migration `1.sqm` (v1 → v2):** adds all new columns (appended in the same order as the `.sq` CREATE statements), creates the new tables and indexes, removes v1 demo rows (`user_lumina_01`, `ut_01`, `prac_1..8`, `act_*`, `ev_*`, `idea_1`, `pattern_1`), deactivates life paths, stops scheduling trackerless v1 habits, de-duplicates occurrences before adding the unique index.

---

## 7. Business rules implemented

- **Scheduling** (`ScheduleRules`): daily; selected weekdays (other days are rest days); N times per week; every N days from the start date; never before the start date or on/after the archive date.
- **Materialization:** on launch, day change and before any schedule edit, PENDING occurrences are created from the last run up to today (max 400 days back). Past days are frozen with the schedule valid at the time, so edits never rewrite history.
- **Completion rate** = completed ÷ eligible scheduled occurrences. Future and today's still-open occurrences are **not** counted as missed; skipped occurrences are neutral; rest days never count; MINIMUM counts as done.
- **Weekly targets:** completions count on their day (capped at the target); a shortfall is counted once at the end of a finished week; partially covered weeks are not scored.
- **Streaks:** per habit = consecutive successful scheduled occurrences (skips neutral); day streak = days with ≥ 80 % done (rest days neutral; today counts only once it qualifies).
- **Comparisons:** this week/month so far vs the same number of days in the previous period.
- **Consistency (Home):** 30-day completion rate.
- **Pattern discovery:** completion by time of day over 30 days, shown with raw counts, only when there are enough samples and a gap of 15 points or more.
- **Goals:** progress from linked habits (completions count or completion rate).
- **Templates:** activation creates a new tracker and habit copies in one transaction; switching archives the old tracker from today and keeps all its history.
- **Wake-time personalization:** morning routines are shifted to the chosen wake-up time (max ±3 h, never across midnight).
- **Default reminders (anti-nagging):** when reminders are on, only the first habit and the two longest habits get a reminder by default.
- **Validation:** habit names 1–60 chars, template names 1–40, descriptions ≤ 160, positive targets, valid times/days/intervals.

---

## 8. Template library

- **41 system templates:** 4 provided from the Discover design (Morning Discipline, Deep Work Block, 30-Day Challenge, 6 AM Routine), 2 community (Evening Wind Down by @sarah, Fitness Starter Pack by @mike), plus 35 from the curated library (daily, morning, evening, fitness, health, mind, study, work, growth, detox, finance, challenges, 16 "Arc" templates).
- Descriptions, categories, recommended-for text and cycle length come from `TemplateCatalog`; habit icon/category/colour is derived from the habit name by `HabitIconResolver`.
- Stored copies are refreshed when `TemplateCatalog.VERSION` changes (currently `3`).
- Blank tracker available in onboarding and Discover.

---

## 9. Android platform

| Item | Details |
|---|---|
| `VajraApplication` | Starts Koin for every entry point; creates the notification channel; runs startup, reminder sync and widget refresh in the background |
| `MainActivity` | Edge-to-edge; status-bar icons follow the in-app theme; Storage Access Framework export ("save as" JSON); POST_NOTIFICATIONS request; handles rotation without recreating (`configChanges`) |
| Reminders | `HabitReminderScheduler` keeps exactly one alarm per habit (stable request codes, obsolete ones cancelled, snoozed/moved times honoured). `ReminderReceiver` posts the notification (privacy: full / generic / hidden) with **Done** and **Snooze 15 min** actions, and reschedules after boot, app update and time/time-zone changes |
| Widgets (Jetpack Glance 1.2.0) | `widget/GlanceWidgets.kt`: `TodayGlanceWidget` (responsive list; compact layout under 170 dp height) and `NowGlanceWidget`, receivers `TodayWidgetReceiver` / `NowWidgetReceiver`, `HabitWidgetAction` (toggle / done / +1 / snooze through `RoutineManager`). Data is a live `Flow` from the repositories (`widget/WidgetData.kt`), so the widget never stores its own copy; `GlanceWidgetController` refreshes both after any change in the app, a notification action or a widget tap. Light/dark colours, hidden-names privacy option |
| Launcher shortcuts | `res/xml/shortcuts.xml` → `MainActivity` actions `ADD_WIDGET_TODAY` / `ADD_WIDGET_NOW` → `requestPinAppWidget` (toast with manual steps if the launcher can't pin) |
| Manifest | `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`; WorkManager (used by Glance) adds `WAKE_LOCK`, `FOREGROUND_SERVICE`, `ACCESS_NETWORK_STATE` |
| Themes | `Theme.VajraX` (light/dark, Android 12+ splash background) matching the Compose splash |

---

## 10. Design system

- **Colours:** Figma indigo `#4F46E5`, background `#F9FAFB`, white cards; full dark palette; semantic tokens (success/warning containers, `accentOnContainer` for contrast on tinted surfaces); per-habit accent colours.
- **Type roles:** display / headline / title / body / label (no ad-hoc sizes in screens).
- **Spacing** 4–32 dp, **shapes** 10 / 16 / 24 dp.
- **Icons:** `VxIcons` — a single line-icon set (Lucide geometry) as vectors; no emoji.
- **Components:** cards, progress ring/bar, stat pods, check circle (48 dp target, spoken state), buttons, pill actions, segmented toggle, category chips, text fields with counters, time picker, confirm dialogs, empty/error/loading states, top bar, list rows, habit editor sheet.
- **Accessibility:** 48 dp touch targets, content descriptions and state descriptions, charts with text summaries, colour never the only signal.

---

## 11. Testing

Run: `bash ./gradlew :shared:testDebugUnitTest` (commonTest + androidUnitTest on the JVM, SQLite in memory).

| Suite | Tests | Covers |
|---|---|---|
| `ScheduleRulesTest` | 6 | daily, weekdays/rest days, interval, archive date, planner skips existing rows, legacy time formats |
| `HabitAnalyticsTest` | 7 | completion rate rules, no-data periods, habit streaks, weekly targets, day streak, week comparison, consistency ranking |
| `HabitFlowIntegrationTest` | 16 | template copy isolation, check-in + undo, schedule edit keeps history, no duplicate occurrences, archive, switching templates, delete-all, **v1 → v2 migration**, start today, undo after snooze, review regressions |
| `NowPickerTest` | 6 | current-habit rule (window, soon, after last completed, snoozed) |
| `TodayPlannerTest` | 5 | phases, counts without skipped / weekly-met, wrap-up, review window — the app/widget parity rules |
| `CheckInActionTest` | 3 | Done / +1 / Start / Log per habit type |
| `PreferencesServiceTest` | 6 | skipped "Your day" saves explicit defaults, reminders / privacy reschedule, widget names only redraw, onboarding progress, home view |
| `ProfileViewModelTest` | 4 | real DB: active routine + default name, invalid email rejected and not saved, saved profile updates the header, reminders switch |
| `CoreRulesTest` | 4 | email rule, stored privacy fallback, cancellation never swallowed, `AppError` messages |
| `TrackerCycleTest` | 1 | days in, end of the first cycle |
| `TimeFormatTest` | 2 | 12/24-hour display |
| `ActivationStateTest` | 3 | customize screen state |
| `SharedCommonTest` | 1 | placeholder (pre-existing) |
| **Total** | **64** | **0 failures** |

### Code review

An independent review of the full diff found **10 defects, all fixed** with regression tests where practical:
stale-notification snooze overwriting a completion; weekly-target edits re-scoring past weeks; snoozed/moved reminders being lost; phantom misses from partially covered weeks; wake-time shift crossing midnight; habit edits undoing today's move; lost "+1" taps under concurrency; undo erasing partial progress; alarms surviving "delete all data"; rotation losing builder/timer state.

---

## 12. Verified on device (Nokia 5.3, Android 12 / API 31)

- ✅ Installing over the old v1 build: migration ran, demo data gone, onboarding shown
- ✅ Onboarding steps, template detail, customize, habit editor, activation
- ✅ Home: real timeline in time order, one-tap check-in, ring/pods update, Undo snackbar
- ✅ Skip with reason (excluded from totals), action sheet
- ✅ Data persists after app restart / reinstall
- ✅ Calendar week + month, Discover (Active badge), Report, Profile
- ✅ Template builder layout and validation
- ⏳ **Pending (device busy — do not install until it is free):** the checklist below, plus the section 4a polish.

### Device validation checklist (run when the device is free)

| # | Check | Steps | Expected |
|---|---|---|---|
| 1 | Widget add | Profile → Home-screen widget → Add → Today list (and Now card) → confirm system dialog | Today list shows routine name, x/y, progress bar and every habit; Now card shows NOW/NEXT habit |
| 2 | Widget Done / undo / +1 | On the Today list tap a habit's circle, tap it again, tap +1 on a count habit (app closed) | Done → undone → count increases; Now card and app show the same state |
| 3 | Widget Snooze | Tap Later (Today list) or Snooze (Now card) | Current habit moves +15 min in app and widgets; reminder rescheduled |
| 4 | Test reminder | Profile → Notifications → Send a test reminder | Notification for the current habit, privacy text as selected |
| 5 | Reminder Done | Tap **Done** on the notification | Habit completed in app; notification gone |
| 6 | Reminder Snooze | Tap **Snooze 15 min** | Habit moved +15 min; alarm rescheduled (`adb shell dumpsys alarm \| grep vajrax`) |
| 7 | Stale notification cleanup | Post a reminder, then complete the habit in the app | Notification disappears |
| 8 | Real reminder | Enable a reminder 2 min ahead on a habit | Notification fires at that time |
| 9 | Dark mode | Profile → Appearance → Dark | All screens readable; status-bar icons light |
| 10 | Landscape | (App is now portrait-locked) | — |
| 11 | Export | Profile → Export my data → Export → pick a folder | JSON file saved (habits, logs, goals, reflections) |
| 12 | Delete all data | Profile → Delete all data → confirm | Returns to onboarding; library still available; old reminders cancelled |
| 13 | Daily loop | Complete NOW habit | Snackbar "Next: …", Home scrolls to the next NOW |
| 14 | Focus timer persistence | Start timer on a timer habit, kill the app, reopen | Timer still running with the correct elapsed time |
| 15 | Weekly review | On Saturday evening / Sunday with check-ins this week | "Weekly review" card → Report reflection sheet opens |
| 16 | Home layout | Scroll the task list; complete the NOW habit | Dashboard stays fixed; list scrolls under it only; new NOW habit centred |
| 17 | Status bar | Scroll Home, Discover, Profile | Nothing shows under the clock/icons; icons readable in light and dark |
| 18 | App shortcuts | Long-press the VAJRAX icon → Today widget / Now widget | App opens and shows the system add-widget dialog |
| 19 | Glass nav | Scroll any tab under the bottom bar (Android 12+ and older) | Content frosted behind the bar; labels crisp; selected pill visible in light and dark |
| 20 | Portrait lock | Rotate the device | App stays portrait |
| 21 | Arc tab | Discover → Arc; onboarding → Arc | Only Arc templates listed |

---

## 13. Build & run

| Item | Value |
|---|---|
| Location | `~/Git/VajraX` on the dev box |
| Build | `bash ./gradlew :androidApp:assembleDebug` (JDK 17; `gradlew` is not executable, so call it through `bash`) |
| APK | `androidApp/build/outputs/apk/debug/androidApp-debug.apk` |
| Install | `adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk` |
| Package / activity | `com.vajrax.android` / `.MainActivity` |
| Static analysis | `bash ./gradlew detekt` (rules + ktlint formatting; fails only on issues not in `*/detekt-baseline.xml`; refresh with `detektBaseline`) |
| Android lint | `bash ./gradlew :shared:lintDebug :androidApp:lintDebug` (baselines `*/lint-baseline.xml`; refresh with `updateLintBaseline`) |
| CI | `.github/workflows/android-ci.yml`: unit tests → detekt → lint → debug APK, reports uploaded as an artifact |

---

## 14. Known limitations

- Backup **import/restore** not implemented (export only).
- Goals can be created and removed, not edited.
- Notification permission is requested during onboarding and when reminders are switched on; if denied, reminders stay off and Profile offers a shortcut to the system notification settings.
- English only for now. Visible text is in string resources, so a language is a translation job; still in code: TalkBack-only descriptions inside `semantics {}`, messages raised by view models / use cases, legal texts, enum labels, and the legacy screens. Dates use English names and weeks start on Monday for everyone.
- Android lint (AGP 8.5.2) cannot read Kotlin 2.4 metadata, so its Kotlin checks are limited; upgrade AGP before shipping (same reason as the R8 warnings).
- Reminders use inexact alarms (`setAndAllowWhileIdle`) and may be delayed a few minutes by Doze.
- iOS target not built; the pre-existing iOS app lacks its `MainViewController`.
- Legacy screens (Learn, Path, Grow, Review) and engines still use their old placeholder data but are unreachable.
- Deprecation warnings for `rememberModalBottomSheetState` (Material3 alpha) remain; no functional impact.

---

## 15. Next steps

1. Run the device validation checklist (section 12) once the device is free.
2. Commit on a feature branch (e.g. `feature/habitflow-offline-mvp`) and open a PR.
3. Backup import/restore; goal editing.
4. Performance measurements from spec 09 (cold start, completion latency, report generation with 1–3 years of data).
5. Translate `strings.xml` (both files) and move the remaining TalkBack descriptions into resources.
6. **Online phase:** Supabase auth + sync — a queue of local changes, incremental upload, deterministic conflict rules, per-user data isolation.

---

## 16. Changed files

**New**
- Domain/core: `core/time/AppClock.kt`, `core/time/TimeFormat.kt`, `domain/habit/{HabitModels,ScheduleRules,HabitIconResolver}.kt`, `domain/analytics/HabitAnalytics.kt`, `domain/usecase/RoutineManager.kt`, `domain/template/{TemplateCatalog,TemplateHabitMapping}.kt`, `domain/repository/HabitFlowRepositories.kt`
- Data: `data/local/Mappers.kt`, `data/repository/HabitFlowRepositoriesImpl.kt`, `sqldelight/.../1.sqm`
- UI: `ui/designsystem/{VxIcons,Components,HabitEditorSheet,Brand,Snackbar}.kt`, `ui/features/today/FocusTimerOverlay.kt`, `ui/features/splash/SplashScreen.kt`, `ui/features/templates/{ActivationViewModel,TemplateScreens,TemplateCard}.kt`, `ui/features/today/HabitActionSheet.kt`, `ui/features/report/{ReportContract,ReportViewModel,ReportScreen}.kt`, `ui/features/builder/{TemplateBuilderViewModel,TemplateBuilderScreen}.kt`, `ui/features/routine/{RoutineViewModels,RoutineScreens}.kt`, `ui/utils/PlatformBackHandler.kt` (+ android/ios actuals), `platform/PlatformActions.kt`
- Android: `VajraApplication.kt`, `reminders/{HabitReminderScheduler,ReminderReceiver}.kt`, `widget/{GlanceWidgets,WidgetData}.kt` (Jetpack Glance), `res/xml/vajra_widget_list_info.xml`, `res/xml/shortcuts.xml` + shortcut icons, themes/colours (`values`, `values-night`, `values-v31`, `values-night-v31`), widget and notification drawables
- Tests: `commonTest/.../{TestFixtures,ScheduleRulesTest,HabitAnalyticsTest}.kt`, `androidUnitTest/.../HabitFlowIntegrationTest.kt`, `androidUnitTest/resources/schema_v1.sql`

**Rewritten / modified**
- `VajraDatabase.sq`, `DatabaseSeeder.kt`, `PracticeRepository(.Impl)`, `TemplateRepository(.Impl)`, `DefaultTemplates.kt` (extra optional fields), `DomainModels.kt` (`SNOOZED`), `MviViewModel.kt`, `DataModule.kt`, `App.kt`, `AppViewModel.kt`, `MainNavigation.kt`, `Theme.kt`, `Color.kt`, `FloatingNavBar.kt`, `WidgetController.kt`, `AndroidWidgetController.kt`
- Features: onboarding, today, calendar, discover, profile (contracts, view models, screens)
- Android: `MainActivity.kt`, `AndroidManifest.xml`, `res/xml/vajra_widget_info.xml`, `strings.xml`
- Build: `shared/build.gradle.kts`, `androidApp/build.gradle.kts`, `gradle/libs.versions.toml` (activity-compose in shared, `androidx.glance:glance-appwidget:1.2.0` in androidApp, coroutines-test, sqldelight sqlite-driver for tests)

**Removed**
- `ui/components/NavTabIcon.kt` (replaced by `VxIcons`), `ui/features/discover/DiscoverModels.kt`
- RemoteViews widget (`widget/VajraTodayWidgetProvider.kt`, `res/layout/vajra_widget_today.xml`) and `shared/.../widget/AndroidWidgetController.kt` — replaced by the Glance widgets

**Quality pass (2026-10-01):** see section 4f; new files `core/{log/VxLog,error/AppError,coroutines/RunCatching}.kt`, `domain/{BusinessRules,habit/CheckInAction,habit/NotificationPrivacy,today/TodayPlanner,sync/ConnectivityMonitor}.kt`, `domain/usecase/{CheckInService,OccurrenceMaterializer,PreferencesService,ProfileService,ProfileRules,GoalService,ReflectionService,TemplateLibraryService}.kt`, `ui/designsystem/{Sheets,ReminderAccess,Haptics,TiltShadow}.kt`, `composeResources/values/strings.xml`, `config/detekt/detekt.yml`, `.editorconfig`, `*/detekt-baseline.xml`, `*/lint-baseline.xml`, tests `PreferencesServiceTest`, `CoreRulesTest`, `ProfileViewModelTest`, `CheckInActionTest`, `TodayPlannerTest`, `TrackerCycleTest`, `ActivationStateTest`.

**Not touched:** the pre-existing uncommitted deletions under `doc/` (`HabitFlow_DocumentAI_Specs.zip`, `stitch_vajrax_life_os_interface/*`) belong to the earlier working tree.
