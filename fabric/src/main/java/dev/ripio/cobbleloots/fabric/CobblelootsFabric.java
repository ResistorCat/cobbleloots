package dev.ripio.cobbleloots.fabric;

import dev.ripio.cobbleloots.Cobbleloots;
import net.fabricmc.api.ModInitializer;

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
        Cobbleloots.init();
        registerReloadListeners();
        registerSounds();
        registerEntities();
        registerItems();
        registerEvents();
    }
}
