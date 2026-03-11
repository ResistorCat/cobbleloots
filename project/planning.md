# Project Planning

## Version b2.3.0: Archaeology Update (ETA: 2026-03-21)

### Task: Fix Structure Filter not working for Loot Ball Sources

**Description**: Resolve issue where structure filter is not working for loot ball sources.
**Steps**:

- [x] Investigate structure filter.
- [x] Fix structure filter.
      **DoD**: Structure filter works correctly.

### Task: Fix Texture Loading for Custom Data on Loot Ball Item (not entity)

**Description**: Resolve issue where items with `LootBallData` NBT tag fail to load textures without the `Texture` NBT workaround.
**Steps**:

- [ ] Investigate client-server sync for the `LootBallData` NBT tag.
- [ ] Ensure necessary data is available on the client for the custom model predicate to work.
- [ ] Remove dependency on the `Texture` NBT workaround.
      **DoD**: Loot ball items render correctly based solely on `LootBallData`.

### Task: In-Game Creative GUI

**Description**: Create a creative-mode GUI for editing loot balls directly in the game.
**Steps**:

- [ ] Design and implement the GUI layout.
- [ ] Add data binding to read from and write to loot ball NBT/data.
      **DoD**: GUI opens successfully in creative mode, and saves changes to the loot ball entity/block.

### Task: Loot Ball Announcer

**Description**: Broadcast a message when rare and special loot is acquired.
**Steps**:

- [ ] Intercept the loot ball opening event for rare/special tiers.
- [ ] Broadcast a customizable chat/title message to the player or server.
      **DoD**: Opening a rare loot ball successfully triggers the intended announcement.

### Task: Custom Functions Support

**Description**: Allow loot balls to execute custom functions via a `function` key in their data.
**Steps**:

- [ ] Parse the `function` key from the loot ball data definition format.
- [ ] Execute the mapped function when the loot ball is opened.
      **DoD**: Opening a configured loot ball correctly fires the assigned game function.

### Task: Custom Model Definitions

**Description**: Add support for a custom model key in loot ball definition files.
**Steps**:

- [ ] Add a custom model override key to the definition schema.
- [ ] Implement the rendering hook to use the override model if present.
      **DoD**: Loot balls (like Ancient variants) correctly render their custom models.

### Task: Spawning & Tracking Commands

**Description**: Add custom commands to find, teleport to, and reset manually placed loot balls.
**Steps**:

- [ ] Implement command to list manually placed, non-despawned loot balls.
- [ ] Implement player teleportation to listed loot balls (Spectator mode).
- [ ] Implement command to reset loot usages for a specific player across all loot balls.
- [ ] Implement command to reset loot usages for a specific loot ball across all players.
- [ ] Implement command to reset loot usages globally (all players, all loot balls).
      **DoD**: Commands execute correctly without errors, and state changes persist on the server.

### Task: Debug Commands

**Description**: Add custom commands to assist with testing and debugging loot ball behaviors, such as structure filters and spawning mechanics.
**Steps**:

- [ ] Implement a command to force-spawn a loot ball near the player with specific data.
- [ ] Implement a command to query current filter states (e.g. current structure, biome, light) at the player's position.
      **DoD**: Debug commands execute successfully and output accurate information to the player.

### Task: Balance Default Acquisition Rates

**Description**: Review and tweak default values for loot ball sources.
**Steps**:

- [ ] Analyze and adjust `spawning` chances to increase frequency and player awareness.
- [ ] Review `generation` configuration values.
- [ ] Review `fishing` configuration values.
      **DoD**: New default values are committed and feel balanced during a standard gameplay test.

### Task: Archaeology Integration

**Description**: Implement new sources to obtain existing and new ancient loot balls.
**Steps**:

- [ ] Inject loot balls into archaeology loot tables (brushing suspicious sand/gravel).
- [ ] Create and configure Ancient loot balls specific to these sources.
      **DoD**: Loot balls can be successfully excavated using a brush.

---

## Version b2.4.0: Hidden Items and Fake Items

### Task: Hidden Items Mechanics

**Description**: Add support for invisible items on the ground.
**Steps**:

- [ ] Implement the hidden item block/entity logic.
- [ ] Add player interaction mechanics (e.g., detection, unearthing).
      **DoD**: Hidden items can be placed, remain undetected unless interacted with properly, and grant loot.

### Task: Loot Finder Item

**Description**: Add an item to help players locate hidden/nearby loot balls.
**Steps**:

- [ ] Create item logic and registry.
- [ ] Implement nearby loot ball detection logic.
- [ ] Add visual/audio feedback when near a hidden loot.
      **DoD**: Item functions in-game and accurately points out or reacts to nearby loot balls.
