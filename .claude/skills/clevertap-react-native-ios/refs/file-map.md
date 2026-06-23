# iOS Bridge — File Map

All files live in `ios/CleverTapReact/`. Headers (`.h`) are listed alongside their implementations (`.mm`).

| File | Role |
|---|---|
| `CleverTapReact.{h,mm}` | The bridge module. Inherits `RCTEventEmitter`, conforms to `RCTBridgeModule`. Holds ~100 `RCT_EXPORT_METHOD` declarations. Defines `supportedEvents`, `constantsToExport`, `startObserving`/`stopObserving`. Also conditionally provides `getTurboModule:` under `RCT_NEW_ARCH_ENABLED`. |
| `CleverTapReactManager.{h,mm}` | Static class providing AppDelegate-side helpers. Host AppDelegate calls into these from `application:didFinishLaunchingWithOptions:`, `application:didReceiveRemoteNotification:`, and `userNotificationCenter:didReceive:`. Thin pass-throughs to `[CleverTap sharedInstance]` and to the bridge module for event dispatch. |
| `CleverTapReactTemplatePresenter.{h,mm}` | Conforms to the CleverTap-iOS-SDK `CTTemplatePresenter` protocol. On `onPresent:` fires `CleverTapCustomTemplatePresent`; on `onCloseClicked` fires `CleverTapCustomTemplateClose`. Registered via `[CleverTap sharedInstance].customTemplates`. |
| `CleverTapReactCustomTemplates.{h,mm}` | Owns the implementation of every `customTemplateGet{String,Number,Boolean,File,Object}Arg`, `customTemplateRunAction`, `customTemplateSetPresented`, `customTemplateSetDismissed`, `customTemplateContextToString`, `syncCustomTemplates`, `syncCustomTemplatesInProd`. The accessor logic is here so `CleverTapReact.mm` stays focused on the bridge surface. |
| `CleverTapReactAppFunctionPresenter.{h,mm}` | Conforms to `CTAppFunctionPresenter`. Fires `CleverTapCustomFunctionPresent` on activation. Registered via `[CleverTap sharedInstance].appFunctions`. |
| `CleverTapReactPendingEvent.{h,mm}` | Class-level NSMutableArray of `(eventName, body)` tuples for events that fire before JS attaches a listener. `+ enqueueWithName:body:` adds; `+ pendingEvents`/`+ clear` flush during `startObserving`. |

## clevertap-react-native.podspec

```ruby
s.name                = 'clevertap-react-native'
s.platform            = :ios, '9.0'
s.source_files        = 'ios/CleverTapReact/*.{h,m,mm}'
s.dependency          'CleverTap-iOS-SDK', '7.6.0'
s.dependency          'React-Core'
```

The podspec compiles every file under `ios/CleverTapReact/` directly. There is no Pods project for this module — host apps install via the React Native autolinking that resolves the podspec from `node_modules/clevertap-react-native/`.

## Example/ios — host app

| Path | Role |
|---|---|
| `Example/ios/Example/AppDelegate.{h,mm}` (or `.swift`) | Reference for how a host app wires `CleverTapReactManager` into AppDelegate. |
| `Example/ios/NotificationServiceSwift/` | Sample notification service extension (Swift) — receives push payload, modifies it, lets host app surface rich notifications. |
| `Example/ios/NotificationContentSwift/` | Sample notification content extension (Swift) — for custom push UI. |
| `Example/ios/NotificationService/` | Older Objective-C variant — kept for backward-compat reference. |
| `Example/ios/NotificationContent/` | Older Objective-C variant. |
| `Example/ios/Podfile` | Picks up `clevertap-react-native.podspec` via RN autolinking. Pins the iOS deployment target. |

When testing a podspec change, run `pod install` from `Example/ios/` rather than `pod update` — install is faster and only re-resolves things that changed.
