import fs from "node:fs";
import path from "node:path";

export interface ReleaseInfo {
  tagName: string;
  name: string;
  body: string;
  publishedAt: string;
  htmlUrl: string;
  isPrerelease: boolean;
}

export interface DocsIndexItem {
  location: string;
  title: string;
  text: string;
}

const DEFAULT_REPO = "ResistorCat/cobbleloots";
const DEFAULT_DOCS_INDEX_URL = "https://resistorcat.github.io/cobbleloots/search/search_index.json";

let cachedReleases: { data: ReleaseInfo[]; expiresAt: number } | null = null;
const fileContentCache = new Map<string, { content: string | null; expiresAt: number }>();
let cachedDocsIndex: { data: DocsIndexItem[]; expiresAt: number } | null = null;

export function clearGithubCache(): void {
  cachedReleases = null;
  fileContentCache.clear();
  cachedDocsIndex = null;
}

export function getRepoName(): string {
  return process.env.GITHUB_REPO || DEFAULT_REPO;
}

export function getLocalRepoRoot(): string | null {
  if (process.env.REPO_ROOT && fs.existsSync(process.env.REPO_ROOT)) {
    return process.env.REPO_ROOT;
  }
  const candidate = path.resolve(process.cwd(), "../..");
  if (fs.existsSync(path.join(candidate, "common")) || fs.existsSync(path.join(candidate, "CHANGELOG.md"))) {
    return candidate;
  }
  const current = path.resolve(process.cwd());
  if (fs.existsSync(path.join(current, "common")) || fs.existsSync(path.join(current, "CHANGELOG.md"))) {
    return current;
  }
  return null;
}

function getGithubHeaders(accept = "application/vnd.github+json"): Record<string, string> {
  const headers: Record<string, string> = {
    "User-Agent": "Cobbleloots-Discord-Bot",
    Accept: accept,
  };
  if (process.env.GITHUB_TOKEN) {
    headers["Authorization"] = `Bearer ${process.env.GITHUB_TOKEN}`;
  }
  return headers;
}

function getFallbackLocalReleases(limit: number): ReleaseInfo[] {
  const repoRoot = getLocalRepoRoot();
  if (!repoRoot) return [];

  const changelogPath = path.join(repoRoot, "CHANGELOG.md");
  if (!fs.existsSync(changelogPath)) return [];

  const content = fs.readFileSync(changelogPath, "utf-8");
  const sections = content.split(/(?=\n##\s+)/).filter((s) => s.trim().startsWith("##"));

  return sections.slice(0, limit).map((sec) => {
    const firstLine = sec.trim().split("\n")[0] || "";
    const tagName = firstLine.replace(/^##\s*/, "").trim();
    return {
      tagName,
      name: tagName,
      body: sec.trim(),
      publishedAt: "",
      htmlUrl: "",
      isPrerelease: tagName.includes("alpha") || tagName.includes("beta"),
    };
  });
}

export async function getReleases(limit = 5): Promise<ReleaseInfo[]> {
  const now = Date.now();
  if (cachedReleases && cachedReleases.expiresAt > now) {
    return cachedReleases.data.slice(0, limit);
  }

  try {
    const repo = getRepoName();
    const res = await fetch(`https://api.github.com/repos/${repo}/releases?per_page=10`, {
      headers: getGithubHeaders(),
    });

    if (!res.ok) {
      return getFallbackLocalReleases(limit);
    }

    const items = (await res.json()) as any[];
    if (!Array.isArray(items)) {
      return getFallbackLocalReleases(limit);
    }

    const releases: ReleaseInfo[] = items.map((r) => ({
      tagName: r.tag_name || "",
      name: r.name || r.tag_name || "Cobbleloots Release",
      body: r.body || "",
      publishedAt: r.published_at || "",
      htmlUrl: r.html_url || "",
      isPrerelease: Boolean(r.prerelease),
    }));

    cachedReleases = {
      data: releases,
      expiresAt: now + 15 * 60 * 1000,
    };

    return releases.slice(0, limit);
  } catch {
    return getFallbackLocalReleases(limit);
  }
}

export async function getLatestReleaseTag(): Promise<string> {
  const releases = await getReleases(1);
  if (releases.length > 0 && releases[0].tagName) {
    return releases[0].tagName;
  }
  return "main";
}

export async function fetchRepoFile(filePath: string, ref?: string): Promise<string | null> {
  const normalized = filePath.replace(/\\/g, "/").replace(/^\/+/, "");
  const targetRef = ref || (await getLatestReleaseTag());
  const cacheKey = `${normalized}@${targetRef}`;
  const now = Date.now();

  const cached = fileContentCache.get(cacheKey);
  if (cached && cached.expiresAt > now) {
    return cached.content;
  }

  // 1. Check local file if in monorepo
  const repoRoot = getLocalRepoRoot();
  if (repoRoot) {
    const localPath = path.resolve(repoRoot, normalized);
    if (fs.existsSync(localPath) && !fs.statSync(localPath).isDirectory()) {
      const content = fs.readFileSync(localPath, "utf-8");
      fileContentCache.set(cacheKey, { content, expiresAt: now + 30 * 60 * 1000 });
      return content;
    }
  }

  // 2. Fetch from GitHub REST API
  try {
    const repo = getRepoName();
    const url = `https://api.github.com/repos/${repo}/contents/${normalized}?ref=${encodeURIComponent(targetRef)}`;

    const res = await fetch(url, {
      headers: getGithubHeaders("application/vnd.github.raw"),
    });

    if (!res.ok) {
      fileContentCache.set(cacheKey, { content: null, expiresAt: now + 5 * 60 * 1000 });
      return null;
    }

    const content = await res.text();
    fileContentCache.set(cacheKey, { content, expiresAt: now + 30 * 60 * 1000 });
    return content;
  } catch {
    return null;
  }
}

export async function searchDocsRemote(query: string, limit = 5): Promise<Array<{ file: string; excerpt: string }>> {
  const now = Date.now();
  let index: DocsIndexItem[] | null = null;

  if (cachedDocsIndex && cachedDocsIndex.expiresAt > now) {
    index = cachedDocsIndex.data;
  } else {
    try {
      const res = await fetch(DEFAULT_DOCS_INDEX_URL);
      if (res.ok) {
        const json = (await res.json()) as any;
        if (json && Array.isArray(json.docs)) {
          index = json.docs as DocsIndexItem[];
          cachedDocsIndex = { data: index, expiresAt: now + 30 * 60 * 1000 };
        }
      }
    } catch {
      index = null;
    }
  }

  if (!index || index.length === 0) {
    return [];
  }

  const queryLower = query.toLowerCase();
  const results: Array<{ file: string; excerpt: string }> = [];

  for (const item of index) {
    const textClean = (item.text || "").replace(/<[^>]*>/g, " ").replace(/\s+/g, " ");
    const titleMatches = (item.title || "").toLowerCase().includes(queryLower);
    const textMatches = textClean.toLowerCase().includes(queryLower);

    if (!titleMatches && !textMatches) continue;

    let excerpt = textClean.slice(0, 250);
    if (textMatches) {
      const idx = textClean.toLowerCase().indexOf(queryLower);
      const start = Math.max(0, idx - 40);
      const end = Math.min(textClean.length, idx + 160);
      excerpt = (start > 0 ? "..." : "") + textClean.slice(start, end) + (end < textClean.length ? "..." : "");
    }

    const file = item.location ? item.location : item.title;
    results.push({ file, excerpt });
    if (results.length >= limit) break;
  }

  return results;
}
