# iOS Bridge — Workflows

## 1. Add a new `RCT_EXPORT_METHOD` to the iOS bridge

**Step 1 — Choose the signature shape.**

```objective-c
// Synchronous side-effect (no return)
RCT_EXPORT_METHOD(doThing:(NSString *)arg) {
    [[CleverTap sharedInstance] doThing:arg];
}

// Callback (Node-style)
RCT_EXPORT_METHOD(getThing:(RCTResponseSenderBlock)callback) {
    id value = [[CleverTap sharedInstance] getThing];
    callback(@[[NSNull null], value ?: [NSNull null]]);
}

// Promise
RCT_EXPORT_METHOD(getThingAsync:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [[CleverTap sharedInstance] getThingAsyncWithCompletion:^(id value, NSError *err) {
        if (err) reject(@"E_THING", err.localizedDescription, err);
        else     resolve(value);
    }];
}

// NSDictionary argument
RCT_EXPORT_METHOD(doThingWithMap:(NSDictionary *)arg) {
    [[CleverTap sharedInstance] doThingWithDictionary:arg];
}
```

**Step 2 — Decide between callback and promise.** Match the Android side's choice — the JS wrapper expects the same shape on both platforms.

**Step 3 — Mind the threading.** `RCT_EXPORT_METHOD` runs on the module's GCD queue by default (`+ (dispatch_queue_t)methodQueue` returns the bridge's main JS-thread-adjacent queue). If you must touch UIKit, dispatch onto `dispatch_get_main_queue()`.

## 2. Add a new event

**Step 1 — Add the event name to `supportedEvents`.** Open `CleverTapReact.mm` and find the `supportedEvents` method:

```objective-c
- (NSArray<NSString *> *)supportedEvents {
    return @[
        // … existing events …
        @"CleverTapFeatureX",
    ];
}
```

**Step 2 — Re-export via `constantsToExport`.** The constants dictionary returns the same event names so host apps can use them as `CleverTap.CleverTapFeatureX`:

```objective-c
+ (NSDictionary *)constantsToExport {
    NSMutableDictionary *constants = [super constantsToExport mutableCopy] ?: [@{} mutableCopy];
    constants[@"CleverTapFeatureX"] = @"CleverTapFeatureX";
    return constants;
}
```

**Step 3 — Fire from the appropriate callback site.** This is usually a CleverTap-SDK delegate method or a presenter callback. Use the `sendOrQueueEvent` pattern:

```objective-c
- (void)onFeatureX:(SomePayload *)payload {
    [self sendOrQueueEvent:@"CleverTapFeatureX"
                      body:@{ @"key": payload.value ?: [NSNull null] }];
}
```

Where `sendOrQueueEvent:body:` is:

```objective-c
- (void)sendOrQueueEvent:(NSString *)name body:(id)body {
    if (hasListeners) {
        [self sendEventWithName:name body:body];
    } else {
        [CleverTapReactPendingEvent enqueueWithName:name body:body];
    }
}
```

**Step 4 — Re-export the constant on the JS side** (`src/index.js`) and add a row in `CleverTapEvent.kt` on Android. All three must be byte-identical.

**Step 5 — Document the payload** in `docs/callbackPayloadFormat.md`.

## 3. Bump `CleverTap-iOS-SDK` version

**Step 1 — Update the podspec.** Edit `clevertap-react-native.podspec`:

```ruby
s.dependency 'CleverTap-iOS-SDK', 'X.Y.Z'
```

**Step 2 — Re-resolve in the Example app.**

```bash
cd Example/ios
pod update CleverTap-iOS-SDK
cd ..
yarn ios
```

If the underlying SDK changed major version, run `pod install --repo-update` instead.

**Step 3 — Check for compile failures.** Common breakage: a delegate protocol gained or removed methods. Search `CleverTapReact.mm` and the presenters for the affected protocol and adjust.

**Step 4 — Smoke-test push, in-app, inbox, variables, custom templates** through the Example app's action menu.

**Step 5 — Update CHANGELOG.** "Updated CleverTap-iOS-SDK to X.Y.Z" + any visible behavior change.

## 4. Integrate a notification service extension

The Example app has Swift and ObjC reference targets:

- `Example/ios/NotificationServiceSwift/` — pre-deliver modification (decryption, asset download)
- `Example/ios/NotificationContentSwift/` — custom in-banner UI for rich notifications

For a host app integrating CleverTap rich push:

**Step 1 — Add a Notification Service Extension target** in Xcode.

**Step 2 — Add the `CTNotificationService` pod or the Swift package** (separate from `CleverTap-iOS-SDK`).

**Step 3 — In the extension's `NotificationService.swift`**, call `CleverTapNotificationService` helpers (see the Swift sample).

**Step 4 — Wire the App Group** so the extension and host app share state.

Reference: `docs/iospushtemplates.md`.

## 5. Debug an iOS-side crash from RN

**Step 1 — Watch the Xcode console.** Or run from the terminal:

```bash
xcrun simctl spawn booted log stream --predicate \
  'process == "Example" OR process == "Example-iOS"' --level=debug
```

**Step 2 — Look for `RCTFatal` or NSException.** RN wraps native exceptions and rejects the bridge call; the exception name and stack identify the failing method.

**Step 3 — Common causes.**
- Calling `[self sendEventWithName:body:]` with a name not in `supportedEvents` → RN warning, event dropped (silent failure, not a crash).
- `NSInvalidArgumentException: -[NSNull length]` → upstream sent a `nil` where a non-null is expected; convert with `?: [NSNull null]` at the bridge boundary.
- `EXC_BAD_ACCESS` in a presenter → presenter was registered with the SDK but deallocated when the bridge reloaded; ensure strong reference on the bridge module.

**Step 4 — Check AppDelegate wiring.** If push doesn't deliver to the listener, confirm the host AppDelegate is calling `CleverTapReactManager` for `didReceiveRemoteNotification:` and `userNotificationCenter:didReceive:`.

## 6. Handle a pending event before JS listens

This is the iOS-specific buffering pattern.

**Step 1 — All event emission must go through `sendOrQueueEvent:body:`.** Avoid direct `[self sendEventWithName:body:]` calls.

**Step 2 — Confirm `startObserving` flushes the queue.** Open `CleverTapReact.mm` and verify:

```objective-c
- (void)startObserving {
    hasListeners = YES;
    for (NSDictionary *event in [CleverTapReactPendingEvent pendingEvents]) {
        [self sendEventWithName:event[@"name"] body:event[@"body"]];
    }
    [CleverTapReactPendingEvent clear];
}

- (void)stopObserving {
    hasListeners = NO;
}
```

**Step 3 — Validate by cold-launch via push.** Tap a push when the app is not running — the click event must reach the JS handler attached during `App.js` mount.

## 7. Build the framework locally

You don't normally need to. The Pod build runs as part of the host app's `pod install`. For debugging compilation issues in isolation:

```bash
cd Example/ios
pod install
xcodebuild -workspace CleverTapReactNativeExample.xcworkspace \
           -scheme CleverTapReactNativeExample \
           -configuration Debug -sdk iphonesimulator
```
