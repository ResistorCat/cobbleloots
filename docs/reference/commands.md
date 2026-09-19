---
title: Commands Reference
description: Reference guide for all in-game commands in Cobbleloots.
icon: material/console
---

# Commands Reference

Cobbleloots provides commands for server operators and mapmakers to manage loot balls, reset player claim history, and configure mod behavior in-game.

All commands require **Operator Permission Level 2** (OP) or higher.

---

## `/cobbleloots reset loot_ball`

Resets the opened state of loot balls so that players can open them again. 

This command supports **lazy reconciliation**: if chunks or entities are currently unloaded, the reset timestamp is stored in the world save data (`CobblelootsWorldData`) and reconciled automatically the moment a player interacts with any loot ball.

---

### `target` — Targeted Loot Ball

Resets the specific loot ball the player is looking at (within a 16-block line of sight).

```mcfunction
/cobbleloots reset loot_ball target [player] [restore_uses]
```

| Argument | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `[player]` | `Player \| Selector` | Executing player | Target player (`@p`, `@a`, player name, or `*` / `all`). |
| `[restore_uses]` | `Boolean` | `true` | `true`: Restores remaining uses to default (for finite balls).<br>`false`: Clears openers without modifying remaining uses. |

!!! note "Infinite Loot Balls"
    Infinite loot balls (`uses = -1`) always retain infinite uses regardless of `restore_uses`.

---

### `all` — Global Reset

Globally resets **all** loot balls in the world, including unloaded ones via world saved data.

```mcfunction
/cobbleloots reset loot_ball all [player] [restore_uses]
```

| Argument | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `[player]` | `Player \| Selector` | Executing player | Target player (`*` or `all` resets for every player). |
| `[restore_uses]` | `Boolean` | `true` | Whether to restore uses on loaded loot balls. |

---

### `entity` — Selected Entities

Resets specific loot ball entities matched by a Minecraft entity selector.

```mcfunction
/cobbleloots reset loot_ball entity <entity_selector> [player] [restore_uses]
```

| Argument | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `<entity_selector>` | `Entity Selector` | *Required* | Entity selector targeting loot balls (e.g. `@e[type=cobbleloots:loot_ball,distance=..15]`). |
| `[player]` | `Player \| Selector` | Executing player | Target player (`*` or `all` resets for every player). |
| `[restore_uses]` | `Boolean` | `true` | Whether to restore uses on the matched balls. |

---

### `undo` — Revert Last Reset

Reverts the last global or per-player reset action, restoring the previous opened state and cooldowns.

```mcfunction
/cobbleloots reset loot_ball undo [player]
```

| Argument | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `[player]` | `Player \| Selector` | Executing player | Reverts the reset for a specific player or all players (`*` / `all`). |

!!! tip "How Undo Works"
    The undo system tracks the previous reset timestamp in `CobblelootsWorldData`. If no previous reset was performed during the current world lifecycle, the command will notify you that there is no action to undo.

---

## Quick Reference & Examples

| Task | Command |
| :--- | :--- |
| Reset the loot ball in front of you for yourself | `/cobbleloots reset loot_ball target` |
| Reset the loot ball in front of you for **all** players | `/cobbleloots reset loot_ball target all` |
| Reset all loot balls on the entire server for all players | `/cobbleloots reset loot_ball all all` |
| Reset all loot balls within 10 blocks for player `Ash` | `/cobbleloots reset loot_ball entity @e[type=cobbleloots:loot_ball,distance=..10] Ash` |
| Reset the targeted ball without replenishing its uses | `/cobbleloots reset loot_ball target @p false` |
| Undo the last reset for yourself | `/cobbleloots reset loot_ball undo` |
| Undo the last global reset | `/cobbleloots reset loot_ball undo all` |

---

## Configuration Commands (`MidnightLib`)

Server operators can inspect and change mod configuration values directly in-game:

```mcfunction
/midnightconfig cobbleloots <key> [value]
```

For a complete list of available configuration options, see the [Configuration Guide](../guides/configuration.md).
