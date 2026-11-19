# DEV-9: Integrar Objetos Valiosos en Tablas de Loot y Pesca Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Distribute Pokémon valuable items registered in DEV-8 across Cobbleloots loot tables (4 shared sub-tables, tiered Poké Balls, and thematic fishing/ocean drops in Lure and Dive Balls), update documentation tooling, and generate changelog fragments.

**Architecture:** Data-driven Minecraft 1.21.1 loot tables using Architectury common module (`common/src/main/resources/data/cobbleloots/loot_table/`). Sub-tables in `loot_ball/shared/` are referenced with `"type": "minecraft:loot_table"` with balanced weight (`weight: 2`), supplemented by direct thematic drops in aquatic balls (`lure.json`, `dive.json`). Python tooling in `scripts/docs.py` automatically synchronizes docs and texture icons.

**Tech Stack:** Minecraft 1.21.1, Architectury Loom, Fabric / NeoForge, Python 3 (Pillow, Rich) for documentation tooling.

**Spec:** [.superpowers/specs/2026-09-29-dev-9-valuable-items-loot-tables-design.md](file:///c:/Users/franc/GitHub/cobbleloots/.worktrees/ripio/dev-9-cobbleloots-integrar-objetos-valiosos-en-las-tablas-de-loot/.superpowers/specs/2026-09-29-dev-9-valuable-items-loot-tables-design.md)

## Global Constraints

- Pinned Minecraft version: `1.21.1` (Java 21).
- Target branch for PR: `origin/alpha`.
- Work isolated in worktree: `.worktrees/ripio/dev-9-cobbleloots-integrar-objetos-valiosos-en-las-tablas-de-loot` on branch `ripio/dev-9-cobbleloots-integrar-objetos-valiosos-en-las-tablas-de-loot`.
- No direct edits to `CHANGELOG.md` (use `.changelog/DEV-9.md` in English following Rule 5 of `AGENTS.md`).
- Documentation invariant: update `docs/` and `MODINFO.md` in the same PR.
- Strict Non-Nitro Discord limit (< 2,000 characters, target <= 1,800 characters) for post-merge announcement saved to `DISCORD.md`.

---

### Task 1: Create 4 Shared Sub-tables for Valuable Items

**Files:**
- Create: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/valuable_common.json`
- Create: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/valuable_uncommon.json`
- Create: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/valuable_rare.json`
- Create: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/valuable_ultra_rare.json`

**Interfaces:**
- Consumes: Registered item IDs from `CobblelootsItems`: `cobbleloots:nugget`, `cobbleloots:big_nugget`, `cobbleloots:pearl`, `cobbleloots:big_pearl`, `cobbleloots:pearl_string`, `cobbleloots:stardust`, `cobbleloots:star_piece`, `cobbleloots:comet_shard`, `cobbleloots:rare_bone`, `cobbleloots:balm_mushroom`.
- Produces: 4 shared loot tables ready to be referenced by main Loot Ball tables.

- [ ] **Step 1: Create `valuable_common.json`**
Write `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/valuable_common.json` containing `cobbleloots:stardust` (count 1-3, weight 1), `cobbleloots:pearl` (count 1-2, weight 1), and `cobbleloots:nugget` (count 1-2, weight 1).

- [ ] **Step 2: Create `valuable_uncommon.json`**
Write `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/valuable_uncommon.json` containing `cobbleloots:star_piece` (count 1-2, weight 1), `cobbleloots:big_pearl` (count 1, weight 1), and `cobbleloots:rare_bone` (count 1, weight 1).

- [ ] **Step 3: Create `valuable_rare.json`**
Write `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/valuable_rare.json` containing `cobbleloots:big_nugget` (count 1, weight 1) and `cobbleloots:balm_mushroom` (count 1, weight 1).

- [ ] **Step 4: Create `valuable_ultra_rare.json`**
Write `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/valuable_ultra_rare.json` containing `cobbleloots:comet_shard` (count 1, weight 1) and `cobbleloots:pearl_string` (count 1, weight 1).

- [ ] **Step 5: Verify JSON syntax**
Run python one-liner to parse all newly created JSONs:
```bash
python -c "import json, glob; [json.load(open(f)) for f in glob.glob('common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/valuable_*.json')]; print('JSONs valid')"
```

- [ ] **Step 6: Commit**
```bash
git add common/src/main/resources/data/cobbleloots/loot_table/loot_ball/shared/valuable_*.json
git commit -m "feat(data): add shared valuable items loot sub-tables"
```

---

### Task 2: Integrate Valuable Items into Poké Ball Loot Tables by Tier

**Files:**
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/poke.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/premier.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/azure.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/citrine.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/verdant.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/roseate.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/slate.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/great.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/safari.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/nest.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/timer.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/quick.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/net.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/ultra.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/dusk.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/luxury.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/master.json`

**Interfaces:**
- Consumes: `cobbleloots:loot_ball/shared/valuable_common`, `cobbleloots:loot_ball/shared/valuable_uncommon`, `cobbleloots:loot_ball/shared/valuable_rare`, `cobbleloots:loot_ball/shared/valuable_ultra_rare`.
- Produces: Tiered Poké Balls rolling valuable items with weight `2`.

- [ ] **Step 1: Add `valuable_common` to Tier 1 balls**
Add `{"type": "minecraft:loot_table", "value": "cobbleloots:loot_ball/shared/valuable_common", "weight": 2}` to `poke.json`, `premier.json`, `azure.json`, `citrine.json`, `verdant.json`, `roseate.json`, and `slate.json`.

- [ ] **Step 2: Add `valuable_uncommon` to Tier 2 balls**
Add `{"type": "minecraft:loot_table", "value": "cobbleloots:loot_ball/shared/valuable_uncommon", "weight": 2}` to `great.json`, `safari.json`, `nest.json`, `timer.json`, `quick.json`, and `net.json`.

- [ ] **Step 3: Add `valuable_rare` to Tier 3 balls**
Add `{"type": "minecraft:loot_table", "value": "cobbleloots:loot_ball/shared/valuable_rare", "weight": 2}` to `ultra.json`, `dusk.json`, and `luxury.json`.

- [ ] **Step 4: Add `valuable_ultra_rare` to Tier 4 ball**
Add `{"type": "minecraft:loot_table", "value": "cobbleloots:loot_ball/shared/valuable_ultra_rare", "weight": 2}` to `master.json`.

- [ ] **Step 5: Verify JSON syntax across modified tables**
Run:
```bash
python -c "import json, glob; [json.load(open(f)) for f in glob.glob('common/src/main/resources/data/cobbleloots/loot_table/loot_ball/*.json')]; print('All loot ball JSONs valid')"
```

- [ ] **Step 6: Commit**
```bash
git add common/src/main/resources/data/cobbleloots/loot_table/loot_ball/*.json
git commit -m "feat(data): integrate valuable items into tiered loot ball tables"
```

---

### Task 3: Integrate Thematic Valuable Items into Fishing and Ocean Loot Balls

**Files:**
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/lure.json`
- Modify: `common/src/main/resources/data/cobbleloots/loot_table/loot_ball/dive.json`

**Interfaces:**
- Consumes: `cobbleloots:pearl`, `cobbleloots:big_pearl`, `cobbleloots:rare_bone`, `cobbleloots:loot_ball/shared/valuable_uncommon`.
- Produces: Enhanced thematic fishing and aquatic loot drops.

- [ ] **Step 1: Update `lure.json`**
Add:
- `valuable_uncommon` shared subtable (weight 2).
- `cobbleloots:pearl` (count min 1 max 3, weight 5).
- `cobbleloots:big_pearl` (count 1, weight 3).
- `cobbleloots:rare_bone` (count 1, weight 2).

- [ ] **Step 2: Update `dive.json`**
Add:
- `valuable_uncommon` shared subtable (weight 2).
- `cobbleloots:pearl` (count min 1 max 3, weight 5).
- `cobbleloots:big_pearl` (count 1, weight 3).

- [ ] **Step 3: Verify JSON syntax**
Run:
```bash
python -c "import json; json.load(open('common/src/main/resources/data/cobbleloots/loot_table/loot_ball/lure.json')); json.load(open('common/src/main/resources/data/cobbleloots/loot_table/loot_ball/dive.json')); print('Lure and Dive valid')"
```

- [ ] **Step 4: Commit**
```bash
git add common/src/main/resources/data/cobbleloots/loot_table/loot_ball/lure.json common/src/main/resources/data/cobbleloots/loot_table/loot_ball/dive.json
git commit -m "feat(data): add thematic fishing and ocean drops to lure and dive loot balls"
```

---

### Task 4: Update Documentation Tooling and Regenerate Docs

**Files:**
- Modify: `scripts/docs.py`
- Modify / Create: `docs/assets/items/cobbleloots/*.png`
- Modify: `docs/loot_balls/**/*.md`

**Interfaces:**
- Consumes: Textures from `common/src/main/resources/assets/cobbleloots/textures/item/`.
- Produces: Copied documentation icons and updated markdown probability tables for all 22 Loot Balls.

- [ ] **Step 1: Add `cobbleloots` namespace support in `scripts/docs.py`**
In `copy_item_icon(namespace: str, item_id: str)`:
```python
    elif namespace == "cobbleloots":
        base_item_path = LOOT_BALLS_ASSETS / "textures"
        search_paths = [
            base_item_path / "item",
        ]
        for path in search_paths:
            if (path / texture_name).exists():
                source_file = path / texture_name
                break
```

- [ ] **Step 2: Run `scripts/docs.py`**
Run: `python scripts/docs.py`
Expected: Output indicates `Generated docs/loot_balls/...` and `Generated docs/reference/biome_tags.md` without errors or warnings for `cobbleloots` items.

- [ ] **Step 3: Verify docs diff**
Run `git status` and `git diff --stat docs/` to confirm that loot ball docs and item icons were updated with valuable items and proper probabilities.

- [ ] **Step 4: Commit**
```bash
git add scripts/docs.py docs/
git commit -m "docs: regenerate loot ball docs with valuable items and icons"
```

---

### Task 5: User Documentation & Changelog Fragment

**Files:**
- Modify: `MODINFO.md`
- Create: `.changelog/DEV-9.md`

**Interfaces:**
- Consumes: Features implemented in Tasks 1-4.
- Produces: Player-facing documentation and release automation fragment.

- [ ] **Step 1: Update `MODINFO.md`**
Add an explicit mention of Valuable Items in the loot ball tables and fishing rewards to `MODINFO.md`.

- [ ] **Step 2: Create `.changelog/DEV-9.md`**
Write `.changelog/DEV-9.md`:
```markdown
### Added
- Integrated Pokémon valuable items (nuggets, pearls, stardust, comet shards, and more) into Loot Ball and fishing loot tables across all rarity tiers.
- Added 4 shared loot sub-tables for valuable items (`valuable_common`, `valuable_uncommon`, `valuable_rare`, and `valuable_ultra_rare`).
- Added thematic fishing and ocean drops to Lure and Dive Loot Balls (Pearls, Big Pearls, and Rare Bones).
- Extended documentation tooling (`scripts/docs.py`) to resolve and bundle `cobbleloots` namespace item textures and regenerated all 22 Loot Ball documentation pages with updated probabilities and icons.
```

- [ ] **Step 3: Commit**
```bash
git add MODINFO.md .changelog/DEV-9.md
git commit -m "docs(changelog): add DEV-9 release fragment and update modinfo"
```

---

### Task 6: Full Verification & CI Smoke Check

**Files:** None (testing and verification)

- [ ] **Step 1: Run full Gradle build**
Execute: `./gradlew build`
Verify all subprojects (`common`, `fabric`, `neoforge`) compile cleanly and process all resources without syntax or mapping errors.

- [ ] **Step 2: In-Game Verification**
Launch `:fabric:runClient` or `:neoforge:runClient`:
- Place and open a Poké Ball (verify chance of Stardust, Pearl, or Nugget).
- Place and open an Ultra Ball (verify chance of Big Nugget or Balm Mushroom).
- Place and open a Master Ball (verify chance of Comet Shard or Pearl String).
- Place and open a Lure Ball or fish with a Lure Rod (verify Pearls, Big Pearls, Rare Bones).
- Run `/cobbleloots debug weights spawning` and `/cobbleloots debug weights fishing` to verify weights resolution.

- [ ] **Step 3: Prepare Post-Merge Assets**
Ensure `DISCORD.md` is populated with the announcement text ready to copy upon merge, formatted according to `.agents/templates/discord-announcement-template.md` (under 2,000 characters).
