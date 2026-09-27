# HabitFlow — AI Coding Rules

## 1. General
- Inspect the existing project before changing architecture.
- Follow the project's established conventions when they are sound.
- Do not invent APIs or dependencies.
- Do not claim implementation without verifying code.
- Do not fabricate test results.
- Keep changes focused and reviewable.

## 2. Architecture
Use feature-based MVVM.

Suggested structure:

lib/
  core/
  data/
  domain/
  features/
    onboarding/
    templates/
    habits/
    daily/
    progress/
    goals/
    settings/
    widget/
  shared/

Separate:
- UI.
- State.
- Domain models.
- Repositories.
- Data sources.

## 3. State Management
Use Bloc/Cubit.

Rules:
- UI does not directly mutate database state.
- Events represent user/system intent.
- States represent renderable UI state.
- Repositories own persistence operations.
- Avoid global mutable state.
- Do not duplicate the same source of truth in multiple blocs.

## 4. Database
Use sqflite with migrations.

Rules:
- Use stable primary keys.
- Add indexes for frequent queries.
- Use transactions for multi-record operations.
- Never silently delete historical logs.
- Use migrations for schema changes.
- Keep database access out of widgets.

## 5. Date and Schedule Rules
- Store timestamps consistently.
- Treat a user's timezone explicitly.
- Separate habit definition from scheduled occurrence.
- Do not mark future occurrences as missed.
- Rest days are not failures.
- Schedule changes must preserve historical logs.

## 6. Analytics
Analytics must derive from persisted records.

Required formulas must be centralized in testable domain services.

Do not calculate important business metrics directly inside widgets.

## 7. Widget
- Widget actions must use the same repository/domain logic as the app.
- Never maintain a second independent habit database for the widget.
- Refresh only the required widget instances.
- Handle stale widget data gracefully.

## 8. Notifications
- Respect notification permission state.
- Cancel obsolete reminders when schedules change.
- Avoid duplicate notifications.
- Persist reminder configuration.

## 9. Error Handling
- Use typed/domain errors where practical.
- Provide user-friendly messages at the UI boundary.
- Log diagnostic information without exposing sensitive data.

## 10. Performance
- Avoid unnecessary rebuilds.
- Use lazy lists.
- Keep database queries bounded and indexed.
- Move expensive analytics off the UI thread/isolate when needed.
- Avoid loading all historical logs for a single-day screen.

## 11. Security
- Do not store secrets in source code.
- Do not log private habit notes.
- Validate all user-controlled input.
- Use secure storage for credentials if authentication is added.
- Keep export/delete operations explicit.

## 12. Code Quality
- Prefer small focused classes.
- Avoid giant widgets.
- Avoid duplicated business logic.
- Use immutable state where practical.
- Add tests for business-critical logic.
- Document non-obvious scheduling and analytics rules.

## 13. AI Agent Workflow
Before coding:
1. Inspect repository.
2. Identify relevant files.
3. Explain intended changes.
4. Implement smallest correct change.
5. Run formatter.
6. Run static analysis.
7. Run tests.
8. Review changed files.
9. Report actual results and remaining issues.

Never rewrite unrelated code merely to satisfy style preferences.
