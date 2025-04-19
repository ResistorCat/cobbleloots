# Cobbleloots ALPHA-2.0.5

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the GitHub repository.

## Changes
- Replaced the external library for configuration with a custom one.
  - This should improve performance and reduce the number of dependencies (preventing mod conflicts).
  - No changes to the configuration format. It's still a YAML file in the same location as before.
  - This change is experimental and may cause issues. Please report any problems you encounter.

## Fixes
- Fixed a bug where the mod would crash when other mods were importing the same YAML library. Now it uses a custom library that should not conflict with other mods.

## Known Issues
- Bonus loot balls invisibility is not working as intended.