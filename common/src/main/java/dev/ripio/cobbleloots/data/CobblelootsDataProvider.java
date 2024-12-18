package dev.ripio.cobbleloots.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ripio.cobbleloots.Cobbleloots;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static dev.ripio.cobbleloots.data.CobblelootsDataDefinitions.SOURCE_TYPES;

public class CobblelootsDataProvider implements CobblelootsDataRecords {
    private static final Map<ResourceLocation, LootBallRecord> lootBallRecords = new HashMap<>();
    private static final Map<String, Integer> NAMED_WEIGHTS = Map.ofEntries(
            Map.entry("common", 80),
            Map.entry("uncommon", 30),
            Map.entry("rare", 12),
            Map.entry("ultra_rare", 1)
    );
    public static final String PATH_LOOT_BALLS = "loot_ball";

    @ExpectPlatform
    public static void registerDataProvider() {
        throw new AssertionError();
    }

    public static void addLootBallRecord(ResourceLocation id, JsonObject jsonObject) {
        LootBallRecord record = deserializeLootBall(jsonObject);
        lootBallRecords.put(id, record);
    }

    public static void removeLootBallRecord(ResourceLocation id) {
        lootBallRecords.remove(id);
    }

    public static void removeLootBallRecord(List<ResourceLocation> ids) {
        ids.forEach(lootBallRecords::remove);
    }

    @Nullable
    public static LootBallRecord getLootBallRecord(ResourceLocation id, int variant) {
        LootBallRecord record = lootBallRecords.get(id);
        if (record == null) return null;
        if (variant < 0) return record;

        if (variant < record.variants().size()) {
            return record.variants().get(variant);
        }

        return record;
    }

    public static List<ResourceLocation> getExistingLootBallIds() {
        return new ArrayList<>(lootBallRecords.keySet());
    }

    private static LootBallRecord deserializeLootBall(JsonObject jsonObject) {
        // Extract name
        String name = "Custom";
        if (jsonObject.has("name")) name = jsonObject.get("name").getAsString();

        // Extract weight
        int weight = NAMED_WEIGHTS.get("common");
        if (jsonObject.has("weight")) {
            try {
                weight = Math.abs(jsonObject.get("weight").getAsInt());
            } catch (NumberFormatException e) {
                String weightString = jsonObject.get("weight").getAsString();
                weight = NAMED_WEIGHTS.getOrDefault(weightString, weight);
            }
        }

        // Extract loot table
        ResourceLocation table = null;
        if (jsonObject.has("table")) table = ResourceLocation.tryParse(jsonObject.get("table").getAsString());

        // Extract texture
        ResourceLocation texture = null;
        if (jsonObject.has("texture")) texture = ResourceLocation.tryParse(jsonObject.get("texture").getAsString());

        // Extract sources
        ArrayList<LootBallSource> sources = new ArrayList<>();
        if (jsonObject.has("sources")) sources = getSources(jsonObject);

        // Extract variants
        ArrayList<LootBallRecord> variants = new ArrayList<>();
        if (jsonObject.has("variants")) {
            JsonArray variantsArray = GsonHelper.getAsJsonArray(jsonObject, "variants");
            for (int i = 0; i < variantsArray.size(); i++) {
                if (variantsArray.get(i).isJsonObject()) {
                    JsonObject variantObject = variantsArray.get(i).getAsJsonObject();
                    LootBallRecord variantRecord = deserializeLootBall(variantObject);
                    variants.add(variantRecord);
                }
            }
        }

        return new LootBallRecord(name, weight, table, texture, sources, variants);
    }

    private static ArrayList<LootBallSource> getSources(JsonObject jsonObject) {
        ArrayList<LootBallSource> sources = new ArrayList<>();
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "sources");
        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.get(i).isJsonObject()) {
                JsonObject sourceObject = jsonArray.get(i).getAsJsonObject();

                // Extract source type
                String sourceType = "";
                if (sourceObject.has("type")) sourceType = sourceObject.get("type").getAsString();

                // Check if is valid source type
                if (!Arrays.asList(SOURCE_TYPES).contains(sourceType)) {
                    Cobbleloots.LOGGER.error("Error loading ball data (Invalid source type): {}", sourceType);
                    continue;
                }

                // Extract source biome (Optional)
                ResourceLocation sourceBiome = null;
                if (sourceObject.has("biome")) sourceBiome = ResourceLocation.tryParse(sourceObject.get("biome").getAsString());

                // Extract source height (Optional)
                LootBallHeight sourceHeight = new LootBallHeight("", "");
                if (sourceObject.has("height")) {
                    String[] sourceHeightString = sourceObject.get("height").getAsString().split(";", -1);
                    if (sourceHeightString.length == 2) {
                        try {
                            if (!sourceHeightString[0].isEmpty()) Integer.parseInt(sourceHeightString[0]);
                            if (!sourceHeightString[1].isEmpty()) Integer.parseInt(sourceHeightString[1]);
                            sourceHeight = new LootBallHeight(sourceHeightString[0], sourceHeightString[1]);
                        } catch (NumberFormatException e) {
                            Cobbleloots.LOGGER.error("Error loading ball data (Invalid source height): {}", e.getMessage());
                            Cobbleloots.LOGGER.warn("Please check your LootBalls datapack ball json files.");
                        }
                    }
                }

                // Extract source weight (Optional)
                int sourceWeight = 1;
                if (sourceObject.has("weight")) sourceWeight = sourceObject.get("weight").getAsInt();

                sources.add(new LootBallSource(sourceType, sourceBiome, sourceHeight, sourceWeight));
            }
        }
        return sources;
    }
}
