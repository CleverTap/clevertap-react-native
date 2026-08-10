---
name: clevertap-react-native-ios
description: Maintains, debugs, and extends the iOS bridge in the CleverTap React Native SDK. Covers the `CleverTapReact.mm` (Objective-C++) module surface with `RCT_EXPORT_METHOD` declarations, the `supportedEvents` array, AppDelegate integration via `CleverTapReactManager`, custom-template presentation (`CleverTapReactTemplatePresenter`, `CleverTapReactCustomTemplates`, `CleverTapReactAppFunctionPresenter`), the pending-event queue (`CleverTapReactPendingEvent`) that holds events fired before JS attaches a listener, and the `CleverTap-iOS-SDK` CocoaPods dependency in `clevertap-react-native.podspec`. Use when editing files under `ios/`, wiring a new `RCT_EXPORT_METHOD`, or debugging an iOS-side bridge issue.
allowed-tools:
  - Read
  - Glob
  - Grep
  - Bash
---

# CleverTap React Native — iOS Bridge

## Pipeline Overview

An iOS-side call traverses these stages:

1. **RN dispatch** -- React Native's bridge invokes a method declared with `RCT_EXPORT_METHOD` in `CleverTapReact.mm`; the same `.mm` file serves both old and new architectures with `RCT_NEW_ARCH_ENABLED` macros for TurboModule conformance
2. **Native execution** -- the exported method resolves `[CleverTap sharedInstance]` (or a named instance for `setInstanceWithAccountId`), converts the JS arguments (`NSDictionary`, `NSArray`, primitives), and calls into the underlying CleverTap-iOS-SDK
3. **Response marshalling** -- synchronous responses use the `RCTPromiseResolveBlock` / `RCTPromiseRejectBlock` pair (for promise-style methods) or `RCTResponseSenderBlock` (callbacks); the JS bridge serializes `NSDictionary`/`NSArray` back to JS objects/arrays
4. **AppDelegate integration** -- `CleverTapReactManager` provides static helpers the host app calls from its `AppDelegate` (`application:didFinishLaunchingWithOptions:`, `application:didReceiveRemoteNotification:`, `userNotificationCenter:...`) so push payloads and notification clicks reach the SDK
5. **Custom template presentation** -- `CleverTapReactTemplatePresenter` and `CleverTapReactAppFunctionPresenter` implement the native SDK's custom-template presenter protocols; on `present`/`close`, they fire `CleverTapCustomTemplatePresent` / `CleverTapCustomTemplateClose` events
6. **Event emission** -- the module inherits `RCTEventEmitter`; events are declared in the `supportedEvents` array and sent via `[self sendEventWithName:body:]`. If JS has not yet called `addListener`, events that arrived too early are stored in `CleverTapReactPendingEvent` and flushed when the listener attaches

## Architectural Rules

- **`.mm` (Objective-C++) is the bridge surface** -- the file extension matters: it lets the module include both Objective-C runtime APIs and C++ headers (used by the new-arch TurboModule codegen output).
- **Single source for both architectures** -- the same `CleverTapReact.mm` handles old and new arch; macros (`RCT_NEW_ARCH_ENABLED`, `getTurboModule:`) gate the new-arch conformance code. There is no oldarch/newarch directory split as on Android.
- **Events MUST be in `supportedEvents`** -- iOS RN strictly validates: emitting an event whose name isn't returned by `- (NSArray<NSString *> *)supportedEvents` produces a runtime warning and the event is dropped. Every new event needs a row here AND a matching constant on the JS side.
- **Pending-event queue is iOS-specific** -- `CleverTapReactPendingEvent` stores `(eventName, body)` tuples for events that fire before the first `addListener` call (e.g., push click that launched the app). The queue is drained when `startObserving` is first invoked by RN.
- **Custom-template presenters live in their own files** -- `CleverTapReactTemplatePresenter` (template UI), `CleverTapReactCustomTemplates` (sync + arg accessors), `CleverTapReactAppFunctionPresenter` (app functions). They share the same event-dispatch contract.
- **AppDelegate integration must be opt-in by the host app** -- `CleverTapReactManager` exposes hooks the host wires into their AppDelegate; we do NOT swizzle. See `docs/integration.md` for the boilerplate hosts must add.
- **Underlying SDK version is pinned** in `clevertap-react-native.podspec`: `s.dependency 'CleverTap-iOS-SDK', '7.6.0'`. Bump only with a coordinated test pass on iOS.
- **Minimum iOS platform:** `9.0` (declared in the podspec). Anything using newer-OS APIs must be `@available`-gated.

## Source Tree

```
clevertap-react-native.podspec                Pod metadata, CleverTap-iOS-SDK version pin
ios/CleverTapReact/
  CleverTapReact.{h,mm}                       Module surface — all RCT_EXPORT_METHOD declarations
                                              + supportedEvents + event dispatch + RCTEventEmitter
  CleverTapReactManager.{h,mm}                Static helpers host AppDelegate calls into
                                              (didFinishLaunching, didReceiveRemoteNotification)
  CleverTapReactTemplatePresenter.{h,mm}      Implements CleverTap-iOS-SDK CTTemplatePresenter
                                              protocol; emits Present/Close events
  CleverTapReactCustomTemplates.{h,mm}        Custom-template sync + argument accessors
  CleverTapReactAppFunctionPresenter.{h,mm}   Implements CTAppFunctionPresenter protocol
  CleverTapReactPendingEvent.{h,mm}           In-memory queue for pre-listener events
```

See [refs/file-map.md](refs/file-map.md) for descriptions and method counts.

## Public API patterns

Standard `RCT_EXPORT_METHOD` (synchronous side-effect):

```objective-c
RCT_EXPORT_METHOD(recordEvent:(NSString *)eventName withProps:(NSDictionary *)props) {
    [[CleverTap sharedInstance] recordEvent:eventName withProps:props];
}
```

Promise-style returning a value:

```objective-c
RCT_EXPORT_METHOD(customTemplateGetStringArg:(NSString *)templateName
                  argName:(NSString *)argName
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    NSString *value = [[CleverTap sharedInstance].customTemplates stringValueFor:argName forTemplate:templateName];
    resolve(value);
}
```

Callback style (Node-style `(error, value)`):

```objective-c
RCT_EXPORT_METHOD(profileGetProperty:(NSString *)propertyName callback:(RCTResponseSenderBlock)callback) {
    id value = [[CleverTap sharedInstance] profileGet:propertyName];
    callback(@[[NSNull null], value ?: [NSNull null]]);
}
```

Event emission (only when the module is observed):

```objective-c
- (void)sendOrQueueEvent:(NSString *)name body:(id)body {
    if (hasListeners) {
        [self sendEventWithName:name body:body];
    } else {
        [CleverTapReactPendingEvent enqueueWithName:name body:body];
    }
}
```

## Events declared on iOS

`supportedEvents` returns the public event names (matching the constants on the JS and Android sides). The complete list:

`CleverTapProfileDidInitialize`, `CleverTapProfileSync`, `CleverTapInAppNotificationShowed`, `CleverTapInAppNotificationDismissed`, `CleverTapInAppNotificationButtonTapped`, `CleverTapInboxDidInitialize`, `CleverTapInboxMessagesDidUpdate`, `CleverTapInboxMessageTapped`, `CleverTapInboxMessageButtonTapped`, `CleverTapDisplayUnitsLoaded`, `CleverTapFeatureFlagsDidUpdate`, `CleverTapProductConfigDidFetch`, `CleverTapProductConfigDidActivate`, `CleverTapProductConfigDidInitialize`, `CleverTapPushNotificationClicked`, `CleverTapPushPermissionResponseReceived`, `CleverTapOnVariablesChanged`, `CleverTapOnOneTimeVariablesChanged`, `CleverTapOnValueChanged`, `CleverTapOnVariablesChangedAndNoDownloadsPending`, `CleverTapOnceVariablesChangedAndNoDownloadsPending`, `CleverTapOnFileValueChanged`, `CleverTapCustomTemplatePresent`, `CleverTapCustomFunctionPresent`, `CleverTapCustomTemplateClose`.

Event names are also exposed to JS via `+ (NSDictionary *)constantsToExport`.

## Workflows

Step-by-step procedures live in [refs/workflows.md](refs/workflows.md):

1. **Add a new `RCT_EXPORT_METHOD` to the iOS bridge** -- declare in `CleverTapReact.mm`, choose callback vs promise signature, marshal args from `NSDictionary`/`NSArray`, call the SDK
2. **Add a new event** -- add the event name to `supportedEvents`, emit via the `sendOrQueueEvent` helper from the appropriate native callback site (often a delegate method or a presenter), ensure the JS layer re-exports the constant
3. **Bump `CleverTap-iOS-SDK` version** -- update `clevertap-react-native.podspec`, run `pod update CleverTap-iOS-SDK` in `Example/ios`, smoke-test with `yarn ios`, update CHANGELOG
4. **Integrate a notification service extension** -- there are two example targets (`Example/ios/NotificationServiceSwift/`, `Example/ios/NotificationContentSwift/`) showing the Swift host-app setup that pipes payloads back via `CleverTapReactManager`
5. **Debug an iOS-side crash from RN** -- check Xcode console for `RCTFatal` / NSException; verify the `supportedEvents` name matches what's being emitted; verify the AppDelegate hooks for push are wired
6. **Handle a pending event before JS listens** -- ensure the emit path goes through `CleverTapReactPendingEvent enqueue` when `hasListeners` is NO; the queue auto-flushes on `startObserving`

## Testing

No iOS unit tests exist in the bridge module. Verification is via the Example app:

```bash
cd Example
yarn install
cd ios && pod install && cd ..
yarn ios
# Or open Example/ios/CleverTapReactNativeExample.xcworkspace in Xcode and Run
```

Inspect the bridge from Xcode's debug console using Objective-C breakpoints in `CleverTapReact.mm`. The Example app's Action list (`Example/app/constants.js`) exercises every public method through `Example/app/app-utils.js`.

## Anti-patterns

- Emitting an event whose name is not in `supportedEvents` — RN warns and drops the event silently in production.
- Using `[self sendEventWithName:...]` unconditionally — if no listener is attached yet (very common right after `application:didFinishLaunchingWithOptions:`), the event is lost. Use the pending-event queue.
- Adding business logic in `CleverTapReactManager` — keep it a thin pass-through; logic belongs in the native SDK.
- Swizzling AppDelegate methods — the integration is explicit by design (see `docs/integration.md`).
- Mismatching the event name between `supportedEvents`, the JS constant in `src/index.js`, and the Android `CleverTapEvent.kt` enum — all three must agree exactly.

## Reference Files

- [refs/architecture-overview.md](refs/architecture-overview.md) -- Diagrams: AppDelegate plumbing, presenter dispatch, pending-event queue lifecycle
- [refs/file-map.md](refs/file-map.md) -- Every iOS source file with one-line description
- [refs/workflows.md](refs/workflows.md) -- Step-by-step procedures for the six common iOS-side tasks
