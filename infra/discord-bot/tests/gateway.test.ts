import { describe, it, expect } from "vitest";
import {
  extractPrompt,
  shouldRespondToMessage,
  splitMessage,
  DEFAULT_GREETING,
} from "../agent/gateway";

describe("Gateway Message Handling", () => {
  const BOT_ID = "123456789012345678";

  describe("extractPrompt", () => {
    it("should remove standard user mention <@ID>", () => {
      const input = `<@${BOT_ID}> ¿Cómo consigo una Master Ball?`;
      expect(extractPrompt(input, BOT_ID)).toBe("¿Cómo consigo una Master Ball?");
    });

    it("should remove nickname user mention <@!ID>", () => {
      const input = `<@!${BOT_ID}> Where do I find Moon Balls?`;
      expect(extractPrompt(input, BOT_ID)).toBe("Where do I find Moon Balls?");
    });

    it("should return empty string if only the bot is mentioned", () => {
      const input = `<@${BOT_ID}>`;
      expect(extractPrompt(input, BOT_ID)).toBe("");
    });

    it("should handle multiple mentions and clean all references to the bot", () => {
      const input = `Hey <@${BOT_ID}> can you tell <@${BOT_ID}> what the config does?`;
      expect(extractPrompt(input, BOT_ID)).toBe("Hey  can you tell  what the config does?");
    });
  });

  describe("shouldRespondToMessage", () => {
    it("should ignore messages from other bots", () => {
      const msg = {
        author: { bot: true },
        mentions: { has: () => true },
      };
      expect(shouldRespondToMessage(msg, BOT_ID)).toBe(false);
    });

    it("should respond if the bot is mentioned", () => {
      const msg = {
        author: { bot: false },
        mentions: { has: (id: string) => id === BOT_ID },
      };
      expect(shouldRespondToMessage(msg, BOT_ID)).toBe(true);
    });

    it("should respond if the message is a reply to the bot", () => {
      const msg = {
        author: { bot: false },
        mentions: { has: () => false },
        reference: { messageId: "987654321" },
      };
      expect(shouldRespondToMessage(msg, BOT_ID, true)).toBe(true);
    });

    it("should not respond to regular messages without mentions or replies", () => {
      const msg = {
        author: { bot: false },
        mentions: { has: () => false },
      };
      expect(shouldRespondToMessage(msg, BOT_ID, false)).toBe(false);
    });
  });

  describe("splitMessage", () => {
    it("should return a single chunk if text is within limit", () => {
      const text = "A short message about Cobbleloots.";
      expect(splitMessage(text, 100)).toEqual([text]);
    });

    it("should split long text at paragraph boundaries (double newlines)", () => {
      const part1 = "First paragraph of documentation.";
      const part2 = "Second paragraph of documentation.";
      const text = `${part1}\n\n${part2}`;
      const chunks = splitMessage(text, part1.length + 5);
      expect(chunks.length).toBe(2);
      expect(chunks[0]).toBe(part1);
      expect(chunks[1]).toBe(part2);
    });

    it("should split at spaces if no newlines exist", () => {
      const text = "one two three four five";
      const chunks = splitMessage(text, 10);
      expect(chunks.length).toBeGreaterThan(1);
      expect(chunks.join(" ")).toBe(text);
    });
  });

  describe("DEFAULT_GREETING", () => {
    it("should provide a friendly English greeting mentioning Cobbleloots", () => {
      expect(DEFAULT_GREETING).toContain("Cobbleloots");
      expect(DEFAULT_GREETING).toContain("mechanics");
      expect(DEFAULT_GREETING).toContain("loot balls");
    });
  });
});
