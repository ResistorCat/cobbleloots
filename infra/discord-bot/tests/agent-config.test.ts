import { describe, it, expect } from "vitest";
import { buildInstructionsPrompt } from "../agent/instructions.ts";
import agentConfig, { tools } from "../agent/agent.ts";
import discordChannel from "../agent/channels/discord.ts";

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

  describe("Agent Definition & Tool Registration", () => {
    it("agent definition should be configured with a model and instructions", () => {
      expect(agentConfig).toBeDefined();
      expect((agentConfig as any).model).toBeDefined();
      expect((agentConfig as any).instructions).toBeDefined();
    });

    it("agent should register all 9 grounding and PocketBase management tools", () => {
      expect((agentConfig as any).tools).toBeDefined();
      expect((agentConfig as any).tools).toHaveLength(9);
      expect(tools).toHaveLength(9);
    });

    it("discord channel should be defined and exported", () => {
      expect(discordChannel).toBeDefined();
    });
  });
});
