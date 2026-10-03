# Task 6 Brief: Pure `pnpm` Multi-Stage Dockerfile & Local Build Verification

## Context & Goal
This is Task 6 of the PocketBase & pnpm re-architecture plan (`.superpowers/plans/2026-10-02-discord-bot-pocketbase-plan.md`).
In this final task, you will:
1. Update `infra/discord-bot/Dockerfile` to an ultra-clean multi-stage build powered by `pnpm@11.20.0` on `node:24-slim`. It must contain zero C++ compilers, zero native module build tools (`node-gyp`, `python3`, `make`, `g++`), and zero `.node` binary copying hacks.
2. Update `infra/discord-bot/.dockerignore` to cleanly ignore unneeded artifacts.
3. Update `infra/discord-bot/README.md` to document the new architecture:
   - Pure `pnpm` package management (`pnpm@11.20.0`).
   - PocketBase backend decoupling (Coolify PocketBase microservice with web admin dashboard).
   - Discord Admin FAQ management (`list_faqs`, `get_faq`, `save_faq`, `delete_faq`).
   - Thread Context Engine (ingesting up to 20 messages in Discord threads).
   - Bilingual support policy (English community default, natural Spanish when addressed in Spanish).
   - Remove all obsolete SQLite references (`better-sqlite3`, `/app/data/bot.db`, `DATABASE_PATH`).
4. Run full test suite & TypeScript typecheck (`pnpm test`, `pnpm run typecheck`).
5. Verify Docker build succeeds cleanly (`docker build -t test-pocketbase-bot infra/discord-bot`).

---

## Exact File Specifications

### 1. `infra/discord-bot/Dockerfile`
```dockerfile
# ==========================================
# Stage 1: Build & Bundle
# ==========================================
FROM node:24-slim AS builder
WORKDIR /app

# Enable Corepack and pnpm
RUN corepack enable && corepack prepare pnpm@11.20.0 --activate

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm run build

# ==========================================
# Stage 2: Production Runtime
# ==========================================
FROM node:24-slim AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000

# Enable Corepack and pnpm for production runtime
RUN corepack enable && corepack prepare pnpm@11.20.0 --activate

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --prod --frozen-lockfile

# Copy built application output, agent directory, runner, and eve metadata from builder stage
COPY --from=builder /app/.output ./.output
COPY --from=builder /app/.eve ./.eve
COPY --from=builder /app/agent ./agent
COPY --from=builder /app/runner.mjs ./runner.mjs
COPY --from=builder /app/tsconfig.json ./tsconfig.json

USER node
EXPOSE 3000

CMD ["pnpm", "run", "start"]
```

### 2. `infra/discord-bot/.dockerignore`
```text
node_modules/
pnpm-debug.log
.env
.env.*
!.env.example
dist/
.eve/
.output/
tests/
.git/
.github/
.vscode/
```

### 3. `infra/discord-bot/README.md`
Rewrite `infra/discord-bot/README.md` to comprehensively document:
- **Architecture Overview**:
  - Multi-stage container on `node:24-slim` with Corepack & `pnpm@11.20.0`.
  - External PocketBase persistence for FAQs with web admin UI (decoupled from the bot container).
  - Discord Gateway listener for `@Cobbleloots Assistant` mentions and replies.
  - Thread Context Engine ingesting up to 20 chronological messages.
  - Bilingual conversational engine (English by default, Spanish when addressed in Spanish).
  - Grounding tools: `search_docs`, `search_faqs`, `list_faqs`, `get_faq`, `save_faq`, `delete_faq`, `inspect_code`, `get_releases`, `ask_question`.
- **Environment Variables Table**:
  - `DISCORD_APPLICATION_ID` (Required)
  - `DISCORD_BOT_TOKEN` (Required)
  - `DISCORD_PUBLIC_KEY` (Required for HTTP interactions)
  - `DISCORD_ADMIN_IDS` (Optional, comma-separated snowflakes)
  - `POCKETBASE_URL` (Required, e.g. `http://pocketbase:8090` or `https://pb.yourdomain.com`)
  - `POCKETBASE_ADMIN_EMAIL` (Required)
  - `POCKETBASE_ADMIN_PASSWORD` (Required)
  - `DEFAULT_MODEL` (Default: `mistral/mistral-nemo`)
  - `AI_GATEWAY_TOKEN` (Optional, for Vercel AI Gateway)
  - `GOOGLE_GENERATIVE_AI_API_KEY` (Conditional, for Gemini)
  - `OPENAI_API_KEY` / `ANTHROPIC_API_KEY` (Conditional)
  - `GITHUB_TOKEN` (Optional, for higher rate limits)
- **Local Development Runbook**:
  - `pnpm install`
  - `pnpm test`
  - `pnpm run typecheck`
  - `pnpm run dev` / `pnpm run build` / `pnpm run start`
- **Coolify v4 Deployment Runbook**:
  - Deploy PocketBase as a standalone service in Coolify (1-click PocketBase template or `ghcr.io/muchobien/pocketbase:latest`).
  - Deploy Discord AI Assistant service with `Dockerfile` build pack pointing to `infra/discord-bot`.
  - Configure environment variables and network connectivity between the services.
  - Verification checklist and troubleshooting.

---

## Verification Commands
In `infra/discord-bot`:
1. `pnpm test` -> 100% tests pass.
2. `pnpm run typecheck` -> 0 TypeScript errors.
3. `docker build -t test-pocketbase-bot infra/discord-bot` -> Clean Docker build finishes with code 0.

## Commit Instructions
```bash
git add infra/discord-bot/Dockerfile infra/discord-bot/.dockerignore infra/discord-bot/README.md
git commit -m "feat(bot): configure pure pnpm multi-stage dockerfile and documentation"
```

## Report Instructions
Write report to:
`c:\Users\franc\GitHub\cobbleloots\.worktrees\ripio\dev-30-cobbleloots-implementar-bot-de-soporte-de-discord-con-eve\.superpowers\sdd\2026-10-02-discord-bot-pocketbase-plan\task-6-report.md`
Report back with:
- Status: DONE | DONE_WITH_CONCERNS | BLOCKED
- Commits created
- Test and Docker build summary
- Concerns (if any)
- Report file path
