---
description: Workflow for implementing new features in Cobbleloots
---
This workflow outlines the standard process for adding new features to the Cobbleloots mod, ensuring consistency and completeness.

1. **Analyze Requirements & Codebase**
   - Understand the feature request.
   - Run `find_by_name` or `grep_search` to identify relevant files (e.g., Config, Events, Entities).
   - Read existing code to understand patterns.

2. **Update Configuration**
   - If the feature requires new settings, modify `CobblelootsConfig.java`.
   - Add new `@Entry` fields with appropriate categories and default values.
   - Update `docs/configuration/index.md` to include the new settings in the relevant table.

3. **Implement Core Logic**
   - Modify or create necessary Java classes (Events, Entities, Items, etc.).
   - Use `CobblelootsConfig` to access the new settings.
   - Ensure changes are compatible with existing systems (e.g., `CobblelootsSourceType`).

4. **Update Documentation**
   - If the feature adds user-facing content (new loot ball type, mechanic, command), update the relevant markdown files in `docs/`.
   - Ensure `docs/configuration/index.md` is up-to-date (if step 2 was performed).

5. **Update Changelog**
   - Add an entry to `CHANGELOG.md` under the current version (or a new version section).
   - Use concise language to describe the "Added", "Changed", or "Fixed" items.

6. **Verify Implementation**
   - Update `TODO.md` to mark the task as complete.
   - (Optional) Create a verification plan or walkthrough artifact if the feature is complex.
