### Gameplay Changes

- **Fishing Loot Balls Rebalance**: Rebalanced fishing rates and aquatic variant distribution to make fishing a more rewarding way to obtain Loot Balls:
  - Increased the default base fishing chance (`fishing_chance`) from `1%` (`0.01`) to `5%` (`0.05`).
  - Increased the default Luck of the Sea multiplier (`fishing_luck_of_the_sea_multiplier`) from `1.25` to `1.4` per level (scaling up to `7.0%` at Level I, `9.8%` at Level II, and `13.72%` at Level III).
  - Expanded and boosted aquatic fishing weights for **Dive**, **Lure**, and **Net** Loot Balls across deep oceans, oceans, coasts, rivers, and swamps, and added any-biome support when using a **Lure Rod**.
  - Reduced the starter Poké Rod weight on **Poké Loot Balls** (`100` → `25`) so aquatic Loot Balls stand out when fishing in marine and fluvial biomes.

### Bug Fixes

- **Multi-Rule Loot Ball Weight Resolution**: Fixed an issue where matching multiple source rules for the same Loot Ball caused later generic fallback rules to overwrite higher-weight rod or biome-specific rules (affecting Dive, Lure, Azure, and other specialized Poké Rod catches).
- **Zero-Chance Fishing Boundary**: Clamped calculated fishing probability to `[0.0, 1.0]` and ensured setting `fishing_chance` to `0` reliably prevents fishing loot ball spawns.
