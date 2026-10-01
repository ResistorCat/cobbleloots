import { defineTool } from "eve/tools";
import { z } from "zod";
import fs from "node:fs";
import path from "node:path";
import { fetchRepoFile, getLocalRepoRoot } from "../lib/github-client";

const ALLOWED_ROOTS = ["common", "fabric", "neoforge"];

export default defineTool({
  description: "Inspect Cobbleloots source code classes, JSON data definitions, or registry files under common/, fabric/, or neoforge/.",
  inputSchema: z.object({
    filePath: z.string().describe("Relative path inside the repo (e.g., 'common/src/main/resources/pack.mcmeta')"),
    startLine: z.number().optional().describe("Starting line (1-indexed)"),
    endLine: z.number().optional().describe("Ending line (1-indexed)"),
  }),
  async execute({ filePath, startLine, endLine }) {
    const normalized = filePath.replace(/\\/g, "/").replace(/^\/+/, "");

    if (normalized.includes("..") || path.isAbsolute(normalized)) {
      return { error: `Access denied. Invalid path or path traversal detected.` };
    }

    const isAllowed = ALLOWED_ROOTS.some(
      (root) => normalized === root || normalized.startsWith(`${root}/`)
    );

    if (!isAllowed) {
      return { error: `Access denied. Can only inspect files inside: ${ALLOWED_ROOTS.join(", ")}` };
    }

    const repoRoot = getLocalRepoRoot();
    if (repoRoot) {
      const targetPath = path.resolve(repoRoot, normalized);
      const isStrictlyInside = ALLOWED_ROOTS.some((root) => {
        const allowedDir = path.resolve(repoRoot, root);
        const rel = path.relative(allowedDir, targetPath);
        return !rel.startsWith("..") && !path.isAbsolute(rel);
      });

      if (!isStrictlyInside) {
        return { error: `Access denied. Can only inspect files inside: ${ALLOWED_ROOTS.join(", ")}` };
      }
    }

    let content: string | null = null;
    if (repoRoot) {
      const targetPath = path.resolve(repoRoot, normalized);
      if (fs.existsSync(targetPath) && !fs.statSync(targetPath).isDirectory()) {
        content = fs.readFileSync(targetPath, "utf-8");
      }
    }

    if (content === null) {
      content = await fetchRepoFile(normalized);
    }

    if (content === null) {
      return { error: `File not found: ${filePath}` };
    }

    const lines = content.split("\n");

    if (startLine !== undefined && endLine !== undefined) {
      const s = Math.max(1, startLine) - 1;
      const e = Math.min(lines.length, endLine);
      return {
        filePath: normalized,
        totalLines: lines.length,
        snippet: lines.slice(s, e).join("\n"),
      };
    }

    return {
      filePath: normalized,
      totalLines: lines.length,
      content: lines.slice(0, 150).join("\n"),
    };
  },
});
