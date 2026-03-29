package dev.ripio.cobbleloots.network;

import dev.ripio.cobbleloots.entity.client.CobblelootsLootBallScreen;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class CobblelootsClientNetwork {
    public static void handleLootBallOpenScreen(CobblelootsLootBallOpenScreenPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
            return;

        Entity entity = minecraft.level.getEntity(payload.entityId());
        if (entity instanceof CobblelootsLootBall lootBall) {
            minecraft.setScreen(new CobblelootsLootBallScreen(lootBall, payload.data()));
        }
    }
}
