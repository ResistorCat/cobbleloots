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
import dev.ripio.cobbleloots.data.custom.filter.CobbleloootsBiomeFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsSourceFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsStructureFilter;
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
import net.minecraft.world.level.chunk.LevelChunk;
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
  private static volatile boolean pendingClientDataRefresh = false;

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
    return CobblelootsFilterEvaluator.testFilter(level, chunk, pos, source, sourceType);
  }

  public static boolean processSourceFilter(ServerLevel level, LevelChunk chunk, BlockPos pos,
      CobblelootsSourceFilter source, CobblelootsSourceType sourceType, @Nullable ServerPlayer player,
      @Nullable ItemStack tool) {
    return CobblelootsFilterEvaluator.testFilter(level, chunk, pos, source, sourceType, player, tool);
  }

  public static void onReload(ResourceManager resourceManager) {
    // Clear filter validation warnings caches
    CobbleloootsBiomeFilter.clearValidationWarnings();
    CobblelootsStructureFilter.clearValidationWarnings();

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

    // Flag that existing entities need their client data refreshed
    pendingClientDataRefresh = true;
  }

  /**
   * Checks if a client data refresh is pending and processes it.
   * Should be called from the server tick to ensure server context is available.
   *
   * @param server The Minecraft server instance
   */
  public static void checkAndRefreshClientData(MinecraftServer server) {
    if (!pendingClientDataRefresh)
      return;
    pendingClientDataRefresh = false;

    int count = 0;
    for (ServerLevel level : server.getAllLevels()) {
      for (Entity entity : level.getAllEntities()) {
        if (entity instanceof CobblelootsLootBall lootBall) {
          lootBall.updateLootBallClientData();
          count++;
        }
      }
    }

    if (count > 0) {
      Cobbleloots.LOGGER.info("Refreshed client data for {} loot ball entities.", count);
    }
  }

  /**
   * Checks if the dimension is disabled for the specified source type.
   *
   * @param level      The server level
   * @param sourceType The source type being checked
   * @return true if dimension is disabled, false otherwise
   */
  public static boolean isDimensionDisabled(ServerLevel level, CobblelootsSourceType sourceType) {
    return CobblelootsFilterEvaluator.isDimensionDisabled(level, sourceType);
  }
}
