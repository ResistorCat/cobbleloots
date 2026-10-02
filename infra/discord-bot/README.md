# Cobbleloots Discord AI Assistant

An intelligent, context-aware Discord assistant for the [Cobbleloots](https://github.com/ResistorCat/cobbleloots) Minecraft mod community, built using the [Eve framework](https://github.com/run-eve/eve).

The bot provides real-time community support for players and maintainers, answering questions about mod mechanics, configuration, loot ball drops, compatibility, and release history.

---

## Features

- **Dynamic Dual-Persona Instructions**:
  - **Player Mode (default)**: Delivers friendly, clear gameplay guidance grounded in official documentation and FAQs without internal Java or development jargon. Features the *Anti-Rush Protocol* to ask clarifying questions before answering underspecified bug reports.
  - **Maintainer/Admin Mode**: For authorized administrators, provides technically detailed answers including Java class references, line numbers, loader architecture (Common vs Fabric vs NeoForge), and repository inspection.
- **Rich Interactive HITL (Human-in-the-Loop)**:
  - `ask_question`: Renders interactive Discord button components for users to clarify environment details (e.g. loader, Minecraft version).
  - `save_faq`: Allows proposing FAQs that can be directly approved and indexed by administrators.
- **Grounding Tools (Hybrid Local & Remote)**:
  - `search_docs`: Searches local MkDocs markdown files (`docs/`) if present, falling back to the public precomputed MkDocs search index (`https://resistorcat.github.io/cobbleloots/search/search_index.json`).
  - `search_faqs`: Full-text search across approved FAQs stored in SQLite.
  - `get_releases`: Queries the GitHub Releases REST API for latest releases and release notes, falling back to local `CHANGELOG.md` and `.changelog/` fragments.
  - `inspect_code`: Inspects source files across `common/`, `fabric/`, and `neoforge/` subprojects (restricted to safe paths, dynamically fetched from the latest published release tag on GitHub or read locally when in the monorepo).
- **Persistent Storage**:
  - Embedded SQLite database (`data/bot.db`) managed with `better-sqlite3` (WAL mode enabled) for durable FAQ storage and player profiles.
- **Discord HTTP Interactions**:
  - Direct Discord HTTP webhook interaction model with cryptographic signature verification (`DISCORD_PUBLIC_KEY`), eliminating gateway heartbeat overhead.

---

## Grounding & Remote Resolution Architecture

To keep production Docker images lightweight and decoupled from the monorepo source tree, the bot implements a hybrid grounding architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                   Discord AI Assistant                      │
└───────┬─────────────────────────┬─────────────────────────┬─┘
        │                         │                         │
 ┌──────▼──────┐           ┌──────▼──────┐           ┌──────▼──────┐
 │ search_docs │           │inspect_code │           │get_releases │
 └──────┬──────┘           └──────┬──────┘           └──────┬──────┘
        │                         │                         │
  Local docs/ exists?       Local repo exists?       Query GitHub API
  ├── Yes -> Read disk      ├── Yes -> Read disk     ├── Success -> Return notes
  └── No  -> Remote index   └── No  -> GitHub API    └── Fail    -> Read CHANGELOG
             (Pages CDN)               (@latest tag)
```

- **Zero Monorepo Bundling in Docker**: The Docker container only needs `infra/discord-bot/`. It does not require a full clone of the Java monorepo, keeping image sizes small and builds fast.
- **Local Dev Speed**: When developing in the monorepo or running tests, local files are read directly from disk with zero network latency.
- **Dynamic Tag Resolution**: Code inspection in container environments automatically resolves against the latest published release tag (e.g., `v2.5.0-alpha.2`), ensuring answers reflect active player releases.

---

## Prerequisites

- **Node.js**: `>= 22.0.0`
- **npm**: `>= 10.0.0`
- A Discord Application configured in the [Discord Developer Portal](https://discord.com/developers/applications).
- API credentials for the LLM model provider (e.g., Google Gemini, OpenAI, or Anthropic).

---

## Discord Developer Portal Setup

Follow these steps to set up the Discord Application and bot credentials:

1. **Create Application**:
   - Go to [Discord Developer Portal](https://discord.com/developers/applications).
   - Click **New Application**, name it (e.g., `Cobbleloots Assistant`), and accept the terms.
2. **Retrieve Application ID & Public Key**:
   - In the **General Information** tab:
     - Copy the **Application ID** (`DISCORD_APPLICATION_ID`).
     - Copy the **Public Key** (`DISCORD_PUBLIC_KEY`).
3. **Configure Bot & Token**:
   - Navigate to the **Bot** tab on the left sidebar.
   - Click **Reset Token** to copy the token (`DISCORD_BOT_TOKEN`).
   - Under **Privileged Gateway Intents**, enable **Message Content Intent** so the bot can listen to `@Cobbleloots` mentions and replies in chat channels.
4. **Set Interactions Endpoint URL (Optional for Slash Commands/Webhooks)**:
   - In **General Information**, locate **Interactions Endpoint URL**.
   - Eve exposes HTTP interactions at `/eve/v1/discord`.
   - Set the URL to:
     ```text
     https://<your-public-domain>/eve/v1/discord
     ```
   - For local development, expose your local port using a tunnel (e.g. `ngrok http 3000` or `cloudflared tunnel`) and provide `https://<tunnel-subdomain>/eve/v1/discord`. Discord will send a `PING` to verify the public key signature automatically.
5. **Invite Bot to Server**:
   - Navigate to **OAuth2** -> **URL Generator**.
   - Scopes: Select `bot` and `applications.commands`.
   - Bot Permissions: Select `Send Messages`, `Embed Links`, `Use Slash Commands`, and `Read Message History`.
   - Copy the generated URL and open it in a browser to invite the bot to your Discord server.

---

## How to Talk to the Bot

Once invited to your Discord server:
- **Mention the bot directly**: `@Cobbleloots ¿dónde encuentro la Moon Ball?`
- **Reply to any previous bot message**: The assistant seamlessly continues the conversation.
- If mentioned with no question (`@Cobbleloots`), the bot replies with a helpful greeting introducing its capabilities.

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
| `DEFAULT_MODEL` | No | `mistral/mistral-nemo` | Default LLM model string for the agent (routes via Vercel AI Gateway). |
| `DATABASE_PATH` | No | `./data/bot.db` | File path for SQLite database storing FAQs and state (`/app/data/bot.db` in Docker). |
| `GITHUB_REPO` | No | `ResistorCat/cobbleloots` | GitHub repository identifier for remote release & code queries. |
| `GITHUB_TOKEN` | No | — | Optional GitHub Personal Access Token for increased API rate limits in production. |
| `REPO_ROOT` | No | Mod repo root | Absolute path to the local Cobbleloots repository root (dev only). |
| `REPO_DOCS_PATH` | No | `<REPO_ROOT>/docs` | Absolute path to local MkDocs markdown folder (dev only). |
| `GOOGLE_GENERATIVE_AI_API_KEY` | Conditional | — | Required if using Google Gemini models (`google/*`). |
| `OPENAI_API_KEY` | Conditional | — | Required if using OpenAI models (`openai/*`). |
| `ANTHROPIC_API_KEY` | Conditional | — | Required if using Anthropic models (`anthropic/*`). |

---

## Installation & Development Runbook

### 1. Install Dependencies
```bash
npm install
```

### 2. Run Tests
Execute the Vitest test suite covering FAQ store, domain tools, HITL workflows, session state, and agent configuration:
```bash
npm test
```
To run a specific test file:
```bash
npx vitest run tests/tools.test.ts
```

### 3. Typecheck
Verify strict TypeScript compilation:
```bash
npm run typecheck
```

### 4. Start Local Development Server
Start the Eve live development server with hot reload:
```bash
npm run dev
```
The server listens for incoming HTTP interaction webhooks at `http://localhost:3000/eve/v1/discord`.

### 5. Production Build & Run
To compile and run the production server locally:
```bash
npm run build
npm run start
```

---

## Production Deployment with Coolify v4 & Cloudflare

The assistant is containerized with a multi-stage Dockerfile and designed to run on a self-hosted server managed via [Coolify](https://coolify.io/) behind a Cloudflare reverse proxy.

### Coolify Setup Runbook

1. **Create New Service in Coolify**:
   - In your Coolify dashboard, select your project/environment and click **+ New** -> **Application** -> **Public Repository** (or Private GitHub App).
   - Repository URL: `https://github.com/ResistorCat/cobbleloots`
   - Branch: `main` (or your target branch)
2. **Configure Application Settings**:
   - **Build Pack**: Select **Dockerfile**.
   - **Base Directory**: Set to `infra/discord-bot`.
   - **Dockerfile Location**: `/Dockerfile` (relative to Base Directory).
   - **Ports Exposes**: `3000`.
3. **Configure Persistent Storage (Volume)**:
   - Go to the **Storages** tab in Coolify.
   - Add a persistent volume mount:
     - **Destination Path**: `/app/data`
     - **Volume Name / Host Path**: `cobbleloots-bot-data` (or `/var/lib/docker/volumes/cobbleloots-bot-data`)
   - This ensures the SQLite database (`/app/data/bot.db`) persists across redeployments and container restarts.
4. **Configure Environment Variables**:
   - Under the **Environment Variables** tab, add all required secrets:
     ```text
     DISCORD_APPLICATION_ID=<your-app-id>
     DISCORD_BOT_TOKEN=<your-bot-token>
     DISCORD_PUBLIC_KEY=<your-public-key>
     DISCORD_ADMIN_IDS=<admin-snowflake-ids>
     DATABASE_PATH=/app/data/bot.db
     DEFAULT_MODEL=mistral/mistral-nemo
     GOOGLE_GENERATIVE_AI_API_KEY=<gemini-api-key>
     GITHUB_TOKEN=<optional-github-pat>
     ```
5. **Set Domains & Cloudflare Reverse Proxy**:
   - In Coolify **Domains**, enter your public domain (e.g. `https://bot.yourdomain.com`).
   - In Cloudflare DNS:
     - Add a `CNAME` or `A` record pointing `bot.yourdomain.com` to your Coolify server IP with Cloudflare Proxy enabled (Orange Cloud / Proxied).
     - SSL/TLS encryption mode: Set to **Full (strict)**.
6. **Register Discord Webhook**:
   - In the Discord Developer Portal for your application:
     - Interactions Endpoint URL: `https://bot.yourdomain.com/eve/v1/discord`
     - Save changes. Discord will send a signature validation ping; the running bot will respond with HTTP 200 `PONG`.
7. **Deploy**:
   - Click **Deploy** in Coolify.
   - Monitor the deployment logs. The multi-stage build compiles native dependencies, builds the Nitro output bundle, and runs under the unprivileged `node` user.

---

## Operations & Maintenance Runbook

### Admin Privileges & FAQ Moderation
- Users whose Discord IDs are listed in `DISCORD_ADMIN_IDS` are automatically detected upon slash command invocation.
- When an admin interacts with the assistant:
  - The agent enters **Maintainer/Admin Mode**, offering in-depth code inspections and architectural insights.
  - The agent can save new FAQs directly to the database without requiring secondary approval (`approved: 1`).
- When a standard player's query uncovers a helpful Q&A, the agent can stage an unapproved FAQ (`approved: 0`), ready for review and activation by mod administrators.

### Database Backups
Because SQLite uses Write-Ahead Logging (WAL mode), backups of `/app/data/bot.db` can be taken online without stopping the container:
```bash
sqlite3 /app/data/bot.db ".backup '/app/data/backup-$(date +%F).db'"
```
