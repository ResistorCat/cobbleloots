# Cobblemon: CobbleLoots

Cobbleloots is a mod for Cobblemon that adds different ways to obtain items based on the original Pokémon video games.

> **IMPORTANT (BETA-2.2.0+):** Recent updates have introduced breaking changes, especially with the configuration. You will need to update your configurations to ensure compatibility.

<!-- TOC -->

- [Cobblemon: CobbleLoots](#cobblemon-cobbleloots)
- [Features](#features)
  - [Loot Balls 💎](#loot-balls-)
    - [Obtaining Loot Balls](#obtaining-loot-balls)
  - [Mapmaking 🗺️](#mapmaking-)
    - [Custom Loot Balls 🎁](#custom-loot-balls-)
  - [Mod configuration ⚙️](#mod-configuration-)
- [Planned Features](#planned-features)
  - [Commands 🛠️](#commands-)
  - [Hidden Items 🔍](#hidden-items-)
  - [Fake Items 🪤](#fake-items-)
<!-- TOC -->

---

# Features

## Loot Balls 💎

These balls contain useful items for the player. They can grant XP and items when opened, and can be used as a decorative entity in survival mode. When a loot ball has no remaining uses, it can be destroyed to drop as a decorative item (this behavior is configurable).

### Obtaining Loot Balls

- **Generation**: Loot balls can be found in the world. Some of them are specific to biomes, while others are more common.
- **Spawning**: Over time, loot balls will spawn in the world around random players.
- **Fishing**: Players can catch loot balls when fishing with Cobblemon Poké Rods. Different rods can catch different tiers of loot balls, and using the _Luck of the Sea_ enchantment increases the chance of finding a loot ball.

Loot balls are categorized into tiers that determine their rarity and loot quality: **Common**, **Uncommon**, **Rare**, and **Ultra Rare**.

## Mapmaking 🗺️

Check out the [documentation](https://resistorcat.github.io/cobbleloots/) to learn how to use the mod objects and features in your custom maps. If you still have questions, feel free to ask on my discord server.

> **Note**: The documentation is a work in progress, and some features may not be fully documented yet. If you need help with a specific feature, please reach out on my [Discord server](https://discord.gg/kbykWUH5dV).

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
  - `biome` tags (biome keys, biome tags, or biome dicts).
  - `dimension` lists.
  - `block` tags (for spawn block or base block).
  - `fluid` tags.
  - `position` (x, y, z coordinates with min/max ranges).
  - `light` levels (block and sky, for spawning).
  - `time` of day (with period options).
  - `date` ranges (e.g., seasonal events).
  - `weather` conditions (clear, rain, thunder).
- **Variants** for a single loot ball type, each with its own optional name, texture, and loot table.

Some of these features are continuously being refined, but the data system is designed for flexibility and future additions.

## Mod configuration ⚙️

The mod configuration is powered by **MidnightLib** and supports in-game editing:

- **Fabric**: Edit via **ModMenu**.
- **NeoForge**: Edit via the Mods menu.
- **Command**: Server OPs can use `/midnightconfig cobbleloots <key> <value>`.

The config file is located in the `config` folder of your Minecraft instance. Check the [Configuration](https://resistorcat.github.io/cobbleloots/guides/configuration/) docs for more information.

---

# Planned Features

## Commands 🛠️

Commands to help mapmakers and server owners to manage the mod features.

## Hidden Items 🔍

Inspired by the original Pokémon games, Hidden Items are invisible objects scattered across the world, waiting to be discovered.

## Fake Items 🪤

Be aware that some objects may not be what they appear to be. Pokémon will sometimes disguise themselves as objects in order to trick you.
