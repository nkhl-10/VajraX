# HabitFlow — Release Checklist

## Product
- [ ] Default templates verified.
- [ ] Template cloning verified.
- [ ] User customization verified.
- [ ] Daily tracking verified.
- [ ] Weekly analytics verified.
- [ ] Monthly analytics verified.
- [ ] Goal tracking verified.
- [ ] Recovery flow verified.

## UI
- [ ] Light theme.
- [ ] Dark theme.
- [ ] Empty states.
- [ ] Error states.
- [ ] Loading states.
- [ ] Accessibility.
- [ ] Small screen.
- [ ] Large screen.
- [ ] Font scaling.

## Android
- [ ] Notification permission.
- [ ] Notification scheduling.
- [ ] Widget installation.
- [ ] Widget completion.
- [ ] Widget synchronization.
- [ ] App restart persistence.
- [ ] Device reboot behavior.
- [ ] Timezone behavior.

## Data
- [ ] Database migration.
- [ ] Historical data preservation.
- [ ] Export.
- [ ] Delete data.
- [ ] Duplicate prevention.

## Security
- [ ] No secrets in source.
- [ ] No sensitive logs.
- [ ] Secure token storage if applicable.
- [ ] Widget privacy.
- [ ] Export privacy.

## Performance
- [ ] Cold start measured.
- [ ] Daily screen measured.
- [ ] Completion latency measured.
- [ ] Weekly analytics measured.
- [ ] Monthly analytics measured.
- [ ] Memory checked.
- [ ] Battery impact checked.

## Testing
- [ ] Unit tests.
- [ ] Database tests.
- [ ] Bloc/Cubit tests.
- [ ] Widget tests.
- [ ] Integration tests.
- [ ] Regression tests.

## Release Gate
Do not release with:
- Data loss.
- Incorrect completion calculations.
- Broken streak logic.
- Widget state corruption.
- Duplicate notifications.
- Critical crashes.
