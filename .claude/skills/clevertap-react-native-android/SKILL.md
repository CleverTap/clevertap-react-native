---
name: clevertap-react-native-android
description: Maintains, debugs, and extends the Android bridge in the CleverTap React Native SDK. Covers the old/new architecture split (`CleverTapModule.kt` shims under `android/src/oldarch/` and `android/src/newarch/`), the shared `CleverTapModuleImpl.java` implementation, the `CleverTapEvent` + `CleverTapEventEmitter` event pipeline with per-event buffering flags, the `CleverTapListenerProxy` native-SDK-callback adapter, type marshalling via `CleverTapUtils`, and the `clevertap-android-sdk` Maven dependency in `android/build.gradle`. Use when editing files under `android/`, wiring a new `@ReactMethod`, or debugging an Android-side bridge issue.
allowed-tools:
  - Read
  - Glob
  - Grep
  - Bash
---

# CleverTap React Native — Android Bridge

## Pipeline Overview

An Android-side call traverses these stages:

1. **RN dispatch** -- React Native's bridge invokes either `android/src/oldarch/CleverTapModule.kt` (legacy bridge) or `android/src/newarch/CleverTapModule.kt` (TurboModule via Fabric); selection is driven by the host app's `newArchEnabled` gradle property. The shim is where the React bridge contract is satisfied: oldarch annotates each method with `@ReactMethod`, newarch uses `override` against the codegen `NativeCleverTapModuleSpec` base class.
2. **Shim delegation** -- the shim holds a single `CleverTapModuleImpl` instance and forwards every call to it verbatim with the same arguments; the shim adds NO logic — it exists purely to satisfy the RN architecture contract.
3. **Native execution** -- `CleverTapModuleImpl.java` is a plain helper class (it does NOT extend `ReactContextBaseJavaModule` and has NO `@ReactMethod` annotations). It resolves the active `CleverTapAPI` instance (or constructs one for `setInstanceWithAccountId`), translates `ReadableMap`/`ReadableArray` args via `CleverTapUtils`, and calls the underlying SDK method.
4. **Response marshalling** -- synchronous results return through the supplied `Promise` (resolved with a `WritableMap` / primitive) or `Callback`; async/native-SDK callbacks land in `CleverTapListenerProxy`
5. **Event emission** -- `CleverTapListenerProxy` translates each native-SDK callback into a `CleverTapEvent` enum value + `WritableMap` payload and calls `CleverTapEventEmitter.sendEvent(...)`; the emitter buffers if `event.isBufferable` is true and the JS layer has not yet attached a listener (signaled via `onEventListenerAdded`)
6. **JS delivery** -- React Native's `DeviceEventManagerModule.RCTDeviceEventEmitter` (old arch) or the new-arch event emitter delivers the event to any registered JS listener

## Architectural Rules

- **The two arch shims contain zero logic** -- `android/src/oldarch/CleverTapModule.kt` and `android/src/newarch/CleverTapModule.kt` are forwarders. Every new bridge method must be added to BOTH files with identical argument lists, each forwarding to the matching `cleverTapModuleImpl.<method>(...)` call.
- **The oldarch shim uses `@ReactMethod`; the newarch shim uses `override`.** The oldarch class extends `ReactContextBaseJavaModule` so the bridge discovers methods via the annotation. The newarch class extends `NativeCleverTapModuleSpec` (codegen output from `src/NativeCleverTapModule.ts`) so it overrides spec methods — no `@ReactMethod` needed there.
- **`CleverTapModuleImpl.java` is a plain helper class** -- it does NOT extend `ReactContextBaseJavaModule` and contains zero `@ReactMethod` annotations. It exposes ~126 plain public methods organized by feature (push, profile, inbox, in-app, variables, custom templates, …). All business marshalling, argument validation, and SDK calls live here. The two arch shims are the only callers.
- **Type conversion goes through `CleverTapUtils`** -- never hand-roll `WritableMap`/`WritableArray` construction in `CleverTapModuleImpl`. Use the helpers (`getWritableMapFromMap`, `getWritableArrayFromList`, `toMap`, `toArray`, JSON converters) — they handle nested types correctly.
- **Events are declared in `CleverTapEvent.kt`** -- each event has an enum entry with `eventName: String` and `isBufferable: Boolean`. Bufferable means: if the event fires before JS attaches a listener, it is queued and flushed when the JS layer calls `onEventListenerAdded`. NEVER add an event string in two places — the enum is the single source.
- **Native SDK callbacks plug in via `CleverTapListenerProxy`** -- one proxy instance is set on the `CleverTapAPI` instance and implements every listener interface the native SDK exposes (push permission, inbox lifecycle, in-app, variables, display units, etc.). Adding a new callback means adding both an interface implementation here AND a corresponding `CleverTapEvent` entry.
- **Package registration is in `CleverTapPackage.kt`** -- it returns the single `CleverTapModule` for whichever architecture is active. Host apps add `new CleverTapPackage()` in their `MainApplication`.
- **Underlying SDK version is pinned** in `android/build.gradle` -- `api 'com.clevertap.android:clevertap-android-sdk:8.1.0'`. The `api` (not `implementation`) configuration intentionally exposes the SDK's public types to host apps.
- **Build config:** `compileSdkVersion 36`, `targetSdkVersion 36`, `minSdkVersion 23`. The module is published as `com.clevertap.react:clevertap-react-native`.

## Source Tree

```
android/build.gradle                            Maven config, SDK versions, arch detection
android/src/main/AndroidManifest.xml            (minimal — host app provides activities/permissions)

android/src/main/java/com/clevertap/react/
  CleverTapModuleImpl.java                      Shared bridge implementation (~100 methods)
  CleverTapPackage.kt                           RN package registration
  CleverTapApplication.kt                       Convenience Application class for host apps
  CleverTapEvent.kt                             Event enum (name + bufferable flag)
  CleverTapEventEmitter.kt                      Emits to JS, buffers when no listener
  CleverTapListenerProxy.kt                     Native-SDK-callback → CleverTapEvent dispatcher
  CleverTapUtils.java                           ReadableMap/Array ↔ Java Map/List/JSON converters
  CleverTapRnAPI.kt                             Public init API (host-app entry point)
  Constants.kt                                  REACT_MODULE_NAME, FCM push type string

android/src/oldarch/CleverTapModule.kt          Legacy-bridge shim. extends ReactContextBaseJavaModule.
                                                 Every method has @ReactMethod and forwards to Impl.
android/src/newarch/CleverTapModule.kt          TurboModule shim. extends NativeCleverTapModuleSpec.
                                                 Every method is `override` and forwards to Impl.
```

See [refs/file-map.md](refs/file-map.md) for descriptions and method counts.

## Public API patterns

A typical Impl method (plain public method, no annotation):

```java
// CleverTapModuleImpl.java
public void getCleverTapID(Callback callback) {
    CleverTapAPI cleverTap = getCleverTapAPI();
    cleverTap.getCleverTapID(cleverTapID -> {
        callbackWithString(callback, cleverTapID);
    });
}
```

The matching oldarch shim entry (annotated):

```kotlin
// android/src/oldarch/CleverTapModule.kt
@ReactMethod
fun getCleverTapID(callback: Callback) {
    cleverTapModuleImpl.getCleverTapID(callback)
}
```

The matching newarch shim entry (overriding the spec):

```kotlin
// android/src/newarch/CleverTapModule.kt
override fun getCleverTapID(callback: Callback) {
    cleverTapModuleImpl.getCleverTapID(callback)
}
```

A method returning a structured payload via callback (Node-style `(error, value)`):

```java
// CleverTapModuleImpl.java
public void getAllInboxMessages(Callback callback) {
    CleverTapAPI cleverTap = getCleverTapAPI();
    ArrayList<CTInboxMessage> messages = cleverTap.getAllInboxMessages();
    WritableArray result = CleverTapUtils.getWritableArrayFromList(toListOfJsonStrings(messages));
    callback.invoke(null, result);
}
```

Event constants are exposed to host apps via `getConstants()` on the OLDARCH shim (Kotlin):

```kotlin
override fun getConstants(): Map<String, Any> {
    val constants = mutableMapOf<String, Any>()
    for (e in CleverTapEvent.values()) {
        constants[e.name] = e.eventName
    }
    return constants
}
```

## Event flow

| `CleverTapEvent` enum | String emitted to JS | Bufferable | Triggered by |
|---|---|---|---|
| `CLEVERTAP_PROFILE_DID_INITIALIZE` | `CleverTapProfileDidInitialize` | yes | `SyncListener.profileDidInitialize()` |
| `CLEVERTAP_PROFILE_SYNC` | `CleverTapProfileSync` | no | `SyncListener.profileDataUpdated()` |
| `CLEVERTAP_INBOX_DID_INITIALIZE` | `CleverTapInboxDidInitialize` | yes | `CTInboxListener.inboxDidInitialize()` |
| `CLEVERTAP_INBOX_MESSAGES_DID_UPDATE` | `CleverTapInboxMessagesDidUpdate` | no | `CTInboxListener.inboxMessagesDidUpdate()` |
| `CLEVERTAP_ON_INBOX_MESSAGE_CLICK` | `CleverTapInboxMessageTapped` | no | `InboxMessageListener.onInboxItemClicked()` |
| `CLEVERTAP_ON_INBOX_BUTTON_CLICK` | `CleverTapInboxMessageButtonTapped` | no | `InboxMessageButtonListener.onInboxButtonClick()` |
| `CLEVERTAP_IN_APP_NOTIFICATION_SHOWED` | `CleverTapInAppNotificationShowed` | yes | `InAppNotificationListener.onShow()` |
| `CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED` | `CleverTapInAppNotificationDismissed` | yes | `InAppNotificationListener.onDismissed()` |
| `CLEVERTAP_ON_INAPP_BUTTON_CLICK` | `CleverTapInAppNotificationButtonTapped` | yes | `InAppNotificationButtonListener.onInAppButtonClick()` |
| `CLEVERTAP_PUSH_NOTIFICATION_CLICKED` | `CleverTapPushNotificationClicked` | yes | `CTPushNotificationListener.onNotificationClickedPayloadReceived()` |
| `CLEVERTAP_ON_PUSH_PERMISSION_RESPONSE` | `CleverTapPushPermissionResponseReceived` | no | `PushPermissionResponseListener.onPushPermissionResponse()` |
| `CLEVERTAP_ON_DISPLAY_UNITS_LOADED` | `CleverTapDisplayUnitsLoaded` | yes | `DisplayUnitListener.onDisplayUnitsLoaded()` |
| `CLEVERTAP_FEATURE_FLAGS_DID_UPDATE` | `CleverTapFeatureFlagsDidUpdate` | yes | `FeatureFlagListener.featureFlagsUpdated()` |
| `CLEVERTAP_PRODUCT_CONFIG_DID_*` | `CleverTapProductConfig*` | yes (initialize), no (fetch/activate) | `CTProductConfigListener.*` |
| `CLEVERTAP_ON_VARIABLES_CHANGED` | `CleverTapOnVariablesChanged` | no | `VariablesChangedCallback.variablesChanged()` |
| `CLEVERTAP_ON_VALUE_CHANGED` | `CleverTapOnValueChanged` | no | `Var.addValueChangedCallback(...)` |
| `CLEVERTAP_CUSTOM_TEMPLATE_PRESENT` / `_CLOSE` | `CleverTapCustomTemplatePresent` / `Close` | yes (present), no (close) | `TemplatePresenter` lifecycle |

Bufferable events are queued until JS calls `onEventListenerAdded`, which is auto-invoked by `src/index.js` on first `addListener`.

## Workflows

Step-by-step procedures live in [refs/workflows.md](refs/workflows.md):

1. **Add a new bridge method** -- declare a plain `public void` in `CleverTapModuleImpl.java` → add a `@ReactMethod`-annotated forwarder in `android/src/oldarch/CleverTapModule.kt` → add the matching `override` forwarder in `android/src/newarch/CleverTapModule.kt` → marshal types via `CleverTapUtils`
2. **Add a new event** -- add enum entry in `CleverTapEvent.kt` (name + bufferable) → fire via `CleverTapEventEmitter.sendEvent(...)` from `CleverTapListenerProxy` → ensure the JS layer re-exports the constant in `src/index.js`
3. **Bump `clevertap-android-sdk` version** -- update `android/build.gradle` `api 'com.clevertap.android:clevertap-android-sdk:X.Y.Z'`, update `Example/android/app/build.gradle` if it pins a version, run `yarn android` to smoke-test, update CHANGELOG
4. **Debug a native crash bubbling up from RN** -- run `adb logcat | grep -iE 'CleverTap|AndroidRuntime'`, identify the failing method in the stack, check that the shim is correctly forwarding to `CleverTapModuleImpl`, verify type marshalling in `CleverTapUtils`
5. **Wire an Android-only feature** -- add the bridge method; on the JS side guard the call with `Platform.OS === 'android'` (or expose an `isAvailable()` helper)

## Testing

There is no Android-side unit test suite in this module. Verification is via the Example app:

```bash
# From Example/ directory
yarn install
yarn android                          # debug build on connected device/emulator
# Or:
cd android && ./gradlew :app:installDebug && adb shell am start -n com.example.clevertaprn/.MainActivity
```

Useful log filters during dev:

```bash
adb logcat -s CleverTap CleverTapReact ReactNativeJS AndroidRuntime
```

The Example app exposes every public method through the Action list in `Example/app/constants.js`; tapping an action calls into `Example/app/app-utils.js` which invokes the corresponding `CleverTap.method(...)`.

## Anti-patterns

- Adding a method to `CleverTapModuleImpl` but forgetting to add the matching `@ReactMethod` forwarder to `oldarch/CleverTapModule.kt` AND the `override` forwarder to `newarch/CleverTapModule.kt` — the React bridge never discovers it.
- Putting `@ReactMethod` on a method in `CleverTapModuleImpl.java`. The Impl is not a ReactModule; the annotation does nothing there. The bridge surface lives on the SHIMS, not on the Impl.
- Hand-rolling `WritableMap` construction with nested types — nested `Map`/`List` are easy to get wrong; use `CleverTapUtils.getWritableMapFromMap` which recurses correctly.
- Adding an event string literal anywhere other than `CleverTapEvent.kt` — the JS-side constant in `src/index.js` MUST match the `eventName` field of the enum.
- Putting business logic in the arch shims — they exist only to satisfy two RN architecture contracts; logic belongs in `CleverTapModuleImpl`.

## Reference Files

- [refs/architecture-overview.md](refs/architecture-overview.md) -- Diagrams: old-arch vs new-arch dispatch, event buffering, listener-proxy fanout
- [refs/file-map.md](refs/file-map.md) -- Every Android source file with one-line description
- [refs/workflows.md](refs/workflows.md) -- Step-by-step procedures for the five common Android-side tasks
