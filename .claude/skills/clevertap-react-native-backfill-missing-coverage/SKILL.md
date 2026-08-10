---
name: clevertap-react-native-backfill-missing-coverage
description: Frames the work when a CleverTap native SDK (Android or iOS) already supports a capability that the React Native SDK never surfaced — for example, multi-instance support via `instanceWithAccountId`, or any other native public API that customers can call directly but RN host apps cannot. Unlike `clevertap-react-native-add-public-method` (which executes a known 1:1 method addition) and `clevertap-react-native-sync-with-native-release` (which is release-driven), this skill's value is in the JS API DESIGN STEP — deciding how a non-trivial native concept should be shaped on the JS side (instance handles vs singletons, builders, listener-as-object, promise vs callback), planning migration when the addition conflicts with existing JS API, and only then handing off to the add-public-method recipe for execution. Use when a customer ask or internal review surfaces "native has it, RN doesn't" and the JS shape is not obviously 1:1.
allowed-tools:
  - Read
  - Glob
  - Grep
  - Bash
  - Edit
  - Write
---

# Backfill Missing Native Coverage in the RN SDK

## When to use

- A capability exists on the native SDK (Android or iOS) but the RN bridge never exposed it.
- The missing capability is non-trivial to surface — the native shape doesn't translate 1:1 to JS, and there's a design question to settle first.
- Trigger phrases: "we don't support X in RN", "native has Y but RN doesn't", "add multi-instance to RN", "backfill X".

If the missing capability is a single method whose JS shape is obvious (e.g., a side-effect `void` with primitive args), skip directly to [`clevertap-react-native-add-public-method`](../clevertap-react-native-add-public-method/SKILL.md). This skill is for cases where the design is the work.

## Pipeline Overview

The skill runs a 7-step flow. Steps 1–4 are the value-add (framing + design). Step 5 onward is execution that delegates to existing skills.

1. **Identify** — confirm with the user what's missing. One method? A feature area (e.g., "multi-instance")? Capture the goal in one sentence.
2. **Audit current coverage** — read JS (`src/index.js`), Android impl (`CleverTapModuleImpl.java`), iOS impl (`CleverTapReact.mm`). Surface partial coverage explicitly — e.g., today's `setInstanceWithAccountId` is partial multi-instance support; the design must account for it, not pretend it isn't there.
3. **Design the JS API** — walk [refs/design-decision-tree.md](refs/design-decision-tree.md). Decide whether the native shape is portable 1:1, needs a richer JS abstraction (instance handle, builder, listener-as-object), or conflicts with existing JS API. Output: a one-pager describing the proposed surface — method signatures, types, lifecycle, examples. Present to the user for sign-off BEFORE writing code.
4. **Migration plan** — only if the new design changes existing JS API:
   - Backwards-compat: deprecate-and-keep (add `@deprecated` JSDoc, keep working) / hard-replace (breaking) / version-gated.
   - CHANGELOG narrative explicitly calling out the change.
   - Decide whether the version bump is MAJOR (SemVer rule of thumb: any host-visible breaking change is MAJOR).
5. **Apply** — for each method in the agreed design, delegate to [`clevertap-react-native-add-public-method`](../clevertap-react-native-add-public-method/SKILL.md). That recipe handles TurboModule spec → JS wrapper → TS types → Android Impl + both arch shims → iOS `RCT_EXPORT_METHOD` → Example app → docs.
6. **Test** — run the Example app on both platforms. Exercise the new methods AND adjacent old ones (regression safety — multi-instance changes shouldn't break single-instance flows).
7. **Document** — update `docs/usage.md` with the new surface and any examples. CHANGELOG entry. If breaking, add a migration section. If the change introduces a new conceptual surface (e.g., multi-instance), consider a dedicated short doc under `docs/`.

## Architectural Rules

- **Design step is mandatory and reviewed.** Step 3 produces a written one-pager. The user signs off before any code is written. Skipping this step is the #1 way clunky JS APIs ship.
- **Partial coverage is acknowledged, not erased.** If something adjacent already exists in the RN SDK (like `setInstanceWithAccountId` for multi-instance), the design names it and decides its fate: extend, deprecate, replace.
- **Cross-platform consistency is part of design.** Some native capabilities differ between iOS and Android (e.g., Android has notification channels, iOS doesn't). The design either supports both symmetrically with `Platform.OS` guards or is intentionally asymmetric with `isAvailable()` helpers. Pick one explicitly.
- **Mechanics live in the add-public-method skill.** This skill never duplicates that recipe. After step 4 it hands off.
- **Breaking changes get explicit MAJOR bumps and migration narratives** — never silent.

## Differentiator vs. other skills

| Situation | Skill |
|---|---|
| Add one new method whose JS shape is obvious (1:1 bridge) | [`clevertap-react-native-add-public-method`](../clevertap-react-native-add-public-method/SKILL.md) |
| New native SDK released → version bump + automated diff | [`clevertap-react-native-sync-with-native-release`](../clevertap-react-native-sync-with-native-release/SKILL.md) |
| Native already has X, RN doesn't, JS shape needs design | **this skill** |
| "Where does feature X live in the RN SDK?" | [`clevertap-react-native`](../clevertap-react-native/SKILL.md) (broad overview) |

## Worked example (illustrative, not prescriptive)

Multi-instance is the canonical case this skill exists for:

- **Native shape:** `CleverTapAPI.instanceWithAccountId(accountId)` returns a `CleverTapAPI` instance; methods on it route to that account.
- **Today's RN partial:** `CleverTap.setInstanceWithAccountId(accountId)` sets a default; subsequent calls on the singleton `CleverTap` object route there. You can't have two live instances simultaneously.
- **Design questions** to walk through (step 3):
  - Does the JS surface stay singleton-with-routing, or expose per-instance handle objects?
  - If handles, do they share the JS event-listener registry or have their own?
  - How does this interact with existing event constants (`CleverTapInboxDidInitialize` etc.) — are they per-instance or global?
  - Migration: keep `setInstanceWithAccountId` for backwards-compat? deprecate? hard-replace?
- **This skill drives the design; the actual proposal lives in step 3's one-pager when you run it.**

## Anti-patterns

- **Skipping the design step.** Going straight to `add-public-method` results in JS APIs that mirror native shape too literally — magic-string parameters where a structured object would be cleaner, raw callbacks where a listener-as-object would compose better.
- **Forgetting partial coverage.** Designing as if no adjacent API exists, then ending up with two competing JS APIs that confuse host integrators.
- **Designing in isolation.** The user must sign off on the one-pager before code lands. No big-bang implementations.
- **Silent breaking changes.** If the new design replaces existing JS API, that's a MAJOR bump with a CHANGELOG migration note — no exceptions.
- **Asymmetric without `isAvailable()`.** If the new capability only exists on one platform, the JS wrapper needs either a `Platform.OS` guard with a clear no-op + warning on the other platform, OR an `isAvailable()` helper so hosts can branch cleanly.

## Testing

The skill itself ships nothing executable. Each invocation produces:

- A one-pager (design — review with the user).
- A set of edits via the `add-public-method` recipe (executed with that skill's verification: lint pass, Example app smoke test on both platforms).
- Updated docs (`docs/usage.md`, CHANGELOG, optionally a migration guide).

## Reference Files

- [refs/design-decision-tree.md](refs/design-decision-tree.md) — Decision tree for the JS-shape design step. Covers 1:1 portability, richer abstractions (instance handles, builders, listener-as-object), promise vs callback, platform asymmetry, and conflicts with existing JS API.

## Related

- [`clevertap-react-native-add-public-method`](../clevertap-react-native-add-public-method/SKILL.md) — the execution recipe this skill delegates to.
- [`clevertap-react-native-sync-with-native-release`](../clevertap-react-native-sync-with-native-release/SKILL.md) — the release-driven sibling; use it when a new native version triggered the work.
- [`clevertap-react-native`](../clevertap-react-native/SKILL.md) — broad overview; use to orient on the JS / Android / iOS layout before designing.
