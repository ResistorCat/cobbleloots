import { defineTool } from "eve/tools";
import { z } from "zod";
import fs from "node:fs";
import path from "node:path";

function getRepoRoot(): string {
  if (process.env.REPO_ROOT) return process.env.REPO_ROOT;
  const candidate = path.resolve(process.cwd(), "../..");
  if (fs.existsSync(path.join(candidate, "CHANGELOG.md")) || fs.existsSync(path.join(candidate, "common"))) {
    return candidate;
  }
  if (fs.existsSync(path.join(process.cwd(), "CHANGELOG.md")) || fs.existsSync(path.join(process.cwd(), "common"))) {
    return process.cwd();
  }
  return candidate;
}

export default defineTool({
  description: "Fetch release notes and pending changelog fragments from CHANGELOG.md and .changelog/.",
  inputSchema: z.object({
    limit: z.number().optional().describe("Number of changelog sections to return (default: 3)"),
  }),
  async execute({ limit = 3 }) {
    const repoRoot = getRepoRoot();
    const changelogPath = path.join(repoRoot, "CHANGELOG.md");
    const fragmentsDir = path.join(repoRoot, ".changelog");

    const pendingFragments: Array<{ file: string; content: string }> = [];
    if (fs.existsSync(fragmentsDir)) {
      const files = fs.readdirSync(fragmentsDir).filter((f) => f.endsWith(".md"));
      for (const file of files) {
        pendingFragments.push({
          file,
          content: fs.readFileSync(path.join(fragmentsDir, file), "utf-8"),
        });
      }
    }

    let recentChangelog = "";
    if (fs.existsSync(changelogPath)) {
      const content = fs.readFileSync(changelogPath, "utf-8");
      const sections = content.split(/(?=\n##\s+)/);
      recentChangelog = sections.slice(0, limit + 1).join("\n");
    }

    return {
      pendingFragments,
      releases: recentChangelog,
    };
  },
});
