package dev.ripio.cobbleloots.neoforge.data;

import com.google.gson.*;
import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.data.CobblelootsCodecs;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsDataProviderImpl {
    public static final ResourceKey<Registry<LootBall>> LOOT_BALL_REGISTRY_KEY = ResourceKey.createRegistryKey(cobblelootsResource("loot_ball"));

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                LOOT_BALL_REGISTRY_KEY,
                CobblelootsCodecs.LOOT_BALL_CODEC,
                CobblelootsCodecs.LOOT_BALL_CODEC
        );
    }

    public static void registerDataProvider() {
        Cobbleloots.LOGGER.info("Registering data provider");
        NeoForge.EVENT_BUS.register(CobblelootsDataProviderImpl.class);
    }
}
