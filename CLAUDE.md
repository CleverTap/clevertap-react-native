# Claude Context for CleverTap React Native SDK

## Project Overview

**Project**: CleverTap React Native SDK
**Type**: React Native Module (Wrapper)
**Purpose**: Provides React Native JavaScript/TypeScript bindings for native CleverTap Android and iOS SDKs
**Repository**: https://github.com/CleverTap/clevertap-react-native

**Native SDK Repositories**:
- Android: https://github.com/CleverTap/clevertap-android-sdk
- iOS: https://github.com/CleverTap/clevertap-ios-sdk

This is a **wrapper SDK** - it doesn't implement analytics functionality directly but bridges React Native to the native CleverTap SDKs on Android and iOS.

---

## Architecture

```
JavaScript/TypeScript Layer (src/)
         | NativeModules / TurboModules
         |
Native Android Layer (android/src/)  <-->  CleverTap Android SDK (via Gradle dependency)
Native iOS Layer (ios/CleverTapReact/)  <-->  CleverTap iOS SDK (via CocoaPods)
```

**Key Points**:
- Supports both Old Architecture (NativeModules) and New Architecture (TurboModules)
- When the native SDKs update, we update our dependency versions and expose new features through the JS API.

---

## Project Structure

```
clevertap-react-native/
├── src/                                    # JavaScript/TypeScript public API
│   ├── index.js                           # Main entry point, all public methods
│   ├── index.d.ts                         # TypeScript type definitions
│   └── NativeCleverTapModule.ts           # TurboModule specification (New Arch)
│
├── android/                                # Android implementation
│   ├── build.gradle                       # VERSION NUMBERS + SDK DEPENDENCY HERE
│   └── src/main/java/com/clevertap/react/
│       ├── CleverTapModuleImpl.java       # Main module, handles NativeModule calls
│       ├── CleverTapRnAPI.kt              # Kotlin API wrapper
│       ├── CleverTapEventEmitter.kt       # Native->JS event callbacks
│       ├── CleverTapListenerProxy.kt      # Native SDK event listeners
│       ├── CleverTapUtils.java            # Type conversions
│       ├── CleverTapEvent.kt              # Calbback event data models
│       ├── CleverTapCustomTemplates.kt    # Clevertap Custom template feature handling
│       ├── CleverTapApplication.kt        # Application class setup
│       ├── CleverTapPackage.kt            # React Native package registration
│       ├── Constants.kt                   # Method/event name constants
│       ├── src/newarch/CleverTapModule.kt # New Architecture (TurboModule)
│       └── src/oldarch/CleverTapModule.kt # Old Architecture (NativeModule)
│
├── ios/                                    # iOS implementation
│   └── CleverTapReact/
│       ├── CleverTapReact.mm / .h         # Main module, handles NativeModule calls
│       ├── CleverTapReactManager.mm / .h  # React module manager
│       ├── CleverTapReactTemplatePresenter.mm / .h
│       ├── CleverTapReactAppFunctionPresenter.mm / .h
│       ├── CleverTapReactCustomTemplates.mm / .h
│       └── CleverTapReactPendingEvent.mm / .h
│
├── Example/                                # Example React Native app for testing
│   ├── app/
│   │   ├── App.js                         # Root component
│   │   ├── constants.js                   # App constants
│   │   ├── ExpandableListView.js          # Custom list view
│   │   ├── DynamicForm.js                 # Dynamic form component
│   │   └── app-utils.js                   # Utility functions
│   ├── android/                           # Example Android app
│   └── ios/                               # Example iOS app
│
├── docs/                                   # Integration guides
│   ├── install.md                         # Installation instructions
│   ├── integration.md                     # Integration guide
│   └── usage.md                           # Complete API documentation
│
├── clevertap-react-native.podspec         # iOS SDK VERSION DEPENDENCY HERE
├── package.json                           # VERSION NUMBER HERE
├── CHANGELOG.md                           # ADD ENTRIES AT TOP
└── README.md                              # Installation info
```

### Key Android Files Explained

- **CleverTapModuleImpl.java**: Main implementation class
  - Handles all JavaScript->Native method calls
  - Maps JS method calls to CleverTap Android SDK APIs
  - Examples: `recordEvent()`, `onUserLogin()`, `pushInstallReferrer()`

- **CleverTapRnAPI.kt**: Kotlin API wrapper
  - Higher-level API wrapper over native SDK

- **CleverTapEventEmitter.kt**: Handles all Native->JS callbacks
  - Buffers events until JS listeners are ready
  - Sends events like InApp shown/dismissed, profile updates, etc.

- **CleverTapListenerProxy.kt**: Bridges CleverTap SDK callbacks to React Native
  - Implements CleverTap SDK listener interfaces
  - Forwards events to `CleverTapEventEmitter`

- **CleverTapModule.kt (newarch/oldarch)**: Architecture-specific module registration
  - `newarch/` for TurboModules (React Native >= 0.71)
  - `oldarch/` for NativeModules (React Native < 0.71)

---

## Changelog Format (STRICT)

The `CHANGELOG.md` has a **strict format** that must be preserved. It's parsed by automation tools.

### Critical Rules

1. **Always add new entries at the TOP** (before all existing entries)
2. **Date format**: `(Month DD YYYY)` - e.g., `(January 23 2026)`
   - Month first, then day, then year. No comma. No leading zero on day.
   - NOT: `(23 January 2026)`, `(2026-01-23)`, or `(Jan 23, 2026)`
3. **Platform tags**: Use exactly these:
   - `[Android Platform]`
   - `[iOS Platform]`
   - `[Android and iOS Platform]` (when both affected)
4. **Link to native SDK changelogs with version anchors**
5. **Use issue links** for bug fixes: `Fixes [#123](url)`
6. **Maintain indentation**: 2 spaces for nested bullets

### Format Template

```markdown
Version X.X.X *(Month DD YYYY)*
-------------------------------------------
**What's new**
* **[Platform Name]**
  * Supports [CleverTap Platform SDK vX.X.X](link-with-anchor).
  * Additional feature details...

**API changes** (if applicable)
* **[Platform Name]**
  * New API: `methodName(params)` - Description
  * Deprecated: `oldMethod()` - Use `newMethod()` instead

**Breaking Changes** (if applicable)
* **[Platform Name]**
  * Removed: `deprecatedAPI()` - Migration guide

**Bug Fixes** (if applicable)
* **[Platform Name]**
  * Fixes - Description of fix
```

---

## Semantic Versioning

Follow [semver](https://semver.org/):

- **MAJOR** (3.0.0 -> 4.0.0): Breaking changes
  - Removed APIs
  - Changed method signatures
  - Behavioral changes that break existing code

- **MINOR** (3.8.0 -> 3.9.0): New features, backwards compatible
  - New APIs
  - New functionality
  - Native SDK updates (usually)

- **PATCH** (3.8.0 -> 3.8.1): Bug fixes only
  - Bug fixes
  - Documentation updates
  - Internal improvements

---

## Code Conventions

### JavaScript/TypeScript
- Main API exported from `src/index.js` as default `CleverTap` object
- TypeScript definitions in `src/index.d.ts`
- TurboModule spec in `src/NativeCleverTapModule.ts`
- Use `callWithCallback()` helper for async operations with callbacks
- Date objects auto-converted to epoch seconds via `convertDateToEpochInProperties()`

### Android (Java/Kotlin)
- Package: `com.clevertap.react`
- Follow Android SDK code style
- Use Kotlin for new code when possible
- Supports both Old and New Architecture

### iOS (Objective-C++)
- Module name: `CleverTapReact`
- Files use `.mm` extension (Objective-C++)
- Follow iOS SDK conventions
- Supports both Old and New Architecture via `RCT_NEW_ARCH_ENABLED`

### Git Commits
Use conventional commits format:
```
type(scope): subject

Examples:
feat(android): add push notification support
fix(ios): resolve in-app crash
chore: bump version to 3.9.0
docs: update integration guide
test: add event tracking tests
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

---

## Resources

### Native SDK Documentation
- [CleverTap Android SDK](https://github.com/CleverTap/clevertap-android-sdk)
- [Android SDK Changelog](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md)
- [CleverTap iOS SDK](https://github.com/CleverTap/clevertap-ios-sdk)
- [iOS SDK Changelog](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md)

### Developer Documentation
- [CleverTap Developer Docs](https://developer.clevertap.com/docs)
- [React Native Documentation](https://reactnative.dev/docs/getting-started)
- [React Native Native Modules (Old Arch)](https://reactnative.dev/docs/native-modules-intro)
- [React Native TurboModules (New Arch)](https://reactnative.dev/docs/the-new-architecture/pillars-turbomodules)
