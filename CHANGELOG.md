# Cobbleloots ALPHA-2.0.1

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the GitHub repository.

## Changes

### Loot Ball NBT
- Added new nbt tag `DespawnTick`. This tag is used to store the tick when the loot ball will despawn. This tag is used to prevent **spawned** loot balls from staying in the world indefinitely. Generated loot balls doesn't have this tag, so they will not despawn.

### Tags
- Added new internal biome and block tags for loot ball generation and spawning.

### Loot Ball Data
- Changed `biome` entry data type in `sources` definitions from `ResourceLocation` to `TagKey<Biome>` for better performance. From now on, biome filters should point to biome tags instead of biome IDs. If no biome filter is specified, the loot ball will be available in all biomes.
- Added an experimental boolean `announce` entry. If set to `true`, when the loot ball is spawned, a message will be sent to all players on the server.
