import { discordChannel } from "eve/channels/discord";
import { isAuthorizedAdmin } from "../lib/auth-utils";

export default discordChannel({
  onCommand: (_ctx, interaction) => {
    const isAdmin = isAuthorizedAdmin(interaction.user.id);

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
