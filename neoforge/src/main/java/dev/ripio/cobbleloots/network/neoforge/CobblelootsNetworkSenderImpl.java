package dev.ripio.cobbleloots.network.neoforge;

import dev.ripio.cobbleloots.network.CobblelootsLootBallOpenScreenPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class CobblelootsNetworkSenderImpl {
    public static void sendLootBallOpenScreen(@NotNull ServerPlayer player, @NotNull CobblelootsLootBallOpenScreenPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
