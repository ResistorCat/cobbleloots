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
