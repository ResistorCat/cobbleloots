# Cobbleloots Discord AI Assistant

An intelligent, context-aware Discord assistant for the [Cobbleloots](https://github.com/ResistorCat/cobbleloots) Minecraft mod community, built using the [Eve framework](https://github.com/run-eve/eve).

The bot provides real-time community support for players and maintainers, answering questions about mod mechanics, configuration, loot ball drops, compatibility, and release history.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       Discord Gateway Listener                              │
│         (@Cobbleloots mentions & thread replies, chunked <= 1900 chars)     │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Eve AI Assistant Server                            │
│                 (Multi-stage node:24-slim container via pnpm)               │
└───────┬──────────────────────────────┬──────────────────────────────┬───────┘
        │                              │                              │
 ┌──────▼──────┐                ┌──────▼──────┐                ┌──────▼──────┐
 │  PocketBase │                │  Grounding  │                │    HITL     │
 │  Backend    │                │  Tools      │                │  Tooling    │
 └──────┬──────┘                └──────┬──────┘                └──────┬──────┘
        │                              │                              │
  ├── faqs collection            ├── search_docs                └── ask_question
  │   (Full CRUD via REST API)   │   (Local / MkDocs CDN)           (Discord buttons)
  └── Web Admin Dashboard        ├── inspect_code
      (No native SQLite builds)  │   (Local / GitHub tag)
                                 └── get_releases
                                     (GitHub API / CHANGELOG)
```

- **Pure `pnpm` Package Management**: Built on `node:24-slim` with Corepack and `pnpm@11.20.0`. Fast, deterministic, reproducible installs with zero native C++ build tools (`node-gyp`, `python3`, `g++`, `make`) required.
- **PocketBase Backend Decoupling**: Replaces embedded SQLite with a lightweight, standalone [PocketBase](https://pocketbase.io/) microservice. The bot interacts purely via REST API with `pocketbase`, providing an out-of-the-box web admin UI (`/_/`) for instant FAQ viewing and curation.
- **Discord Gateway Listener**: Listens in real time for direct `@Cobbleloots Assistant` mentions and reply chains, maintaining a typing indicator while streaming and chunking long responses (up to 1900 characters per Discord message).
- **Thread Context Engine**: When mentioned inside a Discord thread or forum post, the engine automatically fetches up to 20 recent messages chronologically to form complete context before invoking the model.
- **Bilingual Conversational Engine**: By default, replies to the global community in English. When addressed or questioned in Spanish, naturally switches to fluent Spanish while preserving English technical terms, commands, and file paths.
- **Dual-Persona Capability**:
  - **Player Mode (default)**: Delivers clear, friendly gameplay guidance grounded in official documentation and approved FAQs without internal code jargon. Implements the *Anti-Rush Protocol* to ask clarifying questions before answering underspecified bug reports.
  - **Maintainer/Admin Mode**: For authorized administrators (configured via `DISCORD_ADMIN_IDS`), provides deep technical answers referencing Java classes, line numbers, loader architecture (Common vs Fabric vs NeoForge), and repository inspection.
- **Complete Suite of Grounding Tools**:
  - `search_docs`: Searches local MkDocs markdown files (`docs/`) if present, falling back to the public precomputed MkDocs search index (`https://resistorcat.github.io/cobbleloots/search/search_index.json`).
  - `search_faqs`: Semantic and keyword search across approved FAQs in PocketBase.
  - `list_faqs`: Lists existing FAQs with optional category or approval status filters.
  - `get_faq`: Retrieves a single FAQ by ID.
  - `save_faq`: Creates or updates FAQ entries in PocketBase (auto-approved for admins).
  - `delete_faq`: Deletes an FAQ entry by ID.
  - `inspect_code`: Inspects source files across `common/`, `fabric/`, and `neoforge/` subprojects (restricted to safe paths, dynamically fetched from the latest published release tag on GitHub or read locally when in the monorepo).
  - `get_releases`: Queries the GitHub Releases REST API for latest releases and release notes, falling back to local `CHANGELOG.md` and `.changelog/` fragments.
  - `ask_question`: Renders interactive Discord button components for users to clarify environment details (e.g. loader, Minecraft version).

---

## Prerequisites

- **Node.js**: `>= 22.0.0`
- **pnpm**: `>= 11.0.0` (Corepack recommended: `corepack enable`)
- **PocketBase**: Standalone instance (v0.23+ or v0.26+) accessible over HTTP/HTTPS.
- A Discord Application configured in the [Discord Developer Portal](https://discord.com/developers/applications).
- API credentials for your chosen LLM provider (Vercel AI Gateway, Google Gemini, OpenAI, or Anthropic).

---

## Discord Developer Portal Setup

1. **Create Application**:
   - Go to [Discord Developer Portal](https://discord.com/developers/applications).
   - Click **New Application**, name it (e.g., `Cobbleloots Assistant`), and accept terms.
2. **Retrieve Application ID & Public Key**:
   - In the **General Information** tab:
     - Copy the **Application ID** (`DISCORD_APPLICATION_ID`).
     - Copy the **Public Key** (`DISCORD_PUBLIC_KEY`).
3. **Configure Bot & Token**:
   - Navigate to the **Bot** tab on the left sidebar.
   - Click **Reset Token** to copy the token (`DISCORD_BOT_TOKEN`).
   - Under **Privileged Gateway Intents**, enable **Message Content Intent** so the bot can listen to `@Cobbleloots Assistant` mentions and replies in chat channels and threads.
4. **Configure Interactions Endpoint URL (Optional for Slash Commands/Webhooks)**:
   - In **General Information**, locate **Interactions Endpoint URL**.
   - Eve exposes HTTP interactions at `/eve/v1/discord`.
   - Set the URL to:
     ```text
     https://<your-public-domain>/eve/v1/discord
     ```
   - For local development, expose your local port using a tunnel (e.g., `ngrok http 3000` or `cloudflared tunnel`) and provide `https://<tunnel-subdomain>/eve/v1/discord`. Discord will send a `PING` to verify the public key signature automatically.
5. **Invite Bot to Server**:
   - Navigate to **OAuth2** -> **URL Generator**.
   - Scopes: Select `bot` and `applications.commands`.
   - Bot Permissions: Select `Send Messages`, `Send Messages in Threads`, `Embed Links`, `Use Slash Commands`, and `Read Message History`.
   - Copy the generated URL and open it in a browser to invite the bot to your Discord server.

---

## How to Talk to the Bot

Once invited to your Discord server:
- **Mention the bot directly**: `@Cobbleloots Assistant where do I find the Moon Ball?`
- **Reply to any previous bot message**: The assistant seamlessly continues the conversation with context.
- **Thread Context Support**: Mention the bot inside any thread or forum post, and it automatically reads the preceding messages (up to 20) to understand the full conversation history.
- **Spanish Inquiries**: `@Cobbleloots Assistant ¿cómo configuro los drops de pesca?` — the assistant answers fluently in Spanish.
- **Empty mention**: If mentioned with no question (`@Cobbleloots Assistant`), the bot replies with a welcoming greeting explaining what it can do.

---

## Environment Variables

Create a `.env` file in `infra/discord-bot` by copying `.env.example`:

```bash
cp .env.example .env
```

| Variable | Required | Default | Description |
| :--- | :---: | :---: | :--- |
| `DISCORD_APPLICATION_ID` | **Yes** | — | Discord Application ID from Developer Portal. |
| `DISCORD_BOT_TOKEN` | **Yes** | — | Discord Bot Token from Developer Portal. |
| `DISCORD_PUBLIC_KEY` | **Yes** | — | Discord Public Key used for interaction webhook verification. |
| `DISCORD_ADMIN_IDS` | No | `""` | Comma-separated Discord user snowflakes with maintainer/admin permissions. |
| `POCKETBASE_URL` | **Yes** | `http://127.0.0.1:8090` | URL of the PocketBase service (e.g., `http://pocketbase:8090` or `https://pb.yourdomain.com`). |
| `POCKETBASE_ADMIN_EMAIL` | **Yes** | — | PocketBase admin or superuser email for schema bootstrap and authenticated mutations. |
| `POCKETBASE_ADMIN_PASSWORD` | **Yes** | — | PocketBase admin or superuser password. |
| `DEFAULT_MODEL` | No | `mistral/mistral-nemo` | Default LLM model string for the agent. |
| `AI_GATEWAY_TOKEN` | No | — | Optional Vercel AI Gateway bearer token for model routing. |
| `GOOGLE_GENERATIVE_AI_API_KEY` | Conditional | — | Required if using Google Gemini models (`google/*`). |
| `OPENAI_API_KEY` | Conditional | — | Required if using OpenAI models (`openai/*`). |
| `ANTHROPIC_API_KEY` | Conditional | — | Required if using Anthropic models (`anthropic/*`). |
| `GITHUB_REPO` | No | `ResistorCat/cobbleloots` | GitHub repository identifier for remote release & code queries. |
| `GITHUB_TOKEN` | No | — | Optional GitHub Personal Access Token for increased API rate limits in production. |
| `DISABLE_GATEWAY` | No | `false` | Set to `true` to skip the Discord Gateway listener (HTTP interactions only). |
| `PORT` | No | `3000` | Port for the Eve HTTP server. |

---

## Local Development Runbook

### 1. Install Dependencies
```bash
corepack enable
pnpm install
```

### 2. Run Tests
Execute the Vitest test suite covering PocketBase client, FAQ tools, domain tools, HITL workflows, Gateway message handling, and agent configuration:
```bash
pnpm test
```
To run a specific test file:
```bash
pnpm exec vitest run tests/faq-tools.test.ts
```

### 3. Typecheck
Verify strict TypeScript compilation with zero errors:
```bash
pnpm run typecheck
```

### 4. Start Local Development Server
Start the Eve live development server with hot reload:
```bash
pnpm run dev
```
The server listens for incoming HTTP interaction webhooks at `http://localhost:3000/eve/v1/discord`.

### 5. Production Build & Run
To compile and run the production server locally with the gateway runner:
```bash
pnpm run build
pnpm run start
```

---

## Admin FAQ Management

The assistant provides full FAQ management capabilities backed by PocketBase:

### Available Tools
- `search_faqs`: Query FAQs by keyword or topic. Returns approved entries to normal players and all matching entries to admins.
- `list_faqs`: List FAQs in the collection. Supports filtering by `category` and `approved_only` boolean.
- `get_faq`: Fetch complete details of a specific FAQ entry by its PocketBase ID.
- `save_faq`: Create or update an FAQ entry. When called by an admin (or via web UI), FAQs are automatically approved. Normal player submissions are staged with `approved: 0`.
- `delete_faq`: Permanently delete an FAQ entry by ID (restricted to authorized admins).

### Management via PocketBase Web Admin Dashboard
Authorized maintainers can manage FAQs visually without touching code:
1. Open the PocketBase web UI at `https://<pocketbase-domain>/_/`.
2. Log in with your admin credentials.
3. Browse the `faqs` collection to create, edit, approve, or delete questions and answers directly.
4. Changes are immediately available to the Discord assistant on its next inquiry.

---

## Production Deployment with Coolify v4

The bot is packaged as a pure multi-stage Docker container on `node:24-slim` with Corepack and `pnpm@11.20.0`. It is deployed on a self-hosted server running [Coolify v4](https://coolify.io/) alongside PocketBase.

### Step 1: Deploy PocketBase Service
1. In your Coolify dashboard, select your Project & Environment.
2. Click **+ New** -> **Service** -> **PocketBase** (or create a custom application using `ghcr.io/muchobien/pocketbase:latest`).
3. Set up the persistent storage volume for PocketBase data (`/pb_data`).
4. Assign a public domain (e.g., `https://pb.yourdomain.com`) or take note of the internal Docker network hostname (e.g., `http://pocketbase:8090`).
5. Open `https://pb.yourdomain.com/_/` in your browser and create the initial superuser account (`admin@yourdomain.com` / `<secure-password>`).

### Step 2: Deploy Discord AI Assistant Service
1. In Coolify, click **+ New** -> **Application** -> **Public Repository** (or GitHub App).
2. Repository URL: `https://github.com/ResistorCat/cobbleloots`
3. Branch: `main` (or release branch)
4. Configure application build settings:
   - **Build Pack**: `Dockerfile`
   - **Base Directory**: `infra/discord-bot`
   - **Dockerfile Location**: `/Dockerfile` (relative to `infra/discord-bot`)
   - **Ports Exposes**: `3000`
5. Configure Environment Variables:
   ```text
   DISCORD_APPLICATION_ID=<your-app-id>
   DISCORD_BOT_TOKEN=<your-bot-token>
   DISCORD_PUBLIC_KEY=<your-public-key>
   DISCORD_ADMIN_IDS=<admin-snowflake-ids>
   POCKETBASE_URL=http://pocketbase:8090
   POCKETBASE_ADMIN_EMAIL=admin@yourdomain.com
   POCKETBASE_ADMIN_PASSWORD=<secure-password>
   DEFAULT_MODEL=mistral/mistral-nemo
   AI_GATEWAY_TOKEN=<optional-ai-gateway-token>
   GOOGLE_GENERATIVE_AI_API_KEY=<optional-gemini-key>
   GITHUB_TOKEN=<optional-github-pat>
   PORT=3000
   ```
   *(If PocketBase is on the same internal Docker network in Coolify, `http://pocketbase:8090` avoids public internet round-trips).*

6. Configure Domain & Cloudflare (Optional for HTTP webhook interactions):
   - Set Domain: `https://bot.yourdomain.com`.
   - In Cloudflare DNS, point `bot.yourdomain.com` to the server IP with Proxy enabled (Orange Cloud) and SSL/TLS set to **Full (strict)**.
   - If using the Discord Gateway exclusively, HTTP ingress is not strictly required, but the Eve server also provides health endpoints at `/eve/v1/health`.

7. Deploy:
   - Click **Deploy** in Coolify.
   - The multi-stage build will install dependencies with `pnpm --frozen-lockfile`, compile the application via `eve build`, install production dependencies, and run `pnpm run start` under the non-root `node` user.

### Verification Checklist & Troubleshooting
- [ ] **Health Check**: Visit `https://bot.yourdomain.com/eve/v1/health` (or check container logs for `[Runner] Eve server is healthy and listening on port 3000!`).
- [ ] **Gateway Connection**: Verify the log entry `[Gateway] Logged in as <BotName>#<discriminator>`.
- [ ] **PocketBase Schema**: Verify the log entry `[PocketBase] Auto-created 'faqs' collection schema` on first startup if the collection wasn't created yet.
- [ ] **Test Mention**: Send `@Cobbleloots Assistant test` in a Discord channel where the bot has access. Ensure the bot replies and typing indicator displays.
- [ ] **Test Thread Ingestion**: Mention the bot inside a multi-message thread; verify in responses that context from earlier in the thread is incorporated.
