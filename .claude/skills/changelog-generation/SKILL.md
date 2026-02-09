---
name: changelog-generation
description: Generate properly formatted changelog entries for CleverTap React Native SDK releases. Use when updating native SDK versions, adding new features, fixing bugs, or preparing any release. Handles strict formatting rules, version anchor generation, and link validation.
---

# Changelog Generation

> **MANDATORY READING**: This skill document MUST be read in full BEFORE writing any changelog entry. The format is strict and parsed by automation tools. Follow the patterns exactly.

Generate properly formatted changelog entries for CleverTap React Native SDK releases with correct dates, platform tags, and native SDK changelog links.

## Critical Rules

1. **Add at TOP** - New entries MUST be added before all existing entries
2. **Date format** - `(DD Month YYYY)` e.g., `(23 January 2026)`
3. **Platform tags** - Use exactly: `[Android Platform]`, `[iOS Platform]`, `[Android and iOS Platform]`
4. **Link native changelogs** - Always include version anchor links
5. **2-space indentation** - Use 2 spaces for nested bullets

## Entry Template
//todo
```markdown
### Version X.X.X *(DD Month YYYY)*
-------------------------------------------
**What's new**
* **[Platform Name]**
  * Supports [CleverTap Platform SDK vX.X.X](link-with-anchor).
  * Additional feature details...

**API changes** (if applicable)
* **[Platform Name]**
  * New API: `methodName(params)` - Description
  * Deprecated: `oldMethod()` - Use `newMethod()` instead

**Breaking Changes** (if applicable)
* **[Platform Name]**
  * Removed: `deprecatedAPI()` - Migration guide

**Bug Fixes** (if applicable)
* **[Platform Name]**
  * Fixes description
```

## Version Anchor Format

### Android SDK

Format: `#version-XYZ-month-day-year` (no dots, lowercase month)

**Generation**:
1. Remove dots: `7.7.1` -> `771`
2. Lowercase month: `December` -> `december`
3. Combine: `#version-771-december-2-2025`

**Examples**:
- `7.7.1` (Dec 2, 2025) -> `#version-771-december-2-2025`
- `7.6.0` (Oct 17, 2025) -> `#version-760-october-17-2025`

### iOS SDK

Format: `#version-X-Y-Z-month-day-year` (dots as dashes, lowercase month)

**Generation**:
1. Replace dots with dashes: `7.4.2` -> `7-4-2`
2. Lowercase month: `January` -> `january`
3. Combine: `#version-742-january-14-2026`

**Examples**:
- `7.4.2` (Jan 14, 2026) -> `#version-742-january-14-2026`
- `7.3.3` (Sep 20, 2025) -> `#version-733-september-20-2025`

## Complete Examples

### Native SDK Update Only

```markdown
Version 3.9.0 *(23 January 2026)*
-------------------------------------------
**What's new**

* **[Android Platform]**
  * Supports [CleverTap Android SDK v7.7.1](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-771-december-2-2025).
  * Adds support for XYZ

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v7.4.2](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-742-january-14-2026).
```

### With New API

```markdown
Version 3.10.0 *(15 February 2026)*
-------------------------------------------
**What's new**

* **[Android Platform]**
  * Supports [CleverTap Android SDK v7.8.0](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-780-january-30-2026).

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v7.5.0](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-750-february-1-2026).

**API changes**
* **[Android and iOS Platform]**
  * New API: `setCustomInAppListener(callback)` - Allows custom handling of in-app notifications
```

### With Breaking Changes

```markdown
Version 4.0.0 *(1 March 2026)*
-------------------------------------------
**What's new**

* **[Android Platform]**
  * Supports [CleverTap Android SDK v8.0.0](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-800-february-20-2026).

**Breaking Changes**
* **[Android and iOS Platform]**
  * Removes: `recordEvent(eventName, properties)` old callback signature deprecated in v3.5.0
  * Migration: Use the updated `recordEvent(eventName, properties)` without callback
```

## Release Date Calculation

**Default**: Calculate the date 3 days from the current date

Format: DD Month YYYY
- Day: No leading zero (5 not 05)
- Month: Full name (January not Jan)
- Year: 4 digits (2026 not 26)

Example: If today is 2 February 2026, use 5 February 2026

## Content Guidelines
**Important** : Use active voice: "Adds support for..." not "Support was added"

### "What's new"
- Start with native SDK support line

### "API changes"
- List new public methods/classes
- Include parameter descriptions for complex APIs

### "Breaking Changes"
- Clearly explain what changed
- Provide migration instructions

### "Bug Fixes"
- Briefly describe the fix
- Mention affected platforms

## Insertion Process

1. Read entire `CHANGELOG.md`
2. Find position after header (after first `#` line)
3. Insert new entry with blank line before/after
4. Write back to file

**Example**:
```markdown
# Change Log

Version 3.9.0 *(23 January 2026)*     # <-- NEW ENTRY HERE
-------------------------------------------

Version 3.8.1 *(14 January 2026)*    # <-- EXISTING ENTRIES
-------------------------------------------
```

## Common Mistakes

- Date with slashes: `(01/23/2026)` -> Correct: `(23 January 2026)`
- Generic tag: `[Android]` -> Correct: `[Android Platform]`
- Link without anchor: `...CHANGELOG.md` -> Correct: `...CHANGELOG.md#version-742-january-14-2026`
- Entry at bottom of file -> Entry at TOP of file
