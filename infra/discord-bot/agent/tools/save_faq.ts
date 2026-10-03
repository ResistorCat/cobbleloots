import { defineTool } from "eve/tools";
import { z } from "zod";
import { getAuthenticatedPbClient, formatPbError } from "../lib/pb.ts";
import { isAuthorizedAdmin, isSessionAuthorizedAdmin, getSessionCallerId } from "../lib/auth-utils.ts";

export { isAuthorizedAdmin };

export function generateRecordId(): string {
  const chars = "abcdefghijklmnopqrstuvwxyz0123456789";
  let result = "";
  for (let i = 0; i < 15; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

const saveFaqSchema = z.object({
  id: z.string().optional().describe("If updating an existing FAQ, provide its ID. Omit to create a new FAQ."),
  question: z.string().describe("The question or title of the FAQ"),
  answer: z.string().describe("The full answer and instructions in Markdown"),
  category: z.string().describe("Category: Mechanics, Commands, Configuration, or Troubleshooting"),
  keywords: z.string().optional().describe("Comma-separated keywords for search"),
});

export const saveFaq = defineTool({
  description: "Create or update an FAQ in PocketBase. Restricted to administrators.",
  inputSchema: saveFaqSchema,
  execute: async ({ id, question, answer, category, keywords = "" }: { id?: string; question: string; answer: string; category: string; keywords?: string }, ctx: any) => {
    if (!isSessionAuthorizedAdmin(ctx)) {
      return "Unauthorized: Only server administrators can save FAQs.";
    }
    const principalId = getSessionCallerId(ctx);

    try {
      const pb = await getAuthenticatedPbClient();
      if (id) {
        const updated = await pb.collection("faqs").update(id, {
          question,
          answer,
          category,
          keywords,
        });
        return `Successfully updated FAQ "${updated.question}" (ID: \`${updated.id}\`).`;
      }
      const recordId = generateRecordId();
      const created = await pb.collection("faqs").create({
        id: recordId,
        question,
        answer,
        category,
        keywords,
        created_by: principalId,
      });
      return `Successfully created new FAQ "${created.question}" (ID: \`${created.id}\`).`;
    } catch (err) {
      console.error("[save_faq] Error saving FAQ in PocketBase:", err);
      return `Failed to save FAQ: ${formatPbError(err)}`;
    }
  },
} as any);

export default saveFaq;
