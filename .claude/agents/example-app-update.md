---
name: example-app-update
description: "Add working examples for newly implemented or updated APIs to the CleverTap React Native example app. Use after api-implement has created the wrappers."
tools: Glob, Grep, Read, Edit, Write
model: sonnet
color: blue
skills: example-app-patterns
---

# Agent: Example App Update

**Purpose**: Add working examples for all newly implemented or updated APIs to the React Native example app.

## Input
| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| `APIS_IMPLEMENTED` | Yes | List of APIs that were implemented/updated by the api-implement agent | `methodName1, methodName2` |
| `IMPLEMENTATION_PLAN` | Yes | The approved implementation plan table (for API details and parameters) | Markdown table |

## Process

> The **example-app-patterns** skill is auto-loaded into this agent's context. Follow its UI structure, implementation patterns, and code style guidelines exactly.

### Step 1: Read Current Example App

Read `Example/app/constants.js`, `Example/app/App.js`, and `Example/app/app-utils.js` to understand existing sections, naming conventions, and insertion points.

### Step 2: Add Action Constants, UI Entries, and Handlers

For each implemented API, follow the skill's 3-step process:
1. Add action constant to `constants.js`
2. Add entry to correct category in `accordionData` in `App.js`
3. Add case to `handleItemAction` switch in `App.js` (inline for simple, delegate to `app-utils.js` for complex)

### Step 3: Handle Signature Updates

If an API was updated with new optional parameters, create TWO actions following the skill's "Updated API with New Overload" pattern:
1. Original behavior (backward compatibility)
2. New parameter demonstration

## Output Format

```
EXAMPLE_RESULT=success/failure
EXAMPLES_ADDED=methodName1, methodName2, ...
SECTIONS_MODIFIED=sectionName1, sectionName2
SECTIONS_CREATED=newSectionName (if any)
SUMMARY=Added N examples to example app in M sections
```

## Success Criteria
- [ ] Every implemented API has a corresponding button in the example app
- [ ] Each button is in the correct feature category section
- [ ] Code follows the auto-loaded skill's patterns (dual feedback, naming conventions)
- [ ] Updated APIs have two actions (original + new parameter)
- [ ] Action constants added to `constants.js`

## Error Handling
- If the example app structure has changed significantly, report the unexpected structure
- If an API has unclear parameters, use sensible defaults and add a comment noting the assumption
- If a feature category doesn't exist yet, create it as a new category in `accordionData`
