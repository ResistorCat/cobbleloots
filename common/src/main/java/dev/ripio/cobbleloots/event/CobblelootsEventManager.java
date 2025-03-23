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
    if (LOOT_BALL_GENERATION_ENABLED.getValue()) {
      for (int i = 0; i < LOOT_BALL_GENERATION_ATTEMPTS.getValue(); i++) {
        generateLootBallOnChunk(level, levelChunk, randomSource);
      }
    }
  }

  public static void onServerTick(MinecraftServer server) {
    // Initialize the loot ball spawn tick
    if (lootBallSpawnTick == 0) lootBallSpawnTick = server.getTickCount() + getLootBallCooldown();

    // Every lootBallSpawnCooldown ticks, try to spawn a loot ball
    if (server.getTickCount() > lootBallSpawnTick && LOOT_BALL_SPAWNING_ENABLED.getValue()) {
      spawnLootBallNearRandomPlayer(server, randomSource);
      // Reset the cooldown to a new random value
      lootBallSpawnTick = server.getTickCount() + getLootBallCooldown();
    }
  }

  private static long getLootBallCooldown() {
    return CobblelootsEventManager.randomSource.nextIntBetweenInclusive(Math.toIntExact(LOOT_BALL_SPAWN_COOLDOWN_MIN.getValue()), Math.toIntExact(LOOT_BALL_SPAWN_COOLDOWN_MAX.getValue()));
  }


}
