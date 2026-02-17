#!/usr/bin/env python3
"""
Generate documentation.
"""

import re
import json
import shutil
from pathlib import Path
from rich import print
from PIL import Image

# Constants
LOOT_BALLS_DATA = (
    Path(__file__).parent.parent / "common/src/main/resources/data/cobbleloots"
)
LOOT_BALLS_ASSETS = (
    Path(__file__).parent.parent / "common/src/main/resources/assets/cobbleloots"
)
COBBLEMON_ASSETS = Path(__file__).parent.parent / "local/assets/cobblemon"
MINECRAFT_ASSETS = Path(__file__).parent.parent / "local/assets/minecraft"
TEMPLATE_PATH = Path(__file__).parent / "templates/docs/loot_ball.md"
DOCS_OUTPUT_DIR = Path(__file__).parent.parent / "docs/loot_balls"
DOCS_ASSETS_ITEMS_DIR = Path(__file__).parent.parent / "docs/assets/items"
LOOT_TABLES_DIR = LOOT_BALLS_DATA / "loot_table"
BIOME_TAGS_DIR = LOOT_BALLS_DATA / "tags/worldgen/biome"
BIOME_TAGS_TEMPLATE_PATH = Path(__file__).parent / "templates/docs/biome_tags.md"
BIOME_TAGS_OUTPUT_FILE = Path(__file__).parent.parent / "docs/reference/biome_tags.md"

# Special Mappings
ITEM_TEXTURE_MAPPINGS = {
    "enchanted_golden_apple": "item/golden_apple.png",
    "red_banner": "item/banner.png",
    "white_banner": "item/banner.png",
    "yellow_banner": "item/banner.png",
    "black_banner": "item/banner.png",
    "blue_banner": "item/banner.png",
    "green_banner": "item/banner.png",
    "brown_banner": "item/banner.png",
    "purple_banner": "item/banner.png",
    "pink_banner": "item/banner.png",
    "orange_banner": "item/banner.png",
    "cyan_banner": "item/banner.png",
    "light_blue_banner": "item/banner.png",
    "lime_banner": "item/banner.png",
    "magenta_banner": "item/banner.png",
    "gray_banner": "item/banner.png",
    "light_gray_banner": "item/banner.png",
    "clock": "item/clock_00.png",
    "x_special_attack": "item/battle_items/x_sp_atk.png",
    "x_special_defense": "item/battle_items/x_sp_def.png",
}


def parse_name(name: dict | str | list) -> str:
    if isinstance(name, str):
        return name

    if isinstance(name, list):
        return "".join([parse_name(n) for n in name])

    formatted_name = ""
    if name.get("text", ""):
        formatted_name = name["text"]

    if name.get("italic", False):
        formatted_name = f"*{formatted_name}*"

    if name.get("bold", False):
        formatted_name = f"**{formatted_name}**"

    if name.get("color", ""):
        pass

    return formatted_name


def clean_name(name: str) -> str:
    """Remove unwanted markdown formatting for use in YAML headers."""
    # Remove bold (**text** or __text__)
    name = re.sub(r"\*\*(.*?)\*\*", r"\1", name)
    name = re.sub(r"__(.*?)__", r"\1", name)
    # Remove italic (*text* or _text_)
    name = re.sub(r"\*(.*?)\*", r"\1", name)
    name = re.sub(r"_(.*?)_", r"\1", name)
    return name


def copy_item_icon(namespace: str, item_id: str) -> None:
    """Find and copy item texture to docs assets."""
    texture_name = f"{item_id}.png"
    if item_id in ITEM_TEXTURE_MAPPINGS:
        texture_name = ITEM_TEXTURE_MAPPINGS[item_id]

    source_file = None

    # Determine source path based on namespace
    if namespace == "cobblemon":
        base_item_path = COBBLEMON_ASSETS / "textures"
        search_paths = [
            base_item_path / "item/poke_balls",
            base_item_path / "item/medicine",
            base_item_path / "item",
        ]
        for folder in search_paths:
            if (folder / texture_name).exists():
                source_file = folder / texture_name
                break

        if not source_file:
            try:
                found_files = list(base_item_path.rglob(texture_name))
                if found_files:
                    found_files.sort()
                    source_file = found_files[0]
            except Exception:
                pass
    elif namespace == "minecraft":
        base_item_path = MINECRAFT_ASSETS / "textures"
        search_paths = [
            base_item_path / "item",
            base_item_path / "block",
            base_item_path / "mob_effect",
        ]
        for path in search_paths:
            if (path / texture_name).exists():
                source_file = path / texture_name
                break

        if not source_file:
            try:
                found_files = list(base_item_path.rglob(texture_name))
                if found_files:
                    found_files.sort()
                    source_file = found_files[0]
            except Exception:
                pass
    else:
        print(f"[yellow]Warning: Unknown namespace: {namespace}[/yellow]")
        return

    if source_file:
        # Create namespace directory if it doesn't exist
        namespace_dir = DOCS_ASSETS_ITEMS_DIR / namespace
        if not namespace_dir.exists():
            namespace_dir.mkdir(parents=True, exist_ok=True)

        destination = namespace_dir / f"{item_id}.png"

        # Crop animated textures using Pillow
        try:
            with Image.open(source_file) as img:
                width, height = img.size

                # Check if it's an animation strip (height > width)
                if height > width:
                    # Crop to the first frame (top square)
                    cropped_img = img.crop((0, 0, width, width))
                    cropped_img.save(destination)
                    print(f"[cyan]Cropped animated texture for {item_id}[/cyan]")
                else:
                    # Just copy if it's already square (or wider, which is rare for items)
                    shutil.copy2(source_file, destination)

        except Exception as e:
            print(
                f"[yellow]Warning: Failed to process/copy icon {item_id}: {e}[/yellow]"
            )
            try:
                shutil.copy2(source_file, destination)
            except Exception:
                pass
    else:
        print(f"[yellow]Warning: Icon not found for {item_id}[/yellow]")


def parse_loot_table(
    loot_table_id: str, probability_multiplier: float = 1.0
) -> list[dict]:
    """
    Recursively parse a loot table and return a flat list of items with global probabilities.
    """
    items = []

    # Handle external or special loot tables
    if ":" in loot_table_id:
        namespace_loot, path = loot_table_id.split(":")
        if namespace_loot != "cobbleloots":
            # For external loot tables, we can't parse them easily, so return a placeholder
            return [
                {
                    "type": "external",
                    "name": loot_table_id,
                    "weight": 0,
                    "chance": probability_multiplier * 100,
                    "quantity": "?",
                    "notes": "External Loot Table",
                    "icon_html": "",
                    "display_name": f"External: {loot_table_id}",
                }
            ]
    else:
        path = loot_table_id

    file_path = LOOT_TABLES_DIR / f"{path}.json"
    if not file_path.exists():
        return [
            {
                "type": "error",
                "name": loot_table_id,
                "weight": 0,
                "chance": probability_multiplier * 100,
                "quantity": "?",
                "notes": "File not found",
                "icon_html": "",
                "display_name": f"Error: {loot_table_id} not found",
            }
        ]

    try:
        with file_path.open("r") as f:
            data = json.load(f)
    except Exception as e:
        return [
            {
                "type": "error",
                "name": loot_table_id,
                "weight": 0,
                "chance": probability_multiplier * 100,
                "quantity": "?",
                "notes": f"Error reading file: {e}",
                "icon_html": "",
                "display_name": f"Error: {loot_table_id}",
            }
        ]

    pools = data.get("pools", [])
    for pool in pools:
        entries = pool.get("entries", [])
        total_weight = sum(entry.get("weight", 0) for entry in entries)

        if total_weight == 0:
            continue

        for entry in entries:
            weight = entry.get("weight", 1)
            # Calculate local probability for this entry in this pool
            local_probability = weight / total_weight
            # Global probability is the parent's probability * local probability
            global_probability = probability_multiplier * local_probability

            entry_type = entry.get("type", "")

            if entry_type == "minecraft:item" or entry_type == "item":
                raw_name = entry.get("name", "Unknown")
                namespace = "minecraft"
                item_id = raw_name

                if ":" in raw_name:
                    namespace, item_id = raw_name.split(":", 1)

                display_name = item_id.replace("_", " ").title()
                copy_item_icon(namespace, item_id)

                # Process functions
                functions = entry.get("functions", [])
                quantity = "1"
                notes = []

                for func in functions:
                    func_name = func.get("function")
                    if func_name == "minecraft:set_count" or func_name == "set_count":
                        count = func.get("count")
                        if isinstance(count, dict):
                            quantity = (
                                f"{count.get('min', '?')}-{count.get('max', '?')}"
                            )
                        else:
                            quantity = str(count)
                    elif func_name == "minecraft:set_nbt" or func_name == "set_nbt":
                        notes.append("Has custom NBT data")
                    elif (
                        func_name == "minecraft:enchant_randomly"
                        or func_name == "enchant_randomly"
                    ):
                        notes.append("Randomly enchanted")
                    elif (
                        func_name == "minecraft:enchant_with_levels"
                        or func_name == "enchant_with_levels"
                    ):
                        levels = func.get("levels")
                        if isinstance(levels, dict):
                            min_val = levels.get("min")
                            max_val = levels.get("max")
                            if min_val is not None and max_val is not None:
                                notes.append(
                                    f"Enchanted with {min_val}-{max_val} levels"
                                )
                            else:
                                notes.append("Enchanted with levels")
                        else:
                            notes.append(f"Enchanted with {levels} levels")
                    elif "enchant" in func_name:
                        notes.append("Enchanted")
                    elif (
                        func_name == "minecraft:set_banner_pattern"
                        or func_name == "set_banner_pattern"
                    ):
                        notes.append("Custom banner pattern")
                    elif func_name == "minecraft:set_name" or func_name == "set_name":
                        display_name = parse_name(func.get("name"))
                        notes.append("Custom name")
                    elif (
                        func_name == "minecraft:set_fireworks"
                        or func_name == "set_fireworks"
                    ):
                        notes.append("Custom fireworks")
                    elif (
                        func_name == "minecraft:set_potion" or func_name == "set_potion"
                    ):
                        potion_id = func.get("id", "unknown")
                        if ":" in potion_id:
                            potion_id = potion_id.split(":")[1]
                        potion_name = potion_id.replace("_", " ").title()
                        display_name = f"Potion of {potion_name}"

                icon_html = f'<img src="../../../assets/items/{namespace}/{item_id}.png" width="32" height="32" alt="{display_name}" style="image-rendering: pixelated;">'

                items.append(
                    {
                        "type": "item",
                        "namespace": namespace,
                        "id": item_id,
                        "display_name": display_name,
                        "icon_html": icon_html,
                        "quantity": quantity,
                        "weight": weight,
                        "chance": global_probability * 100,
                        "notes": ", ".join(notes),
                    }
                )

            elif entry_type == "minecraft:loot_table":
                # Recursive call
                referenced_table = entry.get("name", "")
                # Some times it is value not name
                if not referenced_table:
                    referenced_table = entry.get("value", "")

                items.extend(parse_loot_table(referenced_table, global_probability))

            else:
                items.append(
                    {
                        "type": "other",
                        "display_name": f"Type: {entry_type} - {entry.get('name', '')}",
                        "icon_html": "",
                        "quantity": "-",
                        "weight": weight,
                        "chance": global_probability * 100,
                        "notes": "",
                    }
                )

    return items


def generate_loot_table_html(loot_table_id: str) -> str:
    """Generate HTML table for a loot table, supporting recursive parsing."""

    # Initial call to recursive parser
    items = parse_loot_table(loot_table_id)

    if not items:
        return "*Empty Loot Table*"

    # Group items by (Chance, Quantity, Notes)
    # We use round(chance, 4) to avoid float precision issues
    # Key: (chance_rounded, quantity, notes) -> list of items
    grouped_map = {}

    for item in items:
        # Create a unique key for grouping
        key = (round(item["chance"], 4), item["quantity"], item["notes"])
        if key not in grouped_map:
            grouped_map[key] = []
        grouped_map[key].append(item)

    # Convert groups to a list for sorting
    # We want to sort by Chance (Desc), then by Display Name of the first item (Asc)
    groups = []
    for key, group_items in grouped_map.items():
        # Sort items inside the group by name
        group_items.sort(key=lambda x: x["display_name"])

        # Calculate total chance for the group
        total_chance = sum(i["chance"] for i in group_items)

        groups.append(
            {
                "key": key,
                "items": group_items,
                "chance": total_chance,
                "quantity": key[1],
                "notes": key[2],
            }
        )

    # Sort groups by Chance Descending, then Name Ascending
    groups.sort(key=lambda x: (-x["chance"], x["items"][0]["display_name"]))

    rows = []

    for group in groups:
        group_items = group["items"]

        if len(group_items) == 1:
            # Single item case - Standard display
            item = group_items[0]
            rows.append(f"""
    <tr>
        <td><div style="display:flex; align-items:center;">{item["icon_html"]} <span style="margin-left:10px;">{item["display_name"]}</span></div></td>
        <td>{item["quantity"]}</td>
        <td>{group["chance"]:.2f}%</td>
        <td>{item["notes"]}</td>
    </tr>""")
        else:
            # Multiple items case - Grouped display

            # We create a list of items for the Name column
            item_list_html = "<br>".join(
                [
                    f'<div style="display:flex; align-items:center;">{i["icon_html"]} <span style="margin-left:5px;">{i["display_name"]}</span></div>'
                    for i in group_items
                ]
            )

            rows.append(f"""
    <tr>
        <td>
            <strong>One of the following:</strong>
            <div style="margin-top: 5px; margin-left: 10px;">
                {item_list_html}
            </div>
        </td>
        <td style="vertical-align:middle;">{group["quantity"]}</td>
        <td style="vertical-align:middle;">{group["chance"]:.2f}%</td>
        <td style="vertical-align:middle;">{group["notes"]}</td>
    </tr>""")

    return f"""<table markdown="span">
    <thead>
        <tr>
            <th>Item</th>
            <th>Quantity</th>
            <th>Chance</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>{"".join(rows)}
    </tbody>
</table>"""


def generate_obtaining_html(sources: dict, source_type: str) -> str:
    """Generate HTML table for a specific source type (generation, spawning, fishing)."""
    if source_type not in sources:
        return "*Not obtainable via this method.*"

    data_list = sources[source_type]
    if not data_list:
        return "*Configuration empty.*"

    # Sort by weight descending
    sorted_data = sorted(data_list, key=lambda x: x.get("weight", 0), reverse=True)

    rows = []
    for item in sorted_data:
        weight = item.get("weight", 0)

        # --- Biome Formatting ---
        biome = item.get("biome", "Any")
        if isinstance(biome, str):
            if biome.startswith("#minecraft:"):
                tag = biome.split(":")[-1]
                biome_display = f'<a href="https://minecraft.wiki/w/Biome_tag_(Java_Edition)#{tag}">{biome}</a>'
            elif biome.startswith("#cobblemon:"):
                tag = biome.split(":")[-1]
                biome_display = f'<a href="https://gitlab.com/cable-mc/cobblemon/-/blob/main/common/src/main/resources/data/cobblemon/tags/worldgen/biome/{tag}.json">{biome}</a>'
            elif biome.startswith("#cobbleloots:"):
                anchor = biome.lstrip("#").replace(":", "").replace("/", "")
                biome_display = (
                    f'<a href="../../../reference/biome_tags/#{anchor}">{biome}</a>'
                )
            elif biome.startswith("minecraft:"):
                biome_id = biome.split(":")[-1]
                wiki_name = biome_id.replace("_", " ").title().replace(" ", "_")
                biome_display = (
                    f'<a href="https://minecraft.wiki/w/{wiki_name}">{biome}</a>'
                )
            else:
                biome_display = biome
        else:
            biome_display = str(biome)

        # --- Condition Gathering ---
        conds = []

        # 1. Structure
        if "structure" in item:
            structure = item["structure"]
            if structure.startswith("#minecraft:"):  # Tag
                conds.append(f"Structure Tag: `{structure}`")
            elif structure.startswith("minecraft:"):
                struct_id = structure.split(":")[-1]
                wiki_name = struct_id.replace("_", " ").title().replace(" ", "_")
                conds.append(
                    f'Structure: <a href="https://minecraft.wiki/w/{wiki_name}">{structure}</a>'
                )
            else:
                conds.append(f"Structure: `{structure}`")

        # 2. Dimensions
        if "dimension" in item and item["dimension"]:
            dims = item["dimension"]
            formatted_dims = []
            for d in dims:
                if d == "minecraft:overworld":
                    formatted_dims.append(
                        '<a href="https://minecraft.wiki/w/Overworld">Overworld</a>'
                    )
                elif d == "minecraft:the_nether":
                    formatted_dims.append(
                        '<a href="https://minecraft.wiki/w/The_Nether">The Nether</a>'
                    )
                elif d == "minecraft:the_end":
                    formatted_dims.append(
                        '<a href="https://minecraft.wiki/w/The_End">The End</a>'
                    )
                else:
                    formatted_dims.append(f"`{d}`")
            conds.append(f"Dimensions: {', '.join(formatted_dims)}")

        # 3. Block Filters (Spawn & Base)
        if "block" in item:
            block_filter = item["block"]
            sub_conds = []
            if "spawn" in block_filter:
                spawn = block_filter["spawn"]
                sub_conds.append(f"Spawn: `{spawn}`")
            if "base" in block_filter:
                base = block_filter["base"]
                sub_conds.append(f"Base: `{base}`")

            if sub_conds:
                nested_list = (
                    "<ul>" + "".join(f"<li>{s}</li>" for s in sub_conds) + "</ul>"
                )
                conds.append(f"Block Requirements:{nested_list}")

        # 4. Fluid
        if "fluid" in item:
            fluid = item["fluid"]
            if fluid.startswith("minecraft:"):
                fluid_id = fluid.split(":")[-1]
                wiki_name = fluid_id.replace("_", " ").title().replace(" ", "_")
                fluid_display = (
                    f'<a href="https://minecraft.wiki/w/{wiki_name}">{fluid}</a>'
                )
            else:
                fluid_display = fluid
            conds.append(f"Fluid: {fluid_display}")

        # 5. Position (X, Y, Z)
        if "position" in item:
            pos = item["position"]
            sub_conds = []
            for axis in ["x", "y", "z"]:
                if axis in pos:
                    p_data = pos[axis]
                    min_val = p_data.get("min")
                    max_val = p_data.get("max")

                    axis_str = ""
                    if min_val is not None and max_val is not None:
                        axis_str = f"[{min_val} to {max_val}]"
                    elif min_val is not None:
                        axis_str = f">= {min_val}"
                    elif max_val is not None:
                        axis_str = f"<= {max_val}"

                    if axis_str:
                        sub_conds.append(f"{axis.upper()}: {axis_str}")

            if sub_conds:
                nested_list = (
                    "<ul>" + "".join(f"<li>{s}</li>" for s in sub_conds) + "</ul>"
                )
                conds.append(f"Position Requirements:{nested_list}")

        # 6. Light Level (Block & Sky)
        if "light" in item:
            light = item["light"]
            sub_conds = []

            if "block" in light:
                l_data = light["block"]
                min_val = l_data.get("min", 0)
                max_val = l_data.get("max", 15)
                sub_conds.append(f"Block Light (torches, etc.): {min_val}-{max_val}")

            if "sky" in light:
                l_data = light["sky"]
                min_val = l_data.get("min", 0)
                max_val = l_data.get("max", 15)
                sub_conds.append(f"Sky Light (sun/moon): {min_val}-{max_val}")

            if sub_conds:
                nested_list = (
                    "<ul>" + "".join(f"<li>{s}</li>" for s in sub_conds) + "</ul>"
                )
                conds.append(f"Light Requirements:{nested_list}")

        # 7. Time
        if "time" in item:
            time_data = item["time"]
            val = time_data.get("value", {})
            min_t = val.get("min", 0)
            max_t = val.get("max", 24000)
            period = time_data.get("period", 0)

            label = f"{min_t}-{max_t}"
            is_common = False

            if period == 24000:
                # Check for friendly names
                if min_t == 13000 and max_t == 23000:
                    label = "Night"
                    is_common = True
                elif min_t == 0 and max_t == 13000:
                    label = "Day"
                    is_common = True
                elif min_t == 23000 and max_t == 24000:
                    label = "Sunrise"
                    is_common = True
                elif min_t == 12000 and max_t == 13000:
                    label = "Sunset"
                    is_common = True

            if not is_common:
                label = f"{min_t}-{max_t} ticks"
                if period > 0 and period != 24000:
                    label += f" (every {period} ticks)"

            # Append detailed info for friendly names to be precise
            if is_common:
                label += f" ({min_t}-{max_t})"

            conds.append(f"Time: {label}")

        # 8. Weather
        if "weather" in item:
            w = item["weather"]
            # Default is true for all, so we check what is explicitly disallowed or required if specific combinations exist.
            # Usually users specify what is allowed.
            allowed = []
            if w.get("rain", True):
                allowed.append("Rain")
            if w.get("thunder", True):
                allowed.append("Thunder")
            if w.get("clear", True):
                allowed.append("Clear")

            if len(allowed) < 3:
                conds.append(f"Weather: {', '.join(allowed)}")

        # 9. Date
        if "date" in item:
            d = item["date"]
            date_from = d.get("from")
            date_to = d.get("to")
            if date_from or date_to:
                conds.append(f"Date: {date_from or '?'} to {date_to or '?'}")

        # 10. Poke Rods
        if "poke_rod" in item:
            rods = item["poke_rod"]
            if rods:
                rod_names = [r.split(":")[-1].replace("_", " ").title() for r in rods]
                nested_list = (
                    "<ul>" + "".join(f"<li>{r}</li>" for r in rod_names) + "</ul>"
                )
                conds.append(
                    f'Fishing with the following <a href="https://wiki.cobblemon.com/index.php/Pok%C3%A9_Rod">rods</a>:{nested_list}'
                )

        # Assemble Conditions Column
        if conds:
            conditions_display = (
                "<ul>" + "".join(f"<li>{c}</li>" for c in conds) + "</ul>"
            )
        else:
            conditions_display = "None"

        rows.append(f"""
    <tr>
        <td>{weight}</td>
        <td>{biome_display}</td>
        <td>{conditions_display}</td>
    </tr>""")

    # Determine explanation text
    explanation = ""
    if source_type == "generation":
        explanation = (
            "<p><em>This occurs when new chunks are generated in a world.</em></p>"
        )
    elif source_type == "spawning":
        explanation = "<p><em>This happens periodically near a random player in the world/server.</em></p>"
    elif source_type == "fishing":
        explanation = "<p><em>This can happen while fishing with poke rods.</em></p>"

    return f"""{explanation}
<table markdown="span">
    <thead>
        <tr>
            <th>Weight</th>
            <th>Biomes</th>
            <th>Conditions</th>
        </tr>
    </thead>
    <tbody>{"".join(rows)}
    </tbody>
</table>"""


def generate_loot_balls_doc():
    # 1. Read the template
    if not TEMPLATE_PATH.exists():
        print(f"[red]Template not found at {TEMPLATE_PATH}[/red]")
        return

    with TEMPLATE_PATH.open("r") as f:
        template = f.read()

    print(f"Loaded template from {TEMPLATE_PATH.name}")

    # Pre-process template to handle variable conflict
    template = template.replace(
        "<td>${loot_table}</td>", "<td>${loot_table_id_display}</td>"
    )

    # 2. Extract data and process
    definitions_path = LOOT_BALLS_DATA / "loot_ball"
    if not definitions_path.exists():
        print(f"[red]Definitions directory not found at {definitions_path}[/red]")
        return

    print(f"Scanning definitions in {definitions_path.name}...")

    if not DOCS_OUTPUT_DIR.exists():
        DOCS_OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    else:
        # Clean up existing files/folders, preserving index.md
        for item in DOCS_OUTPUT_DIR.iterdir():
            if item.name == "index.md":
                continue

            try:
                if item.is_dir():
                    shutil.rmtree(item)
                else:
                    item.unlink()
            except Exception as e:
                print(f"[yellow]Warning: Failed to delete {item.name}: {e}[/yellow]")

    if not DOCS_ASSETS_ITEMS_DIR.exists():
        DOCS_ASSETS_ITEMS_DIR.mkdir(parents=True, exist_ok=True)

    count = 0
    for loot_ball_file in definitions_path.glob("*.json"):
        try:
            with loot_ball_file.open("r") as f:
                loot_ball_data = json.load(f)

            lb_id = loot_ball_file.stem

            parsed_name = parse_name(loot_ball_data.get("name", lb_id))

            # Calculate Rarity based on average weight
            sources = loot_ball_data.get("sources", {})
            all_weights = []
            for source_list in sources.values():
                for entry in source_list:
                    if isinstance(entry, dict):
                        all_weights.append(entry.get("weight", 1))

            rarity = "Common"
            rarity_slug = "tier-1-common"
            if all_weights:
                avg_weight = sum(all_weights) / len(all_weights)
                if avg_weight >= 50:
                    rarity = "Common"
                    rarity_slug = "tier-1-common"
                elif avg_weight >= 20:
                    rarity = '<span style="color: #5555FF">Uncommon</span>'
                    rarity_slug = "tier-2-uncommon"
                elif avg_weight >= 5:
                    rarity = '<span style="color: #FFAA00">Rare</span>'
                    rarity_slug = "tier-3-rare"
                else:
                    rarity = '<span style="color: #AA00AA">Ultra Rare</span>'
                    rarity_slug = "tier-4-ultra-rare"

            # Normalize dictionary
            normalized_data = {
                "id": lb_id,
                "name": parsed_name,
                "clean_title": clean_name(parsed_name),
                "rarity": rarity,
                "xp": loot_ball_data.get("xp", 0),
                "texture": loot_ball_data.get("texture", ""),
                "loot_table_id": loot_ball_data.get("loot_table", ""),
            }

            # 3. Process Loot Table
            loot_table_html = generate_loot_table_html(normalized_data["loot_table_id"])

            # 4. Process Obtaining
            sources = loot_ball_data.get("sources", {})
            gen_html = generate_obtaining_html(sources, "generation")
            spawn_html = generate_obtaining_html(sources, "spawning")
            fishing_html = generate_obtaining_html(sources, "fishing")

            # Fill Template
            current_doc = template
            current_doc = current_doc.replace(
                "title: ${name}", f"title: {normalized_data['clean_title']}"
            )
            current_doc = current_doc.replace(
                f"![${{name}}]", f"![{normalized_data['clean_title']}]"
            )
            current_doc = current_doc.replace("${name}", normalized_data["name"])

            current_doc = (
                current_doc.replace("${id}", normalized_data["id"])
                .replace("${rarity}", str(normalized_data["rarity"]))
                .replace("${xp}", str(normalized_data["xp"]))
                .replace("${texture}", normalized_data["texture"])
                .replace("${loot_table_id_display}", normalized_data["loot_table_id"])
                .replace("${loot_table}", loot_table_html)
                .replace("${generation}", gen_html)
                .replace("${spawning}", spawn_html)
                .replace("${fishing}", fishing_html)
            )

            # Output file
            rarity_dir = DOCS_OUTPUT_DIR / rarity_slug
            rarity_dir.mkdir(parents=True, exist_ok=True)
            output_file = rarity_dir / f"{lb_id}.md"
            with output_file.open("w") as f:
                f.write(current_doc)

            print(f"[green]Generated {output_file.name}[/green]")
            count += 1

        except Exception as e:
            print(f"[red]Failed to process {loot_ball_file.name}: {e}[/red]")

    print(f"Done! Generated {count} loot ball documents.")


def generate_biome_tags_doc():
    """Generates the Biome Tags reference page."""
    print("Generating Biome Tags documentation...")

    if not BIOME_TAGS_TEMPLATE_PATH.exists():
        print(f"[red]Template not found at {BIOME_TAGS_TEMPLATE_PATH}[/red]")
        return

    if not BIOME_TAGS_DIR.exists():
        print(f"[red]Biome tags directory not found at {BIOME_TAGS_DIR}[/red]")
        return

    with BIOME_TAGS_TEMPLATE_PATH.open("r") as f:
        template = f.read()

    # Scan and process tags
    tag_files = sorted(list(BIOME_TAGS_DIR.glob("**/*.json")))

    sections = []

    for tag_file in tag_files:
        tag_id = "cobbleloots:" + str(
            tag_file.relative_to(BIOME_TAGS_DIR).with_suffix("")
        )

        try:
            with tag_file.open("r") as f:
                data = json.load(f)

            values = data.get("values", [])
            formatted_values = []

            for val in values:
                if isinstance(val, str):
                    if val.startswith("minecraft:"):
                        val_id = val.split(":")[-1]
                        wiki_name = val_id.replace("_", " ").title().replace(" ", "_")
                        formatted_values.append(
                            f"[{val}](https://minecraft.wiki/w/{wiki_name})"
                        )
                    else:
                        formatted_values.append(f"`{val}`")
                elif isinstance(val, dict):
                    v_id = val.get("id", "")
                    if v_id.startswith("minecraft:"):
                        val_id = v_id.split(":")[-1]
                        wiki_name = val_id.replace("_", " ").title().replace(" ", "_")
                        formatted_values.append(
                            f"[{v_id}](https://minecraft.wiki/w/{wiki_name})"
                        )
                    else:
                        formatted_values.append(f"`{v_id}`")

            if formatted_values:
                values_list = "\n".join(f"- {v}" for v in formatted_values)
            else:
                values_list = "*No values*"

            sections.append(
                f"## {tag_id}\n\n**ID:** `{tag_id}`\n\n**Included Biomes:**\n\n{values_list}\n"
            )

        except Exception as e:
            print(f"[red]Error processing tag {tag_id}: {e}[/red]")

    # Build Content
    content = "\n".join(sections)

    output = template.replace("${biome_tags}", content)

    # Ensure output dir exists
    BIOME_TAGS_OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)

    with BIOME_TAGS_OUTPUT_FILE.open("w") as f:
        f.write(output)

    print(f"[green]Generated {BIOME_TAGS_OUTPUT_FILE.name}[/green]")


def main():
    generate_loot_balls_doc()
    generate_biome_tags_doc()


if __name__ == "__main__":
    main()
