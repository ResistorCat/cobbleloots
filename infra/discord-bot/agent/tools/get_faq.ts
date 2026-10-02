import { defineTool } from "eve/tools";
import { z } from "zod";
import { getPbClient } from "../lib/pb.ts";
import { isAuthorizedAdmin } from "../lib/auth-utils.ts";

const getFaqSchema = z.object({
  id: z.string().describe("The PocketBase FAQ record ID"),
});

export const getFaq = defineTool({
  description: "Get full details of a specific FAQ by its PocketBase ID. Restricted to administrators.",
  inputSchema: getFaqSchema,
  parameters: getFaqSchema,
  execute: async ({ id }: { id: string }, ctx: any) => {
    const principalId = (ctx?.session?.auth?.current as { id?: string } | undefined)?.id;
    if (!isAuthorizedAdmin(principalId)) {
      return "Unauthorized: Only server administrators can inspect FAQ details.";
    }

    const pb = getPbClient();
    try {
      const record = await pb.collection("faqs").getOne(id);
      return `**FAQ Details (ID: \`${record.id}\`):**\n` +
        `• **Question:** ${record.question}\n` +
        `• **Category:** ${record.category}\n` +
        `• **Keywords:** ${record.keywords || "None"}\n` +
        `• **Created By:** ${record.created_by || "Unknown"}\n\n` +
        `**Answer:**\n${record.answer}`;
    } catch (err) {
      return `Could not find FAQ with ID "${id}": ${(err as Error).message}`;
    }
  },
} as any);

export default getFaq;
