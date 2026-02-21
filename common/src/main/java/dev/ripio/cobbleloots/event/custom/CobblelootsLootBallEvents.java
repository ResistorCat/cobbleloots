package dev.ripio.cobbleloots.event.custom;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import dev.ripio.cobbleloots.util.search.CobblelootsSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.ripio.cobbleloots.data.CobblelootsDataProvider.getRandomLootBallData;

import dev.ripio.cobbleloots.config.CobblelootsConfig;
import static dev.ripio.cobbleloots.entity.CobblelootsEntities.getLootBallEntityType;

public class CobblelootsLootBallEvents {
  public static boolean generateLootBallOnChunk(ServerLevel level, LevelChunk levelChunk, RandomSource randomSource) {
    // STEP: Check chance for loot ball generation attempt
    if (randomSource.nextFloat() > CobblelootsConfig.generation_chance)
      return false;

    // STEP: Select a random position in the chunk
    LevelChunkSection[] sections = levelChunk.getSections();
    int sectionIndex = CobblelootsSearch.searchRandomNonEmptySectionIndex(sections, randomSource);
    if (sectionIndex < 0 || sectionIndex >= sections.length) {
      Cobbleloots.LOGGER.debug("[Cobbleloots] Invalid section index: {} for chunk at {}", sectionIndex,
          levelChunk.getPos());
      return false; // Invalid section index, skip generation
    }
    LevelChunkSection section = sections[sectionIndex];
    BlockPos relativePos = CobblelootsSearch.searchRandomValidLootBallSpawn(section, randomSource);
    if (relativePos == null)
      return false;
    BlockPos pos = relativePos.offset(levelChunk.getPos().getMinBlockX(),
        levelChunk.getSectionYFromSectionIndex(sectionIndex) * LevelChunkSection.SECTION_HEIGHT,
        levelChunk.getPos().getMinBlockZ());

    // STEP: Attempt spawn at given pos
    return spawnLootBall(level, levelChunk, pos, randomSource, CobblelootsSourceType.GENERATION) != null;
  }

  public static void spawnLootBallNearRandomPlayer(MinecraftServer server, RandomSource randomSource) {
    // STEP: Check chance for loot ball spawn attempt
    if (randomSource.nextFloat() > CobblelootsConfig.spawning_chance)
      return;

    // STEP: Choose a random player
    List<ServerPlayer> playerList = server.getPlayerList().getPlayers();
    if (playerList.isEmpty())
      return;
    ServerPlayer player = playerList.get(randomSource.nextInt(playerList.size()));

    // STEP: Select a random chunk near the player
    ServerLevel level = player.serverLevel();
    LevelChunk playerChunk = level.getChunkAt(player.blockPosition());
    ChunkPos playerChunkPos = playerChunk.getPos();
    LevelChunk randomChunk = level.getChunk(playerChunkPos.x + randomSource.nextIntBetweenInclusive(-1, 1),
        playerChunkPos.z + randomSource.nextIntBetweenInclusive(-1, 1));

    // STEP: Select a random position in the chunk
    int sectionIndex = playerChunk.getSectionIndex((int) player.getY());
    LevelChunkSection[] sections = randomChunk.getSections();
    if (sectionIndex < 0 || sectionIndex >= sections.length) {
      Cobbleloots.LOGGER.debug("[Cobbleloots] Invalid section index: {} for player at {}", sectionIndex,
          player.blockPosition());
      return; // Invalid section index, skip spawning
    }
    LevelChunkSection section = sections[sectionIndex];
    BlockPos relativePos = CobblelootsSearch.searchRandomValidLootBallSpawn(section, randomSource);
    if (relativePos == null)
      return;
    BlockPos pos = relativePos.offset(randomChunk.getPos().getMinBlockX(),
        randomChunk.getSectionYFromSectionIndex(sectionIndex) * LevelChunkSection.SECTION_HEIGHT,
        randomChunk.getPos().getMinBlockZ());

    // STEP: Attempt spawn at given pos
    CobblelootsLootBall lootBall = spawnLootBall(level, randomChunk, pos, randomSource, CobblelootsSourceType.SPAWNING);
    if (lootBall != null && CobblelootsConfig.spawning_despawn_time > 0) {
      // Set despawn timer
      lootBall.setDespawnTick(level.getGameTime() + CobblelootsConfig.spawning_despawn_time);
    }
  }

  @Nullable
  public static CobblelootsLootBall spawnLootBall(ServerLevel level, LevelChunk chunk, BlockPos pos,
      RandomSource randomSource, CobblelootsSourceType sourceType) {
    return spawnLootBall(level, chunk, pos, randomSource, sourceType, null, null);
  }

  @Nullable
  public static CobblelootsLootBall spawnLootBall(ServerLevel level, LevelChunk chunk, BlockPos pos,
      RandomSource randomSource, CobblelootsSourceType sourceType, @Nullable ServerPlayer player,
      @Nullable ItemStack tool) {
    // STEP: Get a random loot ball data that passes all filters
    Map.Entry<ResourceLocation, CobblelootsLootBallData> lootBallEntry = getRandomLootBallData(level, chunk, pos,
        sourceType, player, tool);
    if (lootBallEntry == null) {
      return null; // No suitable loot ball data found
    }

    ResourceLocation dataId = lootBallEntry.getKey();
    CobblelootsLootBallData lootBallData = lootBallEntry.getValue();

    // If we couldn't find the ID, don't spawn
    if (dataId == null || dataId == CobblelootsDefinitions.EMPTY_LOCATION) {
      return null;
    }

    // Select random variant if available
    String variant = "";
    if (lootBallData.getVariants() != null && !lootBallData.getVariants().isEmpty()) {
      // Pick a random variant
      List<String> variantKeys = new ArrayList<>(lootBallData.getVariants().keySet());
      if (!variantKeys.isEmpty()) {
        variant = variantKeys.get(randomSource.nextInt(variantKeys.size()));
      }
    }

    // STEP: Create a new loot ball entity
    EntityType<CobblelootsLootBall> lootBallEntityType = getLootBallEntityType();
    CobblelootsLootBall lootBall = lootBallEntityType.create(level);
    if (lootBall == null) {
      return null;
    }

    // STEP: Move loot ball to the given position and add it to the world
    Vec3 vec3 = new Vec3(pos.getX() + 0.25F + randomSource.nextFloat() * 0.5F, pos.getY(),
        pos.getZ() + 0.25F + randomSource.nextFloat() * 0.5F);
    float f = randomSource.nextFloat() * (360F);
    lootBall.moveTo(vec3.x(), vec3.y(), vec3.z(), f, 0.0F);
    level.addFreshEntity(lootBall);

    // STEP: Set loot ball data ID and variant ID
    lootBall.setLootBallDataId(dataId);
    lootBall.setVariantId(variant);

    // STEP: Check random chance to be invisible and have a multiplier
    if (randomSource.nextFloat() < CobblelootsConfig.loot_ball_bonus_chance) {
      if (CobblelootsConfig.loot_ball_bonus_invisible) {
        lootBall.setInvisible(true);
      }
      lootBall.setMultiplier(CobblelootsConfig.loot_ball_bonus_multiplier);
    }

    return lootBall;
  }
}
