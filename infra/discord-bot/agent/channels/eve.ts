import { eveChannel } from "eve/channels/eve";
import { localDev, none } from "eve/channels/auth";

export default eveChannel({
  auth: [localDev(), none()],
  onMessage(ctx) {
    const headers = ctx.eve.request.headers;
    const userId = headers.get("x-discord-user-id") || "anonymous";
    const isAdmin = headers.get("x-discord-is-admin") === "true";

    return {
      auth: {
        principalId: userId,
        principalType: "user",
        authenticator: "discord",
        attributes: {
          isAdmin: isAdmin ? "true" : "false",
        },
      },
    };
  },
});
