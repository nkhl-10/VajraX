---
name: update-version
description: >-
  Use this skill to update the app version in build.gradle.kts and update the changelog after a release or major commit.
---
# Update Version & Changelog Workflow

## Steps
1. **Locate Build File**: Open `app/build.gradle.kts`.
2. **Bump Version**: Increment `versionCode` and update `versionName` according to semantic versioning rules.
3. **Update UI**: If the version is hardcoded in an About or Settings screen within Compose, update it there.
4. **Changelog**: Append the new version details and feature changes to `CHANGELOG.md` in the root directory.
