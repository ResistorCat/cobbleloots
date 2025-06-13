# Cobbleloots ALPHA-2.0.7

> **IMPORTANT**: This update breaks compatibility with previous versions of the mod. You will need to update your data packs and configurations to match the new Loot Ball Data format. 

> **REMEMBER**: Alpha versions may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the [Discord](https://discord.gg/2YGJXxHtBX) server.

## Changes

### Loot Ball Data Rework
- Reworked the Loot Ball Data format to be more flexible and easier to use.
- The Loot Ball definition structure (`data/namespace/loot_ball/{name}.json`) now contains:
  - (**required**) `name` [`Component`]: The name of the Loot Ball, displayed in the inventory. It can be a simple string or a component with formatting.
  - (**optional**) `loot_table` [`ResourceLocation`]: The loot table to use when opening the Loot Ball.
  - (**optional**) `texture` [`ResourceLocation`]: The texture to use for the Loot Ball item. If not specified, the default texture will be used.
  - (**optional**) `sources` [`Map<String, List<SourceFilter>>`]: A map of sources with filters to use for the Loot Ball.
    - (**optional**) `generation` [`List<SourceFilter>`]: A list of filters for the Loot Ball to generate in the world.
    - (**optional**) `spawning` [`List<SourceFilter>`]: A list of filters for the Loot Ball to spawn in the world.
  - (**optional**) `variants` [`Map<String, VariantData>`]: A map of variants for the Loot Ball. Each variant can have its own `name`, `texture`, and `loot_table`. Each variant is identified by a unique `id`.
    - (**optional**) `{variant_id}` [`VariantData`]: The data for the variant, where `{variant_id}` is a unique identifier for the variant.

- The Loot Ball definition structure now uses a new `SourceFilter` format, which allows for more flexible filtering of sources:
  - (**optional**) `weight` [`Integer`]: The weight of the filter, used for random selection. Defaults to `1`.
  - (**optional**) `structure` [`Tag<Structure>`]: A tag of structures that the filter applies to. If not specified, the filter passes for all structures.
  - (**optional**) `biome` [`Tag<Biome>`]: A tag of biomes that the filter applies to. If not specified, the filter passes for all biomes.
  - (**optional**) `dimension` [`List<ResourceLocation>`]: A list of dimensions that the filter applies to. If not specified, the filter passes for all dimensions.
  - (**optional**) `block` [`Map<String, Tag<Block>>`]:
    - (**optional**) `spawn` [`Tag<Block>`]: A tag of blocks that the filter applies to when checking the block the Loot Ball is spawned on. If not specified, the filter passes for all blocks.
    - (**optional**) `base` [`Tag<Block>`]: A tag of blocks that the filter applies to when checking the base block of the Loot Ball. If not specified, the filter passes for all blocks.
  - (**optional**) `fluid` [`Tag<Fluid>`]: A tag of fluids that the filter applies to when checking the fluid the Loot Ball is spawned on. If not specified, the filter passes for all fluids.
  - (**optional**) `position` [`Map<String, MinMaxFilter>`]:
    - (**optional**) `x` [`MinMaxFilter`]: A filter for the x position of the Loot Ball. If not specified, the filter passes for all x positions.
    - (**optional**) `y` [`MinMaxFilter`]: A filter for the y position of the Loot Ball. If not specified, the filter passes for all y positions.
    - (**optional**) `z` [`MinMaxFilter`]: A filter for the z position of the Loot Ball. If not specified, the filter passes for all z positions.
  - (**optional**) (**exclusive to spawning filters**) `light` [`Map<String, MinMaxFilter>`]:
    - (**optional**) `block` [`MinMaxFilter`]: A filter for the block light level of the Loot Ball. If not specified, the filter passes for all block light levels.
    - (**optional**) `sky` [`MinMaxFilter`]: A filter for the sky light level of the Loot Ball. If not specified, the filter passes for all sky light levels.
  - (**optional**) `time` [`Map<String, MinMaxFilter>`]:
    - (**optional**) `value` [`MinMaxFilter`]: A filter for the time of day in ticks when the Loot Ball can spawn. If not specified, the filter passes for all times.
    - (**optional**) `period` [`Integer`]: The period of the time filter in ticks, which defines the modulus for the time value. If not specified, the filter does not apply a period.
  - (**optional**) `weather` [`Map<String, Boolean>`]:
    - (**optional**) `clear` [`Boolean`]: Whether the filter passes when the weather is clear. Defaults to `true`.
    - (**optional**) `rain` [`Boolean`]: Whether the filter passes when it is raining. Defaults to `true`.
    - (**optional**) `thunder` [`Boolean`]: Whether the filter passes when it is thundering. Defaults to `true`.
- The Loot Ball definition structure now uses a new `MinMaxFilter` format, which allows for more flexible filtering of positions:
  - (**optional**) `min` [`Integer`]: The minimum value for the filter. If not specified, the filter passes for all values.
  - (**optional**) `max` [`Integer`]: The maximum value for the filter. If not specified, the filter passes for all values.
- The Loot Ball definition structure now uses a new `VariantData` format, which allows for more flexible variant data:
  - (**optional**) `name` [`Component`]: The name of the variant, displayed in the inventory. If not specified, the parent name will be used.
  - (**optional**) `texture` [`ResourceLocation`]: The texture to use for the variant item. If not specified, the parent texture will be used.
  - (**optional**) `loot_table` [`ResourceLocation`]: The loot table to use when opening the variant Loot Ball. If not specified, the parent loot table will be used.  

### Loot Ball Changes
- You can now place Loot Balls directly where you are looking at instead of placing them on a fixed position centered on the block you are looking at.
- Changed `Variant` NBT Tag type for Loot Balls to `String` instead of `Integer`. It now represents a variant id instead of the position in the variants lists inside the Loot Ball definition JSON.
- Added `PlayerTimer` Loot Ball NBT Tag of type `Long`.
  - It represents a value in `ticks` that the player must wait to reopen the Loot Ball.
  - A value of `0` disables this behaviour.
  - A small value (like `1`) indicates that the player can reopen the Loot Ball indefinitely.
- Added `XP` Loot Ball NBT Tag of type `Integer`.
  - It represents the amount of XP that the Loot Ball will give to the player when opened.
- The default values for Loot Balls are now configurable via the new `loot_ball.defaults` config section. Some default values may still be hardcoded, but most of them can now be changed in the config file.
- Loot balls will now try to spawn on a solid block instead of midair, but they will still spawn midair if no solid block is found after a certain amount of attempts.
- Some default loot balls now have colours applied to their names to make them more distinguishable in the inventory.
- Updated Loot Ball definitions to use the new Loot Ball Data format, and some of the definitions have been changed to reflect the new data structure:
  - Poké (and variants): Now gives `1` XP when opened.
    - The definition serves as a example for data pack creators to create their own Loot Ball definitions. The values you see in the Poké Loot Ball definition are mostly the default values for all Loot Balls (some of exceptions are the `xp` or `weight` values).
  - Great: Now gives `3` XP when opened.
  - Ultra: Now gives `10` XP when opened.
  - Master:
    - Now gives `50` XP when opened.
    - Now it can spawn in all biomes with a weight of `1`.
  - Safari: Now gives `1` XP when opened.
  - Luxury: Now gives `25` XP when opened.
  - Dive:
    - Now gives `5` XP when opened.
    - Generation and Spawning:
      - Now it only generates/spawn inside water
      - Removed the below y-level 62 generation condition.
      - It still generates/spawn in the Dive Loot Ball allowed biomes.
  - Heal:
    - Now gives `10` XP when opened.
    - Generation and Spawning:
      - Now it can generate and spawn in all overworld biomes.
      - Reduced the weight to `10` for all sources to make it rarer.
  - Lure: Removed the `Lure` Loot Ball definition, as it was not used in the mod yet (until the fishing update).

### Creative Tab Changes
- Replaced all items in the Cobbleloots Creative Tab with:
  - `Loot Ball` default item, without any specific data attached.
  - `Textured Loot Ball` items, which are the same as the default item but with a specific texture applied. All the cobblemon textures are now available as textured loot balls.

### Config Changes
- Added new configs:
  - `loot_ball.defaults.uses`: Default amount of uses a Loot Ball has. Defaults to `1`.
  - `loot_ball.defaults.multiplier`: Default loot multiplier for Loot Balls. Defaults to `1.0`.
  - `loot_ball.defaults.xp`: Default XP amount for Loot Balls. Defaults to `0`.
  - `loot_ball.defaults.player_timer`: Default player timer in ticks. Defaults to `0`.
  - `loot_ball.defaults.despawn_tick`: Default despawn tick timer. Defaults to `0`.
  - `loot_ball.survival.drop.enabled`: Enable Loot Balls to drop as decorative items in survival mode. Defaults to `true`.
- Added the ability for Loot Balls to drop as decorative items in survival mode.
  - When a loot ball has no remaining uses, you can destroy them in survival and the loot ball will drop as a decorative item.
  - This change is currently work in progress, so expect some bugs.
  - You can disable this with the new `loot_ball.survival.drop.enabled` config.

## Fixes
- Fixed a bug where wardens could target and attack loot balls; now wardens ignore loot balls as intended.
- Fixed loot balls not despawning when falling into the world void.
- Fixed loot balls not being killed using the `/kill` command.

## Technical
- All codecs were reworked for the new loot ball data format.
- Optimized some internal game detections for loot ball entity targetting and damage.
- Add a limit to random chunk search to optimize loot ball generation.
- Removed internal block check for valid position when spawning a loot ball. This is replaced now by loot ball definition filters.
- Major rewrite of the loot ball code.
- Updated lang files.
