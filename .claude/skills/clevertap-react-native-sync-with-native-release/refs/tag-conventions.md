# Tag conventions and source URLs

Single source of truth for native SDK repo URLs, tag formats, and GitHub tarball URL patterns. The diff tool (`tools/diff_native_api.py`) hard-codes the same values; this document exists so engineers don't have to re-derive them.

## Android — `clevertap-android-sdk`

**Repo:** `https://github.com/CleverTap/clevertap-android-sdk`

The Android SDK is a multi-module monorepo. Each module has its own tag prefix and ships independently. The RN SDK's `android/build.gradle` pins `clevertap-android-sdk` (core); host apps separately depend on push templates and HMS via their own Gradle configuration.

| Module | Tag format | Example | Source path in repo |
|---|---|---|---|
| core | `corev{semver}` | `corev8.1.0` | `clevertap-core/` |
| pushtemplates | `ptv{semver}` | `ptv1.2.3` | `clevertap-pushtemplates/` |
| hms | `hmsv{semver}` | `hmsv1.5.1` | `clevertap-hms/` |
| geofence | `gfv{semver}` | `gfv1.4.0` | `clevertap-geofence/` *(not in current RN scope)* |
| vault | `vlv{semver}` | — | `clevertap-vault/` *(not in current RN scope)* |
| xps | `xpsv{semver}_ptv{semver}` | `xpsv1.3.0_ptv1.0.1` | composite *(not in current RN scope)* |

**Tarball URL template:**

```
https://github.com/CleverTap/clevertap-android-sdk/archive/refs/tags/{tag}.tar.gz
```

Example concrete URL for `corev8.1.0`:

```
https://github.com/CleverTap/clevertap-android-sdk/archive/refs/tags/corev8.1.0.tar.gz
```

**Local clone default path:**

```
/Users/piyush.kukadiya/codebases/clevertap/clevertap-android-sdk
```

(See [[reference_native_sdk_repos]] memory.)

## iOS — `clevertap-ios-sdk`

**Repo:** `https://github.com/CleverTap/clevertap-ios-sdk`

The iOS SDK does NOT use a tag prefix — tags are bare SemVer. The RN SDK's `clevertap-react-native.podspec` pins `CleverTap-iOS-SDK` to the matching version.

| Module | Tag format | Example | Source path in repo |
|---|---|---|---|
| core | `{semver}` | `7.6.0` | `CleverTapSDK/` |
| pushtemplates | TBD on first real run — confirm whether iOS push templates live in `clevertap-ios-sdk` or a separate repo / pod (`CTNotificationService`). | — | TBD |

**Tarball URL template:**

```
https://github.com/CleverTap/clevertap-ios-sdk/archive/refs/tags/{tag}.tar.gz
```

Example concrete URL for `7.6.0`:

```
https://github.com/CleverTap/clevertap-ios-sdk/archive/refs/tags/7.6.0.tar.gz
```

**Local clone default path:**

Not present locally as of plan authoring. The diff tool will fetch the tarball on first run. If you do clone it locally, please add the path to the [[reference_native_sdk_repos]] memory so the tool can find it.

## Source files read by the diff tool

In addition to the public-surface globs (covered by `SOURCE_GLOBS` in the tool), the build-manifest and changelog blocks read these per-module files:

| Platform / module | Build manifest files | Changelog file |
|---|---|---|
| Android / core | `gradle/libs.versions.toml`, `clevertap-core/build.gradle`, `clevertap-core/src/main/AndroidManifest.xml` | `docs/CTCORECHANGELOG.md` |
| Android / pushtemplates | `gradle/libs.versions.toml`, `clevertap-pushtemplates/build.gradle`, `clevertap-pushtemplates/src/main/AndroidManifest.xml` | `docs/CTPUSHTEMPLATESCHANGELOG.md` |
| Android / hms | `gradle/libs.versions.toml`, `clevertap-hms/build.gradle`, `clevertap-hms/src/main/AndroidManifest.xml` | `docs/CTHMSCHANGELOG.md` |
| iOS / core | `CleverTap-iOS-SDK.podspec` | `CHANGELOG.md` |
| iOS / pushtemplates | `CTNotificationService.podspec` (TBD on first real run) | `CHANGELOG.md` |

**Changelog header conventions** the parser supports (one regex covers both):

- Android: `### Version 8.1.0 (April 17, 2026)`
- iOS: `### [Version 7.6.0](https://github.com/.../tag/7.6.0) (April 17, 2026)`

If a future native SDK changes the changelog header format, update the regex in `extract_changelog_entry` and add an example here.

## Cache locations

These match what the diff tool uses by default. Do not relocate without updating both the tool defaults AND this document.

| Purpose | Path |
|---|---|
| Extracted source trees (per repo + tag) | `~/.cache/clevertap-sdk-versions/<repo-slug>-<tag>/` |
| Diff outputs (`diff.json` + `diff.md`) | `~/.cache/clevertap-sdk-diff/<platform>-<module>-<old>-to-<new>/` |

## Cleanup policy

At the end of the sync workflow the skill prompts:

- **Delete now** — wipe both cache locations for this run.
- **Keep in cache** — leave everything; subsequent runs reuse the extracted sources.
- **Keep + remember** — leave everything AND record the cache path in [[reference_native_sdk_repos]] so future sessions skip the "is this cached?" check.

Default suggestion: **Keep in cache** if the combined cache size is under 200 MB. Otherwise prompt for explicit choice.

## Adding a new module

If a new native module enters the RN-supported scope, update three places in this order:

1. `tools/diff_native_api.py` — add an entry to the `REPOS` dict and the `SOURCE_GLOBS` dict, and accept the module name in the `--module` argparse choices.
2. This file — add a row to the matching platform table and an example tarball URL.
3. [[reference_native_sdk_repos]] memory — include the new module in the per-module path summary.
