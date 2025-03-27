package dev.ripio.cobbleloots.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.LevelChunk;

import static dev.ripio.cobbleloots.config.CobblelootsConfig.*;
import static dev.ripio.cobbleloots.event.custom.CobblelootsLootBallEvents.generateLootBallOnChunk;
import static dev.ripio.cobbleloots.event.custom.CobblelootsLootBallEvents.spawnLootBallNearRandomPlayer;

public class CobblelootsEventManager {
  private static long lootBallSpawnTick = 0L;
  private static final RandomSource randomSource = RandomSource.create();

  public static void onChunkGenerate(ServerLevel level, LevelChunk levelChunk) {
    // If loot ball generation is enabled, try to generate a loot ball in the chunk
    if (getBooleanConfig(LOOT_BALL_GENERATION_ENABLED)) {
      int count = 0;
      for (int i = 0; i < getIntConfig(LOOT_BALL_GENERATION_ATTEMPTS); i++) {
        if (count >= getIntConfig(LOOT_BALL_GENERATION_CHUNK_CAP)) break;
        if (generateLootBallOnChunk(level, levelChunk, randomSource)) count++;
      }
    }
  }

  public static void onServerTick(MinecraftServer server) {
    // Initialize the loot ball spawn tick
    if (lootBallSpawnTick == 0) lootBallSpawnTick = server.getTickCount() + getLootBallCooldown();

    // Every lootBallSpawnCooldown ticks, try to spawn a loot ball
    if (server.getTickCount() > lootBallSpawnTick && getBooleanConfig(LOOT_BALL_SPAWNING_ENABLED)) {
      spawnLootBallNearRandomPlayer(server, randomSource);
      // Reset the cooldown to a new random value
      lootBallSpawnTick = server.getTickCount() + getLootBallCooldown();
    }
  }

  private static long getLootBallCooldown() {
    return CobblelootsEventManager.randomSource.nextIntBetweenInclusive(getIntConfig(LOOT_BALL_SPAWNING_COOLDOWN_MIN), getIntConfig(LOOT_BALL_SPAWNING_COOLDOWN_MAX));
  }


}
