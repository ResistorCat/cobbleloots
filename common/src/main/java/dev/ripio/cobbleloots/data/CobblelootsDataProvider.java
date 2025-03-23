package dev.ripio.cobbleloots.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallSource;
import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static dev.ripio.cobbleloots.data.CobblelootsCodecs.EMPTY_BIOME_TAG;
import static dev.ripio.cobbleloots.data.CobblelootsCodecs.LOOT_BALL_CODEC;

public class CobblelootsDataProvider {
  public static final String PATH_LOOT_BALLS = "loot_ball";
  private static final Map<ResourceLocation, CobblelootsLootBallData> lootBallsData = new HashMap<>();

  public static void addLootBallData(ResourceLocation id, JsonElement json) {
    DataResult<CobblelootsLootBallData> result = LOOT_BALL_CODEC.parse(JsonOps.INSTANCE, json);
    CobblelootsLootBallData data = result.resultOrPartial(Cobbleloots.LOGGER::error).orElseThrow();
    lootBallsData.put(id, data);
  }

  public static void removeLootBallData(ResourceLocation id) {
    lootBallsData.remove(id);
  }

  public static void removeLootBallData(List<ResourceLocation> ids) {
    ids.forEach(lootBallsData::remove);
  }

  @Nullable
  public static CobblelootsLootBallData getLootBallData(ResourceLocation id, int variant) {
    CobblelootsLootBallData data = lootBallsData.get(id);
    if (data == null) return null;

    // If the variant is negative, return the base data
    if (variant < 0) return data;

    // Check if valid variant
    List<CobblelootsLootBallData> variantsData = data.getVariants();
    if (variantsData != null) {
      if (variant < variantsData.size()) {
        return variantsData.get(variant);
      }
    }

    return data;
  }

  public static int getTotalWeight(List<ResourceLocation> ids) {
    int total = 0;
    for (ResourceLocation id : ids) {
      CobblelootsLootBallData data = getLootBallData(id, -1);
      if (data != null) {
        total += data.getWeight();
      }
    }
    return total;
  }

  public static @NotNull List<ResourceLocation> getFilteredLootBallIds(ServerLevel level, BlockPos pos, CobblelootsSourceType sourceType) {
    List<ResourceLocation> validLootBallIds = getExistingLootBallIds();
    //Cobbleloots.LOGGER.info("DEBUG: Total loot balls: {}", validLootBallIds.size());
    // Filter loot balls by biome and height
    validLootBallIds.removeIf(id -> {
      // Get loot ball data and sources
      CobblelootsLootBallData data = getLootBallData(id, -1);
      if (data == null) {
        Cobbleloots.LOGGER.error("Data not found for id: {}", id);
        return true;
      }
      List<CobblelootsLootBallSource> sourcesData = data.getSources();
      if (sourcesData == null) {
        //Cobbleloots.LOGGER.info("DEBUG: Sources not found for id: {}", id);
        return true;
      }

      // Search for sourceType source
      CobblelootsLootBallSource source = sourcesData.stream()
          .filter(s -> Objects.equals(s.getType(), sourceType.getName()))
          .findFirst()
          .orElse(null);
      if (source == null) {
        //Cobbleloots.LOGGER.info("DEBUG: Source not found for id: {}", id);
        return true;
      }

      // Check biome
      TagKey<Biome> biomeId = source.getBiome();
      if (biomeId != EMPTY_BIOME_TAG && !level.getBiome(pos).is(biomeId)) {
        //Cobbleloots.LOGGER.info("DEBUG: Expected biome: {}, Found biome: {}", biomeId.location(), level.getBiome(pos).getRegisteredName());
        return true;
      }

      // Check height
      if (!source.getHeight().isInRange(pos.getY())) {
        //Cobbleloots.LOGGER.info("DEBUG: Expected height: {} to {}, Found height: {}", source.getHeight().getMin(), source.getHeight().getMax(), pos.getY());
        return true;
      }

      return false;
    });

    return validLootBallIds;
  }

  public static List<ResourceLocation> getExistingLootBallIds() {
    return new ArrayList<>(lootBallsData.keySet());
  }

  public static void onReload(ResourceManager resourceManager) {
    // Cache data
    List<ResourceLocation> cachedLootBalls = getExistingLootBallIds();

    // Load loot balls
    for (ResourceLocation id : resourceManager.listResources(PATH_LOOT_BALLS, path -> path.getPath().endsWith(".json")).keySet()) {
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
