import { Client, GatewayIntentBits, Partials, ActivityType, Events, type Message } from "discord.js";
import { Client as EveClient } from "eve/client";
import { isAuthorizedAdmin } from "./lib/auth-utils.ts";

export const DEFAULT_GREETING =
  "Hello! I am the official Cobbleloots assistant. You can ask me about mechanics, commands, loot balls, or mod configuration. How can I help you today?";

export function extractPrompt(content: string, botId: string): string {
  const mentionPattern = new RegExp(`<@!?${botId}>`, "g");
  return content.replace(mentionPattern, "").trim();
}

export function shouldRespondToMessage(
  message: {
    author: { bot: boolean };
    mentions: { has: (id: string) => boolean };
    reference?: { messageId?: string | null } | null;
  },
  botId: string,
  isReplyToBot = false
): boolean {
  if (message.author.bot) return false;
  if (message.mentions.has(botId)) return true;
  if (isReplyToBot && message.reference?.messageId) return true;
  return false;
}

export interface TranscriptMessage {
  id?: string;
  createdTimestamp?: number;
  createdAt?: Date | string | number;
  author?: {
    id?: string;
    username?: string;
    displayName?: string;
    globalName?: string;
  };
  content?: string;
}

export function formatThreadTranscript(
  messages: Array<TranscriptMessage> | Iterable<TranscriptMessage>,
  threadName?: string,
  botId?: string
): string {
  const msgList: TranscriptMessage[] = Array.isArray(messages)
    ? messages
    : "values" in (messages as any) && typeof (messages as any).values === "function"
    ? Array.from((messages as any).values())
    : Array.from(messages as any);

  // Sort chronologically if timestamps are provided
  const sorted = [...msgList].sort((a, b) => {
    const timeA = a.createdTimestamp ?? (a.createdAt ? new Date(a.createdAt).getTime() : 0);
    const timeB = b.createdTimestamp ?? (b.createdAt ? new Date(b.createdAt).getTime() : 0);
    if (timeA && timeB && timeA !== timeB) return timeA - timeB;
    return 0;
  });

  const cleanMessages = sorted
    .filter((m) => m && m.content && m.content.trim().length > 0)
    .map((m) => {
      const authorName = m.author?.displayName || m.author?.globalName || m.author?.username || "Unknown";
      const cleanContent = botId ? extractPrompt(m.content!, botId) : m.content!.trim();
      return `${authorName}: ${cleanContent}`;
    })
    .filter((line) => !line.endsWith(": "));

  if (cleanMessages.length === 0) return "";
  const header = threadName ? `[Thread History - #${threadName}]` : "[Thread History]";
  return `${header}\n${cleanMessages.join("\n")}\n\n`;
}

export function splitMessage(text: string, maxLength = 1900): string[] {
  if (text.length <= maxLength) return [text];
  const chunks: string[] = [];
  let remaining = text;

  while (remaining.length > maxLength) {
    let splitIdx = remaining.lastIndexOf("\n\n", maxLength);
    if (splitIdx <= 0) splitIdx = remaining.lastIndexOf("\n", maxLength);
    if (splitIdx <= 0) splitIdx = remaining.lastIndexOf(" ", maxLength);
    if (splitIdx <= 0) splitIdx = maxLength;

    chunks.push(remaining.slice(0, splitIdx).trimEnd());
    remaining = remaining.slice(splitIdx).trimStart();
  }

  if (remaining.length > 0) {
    chunks.push(remaining);
  }

  return chunks;
}

export interface GatewayOptions {
  client?: Client;
  token?: string;
  port?: string | number;
}

export async function handleDiscordMessage(
  message: Message,
  client: Client,
  eveClient: EveClient
): Promise<void> {
  const botId = client.user?.id;
  if (!botId) return;

  // Check if replying directly to a bot message or another message
  let isReplyToBot = false;
  let replyContext = "";
  if (message.reference?.messageId) {
    try {
      const referencedMessage = await message.channel.messages.fetch(message.reference.messageId);
      isReplyToBot = referencedMessage.author.id === botId;
      if (referencedMessage.content) {
        const authorName =
          referencedMessage.author.displayName ||
          referencedMessage.author.globalName ||
          referencedMessage.author.username ||
          "User";
        replyContext = `[Replying to ${authorName}: "${referencedMessage.content}"]\n\n`;
      }
    } catch {
      // Ignored if inaccessible or deleted
    }
  }

  if (!shouldRespondToMessage(message, botId, isReplyToBot)) return;

  const rawPrompt = extractPrompt(message.content, botId);
  if (!rawPrompt) {
    await message.reply(DEFAULT_GREETING);
    return;
  }

  // Extract thread context if inside a Discord thread
  let threadContext = "";
  if ("isThread" in message.channel && typeof message.channel.isThread === "function" && message.channel.isThread()) {
    try {
      const fetched = await message.channel.messages.fetch({ limit: 20 });
      const threadName = "name" in message.channel ? (message.channel.name as string) : undefined;
      threadContext = formatThreadTranscript(fetched.values() as any, threadName, botId);
    } catch (err) {
      console.warn("[Gateway] Could not fetch thread context:", (err as Error).message);
    }
  }

  const finalPrompt = `${threadContext}${replyContext}${rawPrompt}`;

  // Keep typing indicator active
  const sendTyping = () => {
    if ("sendTyping" in message.channel && typeof message.channel.sendTyping === "function") {
      message.channel.sendTyping().catch(() => {});
    }
  };

  sendTyping();
  const typingInterval = setInterval(sendTyping, 7000);

  try {
    const isAdmin = isAuthorizedAdmin(message.author.id);
    const { response } = await eveClient.sessions.create({
      message: finalPrompt,
      headers: {
        "x-discord-user-id": message.author.id,
        "x-discord-is-admin": isAdmin ? "true" : "false",
      },
    });

    const result = await response.result();
    clearInterval(typingInterval);

    const answer = result.message;
    if (!answer || result.status === "failed") {
      await message.reply("Sorry, an error occurred while processing your request. Please try again.");
      return;
    }

    const chunks = splitMessage(answer);
    await message.reply(chunks[0]);
    if (chunks.length > 1 && "send" in message.channel && typeof message.channel.send === "function") {
      for (let i = 1; i < chunks.length; i++) {
        await message.channel.send(chunks[i]);
      }
    }
  } catch (error) {
    clearInterval(typingInterval);
    console.error("Error processing Discord message:", error);
    await message.reply("An error occurred while communicating with the Cobbleloots assistant. Please try again later.");
  }
}

export async function startGateway(options: GatewayOptions = {}): Promise<Client | null> {
  const token = options.token || process.env.DISCORD_BOT_TOKEN;
  if (!token) {
    console.warn("DISCORD_BOT_TOKEN not provided, skipping Discord Gateway listener.");
    return null;
  }

  const port = options.port || process.env.PORT || 3000;
  const eveClient = new EveClient({
    host: `http://127.0.0.1:${port}`,
  });

  const client =
    options.client ||
    new Client({
      intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.MessageContent,
      ],
      partials: [Partials.Channel, Partials.Message],
    });

  client.on(Events.ClientReady, () => {
    console.log(`[Gateway] Discord Gateway connected as ${client.user?.tag}!`);
    client.user?.setActivity("Cobbleloots | Tag me to ask!", {
      type: ActivityType.Custom,
    });
  });

  client.on(Events.MessageCreate, async (message) => {
    await handleDiscordMessage(message, client, eveClient);
  });

  await client.login(token);
  return client;
}
