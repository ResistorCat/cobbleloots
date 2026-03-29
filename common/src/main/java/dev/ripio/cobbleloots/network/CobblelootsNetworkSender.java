package dev.ripio.cobbleloots.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.level.ServerPlayer;

public class CobblelootsNetworkSender {
    @ExpectPlatform
    public static void sendLootBallOpenScreen(ServerPlayer player, CobblelootsLootBallOpenScreenPayload payload) {
        throw new AssertionError();
    }
}
