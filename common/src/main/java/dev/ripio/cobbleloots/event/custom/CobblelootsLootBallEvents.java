package dev.ripio.cobbleloots.event.custom;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.ripio.cobbleloots.config.CobblelootsConfigManager.*;
import static dev.ripio.cobbleloots.data.CobblelootsDataProvider.*;
import static dev.ripio.cobbleloots.entity.CobblelootsEntities.getLootBallEntityType;
import static dev.ripio.cobbleloots.util.CobblelootsUtils.*;

public class CobblelootsLootBallEvents {
  private static final String TEXT_EVENT_LOOT_BALL_SPAWN_SPECIAL = "event.cobbleloots.loot_ball.spawn.special";

  public static void generateLootBallOnChunk(ServerLevel level, LevelChunk levelChunk, RandomSource randomSource) {
    // STEP: Check chance for loot ball generation attempt
    if (randomSource.nextFloat() > getFloatConfig(LOOT_BALL_GENERATION_CHANCE) ) return false;

    // STEP: Select a random position in the chunk
    LevelChunkSection[] sections = levelChunk.getSections();
    int sectionIndex = searchRandomNonEmptySectionIndex(sections, randomSource);
    if (sectionIndex == -1) return;
    LevelChunkSection section = sections[sectionIndex];
    BlockPos relativePos = searchRandomValidLootBallSpawn(section, randomSource);
    if (relativePos == null) return;
    BlockPos pos = relativePos.offset(levelChunk.getPos().getMinBlockX(), levelChunk.getSectionYFromSectionIndex(sectionIndex)*LevelChunkSection.SECTION_HEIGHT, levelChunk.getPos().getMinBlockZ());

    // STEP: Check loot ball cap for the chunk
    //int lootBallCount = level.getEntities(CobblelootsLootBall.class, pos2.getX(), pos2.getY(), pos2.getZ(), MAX_LOOT_BALLS_PER_CHUNK).size();

    // STEP: Attempt spawn at given pos
    spawnLootBall(level, pos, randomSource, CobblelootsSourceType.GENERATION);
  }

  public static void spawnLootBallNearRandomPlayer(MinecraftServer server, RandomSource randomSource) {
    // STEP: Check chance for loot ball spawn attempt
    if (randomSource.nextFloat() > getFloatConfig(LOOT_BALL_SPAWN_CHANCE)) return;

    // STEP: Choose a random player
    List<ServerPlayer> playerList = server.getPlayerList().getPlayers();
    if (playerList.isEmpty()) return;
    ServerPlayer player = playerList.get(randomSource.nextInt(playerList.size()));

    // STEP: Select a random chunk near the player
    ServerLevel level = player.serverLevel();
    LevelChunk playerChunk = level.getChunkAt(player.blockPosition());
    ChunkPos playerChunkPos = playerChunk.getPos();
    LevelChunk randomChunk = level.getChunk(playerChunkPos.x + randomSource.nextIntBetweenInclusive(-1, 1), playerChunkPos.z + randomSource.nextIntBetweenInclusive(-1, 1));

    // STEP: Select a random position in the chunk
    int sectionIndex = playerChunk.getSectionIndex((int) player.getY());
    LevelChunkSection[] sections = randomChunk.getSections();
    LevelChunkSection section = sections[sectionIndex];
    BlockPos relativePos = searchRandomValidLootBallSpawn(section, randomSource);
    if (relativePos == null) return;
    BlockPos pos = relativePos.offset(randomChunk.getPos().getMinBlockX(), randomChunk.getSectionYFromSectionIndex(sectionIndex)*LevelChunkSection.SECTION_HEIGHT, randomChunk.getPos().getMinBlockZ());

    // STEP: Check loot ball cap for the chunk
    //int lootBallCount = level.getEntities(CobblelootsLootBall.class, pos2.getX(), pos2.getY(), pos2.getZ(), MAX_LOOT_BALLS_PER_CHUNK).size();

    // STEP: Attempt spawn at given pos
    CobblelootsLootBall lootBall = spawnLootBall(level, pos, randomSource, CobblelootsSourceType.SPAWNING);
    if (lootBall != null) {
      // STEP: Set despawn tick
      lootBall.setDespawnTick(server.getTickCount() + getIntConfig(LOOT_BALL_DESPAWN_TIME)); //LOOT_BALL_DESPAWN_TIME.getValue());
    }
  }

  @Nullable
  private static CobblelootsLootBall spawnLootBall(ServerLevel level, BlockPos pos, RandomSource randomSource, CobblelootsSourceType sourceType) {
    // STEP: Generate random loot ball data
    @Nullable List<ResourceLocation> lootBallIds = getFilteredLootBallIds(level, pos, sourceType);
    ResourceLocation dataId = weightRandomResourceLocation(randomSource, lootBallIds);
    if (dataId == CobblelootsDefinitions.EMPTY_LOCATION) return null;
    int variant = randomVariant(randomSource, dataId);

    // STEP: Create a new loot ball entity
    EntityType<CobblelootsLootBall> lootBallEntityType = getLootBallEntityType();
    CobblelootsLootBall lootBall = lootBallEntityType.create(level);

    // STEP: Move loot ball to the given position and add it to the world
    Vec3 vec3 = new Vec3(pos.getX() + 0.25F + randomSource.nextFloat()*0.5F, pos.getY(), pos.getZ() + 0.25F + randomSource.nextFloat()*0.5F);
    float f = randomSource.nextFloat() * (360F);
    lootBall.moveTo(vec3.x(), vec3.y(), vec3.z(), f, 0.0F);
    level.addFreshEntity(lootBall);

    // STEP: Set loot ball data
    lootBall.setLootBallData(dataId);
    lootBall.setVariant(variant);

    // STEP: Check random chance to be invisible and have a multiplier
    if (getBooleanConfig(LOOT_BALL_BONUS_ENABLED) && randomSource.nextFloat() < getFloatConfig(LOOT_BALL_BONUS_CHANCE)) {
      lootBall.setInvisible(getBooleanConfig(LOOT_BALL_BONUS_INVISIBLE));
      lootBall.setMultiplier(getFloatConfig(LOOT_BALL_BONUS_MULTIPLIER));
    }

    // STEP: Decoration
    if (getLootBallData(dataId, variant).getAnnounce()) {
      level.getServer().getPlayerList().broadcastSystemMessage(cobblelootsText(TEXT_EVENT_LOOT_BALL_SPAWN_SPECIAL), false);
    }
    return lootBall;
  }

  private static int randomVariant(RandomSource random, ResourceLocation dataId) {
    CobblelootsLootBallData data = getLootBallData(dataId, -1);
    if (data == null) return -1;
    List<CobblelootsLootBallData> variantsData = data.getVariants();
    if (variantsData == null) return -1;
    if (variantsData.isEmpty()) return -1;

    return random.nextIntBetweenInclusive(-1, variantsData.size()-1);
  }

  private static ResourceLocation weightRandomResourceLocation(RandomSource random, @Nullable List<ResourceLocation> lootBallIds) {
    if (lootBallIds == null) return CobblelootsDefinitions.EMPTY_LOCATION;
    if (lootBallIds.isEmpty()) return CobblelootsDefinitions.EMPTY_LOCATION;

    // Calculate total weight
    int totalWeight = getTotalWeight(lootBallIds);
    if (totalWeight == 0) return CobblelootsDefinitions.EMPTY_LOCATION;

    // Choose a random id
    int randomValue = random.nextInt(totalWeight);
    for (ResourceLocation id : lootBallIds) {
      CobblelootsLootBallData data = getLootBallData(id, -1);
      if (data == null) {
        Cobbleloots.LOGGER.error("Data not found for id: {}", id);
        continue;
      }
      randomValue -= data.getWeight();
      if (randomValue < 0) {
        return id;
      }
    }

    return CobblelootsDefinitions.EMPTY_LOCATION;
  }
}
