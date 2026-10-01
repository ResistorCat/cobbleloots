import { defineTool } from "eve/tools";
import { z } from "zod";
import fs from "node:fs";
import path from "node:path";
import { getReleases, getLocalRepoRoot } from "../lib/github-client";

export default defineTool({
  description: "Fetch release notes and pending changelog fragments from GitHub releases API or local CHANGELOG.md.",
  inputSchema: z.object({
    limit: z.number().optional().describe("Number of changelog sections to return (default: 3)"),
  }),
  async execute({ limit = 3 }) {
    const repoRoot = getLocalRepoRoot();
    const pendingFragments: Array<{ file: string; content: string }> = [];

    if (repoRoot) {
      const fragmentsDir = path.join(repoRoot, ".changelog");
      if (fs.existsSync(fragmentsDir)) {
        const files = fs.readdirSync(fragmentsDir).filter((f) => f.endsWith(".md"));
        for (const file of files) {
          pendingFragments.push({
            file,
            content: fs.readFileSync(path.join(fragmentsDir, file), "utf-8"),
          });
        }
      }
    }

    const releaseItems = await getReleases(limit);
    let recentChangelog = "";

    if (releaseItems.length > 0) {
      recentChangelog = releaseItems
        .map((r) => {
          const header = r.name && r.name !== r.tagName ? `${r.name} (${r.tagName})` : r.tagName;
          const dateStr = r.publishedAt ? ` - ${r.publishedAt.slice(0, 10)}` : "";
          return `## ${header}${dateStr}\n\n${r.body || "No release notes provided."}`;
        })
        .join("\n\n---\n\n");
    } else if (repoRoot) {
      const changelogPath = path.join(repoRoot, "CHANGELOG.md");
      if (fs.existsSync(changelogPath)) {
        const content = fs.readFileSync(changelogPath, "utf-8");
        const sections = content.split(/(?=\n##\s+)/);
        recentChangelog = sections.slice(0, limit + 1).join("\n");
      }
    }

    return {
      pendingFragments,
      releases: recentChangelog || "No releases available.",
    };
  },
});
