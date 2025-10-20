# CHANGELOG

This document outlines the changes and fixes made in each version of the CobbleLoots mod from version a2.0.10 onwards.

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

### Fixes

> **REMEMBER**: Alpha versions may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the Discord server.
