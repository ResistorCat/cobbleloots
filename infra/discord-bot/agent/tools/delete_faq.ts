import { defineTool } from "eve/tools";
import { z } from "zod";
import { getAuthenticatedPbClient, formatPbError } from "../lib/pb.ts";
import { isSessionAuthorizedAdmin } from "../lib/auth-utils.ts";

const deleteFaqSchema = z.object({
  id: z.string().describe("The ID of the FAQ record to delete"),
});

export const deleteFaq = defineTool({
  description: "Delete an FAQ from PocketBase. Restricted to administrators.",
  inputSchema: deleteFaqSchema,
  execute: async ({ id }: { id: string }, ctx: any) => {
    console.log(`[delete_faq] Invoked execute for id: "${id}"`);
    if (!isSessionAuthorizedAdmin(ctx)) {
      console.warn(`[delete_faq] Unauthorized attempt for id: "${id}", session auth:`, JSON.stringify(ctx?.session?.auth));
      return "Unauthorized: Only server administrators can delete FAQs.";
    }

    try {
      const pb = await getAuthenticatedPbClient();
      const existing = await pb.collection("faqs").getOne(id);
      await pb.collection("faqs").delete(id);
      console.log(`[delete_faq] Successfully deleted FAQ "${existing.question}" (ID: ${id})`);
      return `Successfully deleted FAQ "${existing.question}" (ID: \`${id}\`).`;
    } catch (err) {
      console.error(`[delete_faq] Error deleting FAQ in PocketBase (ID: ${id}):`, err);
      return `Failed to delete FAQ with ID "${id}": ${formatPbError(err)}`;
    }
  },
} as any);

export default deleteFaq;
