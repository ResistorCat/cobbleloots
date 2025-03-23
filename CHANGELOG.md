# Cobbleloots ALPHA-2.0.1

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the GitHub repository.

## Changes

### Loot Ball Data
- Changed `biome` entry data type in `sources` definitions from `ResourceLocation` to `TagKey<Biome>` for better performance. From now on, biome filters should point to biome tags instead of biome IDs. If no biome filter is specified, the loot ball will be available in all biomes.
- Added an experimental boolean `announce` entry. If set to `true`, when the loot ball is spawned, a message will be sent to all players on the server.
- 