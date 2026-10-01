# Mintlify Documentation Migration (DEV-31) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate Cobbleloots' documentation from MkDocs Material (Python/GitHub Pages) to Mintlify (MDX/Mintlify Cloud) with multiversion channel support (Stable, Beta, Alpha) and automated Node.js CI.

**Architecture:** Centralized Mintlify configuration in `docs/docs.json` defining a multiversion channel switcher (`navigation.global.versions`) and 3 navigation tabs (`Guides`, `Loot Balls`, `Reference`). All 29 markdown pages converted to MDX with native Mintlify components (`<CardGroup>`, `<Note>`, `<Tip>`, `<Warning>`, `<Info>`), clean JSX tables with pixel-art rendering (`style={{ imageRendering: "pixelated" }}`), and modernized CI in `.github/workflows/ci.yml`.

**Tech Stack:** Mintlify (`mint` CLI / `docs.json`), MDX (React/JSX), Node.js 22, GitHub Actions.

**Spec:** [`.superpowers/specs/2026-09-30-mintlify-docs-migration-design.md`](file:///c:/Users/franc/GitHub/cobbleloots/.superpowers/specs/2026-09-30-mintlify-docs-migration-design.md)

## Global Constraints

- Configuration and content MUST reside strictly inside `docs/` (using `docs/docs.json`, never `mint.json`).
- All internal documentation links MUST be root-relative and extensionless (e.g. `/loot_balls/tier-1-common/poke`).
- All void HTML tags in MDX MUST be self-closing (`<img ... />`, `<br />`).
- All table styles and pixel-art rendering MUST use valid JSX object notation (`style={{ imageRendering: "pixelated" }}`).
- Do NOT modify mod Java/Kotlin code or `CHANGELOG.md` directly.
- Superpowers specs and plans MUST reside in `.superpowers/specs/` and `.superpowers/plans/`, never in `docs/`.

---

### Task 1: Setup Mintlify Configuration (`docs/docs.json`) and Remove Legacy Config

**Files:**
- Create: `docs/docs.json`
- Delete: `mkdocs.yml`

**Interfaces:**
- Consumes: Channel URLs (`https://docs.ripio.dev/cobbleloots/{stable,beta,alpha}/`) and design specs from `.superpowers/specs/2026-09-30-mintlify-docs-migration-design.md`.
- Produces: Official Mintlify schema configuration powering all site navigation, brand styling, and version selection.

- [ ] **Step 1: Create `docs/docs.json`**

Create `docs/docs.json` with the full specification:
```json
{
  "$schema": "https://mintlify.com/docs.json",
  "name": "Cobbleloots",
  "theme": "prism",
  "colors": {
    "primary": "#3B82F6",
    "light": "#60A5FA",
    "dark": "#1D4ED8"
  },
  "favicon": "/assets/ball/model/pumpkin.png",
  "logo": {
    "light": "/assets/halloween-logo.png",
    "dark": "/assets/halloween-logo.png"
  },
  "navigation": {
    "global": {
      "versions": [
        {
          "version": "Stable",
          "href": "https://docs.ripio.dev/cobbleloots/stable/",
          "default": true
        },
        {
          "version": "Beta",
          "href": "https://docs.ripio.dev/cobbleloots/beta/"
        },
        {
          "version": "Alpha",
          "href": "https://docs.ripio.dev/cobbleloots/alpha/"
        }
      ]
    },
    "tabs": [
      {
        "tab": "Guides",
        "groups": [
          {
            "group": "Overview",
            "pages": ["index"]
          },
          {
            "group": "Getting Started & Setup",
            "pages": [
              "guides/configuration"
            ]
          },
          {
            "group": "How-To Guides",
            "pages": [
              "guides/how-to/creative",
              "guides/how-to/datapack"
            ]
          }
        ]
      },
      {
        "tab": "Loot Balls",
        "groups": [
          {
            "group": "Overview",
            "pages": ["loot_balls/index"]
          },
          {
            "group": "Tier 1: Common",
            "pages": [
              "loot_balls/tier-1-common/azure",
              "loot_balls/tier-1-common/citrine",
              "loot_balls/tier-1-common/poke",
              "loot_balls/tier-1-common/premier",
              "loot_balls/tier-1-common/roseate",
              "loot_balls/tier-1-common/slate",
              "loot_balls/tier-1-common/verdant"
            ]
          },
          {
            "group": "Tier 2: Uncommon",
            "pages": [
              "loot_balls/tier-2-uncommon/dive",
              "loot_balls/tier-2-uncommon/great",
              "loot_balls/tier-2-uncommon/heal",
              "loot_balls/tier-2-uncommon/lure",
              "loot_balls/tier-2-uncommon/nest",
              "loot_balls/tier-2-uncommon/net",
              "loot_balls/tier-2-uncommon/pumpkin",
              "loot_balls/tier-2-uncommon/quick",
              "loot_balls/tier-2-uncommon/rainbow",
              "loot_balls/tier-2-uncommon/safari",
              "loot_balls/tier-2-uncommon/timer"
            ]
          },
          {
            "group": "Tier 3: Rare",
            "pages": [
              "loot_balls/tier-3-rare/dusk",
              "loot_balls/tier-3-rare/luxury",
              "loot_balls/tier-3-rare/ultra"
            ]
          },
          {
            "group": "Tier 4: Ultra Rare",
            "pages": [
              "loot_balls/tier-4-ultra-rare/master"
            ]
          }
        ]
      },
      {
        "tab": "Reference",
        "groups": [
          {
            "group": "Technical Reference",
            "pages": [
              "reference/commands",
              "reference/biome_tags"
            ]
          }
        ]
      }
    ]
  }
}
```

- [ ] **Step 2: Delete legacy `mkdocs.yml`**

Run: `git rm mkdocs.yml`

- [ ] **Step 3: Commit Task 1 changes**

Run:
```bash
git add docs/docs.json
git commit -m "docs: add docs.json and remove mkdocs.yml"
```

---

### Task 2: Homepage & Core Guides Migration

**Files:**
- Create: `docs/index.mdx` (replaces `docs/index.md`)
- Create: `docs/guides/configuration.mdx` (replaces `docs/guides/configuration.md`)
- Create: `docs/guides/how-to/creative.mdx` (replaces `docs/guides/how-to/creative.md`)
- Create: `docs/guides/how-to/datapack.mdx` (replaces `docs/guides/how-to/datapack.md`)
- Delete: `docs/index.md`, `docs/guides/configuration.md`, `docs/guides/how-to/creative.md`, `docs/guides/how-to/datapack.md`

**Interfaces:**
- Consumes: Content from existing guides and Mintlify components `<CardGroup>`, `<Card>`, `<Note>`, `<Tip>`, `<Warning>`, `<Info>`.
- Produces: Fully functional Guides tab pages in valid MDX format.

- [ ] **Step 1: Convert `docs/index.md` to `docs/index.mdx`**

Rewrite `docs/index.mdx` with:
- Frontmatter (`title`, `description`).
- Clean intro with Modrinth badge.
- Interactive `<CardGroup cols={2}>` containing cards for Loot Balls, Configuration, Creative Mode, Datapacks, Commands, and Biome Tags.
- Value items summary.
- `<Tip>` component replacing MkDocs admonition `!!! tip`.
- Remove `docs/index.md`.

- [ ] **Step 2: Convert `docs/guides/configuration.md` to `docs/guides/configuration.mdx`**

Convert `configuration.mdx`:
- YAML frontmatter with `title: "Configuration"`, `description: "...", `icon: "sliders"`.
- Replace `> **Note for server operators:**` with `<Note>`.
- Clean Markdown tables for Gameplay, Creative, and Sources settings.
- Remove `docs/guides/configuration.md`.

- [ ] **Step 3: Convert `docs/guides/how-to/creative.md` to `docs/guides/how-to/creative.mdx`**

Convert `creative.mdx`:
- YAML frontmatter with `title`, `description`, `icon: "wand-magic-sparkles"`.
- Replace `!!! note` and `!!! tip` blocks with `<Note>` and `<Tip>`.
- Convert internal links from `loot_balls/index.md` or `#anchor` to root-relative `/loot_balls` and local headings.
- Self-close any void tags.
- Remove `docs/guides/how-to/creative.md`.

- [ ] **Step 4: Convert `docs/guides/how-to/datapack.md` to `docs/guides/how-to/datapack.mdx`**

Convert `datapack.mdx`:
- YAML frontmatter with `title`, `description`, `icon: "folder-tree"`.
- Replace `!!! info` and other admonitions with `<Info>` and `<Note>`.
- Clean code blocks and JSON examples.
- Remove `docs/guides/how-to/datapack.md`.

- [ ] **Step 5: Commit Task 2 changes**

Run:
```bash
git add docs/index.mdx docs/guides/
git commit -m "docs(guides): convert homepage and guides to Mintlify MDX"
```

---

### Task 3: Loot Balls Pages Conversion (Overview & 22 Tiers)

**Files:**
- Create: `docs/loot_balls/index.mdx` (replaces `docs/loot_balls/index.md`)
- Create: 7 MDX files in `docs/loot_balls/tier-1-common/` (`azure`, `citrine`, `poke`, `premier`, `roseate`, `slate`, `verdant`)
- Create: 11 MDX files in `docs/loot_balls/tier-2-uncommon/` (`dive`, `great`, `heal`, `lure`, `nest`, `net`, `pumpkin`, `quick`, `rainbow`, `safari`, `timer`)
- Create: 3 MDX files in `docs/loot_balls/tier-3-rare/` (`dusk`, `luxury`, `ultra`)
- Create: 1 MDX file in `docs/loot_balls/tier-4-ultra-rare/` (`master`)
- Delete: All corresponding 23 `.md` files.

**Interfaces:**
- Consumes: Assets in `/assets/...` (ball models and Cobblemon/Minecraft item sprites).
- Produces: Valid JSX tables with pixel-art rendering (`style={{ imageRendering: "pixelated" }}`), self-closing `<img ... />` and `<br />`, and valid frontmatter.

- [ ] **Step 1: Convert `docs/loot_balls/index.md` to `docs/loot_balls/index.mdx`**

Rewrite `docs/loot_balls/index.mdx`:
- Frontmatter with `title: "Loot Balls Overview"`, `description: "..."`, `icon: "circle-dot"`.
- Clean image tag `<img src="/assets/ball/model/poke.png" width="120" style={{ float: "right" }} alt="Poké Ball" />`.
- Links to tiers.
- Remove `docs/loot_balls/index.md`.

- [ ] **Step 2: Convert Tier 1 Common Loot Balls (7 files)**

Convert `azure.md`, `citrine.md`, `poke.md`, `premier.md`, `roseate.md`, `slate.md`, `verdant.md` to `.mdx`:
- Frontmatter with `title`, `description`, `icon: "circle-dot"`.
- Remove `markdown="span"` from `<table>`.
- Change `<img ...>` to `<img ... />` and `<br>` to `<br />`.
- Convert style attributes to JSX style objects: `style={{ display: "flex", alignItems: "center" }}`, `style={{ imageRendering: "pixelated" }}`, `style={{ marginLeft: "10px" }}`.
- Normalize image `src` paths to root-relative `/assets/...`.
- Delete original `.md` files.

- [ ] **Step 3: Convert Tier 2 Uncommon Loot Balls (11 files)**

Convert `dive.md`, `great.md`, `heal.md`, `lure.md`, `nest.md`, `net.md`, `pumpkin.md`, `quick.md`, `rainbow.md`, `safari.md`, `timer.md` to `.mdx`:
- Same JSX table cleanup, self-closing tags, and root-relative `/assets/...` image paths.
- Delete original `.md` files.

- [ ] **Step 4: Convert Tier 3 Rare & Tier 4 Ultra Rare Loot Balls (4 files)**

Convert `dusk.md`, `luxury.md`, `ultra.md` (Tier 3) and `master.md` (Tier 4) to `.mdx`:
- Same JSX table cleanup, self-closing tags, and root-relative `/assets/...` image paths.
- Delete original `.md` files.

- [ ] **Step 5: Commit Task 3 changes**

Run:
```bash
git add docs/loot_balls/
git commit -m "docs(loot_balls): convert loot balls catalog and tier tables to MDX"
```

---

### Task 4: Reference Guides & External References Migration

**Files:**
- Create: `docs/reference/commands.mdx` (replaces `docs/reference/commands.md`)
- Create: `docs/reference/biome_tags.mdx` (replaces `docs/reference/biome_tags.md`)
- Modify: `MODINFO.md`
- Delete: `docs/reference/commands.md`, `docs/reference/biome_tags.md`

**Interfaces:**
- Consumes: Commands reference and Biome tags definitions.
- Produces: Clean Reference tab pages and updated external links.

- [ ] **Step 1: Convert `docs/reference/commands.md` to `docs/reference/commands.mdx`**

Rewrite `commands.mdx`:
- Frontmatter with `title: "Commands Reference"`, `description: "..."`, `icon: "terminal"`.
- Replace `!!! note "Infinite Loot Balls"` with `<Note>`.
- Ensure code block language tags (`mcfunction` or `bash`).
- Remove `docs/reference/commands.md`.

- [ ] **Step 2: Convert `docs/reference/biome_tags.md` to `docs/reference/biome_tags.mdx`**

Rewrite `biome_tags.mdx`:
- Frontmatter with `title: "Biome Tags"`, `description: "..."`, `icon: "map"`.
- Replace `:material-terrain:` heading decorations with clean text.
- Remove `docs/reference/biome_tags.md`.

- [ ] **Step 3: Update `MODINFO.md` documentation link**

In `MODINFO.md`:
Replace `https://resistorcat.github.io/cobbleloots/` with `https://docs.ripio.dev/cobbleloots/stable/`.

- [ ] **Step 4: Commit Task 4 changes**

Run:
```bash
git add docs/reference/ MODINFO.md
git commit -m "docs(reference): convert commands and biome tags to MDX and update MODINFO"
```

---

### Task 5: GitHub Actions CI Pipeline Modernization

**Files:**
- Modify: `.github/workflows/ci.yml`

**Interfaces:**
- Consumes: `docs/docs.json` and `.mdx` files under `docs/`.
- Produces: Automated Node.js 22 GitHub Actions workflow verifying docs structure and broken links on push and pull requests.

- [ ] **Step 1: Update `.github/workflows/ci.yml`**

Replace content of `.github/workflows/ci.yml` with:
```yaml
name: Documentation CI

on:
  push:
    branches:
      - main
      - master
      - beta
      - alpha
    paths:
      - 'docs/**'
      - '.github/workflows/ci.yml'
  pull_request:
    branches:
      - main
      - master
      - beta
      - alpha
    paths:
      - 'docs/**'
      - '.github/workflows/ci.yml'
  workflow_dispatch:

permissions:
  contents: read

jobs:
  validate:
    name: Validate Mintlify Docs
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: docs
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 22
      - name: Validate documentation structure
        run: npx mint validate
      - name: Check for broken links
        run: npx mint broken-links
```

- [ ] **Step 2: Commit Task 5 changes**

Run:
```bash
git add .github/workflows/ci.yml
git commit -m "ci(docs): modernize Documentation CI with Node 22 and Mintlify CLI"
```

---

### Task 6: Local Validation, Verification & Changelog Fragment

**Files:**
- Create: `.changelog/DEV-31.md`

**Interfaces:**
- Consumes: Mintlify CLI (`mint validate`, `mint broken-links`) and project changelog fragment rules in `AGENTS.md`.
- Produces: Passing validation check with 0 warnings/errors and complete player/developer-facing release notes fragment.

- [ ] **Step 1: Run local validation**

Execute in `docs/`:
`npx mint validate`
Verify output exits with code 0.

- [ ] **Step 2: Run broken links check**

Execute in `docs/`:
`npx mint broken-links`
Verify all internal links and asset paths resolve.

- [ ] **Step 3: Create changelog fragment `.changelog/DEV-31.md`**

Create `.changelog/DEV-31.md` adhering to Section 3 Rule 5 in `AGENTS.md`:
```markdown
### Changed
- Migrated official documentation system from MkDocs Material to Mintlify with interactive MDX components, modern layout, and high-performance client rendering.
- Replaced MkDocs configuration with centralized `docs/docs.json` featuring a multiversion dropdown switcher for Stable (`main`), Beta (`beta`), and Alpha (`alpha`) release channels.
- Modernized documentation CI pipeline to run `mint validate` and `mint broken-links` under Node.js 22, removing legacy Python dependencies and `mkdocs.yml`.
```

- [ ] **Step 4: Commit Task 6 changes**

Run:
```bash
git add .changelog/DEV-31.md
git commit -m "chore(changelog): add DEV-31 changelog fragment"
```
