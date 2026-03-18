# Legacy Vault

A self-hosted personal knowledge vault with client-side AES-256-GCM encryption. Store notes, passwords, recipes, reminders, shopping lists, home inventory, and quotes — all encrypted at rest, never readable by the server.

---

## Projects

| Project | Stack | Description |
|---------|-------|-------------|
| `src/LegacyVault.API` | ASP.NET Core / C# | REST API, SQLite, JWT auth, Dockerized |
| `src/LegacyVault.Web` | React + TypeScript (Vite) | SPA, Redux Toolkit, client-side crypto |
| `src/LegacyVault.Mobile` | Expo React Native | iOS/Android app, Expo Router v4 |
| `src/LegacyVault.Android` | Android (Java) | Native Android shell |

---

## Features

- **Encrypted Vaults** — AES-256-GCM encryption with PBKDF2 key derivation; vault password never leaves the device
- **Page Types** — Note, Password, Recipe, Quote, Home Inventory, Reminder, Shopping List
- **General & Vault categories** — General vaults store plain pages; Vault categories always encrypt content
- **Attachments** — File/image uploads per page
- **Reminders** — Recurring reminders with in-app notifications
- **Backup & Restore** — Full encrypted database backup/restore
- **Global Search** — Full-text search across all decrypted page content
- **Dark/Light theme** — System-aware with manual override
- **Self-hosted** — Runs entirely via Docker Compose; no cloud dependency

---

## Quick Start

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### Setup

1. Clone the repo and copy the env template:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` with your values:
   ```env
   JWT_SECRET=<64-character random string>
   VAULT_UPLOADS_PATH=C:/Users/YourName/Documents/.vault/uploads
   VAULT_BACKUPS_PATH=C:/Users/YourName/Documents/.vault/backups
   ```

3. Start the stack:
   ```bash
   docker-compose up
   ```

4. Open [http://localhost:8080](http://localhost:8080) and register your account.

---

## Development

### API (ASP.NET Core)
```bash
cd src/LegacyVault.API
dotnet run
```

### Web (Vite)
```bash
cd src/LegacyVault.Web
npm install
npm run dev
```

### Mobile (Expo)
```bash
cd src/LegacyVault.Mobile
npm install
npx expo start
```

---

## Architecture

```
Browser / Mobile
      │
      │  HTTPS
      ▼
  LegacyVault.Web (React SPA)
      │  ← client-side AES-256-GCM encrypt/decrypt
      │  JWT Bearer
      ▼
  LegacyVault.API (ASP.NET Core)
      │
      ▼
  SQLite (Docker volume)
```

Encryption keys are derived from the vault password via PBKDF2 and held in memory only. The server stores only ciphertext — it cannot read vault contents.

---

## Environment Variables

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | JWT signing key — minimum 64 characters, random |
| `VAULT_UPLOADS_PATH` | Host path for uploaded attachments |
| `VAULT_BACKUPS_PATH` | Host path for database backups |

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
