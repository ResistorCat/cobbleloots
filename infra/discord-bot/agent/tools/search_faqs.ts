import { defineTool } from "eve/tools";
import { z } from "zod";
import { getDb } from "../lib/db";
import { searchFaqs } from "../lib/faq-store";
import type Database from "better-sqlite3";

export default defineTool({
  description: "Search community-curated FAQs from the global knowledge base.",
  inputSchema: z.object({
    query: z.string().describe("Keywords to search for in curated FAQs"),
    limit: z.number().optional().describe("Maximum results to return (default: 5)"),
    dbInstance: z.any().optional(),
  }),
  async execute({ query, limit = 5, dbInstance }) {
    const db = (dbInstance as Database.Database) || getDb();
    const results = searchFaqs(db, query, limit);
    return { results };
  },
});
