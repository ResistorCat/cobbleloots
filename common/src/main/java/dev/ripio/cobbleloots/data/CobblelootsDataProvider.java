package dev.ripio.cobbleloots.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.config.CobblelootsConfig;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallResourceLocation;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallSources;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsBlockFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsDateFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsLightFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsPositionFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobbleloootsBiomeFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsSourceFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsTimeFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsWeatherFilter;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static dev.ripio.cobbleloots.data.CobblelootsCodecs.LOOT_BALL_DATA_CODEC;
import static dev.ripio.cobbleloots.util.math.CobblelootsMath.weightedRandomEntry;

public class CobblelootsDataProvider {
  private static final Map<ResourceLocation, CobblelootsLootBallData> lootBallsData = new HashMap<>();
  private static final List<CobblelootsLootBallResourceLocation> disabledLootBalls = new ArrayList<>();

  public static void addLootBallData(ResourceLocation id, JsonElement json) {
    DataResult<CobblelootsLootBallData> result = LOOT_BALL_DATA_CODEC.parse(JsonOps.INSTANCE, json);
    CobblelootsLootBallData data = result.resultOrPartial(Cobbleloots.LOGGER::error).orElseThrow();
    lootBallsData.put(id, data);
  }

  public static void removeLootBallData(List<ResourceLocation> ids) {
    ids.forEach(lootBallsData::remove);
  }

  public static List<ResourceLocation> getExistingLootBallIds() {
    return new ArrayList<>(lootBallsData.keySet());
  }

  @Nullable
  public static CobblelootsLootBallData getLootBallData(ResourceLocation id) {
    return lootBallsData.get(id);
  }

  /**
   * Gets a random loot ball data that matches the filters for the given location
   * and source type.
   *
   * @param level      The server level
   * @param levelChunk The chunk where the loot ball is being checked
   *                   (used for block and fluid checks)
   * @param pos        The position to check filters against
   * @param sourceType The source type of the loot ball (generation, spawning,
   *                   fishing, etc.)
   * @return A Map.Entry containing both the ResourceLocation ID and the
   *         CobblelootsLootBallData,
   *         or null if no matching loot ball was found
   */
  public static Map.Entry<ResourceLocation, CobblelootsLootBallData> getRandomLootBallData(ServerLevel level,
      LevelChunk levelChunk,
      BlockPos pos, CobblelootsSourceType sourceType) {
    Map<ResourceLocation, Integer> filtered = new HashMap<>();

    // Loop through all loot balls
    for (ResourceLocation id : getExistingLootBallIds()) {
      // Get loot ball data
      CobblelootsLootBallData data = getLootBallData(id);
      if (data == null) {
        Cobbleloots.LOGGER.error("Data not found for id: {}", id);
        continue;
      }
      // Get sources data
      CobblelootsLootBallSources sourcesData = data.getSources();
      if (sourcesData == null) {
        continue;
      }
      // Get sourceType sources
      List<CobblelootsSourceFilter> sourceFilters;
      switch (sourceType) {
        case GENERATION -> sourceFilters = sourcesData.getGeneration();
        case SPAWNING -> sourceFilters = sourcesData.getSpawning();
        case FISHING -> sourceFilters = sourcesData.getFishing();
        case ARCHAEOLOGY -> sourceFilters = sourcesData.getArchaeology();
        default -> throw new IllegalStateException("Unexpected sourceType value: " + sourceType);
      }
      // Process source filters
      for (CobblelootsSourceFilter filter : sourceFilters) {
        if (processSourceFilter(level, levelChunk, pos, filter, sourceType, null, null)) {
          // Add loot ball
          filtered.put(id, filter.getWeight());
        }
      }
    }

    // Get random entry
    if (!filtered.isEmpty()) {
      Map.Entry<ResourceLocation, Integer> weightEntry = weightedRandomEntry(filtered);
      if (weightEntry != null) {
        ResourceLocation key = weightEntry.getKey();
        CobblelootsLootBallData value = getLootBallData(key);
        return new AbstractMap.SimpleEntry<>(key, value);
      }
    }

    // No loot ball found
    return null;
  }

  public static Map.Entry<ResourceLocation, CobblelootsLootBallData> getRandomLootBallData(ServerLevel level,
      LevelChunk levelChunk,
      BlockPos pos, CobblelootsSourceType sourceType, @Nullable ServerPlayer player, @Nullable ItemStack tool) {
    Map<ResourceLocation, Integer> filtered = new HashMap<>();

    // Loop through all loot balls
    for (ResourceLocation id : getExistingLootBallIds()) {
      CobblelootsLootBallData data = getLootBallData(id);
      if (data == null)
        continue;

      CobblelootsLootBallSources sourcesData = data.getSources();
      if (sourcesData == null)
        continue;

      List<CobblelootsSourceFilter> sourceFilters;
      switch (sourceType) {
        case GENERATION -> sourceFilters = sourcesData.getGeneration();
        case SPAWNING -> sourceFilters = sourcesData.getSpawning();
        case FISHING -> sourceFilters = sourcesData.getFishing();
        case ARCHAEOLOGY -> sourceFilters = sourcesData.getArchaeology();
        default -> throw new IllegalStateException("Unexpected sourceType value: " + sourceType);
      }

      for (CobblelootsSourceFilter filter : sourceFilters) {
        if (processSourceFilter(level, levelChunk, pos, filter, sourceType, player, tool)) {
          // Add loot ball
          filtered.put(id, filter.getWeight());
        }
      }
    }

    if (!filtered.isEmpty()) {
      Map.Entry<ResourceLocation, Integer> weightEntry = weightedRandomEntry(filtered);
      if (weightEntry != null) {
        ResourceLocation key = weightEntry.getKey();
        CobblelootsLootBallData value = getLootBallData(key);
        return new AbstractMap.SimpleEntry<>(key, value);
      }
    }
    return null;
  }

  /**
   * Processes all filters in a source filter to determine if a loot ball can
   * spawn
   * at the given position.
   *
   * @param level      The server level
   * @param chunk      The chunk where the loot ball is being checked
   * @param pos        The block position
   * @param source     The source filter to check
   * @param sourceType The source type of the loot ball (generation, spawning,
   *                   fishing, etc.)
   * @return true if all filters pass, false if any filter fails
   */
  public static boolean processSourceFilter(ServerLevel level, LevelChunk chunk, BlockPos pos,
      CobblelootsSourceFilter source, CobblelootsSourceType sourceType) {
    return processSourceFilter(level, chunk, pos, source, sourceType, null, null);
  }

  public static boolean processSourceFilter(ServerLevel level, LevelChunk chunk, BlockPos pos,
      CobblelootsSourceFilter source, CobblelootsSourceType sourceType, @Nullable ServerPlayer player,
      @Nullable ItemStack tool) {
    if (level == null || pos == null || source == null) {
      // Cobbleloots.LOGGER.info("[DEBUG] processSourceFilter: Null parameter
      // received. Level: {}, Pos: {}, Source: {}",
      // level, pos, source);
      return false;
    }

    // Check if dimension is disabled for this source type
    if (isDimensionDisabled(level, sourceType)) {
      return false;
    }

    // Check structure if specified
    if (!checkStructureFilter(level, pos, source.getStructure())) {
      // Cobbleloots.LOGGER.info("[DEBUG] Structure filter failed for source: {} at
      // pos: {}", source, pos);
      return false;
    }

    // Check biome if specified
    if (!checkBiomeFilter(level, pos, source.getBiome())) {
      // Cobbleloots.LOGGER.info("[DEBUG] Biome filter failed for source: {} at pos:
      // {}. Biome at pos: {}", source, pos,
      // level.getBiome(pos).unwrapKey().map(Object::toString).orElse("unknown"));
      return false;
    }

    // Check dimension if specified
    if (!checkDimensionFilter(level, source.getDimension())) {
      // Cobbleloots.LOGGER.info("[DEBUG] Dimension filter failed for source: {}.
      // Expected: {}, Actual: {}", source,
      // source.getDimension(), level.dimension().location());
      return false;
    }

    // Check blocks if specified
    if (!checkBlockFilter(level, chunk, pos, source.getBlock())) {
      // Cobbleloots.LOGGER.info("[DEBUG] Block filter failed for source: {} at pos:
      // {}.", source, pos);
      return false;
    }

    // Check fluid if specified
    if (!checkFluidFilter(level, chunk, pos, source.getFluid())) {
      // Cobbleloots.LOGGER.info("[DEBUG] Fluid filter failed for source: {} at pos:
      // {}.", source, pos);
      return false;
    }

    // Check position if specified
    if (!checkPositionFilter(pos, source.getPosition())) {
      // Cobbleloots.LOGGER.info("[DEBUG] Position filter failed for source: {} at
      // pos: {}", source, pos);
      return false;
    }

    // Check light if specified (only for SPAWNING source type)
    if (sourceType == CobblelootsSourceType.SPAWNING && !checkLightFilter(level, pos, source.getLight())) {
      // Cobbleloots.LOGGER.info("[DEBUG] Light filter failed for source: {} at pos:
      // {}. BlockLight: {}, SkyLight: {}",
      // source, pos, level.getBrightness(LightLayer.BLOCK, pos),
      // level.getBrightness(LightLayer.SKY, pos));
      return false;
    }

    // Check time if specified
    if (!checkTimeFilter(level, source.getTime())) {
      // Cobbleloots.LOGGER.info("[DEBUG] Time filter failed for source: {}. Current
      // game time: {}", source, level.getGameTime());
      return false;
    }

    // Check weather if specified
    if (!checkWeatherFilter(level, source.getWeather())) {
      // Cobbleloots.LOGGER.info("[DEBUG] Weather filter failed for source: {}.
      // IsRaining: {}, IsThundering: {}", source, level.isRaining(),
      // level.isThundering());
      return false;
    }

    // Check date if specified
    if (!checkDateFilter(source.getDate())) {
      return false;
    }

    // Check poke rod if specified (only for FISHING source type)
    if (sourceType == CobblelootsSourceType.FISHING) {
      if (!checkPokeRodFilter(player, tool, source.getPokeRod())) {
        return false;
      }
    }

    // Cobbleloots.LOGGER.info("[DEBUG] All filters passed for source: {} at pos:
    // {}", source, pos);

    // All filters passed
    return true;
  }

  /**
   * Checks if the position is within a piece of a structure matching the tag.
   *
   * @return true if the position is within a piece of the structure, false
   *         otherwise
   */
  private static boolean checkStructureFilter(ServerLevel level, BlockPos pos, TagKey<Structure> structureTag) {
    if (structureTag == null || structureTag.equals(CobblelootsDefinitions.EMPTY_STRUCTURE_TAG)) {
      return true; // No filter specified, so it passes
    }

    StructureStart structureStart = level.structureManager().getStructureWithPieceAt(pos,
        holder -> holder.is(structureTag));

    return structureStart != null && structureStart.isValid();
  }

  /**
   * Checks if the position is in a biome matching the filter.
   *
   * @return true if the position matches the biome filter, false otherwise
   */
  private static boolean checkBiomeFilter(ServerLevel level, BlockPos pos, CobbleloootsBiomeFilter biomeFilter) {
    if (biomeFilter == null || biomeFilter.isEmpty()) {
      return true; // No filter specified, so it passes
    }

    return biomeFilter.test(level, pos);
  }

  /**
   * Checks if the dimension matches the specified filter.
   * 
   * @return true if the dimension matches, false otherwise
   */
  private static boolean checkDimensionFilter(ServerLevel level, List<ResourceLocation> dimensionIds) {
    if (dimensionIds == null || dimensionIds.isEmpty()) {
      return true; // No filter specified, so it passes
    }

    return dimensionIds.contains(level.dimension().location());
  }

  /**
   * Checks if the position has the correct block state.
   * 
   * @return true if the block state matches the filter, false otherwise
   */
  private static boolean checkBlockFilter(ServerLevel level, LevelChunk chunk, BlockPos spawnPos,
      CobblelootsBlockFilter blockFilter) {
    if (blockFilter == null) {
      return true; // No filter specified, so it passes
    }

    LevelChunkSection[] sections = chunk.getSections();

    // Get base block state
    BlockPos basePos = spawnPos.below();
    int baseSectionIndex = chunk.getSectionIndex(basePos.getY());
    if (baseSectionIndex < 0 || baseSectionIndex >= sections.length) {
      return false; // Base section index is invalid
    }
    LevelChunkSection baseSection = sections[baseSectionIndex];
    BlockState baseBlockState = baseSection.getBlockState(basePos.getX() & 15, basePos.getY() & 15,
        basePos.getZ() & 15);

    // Get spawn block state
    int spawnSectionIndex = chunk.getSectionIndex(spawnPos.getY());
    LevelChunkSection spawnSection = sections[spawnSectionIndex];
    BlockState spawnBlockState = spawnSection.getBlockState(spawnPos.getX() & 15, spawnPos.getY() & 15,
        spawnPos.getZ() & 15);

    return blockFilter.isSpawnable(spawnBlockState) && blockFilter.isBase(baseBlockState);
  }

  /**
   * Checks if the position has the correct fluid.
   * 
   * @return true if the fluid state matches the filter, false otherwise
   */
  private static boolean checkFluidFilter(ServerLevel level, LevelChunk chunk, BlockPos pos, TagKey<Fluid> fluidTag) {
    if (fluidTag == null || fluidTag.equals(CobblelootsDefinitions.EMPTY_FLUID_TAG)) {
      return true; // No filter specified, so it passes
    }

    LevelChunkSection[] sections = chunk.getSections();
    int sectionIndex = chunk.getSectionIndex(pos.getY());
    LevelChunkSection section = sections[sectionIndex];
    FluidState fluidState = section.getFluidState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);

    return fluidState.is(fluidTag); // Check if the fluid state matches the tag
  }

  /**
   * Checks if the position is within the specified range.
   * 
   * @return true if the position is within the range, false otherwise
   */
  private static boolean checkPositionFilter(BlockPos pos, CobblelootsPositionFilter positionFilter) {
    if (positionFilter == null) {
      return true; // No filter specified, so it passes
    }

    return positionFilter.isInRange(pos.getX(), pos.getY(), pos.getZ());
  }

  /**
   * Checks if the light level at the position meets the criteria.
   * 
   * @return true if the light level is within the range, false otherwise
   */
  private static boolean checkLightFilter(ServerLevel level, BlockPos pos, CobblelootsLightFilter lightFilter) {
    if (lightFilter == null) {
      return true; // No filter specified, so it passes
    }

    int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
    int skyLight = level.getBrightness(LightLayer.SKY, pos);

    return lightFilter.isInRange(blockLight, skyLight);
  }

  /**
   * Checks if the current time meets the criteria.
   * 
   * @return true if the current time is within the range, false otherwise
   */
  private static boolean checkTimeFilter(ServerLevel level, CobblelootsTimeFilter timeFilter) {
    if (timeFilter == null) {
      return true; // No filter specified, so it passes
    }
    long currentTime = level.getDayTime();
    if (timeFilter.getPeriod() > 0) {
      currentTime = level.getDayTime() % timeFilter.getPeriod();
    } else {
      currentTime = level.getDayTime();
    }
    return timeFilter.getValue().isInRange(((int) currentTime));
  }

  /**
   * Checks if the current weather meets the criteria.
   * 
   * @return true if the current weather matches the filter, false otherwise
   */
  private static boolean checkWeatherFilter(ServerLevel level, CobblelootsWeatherFilter weatherFilter) {
    if (weatherFilter == null) {
      return true; // No filter specified, so it passes
    }

    return weatherFilter.isValid(level.isRaining(), level.isThundering());
  }

  /**
   * Checks if the current date meets the criteria.
   * 
   * @return true if the current date is within the range, false otherwise
   */
  private static boolean checkDateFilter(CobblelootsDateFilter dateFilter) {
    if (dateFilter == null) {
      return true; // No filter specified, so it passes
    }
    return dateFilter.test();
  }

  /**
   * Checks if the current poke rod meets the criteria.
   *
   * @return true if the poke rod matches the filter, false otherwise
   */
  private static boolean checkPokeRodFilter(@Nullable ServerPlayer player, @Nullable ItemStack tool,
      @Nullable dev.ripio.cobbleloots.data.custom.filter.CobblelootsPokeRodFilter filter) {
    if (filter == null)
      return true;
    return filter.test(null, null, null, player, tool);
  }

  public static void onReload(ResourceManager resourceManager) {
    // Cache data
    List<ResourceLocation> cachedLootBalls = getExistingLootBallIds();

    // Load disabled loot balls from config
    disabledLootBalls.clear();
    disabledLootBalls.addAll(CobblelootsConfig.data_pack_disabled_loot_balls.stream()
        .map(CobblelootsLootBallResourceLocation::new).toList());

    // Load loot balls
    for (ResourceLocation id : resourceManager
        .listResources(CobblelootsDefinitions.PATH_LOOT_BALLS, path -> path.getPath().endsWith(".json")).keySet()) {
      // Normalize id
      ResourceLocation normalizedId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
          id.getPath().replace(".json", ""));

      // Check if the loot ball is disabled
      boolean isDisabled = false;
      for (CobblelootsLootBallResourceLocation disabled : disabledLootBalls) {
        if (disabled.matches(normalizedId.getNamespace(), normalizedId.getPath(), "*")) {
          isDisabled = true;
          break;
        }
      }

      if (isDisabled) {
        Cobbleloots.LOGGER.info("Loot ball {} is disabled", normalizedId);
        continue;
      }

      try (InputStream stream = resourceManager.getResourceOrThrow(id).open()) {
        // Parse JSON
        JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
            .getAsJsonObject();
        // Load loot ball data
        addLootBallData(normalizedId, jsonObject);
        cachedLootBalls.remove(normalizedId);
      } catch (IOException | NullPointerException | JsonSyntaxException | NoSuchElementException e) {
        Cobbleloots.LOGGER.error("Error loading loot ball data: {}", id, e);
      }
    }

    // Remove deleted loot balls
    removeLootBallData(cachedLootBalls);

    Cobbleloots.LOGGER.info("Loaded {} Loot Ball data definitions.", lootBallsData.size());
  }

  /**
   * Checks if the dimension is disabled for the specified source type
   * 
   * @param level      The server level
   * @param sourceType The source type being checked
   * @return true if dimension is disabled, false otherwise
   */
  private static boolean isDimensionDisabled(ServerLevel level, CobblelootsSourceType sourceType) {
    ResourceLocation dimensionId = level.dimension().location();

    switch (sourceType) {
      case GENERATION:
        if (CobblelootsConfig.generation_disabled_dimensions.contains(dimensionId))
          return true;
        break;
      case SPAWNING:
        if (CobblelootsConfig.spawning_disabled_dimensions.contains(dimensionId))
          return true;
        break;
      case FISHING:
        if (CobblelootsConfig.fishing_disabled_dimensions.contains(dimensionId))
          return true;
        break;
      case ARCHAEOLOGY:
        if (CobblelootsConfig.archaeology_disabled_dimensions.contains(dimensionId))
          return true;
        break;
      default:
        return false;
    }

    return false;
  }
}
