# CHANGELOG

This document outlines the changes and fixes made in each version of the CobbleLoots mod from version a2.0.10 onwards.

## b2.2.2

### Bug Fixes

- Fixed NeoForge dedicated server crash caused by client-only config screen registration (`IConfigScreenFactory`) being loaded on the server side.

## b2.2.1

### Bug Fixes

- Fixed a NeoForge crash when another mod (e.g. Farming for Blockheads) also bundles MidnightLib, causing a split-package module resolution error.

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
  - New tiers for loot balls were defined:
    - **Common**: More frequent, with simpler loot tables.
    - **Uncommon**: Less frequent, with more complex loot tables.
    - **Rare**: Rare, with more valuable loot tables.
    - **Ultra Rare**: Very rare, with the most valuable loot tables.
  - Ball variants from Poké Ball were split into standalone files.
  - Added 6 new loot balls; Lure, Dusk, Quick, Timer, Nest and Net.
  - **Common Loot Balls**:
    - **Poké**: Can now be found everywhere, spawns in all biomes and can be fished with any rod (or with a _poké rod_ for higher chances).
    - (NEW) **Azure**: Can now be found from generation and spawning sources in oceans, coasts and rivers. It can be fished with any rod in those biomes (or with an _azure rod_ for higher chances).
    - (NEW) **Citrine**: Can now be found from generation and spawning sources in deserts, badlands and savannas. It can be fished with any rod in those biomes (or with a _citrine rod_ for higher chances).
    - (NEW) **Verdant**: Can now be found from generation and spawning sources in forests and plains. It can be fished with any rod in those biomes (or with a _verdant rod_ for higher chances).
    - (NEW) **Roseate**: Can now be found from generation and spawning sources in floral and lush biomes. It can be fished with any rod in those biomes (or with a _roseate rod_ for higher chances).
    - (NEW) **Slate**: Can now be found from generation and spawning sources in caves and volcanic biomes. It can be fished with any rod in those biomes (or with a _slate rod_ for higher chances).
    - **Premier**: Can now be found from generation and spawning sources in mountains. It can be fished with any rod in those biomes (or with a _premier rod_ for higher chances).
  - **Uncommon Loot Balls**:
    - **Great**: Can now be found everywhere, spawns in all biomes and can be fished with any rod (or with a _great rod_ for higher chances).
    - **Dive**: Can now be found from generation and spawning sources in oceans and deep oceans. It can be fished with any rod in those biomes (or with a _dive rod_ for higher chances).
    - **Heal**: Can now be found from generation and spawning sources everywhere in the overworld dimension. It can be fished with any rod in the overworld (or with a _heal rod_ for higher chances).
    - **Safari**: Can now be found from generation and spawning sources in savanna biomes. It can be fished with any rod in those biomes (or with a _safari rod_ for higher chances).
    - **Pumpkin**: Can now be found from generation and spawning sources in biomes defined in the pumpkin biome tag and in the nether dimension (It only spawns during night in the overworld biomes). It can be found only from october 1st to november 1st. It can be fished anywhere with any rod during the same date range.
    - **Rainbow**: Can now be found from generation and spawning sources in biomes defined in the rainbow biome tag. It only spawns during day with "direct" sunlight (sky light level 7 or higher). It can be fished with any rod in those biomes.
    - (NEW) **Lure**: Can be fished with any rod in deep ocean biomes (or with a _lure rod_ for higher chances). It doesn't spawn naturally nor can be obtained from generation sources, as it is exclusively obtained from fishing.
    - (NEW) **Nest**: Can be found from generation and spawning sources in forests and swamps. It can be fished with any rod in those biomes (or with a _nest rod_ for higher chances).
    - (NEW) **Net**: Can be found from generation and spawning sources in oceans, swamps and rivers. It can be fished with any rod in those biomes (or with a _net rod_ for higher chances).
    - (NEW) **Quick**: Can be found from generation and spawning sources in plains and sky biomes, and in the end dimension. It can be fished (only in the overworld) with any rod in those biomes (or with a _quick rod_ for higher chances).
    - (NEW) **Timer**: Can be found from generation and spawning sources in badlands biomes and in the nether dimension. It can be fished (only in the overworld) with any rod in those biomes (or with a _timer rod_ for higher chances).
  - **Rare Loot Balls**:
    - **Ultra**: Can now be found everywhere, spawns in all biomes and can be fished with an _ultra rod_.
    - **Luxury**: Can now be found everywhere in the end dimension from generation and spawning sources. It can't be fished.
    - (NEW) **Dusk**: Can be found from generation sources in spooky and deep dark biomes. It can spawn in those biomes in dark places (low light level). It can be fished during night with any rod in those biomes (or with a _dusk rod_ for higher chances).
  - **Ultra Rare Loot Balls**:
    - **Master**: Can now be found everywhere, spawns in all biomes and can be fished with a _master rod_.
- **Opening Effects**: Added special effects when opening loot balls:
  - Display the received item floating above the loot ball.
  - Particle effects on open (customizable via config).

### Technical Changes

- **Configuration Overhaul**: All configurations options were refactored to use a new configuration system.
  - Implemented full in-game configuration support for Fabric via **ModMenu** and NeoForge via Mods menu.
  - Server OPs can now edit the configuration in-game using `/midnightconfig cobbleloots <key> <value>`.
  - On first launch with this version, old configuration values will be preserved and migrated to the new format.
    - The old `cobbleloots.yaml` file will be renamed to `cobbleloots.yaml.old` after successful migration.
  - Check the new [Configuration](https://resistorcat.github.io/cobbleloots/guides/configuration/) docs for more information.
- **Localization**:
  - Added full localization support for configuration keys (`en_us.json`).
  - Added translation keys for new messages.
  - Added Brazilian Portuguese (`pt_br.json`) translation. Initial base keys contributed by [PrincessStelllar](https://github.com/PrincessStelllar) via [PR #21](https://github.com/ResistorCat/cobbleloots/pull/21).
- **Dependencies**:
  - Switched internal configuration library to **MidnightLib** for better cross-platform support.
- **Updated Biome Filters**: Biome filters now accept:
  - _Biome keys_: `"minecraft:swamp"`
  - _Biome tags_: `"#cobblemon:is_swamp"`
  - _Biome dicts_: `{"id": "#c:is_ocean", "required": false}`
  - _Lists_: Mix of any of the above
- **Updated Documentation**: Updated the mod documentation.
  - New and updated pages for all loot balls, showing general information, loot table and obtaining methods.
  - Updated Reference page with updated biome tag information.
  - Added a new Configuration page with updated information about the configuration system.
  - Updated the mod guides and examples to reflect the new changes.

### Bug Fixes

- Fixed a bug where disabled loot balls were not being disabled after a `/reload` command and only after a server restart.
- Fixed a bug where reloading loot balls with `/reload` would not refresh loot ball client data on existing entities.

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
