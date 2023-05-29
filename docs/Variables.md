# Overview
You can define variables using the CleverTap React Native SDK. When you define a variable in your code, you can sync them to the CleverTap Dashboard via the provided SDK methods.

# Supported Variable Types

Currently, CleverTap SDK supports the following variable types:

- String
- boolean
- JSON Object
- number

# Define Variables

Variables can be defined using the `defineVariables` method. You must provide the names and default values of the variables using a JSON Object. 

```javascript
let variables = {
              'reactnative_var_string': 'reactnative_var_string_value',
              'reactnative_var_map': {
                'reactnative_var_map_string': 'reactnative_var_map_value'
              },
              'reactnative_var_int': 6,
              'reactnative_var_float': 6.9,
              'reactnative_var_boolean': true
            };
CleverTap.defineVariables(variables);
```

# Setup Callbacks

CleverTap React Native SDK provides several callbacks for the developer to receive feedback from the SDK. You can use them as per your requirement, using all of them is not mandatory. They are as follows:

- Status of fetch variables request
- `onVariablesChanged`
- `onValueChanged`

## Status of Variables Fetch Request

This method provides a boolean flag to ensure that the variables are successfully fetched from the server.

```javascript
CleverTap.fetchVariables((err, success) => {
    console.log('fetchVariables result: ', success);
});
```

## `onVariablesChanged`

This callback is invoked when variables are initialized with values fetched from the server. It is called each time new values are fetched.

```javascript
CleverTap.onVariablesChanged((variables) => {
    console.log('onVariablesChanged: ', variables);
});
```

## `onValueChanged`

This callback is invoked when the value of the variable changes. You must provide the name of the variable whose value needs to be observed.

```javascript
CleverTap.onValueChanged('reactnative_var_string', (variable) => {
    console.log('onValueChanged: ', variable);
});
```

# Sync Defined Variables

After defining your variables in the code, you must send/sync variables to the server. To do so, the app must be in DEBUG mode and mark a particular CleverTap user profile as a test profile from the CleverTap dashboard. [Learn how to mark a profile as **Test Profile**](https://developer.clevertap.com/docs/concepts-user-profiles#mark-a-user-profile-as-a-test-profile)

After marking the profile as a test profile, you must sync the app variables in DEBUG mode:

```javascript
// 1. Define CleverTap variables 
// …
// 2. Add variables/values changed callbacks
// …

// 3. Sync CleverTap Variables from DEBUG mode/builds
CleverTap.syncVariables();
```

> 📘 Key Points to Remember
> 
> - In a scenario where there is already a draft created by another user profile in the dashboard, the sync call will fail to avoid overriding important changes made by someone else. In this case, Publish or Dismiss the existing draft before you proceed with syncing variables again. However, you can override a draft you created via the sync method previously to optimize the integration experience.
> - You can receive the following console logs from the CleverTap SDK:
>   - Variables synced successfully.
>   - Unauthorized access from a non-test profile. Please mark this profile as a test profile from the CleverTap dashboard.

# Fetch Variables During a Session

You can fetch the updated values for your CleverTap variables from the server during a session. If variables have changed, the appropriate callbacks will be fired. The provided callback provides a boolean flag that indicates if the fetch call was successful. The callback is fired regardless of whether the variables have changed or not.

```javascript
 CleverTap.fetchVariables((err, success) => {
    console.log('fetchVariables result: ', success);
});
```

# Use Fetched Variables Values

This process involves the following two major steps:

1. Fetch variable values.
2. Access variable values.

## Fetch Variable Values

Variables are updated automatically when server values are received. If you want to receive feedback when a specific variable is updated, use the individual callback:

```javascript
CleverTap.onValueChanged('reactnative_var_string', (variable) => {
    console.log('onValueChanged: ', variable);
});
```

## Access Variable Values

You can access these fetched values in the following two ways:

### Getting all variables

```javascript
CleverTap.getVariables((err, variables) => {
    console.log('getVariables: ', variables, err);
});
```

### Getting a specific variable

```javascript
CleverTap.getVariable('reactnative_var_string', (err, variable) => {
    console.log(`variable value for key \'reactnative_var_string\': ${variable}`);
});
```
