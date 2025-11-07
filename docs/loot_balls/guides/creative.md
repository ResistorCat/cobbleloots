---
title: Editing Loot Balls in Creative mode
description: How to edit Loot Balls in Creative mode, commands, and testing tips.
icon: material/brain
---

# Editing Loot Balls in Creative mode

This guide explains how to edit and test Loot Balls in Creative mode using commands and interactions. Loot Balls are special items in the Cobbleloots mod that can contain random loot, and in Creative mode, you can configure them precisely for testing or custom setups.

### 🎁 Giving Loot Ball Items

Give a configured Loot Ball item that points to a specific loot ball type and variant:

```mcfunction
/give @p cobbleloots:loot_ball[minecraft:custom_data={LootBallData:"cobbleloots:loot_ball/poke",Variant:"verdant",Texture:"cobblemon:textures/poke_balls/verdant_ball.png"}] 1
```

!!!note
    You can replace `cobbleloots:loot_ball/poke`, `verdant`, and the texture path with your desired loot ball resource location, variant key, and texture. For example, to get a Pumpkin Loot Ball, you might use `cobbleloots:loot_ball/pumpkin` and remove the `Variant` field (that loot ball doesn't have variants). Always include the `Texture` field to avoid a bug where the loot ball displays an error texture.

### 🪄 Summoning Loot Ball Entities

To summon a configured Loot Ball entity directly (useful for map placement and testing):

```mcfunction
/summon cobbleloots:loot_ball ~ ~ ~ {LootBallData:"cobbleloots:loot_ball/poke",Variant:"verdant"}
```

### 🎨 Custom Textures

Set a custom texture (overrides loot_ball/variant textures):

```mcfunction
/give @p cobbleloots:loot_ball[minecraft:custom_data={Texture:"cobbleloots:textures/loot_ball/pumpkin.png"}] 1
```

Notes:

- `minecraft:custom_data` is the component used by the mod to store custom data on items.
- For entities in `/summon` commands, the custom data fields are placed directly in the entity NBT.
- `LootBallData` specifies the loot ball type, using a resource location like `cobbleloots:loot_ball/poke`.
- `Variant` specifies a sub-variant within the loot ball type, such as `verdant` for the Poke Ball.
- `Texture` specifies the texture path for items; always include this field in `/give` commands to prevent the loot ball from displaying an error texture due to a current bug (not needed for `/summon`).

### 👆 Creative Interaction Behaviors

In Creative mode, you can interact with Loot Ball entities to test and adjust them:

- 👆 **Right-click** on a Loot Ball entity with an **empty hand** to toggle its visibility (makes it invisible or visible).
- 📦 **Right-click** on a Loot Ball entity while holding **any item** to set the loot ball's contents to that item (useful for quickly populating the ball for testing).
- ✨ **Right-click** on an **invisible** Loot Ball entity while holding a **honeycomb** to toggle spark particles.

### 🔍 Viewing and Editing Loot Ball NBT

For advanced users, you can view and edit the NBT data of Loot Ball entities using Minecraft's data commands. This allows you to inspect or modify fields like `LootBallData`, `Variant`, and other custom properties in real-time.

To get the full NBT data of the nearest Loot Ball entity:

```mcfunction
/data get entity @e[type=cobbleloots:loot_ball,limit=1,sort=nearest]
```

To modify a specific field, such as changing the loot ball type:

```mcfunction
/data modify entity @e[type=cobbleloots:loot_ball,limit=1,sort=nearest] LootBallData set value "cobbleloots:loot_ball/new_type"
```

!!!warning
    Be careful with `/data modify` commands, as incorrect changes can break the entity. Always back up your world before experimenting.

### 💾 Inspecting and Saving Configured Loot Balls

When you use **pick block** (middle-click) on a Loot Ball item or entity, or craft commands with the same `minecraft:custom_data`, the mod preserves the configuration.

To save pre-configured Loot Ball items for distribution or reuse, create item stacks with the appropriate `minecraft:custom_data` fields. You can also add `CustomName` and `Lore` components to make them easily identifiable in inventories.

For example, to create a named Verdant Poke Ball:

```mcfunction
/give @p cobbleloots:loot_ball[minecraft:custom_data={LootBallData:"cobbleloots:loot_ball/poke",Variant:"verdant",Texture:"cobblemon:textures/poke_balls/verdant_ball.png"},minecraft:custom_name='{"text":"My Precious Loot Ball"}'] 1
```

### 🔧 Troubleshooting Tips

- 🖼️ **Textures not applying**: Double-check the `LootBallData` resource path, `Variant` key, and `Texture` path for typos. For `/give` commands, always include the `Texture` field to avoid error textures caused by a current bug.
- 🚫 **Loot Ball won't open in Survival**: Check the `Uses` limit and mod config options. The mod tracks usage, cooldowns, and per-player limits. Also, if no items are configured for the loot ball, player won't be able to open it.
- ⚙️ **Commands not working**: Ensure you're in Creative mode or have the necessary permissions. Some interactions require server-side processing.
- 🐛 **Entity not spawning**: Verify the entity type `cobbleloots:loot_ball` and that the mod is loaded correctly.

!!! info "AI-Generated Content Disclaimer"
    This guide was created with the assistance of AI tools. There may be some errors or inaccuracies.
