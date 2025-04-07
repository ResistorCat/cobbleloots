# Cobbleloots ALPHA-2.0.4

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the GitHub repository.

## Changes
- Updated the creative loot ball item.
  - Now, they have a custom model and texture depending on the loot ball data they hold.
  - Updated its tooltip to show the loot ball data attached to it.
  - `CustomData` item component is now used to store the loot ball data that will be added to the loot ball placed with this item.
  - Added some default loot balls to the cobbleloots tab in the creative inventory.

## Fixes
- Fixed a bug where the mod would export config libraries without masking the namespace, causing conflicts with other mods.

## Known Issues
- Bonus loot balls invisibility is not working as intended.