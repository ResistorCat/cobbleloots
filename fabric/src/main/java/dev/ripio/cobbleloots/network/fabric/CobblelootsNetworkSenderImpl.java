package dev.ripio.cobbleloots.network.fabric;

import dev.ripio.cobbleloots.network.CobblelootsLootBallOpenScreenPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class CobblelootsNetworkSenderImpl {
    public static void sendLootBallOpenScreen(ServerPlayer player, CobblelootsLootBallOpenScreenPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
