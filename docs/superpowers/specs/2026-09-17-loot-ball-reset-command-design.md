# Design Specification: Loot Ball Reset Command Subsystem

- **Linear Issue:** DEV-5 (`[Cobbleloots] Reiniciar loot balls que ya fueron abiertas`)
- **Status:** Approved Draft
- **Date:** 2026-09-17
- **Target Loaders:** Fabric 0.17.2 & NeoForge 21.1.182 (Minecraft 1.21.1)

---

## 1. Problem Statement & Objectives

In Cobbleloots, Loot Balls record players who opened them in an in-memory and NBT-persisted map (`openers: Map<UUID, Long>`). In multiplayer servers, event maps, and adventure worlds, server administrators and map creators need the ability to reset opened Loot Balls without manually replacing entities across the world.

Key challenges to address:
1. **Unloaded Chunks:** A global reset cannot simply iterate loaded entities; loot balls in unloaded chunks must reflect the reset state when eventually loaded and interacted with, without expensive full-world chunk loading.
2. **Infinite Loot Balls vs. Finite Uses:** Finite loot balls depleted to 0 uses should optionally have their uses restored to default, while infinite loot balls (`uses <= -1`) must strictly preserve their infinite status.
3. **Targeting Flexibility:** Commands must support individual players, player selectors (`@p`, `@a`, `@s`), or global wildcards (`all` / `*`).
4. **Revertibility (Undo):** Administrators need an operational safety net to undo an accidental global or player reset.
5. **Auditing & Logging:** Administrative actions must be logged in server logs (`latest.log`) and localized in-game feedback sent exclusively to the command executor.

---

## 2. Architecture Overview

The subsystem uses a **Hybrid Timestamp-Gated Architecture**:

```
+-------------------------------------------------------------+
|                     Minecraft Server                        |
|                                                             |
|  +-------------------------------------------------------+  |
|  |             CobblelootsWorldData (SavedData)          |  |
|  |  - globalResetTimestamp : long                        |  |
|  |  - globalRestoreUses : boolean                        |  |
|  |  - playerResetTimestamps : Map<UUID, Long>            |  |
|  |  - playerRestoreUses : Map<UUID, Boolean>             |  |
|  |  - previousGlobalResetTimestamp : long (Undo)         |  |
|  |  - previousPlayerResetTimestamps : Map<UUID, Long>    |  |
|  +-------------------------------------------------------+  |
|                             |                               |
|        Effective reset time = Math.max(global, player)      |
|                             |                               |
|                             v                               |
|  +-------------------------------------------------------+  |
|  |           CobblelootsLootBall (Entity Hook)           |  |
|  |  openers: Map<UUID, Long> (lastOpenGameTime)          |  |
|  |                                                       |  |
|  |  On interaction / isOpener check:                     |  |
|  |  if (lastOpenGameTime < effectiveResetTime) {         |  |
|  |      openers.remove(playerUUID);                      |  |
|  |      if (shouldRestoreUses && !isInfinite()           |  |
|  |          && remainingUses <= 0) {                     |  |
|  |          uses = DEFAULT_USES;                         |  |
|  |      }                                                |  |
|  |      setChanged();                                    |  |
|  |  }                                                    |  |
|  +-------------------------------------------------------+  |
+-------------------------------------------------------------+
```

### 2.1 Non-Destructive Timestamp Gating
Instead of deleting records irrevocably, opening validity is evaluated against the world's absolute tick count (`ServerLevel.getGameTime()`):
- An opener record is valid if `lastOpenGameTime > effectiveResetTimestamp`.
- If `lastOpenGameTime <= effectiveResetTimestamp`, the interaction lazily clears the old record and restores depleted uses (if configured), allowing the player to open the ball.
- Because older records in `openers` are only purged when superseded by an active reset, reverting (`undo`) restores the previous timestamp, instantly closing balls that were not opened in the interim.

---

## 3. Command Specifications

Permission level required: **Level 2** (Game Master / OP / Command Blocks / Server Console).

Base command: `/cobbleloots reset loot_ball`

### 3.1 Syntax Tree

```text
/cobbleloots reset loot_ball
  |
  |-- all
  |    |-- [restore_uses: bool]
  |    |-- "all" | "*"
  |    |    \-- [restore_uses: bool]
  |    \-- <targets: players>
  |         \-- [restore_uses: bool]
  |
  |-- target
  |    |-- [restore_uses: bool]
  |    |-- "all" | "*"
  |    |    \-- [restore_uses: bool]
  |    \-- <targets: players>
  |         \-- [restore_uses: bool]
  |
  |-- entity <entity_selector: entities>
  |    |-- [restore_uses: bool]
  |    |-- "all" | "*"
  |    |    \-- [restore_uses: bool]
  |    \-- <targets: players>
  |         \-- [restore_uses: bool]
  |
  \-- undo
       |-- "all" | "*"
       \-- <targets: players>
```

### 3.2 Behavior & Arguments

1. **`all` (Global Server Reset)**:
   - Iterates all loaded `CobblelootsLootBall` entities across all dimensions (`server.getAllLevels()`).
   - For loaded entities: immediately resets targeted player(s) in `openers` and restores `uses = DEFAULT_USES` if `uses <= 0 && !isInfinite() && restore_uses`.
   - Stores current `server.overworld().getGameTime()` and `restore_uses` in `CobblelootsWorldData`.
   - Saves previous timestamps into `CobblelootsWorldData` undo history.
2. **`target` (Raycast Reset)**:
   - Requires player executor (`context.getSource().getPlayerOrException()`).
   - Raycasts from player's eye position up to 16 blocks using `player.getViewVector(...)` and entity bounding box intersection matching `CobblelootsLootBall`.
   - Resets the targeted entity in-memory. If no loot ball is in line of sight, returns a failure feedback message.
3. **`entity` (Entity Selector Reset)**:
   - Resolves entities using `EntityArgument.getEntities(...)`.
   - Filters only entities of type `CobblelootsLootBall`.
   - Resets each matched loot ball in-memory.
4. **`undo` (Revert Reset)**:
   - Reverts `globalResetTimestamp` or specific `playerResetTimestamps` to their previous values before the latest reset action in `CobblelootsWorldData`.
   - Provides feedback on the restored timestamp state.

Default argument fallback:
- If `targets` is omitted: applies to `@s` (command executor). If executed from console without targets, fails gracefully requiring explicit targets or `all`.
- If `restore_uses` is omitted: defaults to `true`.

---

## 4. Component & Data Design

### 4.1 `CobblelootsWorldData`
Located in `common/src/main/java/dev/ripio/cobbleloots/data/CobblelootsWorldData.java`.
- Subclasses `net.minecraft.world.level.saveddata.SavedData`.
- Data structure:
  - `long globalResetTimestamp`
  - `boolean globalRestoreUses`
  - `Map<UUID, Long> playerResetTimestamps`
  - `Map<UUID, Boolean> playerRestoreUses`
  - `long previousGlobalResetTimestamp`
  - `Map<UUID, Long> previousPlayerResetTimestamps`
- Serialization:
  - Compatible with MC 1.21.1 `SavedData.Factory<CobblelootsWorldData>` using `CompoundTag` and `HolderLookup.Provider`.
  - Keys: `"GlobalResetTimestamp"`, `"GlobalRestoreUses"`, `"PlayerResets"`, `"PreviousGlobalResetTimestamp"`, `"PreviousPlayerResets"`.

### 4.2 `CobblelootsLootBall` Modifications
Located in `common/src/main/java/dev/ripio/cobbleloots/entity/custom/CobblelootsLootBall.java`.
- Introduce `reconcileResetState(ServerPlayer player)` or check in `canPlayerOpenLootBall`:
  - Retrieves `CobblelootsWorldData.get(player.server)`.
  - Compares `openers.get(uuid)` against `data.getEffectiveResetTimestamp(uuid)`.
  - If superseded: cleans opener entry, restores uses if `data.shouldRestoreUses(uuid) && !isInfinite() && getRemainingUses() <= 0`, and calls `setChanged()`.
- Introduce helper methods:
  - `resetForPlayer(UUID playerUuid, boolean restoreUses)`
  - `resetForAll(boolean restoreUses)`

### 4.3 Platform Command Registration
- **Common Registration**:
  - `dev.ripio.cobbleloots.command.CobblelootsCommands`: Registers the root `/cobbleloots` command tree and dispatches to subcommands.
  - `dev.ripio.cobbleloots.command.LootBallResetCommand`: Implements the Brigadier command node tree and execution handlers.
- **Fabric**:
  - `dev.ripio.cobbleloots.fabric.command.CobblelootsCommandsFabric`: Hooks into `CommandRegistrationCallback.EVENT.register(...)`.
  - Initialized in `CobblelootsFabric.onInitialize()`.
- **NeoForge**:
  - `dev.ripio.cobbleloots.neoforge.command.CobblelootsCommandsNeoForge`: `@SubscribeEvent` on `RegisterCommandsEvent` on `NeoForge.EVENT_BUS`.
  - Registered during NeoForge initialization.

---

## 5. Localization & Logging

### 5.1 Server Logging
On command execution:
```java
Cobbleloots.LOGGER.info(
    "{} executed '/cobbleloots reset loot_ball {}' for {} (restore_uses={}): {} loaded loot balls updated.",
    executorName, subAction, targetDesc, restoreUses, affectedCount
);
```

### 5.2 Translation Keys
Added to `en_us.json`, `es_ec.json`, and `pt_br.json`:
- `commands.cobbleloots.reset.loot_ball.all.success`: Feedback for global reset.
- `commands.cobbleloots.reset.loot_ball.target.success`: Feedback for target raycast reset.
- `commands.cobbleloots.reset.loot_ball.entity.success`: Feedback for entity selector reset.
- `commands.cobbleloots.reset.loot_ball.undo.success`: Feedback for undo.
- `commands.cobbleloots.reset.loot_ball.target.not_found`: Error when raycast finds no loot ball within 16 blocks.
- `commands.cobbleloots.reset.loot_ball.entity.none_matched`: Error when entity selector matches no loot balls.
- `commands.cobbleloots.reset.loot_ball.player_required`: Error when player-only command (raycast or `@s` fallback) is executed by console.

---

## 6. Verification & Quality Assurance

1. **Automated Verification**:
   - Clean compilation: `./gradlew build` (Fabric + NeoForge subprojects).
2. **Functional QA (Fabric & NeoForge client/server)**:
   - Run `/cobbleloots reset loot_ball all` -> verifies loaded balls and world data.
   - Run `/cobbleloots reset loot_ball target` while looking at a Loot Ball from 5, 10, and 15 blocks.
   - Run `/cobbleloots reset loot_ball entity @e[type=cobbleloots:loot_ball,distance=..20]`.
   - Verify depleted finite loot ball (`uses=0`) restores to `uses=1` when `restore_uses=true`, and stays 0 when `restore_uses=false`.
   - Verify infinite loot ball (`uses=-1`) preserves `uses=-1`.
   - Unload chunk containing an opened loot ball, run `/cobbleloots reset loot_ball all`, return to chunk, interact with ball -> verifies lazy reconciliation.
   - Run `/cobbleloots reset loot_ball undo` -> verifies previous state is restored.
