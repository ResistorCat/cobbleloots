import { defineTool } from "eve/tools";
import { z } from "zod";
import { getPbClient } from "../lib/pb.ts";

const searchFaqsSchema = z.object({
  query: z.string().describe("The search query or keyword to look for in FAQs"),
});

export const searchFaqs = defineTool({
  description: "Search community FAQs in PocketBase by question, keywords, or topic.",
  inputSchema: searchFaqsSchema,
  parameters: searchFaqsSchema,
  execute: async ({ query }: { query: string }) => {
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
} as any);

export default searchFaqs;
