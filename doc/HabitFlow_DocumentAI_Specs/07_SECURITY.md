# HabitFlow — Security Requirements

## Security Objectives
Protect user routine data, personal notes, goals, and account credentials.

## Data Classification
Potentially sensitive user data:
- Habit history.
- Personal goals.
- Notes/reflections.
- Routine schedules.
- Export files.
- Account identifiers if cloud sync is introduced.

## Local Storage
- Store only required data.
- Do not store authentication secrets in plain SQLite.
- Use secure platform storage for tokens/credentials.
- Restrict exported files to user-selected destinations.

## Authentication
If cloud accounts are introduced:
- Use established authentication mechanisms.
- Store refresh/access tokens securely.
- Expire/revoke tokens appropriately.
- Never log credentials or tokens.

## Input Validation
Validate:
- Habit names.
- Notes.
- Target values.
- Durations.
- Schedule parameters.
- Imported data.

Prevent invalid dates, negative durations, malformed recurrence rules, and oversized input.

## Privacy
- Do not send habit data to external services without explicit product requirements and user consent.
- AI analysis should use the minimum data required.
- Do not include private habit content in diagnostic logs.
- Provide data export and deletion.

## Notifications
Notifications may reveal private habit information on a lock screen. Provide a privacy setting such as:
- Full notification.
- Generic notification.
- No habit details.

## Widget Privacy
The widget may be visible on the home screen. Allow users to disable sensitive habit names from widgets.

## Logging
Never log:
- Passwords.
- Access tokens.
- Private notes.
- Full personal datasets.

## Export Security
Warn users that exported files contain personal tracking data. Avoid automatically uploading exports.

## Cloud Security — Future
- TLS for all network communication.
- Server-side authorization checks.
- Per-user data isolation.
- Rate limiting.
- Audit important account actions.
- Secure secret management.
