# Technical Specification: Mintlify Documentation Migration with Multiversion Selector (DEV-31)

- **Issue**: [DEV-31](https://linear.app/ripiodev/issue/DEV-31)
- **Author**: Antigravity & Pair Programming Partner
- **Date**: 2026-09-30
- **Status**: Draft / Under Review

---

## 1. Overview & Objectives

Migrate Cobbleloots' official documentation subsystem from MkDocs Material (Python / GitHub Pages) to **Mintlify** (MDX / Mintlify Cloud).

The target architecture provides:
1. **Modern Navigation & MDX Components**: Interactive layout using `<CardGroup>`, `<Tabs>`, `<Note>`, `<Tip>`, and `<Warning>`.
2. **Channel-Based Multiversion Selector**: Header dropdown (`navigation.global.versions`) linking to Stable (`main`), Beta (`beta`), and Alpha (`alpha`) channels deployed under `docs.ripio.dev/cobbleloots/<channel>/`.
3. **Docs Isolation**: Self-contained documentation configuration in `docs/docs.json` (Mintlify modern standard), avoiding clutter in the repository root.
4. **Automated CI**: Fast validation pipeline in `.github/workflows/ci.yml` running `mint validate` and `mint broken-links` with Node.js 22, eliminating Python dependencies and `mkdocs.yml`.

---

## 2. Information Architecture & Navigation

### 2.1 Global Configuration (`docs/docs.json`)

The site configuration resides entirely at `docs/docs.json`.

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

---

## 3. Content Migration & MDX Standards

### 3.1 File Renaming & Frontmatter
All 29 markdown files in `docs/` are renamed from `.md` to `.mdx`.
Every file defines YAML frontmatter with `title` and `description`:
- Python-Markdown icon formats (e.g. `icon: material/cog`, `:material-...:`) are replaced with valid Lucide / FontAwesome icon identifiers (e.g. `icon: "sliders"`, `icon: "terminal"`, `icon: "folder-tree"`, `icon: "circle-dot"`).

### 3.2 Homepage Redesign (`docs/index.mdx`)
The landing page is restructured with `<CardGroup cols={2}>` to provide an intuitive visual hub:
- Direct links to Loot Balls catalog, Configuration guide, Creative mode editor, Datapack creation, Commands, and Biome Tags.

### 3.3 Admonitions & Callouts
Python-Markdown syntax is transformed to native Mintlify MDX components:
- `!!! note ...` and `> **Note:**` $\rightarrow$ `<Note>...</Note>`
- `!!! tip ...` $\rightarrow$ `<Tip>...</Tip>`
- `!!! warning ...` $\rightarrow$ `<Warning>...</Warning>`
- `!!! info ...` $\rightarrow$ `<Info>...</Info>`

### 3.4 Loot Ball Tables & Pixel Art Crisp Rendering
The 22 Loot Ball documentation pages contain HTML tables with 16x16 and 32x32 Pokémon sprites. In MDX/JSX:
1. `markdown="span"` table attributes are removed.
2. Void HTML tags are strictly self-closing: `<img ... />` and `<br />`.
3. Inline styles are transformed into valid JSX objects:
   ```jsx
   <div style={{ display: "flex", alignItems: "center" }}>
     <img 
       src="/assets/items/cobblemon/poke_ball.png" 
       width="32" 
       height="32" 
       alt="Poke Ball" 
       style={{ imageRendering: "pixelated" }} 
     />
     <span style={{ marginLeft: "10px" }}>Poke Ball</span>
   </div>
   ```
4. Asset paths are normalized to root-relative `/assets/...`.
5. Internal markdown links are converted to root-relative, extensionless URLs (e.g. `/loot_balls/tier-1-common/poke`).

---

## 4. Continuous Integration & Deprecations

### 4.1 CI Workflow Update (`.github/workflows/ci.yml`)
1. Path filtering on `docs/**` and `.github/workflows/ci.yml` for triggers on `push` and `pull_request` targeting `main`, `master`, `beta`, and `alpha`.
2. Job executes under Node.js 22 with `working-directory: docs`.
3. Runs:
   - `npx mint validate`
   - `npx mint broken-links`
4. The Python toolchain (`actions/setup-python`, `pip install mkdocs-material`, `mkdocs gh-deploy`) is removed completely.

### 4.2 Repository Deprecations & Cleanup
1. `mkdocs.yml` is deleted from the repository root.
2. In `MODINFO.md`, the documentation URL `https://resistorcat.github.io/cobbleloots/` is updated to `https://docs.ripio.dev/cobbleloots/stable/`.

---

## 5. Verification Plan

### 5.1 Local Verification
- `cd docs && npx mint validate`: Verify that configuration and MDX parsing succeed with zero errors or warnings.
- `cd docs && npx mint broken-links`: Ensure all internal links and asset references resolve.

### 5.2 CI Pipeline Verification
- Trigger `.github/workflows/ci.yml` via GitHub PR check to confirm clean execution on Linux runner.
