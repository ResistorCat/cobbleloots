import { defineTool } from "eve/tools";
import { z } from "zod";

export const askQuestionTool = defineTool({
  description: "Ask the user a question and clarify missing requirements or ambiguous bug details before proceeding. Use this when critical details (such as loader, version, or game mode) are omitted.",
  inputSchema: z.object({
    question: z.string().describe("The focused question to ask the user"),
    options: z.array(z.string()).optional().describe("2-3 concrete options to help the user answer"),
  }),
  async execute({ question, options = [] }: { question: string; options?: string[] }) {
    const formattedOptions = options.length > 0
      ? `\nOptions:\n${options.map((opt, i) => `${i + 1}. **${opt}**`).join("\n")}`
      : "";
    return `CLARIFICATION_REQUIRED: You must present this question to the user now in your message and wait for their answer before providing solutions:\n"${question}"${formattedOptions}`;
  },
});

export default askQuestionTool;
