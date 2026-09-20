# DEV-26 Debug Commands Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement diagnostic commands `/cobbleloots debug weights` and `/cobbleloots debug check <id>` to inspect spawn probabilities and filter criteria evaluations across Fabric and NeoForge.

**Architecture:** Centralize filter testing into `CobblelootsFilterEvaluator` and diagnostic orchestration into `CobblelootsDebugService` in `common/`. Build the command tree using Mojang's Brigadier API in `dev.ripio.cobbleloots.command.CobblelootsCommands`, hooked into Fabric via `CommandRegistrationCallback` and NeoForge via `RegisterCommandsEvent`.

**Tech Stack:** Minecraft 1.21.1 (Mojang mappings), Architectury Loom, Brigadier, Java 21.

**Spec:** [.superpowers/specs/2026-09-20-dev-26-debug-commands-design.md](file:///c:/Users/franc/GitHub/cobbleloots/.superpowers/specs/2026-09-20-dev-26-debug-commands-design.md)

## Global Constraints
- Target Minecraft version: `1.21.1`.
- Target Java version: `21`.
- All core logic must be implemented in `common/`.
- Both Fabric and NeoForge loaders must compile cleanly without warnings or errors.
- Permission level 2 required for all debug commands.
- Localization required for `en_us.json`, `es_ec.json`, and `pt_br.json`.
- Documentation in `docs/reference/commands.md` and `MODINFO.md` must be updated.
- Never edit `CHANGELOG.md` directly; create `.changelog/DEV-26.md`.

---

### Task 1: Diagnostic Domain Models

**Files:**
- Create: `common/src/main/java/dev/ripio/cobbleloots/data/debug/FilterCheckResult.java`
- Create: `common/src/main/java/dev/ripio/cobbleloots/data/debug/LootBallRuleReport.java`
- Create: `common/src/main/java/dev/ripio/cobbleloots/data/debug/LootBallCheckReport.java`
- Create: `common/src/main/java/dev/ripio/cobbleloots/data/debug/LootBallWeightEntry.java`
- Create: `common/src/main/java/dev/ripio/cobbleloots/data/debug/WeightsReport.java`

**Interfaces:**
- Produces:
  - `FilterCheckResult(String filterKey, boolean passed, String expected, String actual)`
  - `LootBallRuleReport(int ruleIndex, int weight, boolean overallPassed, List<FilterCheckResult> filterResults)`
  - `LootBallCheckReport(ResourceLocation id, CobblelootsSourceType sourceType, BlockPos pos, ResourceLocation dimension, ResourceLocation biome, boolean eligible, int totalWeight, List<LootBallRuleReport> rules)`
  - `LootBallWeightEntry(ResourceLocation id, int weight, double percentage)`
  - `WeightsReport(CobblelootsSourceType sourceType, BlockPos pos, ResourceLocation dimension, ResourceLocation biome, int totalWeight, List<LootBallWeightEntry> entries)`

- [ ] **Step 1: Create `FilterCheckResult.java`**

```java
package dev.ripio.cobbleloots.data.debug;

public record FilterCheckResult(
    String filterKey,
    boolean passed,
    String expected,
    String actual
) {}
```

- [ ] **Step 2: Create `LootBallRuleReport.java`**

```java
package dev.ripio.cobbleloots.data.debug;

import java.util.List;

public record LootBallRuleReport(
    int ruleIndex,
    int weight,
    boolean overallPassed,
    List<FilterCheckResult> filterResults
) {}
```

- [ ] **Step 3: Create `LootBallCheckReport.java`**

```java
package dev.ripio.cobbleloots.data.debug;

import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

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

- [ ] **Step 4: Create `LootBallWeightEntry.java`**

```java
package dev.ripio.cobbleloots.data.debug;

import net.minecraft.resources.ResourceLocation;

public record LootBallWeightEntry(
    ResourceLocation id,
    int weight,
    double percentage
) {}
```

- [ ] **Step 5: Create `WeightsReport.java`**

```java
package dev.ripio.cobbleloots.data.debug;

import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record WeightsReport(
    CobblelootsSourceType sourceType,
    BlockPos pos,
    ResourceLocation dimension,
    ResourceLocation biome,
    int totalWeight,
    List<LootBallWeightEntry> entries
) {}
```

- [ ] **Step 6: Verify compilation**
Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit models**
```bash
git add common/src/main/java/dev/ripio/cobbleloots/data/debug/
git commit -m "feat(debug): add diagnostic domain models for weights and check reports"
```

---

### Task 2: Centralized Filter Evaluator & DataProvider Integration

**Files:**
- Create: `common/src/main/java/dev/ripio/cobbleloots/data/CobblelootsFilterEvaluator.java`
- Modify: `common/src/main/java/dev/ripio/cobbleloots/data/CobblelootsDataProvider.java`

**Interfaces:**
- Consumes: Models from Task 1, `CobblelootsSourceFilter`, `CobblelootsSourceType`, `CobblelootsConfig`
- Produces:
  - `CobblelootsFilterEvaluator.testFilter(ServerLevel level, LevelChunk chunk, BlockPos pos, CobblelootsSourceFilter source, CobblelootsSourceType sourceType, @Nullable ServerPlayer player, @Nullable ItemStack tool): boolean`
  - `CobblelootsFilterEvaluator.evaluateRule(ServerLevel level, LevelChunk chunk, BlockPos pos, int ruleIndex, CobblelootsSourceFilter source, CobblelootsSourceType sourceType, @Nullable ServerPlayer player, @Nullable ItemStack tool): LootBallRuleReport`
  - `CobblelootsFilterEvaluator.isDimensionDisabled(ServerLevel level, CobblelootsSourceType sourceType): boolean`

- [ ] **Step 1: Create `CobblelootsFilterEvaluator.java`**
Implement both the fast-fail `testFilter` method (for runtime spawning) and the detailed `evaluateRule` method (for debug diagnostics). Extract the checking logic from `CobblelootsDataProvider`:
- `isDimensionDisabled`
- `checkStructureFilter`
- `checkBiomeFilter`
- `checkDimensionFilter`
- `checkBlockFilter`
- `checkFluidFilter`
- `checkPositionFilter`
- `checkLightFilter`
- `checkTimeFilter`
- `checkWeatherFilter`
- `checkDateFilter`
- `checkPokeRodFilter`

- [ ] **Step 2: Update `CobblelootsDataProvider.processSourceFilter` to delegate to `CobblelootsFilterEvaluator.testFilter`**
Update `processSourceFilter` methods and remove duplicated private filter check methods from `CobblelootsDataProvider.java`.

- [ ] **Step 3: Verify compilation**
Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit evaluator**
```bash
git add common/src/main/java/dev/ripio/cobbleloots/data/CobblelootsFilterEvaluator.java common/src/main/java/dev/ripio/cobbleloots/data/CobblelootsDataProvider.java
git commit -m "refactor(data): extract centralized CobblelootsFilterEvaluator"
```

---

### Task 3: Diagnostic Service (`CobblelootsDebugService`)

**Files:**
- Create: `common/src/main/java/dev/ripio/cobbleloots/data/debug/CobblelootsDebugService.java`

**Interfaces:**
- Consumes: `CobblelootsDataProvider`, `CobblelootsFilterEvaluator`, diagnostic records from Task 1
- Produces:
  - `WeightsReport calculateWeights(ServerLevel level, BlockPos pos, CobblelootsSourceType sourceType, @Nullable ServerPlayer player)`
  - `LootBallCheckReport inspectLootBall(ServerLevel level, BlockPos pos, ResourceLocation id, CobblelootsSourceType sourceType, @Nullable ServerPlayer player)`

- [ ] **Step 1: Implement `CobblelootsDebugService.java`**
```java
package dev.ripio.cobbleloots.data.debug;

import dev.ripio.cobbleloots.data.CobblelootsDataProvider;
import dev.ripio.cobbleloots.data.CobblelootsFilterEvaluator;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallData;
import dev.ripio.cobbleloots.data.custom.CobblelootsLootBallSources;
import dev.ripio.cobbleloots.data.custom.filter.CobblelootsSourceFilter;
import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CobblelootsDebugService {
    // calculateWeights implementation
    // inspectLootBall implementation
}
```

- [ ] **Step 2: Verify compilation**
Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit debug service**
```bash
git add common/src/main/java/dev/ripio/cobbleloots/data/debug/CobblelootsDebugService.java
git commit -m "feat(debug): implement CobblelootsDebugService for weights calculation and inspection"
```

---

### Task 4: Command Handler & Brigadier Tree (`CobblelootsCommands`)

**Files:**
- Create: `common/src/main/java/dev/ripio/cobbleloots/command/CobblelootsCommands.java`
- Create: `common/src/main/java/dev/ripio/cobbleloots/command/CobblelootsDebugCommand.java`

**Interfaces:**
- Consumes: `CobblelootsDebugService`, `CommandDispatcher<CommandSourceStack>`, Brigadier
- Produces:
  - `CobblelootsCommands.register(CommandDispatcher<CommandSourceStack> dispatcher)`

- [ ] **Step 1: Create `CobblelootsDebugCommand.java`**
Implement the execution logic, `SuggestionProvider` for Loot Ball IDs, `SuggestionProvider` for source types (`spawning`, `generation`, `fishing`, `archaeology`), and component formatting for weights and check reports.

- [ ] **Step 2: Create `CobblelootsCommands.java`**
Register the root `/cobbleloots` command with permission requirement (`source.hasPermission(2)`), and build `/cobbleloots debug weights [<source_type>] [<pos>]` and `/cobbleloots debug check <id> [<source_type>] [<pos>]`.

- [ ] **Step 3: Verify compilation**
Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit command handler**
```bash
git add common/src/main/java/dev/ripio/cobbleloots/command/
git commit -m "feat(command): implement /cobbleloots debug weights and check commands"
```

---

### Task 5: Platform Registration (Fabric & NeoForge)

**Files:**
- Modify: `fabric/src/main/java/dev/ripio/cobbleloots/fabric/event/CobblelootsEvents.java`
- Modify: `neoforge/src/main/java/dev/ripio/cobbleloots/neoforge/event/CobblelootsEvents.java`

**Interfaces:**
- Consumes: `CobblelootsCommands.register`
- Hooks:
  - Fabric: `CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CobblelootsCommands.register(dispatcher))`
  - NeoForge: `RegisterCommandsEvent` on `NeoForge.EVENT_BUS`

- [ ] **Step 1: Hook command registration in Fabric**
In `dev.ripio.cobbleloots.fabric.event.CobblelootsEvents.java`:
Add `CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CobblelootsCommands.register(dispatcher));`

- [ ] **Step 2: Hook command registration in NeoForge**
In `dev.ripio.cobbleloots.neoforge.event.CobblelootsEvents.java`:
Add:
```java
@SubscribeEvent
public static void registerCommands(RegisterCommandsEvent event) {
    CobblelootsCommands.register(event.getDispatcher());
}
```

- [ ] **Step 3: Verify both loader compilations**
Run: `./gradlew :fabric:compileJava :neoforge:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit loader hooks**
```bash
git add fabric/src/main/java/dev/ripio/cobbleloots/fabric/event/CobblelootsEvents.java neoforge/src/main/java/dev/ripio/cobbleloots/neoforge/event/CobblelootsEvents.java
git commit -m "feat(command): register cobbleloots commands on Fabric and NeoForge"
```

---

### Task 6: Localization & Translations

**Files:**
- Modify: `common/src/main/resources/assets/cobbleloots/lang/en_us.json`
- Modify: `common/src/main/resources/assets/cobbleloots/lang/es_ec.json`
- Modify: `common/src/main/resources/assets/cobbleloots/lang/pt_br.json`

- [ ] **Step 1: Add keys to `en_us.json`**
Add all `command.cobbleloots.debug.*` strings and filter labels.

- [ ] **Step 2: Add keys to `es_ec.json`**
Add Spanish translations for all debug command keys and filter labels.

- [ ] **Step 3: Add keys to `pt_br.json`**
Add Portuguese translations for all debug command keys and filter labels.

- [ ] **Step 4: Validate JSON formatting**
Run: `./gradlew processResources`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit localization**
```bash
git add common/src/main/resources/assets/cobbleloots/lang/
git commit -m "feat(lang): add English, Spanish, and Portuguese translations for debug commands"
```

---

### Task 7: Documentation Updates

**Files:**
- Modify: `docs/reference/commands.md`
- Modify: `MODINFO.md`

- [ ] **Step 1: Update `docs/reference/commands.md`**
Add full documentation for `/cobbleloots debug weights` and `/cobbleloots debug check <id>`, including syntax, optional arguments (`[source_type]`, `[pos]`), outputs, and examples.

- [ ] **Step 2: Update `MODINFO.md`**
Add reference to debug commands in the Commands section.

- [ ] **Step 3: Commit documentation**
```bash
git add docs/reference/commands.md MODINFO.md
git commit -m "docs(commands): document /cobbleloots debug weights and check commands"
```

---

### Task 8: Changelog Fragment

**Files:**
- Create: `.changelog/DEV-26.md`

- [ ] **Step 1: Write changelog fragment**
Document the new debug commands according to the release engine standards:
```markdown
- **Debug Commands**: Added `/cobbleloots debug weights` and `/cobbleloots debug check <id>` to diagnose spawn chances and evaluate filter conditions in real time.
```

- [ ] **Step 2: Commit changelog fragment**
```bash
git add .changelog/DEV-26.md
git commit -m "chore(changelog): add release fragment for DEV-26"
```

---

### Task 9: Verification & Full Multi-loader Build

- [ ] **Step 1: Run full project build**
Run: `./gradlew build`
Expected: BUILD SUCCESSFUL across all subprojects (`common`, `fabric`, `neoforge`).

- [ ] **Step 2: Run doc validation script if present**
Run: `python scripts/validate_docs.py` (if script exists) or verify mkdocs formatting.
