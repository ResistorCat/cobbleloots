import os
from pathlib import Path
import sys
import pytest
from typer.testing import CliRunner

sys.path.insert(0, str(Path(__file__).parent.parent))
from release import (
    parse_conventional_commit,
    is_mod_commit,
    calculate_version_bump,
    determine_next_version,
    format_changelog_section,
    get_base_version_and_tag,
    update_gradle_properties,
    prepend_changelog,
    write_release_notes,
    write_github_output,
    get_changelog_fragments,
    format_changelog_from_fragments,
    consume_changelog_fragments,
    app,
)

runner = CliRunner()


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
    assert parse_conventional_commit("feat: plain feature") == {
        "type": "feat", "scope": None, "breaking": False, "description": "plain feature"
    }
    assert parse_conventional_commit("feat(common)!: major change\n\nBREAKING CHANGE: api changed") == {
        "type": "feat", "scope": "common", "breaking": True, "description": "major change"
    }
    assert parse_conventional_commit("feat: some change\n\nbreaking-change: lowercase hyphenated")["breaking"] is True
    assert parse_conventional_commit("fix: some change\n\nBREAKING CHANGE: with space")["breaking"] is True
    # Non-conventional commit message returns None
    assert parse_conventional_commit("random non conventional commit message") is None


def test_is_mod_commit_filters_tooling_paths():
    assert is_mod_commit(["common/src/main/Item.java"]) is True
    assert is_mod_commit(["fabric/src/main/Fabric.java"]) is True
    assert is_mod_commit(["neoforge/src/main/NeoForge.java"]) is True
    assert is_mod_commit(["build.gradle"]) is True
    assert is_mod_commit(["gradle.properties"]) is True
    assert is_mod_commit([".agents/rules/architectury.md", "scripts/test.py"]) is False
    assert is_mod_commit([".github/workflows/gradle-ci.yml"]) is False
    assert is_mod_commit(["README.md", "package.json"]) is False


def test_calculate_version_bump():
    commits = [
        {"type": "fix", "breaking": False, "is_mod": True},
        {"type": "docs", "breaking": False, "is_mod": False},
    ]
    assert calculate_version_bump(commits) == "patch"

    commits.append({"type": "feat", "breaking": False, "is_mod": True})
    assert calculate_version_bump(commits) == "minor"

    commits.append({"type": "chore", "breaking": True, "is_mod": True})
    assert calculate_version_bump(commits) == "major"

    # Only non-mod commits
    non_mod_commits = [
        {"type": "feat", "breaking": False, "is_mod": False},
        {"type": "fix", "breaking": False, "is_mod": False},
    ]
    assert calculate_version_bump(non_mod_commits) is None


def test_determine_next_version_main():
    assert determine_next_version("2.3.0", "patch", "main", existing_tags=[]) == ("2.3.1", "release", "v2.3.1")
    assert determine_next_version("2.3.0", "minor", "main", existing_tags=[]) == ("2.4.0", "release", "v2.4.0")
    assert determine_next_version("2.3.0", "major", "main", existing_tags=[]) == ("3.0.0", "release", "v3.0.0")


def test_determine_next_version_beta():
    assert determine_next_version("2.3.0", "minor", "beta", existing_tags=[]) == ("2.4.0-beta.1", "beta", "v2.4.0-beta.1")
    assert determine_next_version("2.3.0", "minor", "beta", existing_tags=["v2.4.0-beta.1"]) == ("2.4.0-beta.2", "beta", "v2.4.0-beta.2")
    assert determine_next_version("2.3.0", "minor", "beta", existing_tags=["v2.4.0-beta.1", "v2.4.0-beta.2"]) == ("2.4.0-beta.3", "beta", "v2.4.0-beta.3")


def test_determine_next_version_alpha():
    assert determine_next_version("2.3.0", "minor", "alpha", existing_tags=[]) == ("2.4.0-alpha.1", "alpha", "v2.4.0-alpha.1")


def test_format_changelog_section():
    commits = [
        {"type": "feat", "scope": "lootball", "description": "add new ball", "is_mod": True},
        {"type": "fix", "scope": None, "description": "fix crash on join", "is_mod": True},
        {"type": "refactor", "scope": "common", "description": "optimize entity tick", "is_mod": True},
        {"type": "feat", "scope": "relay", "description": "ignore tooling commit", "is_mod": False},
    ]
    changelog = format_changelog_section("2.4.0", commits)
    assert "## 2.4.0" in changelog
    assert "### Features" in changelog
    assert "- **lootball**: add new ball" in changelog
    assert "### Bug Fixes" in changelog
    assert "- fix crash on join" in changelog
    assert "### Technical Changes" in changelog
    assert "- **common**: optimize entity tick" in changelog
    assert "ignore tooling commit" not in changelog


def test_get_base_version_and_tag():
    # Empty tags fallback to default
    assert get_base_version_and_tag([], "2.3.0") == ("2.3.0", None)

    # Tags present
    tags = ["v2.1.0", "v2.2.0", "v2.3.0", "v2.4.0-beta.1"]
    assert get_base_version_and_tag(tags, "2.0.0") == ("2.3.0", "v2.3.0")


def test_update_gradle_properties(tmp_path):
    props = tmp_path / "gradle.properties"
    props.write_text("mod_version=2.3.0\nmod_version_type=beta\nminecraft_version=1.21.1\n", encoding="utf-8")
    update_gradle_properties("2.4.0", "release", path=props)
    content = props.read_text(encoding="utf-8")
    assert "mod_version=2.4.0\n" in content
    assert "mod_version_type=release\n" in content
    assert "minecraft_version=1.21.1\n" in content


def test_prepend_changelog(tmp_path):
    changelog_file = tmp_path / "CHANGELOG.md"
    initial = "# CHANGELOG\n\nIntro text.\n\n## b2.3.0\n\n- Old feature\n"
    changelog_file.write_text(initial, encoding="utf-8")

    section = "## 2.4.0\n\n### Features\n- New feature"
    prepend_changelog(section, path=changelog_file)

    updated = changelog_file.read_text(encoding="utf-8")
    assert updated.startswith("# CHANGELOG\n\nIntro text.\n\n")
    assert "## 2.4.0\n\n### Features\n- New feature" in updated
    assert updated.index("## 2.4.0") < updated.index("## b2.3.0")


def test_write_release_notes(tmp_path):
    notes_file = tmp_path / ".release_notes.md"
    section = "## 2.4.0\n\n### Features\n- New feature\n"
    res_path = write_release_notes(section, path=notes_file)
    assert res_path.exists()
    assert res_path.read_text(encoding="utf-8").strip() == section.strip()


def test_write_github_output(tmp_path, monkeypatch):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))
    write_github_output({"has_release": "true", "mod_version": "2.4.0"})
    content = out_file.read_text(encoding="utf-8")
    assert "has_release=true\n" in content
    assert "mod_version=2.4.0\n" in content


def test_cli_help():
    result = runner.invoke(app, ["--help"])
    assert result.exit_code == 0
    assert "--branch" in result.output
    assert "--dry-run" in result.output
    assert "--check" in result.output


def test_determine_next_version_beta_patch_retention():
    # When a pre-release tag like v2.4.0-beta.1 exists, patch bump must retain 2.4.0 core version and increment to beta.2
    assert determine_next_version("2.3.0", "patch", "beta", existing_tags=["v2.4.0-beta.1"]) == (
        "2.4.0-beta.2",
        "beta",
        "v2.4.0-beta.2",
    )
    # Major bump advances core version to next major (3.0.0-beta.1)
    assert determine_next_version("2.3.0", "major", "beta", existing_tags=["v2.4.0-beta.1"]) == (
        "3.0.0-beta.1",
        "beta",
        "v3.0.0-beta.1",
    )


def test_pre_release_anchor_tag_prevents_re_release_loop(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    # Mock git describe returning an existing beta tag on the active branch
    monkeypatch.setattr("release.get_latest_tag_on_branch", lambda: "v2.4.0-beta.1")
    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0", "v2.4.0-beta.1"])

    # Commits since v2.4.0-beta.1 are only tooling/non-mod commits
    def mock_get_commits(anchor_tag):
        assert anchor_tag == "v2.4.0-beta.1"
        return [
            {"type": "docs", "breaking": False, "is_mod": False, "files": ["docs/faq.md"]},
            {"type": "chore", "breaking": False, "is_mod": False, "files": [".agents/rules.md"]},
        ]

    monkeypatch.setattr("release.get_commits_since_last_tag", mock_get_commits)

    result = runner.invoke(app, ["--branch", "beta", "--dry-run"])
    assert result.exit_code == 0
    assert "No mod changes detected. Release not required." in result.output
    content = out_file.read_text(encoding="utf-8")
    assert "has_release=false" in content


def test_dry_run_does_not_write_release_notes(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    # Mock commits that have a mod bump
    monkeypatch.setattr("release.get_commits_since_last_tag", lambda tag: [
        {"type": "feat", "scope": "lootball", "breaking": False, "is_mod": True, "description": "new ball", "files": ["common/Item.java"]}
    ])

    notes_file = tmp_path / ".release_notes.md"
    monkeypatch.setattr("release.ROOT_PATH", tmp_path)

    result = runner.invoke(app, ["--branch", "main", "--dry-run"])
    assert result.exit_code == 0
    assert "[DRY-RUN]" in result.output
    assert not notes_file.exists()


def test_cli_dry_run_current_repo(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))
    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0"])
    monkeypatch.setattr("release.get_latest_tag_on_branch", lambda: "v2.3.0")
    monkeypatch.setattr("release.get_commits_since_last_tag", lambda tag: [])
    result = runner.invoke(app, ["--branch", "main", "--dry-run"])
    assert result.exit_code == 0
    assert "No mod changes detected. Release not required." in result.output
    content = out_file.read_text(encoding="utf-8")
    assert "has_release=false" in content


def test_determine_next_version_ignores_old_prerelease_tags_after_stable_release():
    # With base_version="2.4.0" (stable released), existing pre-release tags like v2.4.0-beta.1
    # belong to the past cycle and must not resurrect. A new minor bump on beta must start 2.5.0-beta.1.
    version, channel, tag = determine_next_version(
        "2.4.0", "minor", "beta", existing_tags=["v2.4.0-beta.1", "v2.4.0"]
    )
    assert version == "2.5.0-beta.1"
    assert channel == "beta"
    assert tag == "v2.5.0-beta.1"


def test_determine_next_version_increments_active_prerelease_cycle_without_stable_tag():
    # When base_version="2.4.0" (derived from v2.4.0-alpha.1) and v2.4.0 stable does not exist,
    # v2.4.0-alpha.1 belongs to the active cycle and must advance to 2.4.0-alpha.2.
    version, channel, tag = determine_next_version(
        "2.4.0", "minor", "alpha", existing_tags=["v2.4.0-alpha.1"]
    )
    assert version == "2.4.0-alpha.2"
    assert channel == "alpha"
    assert tag == "v2.4.0-alpha.2"


def test_determine_next_version_increments_active_prerelease_cycle_with_multiple_tags():
    # In the current repo, v2.5.0-alpha.1 exists without a v2.5.0 stable tag.
    # The next release must advance to 2.5.0-alpha.2.
    version, channel, tag = determine_next_version(
        "2.5.0", "minor", "alpha", existing_tags=["v2.4.0-alpha.1", "v2.5.0-alpha.1"]
    )
    assert version == "2.5.0-alpha.2"
    assert channel == "alpha"
    assert tag == "v2.5.0-alpha.2"


def test_determine_next_version_graduates_unreleased_prerelease_to_stable():
    # When graduating to main with v2.5.0-alpha.1 and v2.5.0-alpha.2 existing,
    # the release version on main must graduate to 2.5.0 (stable).
    version, channel, tag = determine_next_version(
        "2.3.0", "minor", "main", existing_tags=["v2.4.0-alpha.1", "v2.5.0-alpha.1", "v2.5.0-alpha.2"]
    )
    assert version == "2.5.0"
    assert channel == "release"
    assert tag == "v2.5.0"


def test_determine_next_version_promotes_unreleased_alpha_to_beta():
    # When graduating alpha to beta with v2.5.0-alpha.1 and v2.4.0-alpha.1 existing without stable tag,
    # beta must inherit the latest alpha core (2.5.0) and start 2.5.0-beta.1.
    version, channel, tag = determine_next_version(
        "2.3.0", "minor", "beta", existing_tags=["v2.3.0", "v2.4.0-alpha.1", "v2.5.0-alpha.1"]
    )
    assert version == "2.5.0-beta.1"
    assert channel == "beta"
    assert tag == "v2.5.0-beta.1"


def test_determine_next_version_advances_existing_beta_cycle():
    # When v2.5.0-beta.1 already exists, the next bump on beta should increment to 2.5.0-beta.2.
    version, channel, tag = determine_next_version(
        "2.3.0", "minor", "beta", existing_tags=["v2.3.0", "v2.5.0-alpha.1", "v2.5.0-beta.1"]
    )
    assert version == "2.5.0-beta.2"
    assert channel == "beta"
    assert tag == "v2.5.0-beta.2"




def test_graduation_on_main_uses_stable_base_tag(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    # Existing tags in repo: v2.3.0 (stable) and v2.4.0-beta.1 (prerelease)
    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0", "v2.4.0-beta.1"])
    # Latest tag reachable on main branch is v2.4.0-beta.1 from the merged PR
    monkeypatch.setattr("release.get_latest_tag_on_branch", lambda: "v2.4.0-beta.1")

    # On main, commits since base_tag (v2.3.0) must be inspected, NOT latest_branch_tag (v2.4.0-beta.1)
    def mock_get_commits(anchor_tag):
        assert anchor_tag == "v2.3.0"
        return [
            {
                "type": "feat",
                "scope": "lootball",
                "breaking": False,
                "is_mod": True,
                "description": "add new ball",
                "files": ["common/Item.java"],
            }
        ]

    monkeypatch.setattr("release.get_commits_since_last_tag", mock_get_commits)

    result = runner.invoke(app, ["--branch", "main", "--dry-run"])
    assert result.exit_code == 0
    assert "Release calculated: v2.4.0" in result.output
    content = out_file.read_text(encoding="utf-8")
    assert "has_release=true" in content
    assert "mod_version=2.4.0" in content
    assert "tag=v2.4.0" in content
    assert "is_prerelease=false" in content


def test_get_changelog_fragments(tmp_path):
    changelog_dir = tmp_path / ".changelog"
    changelog_dir.mkdir()

    # Ignored files
    (changelog_dir / "README.md").write_text("# Readme", encoding="utf-8")
    (changelog_dir / ".gitkeep").write_text("", encoding="utf-8")
    (changelog_dir / "empty.md").write_text("   \n  ", encoding="utf-8")

    # Valid fragment files
    (changelog_dir / "DEV-1.md").write_text("### Changes\n- Feature 1", encoding="utf-8")
    (changelog_dir / "DEV-2.md").write_text("### Bug Fixes\n- Fix 1", encoding="utf-8")

    fragments = get_changelog_fragments(changelog_dir)
    assert len(fragments) == 2
    paths = [f[0].name for f in fragments]
    assert paths == ["DEV-1.md", "DEV-2.md"]
    assert fragments[0][1] == "### Changes\n- Feature 1"
    assert fragments[1][1] == "### Bug Fixes\n- Fix 1"


def test_format_changelog_from_fragments():
    fragment_1 = """### Gameplay Changes
- **Loot Ball Reset**: Added reset command.

### Bug Fixes
- Fixed targeting bug.
"""
    fragment_2 = """### Gameplay Changes
- **Fishing Loot**: Added new fishing rods.

### Technical Changes
- **Networking**: Overhauled packet registry.
"""
    fragment_3 = """- Just a plain bullet point change."""

    formatted = format_changelog_from_fragments("2.4.0", [fragment_1, fragment_2, fragment_3])

    assert "## 2.4.0" in formatted
    assert "### Gameplay Changes" in formatted
    assert "- **Loot Ball Reset**: Added reset command." in formatted
    assert "- **Fishing Loot**: Added new fishing rods." in formatted
    assert "### Changes" in formatted
    assert "- Just a plain bullet point change." in formatted
    assert "### Technical Changes" in formatted
    assert "- **Networking**: Overhauled packet registry." in formatted
    assert "### Bug Fixes" in formatted
    assert "- Fixed targeting bug." in formatted

    # Verify section order: Gameplay Changes before Changes, before Technical Changes, before Bug Fixes
    pos_gameplay = formatted.index("### Gameplay Changes")
    pos_changes = formatted.index("### Changes")
    pos_tech = formatted.index("### Technical Changes")
    pos_fixes = formatted.index("### Bug Fixes")
    assert pos_gameplay < pos_changes < pos_tech < pos_fixes


def test_consume_changelog_fragments(tmp_path):
    f1 = tmp_path / "DEV-1.md"
    f2 = tmp_path / "DEV-2.md"
    f1.write_text("content", encoding="utf-8")
    f2.write_text("content", encoding="utf-8")

    assert f1.exists() and f2.exists()
    consume_changelog_fragments([f1, f2])
    assert not f1.exists()
    assert not f2.exists()


def test_release_engine_uses_fragments_when_present(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    # Mock commits that trigger a bump
    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0"])
    monkeypatch.setattr("release.get_latest_tag_on_branch", lambda: "v2.3.0")
    monkeypatch.setattr("release.get_commits_since_last_tag", lambda tag: [
        {"type": "feat", "scope": "lootball", "breaking": False, "is_mod": True, "description": "technical commit", "files": ["common/Item.java"]}
    ])

    changelog_dir = tmp_path / ".changelog"
    changelog_dir.mkdir()
    frag_file = changelog_dir / "DEV-5.md"
    frag_file.write_text("### Gameplay Changes\n- **Player Feature**: High-level explanation for players.", encoding="utf-8")

    monkeypatch.setattr("release.ROOT_PATH", tmp_path)
    monkeypatch.setattr("release.get_changelog_fragments", lambda: [(frag_file, frag_file.read_text(encoding="utf-8"))])

    props = tmp_path / "gradle.properties"
    props.write_text("mod_version=2.3.0\nmod_version_type=release\n", encoding="utf-8")
    cl_file = tmp_path / "CHANGELOG.md"
    cl_file.write_text("# CHANGELOG\n\n## 2.3.0\n- Old", encoding="utf-8")

    result = runner.invoke(app, ["--branch", "main"])
    assert result.exit_code == 0
    assert "Release prepared: v2.4.0" in result.output

    # Check that release notes use the fragment and NOT the technical commit description
    notes_file = tmp_path / ".release_notes.md"
    assert notes_file.exists()
    notes_content = notes_file.read_text(encoding="utf-8")
    assert "### Gameplay Changes" in notes_content
    assert "**Player Feature**: High-level explanation for players." in notes_content
    assert "technical commit" not in notes_content

    # Check that fragment was consumed (deleted)
    assert not frag_file.exists()



