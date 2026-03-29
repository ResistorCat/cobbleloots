package dev.ripio.cobbleloots.fabric;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.network.CobblelootsLootBallOpenScreenPayload;
import dev.ripio.cobbleloots.network.CobblelootsLootBallUpdatePayload;
import dev.ripio.cobbleloots.network.CobblelootsNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import static dev.ripio.cobbleloots.entity.fabric.CobblelootsEntitiesImpl.registerEntities;
import static dev.ripio.cobbleloots.fabric.data.CobblelootsReloadListeners.registerReloadListeners;
import static dev.ripio.cobbleloots.fabric.event.CobblelootsEvents.registerEvents;
import static dev.ripio.cobbleloots.item.fabric.CobblelootsItemsImpl.registerItems;
import static dev.ripio.cobbleloots.sound.fabric.CobblelootsLootBallSoundsImpl.registerSounds;

public final class CobblelootsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        PayloadTypeRegistry.playS2C().register(CobblelootsLootBallOpenScreenPayload.ID, CobblelootsLootBallOpenScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CobblelootsLootBallUpdatePayload.ID, CobblelootsLootBallUpdatePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CobblelootsLootBallUpdatePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                CobblelootsNetwork.handleLootBallUpdate(payload, context.player());
            });
        });

        Cobbleloots.init();
        registerReloadListeners();
        registerSounds();
        registerEntities();
        registerItems();
        registerEvents();
    }
}
