---
name: clevertap-react-native
description: Orients work in the CleverTap React Native SDK — a thin bridge between a JS/TS public API and the underlying native iOS (CleverTap-iOS-SDK 7.6.0) and Android (clevertap-android-sdk 8.1.0) SDKs. Covers the three-layer architecture (JS → TurboModule spec → native module), feature map across ~100 public methods, cross-platform conventions, dev loop, and pointers to platform-specific deep dives. Use as the entry point for any work in this repo — adding/changing public APIs, debugging a feature end-to-end, or bumping native SDK versions.
allowed-tools:
  - Read
  - Glob
  - Grep
  - Bash
---

# CleverTap React Native SDK

## Pipeline Overview

A public API call traverses these stages:

1. **JS surface** -- the host app calls `CleverTap.someMethod(...)` from `src/index.js`; the JS wrapper validates arguments minimally and delegates to the native module reference (`CleverTapReact`) backed by the TurboModule spec in `src/NativeCleverTapModule.ts`
2. **Bridge dispatch** -- React Native routes the call to the platform native module: `CleverTapReact.mm` on iOS or `CleverTapModule.kt` (oldarch or newarch) on Android; the Android shim immediately delegates to `CleverTapModuleImpl.java`
3. **Native execution** -- the native module marshals JS arguments into native types and calls the underlying CleverTap SDK (`[CleverTap sharedInstance]` on iOS, `CleverTapAPI` on Android); the response is converted back into a `Promise`/callback payload or fired as an event
4. **Event callbacks** -- native SDK callbacks reach the bridge via `CleverTapListenerProxy` (Android) or `CleverTapReactManager` / presenter classes (iOS); events are dispatched through `CleverTapEventEmitter` (Android) or `RCTEventEmitter` (iOS); the JS layer subscribes via `CleverTap.addListener(eventName, handler)`

## Architectural Rules

- **Thin-wrapper philosophy** -- the RN SDK contains no business logic; it only marshals arguments, dispatches to the native SDK, and marshals responses back. New behavior belongs in the native SDK, not here.
- **Single TurboModule spec, two architectures** -- `src/NativeCleverTapModule.ts` is the contract; Android exposes it via `android/src/oldarch/CleverTapModule.kt` (annotates each method with `@ReactMethod`) and `android/src/newarch/CleverTapModule.kt` (uses `override` against the codegen spec); both shims delegate to the shared `CleverTapModuleImpl.java` (a plain helper class — no `@ReactMethod` annotations there). iOS uses a single `CleverTapReact.mm` with `RCT_NEW_ARCH_ENABLED` macros for the new arch.
- **Event names are the source of truth** -- the strings in `Constants.kt` / `CleverTapEvent.kt` (Android) and `supportedEvents` (iOS) MUST match the constants re-exported from `src/index.js`. Drift breaks listener subscription silently.
- **Type marshalling lives in platform utils** -- `android/src/main/java/com/clevertap/react/CleverTapUtils.java` handles WritableMap/Array conversion on Android; iOS uses inline `RCTConvert` / `NSDictionary` literals.
- **Pending-event queue is iOS-only** -- `CleverTapReactPendingEvent` buffers events fired before JS attaches a listener. Android relies on a "bufferable" flag per event in `CleverTapEvent.kt`.
- **Underlying SDK versions are pinned** -- iOS at `CleverTap-iOS-SDK 7.6.0` (`clevertap-react-native.podspec`), Android at `clevertap-android-sdk 8.1.0` (`android/build.gradle`). Bumping requires coordinated changes — see the workflows ref.
- **Both architectures must compile** -- when adding a public method, wire it through `oldarch` AND `newarch` on Android. Forgetting one is the most common bridge-breakage.

## Source Tree

```
src/                                JS public API
  index.js                          CleverTap object with all methods + event constants
  index.d.ts                        TypeScript declarations for every public method
  NativeCleverTapModule.ts          TurboModule spec (contract for native modules)

ios/CleverTapReact/                 iOS bridge (Objective-C++)
  CleverTapReact.{h,mm}             RCT_EXPORT_METHOD declarations + event dispatch
  CleverTapReactManager.{h,mm}      AppDelegate integration helper
  CleverTapReactTemplatePresenter   Custom template UI presenter
  CleverTapReactCustomTemplates     Custom template lifecycle (present/dismiss/action)
  CleverTapReactAppFunctionPresenter App function presenter
  CleverTapReactPendingEvent        Pending-event queue for pre-JS-listener events

android/src/main/java/com/clevertap/react/   Android bridge (Kotlin + Java)
  CleverTapModuleImpl.java          ALL bridge methods (core implementation)
  CleverTapPackage.kt               RN package registration
  CleverTapApplication.kt           Convenience Application class for host apps
  CleverTapEvent.kt                 Event enum (name + bufferable flag)
  CleverTapEventEmitter.kt          Event dispatch with buffering
  CleverTapListenerProxy.kt         Native-SDK-callback → RN-event proxy
  CleverTapUtils.java               WritableMap/Array conversion utilities
  CleverTapRnAPI.kt                 Public init API for host apps
  Constants.kt                      REACT_MODULE_NAME, FCM push type string

android/src/oldarch/CleverTapModule.kt    Old-arch shim. extends ReactContextBaseJavaModule.
                                          Each method @ReactMethod, forwards to Impl.
android/src/newarch/CleverTapModule.kt    New-arch shim. extends NativeCleverTapModuleSpec
                                          (codegen). Each method `override`, forwards to Impl.

Example/                            Demo app exercising every public API
  app/App.js                        UI driver
  app/constants.js                  ~105 Action keys (one per feature demo)
  app/app-utils.js                  Per-action CleverTap calls
  ios/, android/                    Native projects

docs/                               Integration & feature reference
  install.md, integration.md, usage.md
  callbackPayloadFormat.md          Authoritative shape of every event callback payload
  CustomCodeTemplates.md, Variables.md, pushprimer.md, iospushtemplates.md
```

See [refs/file-map.md](refs/file-map.md) for a one-line description of every file.

## Feature Map

| Feature | JS (`src/index.js`) | iOS (`CleverTapReact.mm`) | Android (`CleverTapModuleImpl.java`) |
|---|---|---|---|
| Push registration | `registerForPush`, `setFCMPushToken`, `pushRegistrationToken`, `promptForPushPermission`, `promptPushPrimer`, `isPushPermissionGranted`, `createNotificationChannel*`, `createNotification` | same names via `RCT_EXPORT_METHOD` | same names — plain methods on Impl + `@ReactMethod`/`override` forwarders on the two arch shims |
| Events & sessions | `recordEvent`, `recordChargedEvent`, `recordScreenView`, `getUserEventLog*`, `session*` | same | same |
| Profile & identity | `onUserLogin`, `profileSet`, `profile*MultiValue*`, `profileIncrement/Decrement*`, `getCleverTapID` | same | same |
| App Inbox | `initializeInbox`, `showInbox`, `dismissInbox`, `get*InboxMessage*`, `mark/deleteInbox*`, `pushInbox*` | same | same |
| In-App | `suspend/discard/resumeInAppNotifications`, `fetchInApps`, `clearInAppResources` | same | same |
| Custom Templates | `syncCustomTemplates*`, `customTemplate*Arg`, `customTemplateRunAction`, `customTemplateSet{Dismissed,Presented}` | same | same |
| Variables (PE) | `syncVariables*`, `fetchVariables`, `defineVariables`, `defineFileVariable`, `getVariable*`, `onValueChanged`, `onVariablesChanged*` | same | same |
| Display Units | `getAllDisplayUnits`, `getDisplayUnitForId`, `pushDisplayUnit*Event*` | same | same |
| Feature Flags (deprecated) | `getFeatureFlag` | same | same |
| Product Config (deprecated) | `setDefaultsMap`, `fetch`, `activate`, `getProductConfig*`, `getNumber`, `getLastFetchTimeStampInMillis` | same | same |
| Config & lifecycle | `setDebugLevel`, `setLocale`, `setLocation`, `setOffline`, `setOptOut`, `setInstanceWithAccountId`, `enable/disablePersonalization`, `enableDeviceNetworkInfoReporting`, `unmute`, `pushInstallReferrer`, `getInitialUrl` | same | same |

Full row-per-method table in [refs/feature-map.md](refs/feature-map.md).

## Public Event Constants

Re-exported from `src/index.js` and matched on both platforms:

`CleverTapProfileDidInitialize`, `CleverTapProfileSync`, `CleverTapInAppNotificationShowed`, `CleverTapInAppNotificationDismissed`, `CleverTapInAppNotificationButtonTapped`, `CleverTapInboxDidInitialize`, `CleverTapInboxMessagesDidUpdate`, `CleverTapInboxMessageTapped`, `CleverTapInboxMessageButtonTapped`, `CleverTapDisplayUnitsLoaded`, `CleverTapFeatureFlagsDidUpdate`, `CleverTapProductConfigDidInitialize`, `CleverTapProductConfigDidFetch`, `CleverTapProductConfigDidActivate`, `CleverTapPushNotificationClicked`, `CleverTapPushPermissionResponseReceived`, `CleverTapOnVariablesChanged`, `CleverTapOnOneTimeVariablesChanged`, `CleverTapOnValueChanged`, `CleverTapOnVariablesChangedAndNoDownloadsPending`, `CleverTapOnceVariablesChangedAndNoDownloadsPending`, `CleverTapOnFileValueChanged`, `CleverTapCustomTemplatePresent`, `CleverTapCustomFunctionPresent`, `CleverTapCustomTemplateClose`.

Authoritative payload shapes for each: `docs/callbackPayloadFormat.md`.

## Workflows

Step-by-step procedures live in [refs/workflows.md](refs/workflows.md):

1. **Debug a feature end-to-end** -- trace JS call → bridge → native SDK; check event listener registration; verify payload shape against `docs/callbackPayloadFormat.md`
2. **Bump native SDK versions** -- coordinated change across `clevertap-react-native.podspec` (iOS), `android/build.gradle` (Android), Example app pods/gradle, CHANGELOG.md, version field in `package.json`
3. **Update the Example app to demo a feature** -- add an Action key in `Example/app/constants.js`, write the handler in `Example/app/app-utils.js`, wire into `Example/app/App.js`
4. **Run lint** -- `npm run lint` (ESLint over `index.js` and JS sources); `npm run lint-fix` for auto-fixable issues
5. **Release prep** -- update `package.json` version, `CHANGELOG.md`, native pod/gradle versions, regenerate docs

**Adding a new public method** is a multi-file, ordered procedure with anti-patterns — it has its own dedicated skill: [`clevertap-react-native-add-public-method`](../clevertap-react-native-add-public-method/SKILL.md).

**Syncing with a new native SDK release** (iOS / Android version bump, possibly with new public APIs to surface) — use the orchestrator skill: [`clevertap-react-native-sync-with-native-release`](../clevertap-react-native-sync-with-native-release/SKILL.md). It runs a public-API diff between native versions, walks the triage decision tree, and delegates per-item to the add-public-method recipe.

**Backfilling a native capability that's missing from RN** (the native SDK already supports something — e.g., multi-instance — but the RN bridge never surfaced it) — use [`clevertap-react-native-backfill-missing-coverage`](../clevertap-react-native-backfill-missing-coverage/SKILL.md). Not release-driven; the work is the JS API DESIGN step (instance handles vs singletons, builders, listener-as-object, migration path), not the mechanics.

For platform-specific bridge work, switch to:
- [`clevertap-react-native-android`](../clevertap-react-native-android/SKILL.md) -- Android bridge deep dive
- [`clevertap-react-native-ios`](../clevertap-react-native-ios/SKILL.md) -- iOS bridge deep dive

## Working in this repo

- **Ignore untracked tooling files** in `git status` (watchman cookies, `Example/android/app/src/main/assets/`, `Example/ios/build/`, `Example/ios/Pods/`, `.idea/`, `.vscode/`, etc.). They are local tooling noise, not part of any change. See the `feedback_untracked_tooling_files` memory entry.
- **No SDK-level unit test framework** exists. The de-facto integration test is running the Example app on each platform and exercising features through the Action menu.
- **Dependency on the Android SDK repo** -- when a feature requires changes in `clevertap-android-sdk` first, those land there and only then bubble up to a version bump here.

## Testing

```bash
npm run lint              # ESLint over JS sources
npm run lint-fix          # Auto-fix lint issues

# Example app — iOS
cd Example && yarn install && cd ios && pod install && cd ..
yarn ios

# Example app — Android
cd Example && yarn install
yarn android
```

There is no `npm test` script wired up; `package.json` currently has only the stub. JS unit tests live only at `Example/__tests__/App-test.js` (renders the App component).

## Reference Files

- [refs/architecture-overview.md](refs/architecture-overview.md) -- Three-layer bridge diagrams (old + new RN architecture), sequence diagrams for a call and a callback
- [refs/feature-map.md](refs/feature-map.md) -- Every public method with its JS / iOS / Android entry-point file and method name
- [refs/file-map.md](refs/file-map.md) -- Top-level file inventory with one-line descriptions
- [refs/workflows.md](refs/workflows.md) -- Cross-platform procedures (debug, version bump, release, Example app updates)
