package dev.ripio.cobbleloots.fabric.client;

import dev.ripio.cobbleloots.entity.client.CobblelootsLootBallModel;
import dev.ripio.cobbleloots.entity.client.CobblelootsLootBallRenderer;
import dev.ripio.cobbleloots.fabric.entity.CobblelootsEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class CobblelootsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        // Register the loot ball renderer
        EntityRendererRegistry.register(
            CobblelootsEntities.LOOT_BALL,
            CobblelootsLootBallRenderer::new
        );

        // Register the loot ball model layer
        EntityModelLayerRegistry.registerModelLayer(
            CobblelootsLootBallModel.LAYER_LOCATION,
            CobblelootsLootBallModel::createBodyLayer
        );
    }
}
