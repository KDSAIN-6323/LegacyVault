Run a Flutter task for the LegacyVault iOS app.

The Flutter project lives at `src/LegacyVault.iOS/`. It uses Dart, flutter_riverpod, go_router, sqflite, and pointycastle.

## Default behavior (no arguments)
Run the unit tests:
```
cd src/LegacyVault.iOS && flutter test
```

## With arguments: $ARGUMENTS

Pass any recognized task name or raw `flutter` arguments:

| Argument     | Command                                          |
|--------------|--------------------------------------------------|
| `test`       | `flutter test`                                   |
| `build`      | `flutter build ios --no-codesign`                |
| `run`        | `flutter run -d iPhone`                          |
| `get`        | `flutter pub get`                                |
| `analyze`    | `flutter analyze`                                |
| `format`     | `dart format lib/`                               |
| `clean`      | `flutter clean`                                  |

If $ARGUMENTS is a recognized shorthand above, expand it. Otherwise pass it through directly:
```
cd src/LegacyVault.iOS && flutter $ARGUMENTS
```

## After running
1. Report whether the task succeeded or failed.
2. List any **errors** with file path and line reference.
3. List any **test failures** with the test name and failure message.
4. List any **analysis warnings** or lint issues.
5. If there are fixable issues, offer to fix them.

---

## Dart / Flutter coding conventions for this project

When writing or modifying Dart code in `src/LegacyVault.iOS/`:

### Async naming
- All `Future<T>` methods must use the `Async` suffix: `loadDataAsync()`, `savePageAsync()`, `deleteAsync()`.
- `Stream<T>` methods do **not** use the suffix (streams are inherently async by convention).
- Boolean async methods: `isValidAsync()`, `existsAsync()`.

### Architecture
- State lives in `StateNotifier` subclasses (Riverpod). Never call async work from `build()`.
- Repositories (`CategoryRepository`, `PageRepository`, `BackupRepository`) own all DB access — notifiers call repositories, never `DatabaseHelper` directly.
- `CryptoServiceImpl` is the only place that touches pointycastle. All encrypt/decrypt calls go through `CryptoService` (abstract interface).

### Crypto contract (must not change)
- PBKDF2-SHA256, 310,000 iterations, 32-byte salt, 32-byte key.
- AES-256-GCM, 12-byte IV, 16-byte auth tag.
- Wire format: Base64(`[16-byte tag][ciphertext]`) — tag **prepended**.
- pointycastle outputs `[ciphertext|tag]` — swap to `[tag|ciphertext]` on encrypt.
- Decrypt: try mobile format first, fallback to web format.
- Never change this contract — it must stay byte-compatible with the Android and Web apps.

### General style
- Prefer `const` constructors wherever possible.
- Use named parameters for any function with more than two parameters.
- Keep widgets small; extract sub-widgets rather than nesting deeply.
- Do not store decrypted content in state longer than the screen that needs it.
