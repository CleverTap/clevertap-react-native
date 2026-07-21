---
name: clevertap-react-native-add-public-method
description: End-to-end workflow for adding a new public method or event to the CleverTap React Native SDK — a coordinated change across the TurboModule spec, JS wrapper, TypeScript types, Android bridge (CleverTapModuleImpl + both arch shims), iOS bridge (RCT_EXPORT_METHOD + supportedEvents), Example app demo, and docs. Includes the dependency-correct order, an anti-pattern list (most common: forgetting one of the Android arch shims, or omitting the iOS event from supportedEvents), and a verification checklist. Use when adding a new `CleverTap.foo(...)` method or a new `CleverTap.someEvent` listener.
allowed-tools:
  - Read
  - Glob
  - Grep
  - Bash
---

# Add a Public Method or Event — End-to-End Workflow

This is a procedural skill. It assumes you already understand the bridge architecture (see [`clevertap-react-native`](../clevertap-react-native/SKILL.md)).

## When to use

- Adding a brand-new method on the `CleverTap` JS object.
- Adding a new event the host app can subscribe to via `CleverTap.addListener(eventName, handler)`.
- Surfacing an already-existing native SDK method through the bridge.

If the work touches only Android or only iOS bridge internals (refactoring, debugging), use the platform-specific skill instead.

## Before you start

This skill assumes the JS API shape is settled — you know the method name, the arguments, the return shape, and whether it's a method or an event. If any of the following is true, run [`clevertap-react-native-backfill-missing-coverage`](../clevertap-react-native-backfill-missing-coverage/SKILL.md) FIRST to design the JS surface, then come back here for execution:

- The native shape doesn't map 1:1 to JS (e.g., the native API returns a stateful handle object, takes a multi-step builder, exposes a listener interface with multiple methods).
- The addition conflicts with existing JS API (deprecation needed, breaking change, migration path required).
- You're not sure whether the new method should be a singleton call or attached to an instance / namespace object.
- Cross-platform shapes differ enough that a unified JS API needs a deliberate design call.

Once the design is settled (a one-pager describing the JS surface, signed off by the user), this skill takes over for the mechanics.

## Method checklist (dependency-correct order)

Work top-to-bottom. Each step depends on the previous one.

1. **TurboModule spec** -- declare the method on the `Spec` interface in `src/NativeCleverTapModule.ts`. This is the cross-platform contract. Use the same JS-level types you'd use in TypeScript (`string`, `number`, `boolean`, `Object`, `Array<...>`, function callbacks).

2. **JS wrapper** -- add the method to the `CleverTap` object in `src/index.js`. Keep it a thin delegation: validate nothing the native layer already validates; forward arguments verbatim to `CleverTapReact.<methodName>(...)`. If the method is platform-only, guard with `Platform.OS === '...'` and either no-op or surface a helpful warning on the other platform.

3. **TypeScript declarations** -- add the matching method signature to `src/index.d.ts`. Use `Callback`, `CallbackString`, etc. from existing patterns. Document deprecations with `@deprecated` JSDoc.

4. **Android — implementation** -- add the method to `android/src/main/java/com/clevertap/react/CleverTapModuleImpl.java` as a plain `public` method (do NOT add `@ReactMethod` — the Impl is a plain helper class, not a `ReactContextBaseJavaModule`). Marshal `ReadableMap` / `ReadableArray` arguments via `CleverTapUtils` helpers (`toMap`, `toArray`, `getWritableMapFromMap`, `getWritableArrayFromList`). Call into the underlying `CleverTapAPI`.

5. **Android — old-arch shim** -- in `android/src/oldarch/CleverTapModule.kt`, add a `@ReactMethod`-annotated `fun` with the matching signature that forwards to `cleverTapModuleImpl.<methodName>(...)`. This is where the React bridge actually discovers the method on the legacy bridge.

6. **Android — new-arch shim** -- in `android/src/newarch/CleverTapModule.kt`, add the matching `override fun` (no `@ReactMethod` — the base class `NativeCleverTapModuleSpec` defines the contract). Same body: forward to `cleverTapModuleImpl.<methodName>(...)`. BOTH shims must exist for the method to work across both RN architectures.

7. **iOS — implementation** -- add an `RCT_EXPORT_METHOD(...)` block in `ios/CleverTapReact/CleverTapReact.mm`. Convert `NSDictionary` / `NSArray` args. Choose the signature shape:
   - Side-effect only → no callback/resolver
   - Returns a value → `RCTResponseSenderBlock callback` invoked with `@[NSNull, value]` (Node-style `(error, value)`)
   - Async with promise → `resolver:` + `rejecter:`

8. **iOS — TurboModule conformance** (only if adding a method that needs to appear on the new arch) -- the codegen output already covers most types via the spec in step 1; for unusual signatures (sync return, JSI objects), add a `getTurboModule:` override block.

9. **Event-only steps (skip if adding a method, not an event):**
   - **Android:** add an entry to `CleverTapEvent.kt` (name + bufferable flag); emit it from `CleverTapListenerProxy` when the native SDK callback fires, using `CleverTapEventEmitter.sendEvent(...)`.
   - **iOS:** add the event name to the `supportedEvents` array in `CleverTapReact.mm`; emit via `[self sendEventWithName:body:]` (or queue via `CleverTapReactPendingEvent` if listeners may not be attached yet).
   - **JS:** re-export the event constant on the `CleverTap` object in `src/index.js`.

10. **Example app demo** -- in `Example/app/constants.js`, add an `Actions` key for the new method. In `Example/app/app-utils.js`, add a handler function that calls `CleverTap.<newMethod>(...)`. In `Example/app/App.js`, ensure the new action is wired into the action menu/list.

11. **Documentation** -- update `docs/usage.md` with an entry under the appropriate feature section. For new events, also document the callback payload shape in `docs/callbackPayloadFormat.md`.

12. **Changelog & version** -- add an entry under "Unreleased" (or your release branch's section) in `CHANGELOG.md`. Do NOT bump the version in `package.json` unless you are doing a release.

## File-touch summary

A new method (no event) touches these files (typical case, ~8 files):

```
src/NativeCleverTapModule.ts                                   [edit]
src/index.js                                                   [edit]
src/index.d.ts                                                 [edit]
android/src/main/java/com/clevertap/react/CleverTapModuleImpl.java   [edit]
android/src/oldarch/CleverTapModule.kt                         [edit]
android/src/newarch/CleverTapModule.kt                         [edit]
ios/CleverTapReact/CleverTapReact.mm                           [edit]
Example/app/constants.js                                       [edit]
Example/app/app-utils.js                                       [edit]
docs/usage.md                                                  [edit]
CHANGELOG.md                                                   [edit]
```

A new event additionally touches:

```
android/src/main/java/com/clevertap/react/CleverTapEvent.kt    [edit, +1 enum entry]
android/src/main/java/com/clevertap/react/CleverTapListenerProxy.kt   [edit, emit site]
ios/CleverTapReact/CleverTapReact.mm                           [edit, +supportedEvents entry + emit site]
src/index.js                                                   [edit, +event constant export]
docs/callbackPayloadFormat.md                                  [edit, +payload schema]
```

## Anti-patterns

- **Forgetting one of the Android arch shims.** This is by far the most common bug. A method declared in `CleverTapModuleImpl` and one shim will work on hosts using one architecture and silently fail on the other. Always edit BOTH `android/src/oldarch/CleverTapModule.kt` (with `@ReactMethod`) and `android/src/newarch/CleverTapModule.kt` (with `override`).
- **Putting `@ReactMethod` on the method in `CleverTapModuleImpl.java`.** The Impl is not a `ReactContextBaseJavaModule`; the annotation does nothing there. The bridge surface lives on the SHIMS, not on the Impl.
- **Adding an event but skipping `supportedEvents` on iOS.** RN emits a runtime warning and drops the event. Symptom: works on Android, silent on iOS.
- **Mismatched event name across the three layers.** The string MUST be byte-identical in `CleverTapEvent.kt` (`eventName`), in iOS `supportedEvents`, and in the `src/index.js` constant export.
- **Adding business logic in any of the bridge files.** The RN SDK is a thin wrapper. New behavior belongs in the underlying native SDK. Bridge files only marshal types and dispatch.
- **Using `[self sendEventWithName:...]` unconditionally on iOS.** Events fired before JS attaches (e.g., during `application:didFinishLaunchingWithOptions:`) are lost. Use the pending-event queue.
- **Inconsistent signature between Android and iOS.** If the Android `@ReactMethod` takes `(Callback)` and the iOS uses `resolver/rejecter`, the JS wrapper has to bridge the difference — keep both platforms aligned on the same shape unless there's a strong reason.
- **`isAvailable()` omission for platform-only APIs.** If a method exists on only one platform, the JS wrapper should `Platform.OS`-guard or expose an `isAvailable()` helper so host apps can branch without try/catch.

## Verification

After landing the changes:

1. **Lint** -- `npm run lint` (must pass).
2. **Android smoke test** -- `cd Example && yarn android`. Open the action menu, find your new action, tap it, watch `adb logcat -s CleverTap CleverTapReact ReactNativeJS` for the expected output.
3. **iOS smoke test** -- `cd Example && yarn ios`. Same flow via Xcode console.
4. **Listener test (events only)** -- attach `CleverTap.addListener('YourEvent', ...)` in `Example/app/App.js`, trigger the native SDK path that emits the event, confirm the handler fires on both platforms.
5. **Cross-arch test (Android only)** -- if your host environment supports it, flip the `newArchEnabled` gradle flag in `Example/android/gradle.properties` and re-run to confirm both arch shims work.
6. **Type-check the JS** -- the `.d.ts` is the source of truth for host-app TypeScript users; a quick `tsc --noEmit` against the example will catch declaration mistakes.

## When in doubt

- Bridge mechanics → [`clevertap-react-native`](../clevertap-react-native/SKILL.md)
- Android-only details → [`clevertap-react-native-android`](../clevertap-react-native-android/SKILL.md)
- iOS-only details → [`clevertap-react-native-ios`](../clevertap-react-native-ios/SKILL.md)
- Payload shapes → `docs/callbackPayloadFormat.md`
