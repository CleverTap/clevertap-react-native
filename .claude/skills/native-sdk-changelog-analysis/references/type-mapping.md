# Type Mapping Reference

Cross-platform type mappings for Android, iOS, and JavaScript/TypeScript.

## Native Type Mapping Table

| Android Type | iOS Type | JavaScript Type | TypeScript Type |
|--------------|----------|-----------------|-----------------|
| `ArrayList<T>`, `List<T>` | `NSArray<T *> *` | `Array` | `any[]` |
| `Map<K, V>`, `HashMap<K, V>` | `NSDictionary<K, V> *` | `Object` | `Record<string, any>` |
| `String` | `NSString *` | `string` | `string` |
| `Integer`, `Long` | `NSNumber *`, `NSInteger` | `number` | `number` |
| `Double` | `NSNumber *` | `number` | `number` |
| `boolean` | `BOOL` | `boolean` | `boolean` |
| `void` | `void` | `undefined` | `void` |
| `Object` | `id` | `any` | `any` |
| `ArrayList<HashMap>` | `NSArray<NSDictionary *> *` | `Array` | `any[]` |

## React Native Bridge Type Mapping

| Android (Java/Kotlin) | React Native Bridge | JavaScript |
|------------------------|---------------------|------------|
| `ReadableMap` | `NSDictionary` | `Object` |
| `ReadableArray` | `NSArray` | `Array` |
| `WritableMap` | `NSDictionary` | `Object` |
| `WritableArray` | `NSArray` | `Array` |
| `String` | `NSString` | `string` |
| `int` / `Integer` | `NSNumber` | `number` |
| `double` / `Double` | `NSNumber` | `number` |
| `boolean` / `Boolean` | `BOOL` / `NSNumber` | `boolean` |
| `Callback` | `RCTResponseSenderBlock` | `Function` |

## Common CleverTap Type Mappings

| Android Type | iOS Type |
|--------------|----------|
| `CTInboxMessage` | `CleverTapInboxMessage` |
| `ArrayList<CTInboxMessage>` | `NSArray<CleverTapInboxMessage *> *` |
| `CTInboxStyleConfig` | `CleverTapInboxStyleConfig` |

## Cross-Platform Inference Rules

When signature found for ONE platform but not the other:

### Android Found -> Infer iOS

```
ArrayList<CTInboxMessage> -> NSArray<CleverTapInboxMessage *> *
Map<String, Object> -> NSDictionary<NSString *, id> *
boolean -> BOOL
void -> void
```

### iOS Found -> Infer Android

```
NSArray<CleverTapInboxMessage *> * -> ArrayList<CTInboxMessage>
NSDictionary<NSString *, id> * -> Map<String, Object>
BOOL -> boolean
void -> void
```

## Nullability Annotations

| Android | iOS | Meaning |
|---------|-----|---------|
| `@NonNull` | `_Nonnull` | Never null |
| `@Nullable` | `_Nullable` | May be null |
| (no annotation) | (no annotation) | Assume nullable |

## Documentation Format

When documenting inferred types:

```
// Inferred from Android: ArrayList<CTInboxMessage> -> NSArray<CleverTapInboxMessage *> *
// Inferred from iOS: NSArray<CleverTapInboxMessage *> * -> ArrayList<CTInboxMessage>
// Type verified from CleverTapAPI.java
// Type verified from CleverTap.h
```
