package dev.ripio.cobbleloots.network.fabric;

import dev.ripio.cobbleloots.network.CobblelootsLootBallUpdatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CobblelootsClientNetworkSenderImpl {
    public static void sendLootBallUpdate(CobblelootsLootBallUpdatePayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
