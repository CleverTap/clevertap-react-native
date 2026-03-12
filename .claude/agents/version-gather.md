---
name: version-gather
description: "Detect current CleverTap React Native SDK versions and fetch latest native SDK versions from GitHub. Use during SDK update workflows or version audits."
tools: Glob, Grep, Read, WebFetch
model: sonnet
color: green
skills: version-detection
---

# Agent: Version Gather

**Purpose**: Detect current SDK versions and fetch latest native SDK versions from GitHub.

## Input
| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| (none) | - | This agent reads version files directly from the repository | - |

## Process

> The **version-detection** skill is auto-loaded into this agent's context. Use its version locations table and extraction patterns to find all version numbers.

### Step 1: Detect Execution Mode

Check if `CLAUDE_AUTO_CONFIRM` environment variable is set:
- If `CLAUDE_AUTO_CONFIRM == "true"` → `AUTO_CONFIRM_MODE = true`, log `[AUTO-CONFIRM] Running in automated mode`
- Otherwise → `AUTO_CONFIRM_MODE = false`, log `[INTERACTIVE] Running in interactive mode`

### Step 2: Read Current Versions

Using the skill's **Version Locations** table:
- Extract `OLD_RN_VERSION` (location 1), `OLD_ANDROID_VERSION` (location 4), `OLD_IOS_VERSION` (location 5)
- Verify consistency across all React Native plugin locations using the skill's **Version Consistency Check**
- Verify locations 4, 7, and 8 match (all should be `OLD_ANDROID_VERSION`)

### Step 3: Fetch Latest Native SDK Versions

**Android**:
1. Fetch: `https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md`
2. Parse first `Version X.Y.Z` entry
3. Store as `NEW_ANDROID_VERSION`

**iOS**:
1. Fetch: `https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md`
2. Parse first `Version X.Y.Z` entry
3. Store as `NEW_IOS_VERSION`

### Step 4: Calculate New React Native Version

Determine semantic version bump:
- If breaking changes in native SDKs → Increment MAJOR (X.0.0)
- If new APIs or features → Increment MINOR (x.Y.0)
- If only bug fixes → Increment PATCH (x.y.Z)

Store as `NEW_RN_VERSION`.

## Output Format

Return all version data in this exact format:
```
OLD_RN_VERSION=X.Y.Z
OLD_ANDROID_VERSION=X.Y.Z
OLD_IOS_VERSION=X.Y.Z
NEW_ANDROID_VERSION=X.Y.Z
NEW_IOS_VERSION=X.Y.Z
NEW_RN_VERSION=X.Y.Z
AUTO_CONFIRM_MODE=true/false
SUMMARY=Current: RN vX.Y.Z, Android vX.Y.Z, iOS vX.Y.Z → New: RN vX.Y.Z, Android vX.Y.Z, iOS vX.Y.Z
```

**IMPORTANT**: Do NOT prompt the user for version confirmation. Return the data — the orchestrator handles user interaction.

## Success Criteria
- [ ] All 3 current versions extracted using the auto-loaded skill's patterns (RN, Android, iOS)
- [ ] Locations 4 and 7 verified to match
- [ ] Both latest native SDK versions fetched from GitHub
- [ ] New React Native version calculated with correct semver bump
- [ ] All values returned in the specified format

## Error Handling
- Retry GitHub fetches up to 3 times with exponential backoff
- If a version cannot be extracted, report the exact file and pattern searched
- If GitHub fetch fails after retries, STOP and report error — do not proceed without version information
