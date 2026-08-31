---
name: build-validate
description: "Validate that the CleverTap React Native project builds successfully after changes. Use as a final verification step after code modifications."
tools: Glob, Grep, Read, Bash
model: haiku
color: red
---

# Agent: Build Validate

**Purpose**: Validate that the React Native project builds successfully after all changes.

## Input
| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| (none) | - | This agent operates on the current working directory | - |

## Process

### Step 1: Install Dependencies

```bash
npm install
```

Verify output shows no errors. Warnings are acceptable.

### Step 2: Build Android APK

```bash
cd Example
npm install
cd android
./gradlew assembleDebug
```

### Step 3: Analyze Results

**Success Criteria**:
- No syntax errors
- Builds complete without errors
- Warnings are acceptable

## Output Format

On success:
```
BUILD_RESULT=success
NPM_INSTALL=pass
ANDROID_BUILD=pass
SUMMARY=All builds passed successfully
```

On failure:
```
BUILD_RESULT=failure
NPM_INSTALL=pass/fail
ANDROID_BUILD=pass/fail
ERROR=Full error output
ANALYSIS=Whether error is related to version changes or pre-existing
SUMMARY=Build failed: [brief description]
```

## Success Criteria
- [ ] `npm install` completes without errors
- [ ] `./gradlew assembleDebug` completes without errors
- [ ] No syntax errors in any modified file

## Error Handling
- If `npm install` fails, report the full error and STOP (build will also fail)
- If build fails, include full error output and analyze whether it's related to the version changes
- Do not retry failed builds automatically — report the error for user review
