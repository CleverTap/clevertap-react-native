# Custom Code Templates

CleverTap React Native SDK 3.1.0 and above offers support for a custom presentation of in-app messages. This allows for utilizing the in-app notifications functionality with custom configuration and presentation logic. There are two types of templates that can be defined through the SDK: Templates and Functions. Templates can contain action arguments while Functions cannot and Functions can be used as actions while Templates cannot. Functions can be either 'visual' or not. 'Visual' functions can contain UI logic and will be part of the [In-App queue](#in-App-queue), while non-visual functions will be triggered directly when invoked and should not contain UI logic.

## Creating templates and functions
 Templates consist of a name, type and arguments (and isVisual for Functions). Type and arguments (and isVisual for Functions) are required and names must be unique across the application. The template definitions are validated for the correctness and will throw `CustomTemplateException` when an invalid template is being created on launching the application. Those exceptions should generally not be caught and template definitions must be valid in order to be triggered correctly.

The templates are defined in а json format with the following scheme:
```json
{
  "TemplateName": {
    "type": "template",
    "arguments": {
      "Argument1": {
        "type": "string|number|boolean|file|action|object",
        "value": "val"
        },
      "Argument2": {
        "type": "object",
        "value": {
          "Nested1": {
            "type": "string|number|boolean|object",
            "value": "val"
          },
          "Nested2": {
            "type": "string|number|boolean|object",
            "value": "val"
          }
        }
      }
    }
  },
  "functionName": {
    "type": "function",
    "isVisual": true|false,
    "arguments": {
      "a": {
      "type": "string|number|boolean|file|object",
      "value": "val"
      }
    }
  }
}
```
The json definitions should be placed in one or more files in the following locations:
 - Android: in `assets` directory
 - iOS: in any directory linked to the project

For a working example, see the Example project: `Example/assets/templates.json` and `Example/android/app/src/main/assets/custom/templates.json`.

Optionally the `react-native-assets` package can be utilized to keep both files in sync.

### Arguments
Arguments are a structure that represent the configuration of the custom code templates. An `Argument` consists of a `type` and `value`.
The supported argument types are:
- Primitives - `boolean`, `number` and `string`. They must have a default value which would be used if no other value is configured for the notification.
- `object` - An `Object` where keys are the argument names and values are ```Arguments``` with supported primitve values.
- `file` - a file argument that will be downloaded when the template is triggered. Files don't have default values.
- `action` - an action argument that could be a function template or a built-in action like ‘close’ or ‘open url’. Actions don't have default values.

#### Hierarchical arguments
You can group arguments together by either using an `object` argument or indicating the group in the argument's name by using a '.' symbol. Both definitions are treated the same. `file` and `action` type arguments can only be added to a group by specifying it in the name of the argument.

The following code snippets define identical arguments:
```json
"arguments": {
    "map": {
        "type": "object",
        "value": {
            "a": {
                "type": "number",
                "value": 5
            },
            "b": {
                "type": "number",
                "value": 6
            }
        }
    }
}
```
and
```json
"arguments": {
    "map.a": {
        "type": "number",
        "value": 5
    },
    "map.b": {
        "type": "number",
        "value": 6
    }
}
```

### Template definition example
```json
"Example template": {
    "type": "template",
    "arguments": {
        "boolArg": {
            "type": "boolean",
            "value": false
        },
        "stringArg": {
            "type": "string",
            "value": "Default"
        },
        "mapArg": {
            "type": "object",
            "value": {
                "int": {
                    "type": "number",
                    "value": 0
                },
                "string": {
                    "type": "string",
                    "value": "Default"
                }
            }
        },
        "actionArg": {
            "type": "action"
        },
        "fileArg": {
            "type": "file"
        }
    }
}
```

## Registering custom templates

Templates must be registered within the native applications:

### For Android
Add the following line in the ```Application.onCreate``` method. If you are extending `CleverTapApplication` add the line before callling ```super.onCreate()```, otherwise add the line before calling ```CleverTapRnAPI.initReactNativeIntegration(this)```

```java
CleverTapCustomTemplates.registerCustomTemplates(this, "templateDefinitionsFileInAssets.json");
```

### For iOS
Add the following line in ```AppDelegate.didFinishLaunchingWithOptions``` before calling ```[CleverTap autoIntegrate]```:
```objc
[CleverTapReactCustomTemplates registerCustomTemplates:@"templates", nil];
```

## Syncing in-app templates to the dashboard

In order for the templates to be usable in campaigns they must be synced with the dashboard. When all templates and functions are defined and registered in the SDK, they can be synced by calling `CleverTap.syncCustomTemplates()` or `CleverTap.syncCustomTemplatesInProd()` in your React application. The syncing can only be done in debug builds and with a SDK user that is marked as 'test user'. We recommend only running this function while developing the templates and delete the invocation in release builds.

## Presenting templates

When a custom template is triggered, the coresponding event `CleverTap.CleverTapCustomTemplatePresent` or `CleverTap.CleverTapCustomFunctionPresent` will be raised. Applications with custom templates or functions must subscribe to those events through `CleverTap.addListener` where the event data will be the template's name. When the event hanlder is invoked, this template will be considered as "active" until `CleverTap.customTemplateSetDismissed(templateName)` is called. While a template is "active" the following functions can be used:

- Obtain argument values by using the appropriate `customTemplateGet*Arg(templateName: string, argName: string)` methods.
- Trigger actions by their name through `CleverTap.customTemplateRunAction(templateName: string, argName: string)`.
- Set the state of the template invocation. `CleverTap.customTemplateSetPresented(templateName: string)` and `CleverTap.customTemplateSetDismissed(templateName: string)` notify the SDK of the state of the current template invocation. The presented state is when an in-app is displayed to the user and the dismissed state is when the in-app is no longer being displayed.

Only one visual template or other InApp message can be displayed at a time by the SDK and no new messages can be shown until the current one is dismissed.

Applications should also subscribe to `CleverTap.CleverTapCustomTemplateClose` which will be raised when a template should be closed (which could occur when an action of type 'close' is triggered). Use this listener to remove the UI associated with the template and call `CleverTap.customTemplateSetDismissed(templateName)` to close it.

### Example

```js
CleverTap.addListener(CleverTap.CleverTapCustomTemplatePresent, templateName => {
    // show the UI for the template and call customTemplateSetDismissed when it is closed
    showTemplateUi({
        template: templateName,
        onClose: ()=> {
            CleverTap.customTemplateSetDismissed(templateName);
        }
    });
    // call customTemplateSetPresented when the UI has become visible to the user
   CleverTap.customTemplateSetPresented(templateName);
});

CleverTap.addListener(CleverTap.CleverTapCustomTemplateClose, templateName => {
    // close the corresponding UI before calling customTemplateSetDismissed
    CleverTap.customTemplateSetDismissed(templateName);
});
```

### In-App queue
When an in-app needs to be shown it is added to a queue (depending on its priority) and is displayed when all messages before it have been dismissed. The queue is persisted to the storage and kept across app launches to ensure all messages are displayed when possible. The custom code in-apps behave in the same way. They will be triggered once their corresponding notification is the next in the queue to be shown. However since the control of the dismissal is left to the application's code, the next in-app message will not be shown until the current code template has called `CleverTap.customTemplateSetDismissed(templateName)`

### File downloading and caching
File arguments are automatically downloaded and are ready for use when an in-app template is presented. The files are downloaded when a file argument has changed and this file is not already cached. For client-side in-apps this happens both at App Launch and retried if needed when an in-app should be presented. For server-side in-apps the file downloading happens only before presenting the in-app. If any of the file arguments of an in-app fails to be downloaded, the whole in-app is skipped and the custom template will not be triggered.