# Loot Ball Reset Command Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a comprehensive `/cobbleloots reset loot_ball` command subsystem across Fabric and NeoForge with hybrid `SavedData` persistence, lazy entity invalidation for unloaded chunks, raycast, entity selectors, and an undo facility.

**Architecture:** A `SavedData` singleton (`CobblelootsWorldData`) on the Overworld storage tracks global and per-player reset timestamps alongside undo history. Loaded `CobblelootsLootBall` entities are updated in memory on demand, while unloaded chunks lazily reconcile on interaction against `CobblelootsWorldData`. Infinite balls preserve their infinite status.

**Tech Stack:** Minecraft 1.21.1, Architectury Loom, Brigadier, Java 21, Fabric API 0.116.6, NeoForge 21.1.182.

**Spec:** [`docs/superpowers/specs/2026-09-17-loot-ball-reset-command-design.md`](file:///C:/Users/franc/GitHub/cobbleloots/.worktrees/ripio/dev-5-cobbleloots-reiniciar-loot-balls-que-ya-fueron-abiertas/docs/superpowers/specs/2026-09-17-loot-ball-reset-command-design.md)

## Global Constraints
- Target Minecraft 1.21.1, Java 21.
- Multi-loader compatible: logic in `common/`, loaders in `fabric/` and `neoforge/`.
- Do not modify `CHANGELOG.md`.
- Preserve existing NBT structure of `CobblelootsLootBall`.
- Non-destructive timestamp gating: infinite loot balls (`uses <= -1`) must never have uses modified.

---

### Task 1: World Persistence Subsystem (`CobblelootsWorldData`)

**Files:**
- Create: `common/src/main/java/dev/ripio/cobbleloots/data/CobblelootsWorldData.java`

**Interfaces:**
- Produces:
  - `CobblelootsWorldData.get(MinecraftServer server)` -> `CobblelootsWorldData`
  - `data.resetAll(long gameTime, boolean restoreUses)` -> `void`
  - `data.resetPlayers(Collection<UUID> players, long gameTime, boolean restoreUses)` -> `void`
  - `data.undoAll()` -> `boolean`
  - `data.undoPlayers(Collection<UUID> players)` -> `boolean`
  - `data.getEffectiveResetTimestamp(UUID playerUuid)` -> `long`
  - `data.shouldRestoreUses(UUID playerUuid)` -> `boolean`

- [ ] **Step 1: Write `CobblelootsWorldData.java`**
```java
package dev.ripio.cobbleloots.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class CobblelootsWorldData extends SavedData {
    public static final String DATA_NAME = "cobbleloots_world_data";

    private long globalResetTimestamp = 0L;
    private boolean globalRestoreUses = true;
    private final Map<UUID, Long> playerResetTimestamps = new HashMap<>();
    private final Map<UUID, Boolean> playerRestoreUses = new HashMap<>();

    // Undo history
    private long previousGlobalResetTimestamp = 0L;
    private boolean previousGlobalRestoreUses = true;
    private final Map<UUID, Long> previousPlayerResetTimestamps = new HashMap<>();
    private final Map<UUID, Boolean> previousPlayerRestoreUses = new HashMap<>();

    public static final Factory<CobblelootsWorldData> FACTORY = new Factory<>(
        CobblelootsWorldData::new,
        CobblelootsWorldData::load,
        null
    );

    public static CobblelootsWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public void resetAll(long gameTime, boolean restoreUses) {
        this.previousGlobalResetTimestamp = this.globalResetTimestamp;
        this.previousGlobalRestoreUses = this.globalRestoreUses;
        this.previousPlayerResetTimestamps.clear();
        this.previousPlayerResetTimestamps.putAll(this.playerResetTimestamps);
        this.previousPlayerRestoreUses.clear();
        this.previousPlayerRestoreUses.putAll(this.playerRestoreUses);

        this.globalResetTimestamp = gameTime;
        this.globalRestoreUses = restoreUses;
        this.playerResetTimestamps.clear();
        this.playerRestoreUses.clear();
        this.setDirty();
    }

    public void resetPlayers(Collection<UUID> players, long gameTime, boolean restoreUses) {
        this.previousPlayerResetTimestamps.clear();
        this.previousPlayerResetTimestamps.putAll(this.playerResetTimestamps);
        this.previousPlayerRestoreUses.clear();
        this.previousPlayerRestoreUses.putAll(this.playerRestoreUses);

        for (UUID uuid : players) {
            this.playerResetTimestamps.put(uuid, gameTime);
            this.playerRestoreUses.put(uuid, restoreUses);
        }
        this.setDirty();
    }

    public boolean undoAll() {
        if (this.globalResetTimestamp == this.previousGlobalResetTimestamp && this.playerResetTimestamps.equals(this.previousPlayerResetTimestamps)) {
            return false;
        }
        this.globalResetTimestamp = this.previousGlobalResetTimestamp;
        this.globalRestoreUses = this.previousGlobalRestoreUses;
        this.playerResetTimestamps.clear();
        this.playerResetTimestamps.putAll(this.previousPlayerResetTimestamps);
        this.playerRestoreUses.clear();
        this.playerRestoreUses.putAll(this.previousPlayerRestoreUses);
        this.setDirty();
        return true;
    }

    public boolean undoPlayers(Collection<UUID> players) {
        boolean changed = false;
        for (UUID uuid : players) {
            if (this.previousPlayerResetTimestamps.containsKey(uuid)) {
                this.playerResetTimestamps.put(uuid, this.previousPlayerResetTimestamps.get(uuid));
                this.playerRestoreUses.put(uuid, this.previousPlayerRestoreUses.getOrDefault(uuid, true));
                changed = true;
            } else if (this.playerResetTimestamps.remove(uuid) != null) {
                this.playerRestoreUses.remove(uuid);
                changed = true;
            }
        }
        if (changed) {
            this.setDirty();
        }
        return changed;
    }

    public long getEffectiveResetTimestamp(UUID playerUuid) {
        long playerReset = this.playerResetTimestamps.getOrDefault(playerUuid, 0L);
        return Math.max(this.globalResetTimestamp, playerReset);
    }

    public boolean shouldRestoreUses(UUID playerUuid) {
        long playerReset = this.playerResetTimestamps.getOrDefault(playerUuid, 0L);
        if (playerReset >= this.globalResetTimestamp && this.playerRestoreUses.containsKey(playerUuid)) {
            return this.playerRestoreUses.get(playerUuid);
        }
        return this.globalRestoreUses;
    }

    public static CobblelootsWorldData load(CompoundTag tag, HolderLookup.Provider provider) {
        CobblelootsWorldData data = new CobblelootsWorldData();
        data.globalResetTimestamp = tag.getLong("GlobalResetTimestamp");
        data.globalRestoreUses = !tag.contains("GlobalRestoreUses") || tag.getBoolean("GlobalRestoreUses");
        data.previousGlobalResetTimestamp = tag.getLong("PreviousGlobalResetTimestamp");
        data.previousGlobalRestoreUses = !tag.contains("PreviousGlobalRestoreUses") || tag.getBoolean("PreviousGlobalRestoreUses");

        if (tag.contains("PlayerResets")) {
            ListTag list = tag.getList("PlayerResets", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag pTag = list.getCompound(i);
                UUID uuid = pTag.getUUID("UUID");
                data.playerResetTimestamps.put(uuid, pTag.getLong("Timestamp"));
                data.playerRestoreUses.put(uuid, !pTag.contains("RestoreUses") || pTag.getBoolean("RestoreUses"));
            }
        }

        if (tag.contains("PreviousPlayerResets")) {
            ListTag list = tag.getList("PreviousPlayerResets", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag pTag = list.getCompound(i);
                UUID uuid = pTag.getUUID("UUID");
                data.previousPlayerResetTimestamps.put(uuid, pTag.getLong("Timestamp"));
                data.previousPlayerRestoreUses.put(uuid, !pTag.contains("RestoreUses") || pTag.getBoolean("RestoreUses"));
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLong("GlobalResetTimestamp", this.globalResetTimestamp);
        tag.putBoolean("GlobalRestoreUses", this.globalRestoreUses);
        tag.putLong("PreviousGlobalResetTimestamp", this.previousGlobalResetTimestamp);
        tag.putBoolean("PreviousGlobalRestoreUses", this.previousGlobalRestoreUses);

        ListTag list = new ListTag();
        for (Map.Entry<UUID, Long> entry : this.playerResetTimestamps.entrySet()) {
            CompoundTag pTag = new CompoundTag();
            pTag.putUUID("UUID", entry.getKey());
            pTag.putLong("Timestamp", entry.getValue());
            pTag.putBoolean("RestoreUses", this.playerRestoreUses.getOrDefault(entry.getKey(), true));
            list.add(pTag);
        }
        tag.put("PlayerResets", list);

        ListTag prevList = new ListTag();
        for (Map.Entry<UUID, Long> entry : this.previousPlayerResetTimestamps.entrySet()) {
            CompoundTag pTag = new CompoundTag();
            pTag.putUUID("UUID", entry.getKey());
            pTag.putLong("Timestamp", entry.getValue());
            pTag.putBoolean("RestoreUses", this.previousPlayerRestoreUses.getOrDefault(entry.getKey(), true));
            prevList.add(pTag);
        }
        tag.put("PreviousPlayerResets", prevList);

        return tag;
    }
}
```

- [ ] **Step 2: Verify Compilation**
Run: `./gradlew :common:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**
Run: `git add common/src/main/java/dev/ripio/cobbleloots/data/CobblelootsWorldData.java`
`git commit -m "feat(data): implement CobblelootsWorldData for reset persistence"`

---

### Task 2: Entity Lifecycle & Invalidation Hook (`CobblelootsLootBall`)

**Files:**
- Modify: `common/src/main/java/dev/ripio/cobbleloots/entity/custom/CobblelootsLootBall.java`

**Interfaces:**
- Consumes:
  - `CobblelootsWorldData.get(MinecraftServer server)`
  - `data.getEffectiveResetTimestamp(UUID playerUuid)`
  - `data.shouldRestoreUses(UUID playerUuid)`
- Produces:
  - `lootBall.reconcileResetState(ServerPlayer player)` -> `void`
  - `lootBall.resetForPlayer(UUID playerUuid, boolean restoreUses)` -> `void`
  - `lootBall.resetForAll(boolean restoreUses)` -> `void`

- [ ] **Step 1: Add reset and reconciliation methods to `CobblelootsLootBall`**
In `CobblelootsLootBall.java`:
```java
public void reconcileResetState(ServerPlayer player) {
    if (player == null || player.getServer() == null) return;
    UUID uuid = player.getUUID();
    if (this.openers.containsKey(uuid)) {
        long lastOpen = this.openers.get(uuid);
        CobblelootsWorldData worldData = CobblelootsWorldData.get(player.getServer());
        long effectiveReset = worldData.getEffectiveResetTimestamp(uuid);
        if (effectiveReset > lastOpen) {
            this.openers.remove(uuid);
            if (worldData.shouldRestoreUses(uuid) && !this.isInfinite() && this.getRemainingUses() <= 0) {
                this.setRemainingUses(DEFAULT_USES);
            }
            this.setChanged();
        }
    }
}

public void resetForPlayer(UUID playerUuid, boolean restoreUses) {
    this.openers.remove(playerUuid);
    if (restoreUses && !this.isInfinite() && this.getRemainingUses() <= 0) {
        this.setRemainingUses(DEFAULT_USES);
    }
    this.setChanged();
}

public void resetForAll(boolean restoreUses) {
    this.openers.clear();
    if (restoreUses && !this.isInfinite() && this.getRemainingUses() <= 0) {
        this.setRemainingUses(DEFAULT_USES);
    }
    this.setChanged();
}
```

- [ ] **Step 2: Invoke `reconcileResetState` inside `canPlayerOpenLootBall`**
In `CobblelootsLootBall.canPlayerOpenLootBall`:
Call `this.reconcileResetState(serverPlayer);` before evaluating `this.isOpener(serverPlayer)` and `this.getRemainingUses() == 0`.

- [ ] **Step 3: Verify Compilation**
Run: `./gradlew :common:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**
Run: `git add common/src/main/java/dev/ripio/cobbleloots/entity/custom/CobblelootsLootBall.java`
`git commit -m "feat(entity): add reset hooks and lazy reconciliation in CobblelootsLootBall"`

---

### Task 3: Localization Keys (`en_us.json`, `es_ec.json`, `pt_br.json`)

**Files:**
- Modify: `common/src/main/resources/assets/cobbleloots/lang/en_us.json`
- Modify: `common/src/main/resources/assets/cobbleloots/lang/es_ec.json`
- Modify: `common/src/main/resources/assets/cobbleloots/lang/pt_br.json`

- [ ] **Step 1: Add command translation keys**
Keys to add:
```json
"commands.cobbleloots.reset.loot_ball.all.success": "Reset all loot balls for %s (%s loaded balls reset).",
"commands.cobbleloots.reset.loot_ball.target.success": "Reset targeted loot ball for %s.",
"commands.cobbleloots.reset.loot_ball.entity.success": "Reset %s selected loot balls for %s.",
"commands.cobbleloots.reset.loot_ball.undo.success": "Reverted loot ball reset for %s.",
"commands.cobbleloots.reset.loot_ball.undo.no_change": "No previous reset action found to undo for %s.",
"commands.cobbleloots.reset.loot_ball.target.not_found": "No loot ball found in line of sight within 16 blocks.",
"commands.cobbleloots.reset.loot_ball.entity.none_matched": "No loot balls matched the entity selector.",
"commands.cobbleloots.reset.loot_ball.player_required": "A player is required to execute this command without explicit targets."
```
Add Spanish translations to `es_ec.json` and Portuguese translations to `pt_br.json`.

- [ ] **Step 2: Commit**
Run: `git add common/src/main/resources/assets/cobbleloots/lang/`
`git commit -m "feat(lang): add reset command localization keys"`

---

### Task 4: Command Subsystem Core (`LootBallResetCommand` & `CobblelootsCommands`)

**Files:**
- Create: `common/src/main/java/dev/ripio/cobbleloots/command/CobblelootsCommands.java`
- Create: `common/src/main/java/dev/ripio/cobbleloots/command/LootBallResetCommand.java`

**Interfaces:**
- Produces:
  - `CobblelootsCommands.register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection)`
- Uses:
  - `CobblelootsWorldData`
  - `CobblelootsLootBall`
  - `Cobbleloots.LOGGER`

- [ ] **Step 1: Write `LootBallResetCommand.java`**
Implement the full Brigadier command tree with:
- Subcommand `all`: with optional `all`/`*` wildcard or player selector, and optional `restore_uses: bool`.
- Subcommand `target`: with 16-block raycast matching `CobblelootsLootBall`.
- Subcommand `entity`: with entity selector matching `CobblelootsLootBall`.
- Subcommand `undo`: with optional `all`/`*` wildcard or player selector.
- Server auditing via `Cobbleloots.LOGGER.info`.
- Command feedback via `context.getSource().sendSuccess(...)`.

- [ ] **Step 2: Write `CobblelootsCommands.java`**
Registers the root `LiteralArgumentBuilder<CommandSourceStack> cobbleloots` node, attaches `reset` -> `loot_ball` -> `LootBallResetCommand.register()`, and registers to `dispatcher`.

- [ ] **Step 3: Verify Compilation**
Run: `./gradlew :common:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**
Run: `git add common/src/main/java/dev/ripio/cobbleloots/command/`
`git commit -m "feat(commands): implement LootBallResetCommand and CobblelootsCommands"`

---

### Task 5: Platform Integration (Fabric & NeoForge Command Hooks)

**Files:**
- Create: `fabric/src/main/java/dev/ripio/cobbleloots/fabric/command/CobblelootsCommandsFabric.java`
- Modify: `fabric/src/main/java/dev/ripio/cobbleloots/fabric/CobblelootsFabric.java`
- Create: `neoforge/src/main/java/dev/ripio/cobbleloots/neoforge/command/CobblelootsCommandsNeoForge.java`
- Modify: `neoforge/src/main/java/dev/ripio/cobbleloots/neoforge/CobblelootsNeoForge.java`

- [ ] **Step 1: Fabric Command Hook**
In `CobblelootsCommandsFabric.java`:
```java
package dev.ripio.cobbleloots.fabric.command;

import dev.ripio.cobbleloots.command.CobblelootsCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CobblelootsCommandsFabric {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(CobblelootsCommands::register);
    }
}
```
In `CobblelootsFabric.java`: Call `CobblelootsCommandsFabric.registerCommands();` inside `onInitialize()`.

- [ ] **Step 2: NeoForge Command Hook**
In `CobblelootsCommandsNeoForge.java`:
```java
package dev.ripio.cobbleloots.neoforge.command;

import dev.ripio.cobbleloots.command.CobblelootsCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CobblelootsCommandsNeoForge {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CobblelootsCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }
}
```
In `CobblelootsNeoForge.java`: Register event listener on `NeoForge.EVENT_BUS.register(CobblelootsCommandsNeoForge.class)`.

- [ ] **Step 3: Verify Multi-Platform Compilation**
Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL on `:common:compileJava`, `:fabric:compileJava`, and `:neoforge:compileJava`.

- [ ] **Step 4: Commit**
Run: `git add fabric/ neoforge/`
`git commit -m "feat(loaders): register commands on Fabric and NeoForge"`

---

### Task 6: Full Verification & Build Gate

**Files:**
- None (Build & Test verification)

- [ ] **Step 1: Execute Full Multi-loader Build**
Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (all jars produced in `fabric/build/libs` and `neoforge/build/libs`).

- [ ] **Step 2: Git Status Check**
Run: `git status`
Verify no untracked or dirty files remain.
