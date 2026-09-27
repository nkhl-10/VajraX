# HabitFlow — Performance Requirements

## Performance Goals

### App Launch
- Cold start should be responsive on supported mid-range Android devices.
- Avoid expensive database or analytics work during the first frame.

### Daily Screen
Target:
- Fast initial rendering.
- Only query data required for the selected day.
- Avoid loading full historical datasets.

### Habit Completion
Target:
- Persist completion quickly.
- Update UI optimistically only when rollback/error handling exists.
- Refresh widget without blocking the UI.

### Analytics
- Weekly/monthly calculations should be performed from efficient queries.
- Cache expensive derived results where appropriate.
- Move heavy calculations off the main UI thread/isolate when required.

### Database
- Index frequently queried fields:
  - user/tracker ID.
  - habit ID.
  - scheduled date.
  - status.
- Use transactions for multi-step writes.
- Paginate or limit long history queries.

### Widget
- Keep widget data small.
- Avoid expensive work during widget refresh.
- Use cached/derived daily data where possible.
- Do not run full monthly analytics for a simple completion action.

### Notifications
- Avoid creating duplicate scheduled notifications.
- Update only affected reminders after a habit change.

## Memory
- Do not keep complete history in memory.
- Use lazy lists for long habit/history screens.
- Dispose controllers/listeners correctly.
- Avoid unnecessary image assets.

## Battery
- Avoid continuous background polling.
- Prefer scheduled notifications and event-driven updates.
- Do not run periodic background work unless required.
- Widget refreshes should be minimal.

## Network
MVP core tracking should work offline.

Future sync:
- Queue local changes.
- Sync incrementally.
- Avoid uploading unchanged records.
- Resolve conflicts deterministically.

## Performance Metrics
Measure:
- Cold start time.
- Warm start time.
- First meaningful UI render.
- Daily screen load.
- Habit completion latency.
- Database query latency.
- Weekly report generation time.
- Monthly report generation time.
- Widget update latency.
- Memory usage.
- Battery impact.
- Notification scheduling latency.

## Performance Testing
Test on:
- Low-end supported Android device.
- Mid-range Android device.
- High-end Android device.

Test datasets:
- 10 habits.
- 50 habits.
- 100 habits.
- 1 year of history.
- 3 years of history.

## Performance Rule
Do not optimize based on assumptions. Profile first, identify the bottleneck, then optimize and measure again.
