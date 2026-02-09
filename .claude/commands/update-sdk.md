# Command: Update Native SDKs

**Purpose**: Update CleverTap React Native SDK to use latest native Android and iOS SDK versions

**Usage**: Ask Claude to "run update-sdk command" or "update to latest native SDKs"

---

## Overview

This command orchestrates the complete SDK update workflow by:
1. Detecting current versions using **version-detection** skill
2. Fetching latest SDK versions from GitHub
3. Analyzing native SDK changelogs for API changes
4. Updating all version files consistently
5. Implementing new API wrappers using **api-wrapper-patterns** skill
6. Update Example app using **example-app-patterns** skill
7. Generating changelog entry using **changelog-generation** skill
8. Validating builds

---

## Execution Modes

### Interactive Mode (Default)
- Prompts for confirmation at key decision points
- Allows manual version selection
- Best for: First-time updates, learning the process

### Auto-Confirm Mode
- Automatically proceeds with detected versions
- No interactive prompts
- Best for: CI/CD pipelines, trusted automation

**Trigger**: Set environment variable `CLAUDE_AUTO_CONFIRM=true`

---

## Phase 0: Pre-Flight Checks

### 0.1 Detect Execution Mode

Check if `CLAUDE_AUTO_CONFIRM` environment variable is set:
```
IF CLAUDE_AUTO_CONFIRM == "true" THEN
    AUTO_CONFIRM_MODE = true
    Log: "[AUTO-CONFIRM] Running in automated mode"
ELSE
    AUTO_CONFIRM_MODE = false
    Log: "[INTERACTIVE] Running in interactive mode"
END IF
```

## Phase 1: Gather Version Information

### 1.1 Read Current Versions

**Use the version-detection skill** to extract:

```
OLD_CLEVERTAP_RN_VERSION = Extract from package.json
OLD_ANDROID_VERSION = Extract from android/build.gradle (SDK dependency)
OLD_IOS_VERSION = Extract from clevertap-react-native.podspec (SDK dependency)
```

**Log current state**:
```
Current React Native Plugin: v{OLD_CLEVERTAP_RN_VERSION}
Current Android SDK:         v{OLD_ANDROID_VERSION}
Current iOS SDK:             v{OLD_IOS_VERSION}
```

### 1.2 Fetch Latest Native SDK Versions

**Android**:
1. Fetch: `https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md`
2. Parse first `Version X.Y.Z` entry
3. Store as `NEW_ANDROID_VERSION`

**iOS**:
1. Fetch: `https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md`
2. Parse first `Version X.Y.Z` entry
3. Store as `NEW_IOS_VERSION`

**Error Handling**:
- Retry up to 3 times with exponential backoff
- If still failing, STOP and report error
- Do not proceed without version information

### 1.3 Version Confirmation

**If AUTO_CONFIRM_MODE = false (Interactive)**:
```
Detected latest Android SDK: {NEW_ANDROID_VERSION}
Current Android SDK: {OLD_ANDROID_VERSION}

Options:
1. Proceed with detected version ({NEW_ANDROID_VERSION})
2. Manually specify a different version
3. Cancel operation

Please select (1/2/3):
```

Wait for user input and validate.

**If AUTO_CONFIRM_MODE = true (Auto-Confirm)**:
```
[AUTO-CONFIRM] Detected latest Android SDK: {NEW_ANDROID_VERSION}
[AUTO-CONFIRM] Current: {OLD_ANDROID_VERSION}
[AUTO-CONFIRM] Proceeding automatically
```

Repeat for iOS.

### 1.4 Calculate New React Native Version

Determine semantic version bump:

```
IF breaking changes in native SDKs THEN
    NEW_RN_VERSION = Increment MAJOR (X.0.0)
ELSE IF new APIs or features THEN
    NEW_RN_VERSION = Increment MINOR (x.Y.0)
ELSE IF only bug fixes THEN
    NEW_RN_VERSION = Increment PATCH (x.y.Z)
END IF
```
---

## Phase 2: Update Version Files

**Use version-detection skill** to know which files to update.

Update all locations:

### 2.1 package.json
```json
"version": "{NEW_RN_VERSION}"
```

### 2.2 android/build.gradle (versionName)
```gradle
versionName "{NEW_RN_VERSION}"
```

### 2.3 android/build.gradle (versionCode)
```gradle
versionCode {NEW_RN_VERSION_AS_INT}
versionName = "{NEW_RN_VERSION}"
```
Convert: 3.9.0 -> 390

### 2.4 android/build.gradle (SDK Dependency)
```gradle
implementation 'com.clevertap.android:clevertap-android-sdk:{NEW_ANDROID_VERSION}'
```

### 2.5 clevertap-react-native.podspec (iOS SDK Dependency)
```ruby
s.dependency 'CleverTap-iOS-SDK', '{NEW_IOS_VERSION}'
```

### 2.6 src/index.js (Library Version Constant)
```javascript
const libVersion = {NEW_RN_VERSION_AS_INT};
```
Convert: 3.9.0 -> 30900

**Verification**: Read back each file to confirm changes applied correctly.

---

## Phase 3: Analyze Native SDK Changes

**Use the native-sdk-changelog-analysis skill** with parameters:
- `platform`: `"both"`
- `old_version`: `OLD_ANDROID_VERSION` / `OLD_IOS_VERSION`
- `new_version`: `NEW_ANDROID_VERSION` / `NEW_IOS_VERSION`

The skill will:
1. Extract and categorize all changelog entries
2. Verify method signatures from native source code
3. Generate a unified implementation plan table
4. **Wait for user acknowledgment** before proceeding

---

## Phase 4: Implement API Wrappers

**MANDATORY**: Implement ALL items in wrapper implementation plan marked NEW_IMPLEMENTATION or UPDATE.

**Do NOT skip this phase** without explicit user approval.

### CRITICAL: Pre-Implementation Checklist

**BEFORE writing ANY wrapper code, you MUST:**

```
Step 1: READ `.claude/skills/api-wrapper-patterns/SKILL.md` in full
Step 2: CHECK the examples for the pattern you're implementing
Step 3: Only THEN proceed to write code
```

## Phase 5: Update Example App

**MANDATORY**: Add ALL items sample usage which are marked as `NEW_IMPLEMENTATION` or `UPDATE` from the wrapper implementation plan.

- Use the **example-app-patterns** skill
- Update `Example/app/` with working examples

## Phase 6: Generate Changelog

**Use changelog-generation skill** for formatting rules.

---

## Phase 7: Build Validation

### 7.1 Install Dependencies
```bash
npm install
yalc push
```

### 7.2 Build Test (Android)
```bash
cd Example
npm install
cd android
./gradlew assembleDebug
cd ../..
```

### 7.3 Build Test (iOS)
```bash
cd Example/ios
pod install
cd ..
npx react-native build-ios --mode Debug
cd ..
```

**Success Criteria**:
- No syntax errors
- Builds complete without errors
- Warnings acceptable

**If build fails**:
- Show full error output
- Analyze if error is related to version changes
- Ask user how to proceed

---

## Success Criteria

Task complete when:
- All version files updated consistently
- Native SDK dependencies updated
- All new APIs analyzed
- Necessary wrappers implemented (or user confirmed not needed)
- Changelog entry added with correct format
- Changelog links validated
- All builds pass

---

## Error Handling

### Network Errors
- Retry changelog fetching up to 3 times
- If persistent failure, STOP and report error

### Version Parse Errors
- Report exact file and pattern searched
- Ask user to verify file format
- Do not make assumptions

### Build Failures
- Report full error output
- Analyze if related to version changes
- If uncertain, ask user before proceeding

---

## Related Skills

- **version-detection** - Used in Phases 1 and 2
- **native-sdk-changelog-analysis** - Used in Phase 3
- **api-wrapper-patterns** - Used in Phase 4 for SDK APIs
- **example-app-patterns** - Used in Phase 5 for example app updates
- **changelog-generation** - Used in Phase 6
