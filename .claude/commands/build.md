Run a full Docker build of the LegacyVault stack and summarize the results.

Run this command from the repo root:
```
docker-compose build --no-cache 2>&1
```

Then:
1. Report whether the build succeeded or failed
2. List any **warnings** with the file and line reference (e.g. CS warnings, SYSLIB obsolete warnings)
3. List any **errors** that caused the build to fail, with context
4. If there are fixable warnings (e.g. obsolete API usage), offer to fix them
