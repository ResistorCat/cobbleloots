### Changed
- Migrated official documentation system from MkDocs Material (Python/GitHub Pages) to Mintlify (MDX/Mintlify Cloud) with interactive components and modern card-based navigation.
- Centralized documentation configuration in `docs/docs.json` featuring a multiversion dropdown switcher for Stable (`main`), Beta (`beta`), and Alpha (`alpha`) release channels.
- Converted all 22 Loot Ball drop tables and documentation guides to MDX with crisp pixel-art sprite rendering.
- Modernized Documentation CI pipeline to run `mint validate` and `mint broken-links` under Node.js 22, removing legacy Python dependencies and `mkdocs.yml`.
