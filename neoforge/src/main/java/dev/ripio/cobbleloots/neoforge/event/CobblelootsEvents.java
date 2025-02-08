package dev.ripio.cobbleloots.neoforge.event;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.data.CobblelootsDataProvider;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = Cobbleloots.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CobblelootsEvents {
  @SubscribeEvent
  public static void registerReloadListeners(AddReloadListenerEvent event) {
    event.addListener((ResourceManagerReloadListener) CobblelootsDataProvider::onReload);
  }
}
