# Task 6 Report: Pure `pnpm` Multi-Stage Dockerfile & Verification

- **Task**: Task 6 - Pure `pnpm` Multi-Stage Dockerfile & Verification
- **Status**: DONE
- **Date**: 2026-10-02
- **Commit**: `0a6f2ecc29986fce697217c2cdacea32fe14c624` (`feat(bot): configure pure pnpm multi-stage dockerfile and documentation`)

---

## 1. Accomplishments & Implemented Changes

1. **`infra/discord-bot/Dockerfile`**:
   - Implemented an ultra-clean, multi-stage Docker build utilizing `node:24-slim` with Corepack activating `pnpm@11.20.0`.
   - Stripped away all legacy C++ toolchains (`python3`, `make`, `g++`, `node-gyp`), prebuilt `.node` copying hacks, and obsolete SQLite data volume directories.
   - Stage 1 (`builder`): copies `package.json`, `pnpm-lock.yaml`, and `pnpm-workspace.yaml`, installs with `pnpm install --frozen-lockfile`, and builds with `pnpm run build` (`eve build`).
   - Stage 2 (`runner`): installs production dependencies via `pnpm install --prod --frozen-lockfile`, copies output bundle and runtime files (`.output`, `.eve`, `agent`, `runner.mjs`, `tsconfig.json`), ensures unprivileged permissions via `chown -R node:node /app`, switches to `USER node`, and runs `pnpm run start`.

2. **`infra/discord-bot/.dockerignore`**:
   - Cleaned to exclude `node_modules`, `pnpm-debug.log`, `.env`, build artifacts (`dist`, `.output`, `.eve`), tests, and VCS folders while retaining `.env.example`.

3. **`infra/discord-bot/README.md`**:
   - Completely rewritten to document the modernized architecture:
     - Multi-stage container architecture with `node:24-slim`, Corepack, and `pnpm@11.20.0`.
     - Standalone PocketBase backend integration with web admin UI for zero-code FAQ curation.
     - Real-time Discord Gateway `@Cobbleloots Assistant` mention and thread reply listener with automated message chunking (<= 1900 chars).
     - Thread Context Engine fetching up to 20 chronological messages.
     - Bilingual conversational behavior (English default, Spanish adaptability).
     - Full grounding suite (`search_docs`, `search_faqs`, `list_faqs`, `get_faq`, `save_faq`, `delete_faq`, `inspect_code`, `get_releases`, `ask_question`).
     - Up-to-date environment variable reference table.
     - Local development and testing runbook with `pnpm`.
     - Step-by-step production deployment guide for Coolify v4 and Cloudflare reverse proxy.

4. **Eve Build & Container Compatibility Fixes**:
   - `infra/discord-bot/pnpm-workspace.yaml`: Configured `allowBuilds` for `esbuild: true` and explicitly disallowed native build scripts (`@mongodb-js/zstd`, `node-liblzma`) to ensure strict zero-compiler builds.
   - `infra/discord-bot/package.json` & `pnpm-lock.yaml`: Re-added `just-bash` to devDependencies (which Eve requires for sandbox template prewarming inside headless Docker build containers without Docker-in-Docker daemons).
   - `infra/discord-bot/agent/agent.ts`: Aligned `defineAgent` configuration with Eve's strict public schema (`defaultTools: false`, non-enumerable attachment of `instructions` and `tools` for unit test verification).
   - `infra/discord-bot/agent/tools/*.ts`: Removed redundant and invalid `parameters` property from `delete_faq.ts`, `get_faq.ts`, `list_faqs.ts`, `save_faq.ts`, and `search_faqs.ts` so all tools conform strictly to Eve's `inputSchema` specification.

---

## 2. Verification Results

### Unit & Integration Test Suite (`pnpm test`)
```text
 ✓ tests/pb-client.test.ts (11 tests) 13ms
 ✓ tests/hitl-tools.test.ts (3 tests) 5ms
 ✓ tests/faq-tools.test.ts (12 tests) 10ms
 ✓ tests/agent-config.test.ts (6 tests) 4ms
 ✓ tests/gateway.test.ts (24 tests) 9ms
 ✓ tests/tools.test.ts (8 tests) 606ms

 Test Files  6 passed (6)
      Tests  64 passed (64)
   Duration  1.33s
```
- 100% of 64 tests passing.

### TypeScript Typecheck (`pnpm run typecheck`)
- Command: `tsc --noEmit`
- Result: Exited with code 0. Zero type errors.

### Local Eve Build (`pnpm run build`)
- Command: `eve build`
- Result: Exited with code 0. Generated Nitro server output at `.output`.

### Docker Image Build (`docker build -t test-pocketbase-bot .`)
- Command: `docker build -t test-pocketbase-bot .`
- Result: Exited with code 0. Image `test-pocketbase-bot:latest` successfully built and tagged.

### Container Runtime Smoke Test (`docker run`)
- Command: `docker run --rm -e DISABLE_GATEWAY=true -e POCKETBASE_URL=http://127.0.0.1:8090 test-pocketbase-bot`
- Log output:
  ```text
  $ node runner.mjs
  [Runner] Starting Eve server from .output/server/index.mjs...
  ➜ Listening on: http://localhost:3000/ (all interfaces)
  [Runner] Eve server is healthy and listening on port 3000!
  [Runner] DISCORD_BOT_TOKEN not provided or DISABLE_GATEWAY=true. Gateway listener skipped.
  ```
- Result: Successfully started as non-root `node` user, bound to port 3000, and verified healthy.

---

## 3. Concerns & Observations

None. All legacy SQLite dependencies, native build tools, and configuration artifacts have been completely eliminated. The container builds purely from source using `pnpm` and runs reliably.
