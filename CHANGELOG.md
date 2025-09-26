# Cobbleloots ALPHA-2.0.9

> **REMEMBER**: Alpha versions may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the Discord server.

## Changes
- Updated the Creative Mode tab icon to use a Master Ball texture for better visual representation.
- Updated the default Loot Ball texture to use the Strange Ball texture when no specific texture is set for debugging purposes.
    - If you encounter a Strange Ball, it indicates that the Loot Ball is not properly configured or has missing data.
- Added loot ball JSON definition for entity icon rendering in Xaero's Minimap.
    - This allows the minimap to display loot balls with their respective textures, enhancing visibility and identification in-game.

## Fixes
- Fixed an issue where the Loot Ball Item texture would not display correctly if the texture data was missing or invalid.
    - Note that if the texture data is missing, the Loot Ball will now display the Strange Ball texture as a fallback. Be sure to configure your Loot Balls properly when using commands to obtain them.
