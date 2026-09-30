import { defineTool } from "eve/tools";
import { z } from "zod";
import fs from "node:fs";
import path from "node:path";

const ALLOWED_ROOTS = ["common", "fabric", "neoforge"];

function getRepoRoot(): string {
  if (process.env.REPO_ROOT) return process.env.REPO_ROOT;
  const candidate = path.resolve(process.cwd(), "../..");
  if (fs.existsSync(path.join(candidate, "common"))) return candidate;
  const rootCandidate = path.resolve(process.cwd());
  if (fs.existsSync(path.join(rootCandidate, "common"))) return rootCandidate;
  return candidate;
}

export default defineTool({
  description: "Inspect Cobbleloots source code classes, JSON data definitions, or registry files under common/, fabric/, or neoforge/.",
  inputSchema: z.object({
    filePath: z.string().describe("Relative path inside the repo (e.g., 'common/src/main/resources/data/cobbleloots/loot_table/...')"),
    startLine: z.number().optional().describe("Starting line (1-indexed)"),
    endLine: z.number().optional().describe("Ending line (1-indexed)"),
  }),
  async execute({ filePath, startLine, endLine }) {
    const repoRoot = getRepoRoot();
    const normalizedPath = path.normalize(filePath).replace(/^(\.\.(\/|\\|$))+/, "");
    const targetPath = path.resolve(repoRoot, normalizedPath);

    const isAllowed = ALLOWED_ROOTS.some((root) => {
      const allowedDir = path.resolve(repoRoot, root);
      return targetPath.startsWith(allowedDir);
    });

    if (!isAllowed) {
      return { error: `Access denied. Can only inspect files inside: ${ALLOWED_ROOTS.join(", ")}` };
    }

    if (!fs.existsSync(targetPath) || fs.statSync(targetPath).isDirectory()) {
      return { error: `File not found: ${filePath}` };
    }

    const content = fs.readFileSync(targetPath, "utf-8");
    const lines = content.split("\n");

    if (startLine !== undefined && endLine !== undefined) {
      const s = Math.max(1, startLine) - 1;
      const e = Math.min(lines.length, endLine);
      return {
        filePath,
        totalLines: lines.length,
        snippet: lines.slice(s, e).join("\n"),
      };
    }

    return {
      filePath,
      totalLines: lines.length,
      content: lines.slice(0, 150).join("\n"),
    };
  },
});
