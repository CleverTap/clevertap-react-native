# Android Bridge — Architecture Overview

## Old vs new architecture

```
                  React Native Bridge
                  ─────────────────────
                          │
       ┌──────────────────┴──────────────────┐
       │                                     │
  Legacy Bridge                       Fabric / TurboModules
  (newArchEnabled=false)              (newArchEnabled=true)
       │                                     │
       ▼                                     ▼
┌────────────────────────────┐    ┌────────────────────────────┐
│ android/src/oldarch/       │    │ android/src/newarch/       │
│   CleverTapModule.kt       │    │   CleverTapModule.kt       │
│                            │    │                            │
│ extends                    │    │ extends                    │
│ ReactContextBaseJavaModule │    │ NativeCleverTapModuleSpec  │
│                            │    │ (codegen output from        │
│ Each method has            │    │  src/NativeCleverTapModule  │
│ @ReactMethod and forwards  │    │  .ts spec)                  │
│ to cleverTapModuleImpl     │    │ Each method is `override`   │
│                            │    │ and forwards to the impl    │
└──────────────┬─────────────┘    └──────────────┬─────────────┘
               │                                 │
               └──────────────────┬──────────────┘
                                  │
                                  ▼
        ┌──────────────────────────────────────────┐
        │ android/src/main/.../CleverTapModuleImpl │
        │ ── plain helper class, no @ReactMethod ──│
        │                                          │
        │  Marshals args via CleverTapUtils,       │
        │  calls CleverTapAPI / dependent SDK,     │
        │  returns via Promise / Callback /         │
        │  CleverTapEventEmitter.                  │
        └──────────────────────────────────────────┘
```

The active gradle property `newArchEnabled` (host app's `gradle.properties`) selects which source set is included; both arch source sets are present in the published AAR but only one is registered at runtime.

## Event-buffering pipeline

```
┌─────────────────────┐
│ Native SDK callback │   (push permission response, inbox update, in-app shown,
│                     │    variables changed, custom template present, …)
└──────────┬──────────┘
           │
           ▼
┌──────────────────────────────────┐
│ CleverTapListenerProxy.kt        │
│ One instance per CleverTapAPI    │
│ Implements every listener        │
│ interface and translates each    │
│ callback into a CleverTapEvent + │
│ WritableMap body                 │
└──────────┬───────────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│ CleverTapEventEmitter.kt         │
│                                  │
│ ┌──────────────────────────────┐ │
│ │ if hasListeners              │ │
│ │   → emit immediately via      │ │
│ │     DeviceEventEmitter        │ │
│ │ else if event.isBufferable   │ │
│ │   → append to buffer          │ │
│ │ else                         │ │
│ │   → drop (event is lost)      │ │
│ └──────────────────────────────┘ │
└──────────┬───────────────────────┘
           │
           ▼
     JS host-app handler
     (CleverTap.addListener)

  ON FIRST addListener:
    JS calls onEventListenerAdded ──→ Emitter sets hasListeners=true
                                       and flushes all buffered events
```

`CleverTapEvent.kt` is the single source of truth for both the event NAME (string sent to JS) and the BUFFERABLE flag.

## Package registration

```
Host app MainApplication.java/kt
   │
   ▼
new CleverTapPackage()
   │
   ▼
CleverTapPackage.createNativeModules(reactContext)
   │
   ├── if newArchEnabled → CleverTapModule(reactContext)  [newarch/]
   └── else              → CleverTapModule(reactContext)  [oldarch/]
                              │
                              ▼
                       Implementation instance: CleverTapModuleImpl
                              │
                              ▼
                       Constructs CleverTapListenerProxy and registers
                       it against the CleverTapAPI default instance.
```

## Why Java for `CleverTapModuleImpl` and Kotlin elsewhere

`CleverTapModuleImpl.java` predates the rest and is intentionally kept in Java to minimize churn — it's the largest file in the module and changes incrementally with every release. New helpers (`CleverTapEvent`, `CleverTapEventEmitter`, `CleverTapListenerProxy`, `CleverTapPackage`, `CleverTapRnAPI`, `CleverTapApplication`) are Kotlin. `CleverTapUtils.java` is also Java to match call-site ergonomics from `CleverTapModuleImpl`.
