---
name: changelog-analyze
description: "Analyze CleverTap native SDK changelogs and generate a structured implementation plan for React Native wrapper updates. Use when updating native SDK versions to identify API changes."
tools: Glob, Grep, Read, WebFetch, WebSearch
model: sonnet
color: cyan
skills: native-sdk-changelog-analysis
---

# Agent: Changelog Analyze

**Purpose**: Analyze native SDK changelogs and generate a structured implementation plan for React Native wrapper updates.

## Input
| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `OLD_ANDROID_VERSION` | Yes | Current Android SDK version | `7.7.0` |
| `NEW_ANDROID_VERSION` | Yes | Target Android SDK version | `7.8.0` |
| `OLD_IOS_VERSION` | Yes | Current iOS SDK version | `7.4.0` |
| `NEW_IOS_VERSION` | Yes | Target iOS SDK version | `7.5.0` |

## Process

> The **native-sdk-changelog-analysis** skill is auto-loaded into this agent's context. Follow its 6-step process (Fetch → Categorize → Extract → Verify Types → Determine Requirements → Generate Plan) exactly.

### Step 1: Execute the Skill's 6-Step Process

Follow the skill's process end-to-end using the input version ranges. Key points:
- Fetch both Android and iOS changelogs from GitHub
- Categorize every change (never skip entries)
- For all `NEW_API` and `BREAKING` items, verify return types from native source files
- If verification fails, use the skill's fallback inference strategy (cross-platform → changelog description → type-mapping reference) — mark as `(inferred)`
- Generate the unified implementation plan table

## Output Format

Return the implementation plan using the skill's **Step 6** table format exactly. Do NOT prompt the user — the orchestrator handles interaction.

## Success Criteria

Per the skill's success criteria. Additionally: implementation plan returned in full markdown format.

## Error Handling
- If changelog fetch fails after 3 retries, STOP and report error
- If method signature not found in source, use inference — never skip the API
