# Changelog Fragments

This directory stores player-facing release note fragments for upcoming releases.

## How It Works
1. When creating a feature or bugfix PR, create a markdown file in this directory named after the Linear issue or feature (e.g., `DEV-5.md` or `fix-target-undo.md`).
2. Write player-facing release notes in English, highlighting what players or server admins will see and experience. Do **not** use technical commit messages or internal class names.
3. Use the standard section headers matching Cobbleloots changelogs:
   - `### Gameplay Changes`
   - `### Changes`
   - `### Technical Changes`
   - `### Bug Fixes`

## Example (`DEV-5.md`)
```markdown
### Gameplay Changes

- **Loot Balls Reset Command**: Added `/cobbleloots reset loot_ball` allowing server admins to reset opened loot balls so players can open them again.
  - Supports resetting targeted loot balls, all balls in a radius, or globally.
  - Includes `/cobbleloots reset loot_ball undo` to revert the last reset action.
```

## Release Automation
When a release is triggered (on push to `alpha`, `beta`, or `main`), the release engine:
1. Reads all `*.md` files in this directory (ignoring `README.md` and hidden files).
2. Merges and formats them under the new version header in `CHANGELOG.md` and `.release_notes.md`.
3. Automatically deletes the consumed fragment files in the release commit.
