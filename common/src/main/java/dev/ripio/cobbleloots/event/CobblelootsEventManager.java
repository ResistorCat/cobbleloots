package dev.ripio.cobbleloots.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.LevelChunk;

import dev.ripio.cobbleloots.config.CobblelootsConfig;
import static dev.ripio.cobbleloots.event.custom.CobblelootsLootBallEvents.generateLootBallOnChunk;
import static dev.ripio.cobbleloots.event.custom.CobblelootsLootBallEvents.spawnLootBallNearRandomPlayer;

public class CobblelootsEventManager {
  private static long lootBallSpawnTick = 0L;
  private static final RandomSource randomSource = RandomSource.create();

  public static void onChunkGenerate(ServerLevel level, LevelChunk levelChunk) {
    // If loot ball generation is enabled, try to generate a loot ball in the chunk
    if (CobblelootsConfig.generation_enabled) {
      int count = 0;
      int attempts = CobblelootsConfig.generation_attempts;
      int chunkCap = CobblelootsConfig.generation_chunk_cap;
      for (int i = 0; i < attempts; i++) {
        if (count >= chunkCap)
          break;
        if (generateLootBallOnChunk(level, levelChunk, randomSource))
          count++;
      }
    }
  }

  public static void onServerTick(MinecraftServer server) {
    // Initialize the loot ball spawn tick
    if (lootBallSpawnTick == 0)
      lootBallSpawnTick = server.getTickCount() + getLootBallCooldown();

    // Every lootBallSpawnCooldown ticks, try to spawn a loot ball
    if (server.getTickCount() > lootBallSpawnTick && CobblelootsConfig.spawning_enabled) {
      spawnLootBallNearRandomPlayer(server, randomSource);
      // Reset the cooldown to a new random value
      lootBallSpawnTick = server.getTickCount() + getLootBallCooldown();
    }
  }

  private static long getLootBallCooldown() {
    int minCooldown = CobblelootsConfig.spawning_cooldown_min;
    int maxCooldown = CobblelootsConfig.spawning_cooldown_max;
    return CobblelootsEventManager.randomSource.nextIntBetweenInclusive(minCooldown, maxCooldown);
  }

}
