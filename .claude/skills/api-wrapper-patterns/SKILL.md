---
name: api-wrapper-patterns
description: Standard patterns for wrapping native CleverTap Android/iOS SDK APIs in React Native. Use when adding new native SDK APIs, updating existing API signatures, implementing cross-platform features, or creating React Native NativeModule bridges.
---

# API Wrapper Patterns

> **MANDATORY READING**: This skill document MUST be read in full BEFORE implementing any API wrapper. Do NOT infer patterns from existing code - follow THIS document exactly.

Standard patterns for wrapping native CleverTap Android/iOS SDK APIs in React Native with proper NativeModules, error handling, and type conversions.

## Core Principle: Keep JS Simple

The JavaScript layer should be a **thin pass-through**. All data transformation, type conversion, and complex logic should happen in the native layers (Android/iOS), not in JavaScript.

**Why?**
- Native layers have direct access to SDK types and utilities
- Reduces code duplication between JS type handling
- Keeps the public API surface clean and predictable
- Avoids runtime type errors in JavaScript

## Decision Tree

```
Is this a public API that apps call directly?
|-- YES -> Does it already exist in React Native?
|  |-- YES -> Does signature need updating?
|  |  |-- YES -> UPDATE existing wrapper
|  |  +-- NO -> NO ACTION
|  +-- NO -> Is this commonly used functionality?
|     |-- YES -> CREATE new wrapper
|     +-- NO -> DISCUSS with user
+-- NO (internal/config/payload) -> NO wrapper needed
```

## Type Mapping

| Kotlin                  | Objective-C | JavaScript       | TypeScript             |
|-------------------------|-------------|------------------|------------------------|
| `Map<String, Any>`      | `NSDictionary` | `Object`      | `Record<string, any>`  |
| `List`                  | `NSArray` | `Array`            | `any[]`                |
| `List<Map<String, Any>>` | `NSArray<NSDictionary *> *` | `Array` | `any[]`       |
| `String`                | `NSString` | `string`          | `string`               |
| `Int`, `Long`           | `NSNumber` | `number`          | `number`               |
| `Double`                | `NSNumber` | `number`          | `number`               |
| `Boolean`               | `BOOL` | `boolean`            | `boolean`              |

**Key Rule**: For complex return types like arrays of objects, always use simple types in JavaScript. Don't attempt to cast or transform the data in the JS layer.

## Code Style Guidelines

### JavaScript Layer (IMPORTANT: Keep It Simple)
- Methods on the default `CleverTap` export object in `src/index.js`
- **AVOID transformations** - pass data directly to/from native without mapping
- **Use simple types** - prefer `Array` over typed arrays for complex returns
- Let native layers handle all data conversion and formatting
- Use `callWithCallback()` helper for async operations

**DO:**
```javascript
getAllInboxMessages: function(callback) {
  callWithCallback('getAllInboxMessages', null, callback);
},
```

**DON'T:**
```javascript
getAllInboxMessages: async function() {
  const result = await CleverTapModule.getAllInboxMessages();
  if (!result) return [];
  return result.map(msg => ({ ...msg, date: new Date(msg.date) }));  // Avoid this!
},
```

### TypeScript Definitions (src/index.d.ts)
- Every public method must have a TypeScript definition
- Use `Callback` type for callback parameters
- Document parameters with JSDoc comments

### TurboModule Spec (src/NativeCleverTapModule.ts)
- Update when adding new native methods
- Follow existing patterns for method signatures
- Use proper TurboModule types

### Naming
- **JavaScript**: camelCase (`recordEvent`, `getCleverTapID`)
- **Android**: camelCase for methods, UPPER_SNAKE_CASE for constants
- **iOS**: camelCase for methods

### Error Handling

Always include:
1. Null checks for required parameters
2. CleverTap API instance null check
3. Descriptive error messages

### Documentation

Every public JavaScript method should be documented in TypeScript definitions:
```typescript
/**
 * Brief description
 * @param param1 - Description
 * @param param2 - Description (optional)
 * @param callback - Callback function receiving the result
 */
```

---

## Implementation Rules

> **CRITICAL**: Read and follow these rules BEFORE writing any code.

### Rule 1: Optional Parameters - Extend Existing APIs

**When a native SDK adds an optional parameter to an existing API, add the same optional parameter to the existing JS method instead of creating a new method.**

### Rule 2: Platform-Specific APIs

**When an API exists only on one platform:**
- Still implement the JS method
- Add platform check in JS OR have native side return gracefully
- Document the platform limitation in the TypeScript definition

### Rule 3: Update All 5 Layers

For every new API, update:
1. `src/index.js` - JS implementation
2. `src/index.d.ts` - TypeScript definition
3. `src/NativeCleverTapModule.ts` - TurboModule spec
4. `android/.../CleverTapModuleImpl.java` - Android implementation
5. `ios/CleverTapReact/CleverTapReact.mm` - iOS implementation

## Pattern 1: Simple Method (No Return Value)

**Use case**: Method that triggers an action but doesn't return data.

**Example**: `recordEvent(eventName, properties)`

### JavaScript Layer (`src/index.js`)

```javascript
recordEvent: function(eventName, properties) {
  CleverTapModule.recordEvent(eventName, properties);
},
```

### TypeScript Definition (`src/index.d.ts`)

```typescript
/**
 * Records an event with the given name
 * @param eventName - The name of the event to record
 * @param properties - Optional properties for the event
 */
recordEvent(eventName: string, properties?: Record<string, any>): void;
```

### Android Layer (`CleverTapModuleImpl.java`)

```java
@ReactMethod
public void recordEvent(String eventName, ReadableMap properties) {
    CleverTapAPI clevertap = getCleverTapAPI();
    if (clevertap == null) return;

    Map<String, Object> props = CleverTapUtils.readableMapToMap(properties);
    clevertap.pushEvent(eventName, props);
}
```

### iOS Layer (`CleverTapReact.mm`)

```objectivec
RCT_EXPORT_METHOD(recordEvent:(NSString *)eventName properties:(NSDictionary *)properties) {
    [[CleverTap sharedInstance] recordEvent:eventName withProps:properties];
}
```

## Pattern 2: Method with Callback Return Value

**Use case**: Method that retrieves data from native SDK asynchronously.

**Example**: `profileGetProperty(propertyName, callback)`

### JavaScript Layer

```javascript
profileGetProperty: function(propertyName, callback) {
  callWithCallback('profileGetProperty', [propertyName], callback);
},
```

### TypeScript Definition

```typescript
/**
 * Gets the value of a user profile property
 * @param propertyName - The name of the property to retrieve
 * @param callback - Callback receiving the property value or null
 */
profileGetProperty(propertyName: string, callback: Callback): void;
```

### Android Layer

```java
@ReactMethod
public void profileGetProperty(String propertyName, Callback callback) {
    CleverTapAPI clevertap = getCleverTapAPI();
    if (clevertap == null) {
        callback.invoke(null);
        return;
    }

    Object value = clevertap.getProperty(propertyName);
    callback.invoke(value);
}
```

### iOS Layer

```objectivec
RCT_EXPORT_METHOD(profileGetProperty:(NSString *)propertyName callback:(RCTResponseSenderBlock)callback) {
    id value = [[CleverTap sharedInstance] profileGet:propertyName];
    callback(@[value ?: [NSNull null]]);
}
```

## Pattern 3: Method Returning Complex Data

**Use case**: Method that returns objects/collections from native SDKs.

**Example**: `getAllInboxMessages(callback)` returns array of inbox messages

### JavaScript Layer

```javascript
getAllInboxMessages: function(callback) {
  callWithCallback('getAllInboxMessages', null, callback);
},
```

### TypeScript Definition

```typescript
/**
 * Retrieves all inbox messages
 * @param callback - Callback receiving array of inbox message objects
 */
getAllInboxMessages(callback: Callback): void;
```

### Android Layer

```java
@ReactMethod
public void getAllInboxMessages(Callback callback) {
    CleverTapAPI clevertap = getCleverTapAPI();
    if (clevertap == null) {
        callback.invoke(null);
        return;
    }

    ArrayList<CTInboxMessage> messages = clevertap.getAllInboxMessages();
    WritableArray result = CleverTapUtils.inboxMessagesToWritableArray(messages);
    callback.invoke(result);
}
```

### iOS Layer

```objectivec
RCT_EXPORT_METHOD(getAllInboxMessages:(RCTResponseSenderBlock)callback) {
    NSArray *messages = [[CleverTap sharedInstance] getAllInboxMessages];
    NSArray *results = [self _cleverTapInboxMessagesToArray:messages];
    callback(@[results ?: @[]]);
}
```

## Common Issues

### Issue 1: Method Not Found
**Symptom**: `TypeError: CleverTapModule.methodName is not a function`
**Cause**: Method name mismatch or missing native registration
**Solution**: Verify exact string match across all files, ensure `@ReactMethod` / `RCT_EXPORT_METHOD`

### Issue 2: Type Conversion Error
**Symptom**: Bridge conversion error
**Cause**: JS type doesn't map to native type properly
**Solution**: Check type mapping table above, use `ReadableMap`/`WritableMap` for Android

### Issue 3: Callback Issues
**Symptom**: Callback never fires or fires with wrong data
**Cause**: Missing callback invocation in native code
**Solution**: Always invoke callback, even with null for error cases

### Issue 4: Architecture Compatibility
**Symptom**: Method works on Old Arch but not New Arch (or vice versa)
**Cause**: Missing TurboModule spec update
**Solution**: Update `src/NativeCleverTapModule.ts` alongside native implementations
