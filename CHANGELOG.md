# Cobbleloots ALPHA-2.0.3

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the GitHub repository.

## Changes
- Added a configuration file to customize the mod's features.
  - A new configuration file is generated in the `config/cobbleloots` folder when the mod is loaded for the first time.
  - At the moment, the configuration file is missing a lot of features, but it will be expanded in future versions.
- Added a chunk cap to loot ball generation which can be configured in the configuration file.

## Fixes
- Fixed a bug where the server would crash when a loot ball spawn attempt happened without players connected.
- Fixed an error in the calculation of nearby chunks for loot ball spawning.
