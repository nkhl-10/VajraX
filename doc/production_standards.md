# VAJRAX: Production Standards & App Stability

To ensure VAJRAX is a production-grade, highly reliable Life OS (not just a prototype), we strictly adhere to the following 20 core Android/KMP production standards.

## 1. App Startup & Fluidity
*   **Cold Start & Baseline Profiles**: Optimize TTID (Time to Initial Display). We will implement **Macrobenchmarks** and **Baseline Profiles** to pre-compile critical paths and reduce Cold Start times.
*   **Jank & ANR Prevention**: Maintain a strict 16ms frame budget (60fps) or 8ms (120fps). No heavy operations on `Dispatchers.Main`. ANRs are unacceptable.
*   **Recomposition Optimization**: Compose Multiplatform UI must be optimized using `@Stable`, `@Immutable`, and `rememberSaveable` to avoid unnecessary layout subcomposition and drawing overhead.

## 2. Memory & Lifecycle Management
*   **Memory Leaks**: Strict context management. Use `LeakCanary` in debug builds. Be cautious with long-lived CoroutineScopes.
*   **Process Death & State Restoration**: Apps can be killed at any time by the Low Memory Killer (LMK). State must be preserved using `SavedStateHandle` (or KMP equivalent) and SQLDelight.
*   **Configuration Changes**: The app must survive rotations, dark mode toggles, and screen resizing without losing user input or crashing.

## 3. Concurrency & Data Flow
*   **Structured Concurrency**: Avoid global scopes. Bind Coroutine lifecycles to ViewModels. Handle cancellation gracefully.
*   **Race Conditions**: Protect shared mutable state using `Mutex` or `StateFlow`.
*   **Unidirectional Data Flow (UDF)**: Immutable UI state emitted from a Single Source of Truth (SSOT). UI sends Events, ViewModel emits State.

## 4. Offline-First & Database
*   **Offline-First**: VAJRAX is fully operational without an internet connection. Network is treated as an enhancement.
*   **Database Indexing**: SQLDelight tables must use proper indexes on frequently queried columns (e.g., `date` in `ActionRecordEntity`) to prevent UI stutter.
*   **Pagination**: For large datasets (e.g., past evidence logs), use pagination or cursor-based loading rather than loading thousands of rows into memory.

## 5. Network & Background Execution
*   **WorkManager & Background Sync**: All background data syncing (Supabase) and Analytics must use `WorkManager` (Android) or `BGTaskScheduler` (iOS) adhering to Doze Mode constraints.
*   **Retry + Exponential Backoff**: Network calls must gracefully retry on failure using an exponential backoff policy instead of aggressively pinging the server.

## 6. Build & Release
*   **R8 / ProGuard**: Release builds must be obfuscated and shrunk using R8.
*   **Crash & ANR Monitoring**: Production releases must have crash reporting (e.g., Crashlytics) to monitor fatal exceptions and out-of-memory errors.
