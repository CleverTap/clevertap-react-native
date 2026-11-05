---
name: api-wrapper-patterns
description: Standard patterns for wrapping native CleverTap Android/iOS SDK APIs in React Native. Use when adding new native SDK APIs, updating existing API signatures, implementing cross-platform features, or creating React Native NativeModule bridges.
---

# API Wrapper Patterns

> **MANDATORY READING**: This skill document MUST be read in full BEFORE implementing any API wrapper. The codebase has specific patterns that must be followed exactly.

## Core Principle

The JavaScript layer is a **thin pass-through**. All data transformation, type conversion, and complex logic happen in native layers (Android/iOS), not JavaScript.

## The 7 Files To Update

Every new API requires changes across up to 7 files. The first 5 are always required. The arch modules (6-7) are thin delegates to CleverTapModuleImpl.

| # | File | Role |
|---|------|------|
| 1 | `src/index.js` | Public JS API - method on `CleverTap` object |
| 2 | `src/index.d.ts` | TypeScript definitions |
| 3 | `src/NativeCleverTapModule.ts` | TurboModule spec (codegen) |
| 4 | `android/.../CleverTapModuleImpl.java` | Android implementation (actual logic) |
| 5 | `ios/CleverTapReact/CleverTapReact.mm` | iOS implementation |
| 6 | `android/src/oldarch/CleverTapModule.kt` | Old Architecture delegate (`@ReactMethod`) |
| 7 | `android/src/newarch/CleverTapModule.kt` | New Architecture delegate (`override fun`) |

### Android Architecture Split

The Android side uses 3 files in a delegation pattern:

```
CleverTapModule.kt (oldarch OR newarch)
    └── delegates to → CleverTapModuleImpl.java (shared implementation)
```

- **`CleverTapModuleImpl.java`**: Contains ALL actual logic. Gets `CleverTapAPI` instance, calls native SDK, converts types.
- **`oldarch/CleverTapModule.kt`**: Extends `ReactContextBaseJavaModule`. Methods use `@ReactMethod` annotation. Delegates every call to `cleverTapModuleImpl`.
- **`newarch/CleverTapModule.kt`**: Extends `NativeCleverTapModuleSpec` (codegen). Methods use `override fun`. Delegates every call to `cleverTapModuleImpl`.

**Key difference between arch modules**: In newarch, numeric params come as `Double` (from codegen) and need `.toInt()` conversion. In oldarch, they come as the actual type (`Int`, `Boolean`). Nullable booleans use `Boolean?` in both.

## Pattern A: Void Method (No Return)

Use for methods that trigger an action with no callback.

### src/index.js

```javascript
suspendInAppNotifications: function () {
    CleverTapReact.suspendInAppNotifications();
},
```

### src/index.d.ts

```typescript
/**
 * Suspends display of InApp Notifications.
 */
suspendInAppNotifications(): void;
```

### src/NativeCleverTapModule.ts

```typescript
suspendInAppNotifications(): void;
```

### CleverTapModuleImpl.java

```java
public void suspendInAppNotifications() {
    CleverTapAPI cleverTap = getCleverTapAPI();
    if (cleverTap != null) {
        cleverTap.suspendInAppNotifications();
    }
}
```

### CleverTapReact.mm

```objectivec
RCT_EXPORT_METHOD(suspendInAppNotifications) {
    RCTLogInfo(@"[CleverTap suspendInAppNotifications]");
    [[self cleverTapInstance] suspendInAppNotifications];
}
```

### oldarch/CleverTapModule.kt

```kotlin
@ReactMethod
fun suspendInAppNotifications() {
    cleverTapModuleImpl.suspendInAppNotifications()
}
```

### newarch/CleverTapModule.kt

```kotlin
override fun suspendInAppNotifications() {
    cleverTapModuleImpl.suspendInAppNotifications()
}
```

## Pattern B: Callback Method (Returns Data)

Use for methods that retrieve data asynchronously.

### src/index.js

Uses the `callWithCallback` helper which handles null callbacks and argument assembly:

```javascript
variants: function (callback) {
    callWithCallback('variants', null, callback);
},
```

With arguments:

```javascript
getUserEventLog: function (eventName, callback) {
    callWithCallback('getUserEventLog', [eventName], callback);
},
```

**How `callWithCallback` works**: It wraps the callback with a default logger if null, assembles args array + callback, then calls `CleverTapReact[method].apply(this, args)`.

### src/index.d.ts

```typescript
/**
 * Returns information about the active variants for the current user.
 * @param callback - Callback receiving array of variant objects
 */
variants(callback: Callback): void;
```

### src/NativeCleverTapModule.ts

```typescript
variants(callback: ((error: Object, result: boolean) => void) | null): void;
```

Note: The TurboModule spec always uses `((error: Object, result: boolean) => void) | null` for callbacks regardless of actual return type.

### CleverTapModuleImpl.java

Uses `callbackWithErrorAndResult(callback, error, result)` helper:

```java
public void variants(final Callback callback) {
    WritableArray result = null;
    String error = null;
    CleverTapAPI cleverTap = getCleverTapAPI();
    if (cleverTap != null) {
        List<Map<String, Object>> variantsList = cleverTap.variants();
        result = variantsToWritableArray(variantsList);
    } else {
        error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
    }
    callbackWithErrorAndResult(callback, error, result);
}
```

**Pattern**: Declare `error = null` and `result = null/default`, populate one based on success/failure, call `callbackWithErrorAndResult` at the end. This helper invokes `callback.invoke(error, result)`.

### CleverTapReact.mm

Uses `[self returnResult:result withCallback:callback andError:nil]` helper:

```objectivec
RCT_EXPORT_METHOD(variants:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap variants]");
    NSArray<NSDictionary<NSString*,id>*> *variants = [[self cleverTapInstance] variants];
    [self returnResult:variants withCallback:callback andError:nil];
}
```

**Pattern**: Call native SDK, pass result to `returnResult:withCallback:andError:`. This helper calls `callback(@[error, result])` with NSNull for nils.

For async iOS operations, wrap in dispatch_async:

```objectivec
RCT_EXPORT_METHOD(getUserEventLog:(NSString*)eventName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getUserEventLog: %@]", eventName);
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        CleverTapEventDetail *detail = [[self cleverTapInstance] getUserEventLog:eventName];
        NSDictionary *result = [self _eventDetailToDict:detail];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self returnResult:result withCallback:callback andError:nil];
        });
    });
}
```

### Arch Modules (both identical delegation)

```kotlin
// oldarch
@ReactMethod
fun variants(callback: Callback?) {
    cleverTapModuleImpl.variants(callback)
}

// newarch
override fun variants(callback: Callback?) {
    cleverTapModuleImpl.variants(callback)
}
```

## Pattern C: Optional Parameter (Extending Existing API)

When a native SDK adds an optional parameter to an existing method, add it as a defaulted parameter in JS and nullable in native.

### src/index.js

```javascript
discardInAppNotifications: function (dismissInAppIfVisible = false) {
    CleverTapReact.discardInAppNotifications(dismissInAppIfVisible);
},
```

### src/index.d.ts

```typescript
/**
 * Discards InApp Notifications.
 * @param dismissInAppIfVisible - Optional. If true, dismisses any currently visible InApp.
 */
discardInAppNotifications(dismissInAppIfVisible?: boolean): void;
```

### src/NativeCleverTapModule.ts

```typescript
discardInAppNotifications(dismissInAppIfVisible?: boolean): void;
```

### CleverTapModuleImpl.java

Use method overloading or nullable Boolean:

```java
public void discardInAppNotifications(Boolean dismissInAppIfVisible) {
    CleverTapAPI cleverTap = getCleverTapAPI();
    if (cleverTap != null) {
        if (dismissInAppIfVisible != null) {
            cleverTap.discardInAppNotifications(dismissInAppIfVisible);
        } else {
            cleverTap.discardInAppNotifications();
        }
    }
}
```

### CleverTapReact.mm

```objectivec
RCT_EXPORT_METHOD(discardInAppNotifications:(BOOL)dismissInAppIfVisible) {
    RCTLogInfo(@"[CleverTap discardInAppNotifications: %d]", dismissInAppIfVisible);
    [[self cleverTapInstance] discardInAppNotifications:dismissInAppIfVisible];
}
```

### Arch Modules

```kotlin
// oldarch
@ReactMethod
fun discardInAppNotifications(dismissInAppIfVisible: Boolean?) {
    cleverTapModuleImpl.discardInAppNotifications(dismissInAppIfVisible)
}

// newarch
override fun discardInAppNotifications(dismissInAppIfVisible: Boolean?) {
    cleverTapModuleImpl.discardInAppNotifications(dismissInAppIfVisible)
}
```

## Pattern D: Listener Registration

For events the JS layer subscribes to via native event emitter.

### src/index.js

```javascript
onVariablesChanged: function (handler) {
    CleverTapReact.onVariablesChanged();
    this.addListener(CleverTapReact.getConstants().CleverTapOnVariablesChanged, handler);
},
```

**Pattern**: Call native to register, then add JS listener for the event constant. Use `addOneTimeListener` for one-shot callbacks.


## Type Mapping

| Kotlin | Objective-C | JavaScript | TurboModule Spec |
|--------|-------------|------------|------------------|
| `String` | `NSString*` | `string` | `string` |
| `Int` | `int` / `NSInteger` | `number` | `number` (Double in newarch) |
| `Boolean` | `BOOL` | `boolean` | `boolean` |
| `Boolean?` | N/A | `boolean` (optional) | `boolean` (optional) |
| `ReadableMap` | `NSDictionary*` | `Object` | `Object \| null` |
| `ReadableArray` | `NSArray*` | `Array` | `string[]` |
| `Callback` | `RCTResponseSenderBlock` | `function(err, res)` | `((error: Object, result: boolean) => void) \| null` |
| `Promise` | `RCTPromiseResolveBlock` | `Promise` | `Promise<T>` |
| `WritableMap` | `NSDictionary*` | (return) Object | - |
| `WritableArray` | `NSArray*` | (return) Array | - |

**Newarch numeric conversion**: TurboModule codegen passes all numbers as `Double`. Convert in newarch module: `importance.toInt()`. Oldarch receives native types directly.

## Type Compatibility Verification

Before implementing or updating a wrapper, verify that the React Native bridge types correctly carry the data the native SDK expects. This is especially important when native SDK changelogs mention changes to data handling without adding new APIs.

### Bridge Type Capabilities

| Bridge Type | Supports Nesting | Supports Mixed Types | Notes |
|-------------|-------------------|----------------------|-------|
| `ReadableMap` / `NSDictionary` | Yes — nested maps via `getMap(key)` | Yes — string, number, boolean, map, array | JS objects pass through as-is including nested structures |
| `ReadableArray` / `NSArray` | Yes — nested arrays and maps | Yes — mixed element types | JS arrays pass through as-is |
| `String` / `NSString` | N/A | N/A | Primitives always safe |
| `int`, `double`, `boolean` | N/A | N/A | Primitives always safe |

### What Can Go Wrong

1. **Manual flattening in the wrapper**: If the wrapper calls `toHashMap()` or iterates over keys and converts individually, nested structures may be lost or incorrectly converted. Check that the wrapper passes data through without unnecessary transformation.

2. **Type narrowing**: If the wrapper declares a parameter as `String` but the native SDK now accepts `Map<String, Object>` (e.g., supporting both simple and structured values), the wrapper needs a signature update.

3. **Unsupported types**: The bridge does not auto-convert `Date`, `byte[]`, custom objects, or enums. These need explicit conversion in the wrapper layer (e.g., `Date` → epoch seconds, enums → string/int constants).

4. **Null handling differences**: Android `ReadableMap.getMap(key)` returns null for missing keys; iOS `NSDictionary[key]` returns nil. Both are safe, but if the wrapper has explicit null checks that reject valid null values, it may break.

### Verification Checklist

When a native SDK change affects data types flowing through an existing wrapper:

- [ ] Read the current wrapper implementation across all layers (JS, Android, iOS)
- [ ] Confirm the bridge types used can carry the new data shapes (use table above)
- [ ] Check for any manual type conversion that might strip structure (e.g., `toHashMap()`, key-by-key iteration, `JSON.stringify()`)
- [ ] Verify parameter types aren't too restrictive for the new native SDK expectations
- [ ] Test with representative data: pass the new data shape from JS through to the native SDK and confirm it arrives intact

### Common Scenarios

| Changelog Says | Check This | Likely Outcome |
|----------------|------------|----------------|
| "Supports nested objects in events/properties" | `pushEvent` / `profilePush` — does `ReadableMap` pass nested maps? | Usually OK — `ReadableMap` supports nesting natively |
| "Now accepts array values in properties" | Property-setting methods — does the wrapper handle arrays in map values? | Usually OK — `ReadableMap` supports array values |
| "New property type support (e.g., Date)" | All property methods — does the wrapper convert the new type? | Likely UPDATE — bridge doesn't auto-convert dates |
| "Changed parameter from String to Map" | Specific method — is the wrapper parameter type updated? | UPDATE — wrapper signature must change |
| "Accepts null/optional for previously required param" | Specific method — is the wrapper param nullable? | VERIFY — may need `?` added to param type |

## Helper Methods

### JavaScript: `callWithCallback(method, args, callback)`

- Wraps null callbacks with a default logger
- Assembles args array and appends callback
- Calls `CleverTapReact[method].apply(this, args)`
- Use `null` for args when method takes no params besides callback

### JavaScript: `convertDateToEpochInProperties(map)`

- Converts Date objects to `"$D_" + epochSeconds` format
- Call before passing profile/event properties to native

### Android: `callbackWithErrorAndResult(callback, error, result)`

- Null-safe callback invocation
- Calls `callback.invoke(error, result)`
- Always use this instead of raw `callback.invoke()`

### Android: `getCleverTapAPI()`

- Returns cached `CleverTapAPI` instance or gets default
- Always null-check the return value

### iOS: `returnResult:withCallback:andError:`

- Null-safe callback invocation
- Converts nils to `[NSNull null]`
- Calls `callback(@[error, result])`

### iOS: `[self cleverTapInstance]`

- Returns the CleverTap shared instance
- Used in every method implementation

## Implementation Checklist

When adding a new API wrapper:
- [ ] Method added to `CleverTap` object in `src/index.js`
- [ ] TypeScript definition added to `src/index.d.ts`
- [ ] Method added to TurboModule spec in `src/NativeCleverTapModule.ts`
- [ ] Implementation added to `CleverTapModuleImpl.java`
- [ ] Delegate added to `oldarch/CleverTapModule.kt` with `@ReactMethod`
- [ ] Delegate added to `newarch/CleverTapModule.kt` with `override fun`
- [ ] Implementation added to `CleverTapReact.mm` with `RCT_EXPORT_METHOD`
- [ ] Method name is identical across all layers (case-sensitive)
- [ ] Callback methods use `callWithCallback` in JS, `callbackWithErrorAndResult` in Android, `returnResult:withCallback:andError:` in iOS
- [ ] Bridge types support the data shapes the native SDK expects (see Type Compatibility Verification)
- [ ] No manual type conversion strips nested structure or narrows types beyond what the native SDK accepts
