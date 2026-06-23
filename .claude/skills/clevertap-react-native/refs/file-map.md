# File Map — Top-level Inventory

One-line description per file. Generated paths and lockfiles excluded.

## Repository root

| Path | Description |
|---|---|
| `package.json` | NPM metadata, lint scripts, peer dep on React Native |
| `clevertap-react-native.podspec` | iOS pod metadata; pins `CleverTap-iOS-SDK 7.6.0` |
| `tsconfig.build.json` | TS build config for codegen output |
| `CHANGELOG.md` | Version history |
| `README.md` | Project overview, installation pointer |
| `LICENSE` | MIT |

## `src/` — JS public API

| Path | Description |
|---|---|
| `src/index.js` | The `CleverTap` object — all public methods + event constant exports |
| `src/index.d.ts` | TypeScript declarations for every public method (host-app facing) |
| `src/NativeCleverTapModule.ts` | TurboModule spec interface (codegen contract) |

## `ios/CleverTapReact/` — iOS bridge

| Path | Description |
|---|---|
| `CleverTapReact.{h,mm}` | Module surface: `RCT_EXPORT_METHOD` declarations, `supportedEvents`, RCTEventEmitter |
| `CleverTapReactManager.{h,mm}` | AppDelegate integration helpers (push, deep link, notifications) |
| `CleverTapReactTemplatePresenter.{h,mm}` | Native presenter conforming to `CTTemplatePresenter` |
| `CleverTapReactCustomTemplates.{h,mm}` | Custom-template sync + argument accessor implementations |
| `CleverTapReactAppFunctionPresenter.{h,mm}` | Implements `CTAppFunctionPresenter` |
| `CleverTapReactPendingEvent.{h,mm}` | Pre-listener event queue (event-loss prevention) |

## `android/` — Android bridge

| Path | Description |
|---|---|
| `android/build.gradle` | Module gradle; pins `com.clevertap.android:clevertap-android-sdk 8.1.0` |
| `android/src/main/AndroidManifest.xml` | Manifest scaffold (host app supplies application/activities) |
| `android/src/main/java/com/clevertap/react/CleverTapModuleImpl.java` | Shared bridge implementation as a plain helper class (~126 public methods, no `@ReactMethod` — the annotations live on the shims) |
| `android/src/main/java/com/clevertap/react/CleverTapPackage.kt` | RN package registration |
| `android/src/main/java/com/clevertap/react/CleverTapApplication.kt` | Convenience Application class for host apps |
| `android/src/main/java/com/clevertap/react/CleverTapEvent.kt` | Event enum (name + `isBufferable`) |
| `android/src/main/java/com/clevertap/react/CleverTapEventEmitter.kt` | DeviceEventEmitter wrapper with buffering |
| `android/src/main/java/com/clevertap/react/CleverTapListenerProxy.kt` | Native-SDK-callback → CleverTapEvent dispatcher |
| `android/src/main/java/com/clevertap/react/CleverTapUtils.java` | ReadableMap/Array ↔ Java type converters + JSON helpers |
| `android/src/main/java/com/clevertap/react/CleverTapRnAPI.kt` | Public Kotlin init API for host apps |
| `android/src/main/java/com/clevertap/react/Constants.kt` | `REACT_MODULE_NAME` + FCM push type string |
| `android/src/oldarch/CleverTapModule.kt` | Legacy-bridge shim. Extends `ReactContextBaseJavaModule`; each method `@ReactMethod`, forwards to Impl |
| `android/src/newarch/CleverTapModule.kt` | TurboModule shim. Extends `NativeCleverTapModuleSpec` (codegen); each method `override`, forwards to Impl |

## `Example/` — demo app

| Path | Description |
|---|---|
| `Example/app/App.js` | Action-list UI, listener registration, navigation |
| `Example/app/app-utils.js` | One handler function per `Action` — wraps the SDK call with a toast |
| `Example/app/constants.js` | `Actions` map — ~105 action keys for the demo UI |
| `Example/app/DynamicForm.js` | Form component for input-driven actions |
| `Example/app/ExpandableListView.js` | Collapsible list component for grouped actions |
| `Example/__tests__/App-test.js` | The only JS unit test — renders the App component |
| `Example/ios/` | iOS Xcode project (Swift host app) + notification service extensions |
| `Example/android/` | Android gradle project (Kotlin host app) |
| `Example/custom-templates/` | Sample custom-template manifest files |
| `Example/metro.config.js` | Metro bundler config |
| `Example/babel.config.js` | Babel preset config |

## `docs/` — integration & feature docs

| Path | Description |
|---|---|
| `docs/install.md` | NPM install + native dependency setup |
| `docs/integration.md` | AppDelegate + MainApplication boilerplate hosts must add |
| `docs/usage.md` | API usage guide + examples (~13KB) |
| `docs/callbackPayloadFormat.md` | Authoritative payload schema for every event |
| `docs/CustomCodeTemplates.md` | Custom-template implementation guide (~10KB) |
| `docs/Variables.md` | Variables (PE) feature guide |
| `docs/pushprimer.md` | Push permission primer guide |
| `docs/iospushtemplates.md` | iOS push templates SDK integration (~25KB) |

## `.claude/` — Claude Code config

| Path | Description |
|---|---|
| `.claude/settings.local.json` | Per-machine permission grants |
| `.claude/skills/clevertap-react-native/` | This skill — broad bridge overview |
| `.claude/skills/clevertap-react-native-android/` | Android bridge deep dive |
| `.claude/skills/clevertap-react-native-ios/` | iOS bridge deep dive |
| `.claude/skills/clevertap-react-native-add-public-method/` | Workflow skill for cross-platform public-method changes |
