---
trigger: model_decision
---
# UI/UX Laws & Principles — Practical Rulebook

A practical reference of major UX laws, Gestalt principles, usability heuristics, and interaction rules for designing modern web and mobile applications.

> **Core principle:** Good UX makes the right action obvious, keeps the interface predictable, gives immediate feedback, prevents mistakes, and preserves the user's context.

---

## Table of Contents

1. [Jakob's Law](#1-jakobs-law)
2. [Fitts's Law](#2-fittss-law)
3. [Hick's Law](#3-hicks-law)
4. [Miller's Law](#4-millers-law)
5. [Doherty Threshold](#5-doherty-threshold)
6. [Peak-End Rule](#6-peak-end-rule)
7. [Serial Position Effect](#7-serial-position-effect)
8. [Von Restorff Effect](#8-von-restorff-effect)
9. [Zeigarnik Effect](#9-zeigarnik-effect)
10. [Aesthetic-Usability Effect](#10-aesthetic-usability-effect)
11. [Tesler's Law](#11-teslers-law)
12. [Parkinson's Law](#12-parkinsons-law)
13. [Postel's Law](#13-postels-law)
14. [Occam's Razor](#14-occams-razor)
15. [Poka-Yoke](#15-poka-yoke)
16. [Goal-Gradient Effect](#16-goal-gradient-effect)
17. [Gestalt: Law of Prägnanz](#17-law-of-pragnanz)
18. [Gestalt: Proximity](#18-law-of-proximity)
19. [Gestalt: Similarity](#19-law-of-similarity)
20. [Gestalt: Common Region](#20-law-of-common-region)
21. [Gestalt: Common Fate](#21-law-of-common-fate)
22. [Gestalt: Continuity](#22-law-of-continuity)
23. [Gestalt: Closure](#23-law-of-closure)
24. [Gestalt: Uniform Connectedness](#24-law-of-uniform-connectedness)
25. [Nielsen's 10 Usability Heuristics](#25-nielsens-10-usability-heuristics)
26. [Practical Mobile/Web Interaction Rules](#26-practical-mobileweb-interaction-rules)
27. [UX Design Checklist](#27-ux-design-checklist)

---

# 1. Jakob's Law

> **Users spend most of their time using products other than yours.**

### Rule

Users bring expectations from products they already know. Familiar patterns reduce learning time.

### Apply it

- Use familiar navigation patterns.
- Use standard icons and terminology.
- Keep common interactions predictable.
- Follow platform conventions.
- Avoid reinventing basic interactions without a strong reason.
- Make similar components behave similarly.

### Example

A mobile app using bottom navigation should generally behave like other familiar mobile apps rather than inventing an unusual navigation model.

---

# 2. Fitts's Law

> **The time required to reach a target depends on its size and distance.**

### Rule

Important and frequently used controls should be large enough and easy to reach.

### Apply it

- Make primary buttons easy to tap.
- Give touch targets sufficient area.
- Place frequently used actions within comfortable reach.
- Avoid tiny controls.
- Keep destructive actions separated from common actions.

### Mobile principle

**Important + frequent = larger + easier to reach.**

---

# 3. Hick's Law

> **The more choices people have, the longer the decision takes.**

### Rule

Reduce unnecessary choices and present complex decisions progressively.

### Apply it

- Don't show every option at once.
- Group related choices.
- Use progressive disclosure.
- Highlight the recommended action.
- Remove low-value options.

### Example

Instead of showing 15 actions on a dashboard, show the 3–5 most important actions and put secondary actions in a menu.

---

# 4. Miller's Law

> **People have limited working memory.**

### Rule

Don't make users remember too much information at once.

### Apply it

- Break complex information into chunks.
- Use headings and sections.
- Use progressive disclosure.
- Show information when it is needed.
- Don't rely on memory between screens.

### Important clarification

The commonly repeated idea that people can hold exactly **7 ± 2 items** is an oversimplification. The practical UX lesson is to reduce working-memory load rather than design around a fixed number.

---

# 5. Doherty Threshold

> **Fast system response helps maintain user engagement.**

### Rule

Interfaces should respond quickly and communicate progress when work takes time.

### Apply it

- Give immediate feedback after interaction.
- Use optimistic UI when safe.
- Use skeleton screens for content loading.
- Show progress for long operations.
- Move expensive work away from the main/UI thread.

### Goal

The user should feel that the interface is responding to them.

---

# 6. Peak-End Rule

> **People tend to remember the most intense part and the ending of an experience.**

### Rule

Design important moments and completion states carefully.

### Apply it

- Make successful completion satisfying.
- Handle errors calmly.
- Make onboarding memorable without adding friction.
- Give users a clear sense of completion.
- Don't end important flows abruptly.

---

# 7. Serial Position Effect

> **People tend to remember items near the beginning and end of a sequence better.**

### Rule

Place important information at strategic positions.

### Apply it

- Put high-priority items first.
- Put important actions at the end where appropriate.
- Don't bury critical information in the middle of long lists.

### Example

Navigation systems often place the most important destinations at the beginning or end of a navigation structure.

---

# 8. Von Restorff Effect

> **An item that differs from surrounding items is more likely to be noticed.**

### Rule

Use visual difference to emphasize important information.

### Apply it

- Highlight the primary CTA.
- Visually distinguish selected states.
- Emphasize warnings carefully.
- Don't make everything visually loud.

### Principle

**If everything is emphasized, nothing is emphasized.**

---

# 9. Zeigarnik Effect

> **Unfinished tasks tend to remain psychologically active.**

### Rule

Make unfinished work visible and easy to continue.

### Apply it

- Save drafts automatically.
- Show incomplete profiles.
- Display progress.
- Provide "Continue" actions.
- Restore unfinished forms.

### Example

`Profile 70% complete → Continue profile`

---

# 10. Aesthetic-Usability Effect

> **People often perceive aesthetically pleasing interfaces as easier to use.**

### Rule

Visual quality influences perceived usability, but aesthetics cannot compensate for broken functionality.

### Apply it

- Use consistent spacing.
- Establish visual hierarchy.
- Use typography intentionally.
- Maintain visual consistency.
- Avoid unnecessary decoration.

### Remember

**Beautiful + confusing = bad UX.**

---

# 11. Tesler's Law

> **Every system has some inherent complexity.**

### Rule

Complexity cannot always be eliminated; designers decide where it lives.

### Apply it

- Hide technical complexity from users where possible.
- Don't expose implementation details unnecessarily.
- Use sensible defaults.
- Automate repetitive decisions.
- Keep advanced controls available without making them mandatory.

### Goal

**The system should carry complexity whenever it can.**

---

# 12. Parkinson's Law

> **Work tends to expand to fill the time available for completion.**

### UX application

Reduce unnecessary steps, waiting, and open-ended workflows.

### Apply it

- Use focused flows.
- Give users clear next actions.
- Avoid unnecessary configuration.
- Provide sensible defaults.
- Don't make simple tasks feel like projects.

---

# 13. Postel's Law

> **Be conservative in what you send and flexible in what you accept.**

### UX application

Be forgiving about reasonable user input while keeping system output predictable.

### Apply it

- Accept common input variations.
- Normalize formatting when appropriate.
- Give useful validation messages.
- Don't punish harmless formatting differences.
- Keep system behavior consistent.

### Example

A phone number field should handle reasonable spaces or separators rather than rejecting the user unnecessarily.

---

# 14. Occam's Razor

> **Prefer the simplest solution that adequately solves the problem.**

### Rule

Don't add UI complexity without user value.

### Apply it

- Remove unnecessary controls.
- Prefer simple flows.
- Use familiar patterns.
- Avoid decorative interactions that don't improve the task.
- Ask whether each element has a purpose.

---

# 15. Poka-Yoke

> **Design systems so mistakes are difficult to make.**

### Rule

Prevent errors before they happen instead of only explaining them afterward.

### Apply it

- Disable impossible actions.
- Use input constraints.
- Provide confirmation for destructive actions.
- Use sensible defaults.
- Validate data at the right time.
- Provide undo where possible.

### Example

Instead of allowing an invalid date to be submitted and then showing an error, prevent invalid dates from being selected.

---

# 16. Goal-Gradient Effect

> **People tend to increase effort as they get closer to a goal.**

### Rule

Make progress visible.

### Apply it

- Show completion percentage.
- Use step indicators.
- Show remaining tasks.
- Celebrate meaningful milestones.
- Make the next step obvious.

### Example

`4 of 5 steps completed`

---

# 17. Law of Prägnanz

> **People tend to perceive and interpret visual information in the simplest organized way possible.**

### Rule

Prefer clear, simple visual structures.

### Apply it

- Reduce visual noise.
- Use clear hierarchy.
- Align elements.
- Group related content.
- Avoid unnecessary shapes and decoration.

---

# 18. Law of Proximity

> **Objects that are close together are perceived as related.**

### Rule

Use spacing to communicate relationships.

### Apply it

- Keep labels close to their controls.
- Keep related settings together.
- Separate unrelated sections with larger spacing.
- Don't rely only on borders.

### Key principle

**Spacing is a communication tool.**

---

# 19. Law of Similarity

> **Similar-looking elements are perceived as belonging together.**

### Rule

Use consistent visual treatment for elements with similar functions.

### Apply it

- Same button type → same visual language.
- Same status → same treatment.
- Same information type → same typography.
- Same interaction → same affordance.

---

# 20. Law of Common Region

> **Elements inside the same bounded area are perceived as a group.**

### Rule

Use cards, containers, backgrounds, or other boundaries when grouping helps comprehension.

### Apply it

- Group related settings.
- Group form sections.
- Group dashboard metrics.
- Avoid excessive cards when spacing alone is sufficient.

---

# 21. Law of Common Fate

> **Elements moving in the same direction are perceived as related.**

### Rule

Use coordinated animation to communicate relationships.

### Apply it

- Animate related elements together.
- Use shared transitions.
- Use movement to show where content came from or where it goes.
- Avoid random animation.

---

# 22. Law of Continuity

> **People tend to perceive continuous lines and patterns as related.**

### Rule

Use alignment and visual flow to guide attention.

### Apply it

- Align related content.
- Maintain consistent grids.
- Use directional visual hierarchy.
- Avoid unnecessary alignment breaks.

---

# 23. Law of Closure

> **People tend to mentally complete incomplete visual patterns.**

### Rule

Simple incomplete shapes can still communicate effectively.

### Apply it

- Use partial shapes carefully.
- Use familiar iconography.
- Don't rely on closure when the meaning could become ambiguous.

---

# 24. Law of Uniform Connectedness

> **Visually connected elements are perceived as belonging together.**

### Rule

Use lines, shared backgrounds, connectors, or other visual connections to communicate relationships.

### Apply it

Useful for:

- Timelines
- Step indicators
- Flow diagrams
- Connected settings
- Data relationships

---

# 25. Nielsen's 10 Usability Heuristics

These are heuristics rather than strict mathematical laws, but they are among the most useful UX evaluation principles.

## 25.1 Visibility of System Status

> Keep users informed about what is happening.

Use:

- Loading states
- Progress indicators
- Success messages
- Sync status
- Upload progress

---

## 25.2 Match Between System and Real World

> Speak the user's language.

Use:

- Familiar terminology
- Familiar concepts
- Natural ordering
- Real-world metaphors when useful

Avoid technical implementation language.

---

## 25.3 User Control and Freedom

> Users should be able to recover from actions.

Provide:

- Back
- Cancel
- Undo
- Close
- Edit
- Restore

---

## 25.4 Consistency and Standards

> Similar things should look and behave similarly.

Don't make the same icon perform different actions in different parts of the app.

---

## 25.5 Error Prevention

> Prevent problems before they occur.

Use:

- Validation
- Constraints
- Confirmation for destructive actions
- Safe defaults
- Undo

---

## 25.6 Recognition Rather Than Recall

> Don't make users remember information unnecessarily.

Prefer:

- Visible options
- Suggestions
- Recent items
- Autocomplete
- Contextual actions

---

## 25.7 Flexibility and Efficiency of Use

> Support both beginners and experienced users.

Use:

- Shortcuts
- Quick actions
- Recent items
- Smart defaults
- Bulk operations

---

## 25.8 Aesthetic and Minimalist Design

> Every element competes for attention.

Remove information that doesn't help users accomplish their task.

---

## 25.9 Help Users Recognize, Diagnose, and Recover From Errors

Good error messages should explain:

1. What happened.
2. Why it happened when useful.
3. What the user can do next.

### Bad

`Error 403`

### Better

`You don't have permission to edit this vehicle.`

---

## 25.10 Help and Documentation

> Good UX should reduce the need for documentation, but help should exist when users need it.

Use:

- Contextual help
- Empty-state guidance
- Tooltips where appropriate
- FAQs
- Searchable help

---

# 26. Practical Mobile/Web Interaction Rules

These are practical design rules derived from common usability patterns.

## Navigation

1. **An X closes a modal or flow.**
2. **A back arrow goes back one screen.**
3. **Don't change the meaning of navigation controls.**
4. **Keep primary navigation stable.**
5. **Deep links should open the exact referenced content.**
6. **Don't unexpectedly reset navigation state.**

## Loading

7. **Never show a confusing blank screen while content loads.**
8. **Use skeletons, spinners, or progress indicators when appropriate.**
9. **Show immediate feedback after important interactions.**
10. **Load the next feed batch before users reach the end.**

## State Preservation

11. **Preserve scroll position when users return to a feed.**
12. **Preserve filters and relevant screen state.**
13. **Automatically save drafts and unfinished input.**
14. **Don't make users re-enter information unnecessarily.**

## Bottom Navigation

15. **Tapping the currently selected tab can return the user to the top.**
16. **Don't overload bottom navigation with too many destinations.**
17. **Use clear labels and recognizable icons.**

## Permissions

18. **Ask for permissions at the moment of need.**
19. **Explain why a sensitive permission is needed when appropriate.**
20. **Don't request camera, location, photos, microphone, or notifications before the user needs the feature.**

## Bottom Sheets

21. **Use bottom sheets for contextual or quick actions when appropriate.**
22. **Allow standard dismissal gestures when supported.**
23. **Don't hide critical navigation inside temporary surfaces.**

## Forms

24. **Ask only for information that is needed.**
25. **Use appropriate input types.**
26. **Preserve entered data.**
27. **Validate close to the point of error.**
28. **Show useful error messages.**
29. **Use defaults when they are genuinely helpful.**
30. **Don't clear a user's entire form because of one invalid field.**

## Feedback

31. **Every important user action should have understandable feedback.**
32. **Use optimistic UI when the action can safely be assumed successful.**
33. **If an operation fails, preserve the user's work.**
34. **Don't use animations that delay basic tasks.**
35. **Use animation to explain change, not merely decorate the interface.**

## Destructive Actions

36. **Make destructive actions visually distinguishable.**
37. **Require confirmation when the consequence is serious and irreversible.**
38. **Prefer Undo when recovery is possible.**
39. **Never make destructive actions easy to trigger accidentally.**

---

# 27. UX Design Checklist

Use this checklist before shipping a screen.

## Understandability

- [ ] Can the user understand the screen within a few seconds?
- [ ] Is the primary action obvious?
- [ ] Are labels understandable?
- [ ] Are interactive elements visually recognizable?

## Navigation

- [ ] Is navigation predictable?
- [ ] Does Back behave consistently?
- [ ] Does Close behave consistently?
- [ ] Are deep links opening the correct destination?
- [ ] Is important state preserved?

## Interaction

- [ ] Are touch targets large enough?
- [ ] Are frequent actions easy to reach?
- [ ] Are there unnecessary choices?
- [ ] Are gestures discoverable?
- [ ] Are interactions consistent?

## Feedback

- [ ] Does every important action provide feedback?
- [ ] Is loading communicated?
- [ ] Is success communicated?
- [ ] Is failure communicated?
- [ ] Can the user recover?

## Forms

- [ ] Is unnecessary input removed?
- [ ] Is entered information preserved?
- [ ] Are errors explained clearly?
- [ ] Are sensible defaults used?
- [ ] Is validation helpful rather than annoying?

## Visual Hierarchy

- [ ] Is the most important content visually dominant?
- [ ] Is spacing communicating relationships?
- [ ] Are similar elements visually consistent?
- [ ] Is there enough contrast?
- [ ] Is visual noise minimized?

## Performance

- [ ] Does the UI respond immediately?
- [ ] Are expensive operations off the main/UI thread?
- [ ] Are loading states implemented?
- [ ] Is pagination/prefetching used where appropriate?
- [ ] Does the interface remain usable during network delays?

## Accessibility

- [ ] Can users understand the interface without relying only on color?
- [ ] Are touch targets accessible?
- [ ] Are text sizes readable?
- [ ] Are content descriptions provided where needed?
- [ ] Does keyboard/screen-reader navigation work where applicable?

---

# The UX Laws in One Line

| Principle | Remember |
|---|---|
| Jakob | **Make it familiar.** |
| Fitts | **Make important targets easy to hit.** |
| Hick | **Reduce unnecessary choices.** |
| Miller | **Reduce memory load.** |
| Doherty | **Keep the interface responsive.** |
| Peak-End | **Design the important moments and ending.** |
| Serial Position | **Put important items strategically.** |
| Von Restorff | **Make important things stand out.** |
| Zeigarnik | **Make unfinished work easy to continue.** |
| Aesthetic-Usability | **Visual quality affects perceived usability.** |
| Tesler | **Hide complexity where possible.** |
| Parkinson | **Keep workflows focused.** |
| Postel | **Be forgiving with reasonable input.** |
| Occam | **Prefer simple solutions.** |
| Poka-Yoke | **Prevent mistakes.** |
| Goal-Gradient | **Show progress toward goals.** |
| Prägnanz | **Prefer simple visual structures.** |
| Proximity | **Spacing communicates relationships.** |
| Similarity | **Similar things should look similar.** |
| Common Region | **Boundaries create groups.** |
| Common Fate | **Shared movement communicates relationships.** |
| Continuity | **Alignment creates visual flow.** |
| Closure | **The brain completes familiar patterns.** |
| Uniform Connectedness | **Visual connections communicate relationships.** |
| Nielsen Heuristics | **Make the system visible, predictable, forgiving, and understandable.** |

---

# Final Design Philosophy

A strong UI/UX system should follow these principles:

**Familiar → Clear → Fast → Predictable → Forgiving → Accessible → Consistent → Minimal**

The goal is not to use every law on every screen.

The goal is to choose the principles that solve the user's actual problem.

> **Don't design interfaces that make users learn your system. Design systems that fit what users already know.**


