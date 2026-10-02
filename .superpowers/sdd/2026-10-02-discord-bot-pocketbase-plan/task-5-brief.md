# Task 5 Brief: Thread Context Engine & Gateway Supercharger (agent/gateway.ts & runner.mjs)

## Context & Project Role
This is Task 5 of the Cobbleloots Discord AI Support Bot re-architecture. In this task, you will implement the thread context engine in `agent/gateway.ts` (retrieving the last 20 messages when mentioned in a Discord thread, formatting the chronological `[Thread History]` block, handling reply contexts, message chunking, typing indicators), clean up `runner.mjs` (removing any leftover C++ `.node` copy code, cleanly supervising Eve HTTP server and Discord Gateway), and add unit tests in `tests/gateway.test.ts`.

## Global Constraints
- Target directory is strictly isolated within `infra/discord-bot/`. Zero edits to Minecraft Java/Kotlin code, Gradle scripts, or `CHANGELOG.md`.
- No C++ compilers or native modules.
- Use `pnpm` (`pnpm test`, `pnpm run typecheck`).
- Thread Context: If `message.channel.isThread()` is true, fetch up to 20 messages, format into `[Thread History - #<name>]\n<Author>: <Content>\n...`, and prepend to the prompt.

## Exact Actions Required

### 1. Write Tests in `infra/discord-bot/tests/gateway.test.ts`
Verify:
- `formatThreadTranscript` formats messages chronologically with author and content.
- `extractPrompt` strips bot mention cleanly.
- `splitMessage` splits long text at paragraph boundary (under 1900 chars).
- `shouldRespondToMessage` responds when mentioned or when replying to bot.

### 2. Implement `infra/discord-bot/agent/gateway.ts`
Implement complete gateway logic:
- `DEFAULT_GREETING`
- `extractPrompt(content, botId)`
- `shouldRespondToMessage(message, botId, isReplyToBot)`
- `formatThreadTranscript(messages, threadName, botId)`
- `splitMessage(text, maxLength)`
- `handleDiscordMessage(message, client, eveClient)`:
  - Checks if bot is mentioned or replied to.
  - If in thread, fetches up to 20 messages via `message.channel.messages.fetch({ limit: 20 })`.
  - Prepends `[Thread History]` to prompt.
  - If replying to a message in a channel, prepends `[Replying to <Author>: "<Content>"]`.
  - Sends typing indicator and refreshes interval.
  - Sends request to Eve client with `x-discord-user-id` and `x-discord-is-admin` headers.
  - Receives response, splits into chunks if needed, replies to user.
- `startGateway(options)`:
  - Connects Discord Client with `Guilds`, `GuildMessages`, `MessageContent` intents.
  - Sets presence activity `Cobbleloots | Tag me to ask!`.
  - Registers `messageCreate` handler.

### 3. Implement Clean `infra/discord-bot/runner.mjs`
- Starts Eve HTTP Server (`.output/server/index.mjs`) on `PORT` (default 3000).
- Polls `http://127.0.0.1:${port}/eve/v1/health` until healthy (up to 60 attempts).
- Connects Discord Gateway listener via `startGateway({ port })` if `DISCORD_BOT_TOKEN` is present and `DISABLE_GATEWAY !== "true"`.
- Clean shutdown on `SIGTERM` and `SIGINT`.
- ZERO native `.node` or SQLite binding copy hacks.

### 4. Run Tests & Typecheck
Run:
`pnpm test`
`pnpm run typecheck`
Verify 100% tests pass and 0 errors.

### 5. Commit Your Work
```bash
git add infra/discord-bot/agent/gateway.ts infra/discord-bot/runner.mjs infra/discord-bot/tests/gateway.test.ts
git commit -m "feat(bot): add thread context engine, message chunking, and clean runner orchestration"
```

## Report Contract
Write your full report to:
`c:\Users\franc\GitHub\cobbleloots\.worktrees\ripio\dev-30-cobbleloots-implementar-bot-de-soporte-de-discord-con-eve\.superpowers\sdd\2026-10-02-discord-bot-pocketbase-plan\task-5-report.md`

Then report back with ONLY:
- Status: DONE | DONE_WITH_CONCERNS | BLOCKED | NEEDS_CONTEXT
- Commits created (short SHA + subject)
- One-line test summary
- Your concerns, if any
- The report file path
