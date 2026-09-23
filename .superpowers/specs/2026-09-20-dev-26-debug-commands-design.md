# Design Specification: DEV-26 Debug Commands (`/cobbleloots debug weights`, `check`)

## 1. Overview & Context

During mod development, modpack creation, and datapack authoring, creators and developers frequently need to diagnose why a Loot Ball does or does not appear at a specific coordinate or what the relative spawn probabilities are in a given biome.

This specification defines the implementation for two diagnostic commands under `/cobbleloots debug`:
1. `/cobbleloots debug weights [source_type] [pos]`: Evaluates and displays a ranked table of all candidate Loot Balls for the target position and biome, along with individual weights and percentage chances relative to the total.
2. `/cobbleloots debug check <id> [source_type] [pos]`: Evaluates all filter criteria for a specific Loot Ball at the target position, outputting a step-by-step diagnostic breakdown showing passed and failed conditions with visual indicators.

---

## 2. Architecture & Components

### 2.1 Multi-Loader Command Registration
The command tree is defined once in `common/` using Mojang's Brigadier API and registered via platform-specific entry points:
- **Common Definition**: `dev.ripio.cobbleloots.command.CobblelootsCommands.register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)`
  - Root command: `/cobbleloots` requiring permission level 2 (`source.hasPermission(2)`).
- **Fabric Hook**: `dev.ripio.cobbleloots.fabric.event.CobblelootsEvents` registers via `CommandRegistrationCallback.EVENT`.
- **NeoForge Hook**: `dev.ripio.cobbleloots.neoforge.event.CobblelootsEvents` registers via `@SubscribeEvent public static void onRegisterCommands(RegisterCommandsEvent event)`.

### 2.2 Domain & Diagnostic Model
Located in `dev.ripio.cobbleloots.data.debug`:
- `FilterCheckResult`:
  ```java
  public record FilterCheckResult(
      String filterKey,
      boolean passed,
      String expected,
      String actual
  ) {}
  ```
- `LootBallRuleReport`:
  ```java
  public record LootBallRuleReport(
      int ruleIndex,
      int weight,
      boolean overallPassed,
      List<FilterCheckResult> filterResults
  ) {}
  ```
- `LootBallCheckReport`:
  ```java
  public record LootBallCheckReport(
      ResourceLocation id,
      CobblelootsSourceType sourceType,
      BlockPos pos,
      ResourceLocation dimension,
      ResourceLocation biome,
      boolean eligible,
      int totalWeight,
      List<LootBallRuleReport> rules
  ) {}
  ```
- `LootBallWeightEntry`:
  ```java
  public record LootBallWeightEntry(
      ResourceLocation id,
      int weight,
      double percentage
  ) {}
  ```
- `WeightsReport`:
  ```java
  public record WeightsReport(
      CobblelootsSourceType sourceType,
      BlockPos pos,
      ResourceLocation dimension,
      ResourceLocation biome,
      int totalWeight,
      List<LootBallWeightEntry> entries
  ) {}
  ```

### 2.3 Centralized Filter Evaluator (`CobblelootsFilterEvaluator`)
Extracted and refactored from `CobblelootsDataProvider` to ensure 100% logic parity between runtime spawning and command diagnostics without code duplication:
- `testFilter(...)`: Fast boolean evaluation with early returns for runtime performance.
- `evaluateRuleFilters(...)`: Exhaustive evaluation that evaluates all filters for a rule and builds a `List<FilterCheckResult>`.
- Evaluated filter categories:
  1. `dimension_config`: Checks if dimension is disabled in `CobblelootsConfig` (`generation_disabled_dimensions`, `spawning_disabled_dimensions`, etc.).
  2. `dimension`: Target dimension list from `CobblelootsSourceFilter.getDimension()`.
  3. `biome`: Biome tag or ID check via `CobbleloootsBiomeFilter.test()`.
  4. `structure`: Structure check via `CobblelootsStructureFilter.test()`.
  5. `block`: Base block underneath (`pos.below()`) and spawn block at `pos`.
  6. `fluid`: Fluid tag at `pos`.
  7. `position`: Coordinates X, Y, Z ranges via `CobblelootsPositionFilter.isInRange()`.
  8. `light`: Block light and sky light levels (active for `SPAWNING`).
  9. `time`: World daytime / modulo cycle.
  10. `weather`: Raining / thundering states.
  11. `date`: Real-world calendar date range.
  12. `poke_rod`: Held rod / player conditions (active for `FISHING`).

### 2.4 Diagnostic Service (`CobblelootsDebugService`)
Coordinates diagnostic data retrieval:
- `calculateWeights(ServerLevel level, BlockPos pos, CobblelootsSourceType sourceType, @Nullable ServerPlayer player)`:
  - Iterates all registered loot ball IDs.
  - Verifies candidate rules using `CobblelootsFilterEvaluator`.
  - Calculates cumulative weight and percentage shares:
    $$\text{percentage} = \frac{\text{weight}}{\text{totalWeight}} \times 100$$
  - Returns `WeightsReport` sorted descending by weight.
- `inspectLootBall(ServerLevel level, BlockPos pos, ResourceLocation id, CobblelootsSourceType sourceType, @Nullable ServerPlayer player)`:
  - Validates loot ball existence.
  - Evaluates each rule under the requested `sourceType`.
  - Compiles full `LootBallCheckReport`.

---

## 3. Command Grammar & Autocompletion

### 3.1 Grammar
```text
/cobbleloots debug weights [<source_type>] [<pos>]
/cobbleloots debug check <id> [<source_type>] [<pos>]
```

### 3.2 Argument Definitions
1. `<id>`: `ResourceLocationArgument.id()` with `SuggestionProvider` querying `CobblelootsDataProvider.getExistingLootBallIds()`.
2. `[<source_type>]`: String argument with suggestions: `spawning`, `generation`, `fishing`, `archaeology`. Default: `spawning`.
3. `[<pos>]`: `BlockPosArgument.blockPos()`. Default: executor's block position (`BlockPos.containing(source.getPosition())`).

---

## 4. Visual Output & Chat Feedback

### 4.1 `/cobbleloots debug weights`
- **Header**:
  `--- [Cobbleloots] Spawn Weights (spawning) @ [X, Y, Z] (biome_id) ---`
- **No Candidates**:
  `No candidate Loot Balls found at this position.`
- **Entries**:
  `• cobbleloots:poke_ball | Weight: 20 | 40.0%`
  `• cobbleloots:great_ball | Weight: 15 | 30.0%`
  `• cobbleloots:ultra_ball | Weight: 10 | 20.0%`
  `• cobbleloots:master_ball | Weight: 5 | 10.0%`
- **Footer**:
  `Total candidates: 4 | Total weight: 50`

### 4.2 `/cobbleloots debug check <id>`
- **Header**:
  `--- [Cobbleloots] Check: <id> (<source_type>) @ [X, Y, Z] ---`
- **Errors / Special States**:
  - Unknown ID: `Error: Loot Ball '<id>' does not exist.` (Red).
  - No rules for source type: `Loot Ball '<id>' has no rules configured for source type '<source_type>'.` (Yellow).
- **Per-Rule Output**:
  `Rule #1 (Weight: 20) -> [PASS]` (Green) or `[FAIL]` (Red)
  `  ✔ Dimension: minecraft:overworld (allowed)`
  `  ✔ Biome: minecraft:plains (matches filter)`
  `  ✖ Block: base: minecraft:sand (expected: [#minecraft:dirt])`
- **Summary**:
  `Result: ELIGIBLE (Weight: 20)` (Green) or `Result: INELIGIBLE` (Red).

---

## 5. Localization & Translation Keys

Translation files to update:
- `common/src/main/resources/assets/cobbleloots/lang/en_us.json`
- `common/src/main/resources/assets/cobbleloots/lang/es_ec.json`
- `common/src/main/resources/assets/cobbleloots/lang/pt_br.json`

Translation key namespace: `command.cobbleloots.debug.*`:
- `weights.header`: `--- [Cobbleloots] Weights (%s) @ [%d, %d, %d] (%s) ---`
- `weights.entry`: `• %s | Weight: %d | %.1f%%`
- `weights.empty`: `No candidate Loot Balls found for %s at this position.`
- `weights.total`: `Total candidates: %d | Total weight: %d`
- `check.header`: `--- [Cobbleloots] Check: %s (%s) @ [%d, %d, %d] ---`
- `check.not_found`: `Loot Ball '%s' does not exist.`
- `check.no_rules`: `Loot Ball '%s' has no rules for source type '%s'.`
- `check.rule_header`: `Rule #%d (Weight: %d) -> %s`
- `check.filter_passed`: `  ✔ %s: %s`
- `check.filter_failed`: `  ✖ %s: %s (expected: %s)`
- `check.result_eligible`: `Result: ELIGIBLE (Weight: %d)`
- `check.result_ineligible`: `Result: INELIGIBLE`
- Filter name keys: `filter.dimension_config`, `filter.dimension`, `filter.biome`, `filter.structure`, `filter.block`, `filter.fluid`, `filter.position`, `filter.light`, `filter.time`, `filter.weather`, `filter.date`, `filter.poke_rod`.

---

## 6. Documentation & Release Notes

1. **User Documentation**:
   - Update `docs/reference/commands.md` with full `/cobbleloots debug weights` and `/cobbleloots debug check` reference (arguments, defaults, outputs, examples).
   - Update `MODINFO.md` command overview section.
2. **Changelog Fragment**:
   - Create `.changelog/DEV-26.md` documenting the new debug commands.
   - Do NOT edit `CHANGELOG.md` directly (strictly enforced by project rules).

---

## 7. Verification & Testing Strategy

1. **Syntax & Compilation**:
   - `./gradlew compileJava`
   - `./gradlew build`
2. **Unit / Diagnostic Tests**:
   - Verify `CobblelootsFilterEvaluator` logic with various filter mock scenarios.
   - Verify weight percentage calculations sum to 100% (or appropriate proportions).
3. **In-game / Multi-loader Verification**:
   - Fabric client/server: execute `/cobbleloots debug weights` in various biomes (e.g. Plains vs Nether).
   - Verify autocompletion for loot ball IDs and source types.
   - Execute `/cobbleloots debug check cobbleloots:poke_ball` at valid and invalid spawn locations.
