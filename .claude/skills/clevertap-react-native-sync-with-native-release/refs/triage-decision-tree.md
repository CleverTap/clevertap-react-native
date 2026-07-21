# Triage decision tree

After running `tools/diff_native_api.py` you have a list of ADDED / REMOVED / CHANGED public APIs. This document is the decision tree to apply to each row.

## ADDED — a new public method appeared in the native SDK

For each ADDED row, ask in this order:

```
Q1. Is the method genuinely host-facing?
    ├── It's a public method an app developer would call directly
    │     in their host app to do something user-visible
    │     → continue to Q2
    │
    └── It's a public method the SDK exposes only so other SDK
        modules / our own internals can call it (helper, factory,
        listener-registration internal)
          → mark "internal" — DO NOT surface, note in CHANGELOG comment

Q2. Does it have a meaningful return value that needs to cross
    the bridge, or is it side-effect only?
    ├── Side-effect only (void return)
    │     → surface as a no-callback method
    │
    ├── Returns a single primitive (String / int / boolean)
    │     → surface with a Callback(error, value) signature
    │       — matches the existing RN convention
    │
    ├── Returns a structured value (Map, List, custom type)
    │     → surface with Callback(error, value); marshal via
    │       CleverTapUtils on Android, NSDictionary on iOS
    │
    └── Async with a completion handler in the native SDK
          → surface as Promise<T> (Android: Promise; iOS:
            RCTPromiseResolveBlock + RCTPromiseRejectBlock)

Q2.5. Are there overloads (e.g. native has both `foo()` and `foo(callback)`)?
    JS has no method overloading. The established pattern in this codebase:
    ONE JS method with an OPTIONAL callback parameter; the bridge null-checks
    and routes to the right native overload.

    Reference Android impl:

        public void fetchInbox(Callback callback) {
            CleverTapAPI cleverTap = getCleverTapAPI();
            if (cleverTap == null) return;
            if (callback == null) {
                cleverTap.fetchInbox();
            } else {
                cleverTap.fetchInbox((FetchInboxCallback) success ->
                    callback.invoke(null, success));
            }
        }

    Reference iOS .mm:

        RCT_EXPORT_METHOD(fetchInbox:(RCTResponseSenderBlock)callback) {
            if (callback == NULL) {
                [[CleverTap sharedInstance] fetchInbox];
            } else {
                [[CleverTap sharedInstance] fetchInboxWithCallback:^(BOOL success) {
                    callback(@[[NSNull null], @(success)]);
                }];
            }
        }

    JS surface (single method, optional callback): `CleverTap.foo(callback?)`
    TS type: `foo(callback?: (error: any, value: T) => void): void`

    **Surface BOTH overloads under this single JS method.** Do NOT defer the
    callback overload citing "needs JS API design" — the pattern is established
    and the (error, value) Node-style shape is derivable from the native
    callback's argument type.

    Defer the callback overload ONLY if:
    - The native callback delivers a custom struct/object with multiple fields
      whose meaning isn't documented in the native header.
    - The native method takes a Builder or other multi-step setup that doesn't
      fit `(args..., optional callback)`.

Q3. Is it platform-only?
    ├── Yes — only on Android (e.g., notification channel APIs)
    │     → still surface, but Platform.OS-guard in the JS wrapper
    │       and document the platform-only nature in usage.md
    │
    ├── Yes — only on iOS (e.g., AppDelegate plumbing)
    │     → same: Platform.OS-guard, document
    │
    └── No, both platforms got the same method this release
          → straightforward cross-platform surface via add-public-method

Q4. Does it need a corresponding event?
    Many "register a listener" methods on the native SDK imply a
    new event the host wants to subscribe to.
    ├── Yes
    │     → also follow the "Add a new event" path in
    │       add-public-method (CleverTapEvent.kt + supportedEvents +
    │       JS constant export + docs/callbackPayloadFormat.md)
    │
    └── No → just the method

→ Once decided "surface", delegate to clevertap-react-native-add-public-method.
```

## REMOVED — a public method that existed in the old version is gone

For each REMOVED row:

```
Q1. Was the removal announced as a breaking change in the
    native SDK's CHANGELOG?
    ├── Yes → this is intentional API removal
    │     → if the method is currently surfaced in RN
    │         → mark as breaking change in RN CHANGELOG
    │         → bump the RN SDK's MAJOR version (SemVer)
    │         → remove the JS wrapper, both Android arch-shim
    │           entries, the Impl method, the iOS
    │           RCT_EXPORT_METHOD, and the d.ts declaration
    │     → if not surfaced → ignore
    │
    └── No → likely a tool false positive
          (overloaded method on a different line, generic that
          the regex confused, etc.)
          → cross-check by `grep -rn "<methodName>"` in the new
            version's source tree; if it's actually still there,
            this is a parsing noise — ignore but note for the
            tool maintainer
```

## CHANGED — a public method's signature changed

For each CHANGED row, compare old/new signatures from diff.md:

```
Q1. What kind of change?
    ├── Added an OPTIONAL parameter at the end
    │     → backward-compatible on the native side;
    │       the RN bridge can pass the new param too
    │     → if the parameter unlocks new behavior, surface it;
    │       otherwise leave the RN signature as-is
    │
    ├── Added a REQUIRED parameter
    │     → breaking. The current RN bridge call sites won't
    │       compile against the new native SDK
    │     → adapt the Impl/.mm method; on the JS side, decide
    │       whether to add the new parameter to the public
    │       wrapper or supply a default
    │
    ├── Return type changed
    │     → adapt the type marshalling; if the new return type
    │       is richer (e.g., String → enum), consider whether
    │       to expose the richer type
    │
    └── Method moved to a different class / parameter renamed
        only
          → likely a parser false positive on rename or
            re-export; cross-check before acting

Q2. Is this a true signature change OR stylistic (parameter
    name change, whitespace, generics tightening)?
    ├── True semantic change → adapt the bridge
    └── Stylistic only → ignore (parser noise)
```

## Cross-platform consistency check

After processing all ADDED/REMOVED/CHANGED for both platforms, do this final pass:

- For each method that is being surfaced on one platform but NOT the other, ask: is this an intentional asymmetry (one platform's SDK genuinely doesn't have it) or a timing issue (the other platform's SDK release is coming)?
- If intentional asymmetry: ensure the JS wrapper guards with `Platform.OS === 'android'` (or ios) and either no-ops or warns on the other platform.
- If timing: note in the CHANGELOG "iOS-only for now, Android support pending vX.Y.Z" so it doesn't get forgotten.

## Build manifest changes

The diff tool's `build` block surfaces non-source changes that still affect the RN SDK or its host apps. Walk each category in order.

### SDK levels (Android)

```
Q1. minSdk went UP (e.g., 21 → 23)?
    ├── RN SDK's android/build.gradle minSdk is lower than the new floor
    │     → BUMP the RN SDK's minSdk to match. This is a breaking change
    │       for host apps that were targeting old API levels via the RN SDK.
    │     → Bump the RN SDK's MAJOR version (SemVer).
    │     → Document in CHANGELOG under "Breaking changes" — host apps with
    │       broader device targets need to know.
    │     → Also bump Example/android minSdk and `docs/install.md` if it
    │       quotes a minimum.
    │
    └── RN SDK's android/build.gradle minSdk is already at or above
          → No action; the native change is invisible to hosts.

Q2. targetSdk / compileSdk went UP?
    ├── targetSdk bump: usually safe; propagate to the RN SDK's
    │   android/build.gradle to stay aligned. Non-breaking but worth a note.
    └── compileSdk bump: propagate. Required if any new API in the bumped
        native SDK depends on it.
```

### iOS platform / deployment targets

```
Q1. s.platform / s.ios.deployment_target went UP?
    ├── Update clevertap-react-native.podspec's s.platform to match
    │     (or stay aligned).
    │   → If raising the floor, treat as a host-impacting change in CHANGELOG.
    │   → Update Example/ios's Podfile platform if it pins one.
    │
    └── No change → nothing to do.

Q2. tvos / osx / watchos targets — out of scope for the RN SDK.
```

### Versions catalog changes (Android, gradle/libs.versions.toml)

```
For each ADDED key in [versions]:
    Is the corresponding library used by the native SDK in a way that
    transitively affects host apps?
    ├── Yes (api-scope dependency) → host apps automatically get it; no
    │     action beyond a CHANGELOG note.
    ├── Yes (implementation-scope, but the new lib is needed for a feature
    │   the RN bridge exposes — e.g., new media-handling lib) → declare
    │   the library in the RN SDK's android/build.gradle, and document
    │   in `docs/install.md` if hosts must do anything.
    └── No / unknown → leave alone; native release notes will tell hosts
        if they need it.

For each REMOVED key:
    The library was dropped. If RN bridge code referenced it directly,
    update or remove the reference.

For each CHANGED key (version bumped):
    Most are silent (transitive bumps). If the library is a *required*
    host-app dep (rare in our case), update `docs/install.md` minimum.
```

### Direct dependency declarations (Android)

Treat the same way as versions-catalog `[libraries]` adds/removes — but
these aren't routed through the catalog, so a literal declaration change
means the native SDK is hard-pinning something. Worth a CHANGELOG note
either way.

### Pod dependencies (iOS, .podspec)

```
For each ADDED s.dependency or s.ios.dependency:
    Host apps using the new RN SDK version will transitively get this
    new pod. If it's something host apps would notice (e.g., a new
    third-party dep with its own integration steps), document in
    `docs/install.md` and CHANGELOG.

For each REMOVED dependency:
    A dep was dropped. If the RN bridge code referenced its types
    (rare), update.

For each CHANGED version spec:
    Usually silent — CocoaPods resolves on host-side `pod install`.
    Worth a CHANGELOG note for major bumps (e.g., SDWebImage 5.x → 6.x).
```

### AndroidManifest uses-permission

```
For each ADDED permission:
    Is the permission always-required by the new SDK behavior?
    ├── Yes → declare in the RN SDK's android/src/main/AndroidManifest.xml
    │     so it merges into host apps automatically.
    │   → Document in `docs/install.md` so security/compliance reviews
    │     see the change.
    │   → CHANGELOG note.
    │
    └── No (optional, gated by a runtime API call) → document in
        `docs/install.md` as "add this if you use feature X" and note
        in CHANGELOG.

For each REMOVED permission:
    Usually a cleanup. Drop from the RN SDK manifest if we declared
    it ourselves; otherwise no action.
```

### AndroidManifest uses-feature

Same pattern as permissions. Less common but the analysis is identical.

### Deprecations announced in the changelog (NOT in the diff)

The build-manifest diff catches *removed* deps, not *deprecated* ones.
For deprecations (e.g., "ExoPlayer is deprecated; migrate to Media3
before v9.0.0"):

1. Add a CHANGELOG note in the RN SDK calling out the upstream
   deprecation, so host integrators see it early.
2. If the RN bridge or Example app uses the deprecated dep, plan
   migration before the native major that removes it. Track in a ticket.
3. Don't migrate prematurely — wait for the native SDK to fully support
   the new path.

## Default heuristic when in doubt

If you cannot decide whether to surface an ADDED method:

1. Search the native SDK CHANGELOG for the method name. Was it called out under "New Features"? → surface.
2. Otherwise check the native SDK docs for the method. Is it linked from a top-level "API reference" or "Getting started" page? → surface.
3. Otherwise: don't surface in this release. Future demand (a customer ask, a docs question) will force the issue, at which point add it then. This is fine — we err on the side of a smaller, well-curated surface.
