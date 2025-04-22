# Cobbleloots ALPHA-2.0.6

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the Discord server.

## Changes
- Added new configurations:
  - `loot_ball.xp.enabled`: Whether to enable loot ball XP when a player opens a loot ball. Default: `true`.
  - `loot_ball.xp.amount`: The amount of XP to give when a player opens a loot ball. Default: `5`.
- Changed configuration keys:
  - `loot_ball.despawn.*` configurations are now under `loot_ball.spawning.despawn.*`.
- Now, the boolean NBT tags for loot balls are always saved regardless of the value. This affects the `Invisible` and `Sparks` tags.
- Reduced the default configuration value for loot ball generation chance from `0.0625` to `0.0513`.
  - This means that the chance for a chunk to generate a loot ball or more (with 2 attempts per chunk) is now `~10%` instead of `~12%`.
- Updated loot balls weights:
  - Heal: `25` -> `20`
  - Great: `25` -> `20`
  - Dive: `25` -> `30`

## Fixes
- Fixed a bug where the loot ball despawn enabling/disabling configuration didn't work properly. The game would always use the default value of `true` regardless of the configuration.
- Fixed a bug where the game would crash if the configurations were of the wrong type. Now the game will log an error message and use the default value instead.
- Fixed a bug where loot ball invisibility didn't work properly and were losing their tag on world reloads and generations.
- Fixed a bug where setting the loot ball uses to negative values (infinite uses) would not work properly, removing its contents and making it unusable. Now, negative values effectively make the loot ball infinite.
- Fixed a bug where loot balls would show a name tag when the language keys were different from default values. Now, it will only render its tag if a custom name is set with NBT.