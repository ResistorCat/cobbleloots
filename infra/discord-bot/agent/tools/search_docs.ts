import { defineTool } from "eve/tools";
import { z } from "zod";
import fs from "node:fs";
import path from "node:path";
import { searchDocsRemote, extractSearchKeywords } from "../lib/github-client.ts";

function getRepoDocsPath(): string | null {
  if (process.env.REPO_DOCS_PATH) {
    if (fs.existsSync(process.env.REPO_DOCS_PATH)) return process.env.REPO_DOCS_PATH;
    return null;
  }
  const candidate = path.resolve(process.cwd(), "../../docs");
  if (fs.existsSync(candidate)) return candidate;
  const rootCandidate = path.resolve(process.cwd(), "docs");
  if (fs.existsSync(rootCandidate)) return rootCandidate;
  return null;
}

export default defineTool({
  description: "Search official Cobbleloots documentation for gameplay mechanics, configuration, commands, and installation instructions. Pass concise keywords (e.g. 'loot ball survival', 'spawning', 'fishing', 'generation', 'reset').",
  inputSchema: z.object({
    query: z.string().describe("Search term or keywords to find in documentation"),
  }),
  async execute({ query }) {
    const docsDir = getRepoDocsPath();

    if (docsDir) {
      const queryLower = query.toLowerCase().trim();
      const keywords = extractSearchKeywords(query);
      const files = fs.readdirSync(docsDir, { recursive: true })
        .filter((file) => typeof file === "string" && file.endsWith(".md")) as string[];

      const scored: Array<{ file: string; excerpt: string; score: number }> = [];

      for (const relativeFile of files) {
        const fullPath = path.join(docsDir, relativeFile);
        const content = fs.readFileSync(fullPath, "utf-8");
        const contentLower = content.toLowerCase();
        const fileLower = relativeFile.toLowerCase();

        let score = 0;
        if (contentLower.includes(queryLower) || fileLower.includes(queryLower)) {
          score += 50;
        }

        for (const kw of keywords) {
          if (fileLower.includes(kw)) score += 15;
          if (contentLower.includes(kw)) score += 3;
        }

        if (score === 0) continue;

        const lines = content.split("\n");
        const matchingLineIdx = lines.findIndex((l) =>
          keywords.some((kw) => l.toLowerCase().includes(kw))
        );
        const start = Math.max(0, matchingLineIdx - 2);
        const end = Math.min(lines.length, matchingLineIdx + 5);
        const excerpt = lines.slice(start, end).join("\n");

        scored.push({ file: relativeFile, excerpt, score });
      }

      if (scored.length > 0) {
        scored.sort((a, b) => b.score - a.score);
        return { matches: scored.slice(0, 5).map(({ file, excerpt }) => ({ file, excerpt })) };
      }
    }

    const remoteMatches = await searchDocsRemote(query, 5);
    if (remoteMatches.length > 0) {
      return { matches: remoteMatches };
    }

    return {
      matches: [],
      message: "No matching documentation found.",
    };
  },
});
