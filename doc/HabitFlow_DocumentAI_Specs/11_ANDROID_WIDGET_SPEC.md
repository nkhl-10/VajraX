# HabitFlow — Android Home Screen Widget Specification

## Objective
Allow users to understand and update their routine without opening the app.

## Primary Questions
The widget must answer:
1. What have I completed today?
2. What should I do now?
3. What is next?

## Compact Widget
Display:
- Today's progress.
- Current/next habit.
- Time.
- Complete button.

Example:

TODAY 6/8
████████░░ 75%

NEXT
Exercise
08:00 AM

[✓ DONE]

## Medium Widget
Display:
- Daily progress.
- Current habit.
- Next 3 habits.
- Quick completion action.

## Large Widget
Display:
- Greeting/date.
- Progress.
- Current habit.
- Upcoming habits.
- Streak.
- Quick actions.

## Widget Actions
- Complete.
- Skip.
- Snooze.
- Open habit detail.
- Open daily plan.
- Open progress.

## Interaction Rule
Routine completion must not require opening the full application.

## State Synchronization
Widget action:
1. Receive action.
2. Validate habit occurrence.
3. Write HabitLog through shared domain/repository logic.
4. Recalculate today's progress.
5. Determine next eligible habit.
6. Refresh widget.
7. Provide immediate UI feedback.

## Stale Data
If widget data is stale:
- Refresh on supported lifecycle/event.
- Never show completion state that contradicts persisted data after refresh.

## Privacy
Support a privacy option to hide habit names from the widget.

## Performance
- No full analytics calculation for a simple completion.
- Avoid unnecessary widget refreshes.
- Keep rendered data small.

## Acceptance Criteria
- User can complete a habit from the widget.
- App reflects the completion immediately after synchronization.
- Widget reflects app changes.
- Next habit updates correctly.
- Daily progress updates correctly.
- Offline completion works.
