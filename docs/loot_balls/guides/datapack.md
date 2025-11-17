---
title: Making a Datapack for Loot Balls
description: Guide on how to create a custom datapack for Loot Balls in Cobbleloots.
icon: material/folder
---

## Making a Datapack for Loot Balls

This page focuses strictly on creating datapacks for Loot Balls (mapmaker / server-owner workflow). It explains the fields you can configure in a loot ball definition and provides clear, copy-paste examples and a small example datapack in this repo.

Important reference details from the mod code:

- Loot Ball data files live under `data/<namespace>/loot_ball/<id>.json` (the mod ships `data/cobbleloots/loot_ball/`).
- Loot tables for loot balls live under `data/<namespace>/loot_table/loot_ball/*.json`.
- The mod expects `LootBallData` (a ResourceLocation string) and `Variant` (a variant id) on the item/entity when reconstructing configured loot balls.

---

## Datapack structure and recommended workflow

Create the datapack skeleton (replace `example_loot_balls` with your namespace):

```
my_loot_balls/
├── pack.mcmeta
└── data/
    └── my_loot_balls/
        ├── loot_ball/           # Loot ball JSON definitions (one file per loot ball id)
        └── loot_table/          # Loot tables referenced by your loot_ball files

```

pack.mcmeta example for Minecraft 1.21.1 (pack_format 48):

```json
{
  "pack": {
    "pack_format": 48,
    "description": "Example Loot Balls for Cobbleloots (1.21.1)"
  }
}
```

---

## Fields you can configure in a loot ball JSON

Below are the common fields the mod reads. Not every field is required; the mod will fall back to sensible defaults when fields are missing.

- `name` (object) — A Minecraft chat component used as the display name, e.g. `{ "text": "My Ball" }`.
- `loot_table` (string ResourceLocation) — Default loot table used when the ball opens, e.g. `my_ns:loot_ball/simple`.
- `texture` (string ResourceLocation) — Default texture resource used by the ball when no variant/custom texture is set.
- `xp` (integer) — XP awarded when the ball is opened (optional).
- `sources` (object) — Controls generation/spawning rules. Typically contains `generation` and/or `spawning` arrays with rule objects. If you don't need world generation, you can omit `sources`.
  - Each rule object supports fields such as `weight`, `structure`, `biome`, `dimension`, `block` (with `spawn`/`base`), `fluid`, `position` ranges, `time`, `weather`, and `date` ranges. Copy the existing rules shipped with the mod as a starting point.
- `variants` (object) — A map of variant id -> variant object. Variant object can override `name`, `loot_table`, and `texture`.

Optional entity/item properties controlled via NBT (when creating items/entities in-game or in a datapack):

- See `LootBallData` (string): the resource path to the loot_ball JSON (e.g. `example_loot_balls:loot_ball/my_ball`).
- `Variant` (string): variant id (must match a key in `variants`), else empty string.
- `Texture` (string): custom texture resource path (overrides loot_ball texture/variant texture when set).
- Visual / runtime properties usually handled at entity-level (not strictly datapack fields): `Sparks`, `Invisible`, `Uses`, `Multiplier`, `DespawnTick`, `PlayerTimer`, `XP` (these are useful when you create an entity via functions or when storing creative-configured items for testing).

---

## Minimal loot_ball JSON example

Place this file as `data/example_loot_balls/loot_ball/my_ball.json` inside your datapack. This example demonstrates using `variants` and includes `sources` for world generation and spawning rules.

```json
{
  "name": { "text": "Mapmaker Ball" },
  "loot_table": "example_loot_balls:loot_ball/simple",
  "texture": "cobbleloots:textures/loot_ball/rainbow.png",
  "xp": 0,
  "sources": {
    "generation": [
      {
        "weight": 100,
        "structure": "cobbleloots:empty",
        "biome": "cobbleloots:empty",
        "dimension": [],
        "block": {
          "spawn": "cobbleloots:empty",
          "base": "cobbleloots:empty"
        },
        "fluid": "cobbleloots:empty",
        "position": {
          "x": { "min": -2147483647, "max": 2147483647 },
          "y": { "min": -2147483647, "max": 2147483647 },
          "z": { "min": -2147483647, "max": 2147483647 }
        },
        "time": {
          "value": { "min": 0, "max": 2147483647 },
          "period": 24000
        },
        "weather": {
          "rain": true,
          "thunder": true,
          "clear": true
        },
        "date": {
          "from": "01-01",
          "to": "12-31"
        }
      }
    ],
    "spawning": [
      {
        "weight": 100,
        "structure": "cobbleloots:empty",
        "biome": "cobbleloots:empty",
        "dimension": [],
        "block": {
          "spawn": "cobbleloots:empty",
          "base": "cobbleloots:empty"
        },
        "fluid": "cobbleloots:empty",
        "position": {
          "x": { "min": -2147483647, "max": 2147483647 },
          "y": { "min": -2147483647, "max": 2147483647 },
          "z": { "min": -2147483647, "max": 2147483647 }
        },
        "light": {
          "block": { "min": 0, "max": 15 },
          "sky": { "min": 0, "max": 15 }
        },
        "time": {
          "value": { "min": 0, "max": 2147483647 },
          "period": 24000
        },
        "weather": {
          "rain": true,
          "thunder": true,
          "clear": true
        },
        "date": {
          "from": "01-01",
          "to": "12-31"
        }
      }
    ]
  },
  "variants": {
    "default": {
      "name": { "text": "Default" },
      "loot_table": "example_loot_balls:loot_ball/simple",
      "texture": "cobbleloots:textures/loot_ball/rainbow.png"
    },
    "rare": {
      "name": { "text": "Rare" },
      "loot_table": "example_loot_balls:loot_ball/rare",
      "texture": "cobbleloots:textures/loot_ball/azure.png"
    }
  }
}
```

Notes:

- Use resource locations for `loot_table` and `texture`.
- Variants are optional but useful to offer multiple flavors of the same logical loot ball.
- The `sources` section controls where and when loot balls can spawn in the world. The example above uses permissive settings (using `cobbleloots:empty` tags) that allow spawning anywhere. You can customize these rules to restrict spawning to specific biomes, structures, dimensions, times, weather conditions, etc.
- The `generation` array controls natural world generation, while `spawning` controls runtime spawning.
- Each spawning rule includes a `light` section (block light and sky light levels) to control lighting conditions.

---

## Minimal loot_table example

Place this at `data/example_loot_balls/loot_table/loot_ball/simple.json`.

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        { "type": "minecraft:item", "name": "minecraft:diamond", "weight": 1 }
      ]
    }
  ]
}
```

---

## Example datapack included in the repo

I've added a minimal example datapack at `docs/loot_balls/creative/example_datapack/` with the following structure:

- `pack.mcmeta` (pack_format 48 for Minecraft 1.21.1)
- `data/example_loot_balls/loot_ball/my_ball.json` (example loot ball)
- `data/example_loot_balls/loot_table/loot_ball/simple.json` (example loot table)

You can download the folder and drop it into your world's `datapacks/` folder.

---

## Where to look in the mod sources (for advanced users)

- Loot ball JSONs shipped with the mod: `common/src/main/resources/data/cobbleloots/loot_ball/`.
- Loot tables: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/`.
- Code references that are useful when authoring datapacks:
  - `common/src/main/java/dev/ripio/cobbleloots/item/CobblelootsItems.java` — shows how the mod writes `CustomData` to items.
  - `common/src/main/java/dev/ripio/cobbleloots/entity/custom/CobblelootsLootBall.java` — entity logic, NBT keys, and how loot tables/variants are resolved.
  - `common/src/main/java/dev/ripio/cobbleloots/data/` — all the CODECs and data loading logic for loot balls and sources.

!!! info "AI-Generated Content Disclaimer"
    This guide was created with the assistance of AI tools. There may be some errors or inaccuracies.
