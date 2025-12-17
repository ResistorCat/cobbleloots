package dev.ripio.cobbleloots.neoforge;

import com.cobblemon.mod.common.client.render.item.CobblemonBuiltinItemRendererRegistry;
import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.entity.client.CobblelootsLootBallRenderer;
import dev.ripio.cobbleloots.item.client.CobblelootsLootBallItemRenderer;
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
import static dev.ripio.cobbleloots.item.neoforge.CobblelootsItemsImpl.getLootBallItem;
import static dev.ripio.cobbleloots.item.neoforge.CobblelootsItemsImpl.registerItems;
import static dev.ripio.cobbleloots.sound.neoforge.CobblelootsLootBallSoundsImpl.registerSounds;

import eu.midnightdust.lib.config.MidnightConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Cobbleloots.MOD_ID)
public final class CobblelootsNeoForge {

    public CobblelootsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Run our common setup.
        Cobbleloots.init();

        // NeoForge setup.
        registerSounds(modEventBus);
        registerEntities(modEventBus);
        registerItems(modEventBus);

        // Register config screen
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, screen) -> MidnightConfig.getScreen(screen, Cobbleloots.MOD_ID));
    }

    @EventBusSubscriber(modid = Cobbleloots.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class CobblelootsClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            Cobbleloots.LOGGER.info("Registering entity renderers");
            EntityRenderers.register(getLootBallEntityType(), CobblelootsLootBallRenderer::new);
            Cobbleloots.LOGGER.info("Registering item renderers");
            CobblemonBuiltinItemRendererRegistry.INSTANCE.register(getLootBallItem(),
                    CobblelootsLootBallItemRenderer::renderLootBallItem);
        }
    }
}
