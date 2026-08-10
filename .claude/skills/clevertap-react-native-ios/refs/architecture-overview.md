# iOS Bridge — Architecture Overview

## Module surface

```
┌─────────────────────────────────────────────────┐
│   ios/CleverTapReact/CleverTapReact.mm           │
│                                                 │
│   @interface CleverTapReact : RCTEventEmitter   │
│   <RCTBridgeModule>                              │
│                                                 │
│   RCT_EXPORT_MODULE()                            │
│                                                 │
│   - (NSArray<NSString *> *)supportedEvents      │
│       → returns event names visible to JS        │
│                                                 │
│   + (NSDictionary *)constantsToExport            │
│       → re-exports event names as constants     │
│                                                 │
│   ~100 × RCT_EXPORT_METHOD(...)                  │
│       → bridge methods                          │
│                                                 │
│   #if RCT_NEW_ARCH_ENABLED                      │
│     - (std::shared_ptr<TurboModule>)             │
│         getTurboModule:(NativeCleverTapModule    │
│         SpecJSI::Params)                         │
│   #endif                                        │
└─────────────────────────────────────────────────┘
                       │
                       │ calls
                       ▼
┌─────────────────────────────────────────────────┐
│       [CleverTap sharedInstance]                 │
│       (CleverTap-iOS-SDK 7.6.0)                  │
└─────────────────────────────────────────────────┘
```

A single `.mm` file serves both architectures. The `RCT_NEW_ARCH_ENABLED` macro selectively compiles the TurboModule conformance code.

## AppDelegate integration

The RN SDK does NOT swizzle. Host apps wire AppDelegate methods explicitly via `CleverTapReactManager`:

```
┌─────────────────────────────┐
│ Host AppDelegate.m/.swift   │
│                             │
│ application:didFinishLaunching:                  │
│   ↓                                              │
│ [CleverTapReactManager applicationDidFinishLaunching… │
│                             │
│ application:didReceiveRemoteNotification:        │
│   ↓                                              │
│ [CleverTapReactManager handleRemoteNotification:..]  │
│                             │
│ userNotificationCenter:didReceive:               │
│   ↓                                              │
│ [CleverTapReactManager didReceiveNotificationResponse:..] │
└─────────────────────────────┘
                ↓ static class methods
┌─────────────────────────────┐
│ CleverTapReactManager.mm    │
│ (thin pass-throughs to the   │
│  CleverTap-iOS-SDK)          │
└─────────────────────────────┘
```

See `docs/integration.md` for the exact host-app boilerplate.

## Custom-template presenter dispatch

```
CleverTap-iOS-SDK fires a template lifecycle event
                    │
                    ▼
┌──────────────────────────────────────┐
│ CleverTapReactTemplatePresenter      │
│ implements CTTemplatePresenter        │
│                                      │
│ - (void)onPresent:(CTTemplateContext *) │
│       → fires CleverTapCustomTemplate │
│         Present                       │
│                                      │
│ - (void)onCloseClicked                 │
│       → fires CleverTapCustomTemplate │
│         Close                         │
└──────────────────────────────────────┘
                    │
                    ▼
        sendOrQueueEvent (via .mm)
                    │
                    ▼
              JS host listener

CleverTapReactAppFunctionPresenter
       implements CTAppFunctionPresenter
       — same pattern, fires
         CleverTapCustomFunctionPresent
```

`CleverTapReactCustomTemplates.{h,mm}` holds the implementation of all `customTemplateGet*Arg` accessor methods. The presenters are separate from the accessor module so each can be registered independently on the SDK.

## Pending-event queue

iOS RN's `RCTEventEmitter` silently drops events when there are no listeners. The SDK works around this with `CleverTapReactPendingEvent`:

```
Native callback fires
         │
         ▼
    sendOrQueueEvent(name, body)
         │
         ▼
   ┌───────────────────────┐
   │ if hasListeners       │
   │   → sendEventWithName │
   │ else                  │
   │   → enqueue           │
   └───────────────────────┘
                        │
                        ▼
              CleverTapReactPendingEvent
              (in-memory FIFO)

           ... time passes ...

   JS calls CleverTap.addListener('X', h)
         │
         ▼
   RN's bridge invokes
   - (void)startObserving
         │
         ▼
   hasListeners = YES
   flush PendingEvent queue
         │
         ▼
   sendEventWithName: for each
   queued (name, body)
```

This is iOS-only because the Android side handles equivalent buffering inside `CleverTapEventEmitter` driven by `CleverTapEvent.isBufferable`.

## Object lifecycle and singleton

- `CleverTapReact` is registered as a singleton module by RN; lives for the bridge's lifetime.
- `[CleverTap sharedInstance]` is a CleverTap-SDK singleton (or a named instance via `instanceWithAccountId:`); persists for the app process lifetime.
- `CleverTapReactManager` is purely static; no instance state.
- Presenters (`CleverTapReactTemplatePresenter`, `CleverTapReactAppFunctionPresenter`) are owned by the CleverTap-SDK after registration; the SDK retains them for the process lifetime.
- `CleverTapReactPendingEvent` is class-level storage (NSMutableArray on the class); cleared on flush.
