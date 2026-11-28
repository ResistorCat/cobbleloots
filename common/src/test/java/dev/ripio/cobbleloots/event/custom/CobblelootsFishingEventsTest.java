package dev.ripio.cobbleloots.event.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.ripio.cobbleloots.config.CobblelootsConfig;
import dev.ripio.cobbleloots.data.CobblelootsCodecs;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsSourceFilter;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CobblelootsFishingEventsTest {

    @BeforeAll
    static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    @DisplayName("Default fishing config values match calibrated progression")
    void testDefaultFishingConfigValues() {
        assertEquals(0.05F, CobblelootsConfig.fishing_chance, 0.0001F);
        assertEquals(1.4F, CobblelootsConfig.fishing_luck_of_the_sea_multiplier, 0.0001F);
    }

    @Test
    @DisplayName("calculateFishingChance scales smoothly across Luck of the Sea levels 0-3")
    void testLuckOfTheSeaProgression() {
        float base = CobblelootsConfig.fishing_chance;
        float mult = CobblelootsConfig.fishing_luck_of_the_sea_multiplier;

        assertEquals(0.05F, CobblelootsFishingEvents.calculateFishingChance(base, mult, 0), 0.0001F);
        assertEquals(0.07F, CobblelootsFishingEvents.calculateFishingChance(base, mult, 1), 0.0001F);
        assertEquals(0.098F, CobblelootsFishingEvents.calculateFishingChance(base, mult, 2), 0.0001F);
        assertEquals(0.1372F, CobblelootsFishingEvents.calculateFishingChance(base, mult, 3), 0.0001F);
    }

    @Test
    @DisplayName("calculateFishingChance clamps to [0.0, 1.0] and handles edge cases safely")
    void testCalculateFishingChanceEdgeCases() {
        assertEquals(0.0F, CobblelootsFishingEvents.calculateFishingChance(0.0F, 1.4F, 3), 0.0001F);
        assertEquals(0.0F, CobblelootsFishingEvents.calculateFishingChance(-0.1F, 1.4F, 2), 0.0001F);
        assertEquals(1.0F, CobblelootsFishingEvents.calculateFishingChance(0.5F, 2.0F, 5), 0.0001F);
        assertEquals(1.0F, CobblelootsFishingEvents.calculateFishingChance(1.5F, 1.4F, 0), 0.0001F);
        assertEquals(0.05F, CobblelootsFishingEvents.calculateFishingChance(0.05F, 1.0F, 3), 0.0001F);
        assertEquals(0.05F, CobblelootsFishingEvents.calculateFishingChance(0.05F, 0.0F, 3), 0.0001F);
        assertEquals(0.05F, CobblelootsFishingEvents.calculateFishingChance(0.05F, -1.4F, 3), 0.0001F);
        assertEquals(0.05F, CobblelootsFishingEvents.calculateFishingChance(0.05F, 1.4F, -1), 0.0001F);
    }

    @Test
    @DisplayName("Aquatic fishing loot balls parse cleanly and prioritize marine/fluvial catches")
    void testAquaticFishingLootBallDefinitions() throws Exception {
        CobblelootsLootBallData dive = loadLootBallData("dive");
        CobblelootsLootBallData lure = loadLootBallData("lure");
        CobblelootsLootBallData net = loadLootBallData("net");
        CobblelootsLootBallData poke = loadLootBallData("poke");

        int diveDeepOceanWeight = maxMatchingWeight(dive.getSources().getFishing(),
                Set.of("#cobblemon:is_deep_ocean", "#cobblemon:is_ocean"), "cobblemon:poke_rod");
        int lureDeepOceanWeight = maxMatchingWeight(lure.getSources().getFishing(),
                Set.of("#cobblemon:is_deep_ocean", "#cobblemon:is_ocean"), "cobblemon:poke_rod");
        int netDeepOceanWeight = maxMatchingWeight(net.getSources().getFishing(),
                Set.of("#cobblemon:is_deep_ocean", "#cobblemon:is_ocean"), "cobblemon:poke_rod");
        int pokePokeRodWeight = maxMatchingWeight(poke.getSources().getFishing(),
                Set.of("#cobblemon:is_deep_ocean", "#cobblemon:is_ocean"), "cobblemon:poke_rod");

        assertTrue(diveDeepOceanWeight + lureDeepOceanWeight + netDeepOceanWeight > pokePokeRodWeight * 2,
                "Aquatic loot balls should strongly outweigh plain Poke Ball in deep ocean");

        int lureRiverWithLureRod = maxMatchingWeight(lure.getSources().getFishing(),
                Set.of("#cobblemon:is_river"), "cobblemon:lure_rod");
        int netRiverWithNetRod = maxMatchingWeight(net.getSources().getFishing(),
                Set.of("#cobblemon:is_river"), "cobblemon:net_rod");
        int diveOceanWithDiveRod = maxMatchingWeight(dive.getSources().getFishing(),
                Set.of("#cobblemon:is_ocean"), "cobblemon:dive_rod");

        assertTrue(lureRiverWithLureRod > pokePokeRodWeight, "Lure Ball should be obtainable and favored in rivers with Lure Rod");
        assertTrue(netRiverWithNetRod > pokePokeRodWeight, "Net Ball should be favored in rivers with Net Rod");
        assertTrue(diveOceanWithDiveRod > pokePokeRodWeight, "Dive Ball should be favored in oceans with Dive Rod");
    }

    private static CobblelootsLootBallData loadLootBallData(String name) throws Exception {
        String resourcePath = "/data/cobbleloots/loot_ball/" + name + ".json";
        try (InputStream stream = CobblelootsFishingEventsTest.class.getResourceAsStream(resourcePath)) {
            assertNotNull(stream, "Missing resource: " + resourcePath);
            JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            DataResult<CobblelootsLootBallData> result = CobblelootsCodecs.LOOT_BALL_DATA_CODEC.parse(JsonOps.INSTANCE, json);
            assertTrue(result.result().isPresent(), "Failed to parse " + resourcePath + ": " + result.error());
            return result.result().get();
        }
    }

    private static int maxMatchingWeight(List<CobblelootsSourceFilter> rules, Set<String> activeBiomeTags, String rodId) {
        ResourceLocation rodLoc = ResourceLocation.parse(rodId);
        int best = 0;
        for (CobblelootsSourceFilter rule : rules) {
            if (rule.getBiome() != null && !rule.getBiome().isEmpty()) {
                boolean biomeMatched = rule.getBiome().entries().stream()
                        .anyMatch(entry -> activeBiomeTags.contains(entry.id()));
                if (!biomeMatched) {
                    continue;
                }
            }
            if (rule.getPokeRod() != null && rule.getPokeRod().getRods() != null && !rule.getPokeRod().getRods().isEmpty()) {
                if (!rule.getPokeRod().getRods().contains(rodLoc)) {
                    continue;
                }
            }
            best = Math.max(best, rule.getWeight());
        }
        return best;
    }
}
