package dev.ripio.cobbleloots.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.data.lootball.CobblelootsLootBallData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static dev.ripio.cobbleloots.data.CobblelootsCodecs.LOOT_BALL_CODEC;

public class CobblelootsDataProvider {
  public static final String PATH_LOOT_BALLS = "loot_ball";
  private static final Map<ResourceLocation, CobblelootsLootBallData> lootBallsData = new HashMap<>();

  public static void addLootBallData(ResourceLocation id, JsonElement json) {
    DataResult<CobblelootsLootBallData> result = LOOT_BALL_CODEC.parse(JsonOps.INSTANCE, json);
    CobblelootsLootBallData data = result.resultOrPartial(Cobbleloots.LOGGER::error).orElseThrow();
    lootBallsData.put(id, data);
    Cobbleloots.LOGGER.info("Added loot ball data for {}", id);
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
  }
}
