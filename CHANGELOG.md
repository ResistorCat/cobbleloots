# Cobbleloots ALPHA-2.0.6

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the GitHub repository.

## Changes
- Added new configurations:
  - `loot_ball.xp.enabled`: Whether to enable loot ball XP when a player opens a loot ball. Default: `true`.
  - `loot_ball.xp.amount`: The amount of XP to give when a player opens a loot ball. Default: `5`.
- Changed configuration keys:
  - `loot_ball.despawn.*` configurations are now under `loot_ball.spawning.despawn.*`.
- Now, the boolean NBT tags for loot balls are always saved regardless of the value. This affects the `Invisible` and `Sparks` tags.

## Fixes
- Fixed a bug where the loot ball despawn enabling/disabling configuration didn't work properly. The game would always use the default value of `true` regardless of the configuration.
- Fixed a bug where the game would crash if the configurations were of the wrong type. Now the game will log an error message and use the default value instead.
- Fixed a bug where loot ball invisibility didn't work properly and were losing their tag on world reloads and generations.