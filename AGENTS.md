# Agent Guidelines & Repository Standards for Cobbleloots

Welcome to the **Cobbleloots** repository. This document is the single source of truth for all autonomous AI agents (Antigravity CLI, Claude Code, Cursor, OpenHands) and human contributors pair-programming in this project.

---

## 1. Project Overview & Architecture

**Cobbleloots** is a multi-loader side-mod for [Cobblemon](https://cobblemon.com/) on Minecraft **1.21.1**. It adds immersive ways to obtain items (Loot Balls, biome-specific drops, fishing loot, creative editing GUIs, and customizable loot tables).

The codebase is built using **Architectury Loom** across three primary subprojects:
- **`common/`** (Loader-Independent): Contains the vast majority of the mod's logic, entity registrations, block definitions, items, loot table parsers, networking packet definitions, and data structures.
- **`fabric/`** (Fabric Loader): Fabric-specific initialization (`CobblelootsFabric`), ModMenu config integration, and Fabric networking/event hooks.
- **`neoforge/`** (NeoForge Loader): NeoForge-specific initialization (`CobblelootsNeoForge`), NeoForge Mod Bus event subscribers, payload handlers (`RegisterPayloadHandlersEvent`), and config screen factories.

### Pinned Versions (from `gradle.properties`):
- **Minecraft**: `1.21.1`
- **Java**: `21` (source & target compatibility)
- **Kotlin**: `2.2.20`
- **Cobblemon**: `1.7.3+1.21.1` (target API `1.7.0`)
- **Fabric Loader**: `0.17.2` | **Fabric API**: `0.116.6+1.21.1`
- **NeoForge**: `21.1.182` | **NeoForge Target**: `21`
- **MidnightLib**: `1.9.2+1.21.1`
- **Architectury Loom**: `1.13-SNAPSHOT` | **Plugin**: `3.4-SNAPSHOT`

> [!IMPORTANT]
> **Version Bump Maintenance Rule**: Whenever a task or PR modifies dependency versions in [`gradle.properties`](file:///gradle.properties), the agent **MUST** update [`.agents/rules/minecraft-loaders.md`](file:///.agents/rules/minecraft-loaders.md) to reflect the new versions and document any required API adjustments.

---

## 2. Essential Commands & Build Verification

All code changes must compile and build cleanly before submission.

### Gradle Commands
- **Full Build (all subprojects)**:
  ```bash
  ./gradlew build
  ```
- **Fabric-only Build**:
  ```bash
  ./gradlew :fabric:build
  ```
- **NeoForge-only Build**:
  ```bash
  ./gradlew :neoforge:build
  ```
- **Fast Syntax & Type Check**:
  ```bash
  ./gradlew compileJava
  ```

### Memory Settings in CI / Headless Environments
In resource-constrained environments (such as GitHub Actions runners with 7GB RAM limits), override the default `-Xmx8G` to prevent OOM errors:
```bash
GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx5g -Dorg.gradle.daemon=false -Dorg.gradle.parallel=false" ./gradlew build
```

---

## 3. Development Invariants & Multi-Platform Rules

1. **Common First**: Always implement game logic, items, blocks, and data in `common/`. Only touch `fabric/` or `neoforge/` if an API requires loader-specific hooks.
2. **Data-Driven Design**: Loot tables, ball definitions, and structures must remain data-driven via JSON files located in `common/src/main/resources/data/cobbleloots/`.
3. **Architectury Abstractions**: Use `@ExpectPlatform` or Architectury registry delegates (`DeferredRegister`, `RegistrySupplier`) for features that require platform-specific implementations. Refer to [`.agents/rules/architectury-rules.md`](file:///.agents/rules/architectury-rules.md).
4. **Preserve Documentation & Superpowers Directory Isolation**: Maintain comments, license headers, and documentation in `docs/`. Never place agent planning, brainstorming, specs, or superpowers artifacts inside `docs/` because `docs/` is compiled by MkDocs for user-facing documentation. All superpowers specs and plans **MUST** be committed under `.superpowers/specs/` and `.superpowers/plans/` respectively.
5. **No Direct Edits to `CHANGELOG.md`**: Do not edit `CHANGELOG.md` in individual feature/bugfix PRs. The changelog is generated automatically at release time to prevent git merge conflicts.
6. **Feature PR Target Branch**: Pull requests for new features (`feat(...)`) MUST target the `alpha` branch (`origin/alpha`).
7. **Early Returns Convention**: Always prefer early returns and guard clauses over deeply nested `if-else` blocks to maximize code clarity and maintainability.

---

## 4. Pull Request Requirements & Standards

Every Pull Request opened by an agent or developer must adhere to the following strict formatting rules. This ensures bi-directional synchronization with Linear and smooth product QA.

### 4.1 PR Title Format
The title **MUST** match:
```text
[<linear-issue-id>] <type>(<scope>): <description>
```
- **`<linear-issue-id>`**: The Linear ticket identifier (e.g., `DEV-5`, `LOOT-42`). Including this automatically links the GitHub PR with the Linear issue.
- **`<type>`**: One of `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `chore`.
- **`<scope>`**: Area affected (e.g., `loot-balls`, `commands`, `fishing`, `config`, `fabric`, `neoforge`).
- **`<description>`**: Short, imperative description in lowercase.

*Example*: `[DEV-5] feat(commands): loot balls reset command`

### 4.2 PR Body Format (4 Mandatory Sections)
The body of the PR **MUST** include exactly these four sections:

```markdown
### ¿Qué se hizo?
<!-- Breve descripción en lenguaje natural de no más de 3 párrafos sobre el trabajo que se hizo.
     Párrafos cortos de no más de 3 líneas cada uno. Se permite usar bullet points secuenciales. -->

### ¿Cómo se ve?
<!-- Descripción de lo que debería ver distinto o nuevo quien revise esta PR al probarlo localmente.
     Si son cambios técnicos sin artefacto visual, mostrar lo más relevante del código u otros artefactos producidos. -->

### ¿Cómo testearlo?
<!-- Detalle tipo runbook paso a paso para reproducir los cambios localmente y hacerle QA de producto a la PR.
     Esta es la sección más importante para la aprobación de la PR. -->
1. Iniciar el cliente con `./gradlew :fabric:runClient` o `:neoforge:runClient`.
2. Ejecutar el comando `/cobbleloots ...`
3. Verificar que ...

### Notas adicionales
- **Supuestos de diseño**: Detalle de supuestos tomados no especificados en el ticket de Linear.
- **Plataformas afectadas**:
  - [ ] Common
  - [ ] Fabric
  - [ ] NeoForge
- **Detalles para el reviewer**: Información clave para comprender la implementación.
```

### 4.3 CI Verification Gate
All PRs targeting `main` must pass the **`Gradle CI`** check (`.github/workflows/gradle-ci.yml`) before they can be merged.

### 4.4 Task Management & Linear as Single Source of Truth
- **Sole Source of Truth**: All tasks, bugs, features, and backlog items are managed exclusively in **Linear** (Team: `RipioDev`, Project: `[Mod] Cobbleloots`).
- **Do NOT use GitHub Issues**: Agents must never query `gh issue list` or manage tasks in GitHub Issues. GitHub Issues is disabled/ignored.
- **Linear Tooling & MCP**: Inspect and update tasks via Linear MCP tools (`get_issue`, `save_issue`, `list_issues`). The workspace automatically mounts Linear MCP via the workspace plugin at [`.agents/plugins/linear/`](file:///.agents/plugins/linear/) using the bridge script [`.agents/plugins/linear/linear-mcp.mjs`](file:///.agents/plugins/linear/linear-mcp.mjs). Requires `LINEAR_API_KEY` configured in the system environment variables or in the project root `.env` file.
- **Autonomous Creation upon Approval**: Once the user approves the draft, design, or requirements of a Linear ticket (e.g. following `/grill-me`, planning, or refinement), the agent **MUST immediately create the issue in Linear** using `save_issue` (linking team, project, title, label, and full body) without waiting for an additional confirmation or stopping at just displaying the markdown.
- **Title format**: `[<REPO>] <título descriptivo en español>` (e.g., `[Cobbleloots] Reiniciar loot balls que ya fueron abiertas`).
- **Tags / Labels**: Must use the proper workspace tag (`Feature`, `Bug`, `Improvement`).
- **Issue Body**: Follow [`.agents/templates/linear-issue-template.md`](file:///.agents/templates/linear-issue-template.md) for acceptance criteria, suggested files, and technical constraints.

### 4.5 Agent Tooling & Windows Execution Guidelines
- **PowerShell Backtick Escaping**: Never pass multi-line markdown containing backticks (` `) inside double-quoted PowerShell commands; PowerShell treats backticks as escape characters. Always write content to temporary files and use flags like `gh pr create --body-file <file>`.
- **Node.js Environment**: Node.js is located at `C:\nvm4w\nodejs` (junction to `C:\Users\franc\AppData\Local\nvm\v24.15.0`).
- **Deep Directory Deletion**: For deeply nested directories (e.g., `.gradle` cache or worktrees exceeding Windows `MAX_PATH`), use Node.js `fs.rmSync(dir, { recursive: true, force: true })` rather than PowerShell `Remove-Item`.

---

## 5. Reference Documents in `.agents/`
- [`.agents/rules/minecraft-loaders.md`](file:///.agents/rules/minecraft-loaders.md): Pinned loader versions and platform registration guides.
- [`.agents/rules/architectury-rules.md`](file:///.agents/rules/architectury-rules.md): Multi-loader patterns and common code guidelines.
- [`.agents/templates/agent-pr-template.md`](file:///.agents/templates/agent-pr-template.md): Mandatory PR template for agent and contributor pull requests.
- [`.agents/templates/linear-issue-template.md`](file:///.agents/templates/linear-issue-template.md): Recommended structure and conventions for Linear tickets.

