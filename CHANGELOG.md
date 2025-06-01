# Cobbleloots ALPHA-2.0.7

> **IMPORTANT**: Alpha versions of the mod may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the Discord server.

## Changes
- Rework the Loot Ball Data definitions, including changes to sources and variants.
- Added `PlayerTimer` Loot Ball NBT Tag of type `Long`. Defaults to `0`.
  - It represents a value in `ticks` that the player must wait to reopen the Loot Ball.
  - A value of `0` disables this behaviour.
  - A small value (like `1`) indicates that the player can reopen the Loot Ball indefinitely.

## Fixes
- Fixed wardens no longer being able to aggro loot balls

## Technical
- All codecs were reworked for the new loot ball data format.