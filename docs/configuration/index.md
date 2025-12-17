---
icon: material/cog
---

# Configuration

!!! warning
    This page was generated with the help of artificial intelligence. It may contain errors or inaccuracies.


Cobbleloots configuration can be edited in-game using the **Mods** menu (requires ModMenu on Fabric) or by editing the `config/cobbleloots.json` file manually.

## Experience (XP)

Settings related to Experience Points rewarded by loot balls.

| Key | Default | Description |
| :--- | :--- | :--- |
| `xp_enabled` | `true` | If enabled, loot balls can give XP to the player upon opening. |

## Bonus Content

Settings for the "Bonus Loot" mechanic, which gives players a chance to get extra items.

| Key | Default | Description |
| :--- | :--- | :--- |
| `bonus_enabled` | `true` | If enabled, opening a loot ball has a chance to trigger a bonus. |
| `bonus_chance` | `0.1` (10%) | The probability (0.0 - 1.0) of a bonus triggering. |
| `bonus_multiplier` | `2.0` | The multiplier applied to the item count when a bonus is triggered. |
| `bonus_invisible` | `true` | Determines if bonus loot balls should be invisible (if applicable to the specific variant). |

## World Generation

Settings for loot balls generating naturally in the world during terrain generation.

| Key | Default | Description |
| :--- | :--- | :--- |
| `generation_enabled` | `true` | Master switch for loot ball world generation. |
| `generation_chance` | `0.0513` | The chance for a loot ball to generate in a valid chunk. |
| `generation_attempts` | `2` | Number of attempts to place a loot ball per chunk. |
| `generation_chunk_cap` | `4` | Maximum number of loot balls allowed to generate in a single chunk. |

## Spawning

Settings for loot balls spawning dynamically over time (similar to mob spawning).

| Key | Default | Description |
| :--- | :--- | :--- |
| `spawning_enabled` | `true` | Master switch for dynamic loot ball spawning. |
| `spawning_chance` | `0.25` (25%) | The chance for a spawn attempt to succeed. |
| `spawning_cooldown_min` | `6000` | Minimum time (in ticks) between spawn attempts (300 ticks = 15 seconds). |
| `spawning_cooldown_max` | `36000` | Maximum time (in ticks) between spawn attempts. |
| `spawning_despawn_enabled`| `true` | If enabled, spawned loot balls will eventually disappear if not collected. |
| `spawning_despawn_time` | `24000` | Time (in ticks) before a spawned loot ball despawns (24000 ticks = 20 minutes). |

## Survival Settings

Settings affecting how players interact with loot balls in Survival mode.

| Key | Default | Description |
| :--- | :--- | :--- |
| `survival_drop_enabled` | `true` | If enabled, players can break loot balls to pick them up as items. |
| `survival_drop_automatic` | `true` | If enabled, loot balls will drop as items automatically when their uses are exhausted (unless destroyed). |
| `survival_destroy_looted` | `false` | If enabled, loot balls are completely destroyed (vanish) when their uses are exhausted. Overrides `survival_drop_automatic`. |

## Loot Ball Defaults

Default values used for new loot balls if not specified in their data files.

| Key | Default | Description |
| :--- | :--- | :--- |
| `defaults_uses` | `1` | Default number of times a loot ball can be opened. |
| `defaults_multiplier` | `1.0` | Default item multiplier. |
| `defaults_xp` | `0` | Default amount of XP contained in a loot ball. |
| `defaults_player_timer` | `0` | Default cooldown (in ticks) for a player to re-open a multi-use loot ball. |
| `defaults_despawn_tick` | `0` | Default despawn time (0 = never despawn). |

## Disabled Features

Lists to disable specific features in certain dimensions or globally.

- **`disabled_dimensions_generation`**: List of dimension IDs where world generation is disabled.
- **`disabled_dimensions_spawning`**: List of dimension IDs where spawning is disabled.
- **`disabled_dimensions_fishing`**: List of dimension IDs where fishing loot is disabled.
- **`disabled_dimensions_archaeology`**: List of dimension IDs where archaeology loot is disabled.
- **`disabled_loot_balls`**: List of loot ball IDs or patterns to disable (e.g., `cobbleloots:poke_ball`).
