package dev.ripio.cobbleloots.data.debug;

import dev.ripio.cobbleloots.data.CobblelootsDataProvider;
import dev.ripio.cobbleloots.data.CobblelootsFilterEvaluator;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallSources;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsSourceFilter;
import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Diagnostic service providing calculation of spawn weights and detailed per-rule inspection
 * for loot balls at given world positions.
 */
public class CobblelootsDebugService {

  private static final ResourceLocation UNKNOWN_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "unknown");

  /**
   * Calculates candidate loot ball weights and probabilities for a specific source type and world location.
   *
   * @param level      Server level
   * @param pos        Target block position
   * @param sourceType Source type being evaluated (spawning, generation, fishing, archaeology)
   * @param player     Player context if available (used for hand item / tool checks)
   * @return A {@link WeightsReport} containing candidate loot balls sorted descending by weight
   */
  public static WeightsReport calculateWeights(ServerLevel level, BlockPos pos, CobblelootsSourceType sourceType,
      @Nullable ServerPlayer player) {
    ResourceLocation dimension = resolveDimension(level);
    ResourceLocation biome = resolveBiome(level, pos);

    if (level == null || pos == null || sourceType == null) {
      return new WeightsReport(sourceType, pos, dimension, biome, 0, List.of());
    }

    LevelChunk chunk = resolveChunk(level, pos);
    ItemStack tool = player != null ? player.getMainHandItem() : null;
    Map<ResourceLocation, Integer> matchedWeights = new HashMap<>();

    for (ResourceLocation id : CobblelootsDataProvider.getExistingLootBallIds()) {
      CobblelootsLootBallData data = CobblelootsDataProvider.getLootBallData(id);
      if (data == null) {
        continue;
      }

      CobblelootsLootBallSources sourcesData = data.getSources();
      if (sourcesData == null) {
        continue;
      }

      List<CobblelootsSourceFilter> sourceFilters = getFiltersForSourceType(sourcesData, sourceType);
      if (sourceFilters.isEmpty()) {
        continue;
      }

      for (CobblelootsSourceFilter filter : sourceFilters) {
        if (CobblelootsFilterEvaluator.testFilter(level, chunk, pos, filter, sourceType, player, tool)) {
          matchedWeights.put(id, filter.getWeight());
        }
      }
    }

    int totalWeight = 0;
    for (int weight : matchedWeights.values()) {
      totalWeight += weight;
    }

    List<LootBallWeightEntry> entries = new ArrayList<>(matchedWeights.size());
    for (Map.Entry<ResourceLocation, Integer> entry : matchedWeights.entrySet()) {
      int weight = entry.getValue();
      double percentage = totalWeight > 0 ? (weight * 100.0) / totalWeight : 0.0;
      entries.add(new LootBallWeightEntry(entry.getKey(), weight, percentage));
    }

    entries.sort(
        Comparator.comparingInt(LootBallWeightEntry::weight).reversed()
            .thenComparing(LootBallWeightEntry::id)
    );

    return new WeightsReport(sourceType, pos, dimension, biome, totalWeight, entries);
  }

  /**
   * Performs an exhaustive inspection of all rules for a specific loot ball at a given world position,
   * without early returns, detailing the individual pass/fail status of each condition.
   *
   * @param level      Server level
   * @param pos        Target block position
   * @param id         Target loot ball resource location identifier
   * @param sourceType Source type being evaluated
   * @param player     Player context if available
   * @return A {@link LootBallCheckReport} containing rule-by-rule diagnostic results
   */
  public static LootBallCheckReport inspectLootBall(ServerLevel level, BlockPos pos, ResourceLocation id,
      CobblelootsSourceType sourceType, @Nullable ServerPlayer player) {
    ResourceLocation dimension = resolveDimension(level);
    ResourceLocation biome = resolveBiome(level, pos);

    if (id == null || level == null || pos == null || sourceType == null) {
      return new LootBallCheckReport(id, sourceType, pos, dimension, biome, false, 0, List.of());
    }

    CobblelootsLootBallData data = CobblelootsDataProvider.getLootBallData(id);
    if (data == null) {
      return new LootBallCheckReport(id, sourceType, pos, dimension, biome, false, 0, List.of());
    }

    CobblelootsLootBallSources sourcesData = data.getSources();
    if (sourcesData == null) {
      return new LootBallCheckReport(id, sourceType, pos, dimension, biome, false, 0, List.of());
    }

    List<CobblelootsSourceFilter> sourceFilters = getFiltersForSourceType(sourcesData, sourceType);
    if (sourceFilters.isEmpty()) {
      return new LootBallCheckReport(id, sourceType, pos, dimension, biome, false, 0, List.of());
    }

    LevelChunk chunk = resolveChunk(level, pos);
    ItemStack tool = player != null ? player.getMainHandItem() : null;
    List<LootBallRuleReport> ruleReports = new ArrayList<>(sourceFilters.size());

    for (int i = 0; i < sourceFilters.size(); i++) {
      int ruleIndex = i + 1;
      CobblelootsSourceFilter filter = sourceFilters.get(i);
      LootBallRuleReport ruleReport = CobblelootsFilterEvaluator.evaluateRule(
          level, chunk, pos, ruleIndex, filter, sourceType, player, tool
      );
      ruleReports.add(ruleReport);
    }

    boolean eligible = ruleReports.stream().anyMatch(LootBallRuleReport::overallPassed);
    int matchedWeight = ruleReports.stream()
        .filter(LootBallRuleReport::overallPassed)
        .mapToInt(LootBallRuleReport::weight)
        .sum();

    return new LootBallCheckReport(id, sourceType, pos, dimension, biome, eligible, matchedWeight, ruleReports);
  }

  private static List<CobblelootsSourceFilter> getFiltersForSourceType(CobblelootsLootBallSources sources,
      CobblelootsSourceType sourceType) {
    if (sources == null || sourceType == null) {
      return List.of();
    }
    List<CobblelootsSourceFilter> filters = switch (sourceType) {
      case GENERATION -> sources.getGeneration();
      case SPAWNING -> sources.getSpawning();
      case FISHING -> sources.getFishing();
      case ARCHAEOLOGY -> sources.getArchaeology();
    };
    return filters != null ? filters : List.of();
  }

  private static ResourceLocation resolveDimension(@Nullable ServerLevel level) {
    if (level == null) {
      return UNKNOWN_LOCATION;
    }
    return level.dimension().location();
  }

  private static ResourceLocation resolveBiome(@Nullable ServerLevel level, @Nullable BlockPos pos) {
    if (level == null || pos == null) {
      return UNKNOWN_LOCATION;
    }
    return level.getBiome(pos).unwrapKey()
        .map(ResourceKey::location)
        .orElse(UNKNOWN_LOCATION);
  }

  @Nullable
  private static LevelChunk resolveChunk(ServerLevel level, BlockPos pos) {
    LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
    if (chunk == null) {
      chunk = level.getChunkAt(pos);
    }
    return chunk;
  }
}
