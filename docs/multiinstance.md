## 🔖 Overview

*Available from CleverTap React Native SDK v4.4.0.*

Multi-instance means **one app talking to two or more CleverTap accounts**.

- Every CleverTap account has an **account ID** and an **account token**.
- The SDK object that talks to one account is called an **instance**.
- In JavaScript you work with a **handle**. A handle is a small object with the same methods as `CleverTap`, but every call and every listener on it goes to **one** account only.
- The top-level `CleverTap` object is the handle for your **default account**: the account in `AndroidManifest.xml` / `Info.plist`. It works exactly as before.

```javascript
const CleverTap = require('clevertap-react-native');

// Default account (AndroidManifest.xml / Info.plist) — unchanged
CleverTap.recordEvent('App Opened');

// A second account, created from JavaScript
const insurance = await CleverTap.createInstance({
    accountId: 'INSURANCE-ACCOUNT-ID',
    accountToken: 'INSURANCE-TOKEN',
    region: 'eu1'
});
insurance.recordEvent('Policy Viewed', { plan: 'gold' });

// Listen to ONLY the insurance account's events
insurance.addListener(CleverTap.CleverTapProfileDidInitialize, (event) => {
    console.log('Insurance profile ready', event);
});
```

| Line | Which account receives it |
|---|---|
| `CleverTap.recordEvent('App Opened')` | Default account only |
| `insurance.recordEvent('Policy Viewed', ...)` | Insurance account only |
| `insurance.addListener(...)` | Fires for the insurance account's events only. The default account's `CleverTapProfileDidInitialize` never reaches it. |

### Do you need this?

| Your app | What to do |
|---|---|
| One CleverTap account (most apps) | Nothing. Keep using `CleverTap` as before. Read [Upgrading from an older SDK version](#upgrading-from-an-older-sdk-version) once. |
| Two or more accounts used at the same time | Use handles. This guide. |
| The whole app switches from account A to account B (for example a brand switch) | `setInstanceWithAccountId` still works. See [setInstanceWithAccountId vs getInstance](#setinstancewithaccountid-vs-getinstance). |

-----------

## The three ways an account can exist

| Way | Who creates it | When | How you get it in JavaScript |
|---|---|---|---|
| **Default account** | The native SDK, from `AndroidManifest.xml` / `Info.plist` | App start | `CleverTap` (the top-level object) |
| **Launch config** (native code) | Your `Application` / `AppDelegate` | App start, before any screen is shown | `CleverTap.getInstance(accountId)` |
| **`createInstance`** (JavaScript) | Your JavaScript | When your JavaScript bundle runs (about 2 seconds into a cold start) | `await CleverTap.createInstance(config)` |

Which one for my extra account?

- The account sends **push notifications** to this app → use a **launch config**. On Android, push for an extra account is shown only when that account is a launch-config account. On both platforms, the tap that opens the app reaches the account. See [Create accounts at app launch](#create-accounts-at-app-launch-native-launch-configs).
- The account shows **in-apps, Native Display units or variables on the first screen** → use a **launch config**, so the account sends its App Launched event and downloads its content at app start, the same way the default account does.
- The credentials are only known at runtime (fetched from your server) → use **`createInstance`**.
- Anything else → either works. `createInstance` needs no native code.

-----------

## Create an account from JavaScript — `createInstance(config)`

Returns a Promise that resolves with the account's handle.

```javascript
let insurance;
try {
    insurance = await CleverTap.createInstance({
        accountId: 'INSURANCE-ACCOUNT-ID',
        accountToken: 'INSURANCE-TOKEN',
        region: 'eu1',
        logLevel: 'debug',
        identityKeys: ['Email', 'Identity'],
        encryptionLevel: 'medium'
    });
} catch (error) {
    console.log(error.code, error.message);
    // e.g. EINVALID createInstance: accountId and accountToken must be non-empty strings
}
```

**Call it on every app start, before any other call for that account.** The native SDK remembers the account between app runs, but `createInstance` is what applies your config for this run. See [Rules you must follow](#rules-you-must-follow).

#### Config reference

| Key | Type | Meaning | Notes |
|---|---|---|---|
| `accountId` | string | CleverTap account ID | Required, non-empty |
| `accountToken` | string | CleverTap account token | Required, non-empty |
| `region` | string | Data center region, for example `'eu1'`, `'in1'`, `'us1'`, `'sg1'` | |
| `proxyDomain` | string | Custom proxy domain for events | iOS cannot combine it with `region`: `region` is applied and the proxy settings are ignored with a warning. Android applies both. |
| `spikyProxyDomain` | string | Custom proxy domain for push impression events | iOS: needs `proxyDomain` too, otherwise ignored with a warning |
| `handshakeDomain` | string | Custom domain for the SDK's first handshake request | |
| `identityKeys` | string[] | Which profile keys identify a user, for example `['Email', 'Identity']` | Only for accounts created from JavaScript. The default account reads `CLEVERTAP_IDENTIFIER` (Android) / `CleverTapIdentifiers` (iOS) instead. |
| `logLevel` | `'off'`, `'info'`, `'debug'`, `'verbose'` | Native log level of this account only | `'verbose'` becomes `'debug'` on iOS. Exact words only: `'Debug'` is rejected. |
| `analyticsOnly` | boolean | Analytics only, no in-apps or other engagement rendering | |
| `enablePersonalization` | boolean | Allow local reads of profile and event properties | |
| `disableAppLaunchedEvent` | boolean | Do not send the automatic "App Launched" event | |
| `encryptionLevel` | `'none'`, `'medium'`, `'high'` | Encryption of the data stored on the device | See [Encryption of PII data](usage.md#encryption-of-pii-data). Creation time only. |
| `encryptionInTransit` | boolean | Encrypt the SDK's network traffic | |
| `useCustomCleverTapId` | boolean | You supply the CleverTap ID yourself | Must be given together with `cleverTapId` |
| `cleverTapId` | string | Your own ID for this user / device, for example `'CUST-88231'` | Used only at creation. Must be given together with `useCustomCleverTapId: true`. |
| `android` | object | `{ useGoogleAdId?: boolean, backgroundSync?: boolean, pushProviders?: [...] }` | Ignored on iOS |
| `ios` | object | `{ disableIDFV?: boolean, enableFileProtection?: boolean }` | Ignored on Android |

- `android.pushProviders` entries need all four parts, the same four as the manifest `CLEVERTAP_PROVIDER_1` key and the `pushRegistrationToken` object: `{ type, prefKey, className, messagingSDKClassName }`. A missing part rejects the whole call.
- A `null` value means "not set".
- A value of the wrong type (for example `logLevel: 3` or `analyticsOnly: 'yes'`) rejects with `EINVALID`.

#### What happens when you call it twice

| Situation | Result |
|---|---|
| First call for this account in this app run | The account is created with your config. The native SDK saves the config. |
| Second call for the same account in the same app run | Resolves at once with the same handle. The new config is **ignored**, with no warning. |
| Next app launch, with a changed config | The new config is applied. |

Example: your app fetches the region from your server.

```javascript
// Launch 1
await CleverTap.createInstance({ accountId: 'B', accountToken: 'T', region: 'eu1' }); // created with eu1
await CleverTap.createInstance({ accountId: 'B', accountToken: 'T', region: 'in1' }); // same run: still eu1, no warning

// Launch 2 (next day)
await CleverTap.createInstance({ accountId: 'B', accountToken: 'T', region: 'in1' }); // now in1
```

#### Errors

| `error.code` | When | Example `error.message` |
|---|---|---|
| `EINVALID` | The config is wrong. Nothing was created. | `createInstance: accountId and accountToken must be non-empty strings` |
| | | `createInstance: logLevel must be one of off, info, debug, verbose` |
| | | `createInstance: cleverTapId and useCustomCleverTapId: true must be given together (or both left out)` |
| | | `createInstance: android.pushProviders[0].className is required` |
| `ECREATE` | The native SDK could not create the account | `createInstance failed for accountId INSURANCE-ACCOUNT-ID` |

-----------

## Get a handle for an existing account — `getInstance(accountId)`

```javascript
const insurance = CleverTap.getInstance('INSURANCE-ACCOUNT-ID');
insurance.recordEvent('Policy Viewed');
```

- Synchronous. It always returns a handle, never `null`.
- The same handle every time: `getInstance('X') === getInstance('X')`.
- Use it for accounts created by a launch config, or for an account you already created with `createInstance` earlier in this app run.
- If the account does not exist, every call on the handle logs **one native warning** and does nothing. Promise methods reject, callback methods return an error. The app never crashes.
- `getInstance('')` or a non-string logs a `console.error` and returns the **default** account's handle.

Example with a typo in the ID:

```javascript
CleverTap.getInstance('INSURANCE-ACOUNT-ID').recordEvent('Policy Viewed');
// Logcat / Xcode console:
// CleverTap instance not found for accountId: INSURANCE-ACOUNT-ID — call ignored. Create it first: ...
```

-----------

## Create accounts at app launch (native launch configs)

#### Why this exists

At app start the native SDK does several things for every account that exists at that moment: it handles the push that opened the app, registers the push token, sends the App Launched event, and downloads in-apps, display units and variables. The default account is created at app start, so it gets all of this on the first screen. An account created from JavaScript exists only once your bundle runs, so it does these things on its next App Launched. A launch config gives your extra accounts the same start as the default account.

Example: a push notification for the insurance account is tapped while the app is closed.

| Time after the tap | What happens |
|---|---|
| ~50 ms | The native SDK does its launch-time work for every account that exists. |
| ~2000 ms | Your JavaScript runs and calls `createInstance` for the insurance account. |

If the account exists only from JavaScript, the tap at 50 ms finds no account and no listener. The `CleverTapPushNotificationClicked` event is **lost**. The default account never has this problem, because it is created at app start. A launch config gives your extra accounts the same protection: they are created at process start, and early events are held until your JavaScript adds a listener.

List the accounts that send push or need content on the first screen. Every listed account adds a little work to app start.

#### Android

If your `Application` class extends `CleverTapApplication`, override `launchConfigs()`:

```java
import com.clevertap.android.sdk.CleverTapInstanceConfig;
import com.clevertap.react.CleverTapApplication;
import com.clevertap.react.CleverTapLaunchConfig;
import com.facebook.react.ReactApplication;
import java.util.Collections;
import java.util.List;

public class MainApplication extends CleverTapApplication implements ReactApplication {

    @Override
    public List<CleverTapLaunchConfig> launchConfigs() {
        CleverTapInstanceConfig insurance = CleverTapInstanceConfig.createInstance(
                this, "INSURANCE-ACCOUNT-ID", "INSURANCE-TOKEN", "eu1");
        return Collections.singletonList(new CleverTapLaunchConfig(insurance));
    }
}
```

If your `Application` class does not extend `CleverTapApplication` and you call `CleverTapRnAPI.initReactNativeIntegration(this)` yourself in `onCreate()`, pass the list as the second argument:

```java
import android.app.Application;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapInstanceConfig;
import com.clevertap.react.CleverTapLaunchConfig;
import com.clevertap.react.CleverTapRnAPI;
import com.facebook.react.ReactApplication;
import java.util.Collections;

public class MainApplication extends Application implements ReactApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycleCallback.register(this);

        CleverTapInstanceConfig insurance = CleverTapInstanceConfig.createInstance(
                this, "INSURANCE-ACCOUNT-ID", "INSURANCE-TOKEN", "eu1");
        CleverTapRnAPI.initReactNativeIntegration(this,
                Collections.singletonList(new CleverTapLaunchConfig(insurance)));
        // ...
    }
}
```

Existing calls with one argument keep working. The config object is the native `CleverTapInstanceConfig`, so every native setter is available (`setDebugLevel`, `setIdentityKeys`, `setEncryptionLevel`, ...).

#### iOS

In your `AppDelegate`, inside `application:didFinishLaunchingWithOptions:`, replace the one-argument `applicationDidLaunchWithOptions:` call with the two-argument version:

```objc
#import <CleverTap-iOS-SDK/CleverTap.h>
#import <CleverTap-iOS-SDK/CleverTapInstanceConfig.h>
#import <clevertap-react-native/CleverTapReactManager.h>

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [CleverTap autoIntegrate];

    CleverTapInstanceConfig *insurance = [[CleverTapInstanceConfig alloc]
        initWithAccountId:@"INSURANCE-ACCOUNT-ID" accountToken:@"INSURANCE-TOKEN" accountRegion:@"eu1"];
    [[CleverTapReactManager sharedInstance]
        applicationDidLaunchWithOptions:launchOptions
                          launchConfigs:@[[[CleverTapReactLaunchConfig alloc] initWithConfig:insurance]]];

    // ... the rest of your React Native setup
    return YES;
}
```

#### JavaScript side

Launch-config accounts already exist when your JavaScript runs. Use `getInstance`. Do **not** call `createInstance` for them, and do not pass a config again. The native config is the single source of truth.

```javascript
const insurance = CleverTap.getInstance('INSURANCE-ACCOUNT-ID');
insurance.addListener(CleverTap.CleverTapPushNotificationClicked, (event) => {
    // Fires even when this push tap cold-started the app
});
```

#### Custom CleverTap ID at launch

If you supply your own CleverTap ID for the account, pass it with the launch config. It is used only when the account is created for the first time.

```java
// Android
CleverTapInstanceConfig insurance = CleverTapInstanceConfig.createInstance(this, "INSURANCE-ACCOUNT-ID", "INSURANCE-TOKEN", "eu1");
insurance.setEnableCustomCleverTapId(true);
new CleverTapLaunchConfig(insurance, "CUST-88231");
```

```objc
// iOS
insurance.useCustomCleverTapId = YES;
[[CleverTapReactLaunchConfig alloc] initWithConfig:insurance cleverTapID:@"CUST-88231"];
```

If you pass the ID but forget the flag, the SDK logs a warning and generates its own ID. One bad launch config never crashes the app: it is logged and skipped, and the other accounts are still created.

The Example app has both snippets ready to uncomment: [MainApplication.java](/Example/android/app/src/main/java/com/reactnct/MainApplication.java) and [AppDelegate.mm](/Example/ios/Example/AppDelegate.mm).

-----------

## What a handle can do

A handle has every `CleverTap` method, with these exceptions.

| Methods | On a handle | Why |
|---|---|---|
| Events, profile, identity, session, App Inbox, Native Display, Product Config, Feature Flags, in-app controls, Variables, push tokens, push permission, `setLocale`, `setLocation`, `setOptOut`, `setOffline`, personalization | ✅ Work on the handle's account | Each account has its own data, inbox, variables and in-app queue. |
| `addListener`, `addOneTimeListener`, `removeListener` | ✅ Only this account's events | See [Listening to events per account](#listening-to-events-per-account). |
| `syncCustomTemplates*`, `customTemplate*` | ✅ Per account | Template **definitions** are registered once for the whole app, but each account presents its own templates. |
| `registerForPush`, `getInitialUrl`, `createNotification`, `createNotificationChannel`, `createNotificationChannelWithSound`, `createNotificationChannelWithGroupId`, `createNotificationChannelWithGroupIdAndSound`, `createNotificationChannelGroup`, `deleteNotificationChannel`, `deleteNotificationChannelGroup` | ⚠️ Exist, but log a `console.warn` and do nothing | These talk to the operating system, which has one of each thing per app, not per account. Call them on `CleverTap`. |
| `setDebugLevel`, `setInstanceWithAccountId`, `createInstance`, `getInstance`, `removeListeners` | ❌ Not on the handle | `setDebugLevel` is global for the whole app (use the `logLevel` config key for one account). The others are app-level factories. |

Handle-only member: `handle.accountId` (read-only string).

```javascript
const insurance = CleverTap.getInstance('INSURANCE-ACCOUNT-ID');
insurance.accountId;                       // 'INSURANCE-ACCOUNT-ID'
insurance.onUserLogin({ Identity: 'jane-001', Email: 'jane@example.com' });
insurance.profileSet({ Plan: 'gold' });
insurance.getCleverTapID((err, id) => console.log('insurance CleverTap ID', id));
insurance.initializeInbox();               // the insurance account's own inbox
insurance.defineVariables({ discount: 0 }); // the insurance account's own variables
insurance.registerForPush();               // console.warn, nothing happens — use CleverTap.registerForPush()
```

**Push tokens.** The device has one FCM / APNs token, but each account needs it to send pushes to this device. Pass the token to every account that should be able to push:

```javascript
CleverTap.setFCMPushToken(token);   // default account
insurance.setFCMPushToken(token);   // insurance account
```

**TypeScript.** The handle and config types are exported:

```typescript
import * as CleverTap from 'clevertap-react-native';
import type { CleverTapInstance, CleverTapInstanceConfig } from 'clevertap-react-native';

const config: CleverTapInstanceConfig = { accountId: 'B', accountToken: 'T' };
const insurance: CleverTapInstance = await CleverTap.createInstance(config);
```

-----------

## Listening to events per account

Every event is delivered to the handle of the account it belongs to. The event names are the same `CleverTap.*` constants you already use.

```javascript
// Default account's events
const subA = CleverTap.addListener(CleverTap.CleverTapInAppNotificationDismissed, (e) => console.log('default', e));

// Insurance account's events
const subB = insurance.addListener(CleverTap.CleverTapInAppNotificationDismissed, (e) => console.log('insurance', e));

subA.remove(); // detaches only this handler
subB.remove();
```

| Event source | `CleverTap.addListener` handler | `insurance.addListener` handler |
|---|---|---|
| In-app of the default account dismissed | ✅ fires | ❌ |
| In-app of the insurance account dismissed | ❌ | ✅ fires |

- `addListener` returns a subscription. `subscription.remove()` detaches that one handler.
- `addOneTimeListener(eventName, handler)` fires once, for the first matching event of that account, then detaches itself.
- `handle.removeListener(eventName)` removes the handlers added on **that handle** for that event. Other handles keep theirs.
- The payload is exactly what you get today. There is no extra "account" field in it; the handle tells you the account.
- Events that fire before your JavaScript adds a listener (for example a push tap that cold-started the app) are held for a short time per account and delivered when the listener is added. This covers the default account and launch-config accounts.

#### Custom templates per account

Register the template definitions once, as today (`CleverTapCustomTemplates.registerCustomTemplates(...)` natively). Each account then presents its own templates, so present / close events arrive on the handle, and the argument reads and dismissal must use the same handle:

```javascript
insurance.addListener(CleverTap.CleverTapCustomTemplatePresent, async (templateName) => {
    const text = await insurance.customTemplateGetStringArg(templateName, 'Text');
    await insurance.customTemplateSetPresented(templateName);
    await insurance.customTemplateSetDismissed(templateName);
});
insurance.syncCustomTemplates(); // uploads the definitions to the insurance account's dashboard (debug builds)
```

-----------

## setInstanceWithAccountId vs getInstance

Both existed or exist for talking to another account. They do different things.

| | `CleverTap.setInstanceWithAccountId('B')` | `CleverTap.getInstance('B')` |
|---|---|---|
| What it does | **Swaps** the account behind the top-level `CleverTap` object. From now on every `CleverTap.*` call and every `CleverTap.addListener` handler is for account B. | Returns a **separate** handle for B. `CleverTap` keeps pointing at the default account. |
| Use when | The whole app deliberately moves to another account (for example a brand switch) and you do not want to touch every call site. | You want to use two accounts at the same time, or address B from one place without changing what `CleverTap` means elsewhere. |
| Handles from `getInstance` / `createInstance` | Not affected by the swap. Each stays pinned to its own account. | |
| Status | Supported, not deprecated. | New in v4.4.0. |

```javascript
// Before v4.4.0 this was the only way to reach account B:
CleverTap.setInstanceWithAccountId('B');
CleverTap.recordEvent('Purchase');          // goes to B
CleverTap.setInstanceWithAccountId('A');    // had to swap back to use A again

// From v4.4.0, no swapping needed:
CleverTap.recordEvent('Purchase');          // default account
CleverTap.getInstance('B').recordEvent('Purchase'); // B, at the same time
```

One change to know: after `setInstanceWithAccountId('B')`, `CleverTap.addListener` handlers hear **only** B's events. In earlier versions they heard the old and the new account's events mixed together.

-----------

## Rules you must follow

| Rule | What goes wrong if you skip it |
|---|---|
| The top-level `CleverTap` object is the manifest / plist account. If your app has no manifest / plist account, use handles for everything. | `CleverTap.recordEvent(...)` logs `CleverTap default instance is not available — call ignored` and does nothing. |
| `createInstance(config)`, or a launch config, must be the **first** thing that touches an account, on **every** app run. | `getInstance('B')` before `createInstance` may bring the account back with the config saved on the previous launch, for example an old region. |
| Launch-config accounts: use `getInstance` only. Never pass a config from JavaScript for them. | A second config in the same run is silently ignored, so a difference between the two hides until it causes wrong data. |
| `createInstance` twice in the same run returns the existing account and ignores the new config. Config changes take effect on the next launch. | You think the new config applied. It did not. |
| `identityKeys` and `encryptionLevel` for the **default** account come from the manifest / plist only. For JavaScript-created accounts they come from `createInstance` only. | The other path is silently ignored by the native SDK. |
| `cleverTapId` and `useCustomCleverTapId: true` must be given together. | `createInstance` rejects with `EINVALID`. |
| OS-level methods (push registration, notification channels, `createNotification`, `getInitialUrl`) belong on `CleverTap`, not on a handle. | On a handle they log a warning and do nothing. |
| `setDebugLevel` is global. For one account, use the `logLevel` config key at creation. | A per-account expectation that cannot be met. |
| Never call `NativeModules.CleverTapReact.*` directly. Always go through the `CleverTap` object or a handle. | Old-architecture Android throws `got N arguments, expected N+1` on every call. |
| Accounts that send push, or that need in-apps or display units on the first screen, should be launch-config accounts. | A `createInstance`-only account misses the push that opened the app and gets its launch-time content on its next App Launched. |

-----------

## What each warning means

All native warnings appear in Logcat (Android) or the Xcode console (iOS). JavaScript warnings appear in Metro / the debugger. In debug builds, Metro also prints `[CleverTap][MultiInstance]` lines that show which account each event was routed to.

| Message | Where | Meaning | Fix |
|---|---|---|---|
| `CleverTap instance not found for accountId: X — call ignored. Create it first: ...` | Native | You called a method on `getInstance('X')`, but no account `X` exists in this app run. Logged on every ignored call. | Call `createInstance` for `X` first, or list it as a launch config. Check the ID for typos. |
| `CleverTap default instance is not available — call ignored. Add the default account to AndroidManifest.xml / Info.plist, or use getInstance(accountId)/createInstance(config) ...` | Native | You called a top-level `CleverTap.*` method, but the app has no manifest / plist account. | Add the default account, or use handles for everything. |
| `<method>: no CleverTap instance exists yet in this app run — the native SDK will drop this call. ...` | Android | A notification channel method or `createNotification` was called before any account existed. | Create an account first (manifest, launch config or `createInstance`). |
| `[CleverTap] <method> is not supported on account handles; call it on the top-level CleverTap object` | JavaScript | You called an OS-level method on a handle. | Call it on `CleverTap`. |
| `[CleverTap] getInstance called with an invalid accountId (...); returning the DEFAULT account handle. ...` | JavaScript (`console.error`) | `getInstance` got an empty string, `undefined` or a non-string. | Pass the real account ID string. |
| `createInstance: iOS cannot combine region with proxyDomain/spikyProxyDomain; region applied, proxy settings ignored (Android applies both)` | iOS | Your config has both `region` and a proxy domain. | Pass only one of them on iOS. |
| `createInstance: spikyProxyDomain requires proxyDomain; ignored` | iOS | `spikyProxyDomain` without `proxyDomain`. | Pass both. |
| `cleverTapID given for 'X' but enableCustomCleverTapId is false — the ID will be IGNORED and the SDK will generate its own` (Android) / `cleverTapID given for 'X' but useCustomCleverTapId ...` (iOS) | Native, at launch | A launch config carries a custom CleverTap ID without the flag. | Set `setEnableCustomCleverTapId(true)` / `useCustomCleverTapId = YES` on the config. |
| `Failed to create launch instance for 'X', skipping` / `launch config for 'X' produced no instance, skipping` | Native, at launch | That launch config could not be created. The other accounts were still created. | Check the config values and the stack trace in the log. |
| Promise rejected with `EINVALID` | JavaScript | The `createInstance` config is wrong. The message names the field. | Fix the config. See [Errors](#errors). |
| Promise rejected with `ECREATE` | JavaScript | The native SDK could not create the account. | Check the native log for the SDK's own error. |
| Callback error `CleverTap not initialized` (Android) / `CleverTap is not initialized` (iOS) | Callback methods | The addressed account does not exist. | Same as the first row. |

-----------

## Upgrading from an older SDK version

Nothing changes for an app that uses one account and calls everything through the `CleverTap` object. Three behaviors did change. Check whether any applies to you.

| Change | Before v4.4.0 | From v4.4.0 | Who is affected |
|---|---|---|---|
| `CleverTap.removeListener(eventName)` | Removed **every** listener for that event name in the whole app, including listeners other libraries added on React Native's raw event emitter. | Removes only the handlers added through `CleverTap.addListener`. | Apps that relied on it to clear listeners added elsewhere. Remove those where they were added. |
| Listeners after `setInstanceWithAccountId('B')` | `CleverTap.addListener` handlers received the old **and** the new account's events, mixed. | Handlers receive only B's events. | Apps that swap accounts and expected both streams. Use `getInstance('A').addListener(...)` for the other account. |
| Direct `NativeModules.CleverTapReact.*` calls | Worked by accident. | Break: every native method has a new trailing account argument. Old-architecture Android throws `got N arguments, expected N+1`, in release builds too. | Any code that bypassed the `CleverTap` object. Call `CleverTap.*` instead. |

Also new, and harmless for existing code:

- `CleverTap.addListener` now returns a subscription object (`{ remove }`). It returned nothing before.
- On iOS, callback methods now return an error when the account does not exist. Before, most of them never called back.

-----------

## Try it in the Example app

The [Example app](/Example/app/App.js) has a **Multi Instance** section: create a second account from JavaScript, record an event, log in a user, set a profile, read the CleverTap ID, call an unknown account (warning, no crash) and use custom templates on the second account. The handlers live in [app-utils.js](/Example/app/app-utils.js). Replace `SECOND_ACCOUNT_CONFIG` with your own test account to see the data on your dashboard.

### For more information,
 - [See the CleverTap JS interface](/src/index.js)
 - [See the CleverTap TS interface](/src/index.d.ts)
