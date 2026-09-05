---
name: feature-implementation
description: >-
  Use this skill when the user asks to implement a new feature. Outlines the step-by-step workflow for analyzing, implementing, testing, and getting approval for new features in VAJRAX.
---
# Feature Implementation Workflow

## Process
1. **Analysis**: Analyze how to implement the requested feature based on VAJRAX's design system (Morphism) and MVI architecture. Read `/doc/rule.md` to ensure the feature doesn't violate core philosophies.
2. **Implementation**: Make the necessary code changes using Jetpack Compose and Kotlin.
3. **Review**: Present the changes to the user and explain the logic.
4. **Approval**: Wait for user's green light. If they request changes, iterate.
5. **Testing**: Build and test the feature using the `build-and-deploy` skill.
6. **Commit**: Once approved, commit the changes to version control.
