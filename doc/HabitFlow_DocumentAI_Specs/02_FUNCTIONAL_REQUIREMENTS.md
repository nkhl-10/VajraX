# HabitFlow — Functional Requirements

## FR-001 Template Library
The system shall display default templates grouped by category.

## FR-002 Template Preview
The user shall be able to preview habits, times, durations, categories, and expected daily effort before activation.

## FR-003 Template Activation
Selecting a template shall create a user-owned tracker copy.

## FR-004 Template Isolation
Editing a user tracker shall never modify the source default template.

## FR-005 Habit Creation
The user shall create a habit with name, time, duration, category, frequency, reminder, and tracking type.

## FR-006 Habit Editing
The user shall modify active habit configuration.

## FR-007 Habit Archiving
The user shall archive a habit without deleting historical records.

## FR-008 Daily Check-in
The user shall mark a scheduled habit as completed directly from the daily screen.

## FR-009 Quick Actions
The user shall be able to complete, skip, snooze, or move an eligible habit.

## FR-010 Habit Values
The system shall support boolean, count, duration, and measurable-value tracking.

## FR-011 Scheduling
The system shall support daily, selected weekday, weekly target, and custom recurring schedules.

## FR-012 Rest Days
Scheduled rest days shall not be counted as missed occurrences.

## FR-013 Future Occurrences
Future scheduled occurrences shall not be counted as missed.

## FR-014 Historical Integrity
Changing a current schedule shall not rewrite historical completion records.

## FR-015 Daily Progress
The system shall calculate completion as completed scheduled occurrences divided by eligible scheduled occurrences.

## FR-016 Weekly Analytics
The system shall show weekly completion, trends, streaks, and habit-level performance.

## FR-017 Monthly Analytics
The system shall show monthly completion, trends, calendar history, and comparisons.

## FR-018 Streaks
The system shall calculate current and longest streaks according to the habit's schedule.

## FR-019 Consistency
The system shall provide consistency metrics that are not dependent only on streak length.

## FR-020 Goals
The user shall create personal goals and associate habits with goals.

## FR-021 Reflection
The user shall record weekly reflections, achievements, obstacles, and planned changes.

## FR-022 Reminders
The system shall schedule local notifications according to user-selected times and repeat rules.

## FR-023 Smart Reminder Rules
The system may suggest a reminder-time change when sufficient historical evidence exists.

## FR-024 Android Widget
The widget shall display daily progress, current/next habit, and quick completion actions.

## FR-025 Widget Synchronization
A widget action shall update persistent application state and refresh displayed information.

## FR-026 Offline Operation
Core tracking shall work without an internet connection.

## FR-027 Export
The user shall be able to export personal tracking data.

## FR-028 Data Deletion
The user shall be able to delete personal tracking data with explicit confirmation.

## FR-029 Empty States
Every list, dashboard, analytics, and template state shall have a meaningful empty state.

## FR-030 Error Handling
Failures shall provide actionable messages and shall not silently lose user data.
