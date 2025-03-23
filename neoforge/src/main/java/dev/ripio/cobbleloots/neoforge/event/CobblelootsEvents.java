package dev.ripio.cobbleloots.neoforge.event;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.data.CobblelootsDataProvider;
import dev.ripio.cobbleloots.event.CobblelootsEventManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Cobbleloots.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CobblelootsEvents {
  @SubscribeEvent
  public static void registerReloadListeners(AddReloadListenerEvent event) {
    event.addListener((ResourceManagerReloadListener) CobblelootsDataProvider::onReload);
  }

  @SubscribeEvent
  public static void registerLevelTickListeners(ServerTickEvent.Post event) {
    MinecraftServer server = event.getServer();
    if (event.hasTime()) CobblelootsEventManager.onServerTick(server);
  }

  @SubscribeEvent
  public static void registerServerChunkListeners(ChunkEvent.Load event) {
    if (event.isNewChunk()) {
      ServerLevel level = (ServerLevel) event.getChunk().getLevel();
      LevelChunk chunk = (LevelChunk) event.getChunk();
      CobblelootsEventManager.onChunkGenerate(level, chunk);
    }
  }

}
