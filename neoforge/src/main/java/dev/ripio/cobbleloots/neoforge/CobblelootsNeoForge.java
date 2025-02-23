package dev.ripio.cobbleloots.neoforge;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.entity.client.CobblelootsLootBallRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static dev.ripio.cobbleloots.entity.neoforge.CobblelootsEntitiesImpl.getLootBallEntityType;
import static dev.ripio.cobbleloots.entity.neoforge.CobblelootsEntitiesImpl.registerEntities;
import static dev.ripio.cobbleloots.item.neoforge.CobblelootsItemsImpl.registerItems;
import static dev.ripio.cobbleloots.sound.neoforge.CobblelootsLootBallSoundsImpl.registerSounds;

@Mod(Cobbleloots.MOD_ID)
public final class CobblelootsNeoForge {

    public CobblelootsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Run our common setup.
        Cobbleloots.init();

        // NeoForge setup.
        registerSounds(modEventBus);
        registerEntities(modEventBus);
        registerItems(modEventBus);
    }

    @EventBusSubscriber(modid = Cobbleloots.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class CobblelootsClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(getLootBallEntityType(), CobblelootsLootBallRenderer::new);
        }
    }
}
