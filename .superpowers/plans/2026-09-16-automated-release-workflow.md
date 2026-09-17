# Automated Semantic Versioning & Multi-Channel Release Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Automate the Cobbleloots release lifecycle with SemVer 2.0.0, Conventional Commits, multi-channel support (`main`, `beta`, `alpha`), JAR artifact naming with Minecraft versions, and automated publishing to GitHub Releases, Modrinth, and CurseForge.

**Architecture:** A standalone Python script `scripts/release.py` performs hybrid commit filtering, SemVer calculation, and properties/changelog updates; `scripts/publish.py` and helper modules are refactored for headless CI execution; `build.gradle` updates the JAR naming convention; and a new GitHub Actions workflow `.github/workflows/release.yml` orchestrates the pipeline with loop-prevention and dry-run safety.

**Tech Stack:** Python 3.12 (standard library, `typer`, `rich`, `pydantic`, `requests`, `pytest`), Gradle 8 / Architectury Loom, GitHub Actions, Git.

**Spec:** [`.superpowers/specs/2026-09-16-automated-release-workflow-design.md`](file:///.superpowers/specs/2026-09-16-automated-release-workflow-design.md)

## Global Constraints
- Target Minecraft version: `1.21.1`, Java 21, Cobblemon `1.7.x`
- Supported platforms: Fabric and NeoForge
- JAR naming convention: `cobbleloots-<loader>-<minecraft_version>-<mod_version>.jar`
- Display name format: `Cobbleloots v<mod_version> [<minecraft_version>]` on GitHub; `... [Fabric]` and `... [NeoForge]` on platforms
- No heavy Node.js dependencies for SemVer logic; pure Python only
- Release commits must include `[skip ci]`
- Non-interactive / headless CI publishing without `typer.confirm()` blocks
- Changelog rule from `AGENTS.md`: do not edit `CHANGELOG.md` manually in feature PRs; only automated release updates

---

### Task 1: JAR Artifact Naming Convention in `build.gradle`

**Files:**
- Modify: [`build.gradle:23-27`](file:///build.gradle#L23-L27)

**Interfaces:**
- Consumes: `$rootProject.archives_name`, `$project.name`, `$rootProject.minecraft_version`
- Produces: Compiled JAR files named `cobbleloots-<loader>-1.21.1-<mod_version>.jar`

- [ ] **Step 1: Update `archivesName` in `build.gradle`**
Modify `build.gradle` line 25:
```groovy
    base {
        // Set up a suffixed format for the mod jar names, e.g. `cobbleloots-fabric-1.21.1`.
        archivesName = "$rootProject.archives_name-$project.name-$rootProject.minecraft_version"
    }
```

- [ ] **Step 2: Verify Gradle build and output filename generation**
Run:
```powershell
./gradlew remapJar
```
Verify: Output files in `fabric/build/libs/` and `neoforge/build/libs/` match `cobbleloots-fabric-1.21.1-2.3.0.jar` and `cobbleloots-neoforge-1.21.1-2.3.0.jar`.

- [ ] **Step 3: Commit**
```bash
git add build.gradle
git commit -m "build(gradle): include minecraft version in archive jar names"
```

---

### Task 2: Robust Configuration & Pydantic Models for CI (`scripts/config.py`, `scripts/models.py`)

**Files:**
- Modify: [`scripts/config.py:28-41`](file:///scripts/config.py#L28-L41)
- Modify: [`scripts/models.py:1-37`](file:///scripts/models.py#L1-L37)
- Create: `scripts/tests/test_config_and_models.py`
- Modify: [`scripts/requirements.txt`](file:///scripts/requirements.txt) (ensure `pytest` is included for testing)

**Interfaces:**
- Consumes: `os.environ`, `gradle.properties`
- Produces: Non-crashing `load_env()`, resilient `ModProperties` model that ignores unexpected extra fields.

- [ ] **Step 1: Write the failing unit tests for config and models**
Create `scripts/tests/test_config_and_models.py`:
```python
import os
import pytest
from pathlib import Path
import sys

# Add scripts directory to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from config import load_env, load_mod_properties
from models import ModProperties


def test_load_env_does_not_raise_when_missing(monkeypatch, tmp_path):
    import config
    monkeypatch.setattr(config, "ENV_PATH", tmp_path / "non_existent.env")
    # Should not raise FileNotFoundError
    load_env()


def test_mod_properties_ignores_extra_fields():
    data = {
        "mod_id": "cobbleloots",
        "mod_version": "2.3.0",
        "mod_version_type": "release",
        "mod_name": "Cobbleloots",
        "mod_description": "desc",
        "mod_authors": "author",
        "mod_license": "MIT",
        "mod_logo": "logo.png",
        "mod_homepage": "https://example.com",
        "mod_source": "https://example.com",
        "mod_issues": "https://example.com",
        "mod_discord": "https://example.com",
        "mod_modrinth": "https://example.com",
        "mod_curseforge": "https://example.com",
        "maven_group": "dev.ripio",
        "archives_name": "cobbleloots",
        "enabled_platforms": "fabric,neoforge",
        "minecraft_version": "1.21.1",
        "cobblemon_target_version": "1.7.0",
        "fabric_loader_target_version": "0.17.2",
        "neoforge_target_version": "21",
        "cobblemon_version": "1.7.3+1.21.1",
        "fabric_loader_version": "0.17.2",
        "fabric_api_version": "0.116.6+1.21.1",
        "fabric_kotlin_version": "1.13.6",
        "neoforge_version": "21.1.182",
        "neoforge_kotlin_version": "5.10.0",
        "modmenu_version": "11.0.3",
        "extra_unknown_future_field": "some_value"
    }
    props = ModProperties(**data)
    assert props.mod_id == "cobbleloots"
    assert props.extra_unknown_future_field == "some_value" or not hasattr(props, "extra_unknown_future_field")
```

- [ ] **Step 2: Run test to verify failure**
Run:
```powershell
python -m pytest scripts/tests/test_config_and_models.py
```
Expected: FAIL with `FileNotFoundError` or `ValidationError`.

- [ ] **Step 3: Update `scripts/config.py` and `scripts/models.py`**
In `scripts/config.py`:
```python
def load_env() -> None:
    """
    Load environment variables from the .env file if it exists.
    """
    if not ENV_PATH.exists():
        return

    with ENV_PATH.open() as f:
        for line in f:
            if line.strip() and not line.startswith("#"):
                key, value = line.strip().split("=", 1)
                os.environ[key] = value
```
In `scripts/models.py`:
```python
from pydantic import BaseModel, ConfigDict


class ModProperties(BaseModel):
    """
    Model for mod properties loaded from gradle.properties.
    """
    model_config = ConfigDict(extra="ignore")

    mod_id: str
    mod_version: str
    mod_version_type: str
    mod_name: str
    mod_description: str
    mod_authors: str
    mod_license: str
    mod_logo: str
    mod_homepage: str
    mod_source: str
    mod_issues: str
    mod_discord: str
    mod_modrinth: str
    mod_curseforge: str
    maven_group: str
    archives_name: str
    enabled_platforms: str
    minecraft_version: str
    cobblemon_target_version: str
    fabric_loader_target_version: str
    neoforge_target_version: str
    cobblemon_version: str
    fabric_loader_version: str
    fabric_api_version: str
    fabric_kotlin_version: str
    neoforge_version: str
    neoforge_kotlin_version: str
    modmenu_version: str = ""
```
Add `pytest~=8.3.4` to `scripts/requirements.txt`.

- [ ] **Step 4: Run test to verify it passes**
Run:
```powershell
python -m pytest scripts/tests/test_config_and_models.py
```
Expected: PASS

- [ ] **Step 5: Commit**
```bash
git add scripts/config.py scripts/models.py scripts/requirements.txt scripts/tests/test_config_and_models.py
git commit -m "fix(scripts): make load_env optional and ignore extra properties in models"
```

---

### Task 3: Headless Mode & Platform Upload Enhancements in Publishing Scripts (`scripts/publish.py`, `scripts/modrinth.py`, `scripts/curseforge.py`)

**Files:**
- Modify: [`scripts/publish.py`](file:///scripts/publish.py)
- Modify: [`scripts/modrinth.py`](file:///scripts/modrinth.py)
- Modify: [`scripts/curseforge.py`](file:///scripts/curseforge.py)
- Create: `scripts/tests/test_publish_headless.py`

**Interfaces:**
- Consumes: `--yes` CLI argument or `CI=true` / `GITHUB_ACTIONS=true` env vars.
- Produces: Headless build and upload execution without interactive prompts; updated display names with loader and Minecraft version; dynamic JAR resolution.

- [ ] **Step 1: Write unit tests for headless flags and artifact path resolution**
Create `scripts/tests/test_publish_headless.py`:
```python
import os
from pathlib import Path
import sys
from typer.testing import CliRunner

sys.path.insert(0, str(Path(__file__).parent.parent))
from publish import app

runner = CliRunner()


def test_build_headless_flag_help():
    result = runner.invoke(app, ["build", "--help"])
    assert result.exit_code == 0
    assert "--yes" in result.output or "-y" in result.output


def test_publish_headless_flag_help():
    result = runner.invoke(app, ["publish", "--help"])
    assert result.exit_code == 0
    assert "--yes" in result.output or "-y" in result.output
```

- [ ] **Step 2: Run test to verify failure**
Run:
```powershell
python -m pytest scripts/tests/test_publish_headless.py
```
Expected: FAIL (`--yes` not found in output).

- [ ] **Step 3: Implement headless mode and updated paths in `publish.py`, `modrinth.py`, `curseforge.py`**
In `scripts/publish.py`:
- Helper `def should_auto_confirm(yes: bool) -> bool: return yes or os.getenv("CI") == "true" or os.getenv("GITHUB_ACTIONS") == "true"`
- Replace `typer.confirm(...)` with check:
  ```python
  if not should_auto_confirm(yes):
      confirm = typer.confirm(...)
      if not confirm:
          raise typer.Exit()
  ```
- Update jar path resolution:
  ```python
  fabric_path = ROOT_PATH / "fabric" / "build" / "libs" / f"{mod_properties.mod_id}-fabric-{mod_properties.minecraft_version}-{mod_properties.mod_version}.jar"
  neoforge_path = ROOT_PATH / "neoforge" / "build" / "libs" / f"{mod_properties.mod_id}-neoforge-{mod_properties.minecraft_version}-{mod_properties.mod_version}.jar"
  ```
In `scripts/modrinth.py`:
- Update metadata display name:
  ```python
  "name": f"Cobbleloots v{mod_properties.mod_version} [{mod_properties.minecraft_version}] [{mod_loader.title()}]",
  "featured": (mod_properties.mod_version_type == "release"),
  ```
In `scripts/curseforge.py`:
- Update metadata display name:
  ```python
  "displayName": f"Cobbleloots v{mod_properties.mod_version} [{mod_properties.minecraft_version}] [{mod_loader.title()}]",
  ```

- [ ] **Step 4: Run test to verify it passes**
Run:
```powershell
python -m pytest scripts/tests/test_publish_headless.py
```
Expected: PASS

- [ ] **Step 5: Commit**
```bash
git add scripts/publish.py scripts/modrinth.py scripts/curseforge.py scripts/tests/test_publish_headless.py
git commit -m "feat(publish): add headless mode and updated jar path resolution"
```

---

### Task 4: Semantic Versioning & Changelog Engine (`scripts/release.py`)

**Files:**
- Create: `scripts/release.py`
- Create: `scripts/tests/test_release_engine.py`

**Interfaces:**
- CLI Parameters: `--branch <branch>`, `--dry-run`, `--check`
- Inputs: Git commit history (`git log`), `gradle.properties`, `CHANGELOG.md`
- Outputs: Updated `gradle.properties`, prepended `CHANGELOG.md`, isolated `.release_notes.md`, GitHub Actions `$GITHUB_OUTPUT`.

- [ ] **Step 1: Write comprehensive unit tests for SemVer engine**
Create `scripts/tests/test_release_engine.py`:
```python
import pytest
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).parent.parent))
from release import (
    parse_conventional_commit,
    is_mod_commit,
    calculate_version_bump,
    determine_next_version,
    format_changelog_section,
)


def test_parse_conventional_commit():
    assert parse_conventional_commit("feat(lootball): add master ball") == {
        "type": "feat", "scope": "lootball", "breaking": False, "description": "add master ball"
    }
    assert parse_conventional_commit("fix!: breaking bug fix") == {
        "type": "fix", "scope": None, "breaking": True, "description": "breaking bug fix"
    }
    assert parse_conventional_commit("chore(relay): update worker") == {
        "type": "chore", "scope": "relay", "breaking": False, "description": "update worker"
    }


def test_is_mod_commit_filters_tooling_paths():
    assert is_mod_commit(["common/src/main/Item.java"]) is True
    assert is_mod_commit(["fabric/src/main/Fabric.java"]) is True
    assert is_mod_commit([".agents/rules/architectury.md", "scripts/test.py"]) is False
    assert is_mod_commit([".github/workflows/gradle-ci.yml"]) is False


def test_calculate_version_bump():
    commits = [
        {"type": "fix", "breaking": False},
        {"type": "docs", "breaking": False},
    ]
    assert calculate_version_bump(commits) == "patch"

    commits.append({"type": "feat", "breaking": False})
    assert calculate_version_bump(commits) == "minor"

    commits.append({"type": "chore", "breaking": True})
    assert calculate_version_bump(commits) == "major"


def test_determine_next_version_main():
    assert determine_next_version("2.3.0", "patch", "main", existing_tags=[]) == ("2.3.1", "release", "v2.3.1")
    assert determine_next_version("2.3.0", "minor", "main", existing_tags=[]) == ("2.4.0", "release", "v2.4.0")
    assert determine_next_version("2.3.0", "major", "main", existing_tags=[]) == ("3.0.0", "release", "v3.0.0")


def test_determine_next_version_beta():
    assert determine_next_version("2.3.0", "minor", "beta", existing_tags=[]) == ("2.4.0-beta.1", "beta", "v2.4.0-beta.1")
    assert determine_next_version("2.3.0", "minor", "beta", existing_tags=["v2.4.0-beta.1"]) == ("2.4.0-beta.2", "beta", "v2.4.0-beta.2")


def test_format_changelog_section():
    commits = [
        {"type": "feat", "scope": "lootball", "description": "add new ball"},
        {"type": "fix", "scope": None, "description": "fix crash on join"},
    ]
    changelog = format_changelog_section("2.4.0", commits)
    assert "## 2.4.0" in changelog
    assert "### Features" in changelog
    assert "- **lootball**: add new ball" in changelog
    assert "### Bug Fixes" in changelog
    assert "- fix crash on join" in changelog
```

- [ ] **Step 2: Run test to verify failure**
Run:
```powershell
python -m pytest scripts/tests/test_release_engine.py
```
Expected: FAIL (`release.py` not found).

- [ ] **Step 3: Implement `scripts/release.py`**
Create `scripts/release.py` with:
1. `parse_conventional_commit(message: str) -> dict | None`
2. `is_mod_commit(files: list[str]) -> bool`
3. `get_commits_since_last_tag(base_tag: str | None) -> list[dict]`
4. `calculate_version_bump(commits: list[dict]) -> str | None` ("major", "minor", "patch", or None)
5. `determine_next_version(base_version: str, bump: str, branch: str, existing_tags: list[str]) -> tuple[str, str, str]`
6. `format_changelog_section(version: str, commits: list[dict]) -> str`
7. `update_gradle_properties(new_version: str, new_channel: str)`
8. `prepend_changelog(section: str)`
9. `write_github_output(outputs: dict[str, str])`
10. Typer CLI application with `--branch`, `--dry-run`, and full error handling.

- [ ] **Step 4: Run test to verify it passes**
Run:
```powershell
python -m pytest scripts/tests/test_release_engine.py
```
Expected: PASS

- [ ] **Step 5: Test `scripts/release.py` against current repo**
Run:
```powershell
python scripts/release.py --branch main --dry-run
```
Verify: Since all recent commits are tooling/infrastructure, output reports: `No mod changes detected. Release not required.`

- [ ] **Step 6: Commit**
```bash
git add scripts/release.py scripts/tests/test_release_engine.py
git commit -m "feat(release): implement automated semantic versioning engine"
```

---

### Task 5: GitHub Actions Release Workflow (`.github/workflows/release.yml`)

**Files:**
- Create: `.github/workflows/release.yml`

**Interfaces:**
- Consumes: Push to `main`, `beta`, `alpha`, or `workflow_dispatch` (with `dry_run` input).
- Produces: Automated tag creation, commit with `[skip ci]`, GitHub Release, and distribution to Modrinth & CurseForge.

- [ ] **Step 1: Write `.github/workflows/release.yml`**
Create `.github/workflows/release.yml` matching the exact design specification:
```yaml
name: Release Automation

on:
  push:
    branches:
      - main
      - beta
      - alpha
  workflow_dispatch:
    inputs:
      dry_run:
        description: "Dry-run mode (verify build and calculation without committing or publishing)"
        required: false
        type: boolean
        default: false

permissions:
  contents: write

jobs:
  release:
    name: Automated Multi-Channel Release
    runs-on: ubuntu-latest

    env:
      GRADLE_OPTS: "-Dorg.gradle.jvmargs=-Xmx5g -Dorg.gradle.daemon=false -Dorg.gradle.parallel=false"

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '21'

      - name: Setup Gradle Cache & Toolchain
        uses: gradle/actions/setup-gradle@v4

      - name: Set up Python
        uses: actions/setup-python@v5
        with:
          python-version: '3.12'

      - name: Install Python Dependencies
        run: pip install -r scripts/requirements.txt

      - name: Configure Git Credentials
        run: |
          git config user.name "github-actions[bot]"
          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"

      - name: Run Semantic Versioning Analysis
        id: versioning
        run: |
          python scripts/release.py --branch ${{ github.ref_name }} ${{ inputs.dry_run && '--dry-run' || '' }}

      - name: Check Release Requirement
        if: steps.versioning.outputs.has_release != 'true'
        run: |
          echo "No mod changes detected requiring a release. Exiting cleanly."

      - name: Compile Mod JARs
        if: steps.versioning.outputs.has_release == 'true'
        run: |
          chmod +x ./gradlew
          ./gradlew remapJar --stacktrace

      - name: Check Dry Run
        if: steps.versioning.outputs.has_release == 'true' && inputs.dry_run == true
        run: |
          echo "=== DRY RUN SUMMARY ==="
          echo "Target Version: ${{ steps.versioning.outputs.mod_version }}"
          echo "Target Channel: ${{ steps.versioning.outputs.mod_version_type }}"
          echo "Tag: ${{ steps.versioning.outputs.tag }}"
          echo "Generated Notes:"
          cat .release_notes.md
          echo "Dry run completed. No git commits or external releases made."

      - name: Commit & Tag Release
        if: steps.versioning.outputs.has_release == 'true' && inputs.dry_run != true
        env:
          GITHUB_TOKEN: ${{ secrets.RELEASE_TOKEN || secrets.GITHUB_TOKEN }}
        run: |
          git add gradle.properties CHANGELOG.md
          git commit -m "chore(release): ${{ steps.versioning.outputs.tag }} [skip ci]"
          git tag -a "${{ steps.versioning.outputs.tag }}" -m "Release ${{ steps.versioning.outputs.tag }}"
          git push origin ${{ github.ref_name }} --tags

      - name: Create GitHub Release
        if: steps.versioning.outputs.has_release == 'true' && inputs.dry_run != true
        env:
          GH_TOKEN: ${{ secrets.RELEASE_TOKEN || secrets.GITHUB_TOKEN }}
        run: |
          PRERELEASE_FLAG=""
          if [ "${{ steps.versioning.outputs.is_prerelease }}" = "true" ]; then
            PRERELEASE_FLAG="--prerelease"
          fi
          gh release create "${{ steps.versioning.outputs.tag }}" \
            --title "Cobbleloots ${{ steps.versioning.outputs.tag }} [${{ steps.versioning.outputs.minecraft_version }}]" \
            --notes-file ".release_notes.md" \
            $PRERELEASE_FLAG \
            fabric/build/libs/*.jar neoforge/build/libs/*.jar

      - name: Publish to Modrinth and CurseForge
        if: steps.versioning.outputs.has_release == 'true' && inputs.dry_run != true
        env:
          MODRINTH_TOKEN: ${{ secrets.MODRINTH_TOKEN }}
          CURSEFORGE_TOKEN: ${{ secrets.CURSEFORGE_TOKEN }}
          MODRINTH_PROJECT_ID: ${{ secrets.MODRINTH_PROJECT_ID }}
          CURSEFORGE_PROJECT_ID: ${{ secrets.CURSEFORGE_PROJECT_ID }}
        run: |
          if [ -z "$MODRINTH_TOKEN" ] || [ -z "$CURSEFORGE_TOKEN" ]; then
            echo "Error: Required secrets MODRINTH_TOKEN or CURSEFORGE_TOKEN are missing."
            exit 1
          fi
          python scripts/publish.py publish --yes
```

- [ ] **Step 2: Validate YAML syntax**
Run:
```powershell
python -c "import yaml; yaml.safe_load(open('.github/workflows/release.yml'))"
```
Expected: PASS with no parsing errors.

- [ ] **Step 3: Commit**
```bash
git add .github/workflows/release.yml
git commit -m "ci(github): add automated release workflow for multi-channel publishing"
```

---

### Task 6: Full End-to-End Local Verification

**Files:**
- All created and modified files.

- [ ] **Step 1: Run complete test suite**
Run:
```powershell
python -m pytest scripts/tests/ -v
```
Expected: All tests PASS.

- [ ] **Step 2: Run Gradle compilation**
Run:
```powershell
./gradlew build
```
Expected: Fabric and NeoForge build cleanly without errors.

- [ ] **Step 3: Verify dry-run CLI execution**
Run:
```powershell
python scripts/release.py --branch main --dry-run
```
Expected: Exit code 0, cleanly reports no mod changes on current branch.
