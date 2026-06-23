# Cross-Platform Workflows

Step-by-step procedures for tasks that span the whole RN SDK. Platform-specific tasks live in the respective platform skill's `refs/workflows.md`.

## 1. Debug a feature end-to-end

Use this when a host app reports "X doesn't work" and you need to localize the failure.

**Step 1 — Confirm the JS surface fires.** Open `src/index.js`, find the method. Add a quick `console.log` in the wrapper (or have the host add one) to confirm the call is reaching the JS bridge.

**Step 2 — Confirm the native module method is registered.** On Android, search `android/src/oldarch/CleverTapModule.kt` (annotated with `@ReactMethod`) AND `android/src/newarch/CleverTapModule.kt` (using `override`); the underlying logic lives in `android/src/main/java/com/clevertap/react/CleverTapModuleImpl.java` as a plain public method. On iOS, search `ios/CleverTapReact/CleverTapReact.mm` for `RCT_EXPORT_METHOD(<name>`.

**Step 3 — Confirm both arch shims (Android) include the method.** Open both `android/src/oldarch/.../CleverTapModule.kt` and `android/src/newarch/.../CleverTapModule.kt` and verify the method exists in both. Most "doesn't work on Android" bug reports trace to a missing shim entry.

**Step 4 — Verify type marshalling.** Step through the arg conversion in `CleverTapUtils` (Android) or the `NSDictionary` literals (iOS). Common issues: nested `null` values, `NaN`/`Infinity` in numeric fields, date serialization.

**Step 5 — Verify the underlying SDK call is reached.** Run the Example app and `adb logcat -s CleverTap CleverTapReact` (Android) or watch the Xcode console (iOS). Look for CleverTap-SDK-internal logs that indicate the call landed.

**Step 6 — For events: verify all three locations agree on the name.** The event string in `CleverTapEvent.kt` (Android), `supportedEvents` (iOS), and the exported constant in `src/index.js` MUST be byte-identical. A typo in any one causes "the listener never fires."

**Step 7 — For events: verify the JS side attached early enough.** On iOS, events that fire before `addListener` are queued in `CleverTapReactPendingEvent`. On Android, only events marked `isBufferable=true` in `CleverTapEvent.kt` are buffered. If a non-bufferable Android event is being missed, the JS attach is too late.

**Step 8 — Cross-reference payload shape.** Open `docs/callbackPayloadFormat.md` and compare against what the listener is actually receiving.

## 2. Bump underlying native SDK versions

Coordinated change. The two native SDK versions are independent — bump them together or individually.

> **For non-trivial bumps** (anything beyond bug-fix-only versions), use the orchestrator skill [`clevertap-react-native-sync-with-native-release`](../../clevertap-react-native-sync-with-native-release/SKILL.md). It runs a public-API diff, walks each ADDED / REMOVED / CHANGED item through the triage decision tree, and delegates per-item to the add-public-method recipe. The steps below are the mechanical version-pin update only.

**Step 1 — Update iOS pinned version.** Edit `clevertap-react-native.podspec`:

```ruby
s.dependency 'CleverTap-iOS-SDK', 'X.Y.Z'
```

**Step 2 — Update Android pinned version.** Edit `android/build.gradle`:

```gradle
api 'com.clevertap.android:clevertap-android-sdk:X.Y.Z'
```

**Step 3 — Re-resolve in the Example app.**

```bash
cd Example
yarn install
cd ios && pod update CleverTap-iOS-SDK && cd ..
yarn ios      # smoke-test iOS
yarn android  # smoke-test Android
```

**Step 4 — Check for API breakage.** Run lint (`npm run lint`) and exercise key methods in the Example app: `registerForPush`, `onUserLogin`, `recordEvent`, `showInbox`, push receive. If the native SDK changed a public type, the bridge file using it will fail to compile.

**Step 5 — Update docs and changelog.** `docs/install.md` if a minimum version of the underlying SDK is documented. `CHANGELOG.md` with a clear note: "Updated CleverTap-iOS-SDK to X.Y.Z" and "Updated clevertap-android-sdk to X.Y.Z".

## 3. Demo a new feature in the Example app

**Step 1 — Add an action key.** Edit `Example/app/constants.js` and add a new entry to the `Actions` object. Follow the existing naming: `UPPER_SNAKE_CASE`.

**Step 2 — Write the handler.** In `Example/app/app-utils.js`, export a function named after the feature (e.g., `do_newFeature`) that calls `CleverTap.<newMethod>(...)` and shows a toast.

**Step 3 — Wire the action into the UI.** In `Example/app/App.js`, map the action key to the handler function so it appears in the action list and triggers the handler on tap.

**Step 4 — (Optional) Add a listener demo.** If the feature emits an event, register the listener in `App.js`'s mount effect and log the payload to confirm wiring.

## 4. Run lint and fix issues

```bash
npm run lint           # report
npm run lint-fix       # auto-fix safe issues
```

ESLint scope: `index.js` and `*/**.js` patterns (per `package.json`). Not all JS in `Example/` is linted by the SDK lint task — its own lint is host-app-level.

## 5. Release prep

When cutting a new version of this SDK:

**Step 1 — Bump version in `package.json`.** Use SemVer. Major if breaking change at the JS API, minor for new features, patch for bug fixes.

**Step 2 — Update `clevertap-react-native.podspec` version.** It mirrors `package.json` `version`.

**Step 3 — Update `CHANGELOG.md`.** Move "Unreleased" items into a versioned section with today's date. Cite ticket IDs where applicable.

**Step 4 — Update `docs/install.md`** if installation steps or minimums changed.

**Step 5 — Smoke-test the Example app on both platforms** (cold launch + a handful of actions covering push, inbox, events, in-app, variables).

**Step 6 — Tag and publish.** `git tag vX.Y.Z` → push → CI handles `npm publish` and the iOS pod trunk push.
