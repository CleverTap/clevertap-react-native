# Command: Update Native SDKs

**Purpose**: Update CleverTap React Native SDK to use latest native Android and iOS SDK versions.
**Usage**: Ask Claude to "run update-sdk command" or "update to latest native SDKs"

---

## Architecture

This command orchestrates 5 phases via Task sub-agents. Each agent reads its own skill files in an isolated context, keeping the main context lean. Phases 2 and 5 each run two agents in parallel for efficiency.

**User interaction points**: Version confirmation (after Phase 1), Plan approval (after Phase 2)

---

## Phase 1: Gather Versions

Spawn a Task sub-agent (`version-gather`):
- Input: (none — reads files directly)
- Return: `VERSION_DATA` containing old/new versions for React Native, Android, iOS

**After agent returns**: Present version data to user for confirmation.
- If user confirms → proceed with confirmed versions
- If user specifies different versions → use those instead
- If user cancels → STOP

---

## Phase 2: Update Versions + Analyze Changes (parallel)

Spawn **both** Task sub-agents so that they run concurrently:

**Agent A** — `version-update`:
- Input: confirmed `NEW_RN_VERSION`, `NEW_ANDROID_VERSION`, `NEW_IOS_VERSION`
- Return: `UPDATE_RESULT` with list of files updated

**Agent B** — `changelog-analyze`:
- Input: `OLD_ANDROID_VERSION`, `NEW_ANDROID_VERSION`, `OLD_IOS_VERSION`, `NEW_IOS_VERSION`
- Return: `IMPLEMENTATION_PLAN` as markdown table

**After both agents return**:
- If `version-update` failed → show error details and STOP
- Present `IMPLEMENTATION_PLAN` to user for approval
- If user approves → proceed
- If user requests changes → modify plan accordingly
- If user holds → STOP for further review

---

## Phase 3: Implement API Wrappers

**MANDATORY**: Implement ALL items marked `NEW_IMPLEMENTATION` or `UPDATE`. Do NOT skip without explicit user approval.

Spawn a Task sub-agent (`api-implement`):
- Input: approved `IMPLEMENTATION_PLAN`
- Return: `IMPLEMENTATION_RESULT` with list of APIs implemented

---

## Phase 4: Update Example App

Spawn a Task sub-agent (`example-app-update`):
- Input: `APIS_IMPLEMENTED`, `IMPLEMENTATION_PLAN`
- Return: `EXAMPLE_RESULT` with list of examples added

---

## Phase 5: Generate Changelog + Build Validation (parallel)

Spawn **both** Task sub-agents so that they run concurrently:

**Agent A** — `changelog-generate`:
- Input: `NEW_RN_VERSION`, `NEW_ANDROID_VERSION`, `NEW_IOS_VERSION`, `IMPLEMENTATION_PLAN`, `APIS_IMPLEMENTED`
- Return: `CHANGELOG_ENTRY` as formatted markdown text

**Agent B** — `build-validate`:
- Input: (none — operates on working directory)
- Return: `BUILD_RESULT` with pass/fail status

**After both agents return**:
- If `build-validate` failed → show full error output, analyze if related to version changes, ask user how to proceed

---

## Success Criteria

Task complete when:
- All 8 version locations updated consistently (including Example/android/app/build.gradle and docs/install.md)
- Native SDK dependencies updated in library, Example app, and install docs
- All new APIs analyzed and implementation plan approved
- Necessary wrappers implemented (or user confirmed not needed)
- Example app updated with new API demonstrations
- Changelog entry added with correct format and validated links
- All builds pass

## Error Handling

- **Network errors**: Agents retry up to 3 times before reporting failure
- **Version parse errors**: Report exact file and pattern, ask user to verify
- **Build failures**: Report full error output, analyze root cause, ask user before proceeding
- **Agent failures**: If any agent returns failure, present the error and STOP — do not proceed to the next phase
- **Parallel phase failures**: In Phases 2 and 5, if either agent fails, report the failure even if the other succeeded
