---
title: Editing Loot Balls in Creative mode
description: How to edit Loot Balls in Creative mode, commands, and testing tips.
icon: material/brain
---

# Editing Loot Balls in Creative mode

This guide covers all the tools available for working with Loot Balls in Creative mode, including commands, interactions, NBT editing, and mapmaking utilities. Whether you're testing loot tables or building custom maps, this page has everything you need.

## Creative Mode Tab

In Creative mode, open the **Cobbleloots** item group tab to find pre-configured Loot Ball items for every ball type. These items come with their texture already set, so you can place them directly into the world.

The creative tab includes:

- **Normal Balls** — Poké, Citrine, Verdant, Azure, Roseate, Slate, Premier, Great, Ultra, Safari, Fast, Level, Lure, Heavy, Love, Friend, Moon, Sport, Park, Net, Dive, Nest, Repeat, Timer, Luxury, Dusk, Heal, Quick, Dream, Beast, Master, and Cherish.
- **Ancient Balls** — Ancient Poké, Ancient Citrine, Ancient Verdant, Ancient Azure, Ancient Roseate, Ancient Slate, Ancient Ivory, Ancient Great, Ancient Ultra, Ancient Feather, Ancient Wing, Ancient Jet, Ancient Heavy, Ancient Leaden, Ancient Gigaton, and Ancient Origin.
- **Special Balls** — Rainbow and Pumpkin (with custom Cobbleloots textures).

!!! note
    Creative tab items only have a `Texture` set — they don't include `LootBallData` or `Variant` by default. This means they display the correct ball appearance, but won't produce any loot when opened in Survival mode. To make them functional, set the `LootBallData` field (see [NBT Reference](#nbt-reference) below).

---

## 👆 Creative Interaction Behaviors

In Creative mode, right-clicking and punching Loot Ball entities behave differently from Survival:

### Right-Click Interactions

| Condition                                   | Action                                                   | Feedback                            |
| ------------------------------------------- | -------------------------------------------------------- | ----------------------------------- |
| **Empty hand**                              | Toggles entity visibility (invisible ↔ visible)          | Potion drink sound + status message |
| **Honeycomb** on an **invisible** Loot Ball | Toggles spark particles on/off                           | Wax on/off sound + status message   |
| **Any other item**                          | Sets the Loot Ball's content to that item (type + count) | Insert sound + confirmation message |

!!! tip
    Setting an item manually overrides the loot table. The Loot Ball will give exactly that item when opened, ignoring any `LootBallData` loot table.

### Punch (Left-Click) Interactions

| Condition                  | Action                                                            |
| -------------------------- | ----------------------------------------------------------------- |
| **Empty hand** in Creative | Drops the current item content (if any), then destroys the entity |
| **Holding an item**        | No effect (attack is blocked)                                     |

!!! note
    In Survival mode, empty Loot Balls can only be punched when they have 0 remaining uses. In Creative, you can always punch them to destroy and recover contents.

### Pick Block (Middle-Click)

**Middle-clicking** a Loot Ball entity copies it to your inventory as an item, preserving **all** entity properties: `LootBallData`, `Variant`, `Texture`, `Sparks`, `Invisible`, `Uses`, `Multiplier`, `DespawnTick`, `PlayerTimer`, and `XP`. This is the best way to duplicate a fully configured Loot Ball.

---

## 🎁 Giving Loot Ball Items

Give yourself a configured Loot Ball item using the `/give` command with `minecraft:custom_data`:

```mcfunction
/give @p cobbleloots:loot_ball[minecraft:custom_data={LootBallData:"cobbleloots:loot_ball/poke",Variant:"verdant",Texture:"cobblemon:textures/poke_balls/verdant_ball.png"}] 1
```

!!! note
    When using `/give`, always include the `Texture` field. Items don't automatically resolve their texture from `LootBallData`/`Variant` the way entities do, which can cause a missing texture if omitted.

### Minimal examples

A simple Poke Ball (no variant):

```mcfunction
/give @p cobbleloots:loot_ball[minecraft:custom_data={LootBallData:"cobbleloots:loot_ball/poke",Texture:"cobblemon:textures/poke_balls/poke_ball.png"}] 1
```

A texture-only decorative ball (no loot data):

```mcfunction
/give @p cobbleloots:loot_ball[minecraft:custom_data={Texture:"cobbleloots:textures/loot_ball/pumpkin.png"}] 1
```

---

## 🪄 Summoning Loot Ball Entities

Summon a Loot Ball entity directly into the world. For entities, NBT fields go directly in the entity data (no `minecraft:custom_data` wrapper):

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootBallData:"cobbleloots:loot_ball/poke",Variant:"verdant"}
```

Entities automatically resolve their texture from `LootBallData` and `Variant`, so you don't need to include the `Texture` field unless you want to override it.

### Examples

Summon a loot ball that's invisible with spark particles:

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootBallData:"cobbleloots:loot_ball/ultra",Invisible:1b,Sparks:1b}
```

Summon a loot ball with custom gameplay overrides:

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootBallData:"cobbleloots:loot_ball/master",Uses:-1,Multiplier:3.0f,XP:100,PlayerTimer:6000L}
```

Summon a loot ball with a custom texture override:

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootBallData:"cobbleloots:loot_ball/poke",Texture:"cobblemon:textures/poke_balls/beast_ball.png"}
```

Summon a loot ball with a direct loot table (bypassing loot ball data):

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootTable:"minecraft:chests/end_city_treasure"}
```

---

## 🔍 Viewing and Editing Loot Ball NBT

Use Minecraft's `/data` commands to inspect and modify Loot Ball entities in real time.

### Inspecting data

Get the full NBT data of the nearest Loot Ball:

```mcfunction
/data get entity @n[type=cobbleloots:loot_ball]
```

Get a specific field:

```mcfunction
/data get entity @n[type=cobbleloots:loot_ball] LootBallData
```

### Modifying data

Change the loot ball type:

```mcfunction
/data modify entity @n[type=cobbleloots:loot_ball] LootBallData set value "cobbleloots:loot_ball/great"
```

Make a loot ball invisible:

```mcfunction
/data modify entity @n[type=cobbleloots:loot_ball] Invisible set value 1b
```

Set infinite uses:

```mcfunction
/data modify entity @n[type=cobbleloots:loot_ball] Uses set value -1
```

Set a loot multiplier:

```mcfunction
/data modify entity @n[type=cobbleloots:loot_ball] Multiplier set value 5.0f
```

Set a player cooldown (in ticks):

```mcfunction
/data modify entity @n[type=cobbleloots:loot_ball] PlayerTimer set value 12000L
```

Set experience points reward:

```mcfunction
/data modify entity @n[type=cobbleloots:loot_ball] XP set value 50
```

Set a despawn tick (the absolute game tick at which the entity is removed):

```mcfunction
/data modify entity @n[type=cobbleloots:loot_ball] DespawnTick set value 0L
```

Override the texture:

```mcfunction
/data modify entity @n[type=cobbleloots:loot_ball] Texture set value "cobblemon:textures/poke_balls/love_ball.png"
```

!!! warning
    Be careful with `/data modify` commands — incorrect values can break the entity. Always back up your world before experimenting.

---

## 📋 NBT Reference

All NBT tags available on a `cobbleloots:loot_ball` entity:

### Identification Tags

| Tag            | Type     | Description                                                                                                                           | Default      |
| -------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------- | ------------ |
| `LootBallData` | `string` | Resource location of the loot ball data definition (e.g. `cobbleloots:loot_ball/poke`). Determines loot table, texture, and variants. | `""` (empty) |
| `Variant`      | `string` | Variant key within the loot ball data (e.g. `verdant`, `citrine`). Each variant can override the texture and loot table.              | `""` (empty) |

### Visual Tags

| Tag         | Type      | Description                                                                                    | Default      |
| ----------- | --------- | ---------------------------------------------------------------------------------------------- | ------------ |
| `Texture`   | `string`  | Custom texture resource path. Overrides the texture from `LootBallData`/`Variant` when set.    | `""` (empty) |
| `Invisible` | `boolean` | Whether the entity is invisible. Invisible loot balls are hidden but can show spark particles. | `false`      |
| `Sparks`    | `boolean` | Whether to display electric spark particles (only visible when the entity is invisible).       | `true`       |

### Gameplay Tags

| Tag           | Type    | Description                                                                                                                          | Default                                         |
| ------------- | ------- | ------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------- |
| `Uses`        | `int`   | Remaining number of times this loot ball can be opened. Set to `-1` for infinite uses.                                               | Config: `loot_ball_default_uses` (1)            |
| `Multiplier`  | `float` | Multiplier applied to item counts when loot is given. For example, `2.0f` doubles the quantity.                                      | Config: `loot_ball_default_multiplier` (1.0)    |
| `XP`          | `int`   | Experience points awarded to the player when they open the loot ball. Requires `loot_ball_xp_enabled` in config.                     | Config: `loot_ball_default_xp` (0)              |
| `PlayerTimer` | `long`  | Cooldown in ticks before the same player can open this loot ball again. Only applies if `Uses` allows multiple openings.             | Config: `loot_ball_default_player_cooldown` (0) |
| `DespawnTick` | `long`  | Absolute game tick at which the entity will be removed. Set to `0` to disable despawning. Spawned loot balls set this automatically. | Config: `loot_ball_default_despawn_tick` (0)    |

### Loot Table Tags (Advanced)

| Tag             | Type     | Description                                                                                                                               |
| --------------- | -------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| `LootTable`     | `string` | Direct loot table resource location (e.g. `minecraft:chests/village/village_plains_house`). Overrides any loot table from `LootBallData`. |
| `LootTableSeed` | `long`   | Seed for the loot table RNG. Set to `0` for random seed.                                                                                  |

### Internal Tags (read-only)

| Tag       | Type   | Description                                                                                              |
| --------- | ------ | -------------------------------------------------------------------------------------------------------- |
| `Openers` | `list` | List of players who have opened this loot ball, with UUIDs and timestamps. Used for per-player tracking. |

!!! info
    Tags stored on **items** use `minecraft:custom_data` as a wrapper. Tags stored on **entities** (via `/summon` or `/data`) go directly in the entity NBT. When placing a Loot Ball item, its `custom_data` tags are read into the entity automatically.

---

## 🗺️ Mapmaking Utilities

### Placing pre-configured Loot Balls

The Loot Ball item can be placed directly on blocks (right-click on a surface). When placed:

- The item's `minecraft:custom_data` is transferred to the entity.
- The entity faces away from the player (180° offset).
- A placement sound plays and a game event is emitted.

This makes it easy to set up Loot Balls in custom maps: configure items with `/give`, then place them where you want.

### Infinite Loot Balls

For map features like repeatable rewards, set `Uses` to `-1`:

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootBallData:"cobbleloots:loot_ball/great",Uses:-1,PlayerTimer:72000L}
```

This creates a Loot Ball that can be opened unlimited times, with a 1-hour cooldown per player (72000 ticks).

### Hidden Loot Balls with Sparks

Create invisible loot balls that hint at their location with particle effects:

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootBallData:"cobbleloots:loot_ball/ultra",Invisible:1b,Sparks:1b,Multiplier:2.0f}
```

To create a completely hidden loot ball (no particles):

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootBallData:"cobbleloots:loot_ball/master",Invisible:1b,Sparks:0b}
```

### Timed Loot Balls

Create loot balls that disappear after a set time. The `DespawnTick` uses the **absolute game tick**, so you need to calculate: `current_game_tick + desired_duration`.

For a loot ball that despawns after 10 minutes (12000 ticks), you can use a command block or function with time calculations, or set it relative to the current time:

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootBallData:"cobbleloots:loot_ball/poke",DespawnTick:12000L}
```

!!! note
    `DespawnTick` is an **absolute** game tick, not a duration. A value of `12000L` means the entity despawns when the world's game time reaches tick 12000. For dynamic timing, use a function or command block to compute `current_tick + duration`.

### Custom Loot Tables

You can assign any Minecraft loot table to a Loot Ball by using the direct `LootTable` tag:

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootTable:"minecraft:chests/ancient_city",Texture:"cobblemon:textures/poke_balls/dusk_ball.png"}
```

This bypasses the Cobbleloots data pack system entirely and uses vanilla (or modded) loot tables directly.

### Named Loot Balls for Distribution

Create named items with lore for easy identification in chests or shops:

```mcfunction
/give @p cobbleloots:loot_ball[minecraft:custom_data={LootBallData:"cobbleloots:loot_ball/poke",Variant:"verdant",Texture:"cobblemon:textures/poke_balls/verdant_ball.png"},minecraft:custom_name='{"text":"Verdant Loot Ball","color":"green","italic":false}',minecraft:lore=['{"text":"A rare loot ball found in forests","color":"gray","italic":false}']] 1
```

---

## ⚙️ Creative Config Defaults

The mod's configuration has a **Creative** category that controls the default values for newly spawned loot ball entities. These act as the baseline when a tag is not explicitly set:

| Config Option                       | Description                     | Default        |
| ----------------------------------- | ------------------------------- | -------------- |
| `loot_ball_default_uses`            | Default remaining uses          | `1`            |
| `loot_ball_default_multiplier`      | Default loot multiplier         | `1.0`          |
| `loot_ball_default_xp`              | Default XP reward               | `0`            |
| `loot_ball_default_player_cooldown` | Default player cooldown (ticks) | `0`            |
| `loot_ball_default_despawn_tick`    | Default despawn tick            | `0` (disabled) |

!!! tip
    These config defaults affect all newly created loot ball entities — both naturally spawned and summoned via commands. Override them per-entity using NBT tags.

---

## 🔧 Troubleshooting Tips

- 🖼️ **Textures not showing on items**: Always include the `Texture` field in `/give` commands. Items don't auto-resolve textures from `LootBallData` like entities do.
- 🚫 **Loot Ball won't open in Survival**: Check the `Uses` count (0 = exhausted), player cooldown (`PlayerTimer`), and that the loot ball has a valid `LootBallData` or `LootTable` set. Empty loot balls (no loot table or items) won't open.
- ⚙️ **Commands not working**: Ensure you have operator permissions. Creative mode interactions only work while in Creative game mode.
- 🐛 **Entity not spawning**: Verify the entity type is `cobbleloots:loot_ball` and the mod is loaded. Check server logs for errors.
- 🔄 **Pick block not working**: Make sure you are in Creative mode. Pick block copies all entity data, including visibility and gameplay properties.
- 📦 **Set item not working**: You must right-click while holding an item. If you're holding a honeycomb and the loot ball is invisible, it will toggle sparks instead of setting the item.

---

!!! info "AI-Generated Content Disclaimer"
    This guide was created with the assistance of AI tools. There may be some errors or inaccuracies.
