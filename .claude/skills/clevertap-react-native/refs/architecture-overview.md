# Architecture Overview — CleverTap React Native SDK

The RN SDK is a three-layer bridge. The same architecture serves old (Bridge) and new (Fabric/TurboModules) React Native architectures; the differences are isolated to the platform native modules.

## Three-layer bridge

```
┌──────────────────────────────────────────────────────────────────┐
│  Host App (JS / TS)                                              │
│  CleverTap.recordEvent('purchase', { amount: 99 })               │
└─────────────────────────┬────────────────────────────────────────┘
                          │ JS function call
┌─────────────────────────▼────────────────────────────────────────┐
│  src/index.js  +  src/NativeCleverTapModule.ts                   │
│  Thin JS wrapper → resolves CleverTapReact (the native module)   │
│  delegated through the TurboModule spec contract.                │
└─────────────────────────┬────────────────────────────────────────┘
                          │ RN bridge dispatch
              ┌───────────┴────────────┐
              │                        │
┌─────────────▼──────────┐  ┌──────────▼─────────────────┐
│  ios/CleverTapReact/   │  │  android/src/{oldarch,     │
│  CleverTapReact.mm     │  │  newarch}/CleverTapModule  │
│  RCT_EXPORT_METHOD     │  │  .kt  → CleverTapModuleImpl │
└─────────────┬──────────┘  └──────────┬─────────────────┘
              │                        │
              │  arg marshalling       │  arg marshalling
              │  via NSDictionary      │  via CleverTapUtils
              │                        │
┌─────────────▼──────────┐  ┌──────────▼─────────────────┐
│  CleverTap-iOS-SDK     │  │  clevertap-android-sdk     │
│  v7.6.0                │  │  v8.1.0                    │
│  [CleverTap sharedInst]│  │  CleverTapAPI              │
└────────────────────────┘  └────────────────────────────┘
```

## Old vs new architecture

The RN architecture choice is the host app's, not the SDK's. The RN SDK supports both:

- **Old architecture (Bridge):** JS-to-native calls go through the asynchronous bridge with serialized JSON. Methods are dispatched by name.
  - Android: `android/src/oldarch/CleverTapModule.kt` extends `ReactContextBaseJavaModule`.
  - iOS: `CleverTapReact.mm` extends `RCTEventEmitter` and declares methods with `RCT_EXPORT_METHOD`.

- **New architecture (Fabric + TurboModules):** JS-to-native calls use JSI with synchronous binding through `NativeCleverTapModuleSpec` codegen.
  - Android: `android/src/newarch/CleverTapModule.kt` extends `NativeCleverTapModuleSpec` (codegen output from the TS spec).
  - iOS: `CleverTapReact.mm` adds a `getTurboModule:` method guarded by `RCT_NEW_ARCH_ENABLED`.

Both architectures reach the same business logic — on Android via the shared `CleverTapModuleImpl`, on iOS via the same `.mm` file body.

## Sequence: a synchronous-with-promise call

Example: `CleverTap.getCleverTapID(callback)`.

```
JS                            Android                          iOS
──                            ───────                          ───
CleverTap.getCleverTapID(cb)
  │
  ▼
CleverTapReact.getCleverTapID(jsCb)
  │
  ▼
RN bridge ──────────────────► CleverTapModule.getCleverTapID(cb)
                                       │
                                       ▼
                              CleverTapModuleImpl.getCleverTapID(cb)
                                       │
                                       ▼
                              cleverTap.getCleverTapID(id ->
                                  callbackWithString(cb, id))
                                       │
                                       ▼
                              RN bridge ──────────────► (delivered to JS)
                                                              │
                                                              ▼
                                                        cb(null, id)
```

The iOS path is the mirror image: `RCT_EXPORT_METHOD(getCleverTapID:(RCTResponseSenderBlock)callback)` → `[CleverTap sharedInstance].profileGetCleverTapID` → `callback(@[NSNull, id])`.

## Sequence: an event callback (push click)

Example: a push notification is tapped while the app is cold-starting.

```
Native OS                  iOS bridge                  JS layer
─────────                  ──────────                  ────────
(user taps push)
   │
   ▼
AppDelegate :didReceiveRemote
   │
   ▼
CleverTapReactManager
.didReceiveRemoteNotification
   │
   ▼
[CleverTap sharedInstance]
.handleNotificationWithData
   │
   ▼
CleverTap-iOS-SDK fires
push-clicked callback
   │
   ▼
CleverTapReact.mm gets the
callback (delegate method)
   │
   ▼
hasListeners == NO
(JS hasn't attached yet)
   │
   ▼
CleverTapReactPendingEvent
.enqueue(name, body)
                          ... time passes ...
                                                         │
                                                         ▼
                                                    JS bundle loads
                                                         │
                                                         ▼
                              CleverTap.addListener(
                              'CleverTapPushNotificationClicked', h)
                                                         │
                                                         ▼
                              RN calls startObserving on .mm
                                       │
                                       ▼
                              hasListeners = YES
                              flush PendingEvent queue
                                       │
                                       ▼
                              [self sendEventWithName:body:]
                                                         │
                                                         ▼
                                                    h(payload)
```

The Android path uses `CleverTapEvent.isBufferable` instead: `CleverTapEventEmitter` keeps the event in an internal buffer until the JS layer calls `onEventListenerAdded` (auto-triggered by the first `CleverTap.addListener`).

## Layered responsibilities at a glance

| Layer | Owns | Does NOT own |
|---|---|---|
| `src/index.js` | Public surface, listener registration via `NativeEventEmitter`, platform guards | Validation, transformation, business logic |
| `src/NativeCleverTapModule.ts` | Cross-platform method contract | Implementation |
| Android shims | Conforming to old/new arch contracts | Business logic — they forward verbatim |
| `CleverTapModuleImpl` | Arg marshalling + SDK calls | UI, persistence, networking — those are in the native SDK |
| `CleverTapReact.mm` | Arg marshalling + SDK calls + event dispatch | Anything else |
| Native SDKs | Networking, persistence, UI, lifecycle | Bridge concerns |
