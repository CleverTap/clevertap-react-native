---
name: version-update
description: "Update all version file locations in the CleverTap React Native SDK with confirmed version numbers. Use after version-gather has determined target versions."
tools: Glob, Grep, Read, Edit
model: sonnet
color: yellow
skills: version-detection
---

# Agent: Version Update

**Purpose**: Update all version file locations with confirmed version numbers.

## Input
| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `NEW_RN_VERSION` | Yes | New React Native plugin version | `3.9.0` |
| `NEW_ANDROID_VERSION` | Yes | New Android SDK version | `7.8.0` |
| `NEW_IOS_VERSION` | Yes | New iOS SDK version | `7.5.0` |

## Process

> The **version-detection** skill is auto-loaded into this agent's context. It contains the authoritative version locations table with all 6 file paths and their patterns.

### Step 1: Update All Locations

For each row in the skill's **Version Locations** table, replace the current pattern with the appropriate new version:
- React Native plugin locations (1, 2, 6) → `NEW_RN_VERSION`
- Android versionCode (3) → convert using integer without zero-padding (3.9.0 → 390)
- Android SDK dependency (4) → `NEW_ANDROID_VERSION`
- iOS SDK dependency (5) → `NEW_IOS_VERSION`
- JS constant (6) → convert using the skill's zero-padded 5-digit formula (3.9.0 → 30900)
- Example app Android SDK dependency (7) → `NEW_ANDROID_VERSION` (must match location 4)
- Install docs Android SDK version (8) → `NEW_ANDROID_VERSION` (must match locations 4 and 7)

### Step 2: Verify All Changes

Read back each modified file and confirm:
- The old version is no longer present
- The new version is correctly set
- No formatting was broken

## Output Format

```
UPDATE_RESULT=success/failure
FILES_UPDATED=package.json, android/build.gradle, clevertap-react-native.podspec, src/index.js, Example/android/app/build.gradle, docs/install.md
SUMMARY=Updated 8 version locations: RN v{NEW_RN_VERSION}, Android v{NEW_ANDROID_VERSION}, iOS v{NEW_IOS_VERSION}
```

If any file failed:
```
UPDATE_RESULT=failure
FAILED_FILES=file1, file2
ERROR=Description of what went wrong
```

## Success Criteria
- [ ] All 8 version locations (per the auto-loaded skill) updated
- [ ] Each file read back and verified
- [ ] No formatting broken in any file
- [ ] Version integer correctly calculated for JS constant and Android versionCode
- [ ] Locations 4, 7, and 8 (Android SDK) match exactly

## Error Handling
- If a file cannot be read, report the exact path and error
- If a pattern is not found in a file, report the file and expected pattern
- Do not make assumptions — ask user to verify file format if pattern is missing
