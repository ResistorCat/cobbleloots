import { describe, it, expect, vi } from "vitest";
import {
  extractPrompt,
  shouldRespondToMessage,
  formatThreadTranscript,
  splitMessage,
  DEFAULT_GREETING,
  handleDiscordMessage,
} from "../agent/gateway.ts";

describe("Discord Gateway Thread Context & Message Handling", () => {
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

    it("should strip bot mention cleanly with prompt following", () => {
      const prompt = extractPrompt("<@123456789> what are loot balls?", "123456789");
      expect(prompt).toBe("what are loot balls?");
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

  describe("formatThreadTranscript", () => {
    it("formatThreadTranscript should format messages chronologically with author and content", () => {
      const mockMessages = [
        { author: { username: "Alice", id: "1" }, content: "My loot ball vanished" },
        { author: { username: "Bob", id: "2" }, content: "Are you on NeoForge?" },
      ];
      const transcript = formatThreadTranscript(mockMessages as any, "desert-help", BOT_ID);
      expect(transcript).toContain("[Thread History - #desert-help]");
      expect(transcript).toContain("Alice: My loot ball vanished");
      expect(transcript).toContain("Bob: Are you on NeoForge?");
    });

    it("should sort messages chronologically when timestamps are out of order", () => {
      const mockMessages = [
        { author: { username: "Bob", id: "2" }, content: "Second message", createdTimestamp: 2000 },
        { author: { username: "Alice", id: "1" }, content: "First message", createdTimestamp: 1000 },
      ];
      const transcript = formatThreadTranscript(mockMessages as any, "desert-help", BOT_ID);
      const lines = transcript.trim().split("\n");
      expect(lines[1]).toBe("Alice: First message");
      expect(lines[2]).toBe("Bob: Second message");
    });

    it("should strip bot mentions in thread message content when botId is provided", () => {
      const mockMessages = [
        { author: { username: "Charlie", id: "3" }, content: `<@${BOT_ID}> How do I configure drops?` },
      ];
      const transcript = formatThreadTranscript(mockMessages as any, "config-chat", BOT_ID);
      expect(transcript).toContain("Charlie: How do I configure drops?");
      expect(transcript).not.toContain(`<@${BOT_ID}>`);
    });

    it("should format using default header when threadName is omitted", () => {
      const mockMessages = [
        { author: { username: "Dave", id: "4" }, content: "Test content" },
      ];
      const transcript = formatThreadTranscript(mockMessages as any);
      expect(transcript).toContain("[Thread History]");
      expect(transcript).toContain("Dave: Test content");
    });

    it("should return empty string if no valid messages exist", () => {
      const mockMessages = [
        { author: { username: "Eve" }, content: "   " },
      ];
      expect(formatThreadTranscript(mockMessages as any)).toBe("");
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

    it("should split long text on paragraph boundary", () => {
      const longText = "Paragraph 1\n\n" + "A".repeat(1500) + "\n\nParagraph 2\n\n" + "B".repeat(1000);
      const chunks = splitMessage(longText, 1800);
      expect(chunks.length).toBeGreaterThan(1);
      expect(chunks[0].length).toBeLessThanOrEqual(1800);
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

  describe("handleDiscordMessage", () => {
    it("should do nothing if bot client ID is missing", async () => {
      const message = { content: "hello" } as any;
      const client = { user: null } as any;
      const eveClient = {} as any;
      await expect(handleDiscordMessage(message, client, eveClient)).resolves.toBeUndefined();
    });

    it("should reply with DEFAULT_GREETING when message contains only bot mention", async () => {
      const replyFn = vi.fn();
      const message = {
        author: { bot: false, id: "user1" },
        content: `<@${BOT_ID}>`,
        mentions: { has: (id: string) => id === BOT_ID },
        reply: replyFn,
      } as any;
      const client = { user: { id: BOT_ID } } as any;
      const eveClient = {} as any;

      await handleDiscordMessage(message, client, eveClient);
      expect(replyFn).toHaveBeenCalledWith(DEFAULT_GREETING);
    });

    it("should fetch thread history and pass transcript to eveClient session", async () => {
      const replyFn = vi.fn();
      const sendTypingFn = vi.fn().mockResolvedValue(undefined);
      const fetchedMessages = new Map([
        ["msg1", { author: { username: "Alice", id: "1" }, content: "Initial issue", createdTimestamp: 100 }],
        ["msg2", { author: { username: "Bob", id: "2" }, content: "Can you help?", createdTimestamp: 200 }],
      ]);

      const message = {
        author: { bot: false, id: "user1" },
        content: `<@${BOT_ID}> I need help`,
        mentions: { has: (id: string) => id === BOT_ID },
        reply: replyFn,
        channel: {
          isThread: () => true,
          name: "support-123",
          sendTyping: sendTypingFn,
          messages: {
            fetch: vi.fn().mockResolvedValue(fetchedMessages),
          },
        },
      } as any;

      const mockEveClient = {
        sessions: {
          create: vi.fn().mockResolvedValue({
            response: {
              result: vi.fn().mockResolvedValue({
                message: "Here is how to solve the issue.",
                status: "completed",
              }),
            },
          }),
        },
      } as any;

      const client = { user: { id: BOT_ID } } as any;

      await handleDiscordMessage(message, client, mockEveClient);

      expect(mockEveClient.sessions.create).toHaveBeenCalled();
      const callArgs = mockEveClient.sessions.create.mock.calls[0][0];
      expect(callArgs.message).toContain("[Thread History - #support-123]");
      expect(callArgs.message).toContain("Alice: Initial issue");
      expect(callArgs.message).toContain("Bob: Can you help?");
      expect(callArgs.message).toContain("I need help");
      expect(replyFn).toHaveBeenCalledWith("Here is how to solve the issue.");
    });

    it("should prepend replying-to context when message references another message", async () => {
      const replyFn = vi.fn();
      const sendTypingFn = vi.fn().mockResolvedValue(undefined);
      const message = {
        author: { bot: false, id: "user1" },
        content: `<@${BOT_ID}> What does that mean?`,
        mentions: { has: (id: string) => id === BOT_ID },
        reference: { messageId: "ref123" },
        reply: replyFn,
        channel: {
          isThread: () => false,
          sendTyping: sendTypingFn,
          messages: {
            fetch: vi.fn().mockResolvedValue({
              id: "ref123",
              author: { id: "user2", username: "Charlie" },
              content: "You need to edit cobbleloots.json",
            }),
          },
        },
      } as any;

      const mockEveClient = {
        sessions: {
          create: vi.fn().mockResolvedValue({
            response: {
              result: vi.fn().mockResolvedValue({
                message: "It means you configure JSON files.",
                status: "completed",
              }),
            },
          }),
        },
      } as any;

      const client = { user: { id: BOT_ID } } as any;

      await handleDiscordMessage(message, client, mockEveClient);

      expect(mockEveClient.sessions.create).toHaveBeenCalled();
      const callArgs = mockEveClient.sessions.create.mock.calls[0][0];
      expect(callArgs.message).toContain('[Replying to Charlie: "You need to edit cobbleloots.json"]');
      expect(callArgs.message).toContain("What does that mean?");
      expect(replyFn).toHaveBeenCalledWith("It means you configure JSON files.");
    });

    it("should send chunked messages if eveClient response exceeds 1900 chars", async () => {
      const replyFn = vi.fn();
      const sendFn = vi.fn();
      const sendTypingFn = vi.fn().mockResolvedValue(undefined);

      const message = {
        author: { bot: false, id: "user1" },
        content: `<@${BOT_ID}> Tell me everything`,
        mentions: { has: (id: string) => id === BOT_ID },
        reply: replyFn,
        channel: {
          send: sendFn,
          sendTyping: sendTypingFn,
        },
      } as any;

      const longResponse = "Part 1\n\n" + "X".repeat(1500) + "\n\nPart 2\n\n" + "Y".repeat(1000);
      const mockEveClient = {
        sessions: {
          create: vi.fn().mockResolvedValue({
            response: {
              result: vi.fn().mockResolvedValue({
                message: longResponse,
                status: "completed",
              }),
            },
          }),
        },
      } as any;

      const client = { user: { id: BOT_ID } } as any;

      await handleDiscordMessage(message, client, mockEveClient);

      expect(replyFn).toHaveBeenCalled();
      expect(sendFn).toHaveBeenCalled();
    });
  });
});
