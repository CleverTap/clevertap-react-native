---
name: clevertap-react-native-example-app
description: >
  How to demonstrate a CleverTap React Native API in the Example app. Use
  whenever a new public method (or event) is added/surfaced — every new API
  MUST get a runnable demo in the Example app. Covers the constants.js Actions
  key -> app-utils.js handler -> App.js wiring pattern, with a worked example
  and a checklist.
---

# React Native Example-app demo pattern

The Example app (`Example/app/`) is the live, runnable demo + manual test surface for every public CleverTap RN API. **Every newly surfaced method MUST get a demo here** — it's not optional and it's not just docs. A method that ships without an Example demo is considered incomplete.

There are three files, edited in this order:

## 1. `Example/app/constants.js` — add an Actions key
`Actions` is a flat map of `KEY: 'KEY'` string constants used to identify menu items.
```js
export const Actions = {
    SET_USER_PROFILE: 'SET_USER_PROFILE',
    // ... add yours (UPPER_SNAKE_CASE, value === key):
    FETCH_INBOX: 'FETCH_INBOX',
};
```

## 2. `Example/app/app-utils.js` — add a handler
Export a handler function that calls the new API with **concrete, realistic values** (never `'foo'`/`{key:'value'}`). Use `showToast(...)` for on-screen feedback and `console.log` for detail. Mirror the existing handlers' style.
```js
export const fetch_inbox = () => {
    CleverTap.fetchInbox((err, success) => {
        showToast('Fetch Inbox', `success=${success}`);
        console.log('fetchInbox ->', err, success);
    });
};

export const push_displayUnitElementClicked = () => {
    CleverTap.pushDisplayUnitElementClickedEventForID('unit_123', {
        elementType: 'banner', position: 0, campaign: 'spring_sale',
    });
    showToast('Display Unit', 'element clicked event recorded');
};
```
For a **property-bag** method, demonstrate mixed types (string + number + bool) so the reader sees what's serializable. For an **overload with optional callback**, demo the callback form (it's the more informative one).

## 3. `Example/app/App.js` — wire the action into the menu
Import the handler and map the `Actions` key to it so it appears in the action list/menu and runs on tap. Match however the existing actions are registered in `App.js` (e.g. an action→handler map or a switch). Add yours alongside a related feature group.

## Checklist (per surfaced method)
- [ ] `constants.js` — `Actions` key added
- [ ] `app-utils.js` — handler added, calls `CleverTap.<method>(...)` with realistic values, gives `showToast` + `console.log` feedback
- [ ] `App.js` — action wired into the menu and runs on tap
- [ ] Values are concrete (no placeholders); overloads demo the callback form
- [ ] The three example files appear in the method's `files_touched`

## Anti-patterns
- Adding the bridge method but **forgetting the demo** (most common miss in auto-syncs).
- Placeholder values (`'Unit Id'`, `{key:'value'}`) — use realistic data.
- Wiring `constants.js` + `app-utils.js` but **not** `App.js` (the button never appears).
