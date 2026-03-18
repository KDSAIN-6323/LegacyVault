Scaffold a new LegacyVault page type end-to-end. The page type name is: $ARGUMENTS

Do all of the following steps in order:

## 1. C# API model
In `src/LegacyVault.API/`, check how existing page types are modeled. Add the new content type to any relevant enums or constants (e.g. the PageType enum/string constants). If there is a content schema or validation, add an entry for the new type.

## 2. TypeScript types — Web
In `src/LegacyVault.Web/src/types/index.ts`:
- Add a `<Name>Content` interface with sensible fields
- Add the type name to the `PageType` union

## 3. TypeScript types — Mobile
In `src/LegacyVault.Mobile/src/types.ts`:
- Add the same `<Name>Content` interface
- Add the type name to the `PageType` union

## 4. Kotlin domain model — Android
In `src/LegacyVault.Android/app/src/main/kotlin/com/legacyvault/app/domain/model/PageContent.kt`:
- Add a new `@Serializable data class <Name>` inside the `PageContent` sealed class
- Fields must match the TypeScript interface exactly (names in camelCase, same optionality)
- Add the new type to the `PageType` enum in `enums/PageType.kt` with an appropriate `label` and `icon`

## 5. Web editor component
Create `src/LegacyVault.Web/src/components/editors/<Name>Editor.tsx` and a matching `<Name>Editor.css`.
- Follow the pattern of existing editors (ReminderEditor, RecipeEditor, etc.)
- The editor should read/write a JSON string via `content` prop + `onChange` callback
- Register the editor in whichever switch/map dispatches editors by page type

## 6. Mobile detail view
In `src/LegacyVault.Mobile/app/app/category/[id]/page/[pageId].tsx`:
- Add a `<Name>View` render block inside the content-type switch
- Display all fields in a readable format consistent with other view blocks in that file

## 7. Android editor screen
Create `src/LegacyVault.Android/app/src/main/kotlin/com/legacyvault/app/ui/pages/editors/<Name>Editor.kt`:
- A `@Composable` function `<Name>Editor(content: PageContent.<Name>, onContentChange: (PageContent.<Name>) -> Unit)`
- Follow Material 3 patterns matching the other editor composables in that directory
- The editor writes back a `PageContent.<Name>` on every field change — no local submit button (save is handled by the parent `PageDetailScreen`)

## 8. Verification checklist
After making all changes, list:
- [ ] C# type registered
- [ ] Web TypeScript type added
- [ ] Mobile TypeScript type added
- [ ] Android Kotlin `PageContent` subclass added
- [ ] Android `PageType` enum entry added
- [ ] Web editor created and wired in
- [ ] Mobile detail view added
- [ ] Android editor composable created
- [ ] Any fields that should be encrypted are handled correctly
