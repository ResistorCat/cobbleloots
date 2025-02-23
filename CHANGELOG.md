# Cobbleloots ALPHA-2.0.0

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the GitHub repository.

> **Note**: This mod replaces my previous mod `Cobblemon: Loot Balls`, but it is not compatible with the old loot balls from the other mod, so I suggest backing up items from previous worlds.

## Changes from previous mod

- The Loot Balls code was completely redone from scratch. From now on, the mod will be called Cobbleloots, to avoid confusion with the old **incompatible** versions.
  - Loot Balls are now **entities** instead of blocks.
  - Loot Ball generation and survival sources **aren't implemented yet**. They will be added in future alpha versions.
  - Loot Balls **now doesn't disappear after being opened**. They will stay in the world until they are destroyed by the players. This change was made to allow players to use the loot balls as **decorations** in future versions.
    - Loot Balls doesn't drop themselves when destroyed. This will be added in future versions.
  - Loot Balls items **now drop into the world** instead of being directly added to the player's inventory.

## New Features

- Loot Balls now have new sounds and animations.
  - The opening sound was updated to be more in line with the original Pokémon games.
  - The opening animation was updated to be more fluid and visually appealing.
  - Various sounds were added/updated for different actions on the loot balls.

### Mapmaking

- The mod now has a [documentation](https://resistorcat.github.io/cobbleloots/) to help mapmakers use the mod objects and features in their custom maps. This will be updated as new features are added.
  - At the moment of this release, the documentation is still a work in progress. If you have any questions, feel free to ask on my discord server. I will update the documentation as soon as possible.
- Added a new `Loot Ball Data` system to help mapmakers create custom loot balls with different properties.
  - You can now create custom loot balls trough datapacks! Check the documentation for more information.
  - The system is **still a work in progress**, and it may change in future versions.
  - The system is not yet implemented in survival sources nor generation. It will be added in future alpha versions.
  - You can customize the loot table, texture, sources, rarity, biomes and more. Some of these features are not yet implemented, but you can write the data for future use.
- Reworked the `Openers` tag to allow more flexibility for mapmakers.
  - The system is still a work in progress, and it may change in future versions.
  - It now stores the `UUID` and the `Timestamp` of the player that opened the loot ball. This will be used in future versions to add more features to the mod, like **cooldowns** and **re-usable** loot balls.
