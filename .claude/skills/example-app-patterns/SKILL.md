---
name: example-app-patterns
description: Standard patterns for demonstrating new APIs in the React Native example app. Use when implementing new API wrappers, updating existing API functionality, or adding new feature categories to the example app. Ensures all APIs have corresponding UI demonstrations.
---

# Example App Update Patterns

## Architecture

The example app is a **Class Component** (`Example/app/App.js`) using an accordion-style UI. Every CleverTap SDK API must have a corresponding demo button.

### File Responsibilities

| File | Purpose |
|------|---------|
| `Example/app/App.js` | Root component. Owns `accordionData` (UI structure), `handleItemAction` (dispatch switch), and form configs |
| `Example/app/constants.js` | `Actions` enum - string constants mapping action names |
| `Example/app/app-utils.js` | Helper functions for complex API calls with toast/console feedback |
| `Example/app/ExpandableListView.js` | Renders accordion categories. Do NOT modify unless changing UI framework |
| `Example/app/DynamicForm.js` | Renders dynamic key-value forms. Do NOT modify unless changing form behavior |

### Data Flow

```
accordionData[].subCategory[].action  -->  handleItemAction(item) switch
                                              |
                        +---------------------+---------------------+
                        |                                           |
                  Simple/inline                              Complex/reusable
              CleverTap.method()                          AppUtils.helperName()
              directly in switch                          (defined in app-utils.js)
```

## Adding a New API Demo

### Step 1: Add Action Constant

In `Example/app/constants.js`, add a new entry to the `Actions` object:

```javascript
export const Actions = {
    // ... existing actions
    NEW_ACTION_NAME: 'NEW_ACTION_NAME',
};
```

**Naming convention**: `SCREAMING_SNAKE_CASE`, descriptive of the feature (e.g., `GET_VARIANTS`, `IN_APPS_DISCARD_WITH_DISMISS`, `FETCH_INAPPS`).

### Step 2: Add to accordionData

In `App.js`, add the entry to the appropriate category in the `accordionData` class property:

```javascript
accordionData = [
    // ... existing categories
    {
        categoryName: 'Existing Category',
        subCategory: [
            // ... existing items
            { action: Actions.NEW_ACTION_NAME, name: 'displayName' },
        ],
    },
];
```

- `action`: References the `Actions` constant from Step 1
- `name`: Display text shown as button label in the UI

### Step 3: Add Handler in switch

In `App.js` `handleItemAction(item)`, add a case to the switch statement.

**Choose inline or AppUtils based on complexity:**

#### Option A: Inline (simple, no callback, one-off)

Use when the call is a simple one-liner or few lines with no callback:

```javascript
case Actions.NEW_ACTION_NAME:
    CleverTap.someMethod();
    break;
```

Real examples from the codebase:
```javascript
case Actions.IN_APPS_SUSPEND:
    CleverTap.suspendInAppNotifications();
    break;
case Actions.OPT_IN:
    CleverTap.setOptOut(false);
    break;
case Actions.SET_DEBUG:
    CleverTap.setDebugLevel(3);
    break;
case Actions.SYNC_VARIABLES:
    CleverTap.syncVariables()
    break;
```

#### Option B: Delegate to AppUtils

Use for complex operations, multi-step logic, or reusable helpers. Add the handler in `app-utils.js`:

```javascript
// In app-utils.js
export const newHelperFunction = () => {
    CleverTap.someMethod((err, res) => {
        console.log('Result: ', res, err);
        showToast(`Result: ${res}`);
    });
};
```

```javascript
// In App.js handleItemAction switch
case Actions.NEW_ACTION_NAME:
    AppUtils.newHelperFunction();
    break;
```

### Step 4: Create New Category (if needed)

If the API belongs to a new feature area, add a new category object to `accordionData`:

```javascript
{
    categoryName: 'New Feature Area',
    subCategory: [
        { action: Actions.NEW_ACTION_NAME, name: 'displayName' },
    ],
},
```

Place it in a logical position among existing categories.

## Existing Categories Reference

| Category | Feature Area |
|----------|-------------|
| User Properties | Profile set/get, multi-values, increment/decrement |
| Identity Management | onUserLogin, getCleverTapID |
| Location | setLocation, setLocale |
| Events | recordEvent, recordChargedEvent |
| Event History | getUserEventLog, getUserLastVisitTs, etc. |
| Product Experiences: Vars | Variables, variants, define/fetch/sync |
| Push Notifications | Channels, registration tokens, createNotification |
| App Inbox | Initialize, show, messages CRUD |
| Push Templates | Various push template types (all use RECORD_EVENT) |
| Push Primer Local InApp | Half-interstitial, alert, hard permission |
| InApp Controls | suspend/discard/resume InApp notifications |
| Custom Templates | Sync custom templates |
| Native Display | Display units |
| Client Side InApps | Fetch/clear InApps |
| Product Config | Fetch, activate, get values |
| Feature Flag | getFeatureFlag |
| App Personalisation | enablePersonalization |
| GDPR | Opt in/out, network info |
| Attributions | Attribution identifier (deprecated) |
| Listeners | Add/remove CleverTap event listeners |
| Enable Debugging | setDebugLevel |

## AppUtils Patterns

### Feedback Pattern

Always provide dual feedback in AppUtils functions:

```javascript
export const helperName = () => {
    CleverTap.method((err, res) => {
        console.log('Description: ', res, err);
        showToast(`Description: ${res}`);
    });
};
```

- `showToast(text1, text2)` - queued toast system (text2 is optional)
- `console.log()` - for detailed debugging
- Some older functions use `alert()` - prefer `showToast()` for new code

### Void Method Pattern

```javascript
export const helperName = () => {
    showToast('Action performed');
    CleverTap.voidMethod(param1, param2);
};
```

### Platform-Specific Pattern

```javascript
export const helperName = () => {
    if (Platform.OS === 'android') {
        CleverTap.androidOnlyMethod('token');
    }
};
```

## Special Patterns

### Updated API with New Overload

When an API adds a new optional parameter, add a separate action for the new variant:

```javascript
// constants.js - two separate actions
IN_APPS_DISCARD: 'IN_APPS_DISCARD',
IN_APPS_DISCARD_WITH_DISMISS: 'IN_APPS_DISCARD_WITH_DISMISS',

// accordionData - two entries in same category
{ action: Actions.IN_APPS_DISCARD, name: 'discardInAppNotifications' },
{ action: Actions.IN_APPS_DISCARD_WITH_DISMISS, name: 'discardInAppNotifications(true)' },

// handleItemAction - two cases
case Actions.IN_APPS_DISCARD:
    CleverTap.discardInAppNotifications();
    break;
case Actions.IN_APPS_DISCARD_WITH_DISMISS:
    CleverTap.discardInAppNotifications(true);
    break;
```

### Listener Registration Pattern

```javascript
case Actions.VARIABLES_CHANGED:
    CleverTap.onVariablesChanged((variables) => {
        console.log('onVariablesChanged: ', variables);
    });
    break;
```

## Checklist

When adding a new API demo, verify:
- [ ] Action constant added to `constants.js`
- [ ] Entry added to correct category in `accordionData`
- [ ] Case added to `handleItemAction` switch
- [ ] If using AppUtils: function exported from `app-utils.js`
- [ ] Feedback provided (console.log + showToast)
- [ ] If API has overloads: separate actions for each variant
