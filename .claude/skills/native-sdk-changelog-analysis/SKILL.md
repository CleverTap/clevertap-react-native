---
name: native-sdk-changelog-analysis
description: Analyze CleverTap native SDK changelogs to identify API changes and generate implementation plans for React Native wrapper updates. Use during SDK version updates, when investigating impact of version changes, planning API wrapper implementations, or reviewing breaking changes before updates.
---

# Native SDK Changelog Analysis

Extract and categorize changes from native Android and iOS SDK changelogs, verify method signatures from source code, and generate structured implementation plans for React Native wrapper updates.

## Input Parameters

| Parameter | Required | Description | Example |
|-----------|----------|-------------|---------|
| `platform` | Yes | Target platform(s) | `"android"`, `"ios"`, `"both"` |
| `old_version` | Yes | Starting version | `"7.0.0"` |
| `new_version` | Yes | Target version | `"7.1.0"` |

### Default Changelog URLs

- **Android**: `https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md`
- **iOS**: `https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md`

## Process Overview

```
Step 1: Fetch Changelog Content
       |
Step 2: Categorize Changes
       |
Step 3: Extract Method Information
       |
Step 4: Determine Return Types <-- MUST fetch native SDK files
       |
Step 5: Determine Wrapper Requirements
       |
Step 6: Generate Implementation Plan -> Wait for user acknowledgment
```

## Step-by-Step Process

### Step 1: Fetch Changelog Content

Use `web_fetch` to retrieve changelog from GitHub. Extract all entries between `old_version` and `new_version` (inclusive). Retry up to 3 times on failure.

### Step 2: Categorize Changes

| Category | Trigger Keywords | Action Needed |
|----------|------------------|---------------|
| `NEW_API` | "New", "Added", "Introduced" | Wrapper implementation |
| `BREAKING` | "Removed", "Breaking", "Changed signature" | Wrapper update |
| `DEPRECATED` | "Deprecated", "Obsolete" | Documentation update |
| `BUG_FIX` | "Fixed", "Bug", "Issue" | No wrapper changes |
| `INTERNAL` | "Refactored", "Optimized", "Internal" | No wrapper changes |

### Step 3: Extract Method Information

For each `NEW_API` or `BREAKING` change, extract:
- Method name (full qualified)
- Parameters (name, type, nullability)
- Platform version where introduced

### Step 4: Determine Return Types

For ALL `NEW_API` and `BREAKING` changes items, attempt to fetch and verify return types from native source code.

**Native SDK Files to Fetch:**

| Platform | File URL |
|----------|----------|
| Android | `https://raw.githubusercontent.com/CleverTap/clevertap-android-sdk/master/clevertap-core/src/main/java/com/clevertap/android/sdk/CleverTapAPI.java` |
| iOS | `https://raw.githubusercontent.com/CleverTap/clevertap-ios-sdk/master/CleverTapSDK/CleverTap.h` |

**Process:**
1. Fetch the appropriate file using `web_fetch`
2. Search for method name (case-insensitive)
3. Extract complete signature with return type and generics

**IMPORTANT: If method signature not found in source files:**

APIs mentioned in changelogs MUST still be implemented even if return type verification fails. Use the following fallback strategy:

1. **Cross-platform inference**: If the same API exists on the other platform with a verified signature, infer the return type
2. **Changelog description inference**: Use the changelog description to infer return type (e.g., "returns variants" -> `Array`)
3. **Common pattern inference**: Use [references/type-mapping.md](references/type-mapping.md) for standard type mappings
4. **Mark as "inferred"**: In the implementation plan table, note `(inferred)` next to the type

**DO NOT skip implementation** just because the exact signature couldn't be verified from source. The changelog is the authoritative source for what APIs exist.

### Step 4b: Verify Type Compatibility Through the Bridge

For each change (including BUG_FIX and INTERNAL that mention data handling), verify that the React Native bridge layer correctly supports the types involved. This catches cases where a native SDK change affects existing wrappers without adding a new API.

**When to check:**
- Changelog mentions new data shapes (e.g., "nested objects", "nested maps", "deep properties")
- Changelog mentions expanded type support (e.g., "now accepts arrays", "supports null values")
- Changelog mentions serialization/encoding changes
- Changelog mentions changes to event, profile, or user property handling

**How to check:**
1. Identify which existing React Native wrapper method(s) are affected by the change
2. Read the current wrapper implementation to see how data flows through the bridge
3. Verify the bridge types can handle the new data shapes:
   - `ReadableMap` supports nested maps (`ReadableMap.getMap(key)`) — nested objects pass through
   - `ReadableArray` supports mixed types and nested arrays/maps — nested arrays pass through
   - `NSDictionary`/`NSArray` in iOS handle nesting natively — nested objects pass through
   - Primitives (`String`, `int`, `boolean`) are always safe across the bridge
4. Check if any manual type conversion in the wrapper (e.g., `toHashMap()`, custom serialization) would strip or flatten the new data

**What to flag:**
- If the wrapper manually flattens or transforms data before passing to the native SDK, and the changelog change relies on that structure being preserved
- If the wrapper uses a restrictive type (e.g., `String` parameter) but the native SDK now accepts a richer type (e.g., `Map`)
- If new value types are supported that the bridge doesn't auto-convert (e.g., custom objects, dates, byte arrays)

**Output:** Add a "Type Compatibility" column to the implementation plan table with one of:
- `OK` — bridge types already support the change, no wrapper update needed
- `VERIFY` — likely compatible but should be tested; note what to verify
- `UPDATE` — wrapper needs changes to support the new types; describe what

### Step 5: Determine Wrapper Requirements

```
API already in React Native wrapper?
|-- YES -> Signature needs updating?
|  |-- YES -> Decision: UPDATE
|  +-- NO -> Type compatibility issue? (from Step 4b)
|     |-- YES -> Decision: UPDATE
|     +-- NO -> Decision: NO_ACTION
+-- NO -> Commonly used functionality?
   |-- YES -> Decision: NEW_IMPLEMENTATION
   |-- MAYBE -> Decision: DISCUSS
   +-- NO -> Decision: SKIP
```

### Step 6: Generate Implementation Plan

Output a single unified table showing ALL changes:

```markdown
## Wrapper Implementation Plan

### Summary
- Platform(s): [platforms]
- Version Range: [old] -> [new]
- Total Changes: [count by category]

### All Changes

| # | Category | API Name | Android Type | iOS Type | Parameters | Platforms | Type Compat | Decision | Notes |
|---|----------|----------|--------------|----------|------------|-----------|-------------|----------|-------|
| 1 | NEW_API | `methodName()` | `ReturnType` | `ReturnType` | params | versions | OK/VERIFY/UPDATE | Decision | notes |

**Legend**: NEW_API | BREAKING | DEPRECATED | BUG_FIX | INTERNAL

## Next Steps
Reply with:
- "Approved" - proceed with all implementations
- "Approved with changes" - specify modifications
- "Hold" - review further
```

Wait for user acknowledgment before proceeding with implementation.

## Implementation Files (React Native)

When implementing wrappers, update these files:

| Layer | File |
|-------|------|
| JavaScript | `src/index.js` |
| TypeScript Definitions | `src/index.d.ts` |
| TurboModule Spec | `src/NativeCleverTapModule.ts` |
| Android | `android/src/main/java/com/clevertap/react/CleverTapModuleImpl.java` |
| iOS | `ios/CleverTapReact/CleverTapReact.mm` |

## References

- [Type Mapping Tables](references/type-mapping.md) - Android <-> iOS <-> JavaScript type conversions
- [Detailed Examples](references/examples.md) - Complete analysis examples

## Success Criteria

- All changes between versions extracted and categorized
- All NEW_API/BREAKING changes have return types determined (verified from source OR inferred)
- APIs in changelog are NEVER skipped due to failed signature lookup - use inference instead
- Type compatibility verified for changes that affect data shapes or type handling (Step 4b)
- Single table output with separate Android/iOS type columns and Type Compat column
- User acknowledgment received before implementation
