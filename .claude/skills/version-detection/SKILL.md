---
name: version-detection
description: Extract current version numbers from CleverTap React Native SDK files. Use when checking version consistency across files, preparing for SDK updates, or generating version reports. Handles all version locations including package.json, build.gradle, podspec, JS constants, and Example app.
---

# Version Detection

Extract current version numbers from CleverTap React Native SDK files to ensure consistency and prepare for updates.

## Version Locations

The CleverTap React Native SDK maintains version numbers in these locations that must stay synchronized:

| # | File | Location | Pattern |
|---|------|----------|---------|
| 1 | `package.json` | Root | `"version": "X.Y.Z"` |
| 2 | `android/build.gradle` | defaultConfig | `versionName "X.Y.Z"` |
| 3 | `android/build.gradle` | defaultConfig | `versionCode XYZ` |
| 4 | `android/build.gradle` | Dependencies | `clevertap-android-sdk:X.Y.Z` |
| 5 | `clevertap-react-native.podspec` | Dependencies | `CleverTap-iOS-SDK', 'X.Y.Z'` |
| 6 | `src/index.js` | Constant | `const libVersion = XXXXX;` |
| 7 | `Example/android/app/build.gradle` | Dependencies | `clevertap-android-sdk:X.Y.Z` |
| 8 | `docs/install.md` | Code snippet | `clevertap-android-sdk:X.Y.Z` |

**Note**: The podspec reads the plugin version from `package.json` automatically, so it doesn't need a separate version update for the plugin version itself.

**Note**: Locations 4, 7, and 8 must always match — they all specify the Android SDK version (library, Example app, and install docs respectively).

## Extraction Process

### React Native Plugin Version (1, 2, 3, 6)

**Files**: `package.json`, `android/build.gradle`, `src/index.js`

Read file, find version pattern, extract `X.Y.Z` format.

### Native SDK Versions (4, 5, 7, 8)

**Android (library)**: `android/build.gradle` -> `api 'com.clevertap.android:clevertap-android-sdk:X.Y.Z'`
**iOS**: `clevertap-react-native.podspec` -> `s.dependency 'CleverTap-iOS-SDK', 'X.Y.Z'`
**Android (Example app)**: `Example/android/app/build.gradle` -> `implementation 'com.clevertap.android:clevertap-android-sdk:X.Y.Z'`
**Android (install docs)**: `docs/install.md` -> `implementation 'com.clevertap.android:clevertap-android-sdk:X.Y.Z'`

### JS Version Constant (6)

**File**: `src/index.js`
**Pattern**: `const libVersion = XXXXX;`

**Format**: Zero-padded integer
- `3.8.1` -> `30801`
- `3.10.2` -> `301002`

**Convert integer to version**:
1. Pad to 6 digits: `30801` -> `030801`
2. Split into pairs: `03|08|01`
3. Remove leading zeros: `3.8.1`

### Android Version Code (3)

**File**: `android/build.gradle`
**Pattern**: `versionCode XYZ`

**Format**: Integer without zero-padding between segments
- `3.8.1` -> `381`
- `3.10.2` -> `3102`

## Version Consistency Check

**React Native Plugin versions** (1, 2, 6) should all match.
**Version code** (3) should correspond to the version.
**Native SDK versions** (4, 5, 7, 8) are independent of the plugin version.
**Locations 4, 7, and 8 must match** — all specify the Android SDK version.

**Validation**:
1. Extract all React Native Plugin versions
2. Convert JS constant to `X.Y.Z` format
3. Compare all values
4. Report any mismatches

## Valid Version Format

Pattern: `X.Y.Z` where X, Y, Z are integers

Valid: `3.8.1`, `3.10.2`, `4.0.0`
Invalid: `3.8`, `v3.8.1`, `3.8.1-beta`
