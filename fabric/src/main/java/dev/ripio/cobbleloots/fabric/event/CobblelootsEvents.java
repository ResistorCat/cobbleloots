package dev.ripio.cobbleloots.fabric.event;

import dev.ripio.cobbleloots.event.CobblelootsEventManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class CobblelootsEvents {
  public static void registerEvents() {
    ServerTickEvents.END_SERVER_TICK.register(CobblelootsEventManager::onServerTick);
    ServerChunkEvents.CHUNK_GENERATE.register(CobblelootsEventManager::onChunkGenerate);
  }
}
