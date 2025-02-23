package dev.ripio.cobbleloots.fabric.client;

import dev.ripio.cobbleloots.entity.client.CobblelootsLootBallModel;
import dev.ripio.cobbleloots.entity.client.CobblelootsLootBallRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import static dev.ripio.cobbleloots.entity.fabric.CobblelootsEntitiesImpl.getLootBallEntityType;

public final class CobblelootsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        // Register the loot ball renderer
        EntityRendererRegistry.register(
            getLootBallEntityType(),
            CobblelootsLootBallRenderer::new
        );

        // Register the loot ball model layer
        EntityModelLayerRegistry.registerModelLayer(
            CobblelootsLootBallModel.LAYER_LOCATION,
            CobblelootsLootBallModel::createBodyLayer
        );
    }
}
