Start the LegacyVault development stack (API + Web).

Run from the repo root:
```
docker-compose up
```

Or if there's a non-Docker dev mode (check for a `package.json` script or `dotnet run` setup):
- API: `dotnet run` in `src/LegacyVault.API/`
- Web: `npm run dev` in `src/LegacyVault.Web/`

Report which services started, which ports they're on, and any startup errors.
