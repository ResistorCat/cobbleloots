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
- **Domain Tools**:
  - `search_docs`: Searches MkDocs documentation markdown files (`docs/`).
  - `search_faqs`: Full-text search across approved FAQs stored in SQLite.
  - `get_releases`: Queries changelog fragments (`.changelog/`) and release notes (`CHANGELOG.md`).
  - `inspect_code`: Inspects source files across `common/`, `fabric/`, and `neoforge/` subprojects (restricted to safe paths).
- **Persistent Storage**:
  - Embedded SQLite database (`data/bot.db`) managed with `better-sqlite3` for durable FAQ storage and player profiles.
- **Discord HTTP Interactions**:
  - Direct Discord HTTP webhook interaction model with cryptographic signature verification (`DISCORD_PUBLIC_KEY`), eliminating gateway heartbeat overhead.

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
   - Under **Privileged Gateway Intents**, no gateway intents are required as the bot uses HTTP Interactions.
4. **Set Interactions Endpoint URL**:
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
| `DEFAULT_MODEL` | No | `google/gemini-2.5-pro` | Default LLM model string for the agent. |
| `DATABASE_PATH` | No | `./data/bot.db` | File path for SQLite database storing FAQs and state. |
| `REPO_ROOT` | No | Mod repo root | Absolute path to the Cobbleloots repository root. |
| `REPO_DOCS_PATH` | No | `<REPO_ROOT>/docs` | Absolute path to the MkDocs markdown documentation folder. |
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
npx vitest run tests/agent-config.test.ts
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
To compile and run the production server:
```bash
npm run build
npm run start
```

---

## Operations & Maintenance Runbook

### Admin Privileges & FAQ Moderation
- Users whose Discord IDs are listed in `DISCORD_ADMIN_IDS` are automatically detected upon slash command invocation.
- When an admin interacts with the assistant:
  - The agent enters **Maintainer/Admin Mode**, offering in-depth code inspections and architectural insights.
  - The agent can save new FAQs directly to the database without requiring secondary approval (`approved: 1`).
- When a standard player's query uncovers a helpful Q&A, the agent can stage an unapproved FAQ (`approved: 0`), ready for review and activation by mod administrators.
