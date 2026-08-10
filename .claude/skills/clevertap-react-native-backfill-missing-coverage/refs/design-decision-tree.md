# JS API Design Decision Tree

For each missing native capability, walk this tree. Output is a one-pager describing the proposed JS surface — present to the user for sign-off before writing code.

## Stage 1 — Is the native shape portable 1:1?

A native method is "portable 1:1" when:

- Its arguments are primitives or simple objects (no native types like `Activity`, `Intent`, `UIViewController` that don't have JS equivalents).
- Its return is either side-effect-only, a primitive, or a serializable map / array.
- It does not require holding a stateful native object across multiple calls (a "handle").
- The same method exists on both Android and iOS with semantically equivalent behavior.

```
Q1. Is the method portable 1:1?
    ├── Yes
    │     → Output: a single JS method signature mirroring the native.
    │     → Proceed to Stage 4 (existing-API conflict check).
    │     → Then hand off to add-public-method.
    │
    └── No → continue to Stage 2.
```

## Stage 2 — Does it need a richer JS abstraction?

When a 1:1 method isn't enough, pick a JS pattern that suits the capability. The choices that show up in CleverTap-wrapper-style SDKs:

### 2a. Stateful native object → JS handle

Use when the native API returns a stateful object the caller is meant to hold (e.g., `CleverTapAPI` instance from `instanceWithAccountId`, a builder, a query handle).

JS shapes to consider:

- **Singleton + routing parameter.** Keep one JS singleton; every method takes an extra `accountId` (or equivalent) arg. Simplest; hides the multi-instance nature. Works when state is small and the call sites are few.
- **JS class / object with methods.** Expose `CleverTap.instanceForAccountId(accountId)` returning an object whose methods mirror the top-level `CleverTap` namespace but route to the named instance. Cleaner ergonomics; matches the native shape. Requires more bridge plumbing — every method needs an `instanceId` first arg in the bridge call.
- **Hybrid.** Singleton stays for the "default" instance; instance objects exist for named ones. Migrates today's API gracefully.

Decision factors:
- How many methods route to the instance? (Few → routing-param is fine. Many → JS object is cleaner.)
- Does the host app typically have one logical instance at a time or several live concurrently? (Several live → JS objects; one at a time → routing-param.)
- Do events also need per-instance routing? (Yes → JS objects make event subscription cleaner; routing-param requires adding an instance discriminator to every event payload.)

### 2b. Multi-step configuration → builder or options object

Use when the native API takes a configuration object built up over multiple calls (`Builder.setX().setY().build()`), or many optional parameters.

JS shapes to consider:

- **Plain options object.** `CleverTap.doThing({ x: 1, y: 2 })`. Idiomatic in JS; most ergonomic.
- **Builder helper.** `CleverTap.thingBuilder().setX(1).setY(2).build()`. Rare in JS; only use if the build step itself does meaningful validation native-side.

Default to plain options. Builders are anti-idiomatic in JS.

### 2c. Stream of events / repeated callbacks → listener-as-object

Use when the native API exposes a listener interface with multiple methods (`onA`, `onB`, `onC`) or fires repeated events.

JS shapes to consider:

- **One event per method, separate `addListener` calls.** Matches today's RN SDK convention. Easy to compose. Works when the listener methods are semantically distinct.
- **Listener-as-object: pass `{ onA: fn, onB: fn, onC: fn }`.** Better when the methods are tightly coupled and host apps will register them together. Rare in this codebase but valid for new APIs.

Default to the per-event pattern (consistent with the existing skill).

### 2d. Long-running operation → promise vs callback

JS shapes to consider:

- **Promise.** Modern; awaitable. The RN bridge supports `RCTPromiseResolveBlock` / `RCTPromiseRejectBlock` on iOS and `Promise` on Android. Use when the host wants `await`.
- **Callback `(error, value)`.** Node-style. Matches the existing RN SDK convention for most read-style methods (`getCleverTapID(cb)`, etc.).

Pick whichever is consistent with the existing surrounding API. For brand-new surfaces, default to Promise — it's the modern idiom and composes better with async/await.

### 2e. None of the above

If the native capability genuinely doesn't fit any of these patterns, escalate: present the native shape to the user and propose a custom JS pattern with justification. Don't force-fit.

## Stage 3 — Cross-platform consistency

```
Q1. Does both native SDKs expose this capability with the same shape?
    ├── Yes — same name, same args, same semantics
    │     → straightforward; design the same JS API for both.
    │
    ├── Partially — both have it, but with different names or arg shapes
    │     → JS wrapper normalizes; bridge code marshals differently per
    │       platform. Document the normalization in the one-pager.
    │
    ├── One platform only (e.g., Android channels, iOS push extensions)
    │     → JS wrapper either:
    │         (a) Platform.OS-guards and no-ops with a console.warn
    │             on the missing platform, OR
    │         (b) Exposes an isAvailable() helper so hosts branch
    │             cleanly without try/catch.
    │     → State the choice explicitly in the one-pager.
    │
    └── Both have it but semantically incompatible (different mental models)
          → Stop. Surface this to the user. Either (i) we don't ship a
            unified API and instead expose two platform-specific surfaces,
            or (ii) we pick one platform's model and have the other adapt.
            Don't paper over the difference silently.
```

## Stage 4 — Conflict with existing JS API

```
Q1. Does the new design touch any existing JS method, event, or constant?
    ├── No → no migration. Proceed to add-public-method.
    │
    ├── Yes, additive only (extends an existing method's signature with
    │   an optional parameter, adds a new event constant, etc.)
    │     → Backwards-compatible. Document under "Additions" in CHANGELOG.
    │
    ├── Yes, deprecating an existing API in favor of the new one
    │     → Mark the old API @deprecated in src/index.d.ts JSDoc, keep
    │       it working for at least one MINOR release before removing.
    │     → CHANGELOG under "Deprecations" with the migration path.
    │
    └── Yes, hard-replacing existing API (the old API cannot stay)
          → BREAKING. MAJOR version bump.
          → CHANGELOG under "Breaking changes" with the migration path.
          → Migration guide in docs/usage.md or a new docs/<feature>.md.
```

## Output: the one-pager

After walking the tree, produce a short document containing:

1. **Goal** — what's being added, in one sentence.
2. **Native shape** — the native method(s) being surfaced, with file/method references.
3. **Proposed JS surface** — exact method signatures (TS), event constants, examples of host-app usage.
4. **Design choices** — bullet points covering: pattern picked (1:1 / handle / options object / listener-as-object / promise), platform symmetry handling, partial-coverage handling (what existing API is affected), migration plan if any.
5. **Open questions** — anything still unsettled, surfaced for the user.

Present to the user. Iterate. Only after sign-off, hand off to [`clevertap-react-native-add-public-method`](../../clevertap-react-native-add-public-method/SKILL.md) and start applying.
