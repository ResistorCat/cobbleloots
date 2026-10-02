import { defineTool } from "eve/tools";
import { z } from "zod";
import { getPbClient } from "../lib/pb.ts";
import { isAuthorizedAdmin } from "../lib/auth-utils.ts";

const deleteFaqSchema = z.object({
  id: z.string().describe("The ID of the FAQ record to delete"),
  confirmed: z.boolean().optional().default(false).describe("Set to true only after the administrator has explicitly confirmed the deletion"),
});

export const deleteFaq = defineTool({
  description: "Delete an FAQ from PocketBase. Restricted to administrators. Requires explicit confirmation.",
  inputSchema: deleteFaqSchema,
  parameters: deleteFaqSchema,
  execute: async ({ id, confirmed = false }: { id: string; confirmed?: boolean }, ctx: any) => {
    const principalId = (ctx?.session?.auth?.current as { id?: string } | undefined)?.id;
    if (!isAuthorizedAdmin(principalId)) {
      return "Unauthorized: Only server administrators can delete FAQs.";
    }

    const pb = getPbClient();
    try {
      const existing = await pb.collection("faqs").getOne(id);

      if (!confirmed) {
        return (
          `⚠️ **Confirmation Required:** Are you sure you want to permanently delete FAQ:\n` +
          `• **Question:** "${existing.question}"\n` +
          `• **Category:** ${existing.category}\n` +
          `• **ID:** \`${existing.id}\`\n\n` +
          `To proceed, reply with: \`@Cobbleloots Assistant confirm delete faq ${existing.id}\``
        );
      }

      await pb.collection("faqs").delete(id);
      return `Successfully deleted FAQ "${existing.question}" (ID: \`${id}\`).`;
    } catch (err) {
      return `Failed to delete FAQ with ID "${id}": ${(err as Error).message}`;
    }
  },
} as any);

export default deleteFaq;
