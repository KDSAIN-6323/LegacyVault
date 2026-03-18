# Changelog

All notable changes to Legacy Vault will be documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased]

---

## [0.1.0] — 2026-03-18

### Added
- Initial project scaffold: `LegacyVault.API`, `LegacyVault.Web`, `LegacyVault.Mobile`, `LegacyVault.Android`
- ASP.NET Core REST API with JWT authentication and refresh tokens
- SQLite database via Entity Framework Core with migrations
- Client-side AES-256-GCM encryption with PBKDF2 key derivation
- Per-category vault passwords with in-memory key cache
- Page types: Note, Password, Recipe, Quote, Home Inventory, Reminder, Shopping List
- General and Vault category types (Vault categories always encrypted)
- File/image attachment support
- Recurring reminders with in-app notifications
- Full-text global search
- Backup and restore functionality
- React + TypeScript SPA with Redux Toolkit state management
- Expo React Native mobile app with Expo Router v4
- Dark/Light theme with system-aware toggle
- Docker Compose self-hosted deployment (API + Web + SQLite volume)
- Nginx production config for the web container
- Branding: "Legacy Vault", purple accent (`#7c3aed`), inline SVG safe/vault logo
