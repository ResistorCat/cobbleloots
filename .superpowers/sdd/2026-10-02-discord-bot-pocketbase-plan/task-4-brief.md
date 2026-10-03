# Task 4 Brief: Dynamic Instructions with Bilingual Logic & Agent Definition

## Context & Project Role
This is Task 4 of the Cobbleloots Discord AI Support Bot re-architecture. In this task, you will configure the dynamic instructions with bilingual logic (English by default, Spanish when addressed in Spanish, thread context rules, anti-rush protocol), register all grounding and PocketBase tools in `agent/agent.ts`, clean up legacy SQLite references in `tests/tools.test.ts` and `tests/hitl-tools.test.ts`, and verify that `tests/agent-config.test.ts` passes.

## Global Constraints
- Target directory is strictly isolated within `infra/discord-bot/`. Zero edits to Minecraft Java/Kotlin code, Gradle scripts, or `CHANGELOG.md`.
- No C++ compilers or native modules.
- Use `pnpm` (`pnpm test`, `pnpm run typecheck`).
- Language Policy: English is the default language. If the user initiates or asks in Spanish, the bot responds in Spanish while preserving exact Minecraft command syntax (`/cobbleloots ...`) and item identifiers.

## Exact Actions Required

### 1. Update `infra/discord-bot/agent/instructions.ts`
Write the dynamic instructions with bilingual and thread context rules:
```typescript
import { defineDynamic, defineInstructions } from "eve/instructions";

export function buildInstructionsPrompt(opts: { isAdmin?: boolean } = {}): string {
  const roleContext = opts.isAdmin
    ? `You are the Cobbleloots Discord Assistant in Maintainer/Admin Mode.
The user is an authenticated administrator or developer.
- You have full access to manage FAQs in PocketBase using save_faq, list_faqs, get_faq, and delete_faq.
- Provide technically precise responses with Java class names (e.g. CobblelootsLootBall.java), exact line references, configs, and Linear/Git references where relevant.
- You can inspect files in common/, fabric/, and neoforge/ using inspect_code.`
    : `You are the Cobbleloots Discord Assistant for players and community members.
- Explain mechanics, commands, recipes, and features in terms of gameplay without internal Java or development jargon.
- Ground your answers in official documentation (search_docs), FAQs (search_faqs), and release notes (get_releases).
- ANTI-RUSH PROTOCOL: If the user reports a bug or issue and omits critical environment details (Minecraft version, Fabric vs NeoForge loader, or survival vs creative mode), do NOT guess. Use ask_question with 2-3 concrete options so the user can clarify before you formulate an answer.`;

  return `${roleContext}

## LANGUAGE RULES (MANDATORY):
1. Default Language: The default and primary language of the bot is English.
2. Spanish Adaptability: If the user speaks to you in Spanish (or explicitly asks in Spanish), reply to them naturally and fluently in Spanish. Keep command syntax (e.g. \`/cobbleloots reset\`), item names, and Minecraft technical identifiers accurate.
3. For any other language or by default, always reply in English.

## THREAD CONTEXT RULES:
- When a "[Thread History]" transcript is provided in the message, read the entire discussion to understand what the player and moderators have already tried.
- Do NOT repeat questions or solutions that were already given in the thread.
- Use the reported loader and Minecraft version from earlier in the thread without asking again.`;
}

export default defineDynamic({
  events: {
    "session.started": (_event, ctx) => {
      const rawAdmin = (ctx.session.auth.current as { attributes?: Record<string, unknown> } | undefined)?.attributes
        ?.isAdmin;
      const isAdmin = rawAdmin === "true" || rawAdmin === true;
      return defineInstructions({
        content: buildInstructionsPrompt({ isAdmin }),
      });
    },
  },
});
```

### 2. Update `infra/discord-bot/agent/agent.ts`
Register all grounding and management tools:
```typescript
import { defineAgent } from "eve/agent";
import { aiGateway } from "@vercel/connect";
import instructions from "./instructions.ts";
import searchDocs from "./tools/search_docs.ts";
import searchFaqs from "./tools/search_faqs.ts";
import listFaqs from "./tools/list_faqs.ts";
import getFaq from "./tools/get_faq.ts";
import saveFaq from "./tools/save_faq.ts";
import deleteFaq from "./tools/delete_faq.ts";
import inspectCode from "./tools/inspect_code.ts";
import getReleases from "./tools/get_releases.ts";
import askQuestion from "./tools/ask_question.ts";

export default defineAgent({
  instructions,
  model: aiGateway("mistral/mistral-nemo"),
  tools: [
    searchDocs,
    searchFaqs,
    listFaqs,
    getFaq,
    saveFaq,
    deleteFaq,
    inspectCode,
    getReleases,
    askQuestion,
  ],
});
```

### 3. Clean up Test Suites
- Update `infra/discord-bot/tests/agent-config.test.ts` to test bilingual instructions and PocketBase tools.
- Update `infra/discord-bot/tests/tools.test.ts`: Remove any legacy `db.ts` / SQLite FAQ store imports (ensure it tests `search_docs`, `inspect_code`, `get_releases`).
- Update `infra/discord-bot/tests/hitl-tools.test.ts`: Ensure it cleanly tests `ask_question`.

### 4. Verify Tests Pass
Run: `pnpm test`
Verify that `agent-config.test.ts`, `tools.test.ts`, and `hitl-tools.test.ts` pass cleanly.

### 5. Commit Your Work
```bash
git add infra/discord-bot/agent/instructions.ts infra/discord-bot/agent/agent.ts infra/discord-bot/tests/
git commit -m "feat(bot): configure bilingual dynamic instructions and register pocketbase tools in agent"
```

## Report Contract
Write your full report to:
`c:\Users\franc\GitHub\cobbleloots\.worktrees\ripio\dev-30-cobbleloots-implementar-bot-de-soporte-de-discord-con-eve\.superpowers\sdd\2026-10-02-discord-bot-pocketbase-plan\task-4-report.md`

Then report back with ONLY:
- Status: DONE | DONE_WITH_CONCERNS | BLOCKED | NEEDS_CONTEXT
- Commits created (short SHA + subject)
- One-line test summary
- Your concerns, if any
- The report file path
