# Task 2 Brief: PocketBase Client & Connection Management (agent/lib/pb.ts)

## Context & Project Role
This is Task 2 of the Cobbleloots Discord AI Support Bot re-architecture. Task 1 migrated package management to `pnpm` and added `pocketbase@^0.26.0`. In this task, you will implement the PocketBase client singleton, superuser authentication, and automatic schema initialization for the `faqs` collection.

## Global Constraints
- Target directory is strictly isolated within `infra/discord-bot/`. Zero edits to Minecraft Java/Kotlin code, Gradle scripts, or `CHANGELOG.md`.
- No C++ compilers or native modules.
- Use `pnpm` (`pnpm test`, `pnpm run typecheck`).
- Follow TDD: write failing test, verify it fails, implement, verify it passes.

## Exact Actions Required

### 1. Write Failing Test in `infra/discord-bot/tests/pb-client.test.ts`
Create `infra/discord-bot/tests/pb-client.test.ts`:
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

### 2. Run Test to Verify Failure
Run inside `infra/discord-bot`:
`pnpm test tests/pb-client.test.ts`
Verify that it fails because `agent/lib/pb.ts` does not exist yet.

### 3. Implement `infra/discord-bot/agent/lib/pb.ts`
Create `infra/discord-bot/agent/lib/pb.ts`:
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
    } else if ((client as any).admins?.authWithPassword) {
      await (client as any).admins.authWithPassword(email, password);
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

### 4. Run Test to Verify It Passes
Run inside `infra/discord-bot`:
`pnpm test tests/pb-client.test.ts`
Verify that it passes.

### 5. Commit Your Work
```bash
git add infra/discord-bot/agent/lib/pb.ts infra/discord-bot/tests/pb-client.test.ts
git commit -m "feat(bot): implement pocketbase client singleton, auth, and schema bootstrap"
```

## Report Contract
Write your full report to:
`c:\Users\franc\GitHub\cobbleloots\.worktrees\ripio\dev-30-cobbleloots-implementar-bot-de-soporte-de-discord-con-eve\.superpowers\sdd\2026-10-02-discord-bot-pocketbase-plan\task-2-report.md`

Then report back with ONLY:
- Status: DONE | DONE_WITH_CONCERNS | BLOCKED | NEEDS_CONTEXT
- Commits created (short SHA + subject)
- One-line test summary
- Your concerns, if any
- The report file path
