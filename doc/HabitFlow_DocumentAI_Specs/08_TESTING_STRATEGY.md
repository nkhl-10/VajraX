# HabitFlow — Testing Strategy

## Testing Pyramid

1. Unit tests.
2. Repository/database tests.
3. Bloc/Cubit tests.
4. Widget tests.
5. Integration tests.
6. Performance tests.

## Unit Tests

### Scheduling
Test:
- Daily habits.
- Weekday schedules.
- Weekly targets.
- Custom recurrence.
- Rest days.
- Timezone boundaries.
- Daylight-saving transitions where applicable.

### Analytics
Test:
- Completion percentage.
- Current streak.
- Longest streak.
- Consistency.
- Weekly aggregation.
- Monthly aggregation.
- Goal progress.
- Future occurrence exclusion.
- Skipped occurrence rules.

## Database Tests
Test:
- Insert.
- Update.
- Delete/archive.
- Duplicate prevention.
- Transactions.
- Migrations.
- Historical data preservation.

## Bloc/Cubit Tests
Test:
- Initial state.
- Loading.
- Success.
- Error.
- Completion action.
- Move.
- Snooze.
- Skip.
- Refresh.
- Empty state.

## Widget Tests
Test:
- Habit completion.
- Progress rendering.
- Empty states.
- Error states.
- Accessibility labels.
- Theme rendering.
- Responsive layouts.

## Integration Tests
Critical journeys:
1. First launch → template → tracker.
2. Tracker → daily completion.
3. Daily completion → weekly report.
4. Habit edit → historical data unchanged.
5. Widget completion → app state updated.
6. Notification → habit action.
7. Export → valid dataset.

## Regression Tests
Every bug fix must include a regression test when practical.

## Test Data
Use deterministic fixture data. Never rely on current date/time in business-logic tests without injecting a clock.

## Quality Gates
Before merge:
- Formatter passes.
- Static analysis passes.
- Unit tests pass.
- Integration tests for affected flows pass.
- No known critical regression.
- No debug logging of private data.

## Acceptance Criteria
A feature is not complete until its expected behavior and failure states are tested.
