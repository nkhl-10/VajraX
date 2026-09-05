---
trigger: model_decision
---
# Design Principles & Programming Patterns

## SOLID Principles
- **Single Responsibility**: Each class has one reason to change.
- **Open/Closed**: Open for extension, closed for modification.
- **Liskov Substitution**: Subclasses must be replaceable for their superclasses.
- **Interface Segregation**: Don't force clients to depend on unused interfaces.
- **Dependency Inversion**: Depend on abstractions, not concretions.

## Kotlin Multiplatform (KMP) Architecture
- **MVVM / State Management**: Use immutable data classes for State. Hoist state to ViewModels in `commonMain`. 
- **Clean Architecture**: Separate Domain (use cases), Data (SQLDelight/Ktor), and Presentation (Compose Multiplatform).
- **Coroutines & Flows**: Use `kotlinx-coroutines` 1.11.0. Use `StateFlow` for state emissions across all platforms.
- **Monorepo Strategy**: Do not use `androidx.*` dependencies blindly. Always favor Compose Multiplatform equivalents. Keep OS-specific hardware and permission code strictly in `androidMain` and `iosMain`.

## Production & Performance Constraints
- **Compose Stability**: Use `@Stable` and `@Immutable` for UI states. Prevent unnecessary recomposition.
- **Coroutines**: Enforce Structured Concurrency. Never block `Dispatchers.Main`. Use `Dispatchers.IO` for DB/Network.
- **Offline-First**: Treat network as enhancement. Always read from/write to local SQLDelight first.
- **Lifecycle**: Handle Process Death and Configuration Changes seamlessly.
- **Build**: Ensure compatibility with R8 obfuscation and Baseline Profiles.
