import { defineTool } from "eve/tools";
import { z } from "zod";
import fs from "node:fs";
import path from "node:path";

function getRepoDocsPath(): string {
  if (process.env.REPO_DOCS_PATH) return process.env.REPO_DOCS_PATH;
  const candidate = path.resolve(process.cwd(), "../../docs");
  if (fs.existsSync(candidate)) return candidate;
  const rootCandidate = path.resolve(process.cwd(), "docs");
  if (fs.existsSync(rootCandidate)) return rootCandidate;
  return candidate;
}

export default defineTool({
  description: "Search official Cobbleloots documentation markdown files for gameplay mechanics, configuration, commands, and installation instructions.",
  inputSchema: z.object({
    query: z.string().describe("Search term or keywords to find in documentation"),
  }),
  async execute({ query }) {
    const docsDir = getRepoDocsPath();
    if (!fs.existsSync(docsDir)) {
      return { matches: [], message: "Docs directory not found." };
    }

    const queryLower = query.toLowerCase();
    const files = fs.readdirSync(docsDir, { recursive: true })
      .filter((file) => typeof file === "string" && file.endsWith(".md")) as string[];

    const matches: Array<{ file: string; excerpt: string }> = [];

    for (const relativeFile of files) {
      const fullPath = path.join(docsDir, relativeFile);
      const content = fs.readFileSync(fullPath, "utf-8");
      if (!content.toLowerCase().includes(queryLower)) continue;

      const lines = content.split("\n");
      const matchingLineIdx = lines.findIndex((l) => l.toLowerCase().includes(queryLower));
      const start = Math.max(0, matchingLineIdx - 2);
      const end = Math.min(lines.length, matchingLineIdx + 5);
      const excerpt = lines.slice(start, end).join("\n");

      matches.push({ file: relativeFile, excerpt });
      if (matches.length >= 5) break;
    }

    return { matches };
  },
});
