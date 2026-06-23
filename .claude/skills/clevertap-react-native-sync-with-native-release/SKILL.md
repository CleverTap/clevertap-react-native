---
name: clevertap-react-native-sync-with-native-release
description: Orchestrator skill for syncing the React Native SDK with a new native SDK release (CleverTap-iOS-SDK or clevertap-android-sdk, or both). Drives the workflow end-to-end — gather inputs, run the native diff tool (`tools/diff_native_api.py` — covers public-API surface, build manifest, AND the matching changelog entry in a single invocation), triage each change, delegate per surfaceable API item to the existing `clevertap-react-native-add-public-method` skill, propagate any minSdk / targetSdk / deployment-target bumps to `clevertap-react-native.podspec` and `android/build.gradle`, surface dependency and permission changes to host integrators via `docs/install.md` and CHANGELOG, smoke-test on the Example app, and prompt the user about cache cleanup. Use whenever a new native SDK has been released and the RN SDK needs to catch up — covers core SDKs plus Android push templates / HMS and iOS push templates.
allowed-tools:
  - Read
  - Glob
  - Grep
  - Bash
  - Edit
  - Write
---

# Sync the RN SDK with a new native SDK release

## When to use

- A new `CleverTap-iOS-SDK` or `clevertap-android-sdk` (core, pushtemplates, or HMS) tag has been published.
- The host-side question is "we just shipped native X.Y.Z — update the RN SDK to support it."
- For one-off public-method additions where the native SDK hasn't changed, use [`clevertap-react-native-add-public-method`](../clevertap-react-native-add-public-method/SKILL.md) directly.
- If the native SDK has had a capability for a while and RN never surfaced it (NOT release-driven — e.g., multi-instance was always supported on native but isn't on RN), use [`clevertap-react-native-backfill-missing-coverage`](../clevertap-react-native-backfill-missing-coverage/SKILL.md). That sibling skill handles the JS API design step that this release-driven workflow assumes is already settled.

## Pipeline Overview

The full sync runs in 8 stages:

1. **Gather inputs** -- platforms (iOS / Android / both), each module's old → new version, driving reason (bug fix vs new feature), and whether a local clone of the native repo is available (the [[reference_native_sdk_repos]] memory has the standard local paths).
2. **Run the diff tool** -- one invocation per `(platform, module)` pair using `tools/diff_native_api.py`. The tool acquires sources (local clone → cache → GitHub tarball), then produces THREE blocks in a single pass:
   - **API diff** — added / removed / changed public methods on classes, protocols, listeners.
   - **Build manifest diff** — Android: SDK levels (minSdk/targetSdk/compileSdk), `gradle/libs.versions.toml` flat-key diff across `[versions]` / `[libraries]` / `[bundles]` / `[plugins]`, direct dependency declarations, `AndroidManifest.xml` `uses-permission` + `uses-feature`. iOS: podspec platform / deployment targets, `swift_version`, dependencies (including platform-prefixed `s.ios.dependency`).
   - **Changelog entry** — the matching `### Version X.Y.Z` (or `### [Version X.Y.Z](...)`) section from the per-module changelog, verbatim. Cross-validation only, not a diff.
   Outputs land at `~/.cache/clevertap-sdk-diff/<platform>-<module>-<old>-to-<new>/{diff.json,diff.md}`.
3. **Present the diff to the user** -- walk through three sections in order: (a) API changes (most actionable), (b) Build manifest changes (often have spillover into the RN SDK's own pins/manifest), (c) Changelog entry (the team's stated intent — sanity check that the diff matches).
4. **Triage each item** -- walk the decision tree in [refs/triage-decision-tree.md](refs/triage-decision-tree.md). Has separate decision branches for API additions/removals/changes AND for build-manifest changes (minSdk bump, dep added/removed/changed, permission added, etc.).
5. **Apply API changes** -- for each ADDED API marked "surface", delegate to the [`clevertap-react-native-add-public-method`](../clevertap-react-native-add-public-method/SKILL.md) recipe (TurboModule spec → JS → TS → Android Impl + both arch shims → iOS `RCT_EXPORT_METHOD` → Example app → docs).
6. **Propagate build-manifest changes** -- if native minSdk went up, bump RN SDK's `android/build.gradle` to match (or higher); if a new required permission appeared, declare it in `android/src/main/AndroidManifest.xml` or document in `docs/install.md` for host integration; if iOS deployment target moved, update `clevertap-react-native.podspec`'s `s.platform`; etc. See the triage tree.
7. **Bump version pins** -- edit `clevertap-react-native.podspec` for iOS and `android/build.gradle` for Android per [`clevertap-react-native-ios` workflow #3](../clevertap-react-native-ios/refs/workflows.md) and [`clevertap-react-native-android` workflow #3](../clevertap-react-native-android/refs/workflows.md).
8. **Update CHANGELOG + package.json** -- summarize what changed; if this is a versioned release also bump `package.json` (per the broad skill's release workflow). Call out any minSdk bumps or new permissions explicitly because they affect host apps.
9. **Smoke test + cleanup** -- run the Example app on both platforms, exercise affected methods, then prompt the user: delete the cache / keep it / keep + remember the path in memory.

## Architectural Rules

- **The diff tool is the source of truth, not release notes.** Release notes are a cross-check, not the input. Items in `docs/CTCORECHANGELOG.md` (or the iOS equivalent) but not in the diff are parser misses worth investigating. Items in the diff but not in the changelog are exactly what this skill exists to surface.
- **Delegate, don't reimplement.** For every "add this method" decision, hand off to `clevertap-react-native-add-public-method`. Don't write bridge code here.
- **Triage is mandatory.** Not every new native public method should surface in RN. Some are internal-but-public, some are platform-only, some are too niche to wrap. The decision tree in `refs/triage-decision-tree.md` exists so this isn't ad-hoc.
- **Tag formats live in one place** -- [refs/tag-conventions.md](refs/tag-conventions.md). Anything else in this skill that mentions tag formats is wrong; fix the reference instead.
- **Cache lives under `~/.cache/clevertap-sdk-*`** — never inside the working tree. See the [[feedback_untracked_tooling_files]] memory.

## Inputs the user should have on hand

1. **Platforms being bumped** — iOS, Android, or both.
2. **Old → new version per module.** For Android: `corev{semver}` (e.g., `corev8.1.0` → `corev8.2.0`), `ptv{semver}`, `hmsv{semver}`. For iOS: bare `{semver}` (e.g., `7.6.0` → `7.7.0`).
3. **Driving reason** — "bug fix only" (skip to version bump) vs "new feature(s) to surface" (full diff + triage flow) vs "deprecation/removal" (CHANGED + REMOVED focus).
4. **Local clone availability** — paths in [[reference_native_sdk_repos]]. If a local clone is at the requested tag, the tool reads from there (fastest).
5. **Linked tickets / native PRs** — useful for triage when deciding whether an ADDED method is host-facing or internal-helper.
6. **The CleverTap native CHANGELOG entry for the release** — paste-able for cross-validation against the diff.

If any of these are missing, use AskUserQuestion before invoking the diff tool. Don't guess versions.

## Workflow

```
1. Confirm inputs (AskUserQuestion if any unknown)
2. For each (platform, module) in scope:
       ./tools/diff_native_api.py \
           --platform <p> --module <m> \
           --old-version <old> --new-version <new> \
           [--local-path <path from [[reference_native_sdk_repos]]>]
3. Read the resulting diff.md to the user (or summarize if large)
4. For each ADDED:
       Walk refs/triage-decision-tree.md
       If "surface":
           → invoke clevertap-react-native-add-public-method recipe
       If "internal" / "skip":
           → note in CHANGELOG comment; move on
5. For each REMOVED:
       Walk decision tree:
           → deprecate in src/index.js with @deprecated JSDoc, or
           → remove if the underlying SDK already removed it (breaking)
6. For each CHANGED:
       Read the old/new signatures from diff.md
       → adapt the bridge implementation to match
       → If breaking, note in CHANGELOG under "Breaking changes"
7. Bump version pins:
       iOS:     clevertap-react-native.podspec  →  's.dependency 'CleverTap-iOS-SDK', '<new>''
       Android: android/build.gradle            →  "api 'com.clevertap.android:clevertap-android-sdk:<new>'"
8. Run npm run lint
9. cd Example && yarn install && (cd ios && pod install) && yarn ios
        (separately) yarn android
   Exercise the new/changed methods through Example/app/constants.js actions
10. Update CHANGELOG.md (move "Unreleased" entries into the new versioned section)
11. AskUserQuestion: cleanup?
        [Delete now] / [Keep in cache] / [Keep + record in memory]
```

## Anti-patterns

- **Running the skill without the diff.** Release notes lie. The diff is the input. If the tool errors, fix the tool — don't fall back to release-notes-only triage.
- **Surfacing every ADDED method.** Some are clearly internal helpers, deprecated overloads, or platform-specific niche APIs. The decision tree exists.
- **Skipping the cross-platform consistency check.** If a method was added to Android core but not to iOS core in the same release window, that's worth flagging — either iOS will follow soon, or the API is Android-only. Both cases need explicit handling in the JS layer (`Platform.OS` guard or `isAvailable()`).
- **Deleting the cache mid-workflow.** The diff.json is the contract this skill consumes. Cleanup is only at the end.
- **Bumping the version pin before triage is complete.** The version bump is the LAST mechanical change; if you bump first and discover an ADDED method requires careful surfacing, you've already advertised support that doesn't exist yet.
- **Bumping the native SDK pin without checking minSdk follow-through.** If the native release moved minSdk (e.g., 21 → 23) and the RN SDK's `android/build.gradle` is still on the lower bound, host apps with broader device targets will fail to compile against the new RN SDK. The build-manifest diff surfaces this; act on it.
- **Treating a new `<uses-permission>` as the native team's problem.** If the native SDK declares a new required permission (e.g., POST_NOTIFICATIONS), host apps must merge it. Either declare it in the RN SDK's own `android/src/main/AndroidManifest.xml` (if always-required) or document the manual step in `docs/install.md` (if optional).
- **Ignoring deprecation notes in the changelog entry panel.** The build-manifest diff catches *removed* deps but not *deprecated* ones. The changelog cross-validation panel is where deprecations land (e.g., ExoPlayer → Media3). Read the entry; if a deprecation is announced, add a CHANGELOG note and plan migration before the next major.
- **Forgetting to update the iOS `Example/ios/Podfile.lock`.** `pod install` regenerates this; commit it.

## Auto-apply mode (used by CI)

When invoked from GitHub Actions via the `clevertap-wrapper-sync` automation (see `piyush-kukadiya/clevertap-wrapper-tooling`), the skill runs without a human in the loop. The orchestration prompt at `clevertap-wrapper-tooling/prompts/sync-orchestrator.md` activates this mode. Behavior changes:

- **No `AskUserQuestion` calls.** Use the decision trees' defaults. Where a tree says "ask the user", treat that as DEFER + continue.
- **Never destructive on ambiguity.** Items whose triage isn't clear-cut get added to the `deferred` list with a rationale, not surfaced.
- **Structured JSON output is required.** At the end of the run, write a structured log to stdout containing every triage decision:

```json
{
  "platform": "android",
  "module": "core",
  "old_version": "8.1.0",
  "new_version": "8.2.0",
  "surfaced": [{"name": "unmute", "rationale": "explicit new public API in changelog", "files_touched": [...]}],
  "skipped": [{"name": "...", "rationale": "internal helper, @RestrictTo"}],
  "deferred": [{"name": "...", "rationale": "needs JS API design — see backfill-missing-coverage skill"}],
  "build_propagated": [{"change": "minSdk 21→23", "files": ["android/build.gradle"]}],
  "changelog_entry": "- [Android] Bump clevertap-android-sdk to 8.2.0 — adds `unmute`, minSdk 21→23",
  "tokens_used": 123456,
  "cost_usd_estimate": 1.23
}
```

The CI wrapper consumes this log to (a) feed the PR description generator and (b) compute cost for the soft-cap check.

### CHANGELOG-tagging convention (auto-apply mode)

In auto-apply runs, CHANGELOG additions are prefixed with the platform tag so two platforms can be synced into the same branch without textual conflicts:

```markdown
## [Unreleased]
- [Android] Bump clevertap-android-sdk to 8.2.0 — adds `unmute`, minSdk 21→23
- [iOS] Bump CleverTap-iOS-SDK to 7.7.0 — adds PiP support methods
```

This is enforced in auto-apply mode. In interactive mode, follow whatever the existing CHANGELOG conventions look like.

## Testing

The skill is verified by running a real diff:

```bash
./tools/diff_native_api.py --platform android --module core \
    --old-version 8.0.0 --new-version 8.1.0 \
    --local-path /Users/piyush.kukadiya/codebases/clevertap/clevertap-android-sdk
```

Then cross-reference the resulting `diff.md` against the corresponding section in `clevertap-android-sdk/docs/CTCORECHANGELOG.md`. Items match → tool is healthy. Items present in changelog but not in diff → regex misses; revisit `SOURCE_GLOBS` and parsing rules. Items present in diff but not in changelog → potential value-add (or noise from a refactor).

## Reference Files

- [refs/triage-decision-tree.md](refs/triage-decision-tree.md) — Decision tree for each ADDED/REMOVED/CHANGED row
- [refs/tag-conventions.md](refs/tag-conventions.md) — Per-module tag formats, GitHub tarball URL patterns, local clone defaults
- `tools/diff_native_api.py` — The diff tool (~stdlib only, ~400 lines, runnable as `python3 ./tools/diff_native_api.py --help`)

## Related skills

- [`clevertap-react-native`](../clevertap-react-native/SKILL.md) — broad RN SDK overview; consult for the JS layer architecture
- [`clevertap-react-native-add-public-method`](../clevertap-react-native-add-public-method/SKILL.md) — the recipe this skill delegates to for each surfaceable item
- [`clevertap-react-native-android`](../clevertap-react-native-android/SKILL.md) — Android bridge deep dive; version bump in workflow #3
- [`clevertap-react-native-ios`](../clevertap-react-native-ios/SKILL.md) — iOS bridge deep dive; version bump in workflow #3
