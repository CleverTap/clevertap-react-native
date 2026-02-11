---
name: changelog-generate
description: "Generate a properly formatted changelog entry for CleverTap React Native SDK releases. Use after API implementation to document changes in CHANGELOG.md."
tools: Glob, Grep, Read, Edit, Write, WebFetch
model: sonnet
color: yellow
skills: changelog-generation
---

# Agent: Changelog Generate

**Purpose**: Generate a properly formatted changelog entry for the React Native SDK release.

## Input
| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `NEW_RN_VERSION` | Yes | New React Native plugin version | `3.9.0` |
| `NEW_ANDROID_VERSION` | Yes | New Android SDK version | `7.8.0` |
| `NEW_IOS_VERSION` | Yes | New iOS SDK version | `7.5.0` |
| `IMPLEMENTATION_PLAN` | Yes | The implementation plan table with all changes | Markdown table |
| `APIS_IMPLEMENTED` | Yes | List of APIs that were newly implemented or updated | `methodName1, methodName2` |

## Process

> The **changelog-generation** skill is auto-loaded into this agent's context. Follow its strict formatting rules, entry template, and anchor format exactly — the format is parsed by automation tools.

### Step 1: Fetch Changelog Dates

Fetch both native SDK changelogs (URLs in the skill's examples) to extract release dates for version anchor generation.

### Step 2: Compose and Insert Changelog Entry

Follow the skill's entry template, anchor format rules, and insertion process.

## Output Format

```
CHANGELOG_RESULT=success/failure
ENTRY_PREVIEW=[full changelog entry text]
SUMMARY=Added changelog entry for version {NEW_RN_VERSION} with N new APIs, M updates
```

## Success Criteria
- [ ] Entry added at TOP of CHANGELOG.md
- [ ] Formatting follows the auto-loaded skill's rules (date format, platform tags, anchor format, active voice)
- [ ] Native SDK links include correct version anchors
- [ ] All new/updated APIs listed in "API changes" section

## Error Handling
- If CHANGELOG.md cannot be read, STOP and report error
- If anchor date cannot be determined from native changelogs, use "TBD" and flag for manual review
- If the entry format doesn't match the skill's template, review the skill and try again
