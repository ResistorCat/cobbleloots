package dev.ripio.cobbleloots.network.neoforge;

import dev.ripio.cobbleloots.network.CobblelootsLootBallUpdatePayload;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class CobblelootsClientNetworkSenderImpl {
    public static void sendLootBallUpdate(@NotNull CobblelootsLootBallUpdatePayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
