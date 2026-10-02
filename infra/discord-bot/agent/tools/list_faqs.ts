import { defineTool } from "eve/tools";
import { z } from "zod";
import { getPbClient } from "../lib/pb.ts";
import { isAuthorizedAdmin } from "../lib/auth-utils.ts";

const listFaqsSchema = z.object({
  category: z.string().optional().describe("Optional category to filter by (e.g. Mechanics, Commands, Configuration)"),
  page: z.number().optional().default(1),
});

export const listFaqs = defineTool({
  description: "List existing FAQs from PocketBase with their IDs, categories, and questions. Restricted to administrators.",
  inputSchema: listFaqsSchema,
  execute: async ({ category, page = 1 }: { category?: string; page?: number }, ctx: any) => {
    const principalId = (ctx?.session?.auth?.current as { id?: string } | undefined)?.id;
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
} as any);

export default listFaqs;
