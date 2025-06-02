# Cobbleloots ALPHA-2.0.7

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the Discord server.

## Changes
- Rework the Loot Ball Data definitions, including changes to sources and variants.
- Added `PlayerTimer` Loot Ball NBT Tag of type `Long`. Defaults to `0`.
  - It represents a value in `ticks` that the player must wait to reopen the Loot Ball.
  - A value of `0` disables this behaviour.
  - A small value (like `1`) indicates that the player can reopen the Loot Ball indefinitely.
- Added new configs:
  - `loot_ball.defaults.uses`: Default amount of uses a Loot Ball has. Defaults to `1`.
  - `loot_ball.defaults.multiplier`: Default loot multiplier for Loot Balls. Defaults to `1.0`.
  - `loot_ball.defaults.player_timer`: Default player timer in ticks. Defaults to `0`.
  - `loot_ball.defaults.despawn_tick`: Default despawn tick timer. Defaults to `0`.
  - `loot_ball.survival.drop.enabled`: Enable Loot Balls to drop as decorative items in survival mode. Defaults to `true`.
- Added the ability for Loot Balls to drop as decorative items in survival mode.
  - When a loot ball has no remaining uses, you can destroy them in survival and the loot ball will drop as a decorative item.
  - This change is currently work in progress, so expect some bugs.
  - You can disable this with the new `loot_ball.survival.drop.enabled` config.

## Fixes
- Fixed a bug where wardens could target and attack loot balls; now wardens ignore loot balls as intended.
- Fixed loot balls not despawning when falling into the world void.
- Fixed loot balls not being killed using the `/kill` command.

## Technical
- All codecs were reworked for the new loot ball data format.
- Optimized some internal game detections for loot ball entity targetting and damage.
- Add a limit to random chunk search to optimize loot ball generation.
- Removed internal block check for valid position when spawning a loot ball. This is replaced now by loot ball definition filters.
