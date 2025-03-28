# Cobbleloots ALPHA-2.0.3

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the GitHub repository.

## Changes
- Added a configuration file to customize the mod's features.
  - A new configuration file is generated in the `config/cobbleloots` folder when the mod is loaded for the first time.
  - At the moment, the configuration file is missing a lot of features, but it will be expanded in future versions.
- Added a chunk cap to loot ball generation which can be configured in the configuration file.

### Configurations

> **NOTE**: The configuration file is in YAML format, so keys are referenced using a dot notation.

#### Loot Ball Bonus
- `loot_ball.bonus.enabled`: Enables/disables the bonus feature for loot balls (default: true)
- `loot_ball.bonus.chance`: Chance for a loot ball to provide a bonus (0-1, default: 0.1)
- `loot_ball.bonus.multiplier`: Multiplier applied to loot when bonus is active (default: 2.0)
- `loot_ball.bonus.invisible`: Whether bonus loot balls are invisible (default: true)

#### Loot Ball Generation
- `loot_ball.generation.enabled`: Enables/disables loot ball generation in chunks (default: true)
- `loot_ball.generation.chance`: Chance for loot ball generation on each attempt (0-1, default: 0.0625)
- `loot_ball.generation.attempts`: Number of attempts to generate loot balls per chunk (min: 1, default: 2)
- `loot_ball.generation.chunk_cap`: Maximum number of loot balls that can be generated per chunk (min: 1, default: 4)

#### Loot Ball Spawning
- `loot_ball.spawning.enabled`: Enables/disables periodic loot ball spawning near players (default: true)
- `loot_ball.spawning.chance`: Chance for a loot ball to spawn near a random player (0-1, default: 0.25)
- `loot_ball.spawning.cooldown.min`: Minimum ticks between loot ball spawn attempts (min: 0, default: 6000)
- `loot_ball.spawning.cooldown.max`: Maximum ticks between loot ball spawn attempts (min: 0, default: 36000)

#### Loot Ball Despawn
- `loot_ball.despawn.enabled`: Enables/disables loot ball despawning (default: true)
- `loot_ball.despawn.time`: Time in ticks before a loot ball despawns (min: 0, default: 24000)

## Fixes
- Fixed a bug where the server would crash when a loot ball spawn attempt happened without players connected.
- Fixed an error in the calculation of nearby chunks for loot ball spawning.
- Fixed a visual bug where loot balls would always render with poké ball texture instead of the correct loot ball texture from custom data.
