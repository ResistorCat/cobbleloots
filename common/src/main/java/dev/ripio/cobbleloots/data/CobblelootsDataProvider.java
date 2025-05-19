package dev.ripio.cobbleloots.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallSources;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsSourceFilter;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static dev.ripio.cobbleloots.data.CobblelootsCodecs.LOOT_BALL_DATA_CODEC;
import static dev.ripio.cobbleloots.util.CobblelootsDefinitions.EMPTY_BIOME_TAG;
import static dev.ripio.cobbleloots.util.math.CobblelootsMath.weightedRandomEntry;

public class CobblelootsDataProvider {
  private static final Map<ResourceLocation, CobblelootsLootBallData> lootBallsData = new HashMap<>();

  public static void addLootBallData(ResourceLocation id, JsonElement json) {
    DataResult<CobblelootsLootBallData> result = LOOT_BALL_DATA_CODEC.parse(JsonOps.INSTANCE, json);
    CobblelootsLootBallData data = result.resultOrPartial(Cobbleloots.LOGGER::error).orElseThrow();
    lootBallsData.put(id, data);
  }

  public static void removeLootBallData(List<ResourceLocation> ids) {ids.forEach(lootBallsData::remove);}

  public static List<ResourceLocation> getExistingLootBallIds() {
    return new ArrayList<>(lootBallsData.keySet());
  }

  @Nullable
  public static CobblelootsLootBallData getLootBallData(ResourceLocation id) {return lootBallsData.get(id);}


  public static CobblelootsLootBallData getRandomLootBallData(ServerLevel level, BlockPos pos, CobblelootsSourceType sourceType) {
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
      if (sourcesData == null) { continue; }
      // Get sourceType sources
      List<CobblelootsSourceFilter> sourceFilters;
      switch (sourceType) {
        case GENERATION -> sourceFilters = sourcesData.getGeneration();
        case SPAWNING -> sourceFilters = sourcesData.getSpawning();
        case FISHING -> sourceFilters = sourcesData.getFishing();
        case ARCHAEOLOGY -> sourceFilters = sourcesData.getArchaeology();
        default -> throw new IllegalStateException("Unexpected value: " + sourceType);
      }
      // Process source filters
      for (CobblelootsSourceFilter filter : sourceFilters) {
        if (processSourceFilter(level, pos, filter)) {
          // Add loot ball
          filtered.put(id, filter.getWeight());
        }
      }
    }

    // Get random entry
    if (!filtered.isEmpty()) {
      Map.Entry<ResourceLocation, Integer> entry = weightedRandomEntry(filtered);
      if (entry != null) return getLootBallData(entry.getKey());
    }

    // No loot ball found
    return null;
  }

  public static boolean processSourceFilter(ServerLevel level, BlockPos pos, CobblelootsSourceFilter source) {
    // Check structure
    StructureStart structureSet = level.structureManager().getStructureWithPieceAt(pos, source.getStructure());
    Cobbleloots.LOGGER.info("DEBUG: Structure: {}, StructureSet: {}", source.getStructure(), structureSet);

    // TODO: Complete filters

    return true;
  }

//  public static @NotNull List<ResourceLocation> getFilteredLootBallIds(ServerLevel level, BlockPos pos, CobblelootsSourceType sourceType) {
//    List<ResourceLocation> validLootBallIds = getExistingLootBallIds();
//    //Cobbleloots.LOGGER.info("DEBUG: Total loot balls: {}", validLootBallIds.size());
//    // Filter loot balls by biome and height
//    validLootBallIds.removeIf(id -> {
//      // Get loot ball data and sources
//      CobblelootsLootBallData data = getLootBallData(id, -1);
//      if (data == null) {
//        Cobbleloots.LOGGER.error("Data not found for id: {}", id);
//        return true;
//      }
//      List<CobblelootsSourceFilter> sourcesData = data.getSources();
//      if (sourcesData == null) {
//        //Cobbleloots.LOGGER.info("DEBUG: Sources not found for id: {}", id);
//        return true;
//      }
//
//      // Search for sourceType source
//      CobblelootsSourceFilter source = sourcesData.stream()
//          .filter(s -> Objects.equals(s.getType(), sourceType.getName()))
//          .findFirst()
//          .orElse(null);
//      if (source == null) {
//        //Cobbleloots.LOGGER.info("DEBUG: Source not found for id: {}", id);
//        return true;
//      }
//
//      // Check biome
//      TagKey<Biome> biomeId = source.getBiome();
//      if (biomeId != EMPTY_BIOME_TAG && !level.getBiome(pos).is(biomeId)) {
//        //Cobbleloots.LOGGER.info("DEBUG: Expected biome: {}, Found biome: {}", biomeId.location(), level.getBiome(pos).getRegisteredName());
//        return true;
//      }
//
//      // Check height
//      if (!source.getHeight().isInRange(pos.getY())) {
//        //Cobbleloots.LOGGER.info("DEBUG: Expected height: {} to {}, Found height: {}", source.getHeight().getMin(), source.getHeight().getMax(), pos.getY());
//        return true;
//      }
//
//      return false;
//    });
//
//    return validLootBallIds;
//  }

  public static void onReload(ResourceManager resourceManager) {
    // Cache data
    List<ResourceLocation> cachedLootBalls = getExistingLootBallIds();

    // Load loot balls
    for (ResourceLocation id : resourceManager.listResources(CobblelootsDefinitions.PATH_LOOT_BALLS, path -> path.getPath().endsWith(".json")).keySet()) {
      try (InputStream stream = resourceManager.getResourceOrThrow(id).open()) {
        // Parse JSON
        JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        // Normalize id
        id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace(".json", ""));
        // Load loot ball data
        addLootBallData(id, jsonObject);
        cachedLootBalls.remove(id);
      } catch (IOException | NullPointerException | JsonSyntaxException | NoSuchElementException e) {
        Cobbleloots.LOGGER.error("Error loading loot ball data: {}", id, e);
      }
    }

    // Remove deleted loot balls
    removeLootBallData(cachedLootBalls);

    Cobbleloots.LOGGER.info("Loaded {} Loot Ball data definitions.", lootBallsData.size());
  }
}
