---
title: Making a Datapack for Loot Balls
description: Guide on how to create a custom datapack for Loot Balls in Cobbleloots.
icon: material/folder
---

## Making a Datapack for Loot Balls

This guide explains how to create datapacks that define custom **Loot Balls** for Cobbleloots. It covers the JSON format, all available filter fields, and provides real-world examples.

---

## Datapack Structure

Create the following directory layout (replace `my_loot_balls` with your namespace):

```
my_loot_balls/
├── pack.mcmeta
└── data/
    └── my_loot_balls/
        ├── loot_ball/           # Loot ball definitions (one JSON per loot ball)
        └── loot_table/
            └── loot_ball/       # Loot tables referenced by your loot ball files
```

`pack.mcmeta` for Minecraft 1.21.1 (pack_format `48`):

```json
{
  "pack": {
    "pack_format": 48,
    "description": "My custom Loot Balls for Cobbleloots"
  }
}
```

Drop the folder into your world's `datapacks/` directory.

---

## Loot Ball JSON Format

Each file in `data/<namespace>/loot_ball/` defines a single loot ball. The filename (without `.json`) becomes its ID.

### Top-level fields

| Field        | Type                                                            | Required | Default        | Description                                                                       |
| ------------ | --------------------------------------------------------------- | -------- | -------------- | --------------------------------------------------------------------------------- |
| `name`       | [Text Component](https://minecraft.wiki/w/Raw_JSON_text_format) | ✅       | —              | Display name. Can be a single object or an array of objects for multi-color text. |
| `loot_table` | [Resource Location](https://minecraft.wiki/w/Identifier)        | —        | empty          | Default loot table. E.g. `my_ns:loot_ball/rewards`.                               |
| `texture`    | [Resource Location](https://minecraft.wiki/w/Identifier)        | —        | empty          | Default texture path. E.g. `cobblemon:textures/poke_balls/poke_ball.png`.         |
| `xp`         | Integer                                                         | —        | config default | XP awarded when opened.                                                           |
| `sources`    | Object                                                          | —        | empty          | Controls where and when loot balls appear. See [Sources](#sources).               |
| `variants`   | Object                                                          | —        | `{}`           | Map of variant ID → variant object. See [Variants](#variants).                    |

---

## Sources

The `sources` object contains arrays of **source rules** for each source type:

| Source type | Key           | Description                                                    |
| ----------- | ------------- | -------------------------------------------------------------- |
| Generation  | `generation`  | Natural world generation (placed when chunks generate).        |
| Spawning    | `spawning`    | Runtime spawning (like mob spawning). Supports `light` filter. |
| Fishing     | `fishing`     | Caught with fishing rods. Supports `poke_rod` filter.          |
| Archaeology | `archaeology` | Found through archaeology mechanics.                           |

Each source type holds a **list of source rules**. Each rule defines conditions for that loot ball to appear in that context. If **any** rule in a source type passes all its filters, the loot ball becomes a candidate (weighted random selection).

```json
"sources": {
  "generation": [ { ... rule ... }, { ... rule ... } ],
  "spawning":   [ { ... rule ... } ],
  "fishing":    [ { ... rule ... } ]
}
```

---

## Source Rule Filters

Every source rule is an object with a `weight` and optional filters. **All specified filters must pass** for the rule to match (AND logic between filters). Omitted filters are treated as "allow all".

### Overview

| Filter                  | Key         | Available in      | Description                                  |
| ----------------------- | ----------- | ----------------- | -------------------------------------------- |
| [Weight](#weight)       | `weight`    | All               | Weighted random selection probability.       |
| [Structure](#structure) | `structure` | All               | Restrict to inside a specific structure.     |
| [Biome](#biome)         | `biome`     | All               | Restrict to specific biomes or biome tags.   |
| [Dimension](#dimension) | `dimension` | All               | Restrict to specific dimensions.             |
| [Block](#block)         | `block`     | All               | Require specific blocks at spawn position.   |
| [Fluid](#fluid)         | `fluid`     | All               | Require a specific fluid at spawn position.  |
| [Position](#position)   | `position`  | All               | Restrict to coordinate ranges (x, y, z).     |
| [Light](#light)         | `light`     | **Spawning only** | Restrict to light level ranges.              |
| [Time](#time)           | `time`      | All               | Restrict to in-game time ranges.             |
| [Weather](#weather)     | `weather`   | All               | Restrict to weather conditions.              |
| [Date](#date)           | `date`      | All               | Restrict to real-world calendar date ranges. |
| [Poké Rod](#poke-rod)   | `poke_rod`  | **Fishing only**  | Restrict to specific Cobblemon fishing rods. |

---

### Weight

Controls the probability of this rule being selected when multiple loot balls match.

| Field    | Type    | Default | Description                                                                                                      |
| -------- | ------- | ------- | ---------------------------------------------------------------------------------------------------------------- |
| `weight` | Integer | `1`     | Higher values = more likely to be chosen. Values are relative to all other matching rules across all loot balls. |

```json
{ "weight": 100 }
```

!!! tip
    Common loot balls use weights of `50`–`100`, uncommon `25`–`50`, rare `1`–`10`.

---

### Structure

Restricts spawning to inside a specific structure. The position must be within a piece of the matching structure.

| Field       | Type               | Default   | Description                                        |
| ----------- | ------------------ | --------- | -------------------------------------------------- |
| `structure` | String (block tag) | _not set_ | A structure tag. Uses the `#namespace:tag` format. |

```json
{ "weight": 10, "structure": "#minecraft:village" }
```

!!! info
    If omitted, the loot ball can appear anywhere regardless of structures.

---

### Biome

Restricts spawning to specific biomes. Supports multiple input formats.

**Simple format** — a single string (biome ID or biome tag):

```json
{ "weight": 80, "biome": "#cobblemon:is_forest" }
```

**Object format** — allows setting the `required` flag:

```json
{
  "weight": 80,
  "biome": { "id": "#cobbleloots:loot_ball/pumpkin", "required": false }
}
```

**List format** — combine multiple entries:

```json
{
  "weight": 50,
  "biome": [
    "#cobblemon:is_ocean",
    "#cobblemon:is_river",
    { "id": "#cobbleloots:loot_ball/pumpkin", "required": false }
  ]
}
```

#### Biome entry fields

| Field      | Type    | Default | Description                                                                                                                                                                                                                                                     |
| ---------- | ------- | ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `id`       | String  | —       | A biome ID (e.g. `minecraft:swamp`) or a biome tag (e.g. `#cobblemon:is_forest`). Tags start with `#`.                                                                                                                                                          |
| `required` | Boolean | `true`  | Whether loading should fail if this entry is not found. Set to `false` for entries from mods that may not be installed or tags defined in other datapacks. A tag that fails to load can still be referenced. Simple string entries are always `required: true`. |

!!! info
    If omitted entirely, the loot ball can appear in any biome.

---

### Dimension

Restricts spawning to one or more dimensions.

| Field       | Type            | Default    | Description                   |
| ----------- | --------------- | ---------- | ----------------------------- |
| `dimension` | List of Strings | `[]` (any) | Dimension resource locations. |

```json
{ "weight": 5, "dimension": ["minecraft:the_end"] }
```

```json
{ "weight": 30, "dimension": ["minecraft:overworld", "minecraft:the_nether"] }
```

!!! info
    An empty list (or omitted field) means the rule passes in **any** dimension.

---

### Block

Restricts spawning based on the block at the spawn position and the block directly below it. Both fields use block tags.

| Field         | Type               | Default   | Description                                                                |
| ------------- | ------------------ | --------- | -------------------------------------------------------------------------- |
| `block.spawn` | String (block tag) | _not set_ | Tag for the block **at** the spawn position (where the loot ball appears). |
| `block.base`  | String (block tag) | _not set_ | Tag for the block **below** the spawn position (the floor block).          |

```json
{
  "weight": 50,
  "block": {
    "spawn": "cobbleloots:loot_ball_spawnable",
    "base": "cobbleloots:loot_ball_base"
  }
}
```

!!! info
    If omitted, any block combination is allowed.

---

### Fluid

Restricts spawning to positions containing a specific fluid ( e.g. for underwater loot balls).

| Field   | Type               | Default   | Description                        |
| ------- | ------------------ | --------- | ---------------------------------- |
| `fluid` | String (fluid tag) | _not set_ | A fluid tag at the spawn position. |

```json
{
  "weight": 40,
  "biome": "#cobblemon:is_deep_ocean",
  "fluid": "minecraft:water"
}
```

!!! info
    If omitted, any fluid is allowed.

---

### Position

Restricts spawning to specific coordinate ranges. Each axis uses a min/max range.

| Field        | Type   | Default | Description                                        |
| ------------ | ------ | ------- | -------------------------------------------------- |
| `position.x` | Object | any     | `{ "min": int, "max": int }` — X coordinate range. |
| `position.y` | Object | any     | `{ "min": int, "max": int }` — Y coordinate range. |
| `position.z` | Object | any     | `{ "min": int, "max": int }` — Z coordinate range. |

```json
{
  "weight": 50,
  "position": {
    "y": { "min": -64, "max": 0 }
  }
}
```

This example restricts the loot ball to underground locations (below Y=0).

!!! info
    Omitted axes allow any value for that coordinate.

---

### Light

**Spawning only.** Restricts spawning based on light levels at the position.

| Field         | Type   | Default                   | Description                                                    |
| ------------- | ------ | ------------------------- | -------------------------------------------------------------- |
| `light.block` | Object | `{ "min": 0, "max": 15 }` | Block light level range (0–15). Light from torches, lava, etc. |
| `light.sky`   | Object | `{ "min": 0, "max": 15 }` | Sky light level range (0–15). Light from the sky.              |

```json
{
  "weight": 10,
  "biome": "#cobblemon:is_spooky",
  "light": {
    "block": { "max": 5 },
    "sky": { "max": 5 }
  }
}
```

This example spawns only in dark spooky biomes (low light).

!!! warning
    The `light` filter is **only evaluated for `spawning` rules**. It is ignored in `generation`, `fishing`, and `archaeology`.

---

### Time

Restricts spawning based on in-game time. Uses modular arithmetic to cycle within a period.

| Field         | Type    | Default | Description                                                                                                                                                         |
| ------------- | ------- | ------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `time.value`  | Object  | any     | `{ "min": int, "max": int }` — The time range (in ticks).                                                                                                           |
| `time.period` | Integer | `0`     | The period for modular time. The game time is computed as `dayTime % period`. Use `24000` for a standard day cycle. `0` = no modular wrapping (uses raw game time). |

```json
{
  "weight": 25,
  "time": {
    "value": { "min": 13000, "max": 23000 },
    "period": 24000
  }
}
```

This example restricts the loot ball to **nighttime** (ticks 13000–23000 in a 24000-tick day cycle).

!!! tip "Minecraft day cycle reference"
    | Phase | Ticks |
    |---|---|
    | Sunrise | 0 – 1000 |
    | Day | 1000 – 13000 |
    | Sunset | 13000 – 13800 |
    | Night | 13800 – 22300 |
    | Dawn | 22300 – 24000 |

---

### Weather

Restricts spawning to specific weather conditions. Each weather type can be enabled or disabled independently.

| Field             | Type    | Default | Description                          |
| ----------------- | ------- | ------- | ------------------------------------ |
| `weather.rain`    | Boolean | `true`  | Allow spawning during rain.          |
| `weather.thunder` | Boolean | `true`  | Allow spawning during thunderstorms. |
| `weather.clear`   | Boolean | `true`  | Allow spawning during clear weather. |

```json
{
  "weight": 50,
  "weather": {
    "rain": true,
    "thunder": false,
    "clear": false
  }
}
```

This example restricts the loot ball to **rainy** weather only (not thunderstorms, not clear).

!!! info
    All three default to `true` — if omitted, any weather is allowed.

---

### Date

Restricts spawning to a real-world calendar date range. Uses the server's system clock.

| Field       | Type   | Default    | Description                               |
| ----------- | ------ | ---------- | ----------------------------------------- |
| `date.from` | String | `""` (any) | Start date in `MM-dd` format (inclusive). |
| `date.to`   | String | `""` (any) | End date in `MM-dd` format (inclusive).   |

```json
{
  "weight": 25,
  "date": { "from": "10-01", "to": "11-01" }
}
```

This example restricts the loot ball to the **Halloween season** (October 1 – November 1).

!!! tip
    Year-wrapping ranges are supported. For example, `{ "from": "12-01", "to": "01-31" }` matches December through January.

---

### Poké Rod

**Fishing only.** Restricts the rule to specific Cobblemon fishing rods.

| Field      | Type            | Default        | Description                     |
| ---------- | --------------- | -------------- | ------------------------------- |
| `poke_rod` | List of Strings | `[]` (any rod) | List of Cobblemon rod item IDs. |

```json
{ "weight": 50, "poke_rod": ["cobblemon:great_rod", "cobblemon:ultra_rod"] }
```

!!! tip "Common pattern"
    Most shipped loot balls use **two fishing rules**: one with a specific rod (high weight), and one without (low weight, any rod). This lets the loot ball be fished anywhere but gives a bonus when using the matching rod:
    ```json
    "fishing": [
      { "weight": 50, "biome": "#cobblemon:is_forest", "poke_rod": ["cobblemon:nest_rod"] },
      { "weight": 5, "biome": "#cobblemon:is_forest" }
    ]
    ```

!!! warning
    The `poke_rod` filter is **only evaluated for `fishing` rules**. It is ignored in other source types. If `poke_rod` is set but the player is not using one of the listed rods, the rule **fails**.

---

## Variants

Variants allow a single loot ball to have multiple flavors. Each variant can override the display name, loot table, and texture.

The `variants` field is a map where keys are variant IDs (strings) and values are variant objects:

| Field        | Type              | Default | Description                             |
| ------------ | ----------------- | ------- | --------------------------------------- |
| `name`       | Text Component    | empty   | Override display name for this variant. |
| `loot_table` | Resource Location | empty   | Override loot table for this variant.   |
| `texture`    | Resource Location | empty   | Override texture for this variant.      |

```json
"variants": {
  "default": {
    "name": { "text": "Standard" },
    "loot_table": "my_ns:loot_ball/standard",
    "texture": "cobblemon:textures/poke_balls/poke_ball.png"
  },
  "shiny": {
    "name": { "text": "Shiny", "color": "gold", "bold": true },
    "loot_table": "my_ns:loot_ball/shiny_rewards",
    "texture": "cobblemon:textures/poke_balls/premier_ball.png"
  }
}
```

---

## Loot Tables

Loot tables use the standard [Minecraft loot table format](https://minecraft.wiki/w/Loot_table). Place them at `data/<namespace>/loot_table/loot_ball/<name>.json`.

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        { "type": "minecraft:item", "name": "minecraft:diamond", "weight": 1 },
        {
          "type": "minecraft:item",
          "name": "minecraft:gold_ingot",
          "weight": 5,
          "functions": [
            {
              "function": "minecraft:set_count",
              "count": { "min": 1, "max": 3 }
            }
          ]
        }
      ]
    }
  ]
}
```

!!! tip
    You can reference shared loot tables using `{ "type": "minecraft:loot_table", "value": "cobbleloots:loot_ball/shared/medicine_common" }` to include the mod's built-in loot pools.

!!! tip
    Shipped loot tables were developed using the [Loot Table Editor](https://misode.github.io/loot-table/) from [Misode](https://github.com/misode). You can use it to generate your own loot tables, but it doesn't support modded items ids. You have to test the generated loot tables in a datapack to ensure they work.

---

## Example Datapack

A working example datapack is included in the mod's repository [here](https://github.com/ResistorCat/cobbleloots/tree/main/docs/guides/how-to/example_datapack/):

```
example_datapack/
├── pack.mcmeta
└── data/
    └── example_loot_balls/
        ├── loot_ball/
        │   ├── my_ball.json              # Multiple filters and variants example
        │   └── forest_ball.json          # Minimal practical example
        └── loot_table/
            └── loot_ball/
                ├── simple.json           # Basic loot table
                └── rare.json             # Rare variant loot table
```

Download the folder and drop it into your world's `datapacks/` directory to test.

---

## Where to Look in the Mod Sources (Advanced)

| Resource                      | Path                                                                                    |
| ----------------------------- | --------------------------------------------------------------------------------------- |
| Shipped loot ball definitions | `common/src/main/resources/data/cobbleloots/loot_ball/`                                 |
| Shipped loot tables           | `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/`                      |
| Filter codecs and defaults    | `common/src/main/java/dev/ripio/cobbleloots/data/custom/filter/CobblelootsFilters.java` |
| Filter processing logic       | `common/src/main/java/dev/ripio/cobbleloots/data/CobblelootsDataProvider.java`          |
| Data model classes            | `common/src/main/java/dev/ripio/cobbleloots/data/custom/`                               |
| Item NBT handling             | `common/src/main/java/dev/ripio/cobbleloots/item/CobblelootsItems.java`                 |
| Entity logic                  | `common/src/main/java/dev/ripio/cobbleloots/entity/custom/CobblelootsLootBall.java`     |

!!! info "AI-Generated Content Disclaimer"
    This guide was created with the assistance of AI tools. There may be some errors or inaccuracies.
