import { describe, it, expect, beforeEach } from "vitest";
import { getDb } from "../agent/lib/db";
import { initFaqSchema, insertFaq, searchFaqs, getFaqById } from "../agent/lib/faq-store";
import type Database from "better-sqlite3";

describe("FAQ Store & Database", () => {
  let db: Database.Database;

  beforeEach(() => {
    db = getDb(":memory:");
    initFaqSchema(db);
  });

  it("should initialize schema and insert an FAQ", () => {
    const id = insertFaq(db, {
      question: "How do I reset loot balls?",
      answer: "Use /cobbleloots reset lootballs <player>",
      category: "commands",
      approvedBy: "123456789",
    });

    expect(id).toBe(1);
    const record = getFaqById(db, id);
    expect(record).not.toBeNull();
    expect(record?.question).toBe("How do I reset loot balls?");
    expect(record?.category).toBe("commands");
  });

  it("should search FAQs matching keywords", () => {
    insertFaq(db, {
      question: "How do I reset loot balls?",
      answer: "Use the reset command",
      category: "commands",
    });
    insertFaq(db, {
      question: "Where do PokeBalls spawn?",
      answer: "They spawn naturally across biomes.",
      category: "gameplay",
    });

    const results = searchFaqs(db, "reset");
    expect(results.length).toBe(1);
    expect(results[0].question).toContain("reset");

    const emptyResults = searchFaqs(db, "nonexistent");
    expect(emptyResults.length).toBe(0);
  });
});
