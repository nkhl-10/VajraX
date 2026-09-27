# HabitFlow — System Architecture

## Architecture

UI → ViewModel/Bloc → Use Cases/Domain → Repository → Data Source → SQLite

Android Widget → Shared Domain/Repository → SQLite

Notifications → Scheduling Service → Android Notification System

## Layers

### Presentation
Screens, widgets, UI state, navigation.

### Domain
Habit rules, scheduling, completion calculations, streaks, consistency, goal progress.

### Data
SQLite database, repositories, DTOs, migrations.

### Platform
Android notifications, home-screen widget, secure storage, export/share.

## Source of Truth
SQLite is the source of truth for MVP tracking data.

The widget must not maintain an independent copy of habit state.

## Domain Mapping & Integration

To preserve the existing `VajraX` behavioral engine (Phases 0-19) and avoid redundancy, the HabitFlow concepts have been explicitly mapped to the existing `VajraX` domain architecture. This ensures the widget and future features reuse the same SQLite source of truth and domain rules.

| HabitFlow concept       | VajraX existing concept |
| ----------------------- | ----------------------- |
| Habit                   | Practice                |
| Habit occurrence/action | ActionRecord            |
| Routine/life structure  | LifePath                |
| Habit status            | ActionStatus            |
| Habit repository        | PracticeRepository      |

## Core Entities

### Template
- id
- name
- description
- category
- version
- isDefault

### Tracker
- id
- templateId
- name
- userId
- startDate
- status

### Habit
- id
- trackerId
- name
- category
- type
- target
- duration
- schedule
- reminder
- status
- sortOrder

### HabitLog
- id
- habitId
- scheduledDate
- status
- value
- note
- completedAt

### Goal
- id
- title
- description
- target
- startDate
- endDate
- status

### GoalHabit
- goalId
- habitId
- contributionWeight

### Reflection
- id
- date
- type
- achievements
- obstacles
- nextActions

## Core Business Rules
- Templates are immutable defaults.
- User trackers are independent copies.
- Historical logs are immutable except for explicit correction.
- Future occurrences are not missed.
- Rest days are not failures.
- Streaks follow schedule rules.
- Analytics use persisted logs and schedules.
