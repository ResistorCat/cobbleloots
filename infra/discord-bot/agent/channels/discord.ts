import { discordChannel } from "eve/channels/discord";

export default discordChannel({
  onCommand: (_ctx, interaction) => {
    const adminIds = (process.env.DISCORD_ADMIN_IDS || "")
      .split(",")
      .map((id) => id.trim())
      .filter(Boolean);
    const isAdmin = adminIds.includes(interaction.user.id);

    return {
      auth: {
        principalId: interaction.user.id,
        principalType: "user",
        authenticator: "discord",
        attributes: {
          channelId: interaction.channelId,
          guildId: interaction.guildId ?? "",
          isAdmin: isAdmin ? "true" : "false",
        },
      },
    };
  },
});
