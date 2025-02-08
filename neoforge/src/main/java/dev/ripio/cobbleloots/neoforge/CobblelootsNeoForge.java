package dev.ripio.cobbleloots.neoforge;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.entity.client.CobblelootsLootBallRenderer;
import dev.ripio.cobbleloots.neoforge.entity.CobblelootsEntities;
import dev.ripio.cobbleloots.neoforge.item.CobblelootsItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(Cobbleloots.MOD_ID)
public final class CobblelootsNeoForge {

    public CobblelootsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Run our common setup.
        Cobbleloots.init();

        // NeoForge setup.
        CobblelootsEntities.register(modEventBus);
        CobblelootsItems.register(modEventBus);
    }

    @EventBusSubscriber(modid = Cobbleloots.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class CobblelootsClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(CobblelootsEntities.LOOT_BALL.get(), CobblelootsLootBallRenderer::new);
        }
    }
}
