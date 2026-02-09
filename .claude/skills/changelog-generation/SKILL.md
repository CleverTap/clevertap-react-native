---
name: changelog-generation
description: Generate properly formatted changelog entries for CleverTap React Native SDK releases. Use when updating native SDK versions, adding new features, fixing bugs, or preparing any release. Handles strict formatting rules, version anchor generation, and link validation.
---

# Changelog Generation

> **MANDATORY READING**: This skill document MUST be read in full BEFORE writing any changelog entry. The format is strict and parsed by automation tools. Follow the patterns exactly.

Generate properly formatted changelog entries for CleverTap React Native SDK releases with correct dates, platform tags, and native SDK changelog links.

## Critical Rules

1. **Add at TOP** - New entries MUST be added before all existing entries (after the `Change Log` / `==========` header)
2. **Date format** - `(Month DD YYYY)` e.g., `*(October 3 2025)*`
   - Month first, then day, then year
   - No comma between day and year
   - No leading zero on day (`3` not `03`)
   - Full month name (`January` not `Jan`)
   - Wrapped in `*( )*` for italics
3. **Platform tags** - Use exactly: `[Android Platform]`, `[iOS Platform]`, `[Android and iOS Platform]`
4. **Link native changelogs** - Always include version anchor links (see anchor format below)
5. **2-space indentation** - Use 2 spaces for nested bullets under platform tags

## Entry Template

```markdown
Version X.X.X *(Month DD YYYY)*
-------------------------------------------
**What's new**
* **[Platform Name]**
  * Supports [CleverTap Platform SDK vX.X.X](link-with-anchor).
  * Additional feature details...

**API changes**
* **[Platform Name]**
  * Adds a new API `methodName(params)` - Description
    * `methodName(param1, param2)`
  * Deprecates: `oldMethod()` - Use `newMethod()` instead

**Breaking Changes**
* **[Platform Name]**
  * Removed: `deprecatedAPI()` - Migration guide

**Bug Fixes**
* **[Platform Name]**
  * Fixes description of the bug fix.
```
## Changelog Link Format

### Android SDK Base URL

```
https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md
```

### iOS SDK Base URL

```
https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md
```

### Version Anchor Format

Both Android and iOS use the **same anchor generation rule**:

Format: `#version-X-Y-Z-month-day-year` (dots as dashes, lowercase month)

**Generation**:
1. Replace dots with dashes: `7.4.2` -> `7-4-2`
2. Lowercase month: `January` -> `january`
3. Combine: `#version-742-january-14-2026`

**Examples**:
- `7.4.2` (Jan 14, 2026) -> `#version-742-january-14-2026`
- `7.3.3` (Sep 20, 2025) -> `#version-733-september-20-2025`

## Complete Examples

### Native SDK Update (Both Platforms)

```markdown
Version 3.7.0 *(October 3 2025)*
-------------------------------------------
**What's new**
* **[Android Platform]**
  * Supports [CleverTap Android SDK v7.5.2](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-752-september-11-2025).

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v7.3.3](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-733-september-11-2025).
```

### With New APIs

```markdown
Version 3.8.0 *(December 22 2025)*
-------------------------------------------
**What's new**
* **[Android Platform]**
  * Supports [CleverTap Android SDK v7.7.1](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-771-december-02-2025).

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v7.4.1](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-741-december-02-2025).

**API changes**
* **[Android and iOS Platform]**
  * Adds a new API `variants` to fetch A/B experiment variants for the user, enabling easier access to experiment data for custom implementations.
    * `variants()`
  * Adds a new overloaded API for `discardInAppNotifications()` method. Calling this with `true` immediately dismisses any currently visible In-App notification in addition to clearing the queue of pending messages.
    * `discardInAppNotifications(dismissInAppIfVisible: Boolean)`
```

### With Bug Fixes

```markdown
Version 3.8.1 *(January 14 2026)*
-------------------------------------------
**What's new**
* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v7.4.2](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-742-january-14-2026).

**Bug Fixes**
* **[iOS Platform]**
  * Fixes a bug where DOB value is passed incorrectly when performing profile events.
  * Optimizes app initialization sequence by loading variable and variant caches earlier in the startup process.
```

### With Breaking Changes

```markdown
Version 3.0.0 *(October 8 2024)*
-------------------------------------------
**What's new**
* **[Android Platform]**
  * Supports [CleverTap Android SDK v7.0.1](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-701-september-2-2024).

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v7.0.1](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-701-august-22-2024).

**Breaking Changes**
* **[Android Platform]**
  * Ensure that your custom `Application` class extends `CleverTapApplication` or calls `CleverTapRnAPI.initReactNativeIntegration(this);`.

**Bug Fixes**
* **[Android Platform]**
  * Fixes an ANR caused by extremely old InApp campaigns.
```

### API Change with Details

When an existing API is updated (new optional parameter, behavior change), use this pattern:

```markdown
**API changes**
* **[Android and iOS Platform]**
  * Updates the `setOptOut(userOptOut)` API. This upgraded API improves GDPR opt-out functionality by allowing you to control whether critical system events are still sent to CleverTap. This is a non-breaking change.
    * `setOptOut(userOptOut, allowSystemEvents)`
```

### Issue/Bug Fix References

When a bug fix references a GitHub issue, use this format:
```markdown
* Fixes [#480](https://github.com/CleverTap/clevertap-react-native/issues/480) – description of the issue.
```

## Release Date Calculation

**Default**: Calculate the date 3 days from the current date.

Format: `Month DD YYYY`
- Month: Full name (`January` not `Jan`)
- Day: No leading zero (`5` not `05`)
- Year: 4 digits (`2026` not `26`)
- No comma between day and year

Example: If today is February 2, 2026, use `February 5 2026`.

## Content Guidelines

**Important**: Use active voice: "Adds support for..." not "Support was added"

### "What's new"
- Always start with native SDK support line(s)
- Each platform gets its own bullet group

### "API changes"
- List new public methods with backtick-formatted signatures
- For complex APIs, include brief parameter descriptions
- For updated APIs, describe what changed and note if it's non-breaking

### "Breaking Changes"
- Clearly explain what changed
- Provide migration instructions or link to migration guide

### "Bug Fixes"
- Start each fix with "Fixes" (active voice)
- For GitHub issue references: `Fixes [#NNN](url) – description`
- Briefly describe the fix and affected scenario

## Insertion Process

1. Read entire `CHANGELOG.md`
2. Find position after the file header:
   ```
   Change Log
   ==========
   ```
3. Insert new entry with a blank line between the header and the new entry
