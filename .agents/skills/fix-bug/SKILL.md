---
name: fix-bug
description: >-
  Use this skill when the user asks to identify, debug, or fix an issue in the codebase.
---
# Bug Fix Workflow

## Steps
1. **Analyze Error**: Read the logcat, stacktrace, or error message provided by the user.
2. **Locate Source**: Use codebase search tools to find the exact file and line causing the issue.
3. **Propose Fix**: Explain *why* the bug is happening and propose a solution that aligns with Kotlin/Android best practices.
4. **Implement**: Modify the code to fix the bug.
5. **Verify**: Ensure the fix doesn't break VAJRAX's existing UI or architecture. Ask the user to verify the fix in the running app.
