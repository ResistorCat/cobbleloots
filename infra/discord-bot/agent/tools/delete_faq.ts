import { defineTool } from "eve/tools";
import { always } from "eve/tools/approval";
import { z } from "zod";
import { getAuthenticatedPbClient, formatPbError } from "../lib/pb.ts";
import { isSessionAuthorizedAdmin } from "../lib/auth-utils.ts";

const deleteFaqSchema = z.object({
  id: z.string().describe("The ID of the FAQ record to delete"),
});

export const deleteFaq = defineTool({
  description: "Delete an FAQ from PocketBase. Restricted to administrators. Gated by human approval.",
  inputSchema: deleteFaqSchema,
  approval: always(),
  execute: async ({ id }: { id: string }, ctx: any) => {
    if (!isSessionAuthorizedAdmin(ctx)) {
      return "Unauthorized: Only server administrators can delete FAQs.";
    }

    try {
      const pb = await getAuthenticatedPbClient();
      const existing = await pb.collection("faqs").getOne(id);
      await pb.collection("faqs").delete(id);
      return `Successfully deleted FAQ "${existing.question}" (ID: \`${id}\`).`;
    } catch (err) {
      console.error("[delete_faq] Error deleting FAQ in PocketBase:", err);
      return `Failed to delete FAQ with ID "${id}": ${formatPbError(err)}`;
    }
  },
} as any);

export default deleteFaq;
