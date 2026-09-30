import { describe, it, expect } from "vitest";
import searchDocsTool from "../agent/tools/search_docs";
import inspectCodeTool from "../agent/tools/inspect_code";
import getReleasesTool from "../agent/tools/get_releases";
import searchFaqsTool from "../agent/tools/search_faqs";
import { getDb } from "../agent/lib/db";
import { initFaqSchema, insertFaq } from "../agent/lib/faq-store";

describe("Grounding Tools", () => {
  it("search_docs should find content from docs", async () => {
    const result = await (searchDocsTool as any).execute({ query: "loot" }, {} as any);
    expect(result.matches).toBeDefined();
    expect(Array.isArray(result.matches)).toBe(true);
  });

  it("inspect_code should reject paths outside allowed directories", async () => {
    const result = await (inspectCodeTool as any).execute({ filePath: "../../../package.json" }, {} as any);
    expect(result.error).toBeDefined();
  });

  it("inspect_code should reject paths with partial directory names like common-secrets", async () => {
    const result = await (inspectCodeTool as any).execute({ filePath: "common-secrets/passwords.txt" }, {} as any);
    expect(result.error).toBeDefined();
    expect(result.error).toContain("Access denied");
  });

  it("get_releases should return recent changelog entries", async () => {
    const result = await (getReleasesTool as any).execute({ limit: 2 }, {} as any);
    expect(result.releases).toBeDefined();
  });

  it("search_faqs should return matches from database", async () => {
    const db = getDb(":memory:");
    initFaqSchema(db);
    insertFaq(db, { question: "Can I fish loot balls?", answer: "Yes with fishing config" });

    const result = await (searchFaqsTool as any).execute({ query: "fish", dbInstance: db }, {} as any);
    expect(result.results.length).toBeGreaterThan(0);
    expect(result.results[0].question).toContain("fish");
  });
});
