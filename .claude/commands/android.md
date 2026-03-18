Run an Android Gradle task for the LegacyVault native Android app.

The Android project lives at `src/LegacyVault.Android/`.

## Default behavior (no arguments)
Run the debug unit tests:
```
cd src/LegacyVault.Android && ./gradlew testDebugUnitTest
```

## With arguments: $ARGUMENTS

Pass any valid Gradle task directly, e.g.:
- `build` → `./gradlew assembleDebug`
- `test` → `./gradlew testDebugUnitTest`
- `release` → `./gradlew assembleRelease`
- `lint` → `./gradlew lint`
- `clean` → `./gradlew clean`
- `install` → `./gradlew installDebug` (requires connected device/emulator)

If $ARGUMENTS is provided, run:
```
cd src/LegacyVault.Android && ./gradlew $ARGUMENTS
```

## After running
1. Report whether the task succeeded or failed
2. List any **build errors** with file and line references
3. List any **test failures** with the test name and failure message
4. List any **lint warnings** flagged as errors
5. If there are fixable issues (e.g. unused imports, deprecation warnings), offer to fix them
