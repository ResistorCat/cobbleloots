# Cobbleloots ALPHA-2.0.1

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the GitHub repository.

## Changes

### Loot Ball Generation and Spawning
- Loot balls can now be generated with new chunks in the world. The generation is based on biome tags filters and height filters.
  - The generation can be configured in data packs and enabled or disabled for individual loot balls. By setting the `type` entry to `generated` in a source definition, the loot ball will be generated in the world.
  - The generator will try to spawn a loot ball in a section of a chunk. By default, 3 attempts are made to spawn a loot ball in a chunk.
  - Reduced the performance impact of loot ball generation compared to the previous mod, by checking smaller sections of the world.
- Loot balls can now spawn nearby a random player. The spawning uses the same biome tags filters and height filters as generation, but they can be configured separately in data packs.
  - Every certain amount of time, the mod will try to spawn a loot ball near a random player. By default, the mod will try to spawn a loot ball every 5-30 minutes.

### Loot Ball
- Reverted changes to item obtaining in survival. Now, the item will be directly added to the player's inventory instead of dropping it on the ground. If the player's inventory is full, the item will be dropped on the ground.
  - In the future, I will add a config option to choose between these two behaviors.

### Loot Ball NBT
- Added new nbt tag `DespawnTick`. This tag is used to store the tick when the loot ball will despawn. This tag is used to prevent **spawned** loot balls from staying in the world indefinitely. Generated loot balls doesn't have this tag, so they will not despawn.

### Tags
- Added new internal biome and block tags for loot ball generation and spawning.

### Loot Ball Data
- Changed `biome` entry data type in `sources` definitions from `ResourceLocation` to `TagKey<Biome>` for better performance. From now on, biome filters should point to biome tags instead of biome IDs. If no biome filter is specified, the loot ball will be available in all biomes.
- Added an experimental boolean `announce` entry. If set to `true`, when the loot ball is spawned, a message will be sent to all players on the server.
