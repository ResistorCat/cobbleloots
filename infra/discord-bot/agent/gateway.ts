import {
  Client,
  GatewayIntentBits,
  Partials,
  ActivityType,
  Events,
  ActionRowBuilder,
  ButtonBuilder,
  ButtonStyle,
  type Message,
  type ButtonInteraction,
} from "discord.js";
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
  messages: Iterable<TranscriptMessage>,
  threadName?: string,
  botId?: string
): string {
  const arr = Array.from(messages);
  if (arr.length === 0) return "";

  // Sort chronologically ascending
  arr.sort((a, b) => {
    const timeA = a.createdTimestamp || (a.createdAt ? new Date(a.createdAt).getTime() : 0);
    const timeB = b.createdTimestamp || (b.createdAt ? new Date(b.createdAt).getTime() : 0);
    return timeA - timeB;
  });

  const lines: string[] = [];
  if (threadName) {
    lines.push(`[Thread History - #${threadName}]`);
  } else {
    lines.push("[Thread History]");
  }

  for (const m of arr) {
    if (!m.content) continue;
    const authorName =
      m.author?.displayName ||
      m.author?.globalName ||
      m.author?.username ||
      "User";
    const cleanContent = (botId ? extractPrompt(m.content, botId) : m.content).trim();
    if (cleanContent) {
      lines.push(`${authorName}: ${cleanContent}`);
    }
  }

  return lines.length > 1 ? lines.join("\n") + "\n\n" : "";
}

export function splitMessage(text: string, maxLength = 1900): string[] {
  if (text.length <= maxLength) return [text];

  const chunks: string[] = [];
  let remaining = text;

  while (remaining.length > maxLength) {
    let splitIndex = remaining.lastIndexOf("\n\n", maxLength);
    if (splitIndex === -1 || splitIndex < maxLength / 2) {
      splitIndex = remaining.lastIndexOf("\n", maxLength);
    }
    if (splitIndex === -1 || splitIndex < maxLength / 2) {
      splitIndex = remaining.lastIndexOf(" ", maxLength);
    }
    if (splitIndex === -1) {
      splitIndex = maxLength;
    }

    chunks.push(remaining.substring(0, splitIndex).trim());
    remaining = remaining.substring(splitIndex).trim();
  }

  if (remaining.length > 0) {
    chunks.push(remaining);
  }

  return chunks;
}

export function buildApprovalButtons(
  sessionId: string,
  requestId: string,
  options?: Array<{ id: string; label?: string; style?: string }>
): ActionRowBuilder<ButtonBuilder> {
  const row = new ActionRowBuilder<ButtonBuilder>();
  const opts =
    options && options.length > 0
      ? options
      : [
          { id: "approve", label: "Approve", style: "primary" },
          { id: "cancel", label: "Cancel", style: "danger" },
        ];

  for (const opt of opts) {
    const style =
      opt.id === "approve"
        ? ButtonStyle.Success
        : opt.id === "cancel" || opt.style === "danger"
        ? ButtonStyle.Danger
        : ButtonStyle.Primary;

    row.addComponents(
      new ButtonBuilder()
        .setCustomId(`eve:${sessionId}:${requestId}:${opt.id}`)
        .setLabel(opt.label || opt.id)
        .setStyle(style)
    );
  }

  return row;
}

export async function handleButtonInteraction(
  interaction: ButtonInteraction,
  eveClient: EveClient
): Promise<void> {
  if (!interaction.customId?.startsWith("eve:")) return;

  const parts = interaction.customId.split(":");
  const sessionId = parts[1];
  const requestId = parts[2];
  const optionId = parts[3];

  if (!sessionId || !requestId || !optionId) return;

  // Authorization check: Only authorized admins can approve
  if (!isAuthorizedAdmin(interaction.user.id)) {
    await interaction.reply({
      content: "❌ Unauthorized: Only server administrators can approve this action.",
      ephemeral: true,
    });
    return;
  }

  await interaction.deferUpdate();

  try {
    const session = eveClient.sessions.attach(sessionId);
    const messageResponse = await session.respond([
      { requestId, optionId },
    ]);
    const nextResult = await messageResponse.result();

    const outcome =
      nextResult.message ||
      (optionId === "approve"
        ? "✅ Action approved and executed successfully."
        : "❌ Action cancelled by administrator.");

    await interaction.editReply({
      content: `${interaction.message.content}\n\n**Decision Result:**\n${outcome}`,
      components: [],
    });
  } catch (err) {
    console.error("[Gateway] Error processing approval decision:", err);
    await interaction.followUp({
      content: `Error processing decision: ${(err as Error).message}`,
      ephemeral: true,
    });
  }
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

    // Check for Human-In-The-Loop approval or input requests
    if (result.status === "waiting" && result.inputRequests && result.inputRequests.length > 0) {
      const req = result.inputRequests[0];
      const row = buildApprovalButtons(result.sessionId, req.requestId, req.options);
      const promptText =
        req.prompt ||
        `⚠️ **Approval Required:** The assistant is requesting authorization to execute **${(req as any).action?.name || "an action"}**.`;

      await message.reply({
        content: promptText,
        components: [row],
      });
      return;
    }

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

  client.on(Events.InteractionCreate, async (interaction) => {
    if (interaction.isButton()) {
      await handleButtonInteraction(interaction, eveClient);
    }
  });

  await client.login(token);
  return client;
}
