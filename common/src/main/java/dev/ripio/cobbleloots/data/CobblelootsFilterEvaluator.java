package dev.ripio.cobbleloots.data;

import dev.ripio.cobbleloots.config.CobblelootsConfig;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsBlockFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsDateFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsLightFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsPokeRodFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsPositionFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobbleloootsBiomeEntry;
import dev.ripio.cobbleloots.data.custom.filter.CobbleloootsBiomeFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsSourceFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsStructureEntry;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsStructureFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsTimeFilter;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsWeatherFilter;
import dev.ripio.cobbleloots.data.debug.FilterCheckResult;
import dev.ripio.cobbleloots.data.debug.LootBallRuleReport;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized evaluator for loot ball source filter conditions.
 *
 * <p>Provides fast boolean checks with early returns for runtime spawning via {@link #testFilter},
 * as well as comprehensive diagnostics without early returns via {@link #evaluateRule}.</p>
 */
public class CobblelootsFilterEvaluator {

  /**
   * Fast evaluation with early returns for runtime spawning or generation.
   *
   * @param level Server level
   * @param chunk Chunk where the position is located
   * @param pos Block position to test
   * @param source Source filter definition
   * @param sourceType Source type (spawning, generation, fishing, archaeology)
   * @return true if all filter conditions pass, false otherwise
   */
  public static boolean testFilter(ServerLevel level, LevelChunk chunk, BlockPos pos,
      CobblelootsSourceFilter source, CobblelootsSourceType sourceType) {
    return testFilter(level, chunk, pos, source, sourceType, null, null);
  }

  /**
   * Fast evaluation with early returns for runtime spawning, fishing, or generation.
   *
   * @param level Server level
   * @param chunk Chunk where the position is located
   * @param pos Block position to test
   * @param source Source filter definition
   * @param sourceType Source type (spawning, generation, fishing, archaeology)
   * @param player Player context if available
   * @param tool Item stack / fishing rod if available
   * @return true if all filter conditions pass, false otherwise
   */
  public static boolean testFilter(ServerLevel level, LevelChunk chunk, BlockPos pos,
      CobblelootsSourceFilter source, CobblelootsSourceType sourceType,
      @Nullable ServerPlayer player, @Nullable ItemStack tool) {
    if (level == null || pos == null || source == null) {
      return false;
    }

    if (isDimensionDisabled(level, sourceType)) {
      return false;
    }

    if (!checkStructureFilter(level, pos, source.getStructure())) {
      return false;
    }

    if (!checkBiomeFilter(level, pos, source.getBiome())) {
      return false;
    }

    if (!checkDimensionFilter(level, source.getDimension())) {
      return false;
    }

    if (!checkBlockFilter(level, chunk, pos, source.getBlock())) {
      return false;
    }

    if (!checkFluidFilter(level, chunk, pos, source.getFluid())) {
      return false;
    }

    if (!checkPositionFilter(pos, source.getPosition())) {
      return false;
    }

    if (sourceType == CobblelootsSourceType.SPAWNING && !checkLightFilter(level, pos, source.getLight())) {
      return false;
    }

    if (!checkTimeFilter(level, source.getTime())) {
      return false;
    }

    if (!checkWeatherFilter(level, source.getWeather())) {
      return false;
    }

    if (!checkDateFilter(source.getDate())) {
      return false;
    }

    if (sourceType == CobblelootsSourceType.FISHING && !checkPokeRodFilter(player, tool, source.getPokeRod())) {
      return false;
    }

    return true;
  }

  /**
   * Evaluates all configured filter conditions for a source rule (without early returns)
   * and collects diagnostic results for each active filter.
   *
   * @param level Server level
   * @param chunk Chunk where the position is located
   * @param pos Block position to test
   * @param ruleIndex Index of the rule within the source list
   * @param source Source filter definition
   * @param sourceType Source type being evaluated
   * @return Rule diagnostic report
   */
  public static LootBallRuleReport evaluateRule(ServerLevel level, LevelChunk chunk, BlockPos pos, int ruleIndex,
      CobblelootsSourceFilter source, CobblelootsSourceType sourceType) {
    return evaluateRule(level, chunk, pos, ruleIndex, source, sourceType, null, null);
  }

  /**
   * Evaluates all configured filter conditions for a source rule (without early returns)
   * and collects diagnostic results for each active filter.
   *
   * @param level Server level
   * @param chunk Chunk where the position is located
   * @param pos Block position to test
   * @param ruleIndex Index of the rule within the source list
   * @param source Source filter definition
   * @param sourceType Source type being evaluated
   * @param player Player context if available
   * @param tool Item stack / fishing rod if available
   * @return Rule diagnostic report
   */
  public static LootBallRuleReport evaluateRule(ServerLevel level, LevelChunk chunk, BlockPos pos, int ruleIndex,
      CobblelootsSourceFilter source, CobblelootsSourceType sourceType,
      @Nullable ServerPlayer player, @Nullable ItemStack tool) {
    if (source == null) {
      return new LootBallRuleReport(ruleIndex, 0, false, List.of());
    }

    if (chunk == null && level != null && pos != null) {
      chunk = level.getChunkAt(pos);
    }

    List<FilterCheckResult> filterResults = new ArrayList<>();

    // 1. dimension_config
    boolean dimConfigPassed = !isDimensionDisabled(level, sourceType);
    ResourceLocation dimId = level.dimension().location();
    filterResults.add(new FilterCheckResult(
        "dimension_config",
        dimConfigPassed,
        "allowed in config",
        dimConfigPassed ? dimId + " (allowed)" : dimId + " (disabled in config)"
    ));

    // 2. dimension
    if (source.getDimension() != null && !source.getDimension().isEmpty()) {
      ResourceLocation currentDim = level.dimension().location();
      boolean dimPassed = checkDimensionFilter(level, source.getDimension());
      filterResults.add(new FilterCheckResult(
          "dimension",
          dimPassed,
          source.getDimension().toString(),
          dimPassed ? currentDim + " (allowed)" : currentDim.toString()
      ));
    }

    // 3. biome
    if (source.getBiome() != null && !source.getBiome().isEmpty()) {
      Holder<Biome> biomeHolder = level.getBiome(pos);
      ResourceLocation currentBiomeId = biomeHolder.unwrapKey()
          .map(ResourceKey::location)
          .orElse(ResourceLocation.parse("cobbleloots:unknown"));
      boolean biomePassed = checkBiomeFilter(level, pos, source.getBiome());
      List<String> expectedEntries = source.getBiome().entries().stream().map(CobbleloootsBiomeEntry::id).toList();
      filterResults.add(new FilterCheckResult(
          "biome",
          biomePassed,
          expectedEntries.toString(),
          biomePassed ? currentBiomeId + " (matches filter)" : currentBiomeId.toString()
      ));
    }

    // 4. structure
    if (source.getStructure() != null && !source.getStructure().isEmpty()) {
      boolean structurePassed = checkStructureFilter(level, pos, source.getStructure());
      List<String> expectedStructures = source.getStructure().entries().stream().map(CobblelootsStructureEntry::id).toList();
      filterResults.add(new FilterCheckResult(
          "structure",
          structurePassed,
          expectedStructures.toString(),
          structurePassed ? "inside structure (matches filter)" : "none"
      ));
    }

    // 5. block
    if (source.getBlock() != null) {
      CobblelootsBlockFilter blockFilter = source.getBlock();
      BlockPos basePos = pos.below();
      BlockState baseBlockState;
      BlockState spawnBlockState;

      if (chunk != null) {
        LevelChunkSection[] sections = chunk.getSections();
        int baseIndex = chunk.getSectionIndex(basePos.getY());
        int spawnIndex = chunk.getSectionIndex(pos.getY());
        baseBlockState = (baseIndex >= 0 && baseIndex < sections.length && sections[baseIndex] != null)
            ? sections[baseIndex].getBlockState(basePos.getX() & 15, basePos.getY() & 15, basePos.getZ() & 15)
            : level.getBlockState(basePos);
        spawnBlockState = (spawnIndex >= 0 && spawnIndex < sections.length && sections[spawnIndex] != null)
            ? sections[spawnIndex].getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15)
            : level.getBlockState(pos);
      } else {
        baseBlockState = level.getBlockState(basePos);
        spawnBlockState = level.getBlockState(pos);
      }

      boolean spawnOk = blockFilter.isSpawnable(spawnBlockState);
      boolean baseOk = blockFilter.isBase(baseBlockState);
      boolean blockPassed = spawnOk && baseOk;

      ResourceLocation baseBlockId = BuiltInRegistries.BLOCK.getKey(baseBlockState.getBlock());
      ResourceLocation spawnBlockId = BuiltInRegistries.BLOCK.getKey(spawnBlockState.getBlock());
      boolean emptySpawn = blockFilter.getSpawn().equals(CobblelootsDefinitions.EMPTY_BLOCK_TAG);

      String expected;
      String actual;

      if (blockPassed) {
        expected = formatBlockFilterExpected(blockFilter);
        actual = emptySpawn ? "base: " + baseBlockId : "base: " + baseBlockId + ", spawn: " + spawnBlockId;
      } else if (!baseOk && spawnOk) {
        expected = formatTagExpected(blockFilter.getBase());
        actual = "base: " + baseBlockId;
      } else if (!spawnOk && baseOk) {
        expected = formatTagExpected(blockFilter.getSpawn());
        actual = "spawn: " + spawnBlockId;
      } else {
        expected = formatBlockFilterExpected(blockFilter);
        actual = "base: " + baseBlockId + ", spawn: " + spawnBlockId;
      }

      filterResults.add(new FilterCheckResult("block", blockPassed, expected, actual));
    }

    // 6. fluid
    if (source.getFluid() != null && !source.getFluid().equals(CobblelootsDefinitions.EMPTY_FLUID_TAG)) {
      TagKey<Fluid> fluidTag = source.getFluid();
      FluidState fluidState;
      if (chunk != null) {
        int sectionIndex = chunk.getSectionIndex(pos.getY());
        LevelChunkSection[] sections = chunk.getSections();
        fluidState = (sectionIndex >= 0 && sectionIndex < sections.length && sections[sectionIndex] != null)
            ? sections[sectionIndex].getFluidState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15)
            : level.getFluidState(pos);
      } else {
        fluidState = level.getFluidState(pos);
      }

      boolean fluidPassed = fluidState.is(fluidTag);
      ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluidState.getType());
      filterResults.add(new FilterCheckResult(
          "fluid",
          fluidPassed,
          "[#" + fluidTag.location() + "]",
          fluidPassed ? fluidId + " (matches [#" + fluidTag.location() + "])" : fluidId.toString()
      ));
    }

    // 7. position
    if (source.getPosition() != null) {
      CobblelootsPositionFilter positionFilter = source.getPosition();
      boolean posPassed = checkPositionFilter(pos, positionFilter);
      String posExpected = String.format("x:[%d..%d], y:[%d..%d], z:[%d..%d]",
          positionFilter.getX().getMin(), positionFilter.getX().getMax(),
          positionFilter.getY().getMin(), positionFilter.getY().getMax(),
          positionFilter.getZ().getMin(), positionFilter.getZ().getMax());
      String posActual = String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
      filterResults.add(new FilterCheckResult(
          "position",
          posPassed,
          posExpected,
          posPassed ? posActual + " (in range)" : posActual
      ));
    }

    // 8. light (only for SPAWNING)
    if (sourceType == CobblelootsSourceType.SPAWNING && source.getLight() != null) {
      CobblelootsLightFilter lightFilter = source.getLight();
      int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
      int skyLight = level.getBrightness(LightLayer.SKY, pos);
      boolean lightPassed = checkLightFilter(level, pos, lightFilter);
      String lightExpected = String.format("block:[%d..%d], sky:[%d..%d]",
          lightFilter.getBlock().getMin(), lightFilter.getBlock().getMax(),
          lightFilter.getSky().getMin(), lightFilter.getSky().getMax());
      String lightActual = String.format("block:%d, sky:%d", blockLight, skyLight);
      filterResults.add(new FilterCheckResult(
          "light",
          lightPassed,
          lightExpected,
          lightPassed ? lightActual + " (in range)" : lightActual
      ));
    }

    // 9. time
    if (source.getTime() != null) {
      CobblelootsTimeFilter timeFilter = source.getTime();
      long currentTime = timeFilter.getPeriod() > 0 ? level.getDayTime() % timeFilter.getPeriod() : level.getDayTime();
      boolean timePassed = checkTimeFilter(level, timeFilter);
      String timeExpected = String.format("[%d..%d]%s",
          timeFilter.getValue().getMin(), timeFilter.getValue().getMax(),
          timeFilter.getPeriod() > 0 ? " (period " + timeFilter.getPeriod() + ")" : "");
      String timeActual = String.valueOf(currentTime);
      filterResults.add(new FilterCheckResult(
          "time",
          timePassed,
          timeExpected,
          timePassed ? timeActual + " (in range)" : timeActual
      ));
    }

    // 10. weather
    if (source.getWeather() != null) {
      CobblelootsWeatherFilter weatherFilter = source.getWeather();
      boolean isRaining = level.isRaining();
      boolean isThundering = level.isThundering();
      boolean weatherPassed = checkWeatherFilter(level, weatherFilter);
      String currentWeather = isThundering ? "thundering" : (isRaining ? "raining" : "clear");
      List<String> allowedWeather = new ArrayList<>();
      if (weatherFilter.getClear()) allowedWeather.add("clear");
      if (weatherFilter.getRain()) allowedWeather.add("raining");
      if (weatherFilter.getThunder()) allowedWeather.add("thundering");
      filterResults.add(new FilterCheckResult(
          "weather",
          weatherPassed,
          allowedWeather.toString(),
          weatherPassed ? currentWeather + " (matches filter)" : currentWeather
      ));
    }

    // 11. date
    if (source.getDate() != null) {
      CobblelootsDateFilter dateFilter = source.getDate();
      boolean datePassed = checkDateFilter(dateFilter);
      String currentDate = MonthDay.from(LocalDate.now()).format(DateTimeFormatter.ofPattern("MM-dd"));
      String from = (dateFilter.getFrom() != null && !dateFilter.getFrom().isEmpty()) ? dateFilter.getFrom() : "*";
      String to = (dateFilter.getTo() != null && !dateFilter.getTo().isEmpty()) ? dateFilter.getTo() : "*";
      String dateExpected = from + " to " + to;
      filterResults.add(new FilterCheckResult(
          "date",
          datePassed,
          dateExpected,
          datePassed ? currentDate + " (in range)" : currentDate
      ));
    }

    // 12. poke_rod (only for FISHING)
    if (sourceType == CobblelootsSourceType.FISHING && source.getPokeRod() != null) {
      CobblelootsPokeRodFilter pokeRodFilter = source.getPokeRod();
      boolean pokeRodPassed = checkPokeRodFilter(player, tool, pokeRodFilter);
      String currentTool = (tool != null && !tool.isEmpty()) ? BuiltInRegistries.ITEM.getKey(tool.getItem()).toString() : "none";
      List<ResourceLocation> expectedRods = pokeRodFilter.getRods();
      String rodExpected = expectedRods != null ? expectedRods.toString() : "any";
      filterResults.add(new FilterCheckResult(
          "poke_rod",
          pokeRodPassed,
          rodExpected,
          pokeRodPassed ? currentTool + " (matches filter)" : currentTool
      ));
    }

    boolean overallPassed = filterResults.stream().allMatch(FilterCheckResult::passed);
    return new LootBallRuleReport(ruleIndex, source.getWeight(), overallPassed, filterResults);
  }

  /**
   * Checks if the dimension is disabled in configuration for the specified source type.
   */
  public static boolean isDimensionDisabled(ServerLevel level, CobblelootsSourceType sourceType) {
    if (level == null || sourceType == null) {
      return false;
    }
    ResourceLocation dimensionId = level.dimension().location();

    return switch (sourceType) {
      case GENERATION -> CobblelootsConfig.generation_disabled_dimensions.contains(dimensionId);
      case SPAWNING -> CobblelootsConfig.spawning_disabled_dimensions.contains(dimensionId);
      case FISHING -> CobblelootsConfig.fishing_disabled_dimensions.contains(dimensionId);
      case ARCHAEOLOGY -> CobblelootsConfig.archaeology_disabled_dimensions.contains(dimensionId);
      default -> false;
    };
  }

  public static boolean checkStructureFilter(ServerLevel level, BlockPos pos,
      @Nullable CobblelootsStructureFilter structureFilter) {
    if (structureFilter == null || structureFilter.isEmpty()) {
      return true;
    }
    return structureFilter.test(level, pos);
  }

  public static boolean checkBiomeFilter(ServerLevel level, BlockPos pos,
      @Nullable CobbleloootsBiomeFilter biomeFilter) {
    if (biomeFilter == null || biomeFilter.isEmpty()) {
      return true;
    }
    return biomeFilter.test(level, pos);
  }

  public static boolean checkDimensionFilter(ServerLevel level, @Nullable List<ResourceLocation> dimensionIds) {
    if (dimensionIds == null || dimensionIds.isEmpty()) {
      return true;
    }
    return dimensionIds.contains(level.dimension().location());
  }

  public static boolean checkBlockFilter(ServerLevel level, @Nullable LevelChunk chunk, BlockPos spawnPos,
      @Nullable CobblelootsBlockFilter blockFilter) {
    if (blockFilter == null) {
      return true;
    }

    LevelChunkSection[] sections = chunk != null ? chunk.getSections() : null;
    BlockPos basePos = spawnPos.below();
    BlockState baseBlockState;
    BlockState spawnBlockState;

    if (chunk != null && sections != null) {
      int baseSectionIndex = chunk.getSectionIndex(basePos.getY());
      if (baseSectionIndex >= 0 && baseSectionIndex < sections.length && sections[baseSectionIndex] != null) {
        baseBlockState = sections[baseSectionIndex].getBlockState(basePos.getX() & 15, basePos.getY() & 15,
            basePos.getZ() & 15);
      } else {
        baseBlockState = level.getBlockState(basePos);
      }

      int spawnSectionIndex = chunk.getSectionIndex(spawnPos.getY());
      if (spawnSectionIndex >= 0 && spawnSectionIndex < sections.length && sections[spawnSectionIndex] != null) {
        spawnBlockState = sections[spawnSectionIndex].getBlockState(spawnPos.getX() & 15, spawnPos.getY() & 15,
            spawnPos.getZ() & 15);
      } else {
        spawnBlockState = level.getBlockState(spawnPos);
      }
    } else {
      baseBlockState = level.getBlockState(basePos);
      spawnBlockState = level.getBlockState(spawnPos);
    }

    return blockFilter.isSpawnable(spawnBlockState) && blockFilter.isBase(baseBlockState);
  }

  public static boolean checkFluidFilter(ServerLevel level, @Nullable LevelChunk chunk, BlockPos pos,
      @Nullable TagKey<Fluid> fluidTag) {
    if (fluidTag == null || fluidTag.equals(CobblelootsDefinitions.EMPTY_FLUID_TAG)) {
      return true;
    }

    if (chunk != null) {
      int sectionIndex = chunk.getSectionIndex(pos.getY());
      LevelChunkSection[] sections = chunk.getSections();
      if (sectionIndex >= 0 && sectionIndex < sections.length && sections[sectionIndex] != null) {
        FluidState fluidState = sections[sectionIndex].getFluidState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        return fluidState.is(fluidTag);
      }
    }

    return level.getFluidState(pos).is(fluidTag);
  }

  public static boolean checkPositionFilter(BlockPos pos, @Nullable CobblelootsPositionFilter positionFilter) {
    if (positionFilter == null) {
      return true;
    }
    return positionFilter.isInRange(pos.getX(), pos.getY(), pos.getZ());
  }

  public static boolean checkLightFilter(ServerLevel level, BlockPos pos,
      @Nullable CobblelootsLightFilter lightFilter) {
    if (lightFilter == null) {
      return true;
    }
    int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
    int skyLight = level.getBrightness(LightLayer.SKY, pos);
    return lightFilter.isInRange(blockLight, skyLight);
  }

  public static boolean checkTimeFilter(ServerLevel level, @Nullable CobblelootsTimeFilter timeFilter) {
    if (timeFilter == null) {
      return true;
    }
    long currentTime = level.getDayTime();
    if (timeFilter.getPeriod() > 0) {
      currentTime = level.getDayTime() % timeFilter.getPeriod();
    }
    return timeFilter.getValue().isInRange((int) currentTime);
  }

  public static boolean checkWeatherFilter(ServerLevel level, @Nullable CobblelootsWeatherFilter weatherFilter) {
    if (weatherFilter == null) {
      return true;
    }
    return weatherFilter.isValid(level.isRaining(), level.isThundering());
  }

  public static boolean checkDateFilter(@Nullable CobblelootsDateFilter dateFilter) {
    if (dateFilter == null) {
      return true;
    }
    return dateFilter.test();
  }

  public static boolean checkPokeRodFilter(@Nullable ServerPlayer player, @Nullable ItemStack tool,
      @Nullable CobblelootsPokeRodFilter filter) {
    if (filter == null) {
      return true;
    }
    return filter.test(null, null, null, player, tool);
  }

  private static String formatTagExpected(TagKey<Block> tag) {
    if (tag == null || tag.equals(CobblelootsDefinitions.EMPTY_BLOCK_TAG)) {
      return "any";
    }
    return "[#" + tag.location() + "]";
  }

  private static String formatBlockFilterExpected(CobblelootsBlockFilter filter) {
    return "base: " + formatTagExpected(filter.getBase()) + ", spawn: " + formatTagExpected(filter.getSpawn());
  }
}
