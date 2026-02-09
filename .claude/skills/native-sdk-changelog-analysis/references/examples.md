# Changelog Analysis Examples

Detailed examples of analyzing native SDK changelogs and generating implementation plans.

## Example 1: Android SDK Update (Single Platform)

**Input:**
```
Platform: android
Old Version: 5.0.0
New Version: 5.1.0
```

**Process:**
1. Fetch Android changelog from GitHub
2. Extract entries between 5.0.0 and 5.1.0
3. Find NEW_API: `getFeatureFlag(String flagName, boolean defaultValue)`
4. Fetch `CleverTapAPI.java` using `web_fetch`
5. Search file and find signature:
   ```java
   @NonNull
   public boolean getFeatureFlag(@NonNull String flagName, boolean defaultValue)
   ```
6. Categorize as `NEW_IMPLEMENTATION`

**Output Table:**

| # | Category | API Name | Android Type | iOS Type | Parameters | Decision | Notes |
|---|----------|----------|--------------|----------|------------|----------|-------|
| 1 | NEW_API | `getFeatureFlag()` | `boolean` | N/A | `flagName: String`, `defaultValue: bool` | NEW_IMPLEMENTATION | Android-only API |

---

## Example 2: Cross-Platform Analysis

**Input:**
```
Platform: both
Old Version: 5.0.0
New Version: 5.1.0
```

**Process:**
1. Analyze Android changelog -> Find `suspendInAppNotifications()`
2. Fetch `CleverTapAPI.java`
3. Find signature: `public void suspendInAppNotifications()`
4. Analyze iOS changelog -> Find `suspendInAppNotifications`
5. Fetch `CleverTap.h`
6. Find signature: `- (void)suspendInAppNotifications;`
7. Confirm types match (both `void`)
8. Categorize as `NEW_IMPLEMENTATION` for both platforms

**Output Table:**

| # | Category | API Name | Android Type | iOS Type | Parameters | Decision | Notes |
|---|----------|----------|--------------|----------|------------|----------|-------|
| 1 | NEW_API | `suspendInAppNotifications()` | `void` | `void` | none | NEW_IMPLEMENTATION | Both platforms |

---

## Example 3: Mixed Categories

**Input:**
```
Platform: both
Old Version: 6.0.0
New Version: 6.2.0
```

**Output Table (all categories):**

| # | Category | API Name | Android Type | iOS Type | Platforms | Decision | Notes |
|---|----------|----------|--------------|----------|-----------|----------|-------|
| 1 | NEW_API | `getVariants()` | `Map<String, Object>` | `NSDictionary<NSString *, id> *` | Android 6.1.0+, iOS 6.1.0+ | NEW_IMPLEMENTATION | |
| 2 | BREAKING | `pushEvent()` | Signature changed | Signature changed | Both | UPDATE | Added required parameter |
| 3 | DEPRECATED | `getLocation()` | `String` | `NSString *` | Both | UPDATE | Use `getCurrentLocation()` |
| 4 | BUG_FIX | Image caching | - | - | iOS 15+ | NO_ACTION | Fixed memory issue |
| 5 | INTERNAL | Database upgrade | - | - | Both | NO_ACTION | Performance improvement |

---

## Example 4: Method Not Found - Verification Flow

**Scenario:** Changelog mentions `newCustomMethod()` but it's not in public API files.

**Response to User:**
```
I couldn't find `newCustomMethod()` in the public API files:
- iOS: CleverTap.h
- Android: CleverTapAPI.java

Could you verify:
1. The exact method name from the changelog?
2. Is this a public-facing API or internal/private?
3. Is the SDK version correct?
```

Do NOT proceed with implementation until user confirms.

---

## Example 5: Complete Implementation Plan Output

```markdown
## Wrapper Implementation Plan

### Summary
- Platform(s): Android, iOS
- Version Range: 5.0.0 -> 5.1.0
- Total Changes: 8
  - NEW_API: 3
  - BREAKING: 1
  - DEPRECATED: 1
  - BUG_FIX: 2
  - INTERNAL: 1

### All Changes

| # | Category | API Name | Android Type | iOS Type | Parameters | Platforms | Decision | Notes |
|---|----------|----------|--------------|----------|------------|-----------|----------|-------|
| 1 | NEW_API | `getAllInboxMessages()` | `ArrayList<CTInboxMessage>` | `NSArray<CleverTapInboxMessage *> *` | none | Android 5.0.0+, iOS 4.2.0+ | NEW_IMPLEMENTATION | Type verified from both native files |
| 2 | NEW_API | `setOptOut(enabled)` | `void` | `void` | `enabled: bool` | Android 4.5.0+, iOS 4.5.0+ | NEW_IMPLEMENTATION | |
| 3 | NEW_API | `getFeatureFlag(name, default)` | `boolean` | N/A | `name: String`, `defaultValue: bool` | Android 5.1.0+ | NEW_IMPLEMENTATION | Android-only API |
| 4 | BREAKING | `oldMethod()` | N/A | N/A | none | Android 7.1.0+ | UPDATE | Removed -- use `newMethod()` instead |
| 5 | DEPRECATED | `legacyMethod()` | `String` | `NSString *` | none | Android 7.1.0+, iOS 7.1.0+ | UPDATE | Add deprecation notice, document replacement |
| 6 | BUG_FIX | Push notification crash | - | - | - | Android 14+ | NO_ACTION | Fixed crash in rendering |
| 7 | BUG_FIX | InApp memory leak | - | - | - | Android, iOS | NO_ACTION | Fixed memory leak |
| 8 | INTERNAL | Network layer refactor | - | - | - | Android, iOS | NO_ACTION | Internal optimization |

**Legend**: NEW_API | BREAKING | DEPRECATED | BUG_FIX | INTERNAL

## Next Steps

Reply with:
- "Approved" - proceed with all implementations
- "Approved with changes" - specify modifications
- "Hold" - review further

Once approved, I will:
1. Implement each item marked `NEW_IMPLEMENTATION` in all 5 layers (JS, TS, TurboModule, Android, iOS)
2. Update items marked `UPDATE`
3. Update documentation and example app
```
