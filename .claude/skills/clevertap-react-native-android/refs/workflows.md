# Android Bridge — Workflows

## 1. Add a new bridge method

**Step 1 — Implement in `CleverTapModuleImpl.java`** as a plain `public` method (NO `@ReactMethod` annotation — the Impl is a plain helper class). Add the method under the section matching its feature (push, profile, inbox, etc.). Choose the signature shape:

```java
// Synchronous side-effect
public void doThing(String arg) {
    getCleverTapAPI().doThing(arg);
}

// Callback-returning (Node-style (error, value))
public void getThing(Callback callback) {
    String value = getCleverTapAPI().getThing();
    callback.invoke(null, value);
}

// Promise-returning
public void getThingAsync(Promise promise) {
    getCleverTapAPI().getThingAsync(
        value -> promise.resolve(value),
        error -> promise.reject(error));
}

// Map argument
public void doThingWithMap(ReadableMap arg) {
    HashMap<String, Object> map = CleverTapUtils.toMap(arg);
    getCleverTapAPI().doThingWithMap(map);
}
```

**Step 2 — Add the `@ReactMethod` forwarder in `android/src/oldarch/CleverTapModule.kt`:**

```kotlin
@ReactMethod
fun doThing(arg: String?) {
    cleverTapModuleImpl.doThing(arg)
}
```

**Step 3 — Add the `override` forwarder in `android/src/newarch/CleverTapModule.kt`:**

```kotlin
override fun doThing(arg: String?) {
    cleverTapModuleImpl.doThing(arg)
}
```

The newarch signature must match the codegen output from `src/NativeCleverTapModule.ts` — declare the method on the TS spec first, then run codegen, then add the `override`.

**Step 4 — (Optional) Lint.** Run `./gradlew :clevertap-react-native:lint` from the `android/` directory if you suspect Kotlin syntax issues.

## 2. Add a new event

**Step 1 — Declare in `CleverTapEvent.kt`.** Add an enum entry:

```kotlin
CLEVERTAP_FEATURE_X(eventName = "CleverTapFeatureX", isBufferable = false),
```

- `eventName` must match the iOS `supportedEvents` string and the JS-side constant byte-for-byte.
- `isBufferable = true` if the event can fire before JS attaches (e.g., during cold start) and is safe to replay.

**Step 2 — Emit from `CleverTapListenerProxy.kt`.** Implement (or extend) the matching SDK listener interface. From its callback method:

```kotlin
override fun onFeatureX(payload: SomePayload) {
    eventEmitter.sendEvent(
        CleverTapEvent.CLEVERTAP_FEATURE_X,
        toWritableMap(payload)
    )
}
```

**Step 3 — Wire the listener registration in `CleverTapModuleImpl`.** If the proxy isn't already passed to the CleverTap SDK for this listener type, register it: `cleverTap.setFeatureXListener(listenerProxy)`.

**Step 4 — Re-export the constant in `src/index.js`.** Find the event constants block and add:

```js
CleverTapFeatureX: 'CleverTapFeatureX',
```

**Step 5 — Document the payload shape** in `docs/callbackPayloadFormat.md`.

## 3. Bump `clevertap-android-sdk` version

**Step 1.** Edit `android/build.gradle`:

```gradle
api 'com.clevertap.android:clevertap-android-sdk:X.Y.Z'
```

The `api` configuration (not `implementation`) intentionally exposes the SDK's public types to host apps so they can keep using `CTInboxStyleConfig`, `CleverTapInstanceConfig`, etc. directly.

**Step 2 — Update Example app pin if any.** Check `Example/android/app/build.gradle`. The example app uses the transitive dep by default but sometimes pins explicitly for testing.

**Step 3 — Smoke test.** `cd Example && yarn android`. Exercise push, in-app, inbox, variables.

**Step 4 — Check for breakage.** If the SDK changed a public type signature, `CleverTapModuleImpl` won't compile. Most common: listener interface methods added or removed.

**Step 5 — Update CHANGELOG.** "Updated clevertap-android-sdk to X.Y.Z" with any visible behavior change.

## 4. Debug a native crash from RN

**Step 1 — Capture the crash.** `adb logcat -s AndroidRuntime CleverTap CleverTapReact ReactNativeJS`. Look for the `FATAL EXCEPTION` block.

**Step 2 — Identify the failing method.** The stack frame in `com.clevertap.react.CleverTapModuleImpl` tells you which bridge method faulted (the topmost shim frame in `com.clevertap.react.CleverTapModule` confirms the same name on the arch shim).

**Step 3 — Check arg marshalling first.** Most crashes are `ClassCastException` or `NullPointerException` when an incoming `ReadableMap` field isn't the type the bridge expected. Use `CleverTapUtils.toMap()` and then null-check.

**Step 4 — Check listener wiring.** If the crash is in `CleverTapListenerProxy`, ensure the proxy hasn't outlived its `ReactContext` — when the host app reloads (dev menu), the bridge tears down but the SDK keeps the listener reference.

**Step 5 — Check threading.** Bridge methods are called on the RN module thread, but SDK callbacks fire on the SDK's executor. If you touch React state outside a `runOnUiQueueThread`, you'll see Android UI-thread assertions.

## 5. Wire an Android-only feature

Some features only exist on Android (e.g., notification channels). Pattern:

**Step 1 — Add the `@ReactMethod` and arch shim entries as usual.**

**Step 2 — In `src/index.js`, gate the call:**

```js
createNotificationChannel(...args) {
    if (Platform.OS !== 'android') {
        console.warn('createNotificationChannel is Android-only');
        return;
    }
    CleverTapReact.createNotificationChannel(...args);
}
```

**Step 3 — Document the platform-only nature** in `docs/usage.md` and as a comment near the `index.d.ts` declaration.

**Step 4 — Provide a no-op or `isAvailable()` helper** if host apps may want to branch instead of conditional logging.

## 6. Run lint on the Android module

```bash
cd android
./gradlew :clevertap-react-native:lintDebug
```

Lint output: `android/build/reports/lint-results-debug.html`. Treat warnings about hardcoded strings and missing `@Nullable` annotations as fixable.

## 7. Build the AAR locally

```bash
cd android
./gradlew :clevertap-react-native:assembleRelease
```

Output: `android/build/outputs/aar/clevertap-react-native-release.aar`. Useful for testing a host app against an unpublished change — drop the AAR into the host's `libs/` and adjust gradle.
