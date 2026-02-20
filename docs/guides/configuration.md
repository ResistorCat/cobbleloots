---
icon: material/cog
---

# Configuration

Cobbleloots configuration can be edited in-game using the **Mods** menu (requires [ModMenu](https://modrinth.com/mod/modmenu) on Fabric) or by editing the `config/cobbleloots/cobbleloots.yaml` file manually.

> **Note for server operators:** Server OPs can edit the configuration in-game using `/midnightconfig cobbleloots <key> <value>`.

## Manual Configuration

The following options are available:

### Gameplay

These settings apply to all loot balls in the game.

| Key                             | Type         | Default          | Description                                                                                |
| ------------------------------- | ------------ | ---------------- | ------------------------------------------------------------------------------------------ |
| `loot_ball_empty_behavior`      | Enum         | `DROP_AUTOMATIC` | Determines behavior when empty (`DROP_AUTOMATIC`, `DROP_MANUAL`, `DESTROY`, `KEEP`).       |
| `loot_ball_bonus_chance`        | Float        | `0.1`            | Probability (from 0.0 to 1.0) of any spawned/generated/fished loot ball being a bonus one. |
| `loot_ball_bonus_multiplier`    | Float        | `2.0`            | Item multiplier when opening a bonus loot ball.                                            |
| `loot_ball_bonus_invisible`     | Boolean      | `true`           | If true, bonus loot balls are spawned invisible to make them harder to find.               |
| `loot_ball_effects_enabled`     | Boolean      | `true`           | If true, particles and sounds appear when opening.                                         |
| `loot_ball_xp_enabled`          | Boolean      | `true`           | If true, loot balls can drop experience.                                                   |
| `data_pack_disabled_loot_balls` | List(String) | `[]`             | List of loot ball IDs to disable (e.g., `cobbleloots:loot_ball/poke`).                     |

### Creative

These settings apply as the default values for new loot balls, including the ones from creative menu. Some settings are overriden by the loot ball's data pack definition.

| Key                                 | Type    | Default | Description                                                                    |
| ----------------------------------- | ------- | ------- | ------------------------------------------------------------------------------ |
| `loot_ball_default_xp`              | Integer | `0`     | Default XP reward. Overriden by the loot ball's data pack definition.          |
| `loot_ball_default_uses`            | Integer | `1`     | Default amount of uses. Negative values make it infinite.                      |
| `loot_ball_default_multiplier`      | Float   | `1.0`   | Default item multiplier.                                                       |
| `loot_ball_default_player_cooldown` | Integer | `0`     | Default player cooldown in ticks to reopen. If 0, the player cannot reopen it. |
| `loot_ball_default_despawn_tick`    | Integer | `0`     | Default time in ticks before it despawns (0 = never).                          |

### Sources

Settings regulating where and how Loot Balls appear in the world naturally.

| Key                                  | Type         | Default  | Description                                                         |
| ------------------------------------ | ------------ | -------- | ------------------------------------------------------------------- |
| `generation_enabled`                 | Boolean      | `true`   | Enable world generation.                                            |
| `generation_chance`                  | Float        | `0.0513` | Probability of generating in a valid chunk.                         |
| `generation_attempts_per_chunk`      | Integer      | `2`      | Placement attempts per chunk.                                       |
| `generation_chunk_cap`               | Integer      | `4`      | Maximum amount of loot balls per chunk.                             |
| `generation_disabled_dimensions`     | List(String) | `[]`     | Dimensions where generation is disabled.                            |
| `spawning_enabled`                   | Boolean      | `true`   | Enable dynamic spawning around players.                             |
| `spawning_chance`                    | Float        | `0.25`   | Success probability per spawn attempt.                              |
| `spawning_cooldown_min`              | Integer      | `6000`   | Minimum ticks between spawn attempts.                               |
| `spawning_cooldown_max`              | Integer      | `36000`  | Maximum ticks between spawn attempts.                               |
| `spawning_despawn_time`              | Integer      | `24000`  | Ticks before spawned balls despawn. Set to 0 to disable despawning. |
| `spawning_disabled_dimensions`       | List(String) | `[]`     | Dimensions where spawning is disabled.                              |
| `fishing_enabled`                    | Boolean      | `true`   | Enable fishing up loot balls.                                       |
| `fishing_chance`                     | Float        | `0.01`   | Base chance to hook a loot ball.                                    |
| `fishing_luck_of_the_sea_multiplier` | Float        | `1.25`   | Multiplier applied per Luck of the Sea level.                       |
| `fishing_despawn_time`               | Integer      | `24000`  | Ticks before fished balls despawn. Set to 0 to disable despawning.  |
| `fishing_disabled_dimensions`        | List(String) | `[]`     | Dimensions where fishing them is disabled.                          |
| `archaeology_disabled_dimensions`    | List(String) | `[]`     | Dimensions where archaeology drops are disabled.                    |
