import { defineTool } from "eve/tools";
import { always } from "eve/tools/approval";
import { z } from "zod";
import { getDb } from "../lib/db";
import { insertFaq } from "../lib/faq-store";
import { isAuthorizedAdmin } from "../lib/auth-utils";
import type Database from "better-sqlite3";

export { isAuthorizedAdmin };

export default defineTool({
  description: "Propose saving a frequently asked question and official answer to the global FAQ database. Requires moderator or admin approval before persistence.",
  inputSchema: z.object({
    question: z.string().describe("The user query or recurrent question"),
    answer: z.string().describe("The curated, accurate answer based on official docs or code"),
    category: z.string().optional().describe("Category, e.g. 'loot-balls', 'installation', 'commands'"),
    dbInstance: z.any().optional(),
  }),
  approval: {
    request: always(),
    response: ({ responder }) => {
      if (!isAuthorizedAdmin(responder.principalId)) {
        return {
          status: "rejected",
          reason: "Only authorized Cobbleloots moderators or admins can approve FAQ entries.",
        };
      }
      return { status: "allowed" };
    },
  },
  async execute({ question, answer, category, dbInstance }, ctx) {
    const db = (dbInstance as Database.Database) || getDb();
    const approver = ctx.session?.auth?.current?.principalId ?? "system";
    const id = insertFaq(db, {
      question,
      answer,
      category: category ?? "general",
      approvedBy: approver,
    });

    return {
      success: true,
      faqId: id,
      message: `FAQ #${id} successfully recorded into the knowledge base.`,
    };
  },
});
