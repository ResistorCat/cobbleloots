# Task 1 Brief: Package Scaffolding Migration to pnpm & Legacy SQLite Cleanup

## Context & Project Role
This is Task 1 of the Cobbleloots Discord AI Support Bot re-architecture. In this task, you will migrate the project from npm to `pnpm` (`pnpm@11.20.0`), remove all legacy SQLite code (`better-sqlite3`, memory backends, test suites), install pure JavaScript dependencies including `pocketbase@^0.26.0`, and generate `pnpm-lock.yaml`.

## Global Constraints
- Target directory is strictly isolated within `infra/discord-bot/`. Zero edits to Minecraft Java/Kotlin code, Gradle scripts, or `CHANGELOG.md`.
- No C++ compilers or native `.node` modules.
- Use `pnpm` (Corepack / system `pnpm`).

## Exact Actions Required

### 1. Update `infra/discord-bot/package.json`
Write exact contents to `infra/discord-bot/package.json`:
```json
{
  "name": "discord-bot",
  "version": "1.0.0",
  "type": "module",
  "packageManager": "pnpm@11.20.0",
  "imports": {
    "#*": "./agent/*"
  },
  "scripts": {
    "dev": "eve dev",
    "build": "eve build",
    "start": "node runner.mjs",
    "typecheck": "tsc --noEmit",
    "test": "vitest run"
  },
  "dependencies": {
    "@vercel/connect": "^2.2.0",
    "ai": "^7.0.105",
    "discord.js": "^14.27.0",
    "dotenv": "^17.2.3",
    "eve": "^0.68.0",
    "pocketbase": "^0.26.0",
    "zod": "^4.5.4"
  },
  "devDependencies": {
    "@types/node": "^24.0.0",
    "typescript": "^7.0.2",
    "vitest": "^3.0.0"
  },
  "engines": {
    "node": ">=22.0.0"
  }
}
```

### 2. Delete Legacy SQLite Files and Test Suites
Remove these files if they exist:
- `infra/discord-bot/package-lock.json`
- `infra/discord-bot/agent/lib/sqlite-memory-backend.ts`
- `infra/discord-bot/agent/memory/player-profile.ts`
- `infra/discord-bot/agent/lib/db.ts`
- `infra/discord-bot/agent/lib/faq-store.ts`
- `infra/discord-bot/tests/faq-store.test.ts`
- `infra/discord-bot/tests/session-state.test.ts`

### 3. Run `pnpm install` in `infra/discord-bot`
Execute `pnpm install` inside `infra/discord-bot/`.
This will generate `infra/discord-bot/pnpm-lock.yaml`.

### 4. Commit Your Work
```bash
git add infra/discord-bot/
git commit -m "chore(bot): migrate package manager to pnpm and remove legacy sqlite dependencies"
```

## Report Contract
Write your full report to:
`c:\Users\franc\GitHub\cobbleloots\.worktrees\ripio\dev-30-cobbleloots-implementar-bot-de-soporte-de-discord-con-eve\.superpowers\sdd\2026-10-02-discord-bot-pocketbase-plan\task-1-report.md`

Then report back to the controller with ONLY:
- Status: DONE | DONE_WITH_CONCERNS | BLOCKED | NEEDS_CONTEXT
- Commits created (short SHA + subject)
- One-line test/install summary
- Your concerns, if any
- The report file path
