# Task 3 Brief: PocketBase Grounding & Admin Tools (agent/tools/)

## Context & Project Role
This is Task 3 of the Cobbleloots Discord AI Support Bot re-architecture. Task 2 created the PocketBase client in `agent/lib/pb.ts`. In this task, you will implement all 5 FAQ tools backed by PocketBase:
- `search_faqs.ts` (public, used by the model for grounding)
- `list_faqs.ts` (admin-only)
- `get_faq.ts` (admin-only)
- `save_faq.ts` (admin-only, direct execution)
- `delete_faq.ts` (admin-only, with interactive confirmation gate)

## Global Constraints
- Target directory is strictly isolated within `infra/discord-bot/`. Zero edits to Minecraft Java/Kotlin code, Gradle scripts, or `CHANGELOG.md`.
- No C++ compilers or native modules.
- Use `pnpm` (`pnpm test`, `pnpm run typecheck`).
- Follow TDD: write failing tests in `tests/faq-tools.test.ts`, implement tools, verify all tests pass.

## Exact Actions Required

### 1. Write Failing Tests in `infra/discord-bot/tests/faq-tools.test.ts`
Create `infra/discord-bot/tests/faq-tools.test.ts`:
```typescript
import { describe, it, expect, vi, beforeEach } from "vitest";
import { searchFaqs } from "../agent/tools/search_faqs.ts";
import { listFaqs } from "../agent/tools/list_faqs.ts";
import { getFaq } from "../agent/tools/get_faq.ts";
import { saveFaq } from "../agent/tools/save_faq.ts";
import { deleteFaq } from "../agent/tools/delete_faq.ts";
import { resetPbClientForTesting, getPbClient } from "../agent/lib/pb.ts";

describe("PocketBase FAQ Tools", () => {
  beforeEach(() => {
    resetPbClientForTesting();
    process.env.DISCORD_ADMIN_IDS = "admin_123,super_admin";
  });

  describe("searchFaqs", () => {
    it("should return message if no FAQs match", async () => {
      const client = getPbClient();
      vi.spyOn(client, "collection").mockReturnValue({
        getList: vi.fn().mockResolvedValue({ items: [] }),
      } as any);

      const result = await searchFaqs.execute({ query: "nonexistent" }, {} as any);
      expect(result).toContain('No community FAQs found matching "nonexistent"');
    });

    it("should return formatted list of matching FAQs", async () => {
      const client = getPbClient();
      vi.spyOn(client, "collection").mockReturnValue({
        getList: vi.fn().mockResolvedValue({
          items: [
            {
              id: "rec1",
              category: "Mechanics",
              question: "Do loot balls respawn?",
              answer: "No, they do not respawn automatically.",
            },
          ],
        }),
      } as any);

      const result = await searchFaqs.execute({ query: "respawn" }, {} as any);
      expect(result).toContain("[FAQ: Mechanics] Do loot balls respawn?");
      expect(result).toContain("No, they do not respawn automatically.");
    });
  });

  describe("listFaqs", () => {
    it("should reject non-admin users", async () => {
      const result = await listFaqs.execute({}, {
        session: { auth: { current: { id: "player_456" } } },
      } as any);
      expect(result).toContain("Unauthorized");
    });

    it("should list FAQs for authorized admin", async () => {
      const client = getPbClient();
      vi.spyOn(client, "collection").mockReturnValue({
        getList: vi.fn().mockResolvedValue({
          page: 1,
          totalPages: 1,
          totalItems: 1,
          items: [
            { id: "faq_abc", category: "Commands", question: "How to reload config?" },
          ],
        }),
      } as any);

      const result = await listFaqs.execute({}, {
        session: { auth: { current: { id: "admin_123" } } },
      } as any);
      expect(result).toContain("Cobbleloots FAQs");
      expect(result).toContain("How to reload config?");
      expect(result).toContain("faq_abc");
    });
  });

  describe("getFaq", () => {
    it("should reject non-admin users", async () => {
      const result = await getFaq.execute({ id: "rec1" }, {
        session: { auth: { current: { id: "player_456" } } },
      } as any);
      expect(result).toContain("Unauthorized");
    });

    it("should return full FAQ details for admin", async () => {
      const client = getPbClient();
      vi.spyOn(client, "collection").mockReturnValue({
        getOne: vi.fn().mockResolvedValue({
          id: "rec1",
          question: "How to catch Pokémon?",
          answer: "Throw a Poké Ball.",
          category: "Mechanics",
          keywords: "catch, ball",
          created_by: "admin_123",
        }),
      } as any);

      const result = await getFaq.execute({ id: "rec1" }, {
        session: { auth: { current: { id: "admin_123" } } },
      } as any);
      expect(result).toContain("How to catch Pokémon?");
      expect(result).toContain("Throw a Poké Ball.");
    });
  });

  describe("saveFaq", () => {
    it("should reject non-admin users", async () => {
      const result = await saveFaq.execute(
        { question: "Q", answer: "A", category: "General" },
        { session: { auth: { current: { id: "player_456" } } } } as any
      );
      expect(result).toContain("Unauthorized");
    });

    it("should create a new FAQ for admin", async () => {
      const client = getPbClient();
      vi.spyOn(client, "collection").mockReturnValue({
        create: vi.fn().mockResolvedValue({ id: "new_123", question: "New Q" }),
      } as any);

      const result = await saveFaq.execute(
        { question: "New Q", answer: "New A", category: "Mechanics", keywords: "test" },
        { session: { auth: { current: { id: "admin_123" } } } } as any
      );
      expect(result).toContain('Successfully created new FAQ "New Q" (ID: `new_123`)');
    });

    it("should update an existing FAQ when id is provided", async () => {
      const client = getPbClient();
      vi.spyOn(client, "collection").mockReturnValue({
        update: vi.fn().mockResolvedValue({ id: "exist_123", question: "Updated Q" }),
      } as any);

      const result = await saveFaq.execute(
        { id: "exist_123", question: "Updated Q", answer: "Updated A", category: "Mechanics" },
        { session: { auth: { current: { id: "admin_123" } } } } as any
      );
      expect(result).toContain('Successfully updated FAQ "Updated Q" (ID: `exist_123`)');
    });
  });

  describe("deleteFaq", () => {
    it("should reject non-admin users", async () => {
      const result = await deleteFaq.execute({ id: "rec1" }, {
        session: { auth: { current: { id: "player_456" } } },
      } as any);
      expect(result).toContain("Unauthorized");
    });

    it("should prompt for confirmation when confirmed is not true", async () => {
      const client = getPbClient();
      vi.spyOn(client, "collection").mockReturnValue({
        getOne: vi.fn().mockResolvedValue({ id: "rec1", question: "Delete Me?", category: "General" }),
      } as any);

      const result = await deleteFaq.execute({ id: "rec1", confirmed: false }, {
        session: { auth: { current: { id: "admin_123" } } },
      } as any);
      expect(result).toContain("Confirmation Required");
      expect(result).toContain("Delete Me?");
      expect(result).toContain("confirm delete faq rec1");
    });

    it("should delete FAQ when confirmed is true", async () => {
      const client = getPbClient();
      const deleteMock = vi.fn().mockResolvedValue(true);
      vi.spyOn(client, "collection").mockReturnValue({
        getOne: vi.fn().mockResolvedValue({ id: "rec1", question: "Delete Me?", category: "General" }),
        delete: deleteMock,
      } as any);

      const result = await deleteFaq.execute({ id: "rec1", confirmed: true }, {
        session: { auth: { current: { id: "admin_123" } } },
      } as any);
      expect(deleteMock).toHaveBeenCalledWith("rec1");
      expect(result).toContain('Successfully deleted FAQ "Delete Me?" (ID: `rec1`)');
    });
  });
});
```

### 2. Run Test to Verify Failure
Run inside `infra/discord-bot`:
`pnpm test tests/faq-tools.test.ts`
Verify it fails because `list_faqs.ts`, `get_faq.ts`, `delete_faq.ts` do not exist yet.

### 3. Implement the Tools
- Update `infra/discord-bot/agent/tools/search_faqs.ts`
- Create `infra/discord-bot/agent/tools/list_faqs.ts`
- Create `infra/discord-bot/agent/tools/get_faq.ts`
- Update `infra/discord-bot/agent/tools/save_faq.ts`
- Create `infra/discord-bot/agent/tools/delete_faq.ts`
(Follow exact implementations defined in the plan).

### 4. Run Tests & Verify Pass
Run: `pnpm test tests/faq-tools.test.ts`
Verify all 9 tests pass.

### 5. Commit Your Work
```bash
git add infra/discord-bot/agent/tools/ infra/discord-bot/tests/faq-tools.test.ts
git commit -m "feat(bot): implement pocketbase faq grounding and admin management tools"
```

## Report Contract
Write your full report to:
`c:\Users\franc\GitHub\cobbleloots\.worktrees\ripio\dev-30-cobbleloots-implementar-bot-de-soporte-de-discord-con-eve\.superpowers\sdd\2026-10-02-discord-bot-pocketbase-plan\task-3-report.md`

Then report back with ONLY:
- Status: DONE | DONE_WITH_CONCERNS | BLOCKED | NEEDS_CONTEXT
- Commits created (short SHA + subject)
- One-line test summary
- Your concerns, if any
- The report file path
