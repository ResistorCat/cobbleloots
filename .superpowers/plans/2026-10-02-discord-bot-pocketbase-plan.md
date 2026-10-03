# Discord AI Support Bot: PocketBase & pnpm Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Re-architect the Cobbleloots Discord AI Support Bot (`infra/discord-bot/`) by migrating package management to `pnpm`, decoupling persistent FAQ storage into PocketBase with web dashboard access, ingesting Discord thread history for conversational continuity, and establishing English as default with bilingual Spanish support.

**Architecture:** A dual-stage Docker container built with `pnpm` on `node:24-slim` running 100% pure JavaScript/TypeScript without native C++ compilation modules or local database files. Discord Gateway (`discord.js`) connects via persistent WebSocket, extracts up to 20 messages of chronological thread context when mentioned inside a thread, and dispatches requests to an internal Eve HTTP server (`eve@0.68.0`) on `http://127.0.0.1:3000`. Eve interfaces with Vercel AI Gateway (`mistral/mistral-nemo`) and queries PocketBase (`pocketbase` SDK) for FAQ operations (`search_faqs`, `list_faqs`, `get_faq`, `save_faq`, `delete_faq`).

**Tech Stack:** Node.js 24, `pnpm@11.20.0`, Eve Framework (`eve@0.68.0`), `discord.js@14.27.0`, `pocketbase@0.26.0`, Vercel AI Gateway (`mistral/mistral-nemo`), Vitest, TypeScript.

**Spec:** [.superpowers/specs/2026-10-02-discord-ai-bot-pocketbase-design.md](file:///c:/Users/franc/GitHub/cobbleloots/.superpowers/specs/2026-10-02-discord-ai-bot-pocketbase-design.md)

## Global Constraints

- **Strict Isolation:** Target directory is strictly isolated within `infra/discord-bot/`. Zero edits to Minecraft Java/Kotlin code, Gradle scripts, or `CHANGELOG.md`.
- **Zero Native C++ Compilers:** No `better-sqlite3`, `python3`, `make`, `g++`, or native `.node` addons in the bot container.
- **Package Manager:** Strictly `pnpm` (`packageManager: pnpm@11.20.0`, `pnpm-lock.yaml`).
- **Language Policy:** English is the default language. If a user writes or asks in Spanish, the bot responds in natural Spanish with exact Minecraft IDs/command syntax.
- **Admin Security:** FAQ write operations (`save_faq`, `delete_faq`, `list_faqs`, `get_faq`) are strictly restricted to administrators listed in `DISCORD_ADMIN_IDS`. Deletion requires explicit interactive confirmation.

---

## File Structure

```text
infra/discord-bot/
├── package.json                    # pnpm configuration, dependencies (pocketbase, eve, discord.js)
├── pnpm-lock.yaml                  # pnpm lockfile
├── tsconfig.json                   # TypeScript config
├── vitest.config.ts                # Vitest config
├── Dockerfile                      # Multi-stage pnpm build on node:24-slim (no C++ compilers)
├── runner.mjs                      # Orchestrator (Eve HTTP :3000 + Discord Gateway listener)
├── agent/
│   ├── agent.ts                    # Eve agent definition (tools + mistral-nemo model)
│   ├── instructions.ts             # Dynamic prompt (English default + Spanish support + thread rules)
│   ├── gateway.ts                  # discord.js client with thread history extraction
│   ├── lib/
│   │   ├── auth-utils.ts           # Admin authorization helper (DISCORD_ADMIN_IDS)
│   │   └── pb.ts                   # PocketBase client singleton, auth, and schema bootstrap
│   └── tools/
│       ├── search_docs.ts          # Search docs/ markdown
│       ├── search_faqs.ts          # Query PocketBase faqs collection (public)
│       ├── list_faqs.ts            # List all FAQs from PocketBase (admin-only)
│       ├── get_faq.ts              # Retrieve single FAQ by ID (admin-only)
│       ├── save_faq.ts             # Create or update FAQ in PocketBase (admin-only)
│       ├── delete_faq.ts           # Delete FAQ with confirmation check (admin-only)
│       ├── inspect_code.ts         # Code inspection in common/, fabric/, neoforge/
│       ├── get_releases.ts         # GitHub API releases
│       └── ask_question.ts         # HITL interactive clarification tool
└── tests/
    ├── pb-client.test.ts           # PocketBase client & auth tests
    ├── faq-tools.test.ts           # Search, list, get, save, delete FAQ tools tests
    ├── agent-config.test.ts        # Instructions & bilingual prompt tests
    ├── gateway.test.ts             # Message handling, thread history, chunking tests
    ├── hitl-tools.test.ts          # ask_question tool tests
    └── tools.test.ts               # search_docs, inspect_code, get_releases tests
```

---

## Tasks

### Task 1: Package Scaffolding Migration to `pnpm` & Legacy SQLite Cleanup

**Files:**
- Modify: `infra/discord-bot/package.json`
- Delete: `infra/discord-bot/package-lock.json`
- Delete: `infra/discord-bot/agent/lib/sqlite-memory-backend.ts`
- Delete: `infra/discord-bot/agent/memory/player-profile.ts`
- Delete: `infra/discord-bot/agent/lib/db.ts`
- Delete: `infra/discord-bot/agent/lib/faq-store.ts`
- Delete: `infra/discord-bot/tests/faq-store.test.ts`
- Delete: `infra/discord-bot/tests/session-state.test.ts`
- Create: `infra/discord-bot/pnpm-lock.yaml` (via `pnpm install`)

**Interfaces:**
- Consumes: Clean Node.js 24 environment with `pnpm`.
- Produces: Project configured with `pocketbase@^0.26.0`, all SQLite code and native C++ scripts removed.

- [ ] **Step 1: Update `infra/discord-bot/package.json` with pnpm and pocketbase**

Replace `infra/discord-bot/package.json`:
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

- [ ] **Step 2: Delete legacy SQLite files and test suites**

Remove:
- `infra/discord-bot/package-lock.json`
- `infra/discord-bot/agent/lib/sqlite-memory-backend.ts`
- `infra/discord-bot/agent/memory/player-profile.ts`
- `infra/discord-bot/agent/lib/db.ts`
- `infra/discord-bot/agent/lib/faq-store.ts`
- `infra/discord-bot/tests/faq-store.test.ts`
- `infra/discord-bot/tests/session-state.test.ts`

- [ ] **Step 3: Run `pnpm install` in `infra/discord-bot`**

Run: `cd infra/discord-bot && pnpm install`
Expected: Installs all pure JS dependencies and generates `pnpm-lock.yaml`.

- [ ] **Step 4: Commit changes**

```bash
git add infra/discord-bot/package.json infra/discord-bot/pnpm-lock.yaml
git commit -m "chore(bot): migrate package manager to pnpm and remove legacy sqlite dependencies"
```

---

### Task 2: PocketBase Client & Connection Management (`agent/lib/pb.ts`)

**Files:**
- Create: `infra/discord-bot/agent/lib/pb.ts`
- Test: `infra/discord-bot/tests/pb-client.test.ts`

**Interfaces:**
- Consumes: `process.env.POCKETBASE_URL`, `process.env.POCKETBASE_ADMIN_EMAIL`, `process.env.POCKETBASE_ADMIN_PASSWORD`.
- Produces:
  - `getPbClient(): PocketBase`
  - `ensurePocketBaseAuth(): Promise<boolean>`
  - `ensureFaqsCollection(): Promise<void>`
  - `resetPbClientForTesting(): void`

- [ ] **Step 1: Write failing test in `infra/discord-bot/tests/pb-client.test.ts`**

```typescript
import { describe, it, expect, beforeEach } from "vitest";
import { getPbClient, resetPbClientForTesting } from "../agent/lib/pb.ts";

describe("PocketBase Client Singleton", () => {
  beforeEach(() => {
    resetPbClientForTesting();
  });

  it("should return a PocketBase client instance pointing to configured URL", () => {
    process.env.POCKETBASE_URL = "http://127.0.0.1:8090";
    const client = getPbClient();
    expect(client).toBeDefined();
    expect(client.baseUrl).toBe("http://127.0.0.1:8090");
  });

  it("should reuse the same client singleton on subsequent calls", () => {
    const client1 = getPbClient();
    const client2 = getPbClient();
    expect(client1).toBe(client2);
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd infra/discord-bot && pnpm test tests/pb-client.test.ts`
Expected: FAIL (`../agent/lib/pb.ts` not found).

- [ ] **Step 3: Implement `infra/discord-bot/agent/lib/pb.ts`**

```typescript
import PocketBase from "pocketbase";

let pbInstance: PocketBase | null = null;

export function resetPbClientForTesting(): void {
  pbInstance = null;
}

export function getPbClient(): PocketBase {
  if (pbInstance) return pbInstance;
  const url = process.env.POCKETBASE_URL || "http://127.0.0.1:8090";
  pbInstance = new PocketBase(url);
  return pbInstance;
}

export async function ensurePocketBaseAuth(client = getPbClient()): Promise<boolean> {
  const email = process.env.POCKETBASE_ADMIN_EMAIL;
  const password = process.env.POCKETBASE_ADMIN_PASSWORD;

  if (!email || !password) {
    return false;
  }

  try {
    // PocketBase v0.23+ superusers or legacy admins
    if ("_superusers" in client.collection) {
      await client.collection("_superusers").authWithPassword(email, password);
    } else if (client.admins?.authWithPassword) {
      await client.admins.authWithPassword(email, password);
    }
    return true;
  } catch (err) {
    console.warn("[PocketBase] Authentication failed:", (err as Error).message);
    return false;
  }
}

export async function ensureFaqsCollection(client = getPbClient()): Promise<void> {
  const authed = await ensurePocketBaseAuth(client);
  if (!authed) return;

  try {
    await client.collections.getOne("faqs");
  } catch {
    // Collection does not exist, create it
    try {
      await client.collections.create({
        name: "faqs",
        type: "base",
        schema: [
          { name: "question", type: "text", required: true },
          { name: "answer", type: "text", required: true },
          { name: "keywords", type: "text", required: false },
          { name: "category", type: "text", required: true },
          { name: "created_by", type: "text", required: false },
        ],
      });
      console.log("[PocketBase] Auto-created 'faqs' collection schema.");
    } catch (createErr) {
      console.warn("[PocketBase] Could not auto-create 'faqs' collection:", (createErr as Error).message);
    }
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd infra/discord-bot && pnpm test tests/pb-client.test.ts`
Expected: PASS.

- [ ] **Step 5: Commit changes**

```bash
git add infra/discord-bot/agent/lib/pb.ts infra/discord-bot/tests/pb-client.test.ts
git commit -m "feat(bot): implement pocketbase client singleton, auth, and schema bootstrap"
```

---

### Task 3: PocketBase Grounding & Admin Tools (`agent/tools/`)

**Files:**
- Modify: `infra/discord-bot/agent/tools/search_faqs.ts`
- Modify: `infra/discord-bot/agent/tools/save_faq.ts`
- Create: `infra/discord-bot/agent/tools/list_faqs.ts`
- Create: `infra/discord-bot/agent/tools/get_faq.ts`
- Create: `infra/discord-bot/agent/tools/delete_faq.ts`
- Test: `infra/discord-bot/tests/faq-tools.test.ts`

**Interfaces:**
- Consumes: `getPbClient()`, `isAuthorizedAdmin()`.
- Produces:
  - `searchFaqs`: Public tool to search questions/keywords in PocketBase.
  - `listFaqs`: Admin-only tool listing all FAQs with ID, Category, Question.
  - `getFaq`: Admin-only tool showing full details of a specific FAQ by ID.
  - `saveFaq`: Admin-only tool creating or updating an FAQ record.
  - `deleteFaq`: Admin-only tool with confirmation gate deleting an FAQ.

- [ ] **Step 1: Write failing test in `infra/discord-bot/tests/faq-tools.test.ts`**

```typescript
import { describe, it, expect, vi, beforeEach } from "vitest";
import { searchFaqs } from "../agent/tools/search_faqs.ts";
import { listFaqs } from "../agent/tools/list_faqs.ts";
import { getFaq } from "../agent/tools/get_faq.ts";
import { saveFaq } from "../agent/tools/save_faq.ts";
import { deleteFaq } from "../agent/tools/delete_faq.ts";

describe("PocketBase FAQ Tools", () => {
  it("searchFaqs should return formatted results", async () => {
    const result = await searchFaqs.execute({ query: "loot ball" }, {} as any);
    expect(typeof result).toBe("string");
  });

  it("deleteFaq should require confirmation before deleting", async () => {
    const unconfirmed = await deleteFaq.execute({ id: "rec123", confirmed: false }, {
      session: { auth: { current: { id: "admin_user" } } },
    } as any);
    expect(unconfirmed).toContain("confirmation");

    const unauthorized = await deleteFaq.execute({ id: "rec123", confirmed: true }, {
      session: { auth: { current: { id: "player_user" } } },
    } as any);
    expect(unauthorized).toContain("Unauthorized");
  });
});
```

- [ ] **Step 2: Run test to verify failure**

Run: `cd infra/discord-bot && pnpm test tests/faq-tools.test.ts`
Expected: FAIL (missing tools).

- [ ] **Step 3: Implement `agent/tools/search_faqs.ts`**

```typescript
import { defineTool } from "eve/tool";
import { z } from "zod";
import { getPbClient } from "../lib/pb.ts";

export const searchFaqs = defineTool({
  description: "Search community FAQs in PocketBase by question, keywords, or topic.",
  parameters: z.object({
    query: z.string().describe("The search query or keyword to look for in FAQs"),
  }),
  execute: async ({ query }) => {
    const pb = getPbClient();
    try {
      const sanitized = query.replace(/['"\\]/g, "");
      const records = await pb.collection("faqs").getList(1, 5, {
        filter: `question ~ "${sanitized}" || keywords ~ "${sanitized}" || answer ~ "${sanitized}"`,
      });

      if (records.items.length === 0) {
        return `No community FAQs found matching "${query}".`;
      }

      return records.items
        .map(
          (item) =>
            `### [FAQ: ${item.category || "General"}] ${item.question} (ID: ${item.id})\n${item.answer}`
        )
        .join("\n\n---\n\n");
    } catch (err) {
      return `FAQ search currently unavailable: ${(err as Error).message}`;
    }
  },
});

export default searchFaqs;
```

- [ ] **Step 4: Implement `agent/tools/list_faqs.ts`**

```typescript
import { defineTool } from "eve/tool";
import { z } from "zod";
import { getPbClient } from "../lib/pb.ts";
import { isAuthorizedAdmin } from "../lib/auth-utils.ts";

export const listFaqs = defineTool({
  description: "List existing FAQs from PocketBase with their IDs, categories, and questions. Restricted to administrators.",
  parameters: z.object({
    category: z.string().optional().describe("Optional category to filter by (e.g. Mechanics, Commands, Configuration)"),
    page: z.number().optional().default(1),
  }),
  execute: async ({ category, page = 1 }, ctx) => {
    const principalId = (ctx.session?.auth?.current as { id?: string } | undefined)?.id;
    if (!isAuthorizedAdmin(principalId)) {
      return "Unauthorized: Only server administrators can list all FAQs.";
    }

    const pb = getPbClient();
    try {
      const filter = category ? `category = "${category.replace(/['"\\]/g, "")}"` : "";
      const result = await pb.collection("faqs").getList(page, 20, {
        filter,
        sort: "category,question",
      });

      if (result.items.length === 0) {
        return "No FAQs found in the database.";
      }

      const rows = result.items.map(
        (item, idx) => `${idx + 1}. **[${item.category || "General"}]** ${item.question} *(ID: \`${item.id}\`)*`
      );

      return `**Cobbleloots FAQs (Page ${result.page}/${result.totalPages}, Total: ${result.totalItems}):**\n\n${rows.join("\n")}`;
    } catch (err) {
      return `Failed to list FAQs: ${(err as Error).message}`;
    }
  },
});

export default listFaqs;
```

- [ ] **Step 5: Implement `agent/tools/get_faq.ts`**

```typescript
import { defineTool } from "eve/tool";
import { z } from "zod";
import { getPbClient } from "../lib/pb.ts";
import { isAuthorizedAdmin } from "../lib/auth-utils.ts";

export const getFaq = defineTool({
  description: "Get full details of a specific FAQ by its PocketBase ID. Restricted to administrators.",
  parameters: z.object({
    id: z.string().describe("The PocketBase FAQ record ID"),
  }),
  execute: async ({ id }, ctx) => {
    const principalId = (ctx.session?.auth?.current as { id?: string } | undefined)?.id;
    if (!isAuthorizedAdmin(principalId)) {
      return "Unauthorized: Only server administrators can inspect FAQ details.";
    }

    const pb = getPbClient();
    try {
      const record = await pb.collection("faqs").getOne(id);
      return `**FAQ Details (ID: \`${record.id}\`):**\n` +
        `• **Question:** ${record.question}\n` +
        `• **Category:** ${record.category}\n` +
        `• **Keywords:** ${record.keywords || "None"}\n` +
        `• **Created By:** ${record.created_by || "Unknown"}\n\n` +
        `**Answer:**\n${record.answer}`;
    } catch (err) {
      return `Could not find FAQ with ID "${id}": ${(err as Error).message}`;
    }
  },
});

export default getFaq;
```

- [ ] **Step 6: Implement `agent/tools/save_faq.ts`**

```typescript
import { defineTool } from "eve/tool";
import { z } from "zod";
import { getPbClient } from "../lib/pb.ts";
import { isAuthorizedAdmin } from "../lib/auth-utils.ts";

export const saveFaq = defineTool({
  description: "Create or update an FAQ in PocketBase. Restricted to administrators.",
  parameters: z.object({
    id: z.string().optional().describe("If updating an existing FAQ, provide its ID. Omit to create a new FAQ."),
    question: z.string().describe("The question or title of the FAQ"),
    answer: z.string().describe("The full answer and instructions in Markdown"),
    category: z.string().describe("Category: Mechanics, Commands, Configuration, or Troubleshooting"),
    keywords: z.string().optional().describe("Comma-separated keywords for search"),
  }),
  execute: async ({ id, question, answer, category, keywords = "" }, ctx) => {
    const principalId = (ctx.session?.auth?.current as { id?: string } | undefined)?.id;
    if (!isAuthorizedAdmin(principalId)) {
      return "Unauthorized: Only server administrators can save FAQs.";
    }

    const pb = getPbClient();
    try {
      if (id) {
        const updated = await pb.collection("faqs").update(id, {
          question,
          answer,
          category,
          keywords,
        });
        return `Successfully updated FAQ "${updated.question}" (ID: \`${updated.id}\`).`;
      }

      const created = await pb.collection("faqs").create({
        question,
        answer,
        category,
        keywords,
        created_by: principalId,
      });
      return `Successfully created new FAQ "${created.question}" (ID: \`${created.id}\`).`;
    } catch (err) {
      return `Failed to save FAQ: ${(err as Error).message}`;
    }
  },
});

export default saveFaq;
```

- [ ] **Step 7: Implement `agent/tools/delete_faq.ts`**

```typescript
import { defineTool } from "eve/tool";
import { z } from "zod";
import { getPbClient } from "../lib/pb.ts";
import { isAuthorizedAdmin } from "../lib/auth-utils.ts";

export const deleteFaq = defineTool({
  description: "Delete an FAQ from PocketBase. Restricted to administrators. Requires explicit confirmation.",
  parameters: z.object({
    id: z.string().describe("The ID of the FAQ record to delete"),
    confirmed: z.boolean().optional().default(false).describe("Set to true only after the administrator has explicitly confirmed the deletion"),
  }),
  execute: async ({ id, confirmed }, ctx) => {
    const principalId = (ctx.session?.auth?.current as { id?: string } | undefined)?.id;
    if (!isAuthorizedAdmin(principalId)) {
      return "Unauthorized: Only server administrators can delete FAQs.";
    }

    const pb = getPbClient();
    try {
      const existing = await pb.collection("faqs").getOne(id);

      if (!confirmed) {
        return (
          `⚠️ **Confirmation Required:** Are you sure you want to permanently delete FAQ:\n` +
          `• **Question:** "${existing.question}"\n` +
          `• **Category:** ${existing.category}\n` +
          `• **ID:** \`${existing.id}\`\n\n` +
          `To proceed, reply with: \`@Cobbleloots Assistant confirm delete faq ${existing.id}\``
        );
      }

      await pb.collection("faqs").delete(id);
      return `Successfully deleted FAQ "${existing.question}" (ID: \`${id}\`).`;
    } catch (err) {
      return `Failed to delete FAQ with ID "${id}": ${(err as Error).message}`;
    }
  },
});

export default deleteFaq;
```

- [ ] **Step 8: Run tests to verify they pass**

Run: `cd infra/discord-bot && pnpm test tests/faq-tools.test.ts`
Expected: PASS.

- [ ] **Step 9: Commit changes**

```bash
git add infra/discord-bot/agent/tools/ infra/discord-bot/tests/faq-tools.test.ts
git commit -m "feat(bot): implement pocketbase faq grounding and admin management tools"
```

---

### Task 4: Dynamic Instructions with Bilingual Logic & Agent Definition

**Files:**
- Modify: `infra/discord-bot/agent/instructions.ts`
- Modify: `infra/discord-bot/agent/agent.ts`
- Test: `infra/discord-bot/tests/agent-config.test.ts`

**Interfaces:**
- Consumes: Admin flag from headers/attributes.
- Produces: System prompt enforcing English by default, natural Spanish if addressed in Spanish, anti-rush protocol, and thread context awareness.

- [ ] **Step 1: Write test for bilingual instructions in `tests/agent-config.test.ts`**

```typescript
import { describe, it, expect } from "vitest";
import { buildInstructionsPrompt } from "../agent/instructions.ts";

describe("Dynamic Instructions Prompt", () => {
  it("should enforce English as default and Spanish when requested", () => {
    const prompt = buildInstructionsPrompt({ isAdmin: false });
    expect(prompt).toContain("English");
    expect(prompt).toContain("Spanish");
    expect(prompt).toContain("Thread History");
  });

  it("should grant technical depth and class references for admins", () => {
    const prompt = buildInstructionsPrompt({ isAdmin: true });
    expect(prompt).toContain("Maintainer");
    expect(prompt).toContain("save_faq");
  });
});
```

- [ ] **Step 2: Update `agent/instructions.ts`**

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

- [ ] **Step 3: Update `agent/agent.ts` with all tools**

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

- [ ] **Step 4: Run test to verify it passes**

Run: `cd infra/discord-bot && pnpm test tests/agent-config.test.ts`
Expected: PASS.

- [ ] **Step 5: Commit changes**

```bash
git add infra/discord-bot/agent/instructions.ts infra/discord-bot/agent/agent.ts infra/discord-bot/tests/agent-config.test.ts
git commit -m "feat(bot): configure bilingual dynamic instructions and register pocketbase tools in agent"
```

---

### Task 5: Thread Context Engine & Gateway Supercharger (`agent/gateway.ts` & `runner.mjs`)

**Files:**
- Modify: `infra/discord-bot/agent/gateway.ts`
- Modify: `infra/discord-bot/runner.mjs`
- Test: `infra/discord-bot/tests/gateway.test.ts`

**Interfaces:**
- Consumes: Discord `messageCreate` event.
- Produces:
  - If in a thread: fetches last 20 messages, creates formatted `[Thread History]` block, feeds into Eve session.
  - If in regular channel: handles mention and reply reference.
  - Automatic message splitting (`splitMessage`) at 1900 chars on newline boundaries.
  - Clean `runner.mjs` starting Eve HTTP server on port 3000 and Gateway listener.

- [ ] **Step 1: Write test for thread context formatting in `tests/gateway.test.ts`**

```typescript
import { describe, it, expect } from "vitest";
import { formatThreadTranscript, extractPrompt, splitMessage } from "../agent/gateway.ts";

describe("Discord Gateway Thread Context & Helpers", () => {
  it("formatThreadTranscript should format messages chronologically with author and content", () => {
    const mockMessages = [
      { author: { username: "Alice", id: "1" }, content: "My loot ball vanished" },
      { author: { username: "Bob", id: "2" }, content: "Are you on NeoForge?" },
    ];
    const transcript = formatThreadTranscript(mockMessages as any, "desert-help", "bot_id");
    expect(transcript).toContain("[Thread History - #desert-help]");
    expect(transcript).toContain("Alice: My loot ball vanished");
    expect(transcript).toContain("Bob: Are you on NeoForge?");
  });

  it("extractPrompt should strip bot mention cleanly", () => {
    const prompt = extractPrompt("<@123456789> what are loot balls?", "123456789");
    expect(prompt).toBe("what are loot balls?");
  });

  it("splitMessage should split long text on paragraph boundary", () => {
    const longText = "Paragraph 1\n\n" + "A".repeat(1500) + "\n\nParagraph 2\n\n" + "B".repeat(1000);
    const chunks = splitMessage(longText, 1800);
    expect(chunks.length).toBeGreaterThan(1);
    expect(chunks[0].length).toBeLessThanOrEqual(1800);
  });
});
```

- [ ] **Step 2: Update `agent/gateway.ts`**

```typescript
import { Client, GatewayIntentBits, Partials, ActivityType, Events, type Message } from "discord.js";
import { Client as EveClient } from "eve/client";
import { isAuthorizedAdmin } from "./lib/auth-utils.ts";

export const DEFAULT_GREETING =
  "Hello! I am the official Cobbleloots assistant. You can ask me about mechanics, commands, loot tables, or mod configuration. How can I help you today?";

export function extractPrompt(content: string, botId: string): string {
  const mentionPattern = new RegExp(`<@!?${botId}>`, "g");
  return content.replace(mentionPattern, "").trim();
}

export function shouldRespondToMessage(
  message: {
    author: { bot: boolean };
    mentions: { has: (id: string) => boolean };
    reference?: { messageId?: string | null } | null;
  },
  botId: string,
  isReplyToBot = false
): boolean {
  if (message.author.bot) return false;
  if (message.mentions.has(botId)) return true;
  if (isReplyToBot && message.reference?.messageId) return true;
  return false;
}

export function formatThreadTranscript(
  messages: Array<{ author: { username: string; id: string }; content: string }>,
  threadName: string,
  botId: string
): string {
  const cleanMessages = messages
    .filter((m) => m.content && m.content.trim().length > 0)
    .map((m) => {
      const cleanContent = extractPrompt(m.content, botId);
      return `${m.author.username}: ${cleanContent}`;
    });

  return `[Thread History - #${threadName}]\n${cleanMessages.join("\n")}\n\n`;
}

export function splitMessage(text: string, maxLength = 1900): string[] {
  if (text.length <= maxLength) return [text];
  const chunks: string[] = [];
  let remaining = text;

  while (remaining.length > maxLength) {
    let splitIdx = remaining.lastIndexOf("\n\n", maxLength);
    if (splitIdx <= 0) splitIdx = remaining.lastIndexOf("\n", maxLength);
    if (splitIdx <= 0) splitIdx = remaining.lastIndexOf(" ", maxLength);
    if (splitIdx <= 0) splitIdx = maxLength;

    chunks.push(remaining.slice(0, splitIdx).trimEnd());
    remaining = remaining.slice(splitIdx).trimStart();
  }

  if (remaining.length > 0) {
    chunks.push(remaining);
  }

  return chunks;
}

export interface GatewayOptions {
  client?: Client;
  token?: string;
  port?: string | number;
}

export async function handleDiscordMessage(
  message: Message,
  client: Client,
  eveClient: EveClient
): Promise<void> {
  const botId = client.user?.id;
  if (!botId) return;

  // Check if replying directly to a bot message
  let isReplyToBot = false;
  let replyContext = "";
  if (message.reference?.messageId) {
    try {
      const referencedMessage = await message.channel.messages.fetch(message.reference.messageId);
      isReplyToBot = referencedMessage.author.id === botId;
      if (!isReplyToBot && referencedMessage.content) {
        replyContext = `[Replying to ${referencedMessage.author.username}: "${referencedMessage.content}"]\n\n`;
      }
    } catch {
      // Ignored if inaccessible
    }
  }

  if (!shouldRespondToMessage(message, botId, isReplyToBot)) return;

  const rawPrompt = extractPrompt(message.content, botId);
  if (!rawPrompt) {
    await message.reply(DEFAULT_GREETING);
    return;
  }

  // Extract thread context if inside a Discord thread
  let threadContext = "";
  if ("isThread" in message.channel && typeof message.channel.isThread === "function" && message.channel.isThread()) {
    try {
      const fetched = await message.channel.messages.fetch({ limit: 20 });
      const sorted = Array.from(fetched.values()).reverse();
      threadContext = formatThreadTranscript(sorted, message.channel.name, botId);
    } catch (err) {
      console.warn("[Gateway] Could not fetch thread context:", (err as Error).message);
    }
  }

  const finalPrompt = `${threadContext}${replyContext}${rawPrompt}`;

  // Keep typing indicator active
  const sendTyping = () => {
    if ("sendTyping" in message.channel && typeof message.channel.sendTyping === "function") {
      message.channel.sendTyping().catch(() => {});
    }
  };

  sendTyping();
  const typingInterval = setInterval(sendTyping, 7000);

  try {
    const isAdmin = isAuthorizedAdmin(message.author.id);
    const { response } = await eveClient.sessions.create({
      message: finalPrompt,
      headers: {
        "x-discord-user-id": message.author.id,
        "x-discord-is-admin": isAdmin ? "true" : "false",
      },
    });

    const result = await response.result();
    clearInterval(typingInterval);

    const answer = result.message;
    if (!answer || result.status === "failed") {
      await message.reply("Sorry, an error occurred while processing your request. Please try again.");
      return;
    }

    const chunks = splitMessage(answer);
    await message.reply(chunks[0]);
    if (chunks.length > 1 && "send" in message.channel && typeof message.channel.send === "function") {
      for (let i = 1; i < chunks.length; i++) {
        await message.channel.send(chunks[i]);
      }
    }
  } catch (error) {
    clearInterval(typingInterval);
    console.error("Error processing Discord message:", error);
    await message.reply("An error occurred while communicating with the Cobbleloots assistant. Please try again later.");
  }
}

export async function startGateway(options: GatewayOptions = {}): Promise<Client | null> {
  const token = options.token || process.env.DISCORD_BOT_TOKEN;
  if (!token) {
    console.warn("DISCORD_BOT_TOKEN not provided, skipping Discord Gateway listener.");
    return null;
  }

  const port = options.port || process.env.PORT || 3000;
  const eveClient = new EveClient({
    host: `http://127.0.0.1:${port}`,
  });

  const client =
    options.client ||
    new Client({
      intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.MessageContent,
      ],
      partials: [Partials.Channel, Partials.Message],
    });

  client.on(Events.ClientReady, () => {
    console.log(`[Gateway] Discord Gateway connected as ${client.user?.tag}!`);
    client.user?.setActivity("Cobbleloots | Tag me to ask!", {
      type: ActivityType.Custom,
    });
  });

  client.on(Events.MessageCreate, async (message) => {
    await handleDiscordMessage(message, client, eveClient);
  });

  await client.login(token);
  return client;
}
```

- [ ] **Step 3: Update `runner.mjs`**

```javascript
import { spawn } from "node:child_process";
import { setTimeout } from "node:timers/promises";

// 1. Start Eve HTTP Server
const serverEntry = ".output/server/index.mjs";
console.log(`[Runner] Starting Eve server from ${serverEntry}...`);

const server = spawn(process.execPath, [serverEntry], {
  stdio: "inherit",
  env: process.env,
});

server.on("error", (err) => {
  console.error("[Runner] Failed to start Eve server:", err);
  process.exit(1);
});

server.on("exit", (code, signal) => {
  console.log(`[Runner] Eve server exited with code ${code}, signal ${signal}`);
  process.exit(code ?? 0);
});

// 2. Poll for Health Readiness
async function waitForHealth(port, maxAttempts = 60) {
  const url = `http://127.0.0.1:${port}/eve/v1/health`;
  for (let i = 0; i < maxAttempts; i++) {
    try {
      const res = await fetch(url);
      if (res.ok) return true;
    } catch {
      // Server not ready yet
    }
    await setTimeout(500);
  }
  throw new Error(`[Runner] Eve server did not respond at ${url} within timeout`);
}

const port = process.env.PORT || 3000;
try {
  await waitForHealth(port);
  console.log(`[Runner] Eve server is healthy and listening on port ${port}!`);
} catch (err) {
  console.error(err.message);
  server.kill("SIGTERM");
  process.exit(1);
}

// 3. Start Discord Gateway Listener
if (process.env.DISCORD_BOT_TOKEN && process.env.DISABLE_GATEWAY !== "true") {
  console.log("[Runner] Starting Discord Gateway listener for @mentions...");
  try {
    const { startGateway } = await import("./agent/gateway.ts");
    await startGateway({ port });
  } catch (err) {
    console.error("[Runner] Failed to start Discord Gateway listener:", err);
  }
} else {
  console.log("[Runner] DISCORD_BOT_TOKEN not provided or DISABLE_GATEWAY=true. Gateway listener skipped.");
}

// 4. Graceful Shutdown Handlers
function shutdown(signal) {
  console.log(`[Runner] Received ${signal}, shutting down gracefully...`);
  server.kill(signal);
  process.exit(0);
}

process.on("SIGTERM", () => shutdown("SIGTERM"));
process.on("SIGINT", () => shutdown("SIGINT"));
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd infra/discord-bot && pnpm test tests/gateway.test.ts`
Expected: PASS.

- [ ] **Step 5: Commit changes**

```bash
git add infra/discord-bot/agent/gateway.ts infra/discord-bot/runner.mjs infra/discord-bot/tests/gateway.test.ts
git commit -m "feat(bot): add thread context engine, message chunking, and clean runner orchestration"
```

---

### Task 6: Pure `pnpm` Multi-Stage Dockerfile & Verification

**Files:**
- Modify: `infra/discord-bot/Dockerfile`
- Modify: `infra/discord-bot/.dockerignore`
- Modify: `infra/discord-bot/README.md`

**Interfaces:**
- Consumes: `package.json`, `pnpm-lock.yaml`.
- Produces: Ultra-fast multi-stage Docker container built with `pnpm` on `node:24-slim` with zero C++ compilers or native modules.

- [ ] **Step 1: Update `infra/discord-bot/Dockerfile`**

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

- [ ] **Step 2: Update `infra/discord-bot/.dockerignore`**

Ensure `node_modules`, `.output`, `.eve`, `.git` are ignored:
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

- [ ] **Step 3: Update `infra/discord-bot/README.md` with PocketBase & pnpm instructions**

Document all environment variables, Coolify setup steps, and admin commands.

- [ ] **Step 4: Run full test suite and TypeScript check**

Run:
```bash
cd infra/discord-bot
pnpm test
pnpm run typecheck
```
Expected: 100% test pass, 0 typecheck errors.

- [ ] **Step 5: Test Docker build locally**

Run: `docker build -t test-pocketbase-bot infra/discord-bot`
Expected: Build finishes cleanly in ~15-20 seconds with exit code 0.

- [ ] **Step 6: Commit changes**

```bash
git add infra/discord-bot/Dockerfile infra/discord-bot/.dockerignore infra/discord-bot/README.md
git commit -m "feat(bot): configure pure pnpm multi-stage dockerfile and documentation"
```

---

## Verification Plan

### Automated Tests
Run inside `infra/discord-bot/`:
1. `pnpm test` - runs all unit test suites (pb-client, faq-tools, agent-config, gateway, hitl-tools, grounding tools).
2. `pnpm run typecheck` - strict TypeScript compile check with zero errors.

### Docker Build & Runtime Verification
1. Build container: `docker build -t cobbleloots-discord-bot infra/discord-bot`
2. Test container run: `docker run --rm -e DISABLE_GATEWAY=true cobbleloots-discord-bot`
3. Verify output:
   ```text
   [Runner] Starting Eve server from .output/server/index.mjs...
   ➜ Listening on: http://localhost:3000/ (all interfaces)
   [Runner] Eve server is healthy and listening on port 3000!
   [Runner] DISCORD_BOT_TOKEN not provided or DISABLE_GATEWAY=true. Gateway listener skipped.
   ```
