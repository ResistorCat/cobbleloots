import { describe, it, expect, beforeEach, afterEach } from "vitest";
import searchDocsTool from "../agent/tools/search_docs";
import inspectCodeTool from "../agent/tools/inspect_code";
import getReleasesTool from "../agent/tools/get_releases";
import searchFaqsTool from "../agent/tools/search_faqs";
import { getDb } from "../agent/lib/db";
import { initFaqSchema, insertFaq } from "../agent/lib/faq-store";
import {
  getReleases,
  getLatestReleaseTag,
  fetchRepoFile,
  searchDocsRemote,
  clearGithubCache,
} from "../agent/lib/github-client";

describe("Grounding Tools", () => {
  const origDocsPath = process.env.REPO_DOCS_PATH;

  beforeEach(() => {
    clearGithubCache();
  });

  afterEach(() => {
    process.env.REPO_DOCS_PATH = origDocsPath;
  });

  it("search_docs should find content from local docs when present", async () => {
    const result = await (searchDocsTool as any).execute({ query: "loot" }, {} as any);
    expect(result.matches).toBeDefined();
    expect(Array.isArray(result.matches)).toBe(true);
    expect(result.matches.length).toBeGreaterThan(0);
  });

  it("search_docs should fall back to remote search index when local docs directory is absent", async () => {
    process.env.REPO_DOCS_PATH = "/tmp/non-existent-docs-dir-test";
    const result = await (searchDocsTool as any).execute({ query: "loot" }, {} as any);
    expect(result.matches).toBeDefined();
    expect(Array.isArray(result.matches)).toBe(true);
    expect(result.matches.length).toBeGreaterThan(0);
  });

  it("inspect_code should inspect valid file under common/", async () => {
    const result = await (inspectCodeTool as any).execute(
      { filePath: "common/src/main/resources/pack.mcmeta" },
      {} as any
    );
    expect(result.error).toBeUndefined();
    expect(result.filePath).toBe("common/src/main/resources/pack.mcmeta");
    expect(result.content).toContain("Cobbleloots");
  });

  it("inspect_code should reject paths outside allowed directories", async () => {
    const result = await (inspectCodeTool as any).execute({ filePath: "../../../package.json" }, {} as any);
    expect(result.error).toBeDefined();
    expect(result.error).toContain("Access denied");
  });

  it("inspect_code should reject paths with partial directory names like common-secrets", async () => {
    const result = await (inspectCodeTool as any).execute({ filePath: "common-secrets/passwords.txt" }, {} as any);
    expect(result.error).toBeDefined();
    expect(result.error).toContain("Access denied");
  });

  it("get_releases should return recent releases", async () => {
    const result = await (getReleasesTool as any).execute({ limit: 2 }, {} as any);
    expect(result.releases).toBeDefined();
    expect(typeof result.releases).toBe("string");
    expect(result.releases.length).toBeGreaterThan(0);
  });

  it("search_faqs should return matches from database", async () => {
    const db = getDb(":memory:");
    initFaqSchema(db);
    insertFaq(db, { question: "Can I fish loot balls?", answer: "Yes with fishing config" });

    const result = await (searchFaqsTool as any).execute({ query: "fish", dbInstance: db }, {} as any);
    expect(result.results.length).toBeGreaterThan(0);
    expect(result.results[0].question).toContain("fish");
  });

  it("github-client should fetch releases and latest tag", async () => {
    const releases = await getReleases(3);
    expect(releases).toBeDefined();
    expect(Array.isArray(releases)).toBe(true);
    expect(releases.length).toBeGreaterThan(0);

    const latestTag = await getLatestReleaseTag();
    expect(latestTag).toBeDefined();
    expect(typeof latestTag).toBe("string");
    expect(latestTag.length).toBeGreaterThan(0);
  });

  it("github-client should fetch file content and search remote docs", async () => {
    const content = await fetchRepoFile("common/src/main/resources/pack.mcmeta");
    expect(content).toBeDefined();
    expect(content).toContain("Cobbleloots");

    const remoteDocs = await searchDocsRemote("loot", 2);
    expect(remoteDocs).toBeDefined();
    expect(Array.isArray(remoteDocs)).toBe(true);
    expect(remoteDocs.length).toBeGreaterThan(0);
  });
});
