# CHANGELOG

This document outlines the changes and fixes made in each version of the CobbleLoots mod from version a2.0.10 onwards.

## b2.2.0

### Changes

- **In-Game Configuration**: Implemented full in-game configuration support using MidnightLib.
  - Users can now edit mod settings directly from the game menu (Fabric via ModMenu, NeoForge via Mods menu).
  - All configuration options are accessible and categorized for better usability.
- **Legacy Config Migration**: Added an automatic migration system for existing `cobbleloots.yaml` files.
  - On first launch with the new version, old configuration values will be preserved and migrated to the new format.
  - The old `cobbleloots.yaml` file will be renamed to `cobbleloots.yaml.old` after successful migration.
- **Localization**: Added full localization support for configuration keys (`en_us.json`).
- **Dependencies**:
  - Switched internal configuration library to MidnightLib for better cross-platform support.
- **Cleanup**: Removed unused `xp_amount` configuration key to avoid confusion.
- **Fishing Update**:
  - Implemented fishing as a new loot ball source.
  - Players can now catch loot balls when fishing with Cobblemon Poké Rods.
  - Different rods can catch different tiers of loot balls (e.g. Master Rod is required for Master Loot Balls).
  - Using the "Luck of the Sea" enchantment increases the chance of finding a loot ball.
  - Expanded fishing sources to: **Dive**, **Safari**, **Rainbow**, and **Pumpkin** loot balls.
  - Added new **Lure Loot Ball**: Exclusive to fishing with a Lure Rod, contains sea treasures.
  - Added configurable despawn delay for fished loot balls.
  - Fished loot balls now move towards the player (similar to items).

## a2.1.0

### Changes

- Updated the mod to be compatible with Cobblemon 1.7.
  - Updated dependencies to the same versions used in the official Cobblemon 1.7 repository.

## a2.0.10

### Changes

- Added a new `date` filter to allow loot balls to spawn only during a specific date range.
  - Accepts `from` and `to` parameters in `MM-DD` format (e.g., `01-01` for January 1st). Those are inclusive.
- Added a new seasonal [Pumpkin Loot Ball](https://resistorcat.github.io/cobbleloots/loot_balls/ball_types/pumpkin/) for Halloween.
  - Spawns from October 24th to November 2nd.
  - Can be found in spooky biomes in the Overworld and also in the Nether.
  - Contains exclusive Halloween-themed loot, including unique banners, candies, and other surprises.
- Updated documentation for loot balls to improve clarity and consistency.
- Updated the mod logo to a Halloween-themed version.
- Added a new configuration option to disable specific loot balls via the config file.
  - This allows server admins to easily manage which loot balls are active without modifying data packs.
  - The key for this config is `loot_ball.disabled.loot_balls`, and it accepts a list of loot ball IDs or patterns (e.g., `cobbleloots:*` to disable all CobbleLoots loot balls, or `cobbleloots:loot_ball/poke` to disable only the "poke" loot ball and its variants).
