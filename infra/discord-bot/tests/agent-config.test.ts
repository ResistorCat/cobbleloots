import { describe, it, expect } from "vitest";
import { buildInstructionsPrompt } from "../agent/instructions";
import agentConfig from "../agent/agent";
import discordChannel from "../agent/channels/discord";

describe("Agent Configuration & Instructions", () => {
  it("should generate friendly gameplay prompt for standard player", () => {
    const prompt = buildInstructionsPrompt({ isAdmin: false });
    expect(prompt).toContain("player-facing");
    expect(prompt).not.toContain("Include Java class names");
  });

  it("should generate friendly gameplay prompt by default without arguments", () => {
    const prompt = buildInstructionsPrompt();
    expect(prompt).toContain("player-facing");
  });

  it("should generate technical maintainer prompt for admin", () => {
    const prompt = buildInstructionsPrompt({ isAdmin: true });
    expect(prompt).toContain("Maintainer/Admin Mode");
    expect(prompt).toContain("CobblelootsLootBall.java");
  });

  it("agent definition should be configured with a model", () => {
    expect(agentConfig).toBeDefined();
  });

  it("discord channel should be defined and exported", () => {
    expect(discordChannel).toBeDefined();
  });
});
