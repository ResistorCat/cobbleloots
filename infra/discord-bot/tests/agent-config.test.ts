import { describe, it, expect } from "vitest";
import { buildInstructionsPrompt } from "../agent/instructions.ts";
import agentConfig from "../agent/agent.ts";
import discordChannel from "../agent/channels/discord.ts";
import eveChannel from "../agent/channels/eve.ts";
import searchDocs from "../agent/tools/search_docs.ts";
import searchFaqs from "../agent/tools/search_faqs.ts";
import listFaqs from "../agent/tools/list_faqs.ts";
import getFaq from "../agent/tools/get_faq.ts";
import saveFaq from "../agent/tools/save_faq.ts";
import deleteFaq from "../agent/tools/delete_faq.ts";
import inspectCode from "../agent/tools/inspect_code.ts";
import getReleases from "../agent/tools/get_releases.ts";
import askQuestion from "../agent/tools/ask_question.ts";

describe("Agent Configuration & Dynamic Instructions", () => {
  describe("buildInstructionsPrompt", () => {
    it("should enforce English as default and Spanish when requested", () => {
      const prompt = buildInstructionsPrompt({ isAdmin: false });
      expect(prompt).toContain("Default Language: The default and primary language of the bot is English.");
      expect(prompt).toContain("Spanish Adaptability: If the user speaks to you in Spanish");
      expect(prompt).toContain("[Thread History]");
      expect(prompt).toContain("ANTI-RUSH PROTOCOL");
    });

    it("should generate player-facing instructions by default without arguments", () => {
      const prompt = buildInstructionsPrompt();
      expect(prompt).toContain("You are the Cobbleloots Discord Assistant for players and community members.");
      expect(prompt).toContain("search_docs");
      expect(prompt).toContain("search_faqs");
      expect(prompt).toContain("get_releases");
      expect(prompt).not.toContain("Maintainer/Admin Mode");
    });

    it("should grant technical depth and PocketBase FAQ management tools for admins", () => {
      const prompt = buildInstructionsPrompt({ isAdmin: true });
      expect(prompt).toContain("Maintainer/Admin Mode");
      expect(prompt).toContain("manage FAQs in PocketBase using save_faq, list_faqs, get_faq, and delete_faq");
      expect(prompt).toContain("CobblelootsLootBall.java");
      expect(prompt).toContain("inspect_code");
    });
  });

  describe("Agent Definition & Tool Suite", () => {
    it("agent definition should be configured with a model", () => {
      expect(agentConfig).toBeDefined();
      expect((agentConfig as any).model).toBeDefined();
    });

    it("all 9 grounding and PocketBase management tools should be valid tool definitions", () => {
      const tools = [
        searchDocs,
        searchFaqs,
        listFaqs,
        getFaq,
        saveFaq,
        deleteFaq,
        inspectCode,
        getReleases,
        askQuestion,
      ];
      expect(tools).toHaveLength(9);
      for (const tool of tools) {
        expect(tool).toBeDefined();
      }
    });

    it("discord and eve channels should be defined and exported", () => {
      expect(discordChannel).toBeDefined();
      expect(eveChannel).toBeDefined();
    });
  });
});
