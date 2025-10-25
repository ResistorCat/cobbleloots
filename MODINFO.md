# Cobblemon: CobbleLoots

Cobbleloots is a mod for Cobblemon that adds different ways to obtain items based on the original Pokémon video games.

> **Note**: This mod replaces my previous mod `Cobblemon: Loot Balls`, but it is not compatible with the old loot balls from the other mod, so I suggest backing up items from previous worlds.

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the [GitHub repository](https://github.com/ResistorCat/cobbleloots/issues) or on my [Discord server](https://discord.gg/2YGJXxHtBX).

> **IMPORTANT (ALPHA-2.0.7+):** Recent updates have introduced breaking changes, especially with the Loot Ball Data format. You will need to update your data packs and configurations to ensure compatibility.

<!-- TOC -->

- [Cobblemon: CobbleLoots](#cobblemon-cobbleloots)
- [Features](#features)
  - [Loot Balls 💎](#loot-balls-)
    - [Obtaining Loot Balls (WIP)](#obtaining-loot-balls-wip)
  - [Mapmaking 🗺️](#mapmaking-)
    - [Custom Loot Balls 🎁](#custom-loot-balls-)
  - [Mod configuration ⚙️](#mod-configuration-)
- [Planned Features](#planned-features)
  - [Other Sources 🎣](#other-sources-)
  - [Commands 🛠️](#commands-)
  - [Hidden Items 🔍](#hidden-items-)
  - [Fake Items 🪤](#fake-items-)
- [Looking for the old mod? (Loot Balls)](#looking-for-the-old-mod-loot-balls)
<!-- TOC -->

---

# Features

## Loot Balls 💎

Based on my previous work on the **Cobblemon mod: Loot Balls**, these balls contain useful items for the player. They can grant XP and items when opened, and can be used as a decorative entity in survival mode. When a loot ball has no remaining uses, it can be destroyed to drop as a decorative item (this behavior is configurable).

> **Note**: The loot balls from the previous mod (versions b1.x or older) are not compatible with this one!

### Obtaining Loot Balls (WIP)

- **Generation**: Loot balls can be found in the world. Some of them are specific to biomes, while others are more common.
- **Spawning**: Overtime, loot balls will spawn in the world around random players.

## Mapmaking 🗺️

Check out the [documentation](https://resistorcat.github.io/cobbleloots/) to learn how to use the mod objects and features in your custom maps. If you still have questions, feel free to ask on my discord server.

> **Note**: The documentation is a work in progress, and some features may not be fully documented yet. If you need help with a specific feature, please reach out on my [Discord server](https://discord.gg/2YGJXxHtBX).

### Custom Loot Balls 🎁

Create custom loot balls with different items and properties using the new Loot Ball Data system. You can customize various aspects, including:

- The **name** displayed in-game.
- The **loot table** used when opened.
- The **texture** of the loot ball.
- The amount of **XP** granted to the player.
- **Player-specific timers** (cooldowns) before a player can reopen the loot ball.
- **Sources** for how loot balls appear (e.g., `generation`, `spawning`), with detailed filters:
  - `weight` for rarity.
  - `structure` tags.
  - `biome` tags.
  - `dimension` lists.
  - `block` tags (for spawn block or base block).
  - `fluid` tags.
  - `position` (x, y, z coordinates with min/max ranges).
  - `light` levels (block and sky, for spawning).
  - `time` of day (with period options).
  - `weather` conditions (clear, rain, thunder).
- **Variants** for a single loot ball type, each with its own optional name, texture, and loot table.

Some of these features are continuously being refined, but the data system is designed for flexibility and future additions.

## Mod configuration ⚙️

You can customize many of the mod's features in the config file. The config file is located in the `config/cobbleloots` folder of your Minecraft instance, and is named `cobbleloots.yaml`. You can edit it with any text editor.
Key configurable aspects include:

- Default loot ball properties.
- Survival mode drop behavior.
- Disabling loot balls in specific dimensions.

---

# Planned Features

## Other Sources 🎣

More ways to obtain loot balls in survival mode, such as fishing and archaeology.

## Commands 🛠️

Commands to help mapmakers and server owners to manage the mod features.

## Hidden Items 🔍

Inspired by the original Pokémon games, Hidden Items are invisible objects scattered across the world, waiting to be discovered.

## Fake Items 🪤

Be aware that some objects may not be what they appear to be. Pokémon will sometimes disguise themselves as objects in order to trick you.

---

# Looking for the old mod? (Loot Balls)

If you are looking for the old mod, you can still find the files and docs in the [Github Repository for Loot Balls](https://github.com/ResistorCat/cobblemon-loot-balls). Keep in mind that the old mod is only compatible with `Cobblemon 1.5.x` versions and is no longer maintained.
