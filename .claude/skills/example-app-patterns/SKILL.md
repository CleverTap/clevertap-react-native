---
name: example-app-patterns
description: Standard patterns for demonstrating new APIs in the React Native example app. Use when implementing new API wrappers, updating existing API functionality, or adding new feature categories to the example app. Ensures all APIs have corresponding UI demonstrations.
---

# Example App Update Patterns

Standard patterns for demonstrating CleverTap React Native SDK APIs in the example app with proper UI organization, implementation methods, and user feedback.

## Overview

The example app (`Example/app/`) serves as a live demo and testing ground for all CleverTap React Native SDK APIs. Every new or updated API MUST be demonstrated.

**Structure**:
- UI sections organized by feature category (ExpandableListView)
- Individual API buttons
- Implementation methods calling SDK APIs
- Helper methods for logging and user feedback (Toast, console.log)

**Key Files**:
- `Example/app/App.js` - Root component and navigation
- `Example/app/constants.js` - App constants and API configurations
- `Example/app/ExpandableListView.js` - Custom expandable list component
- `Example/app/DynamicForm.js` - Dynamic form component
- `Example/app/app-utils.js` - Utility functions

## Adding New APIs

### Step 1: Add API to Constants/Config

Add the new API entry in the appropriate section of the constants or app configuration:

```javascript
{
  title: 'User-Facing API Name',
  description: 'Brief description of what this API does.',
  action: 'implementationMethodName',
}
```

### Step 2: Add Implementation Method

In the appropriate component or App.js, add the handler:

**Methods returning data (with callback)**:
```javascript
const methodName = () => {
  CleverTap.apiMethod((error, result) => {
    if (result === null || result === undefined) {
      Toast.show('No result found');
      console.log('API Name -> No result');
    } else {
      Toast.show('Result fetched, check console');
      console.log('API Name -> Result:', result);
    }
  });
};
```

**Methods without return values**:
```javascript
const methodName = () => {
  CleverTap.apiMethod();
  Toast.show('Action triggered');
  console.log('API Name -> Called successfully');
};
```

**Methods with parameters**:
```javascript
const methodName = () => {
  const params = {
    key1: 'value1',
    key2: 123,
  };

  CleverTap.apiMethod(params, (error, result) => {
    if (result === null || result === undefined) {
      Toast.show('Operation failed');
      console.log('API Name -> Failed');
    } else {
      Toast.show('Success, check console');
      console.log('API Name -> Result:', result);
    }
  });
};
```

### Step 3: Create New Section (If Needed)

If API belongs to a new feature category, add a new expandable section:

```javascript
{
  title: 'New Feature Category',
  items: [
    {
      title: 'API Name',
      description: 'Description',
      action: 'methodName',
    },
    {
      title: 'Another API',
      description: 'Description',
      action: 'anotherMethod',
    },
  ],
}
```

## Updating Existing APIs

### When Signature Changes

If API adds new optional parameters, create TWO entries to demonstrate both use cases:

**Entry 1: Original behavior**
```javascript
{
  title: 'Original API Name',
  description: 'Original description without new parameter.',
  action: 'methodNameOriginal',
}
```

**Entry 2: New parameter**
```javascript
{
  title: 'API Name (With New Feature)',
  description: 'Description highlighting the new parameter behavior.',
  action: 'methodNameWithNewParam',
}
```

**Implementation methods**:
```javascript
// Original method - backward compatibility
const methodNameOriginal = () => {
  CleverTap.apiMethod((error, result) => {
    if (result === null) {
      Toast.show('No result found');
      console.log('API Name (Original) -> No result');
    } else {
      Toast.show('Result fetched, check console');
      console.log('API Name (Original) -> Result:', result);
    }
  });
};

// New method - demonstrates new parameter
const methodNameWithNewParam = () => {
  CleverTap.apiMethod({ newParam: true }, (error, result) => {
    if (result === null) {
      Toast.show('No result found');
      console.log('API Name (With Param) -> No result');
    } else {
      Toast.show('Result with new parameter, check console');
      console.log('API Name (With Param) -> Result:', result);
    }
  });
};
```

**Why two entries?**
- Demonstrates backward compatibility
- Shows new feature clearly
- Allows testing both behaviors
- Makes parameter impact obvious

## Code Style Guidelines

### Method Naming
- Descriptive camelCase names
- Match SDK API name when possible
- Example: `getCleverTapId`, `recordCustomEvent`

### User Feedback
Always provide dual feedback:
1. **Visual**: `Toast.show()` for immediate user feedback
2. **Console**: `console.log()` for detailed debugging

### Error Handling
Always check for null/undefined results:
```javascript
if (result === null || result === undefined) {
  Toast.show('No result found');
  console.log('API Name -> No result');
} else {
  Toast.show('Result fetched, check console');
  console.log('API Name -> Result:', result);
}
```

### Platform-Specific APIs
Wrap platform-specific sections:
```javascript
import { Platform } from 'react-native';

const methodName = () => {
  if (Platform.OS === 'android') {
    CleverTap.androidOnlyMethod();
    Toast.show('Android-only API called');
  } else {
    Toast.show('Not available on this platform');
  }
};
```

## Common Patterns

### Pattern 1: Simple Getter

```javascript
const getCleverTapId = () => {
  CleverTap.getCleverTapID((error, id) => {
    if (id === null) {
      Toast.show('CleverTap ID = NULL');
      console.log('CleverTap ID -> NULL');
    } else {
      Toast.show('CleverTap ID = ' + id);
      console.log('CleverTap ID ->', id);
    }
  });
};
```

### Pattern 2: List Fetcher

```javascript
const getAllInboxMessages = () => {
  CleverTap.getAllInboxMessages((error, messages) => {
    if (!messages || messages.length === 0) {
      Toast.show('No messages found');
      console.log('Inbox Messages -> Empty');
    } else {
      Toast.show(messages.length + ' messages found, check console');
      console.log('Inbox Messages -> Count:', messages.length);
      console.log('Inbox Messages -> Data:', messages);
    }
  });
};
```

### Pattern 3: Action Trigger

```javascript
const recordCustomEvent = () => {
  const eventData = {
    'Product Name': 'Casio Chronograph Watch',
    'Category': 'Mens Watch',
    'Price': 59.99,
    'Date': new Date(),
  };
  CleverTap.recordEvent('Product Viewed', eventData);
  Toast.show('Event recorded');
  console.log('Product Viewed -> Event Data:', eventData);
};
```

### Pattern 4: Complex Operation

```javascript
const setMultiValueForKey = () => {
  const values = ['Apple', 'Orange', 'Banana'];
  const key = 'Favorite Fruits';

  CleverTap.profileSetMultiValues(key, values);
  Toast.show('Multi-values set for ' + key);
  console.log('Profile -> Set', key, '=', values);
};
```

### Pattern 5: Platform-Specific API

```javascript
const registerForPush = () => {
  if (Platform.OS === 'ios') {
    // iOS push registration is handled by the system
    Toast.show('iOS push handled by system');
    console.log('Push -> iOS system handled');
  } else {
    CleverTap.registerForPush();
    Toast.show('Registered for push notifications');
    console.log('Push -> Registered');
  }
};
```

## Testing Checklist

- [ ] New API has corresponding entry in UI
- [ ] Entry is in correct feature category
- [ ] Description is clear and helpful
- [ ] Implementation method exists and named correctly
- [ ] Method handles null/error cases
- [ ] Both `Toast.show` and `console.log` are used
- [ ] Platform checks added if needed
- [ ] Example data is realistic and helpful
- [ ] Code follows existing patterns
- [ ] App builds and runs without errors on both platforms

## Common Issues

### Issue 1: Button Not Visible
**Cause**: Wrong section or missing configuration entry
**Solution**: Check correct section, verify configuration entry

### Issue 2: Method Not Found
**Cause**: Method name mismatch between config and implementation
**Solution**: Verify exact method name match, including case

### Issue 3: No User Feedback
**Cause**: Missing `Toast.show()` call
**Solution**: Always add `Toast.show()` for user feedback

### Issue 4: Crash on Button Press
**Cause**: Missing null checks or type mismatch
**Solution**: Add null checks, verify return type, wrap in try-catch if needed
