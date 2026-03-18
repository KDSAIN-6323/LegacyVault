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

## 4. Web editor component
Create `src/LegacyVault.Web/src/components/editors/<Name>Editor.tsx` and a matching `<Name>Editor.css`.
- Follow the pattern of existing editors (ReminderEditor, RecipeEditor, etc.)
- The editor should read/write a JSON string via `content` prop + `onChange` callback
- Register the editor in whichever switch/map dispatches editors by page type

## 5. Mobile detail view
In `src/LegacyVault.Mobile/app/app/category/[id]/page/[pageId].tsx`:
- Add a `<Name>View` render block inside the content-type switch
- Display all fields in a readable format consistent with other view blocks in that file

## 6. Verification checklist
After making all changes, list:
- [ ] C# type registered
- [ ] Web TypeScript type added
- [ ] Mobile TypeScript type added
- [ ] Web editor created and wired in
- [ ] Mobile detail view added
- [ ] Any fields that should be encrypted are handled correctly
