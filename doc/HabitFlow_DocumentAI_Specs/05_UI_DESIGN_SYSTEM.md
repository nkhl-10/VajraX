# HabitFlow — UI Design System

## Design Direction
Clean, calm, productivity-oriented, modern Material 3 interface.

The UI should feel supportive rather than punitive.

## Design Principles
- One primary action per context.
- High information hierarchy.
- Minimal daily interaction.
- Consistent spacing.
- Accessible touch targets.
- Avoid excessive decoration.
- Use color to communicate state, not to create pressure.

## Color Roles
Define semantic tokens rather than hardcoded colors:
- Primary.
- OnPrimary.
- Secondary.
- Surface.
- SurfaceVariant.
- Background.
- OnBackground.
- OnSurface.
- Success.
- Warning.
- Error.
- Disabled.

Support light and dark themes.

## Typography
Use Material 3 typography roles:
- Display.
- Headline.
- Title.
- Body.
- Label.

Do not hardcode font sizes throughout screens.

## Spacing
Use a consistent spacing scale:
4, 8, 12, 16, 20, 24, 32.

## Shape
Use a small set of semantic corner radii:
- Small controls.
- Medium cards.
- Large sheets/dialogs.

## Components
Reusable components:
- HabitCard.
- HabitTimelineItem.
- CompletionButton.
- ProgressRing.
- ProgressBar.
- StreakBadge.
- TemplateCard.
- CategoryChip.
- ScheduleEditor.
- ReminderEditor.
- CalendarDay.
- AnalyticsCard.
- GoalCard.
- ReflectionCard.
- EmptyState.
- ErrorState.
- LoadingSkeleton.

## Habit States
- Pending.
- Completed.
- Skipped.
- Snoozed.
- Overdue.
- Rest day.
- Disabled.

Each state must have accessible visual and textual representation.

## Interaction
- One-tap completion.
- Immediate visual feedback.
- Undo where accidental completion is possible.
- Avoid confirmation dialogs for routine completion.
- Confirm destructive actions.

## Accessibility
- Minimum accessible touch target.
- Meaningful semantic labels.
- Do not rely only on color.
- Support system font scaling.
- Support dark mode.
- Ensure charts have textual summaries.

## Widget Design
Widget hierarchy:
1. Today's progress.
2. Current/next habit.
3. Primary quick action.
4. Upcoming tasks.

The widget must remain readable at compact sizes.
