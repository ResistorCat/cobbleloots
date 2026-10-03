import { defineTool } from "eve/tools";
import { z } from "zod";
import { getAuthenticatedPbClient, formatPbError } from "../lib/pb.ts";
import { isSessionAuthorizedAdmin } from "../lib/auth-utils.ts";

const getFaqSchema = z.object({
  id: z.string().describe("The PocketBase FAQ record ID"),
});

export const getFaq = defineTool({
  description: "Get full details of a specific FAQ by its PocketBase ID. Restricted to administrators.",
  inputSchema: getFaqSchema,
  execute: async ({ id }: { id: string }, ctx: any) => {
    if (!isSessionAuthorizedAdmin(ctx)) {
      return "Unauthorized: Only server administrators can inspect FAQ details.";
    }

    try {
      const pb = await getAuthenticatedPbClient();
      const record = await pb.collection("faqs").getOne(id);
      return `**FAQ Details (ID: \`${record.id}\`):**\n` +
        `• **Question:** ${record.question}\n` +
        `• **Category:** ${record.category}\n` +
        `• **Keywords:** ${record.keywords || "None"}\n` +
        `• **Created By:** ${record.created_by || "Unknown"}\n\n` +
        `**Answer:**\n${record.answer}`;
    } catch (err) {
      console.error("[get_faq] Error fetching FAQ in PocketBase:", err);
      return `Could not find FAQ with ID "${id}": ${formatPbError(err)}`;
    }
  },
} as any);

export default getFaq;
