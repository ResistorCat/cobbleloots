# CHANGELOG

This document outlines the changes and fixes made in each version of the CobbleLoots mod from version a2.0.10 onwards.

## b2.2.0

### Gameplay Changes

- **Added Loot Ball Fishing**:
  - Implemented fishing as a new loot ball source.
  - Players can now catch loot balls when fishing with Cobblemon Poké Rods.
  - Different rods can catch different tiers of loot balls (e.g. Master Rod is required for Master Loot Balls).
  - Using the _Luck of the Sea_ enchantment increases the chance of finding a loot ball.
  - Added configurable despawn delay for fished loot balls.
  - Fished loot balls move towards the player (similar to items).
- **Loot Ball Balance Overhaul**: All loot tables and loot ball definitions were reworked to be more balanced.
  - Split basic ball variants from Poké Ball into standalone files:
    - **Poké**: Can now be found everywhere, spawns in all biomes and can be fished with any rod (or with a _poké rod_ for higher chances).
    - **Azure**: Can now be found from generation and spawning sources in oceans, coasts and rivers. It can be fished with any rod in those biomes (or with an _azure rod_ for higher chances).
    - **Citrine**: Can now be found from generation and spawning sources in deserts, badlands and savannas. It can be fished with any rod in those biomes (or with a _citrine rod_ for higher chances).
    - **Verdant**: Can now be found from generation and spawning sources in forests and plains. It can be fished with any rod in those biomes (or with a _verdant rod_ for higher chances).
    - **Roseate**: Can now be found from generation and spawning sources in floral and lush biomes. It can be fished with any rod in those biomes (or with a _roseate rod_ for higher chances).
- **Opening Effects**: Added special effects when opening loot balls:
  - Display the received item floating above the loot ball.
  - Particle effects on open (customizable via config).

### Technical Changes

- **In-Game Configuration**: Implemented full in-game configuration support using MidnightLib.
  - Users can now edit mod settings directly from the game menu (Fabric via ModMenu, NeoForge via Mods menu).
  - All configuration options are accessible and categorized for better usability.
- **Legacy Config Migration**: Added an automatic migration system for existing `cobbleloots.yaml` files.
  - On first launch with the new version, old configuration values will be preserved and migrated to the new format.
  - The old `cobbleloots.yaml` file will be renamed to `cobbleloots.yaml.old` after successful migration.
- **Localization**:
  - Added full localization support for configuration keys (`en_us.json`).
  - Added translation keys for new messages.
- **Dependencies**:
  - Switched internal configuration library to MidnightLib for better cross-platform support.
- **Cleanup**: Removed unused `xp_amount` configuration key to avoid confusion.
- **Updated biome filters**: Biome filters now accept:
  - _Biome keys_: `"minecraft:swamp"`
  - _Biome tags_: `"#cobblemon:is_swamp"`
  - _Biome dicts_: `{"id": "#c:is_ocean", "required": false}`
  - _Lists_: Mix of any of the above
- **New Configuration**: Added `loot_ball.defaults.effects_enabled` to toggle opening effects.

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
