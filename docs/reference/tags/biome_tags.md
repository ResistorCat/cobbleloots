---
title: Biome Tags
type: reference
icon: material/terrain
---

# Biome Tags Reference

This page provides a reference for the biome tags used in Cobbleloots. These tags define which biomes Loot Balls can spawn in.

---

## `cobbleloots:loot_ball/dive`

**Description:** This biome tag is used for the Dive Loot Ball and includes all biomes that are considered oceans by Cobblemon.

**Includes:**

- [`#cobblemon:is_ocean`](https://gitlab.com/cable-mc/cobblemon/-/blob/main/common/src/main/resources/data/cobblemon/tags/worldgen/biome/is_ocean.json)

**Usage Example:** This tag is referenced by the Dive Loot Ball for both generation and spawning, restricting it to ocean biomes and requiring the presence of water as a fluid.

---

## `cobbleloots:loot_ball/luxury`

**Description:** This biome tag is used for the Luxury Loot Ball and includes all biomes that are considered End biomes by Cobblemon.

**Includes:**

- [`#cobblemon:is_end`](https://cobblemon.github.io/data/tags/worldgen/biome/is_end/)

**Usage Example:** This tag is referenced by the Luxury Loot Ball for both generation and spawning, restricting it to End biomes.

---

## `cobbleloots:loot_ball/pumpkin`

**Description:** This biome tag is used for the Pumpkin Loot Ball and includes a selection of forest, swamp, and plains-like biomes.

**Includes:**

- `minecraft:dark_forest`
- `minecraft:swamp`
- `minecraft:taiga`
- `minecraft:old_growth_pine_taiga`
- `minecraft:old_growth_spruce_taiga`
- `minecraft:plains`
- `minecraft:sunflower_plains`
- `minecraft:forest`
- `minecraft:birch_forest`
- `minecraft:old_growth_birch_forest`
- `minecraft:mangrove_swamp`
- `minecraft:wooded_badlands`
- `minecraft:deep_dark`

**Usage Example:** This tag is referenced by the Pumpkin Loot Ball for generation, restricting it to the specified biomes.

---

## `cobbleloots:loot_ball/rainbow`

**Description:** This biome tag is used for the Rainbow Loot Ball and includes a selection of colorful or rare biomes.

**Includes:**

- `minecraft:mushroom_fields`
- `minecraft:flower_forest`
- `minecraft:meadow`
- `minecraft:sunflower_plains`
- `minecraft:warm_ocean`
- `minecraft:cherry_grove`

**Usage Example:** This tag is referenced by the Rainbow Loot Ball for both generation and spawning, restricting it to these special biomes.

---

## `cobbleloots:loot_ball/safari`

**Description:** This biome tag is used for the Safari Loot Ball and includes all biomes that are considered savanna by Cobblemon.

**Includes:**

- [`#cobblemon:is_savanna`](https://cobblemon.github.io/data/tags/worldgen/biome/is_savanna/)

**Usage Example:** This tag is referenced by the Safari Loot Ball for both generation and spawning, restricting it to savanna biomes.
