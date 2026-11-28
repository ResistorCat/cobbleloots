from datetime import datetime, timedelta, timezone
from pathlib import Path
import sys
from unittest.mock import MagicMock
import pytest
from typer.testing import CliRunner

sys.path.insert(0, str(Path(__file__).parent.parent))
from release import (
    TOOLING_SCOPES,
    _is_tooling_scope,
    parse_conventional_commit,
    is_mod_commit,
    normalize_channel,
    resolve_channel_from_labels,
    fetch_pr_labels_for_commit,
    resolve_channel_for_commit,
    calculate_version_bump,
    determine_next_version,
    get_latest_unreleased_beta_tag,
    evaluate_beta_promotion,
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
        "type": "feat",
        "scope": "lootball",
        "breaking": False,
        "description": "add master ball",
    }
    assert parse_conventional_commit("fix!: breaking bug fix") == {
        "type": "fix",
        "scope": None,
        "breaking": True,
        "description": "breaking bug fix",
    }
    assert parse_conventional_commit("chore(relay): update worker") == {
        "type": "chore",
        "scope": "relay",
        "breaking": False,
        "description": "update worker",
    }
    assert parse_conventional_commit("feat: plain feature") == {
        "type": "feat",
        "scope": None,
        "breaking": False,
        "description": "plain feature",
    }
    assert parse_conventional_commit(
        "feat(common)!: major change\n\nBREAKING CHANGE: api changed"
    ) == {
        "type": "feat",
        "scope": "common",
        "breaking": True,
        "description": "major change",
    }
    assert (
        parse_conventional_commit(
            "feat: some change\n\nbreaking-change: lowercase hyphenated"
        )["breaking"]
        is True
    )
    assert (
        parse_conventional_commit("fix: some change\n\nBREAKING CHANGE: with space")[
            "breaking"
        ]
        is True
    )
    assert parse_conventional_commit("random non conventional commit message") is None


def test_tooling_scopes_include_build_and_gradle():
    assert "build" in TOOLING_SCOPES
    assert "gradle" in TOOLING_SCOPES
    assert _is_tooling_scope("build") is True
    assert _is_tooling_scope("gradle") is True
    assert _is_tooling_scope("build,gradle") is True
    assert _is_tooling_scope("Build") is True
    assert _is_tooling_scope("lootball") is False
    assert _is_tooling_scope("fabric,build") is False
    assert _is_tooling_scope(None) is False


def test_is_mod_commit_filters_tooling_build_and_test_paths():
    # Real mod source/resource files count as mod commits
    assert is_mod_commit(["common/src/main/Item.java"]) is True
    assert is_mod_commit(["fabric/src/main/Fabric.java"]) is True
    assert is_mod_commit(["neoforge/src/main/NeoForge.java"]) is True
    assert is_mod_commit(["common/src/main/resources/data/cobbleloots/loot.json"]) is True

    # Build, Gradle, and test-only files must NOT count as mod commits
    assert is_mod_commit(["build.gradle"]) is False
    assert is_mod_commit(["settings.gradle"]) is False
    assert is_mod_commit(["gradle.properties"]) is False
    assert is_mod_commit(["common/build.gradle"]) is False
    assert is_mod_commit(["fabric/build.gradle"]) is False
    assert is_mod_commit(["neoforge/build.gradle.kts"]) is False
    assert is_mod_commit(["gradle/wrapper/gradle-wrapper.properties"]) is False
    assert is_mod_commit(["src/test/java/ExampleTest.java"]) is False
    assert is_mod_commit(["fabric/src/test/java/ModIconTest.java"]) is False
    assert is_mod_commit(["common/src/test/resources/fixture.json"]) is False

    # Repo tooling/docs paths do not count
    assert is_mod_commit([".agents/rules/architectury.md", "scripts/test.py"]) is False
    assert is_mod_commit([".github/workflows/gradle-ci.yml"]) is False
    assert is_mod_commit(["README.md", "RELEASING.md"]) is False
    assert is_mod_commit([]) is False

    # Mixed commit touching build/test AND real mod code counts as a mod commit
    assert (
        is_mod_commit(
            [
                "fabric/build.gradle",
                "fabric/src/test/java/IconTest.java",
                "fabric/src/main/resources/assets/cobbleloots/icon.png",
            ]
        )
        is True
    )


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

    # Non-mod commits never bump
    non_mod_commits = [
        {"type": "feat", "breaking": False, "is_mod": False},
        {"type": "fix", "breaking": False, "is_mod": False},
    ]
    assert calculate_version_bump(non_mod_commits) is None

    # Non-releasing commit types on mod paths do not bump
    non_releasing_mod_commits = [
        {"type": "build", "breaking": False, "is_mod": True},
        {"type": "ci", "breaking": False, "is_mod": True},
        {"type": "test", "breaking": False, "is_mod": True},
        {"type": "chore", "breaking": False, "is_mod": True},
        {"type": "refactor", "breaking": False, "is_mod": True},
        {"type": "docs", "breaking": False, "is_mod": True},
    ]
    assert calculate_version_bump(non_releasing_mod_commits) is None


def test_normalize_channel():
    assert normalize_channel("alpha") == "alpha"
    assert normalize_channel("BETA") == "beta"
    assert normalize_channel("release") == "release"
    assert normalize_channel("stable") == "release"
    with pytest.raises(ValueError):
        normalize_channel("main")


def test_determine_next_version_release():
    assert determine_next_version("2.3.0", "patch", "release", existing_tags=[]) == (
        "2.3.1",
        "release",
        "v2.3.1",
    )
    assert determine_next_version("2.3.0", "minor", "release", existing_tags=[]) == (
        "2.4.0",
        "release",
        "v2.4.0",
    )
    assert determine_next_version("2.3.0", "major", "release", existing_tags=[]) == (
        "3.0.0",
        "release",
        "v3.0.0",
    )


def test_determine_next_version_beta():
    assert determine_next_version("2.3.0", "minor", "beta", existing_tags=[]) == (
        "2.4.0-beta.1",
        "beta",
        "v2.4.0-beta.1",
    )
    assert determine_next_version(
        "2.3.0", "minor", "beta", existing_tags=["v2.4.0-beta.1"]
    ) == ("2.4.0-beta.2", "beta", "v2.4.0-beta.2")
    assert determine_next_version(
        "2.3.0", "minor", "beta", existing_tags=["v2.4.0-beta.1", "v2.4.0-beta.2"]
    ) == ("2.4.0-beta.3", "beta", "v2.4.0-beta.3")


def test_determine_next_version_alpha():
    assert determine_next_version("2.3.0", "minor", "alpha", existing_tags=[]) == (
        "2.4.0-alpha.1",
        "alpha",
        "v2.4.0-alpha.1",
    )


def test_format_changelog_section():
    commits = [
        {"type": "feat", "scope": "lootball", "description": "add new ball", "is_mod": True},
        {"type": "fix", "scope": None, "description": "fix crash on join", "is_mod": True},
        {
            "type": "refactor",
            "scope": "common",
            "description": "optimize entity tick",
            "is_mod": True,
        },
        {
            "type": "feat",
            "scope": "relay",
            "description": "ignore tooling commit",
            "is_mod": False,
        },
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
    assert get_base_version_and_tag([], "2.3.0") == ("2.3.0", None)

    tags = ["v2.1.0", "v2.2.0", "v2.3.0", "v2.4.0-beta.1"]
    assert get_base_version_and_tag(tags, "2.0.0") == ("2.3.0", "v2.3.0")


def test_update_gradle_properties(tmp_path):
    props = tmp_path / "gradle.properties"
    props.write_text(
        "mod_version=2.3.0\nmod_version_type=beta\nminecraft_version=1.21.1\n",
        encoding="utf-8",
    )
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
    assert "--channel" in result.output
    assert "--detect-pr-channel" in result.output
    assert "--check-stable-promotion" in result.output
    assert "--dry-run" in result.output
    assert "--check" in result.output


def test_determine_next_version_beta_patch_retention():
    assert determine_next_version(
        "2.3.0", "patch", "beta", existing_tags=["v2.4.0-beta.1"]
    ) == ("2.4.0-beta.2", "beta", "v2.4.0-beta.2")
    assert determine_next_version(
        "2.3.0", "major", "beta", existing_tags=["v2.4.0-beta.1"]
    ) == ("3.0.0-beta.1", "beta", "v3.0.0-beta.1")


def test_pre_release_anchor_tag_prevents_re_release_loop(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    monkeypatch.setattr(
        "release.get_latest_tag_on_branch", lambda ref=None: "v2.4.0-beta.1"
    )
    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0", "v2.4.0-beta.1"])

    def mock_get_commits(anchor_tag):
        assert anchor_tag == "v2.4.0-beta.1"
        return [
            {
                "type": "docs",
                "breaking": False,
                "is_mod": False,
                "files": ["docs/faq.md"],
            },
            {
                "type": "chore",
                "breaking": False,
                "is_mod": False,
                "files": [".agents/rules.md"],
            },
        ]

    monkeypatch.setattr("release.get_commits_since_last_tag", mock_get_commits)

    result = runner.invoke(app, ["--branch", "main", "--channel", "beta", "--dry-run"])
    assert result.exit_code == 0
    assert "No mod changes detected. Release not required." in result.output
    content = out_file.read_text(encoding="utf-8")
    assert "has_release=false" in content


def test_dry_run_does_not_write_release_notes(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    monkeypatch.setattr(
        "release.get_commits_since_last_tag",
        lambda tag: [
            {
                "type": "feat",
                "scope": "lootball",
                "breaking": False,
                "is_mod": True,
                "description": "new ball",
                "files": ["common/Item.java"],
            }
        ],
    )

    notes_file = tmp_path / ".release_notes.md"
    monkeypatch.setattr("release.ROOT_PATH", tmp_path)

    result = runner.invoke(
        app, ["--branch", "main", "--channel", "release", "--dry-run"]
    )
    assert result.exit_code == 0
    assert "[DRY-RUN]" in result.output
    assert not notes_file.exists()


def test_cli_dry_run_current_repo(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))
    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0"])
    monkeypatch.setattr("release.get_latest_tag_on_branch", lambda ref=None: "v2.3.0")
    monkeypatch.setattr("release.get_commits_since_last_tag", lambda tag: [])
    result = runner.invoke(
        app, ["--branch", "main", "--channel", "release", "--dry-run"]
    )
    assert result.exit_code == 0
    assert "No mod changes detected. Release not required." in result.output
    content = out_file.read_text(encoding="utf-8")
    assert "has_release=false" in content


def test_cli_channel_independent_of_branch_name(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0", "v2.5.0-alpha.4"])
    monkeypatch.setattr(
        "release.get_latest_tag_on_branch", lambda ref=None: "v2.5.0-alpha.4"
    )
    monkeypatch.setattr(
        "release.get_commits_since_last_tag",
        lambda tag: [
            {
                "type": "feat",
                "scope": "lootball",
                "breaking": False,
                "is_mod": True,
                "description": "new ball",
                "files": ["common/src/main/Item.java"],
            }
        ],
    )

    # Branch is main, but --channel alpha produces an alpha release
    result = runner.invoke(app, ["--branch", "main", "--channel", "alpha", "--dry-run"])
    assert result.exit_code == 0
    content = out_file.read_text(encoding="utf-8")
    assert "mod_version=2.5.0-alpha.5" in content
    assert "mod_version_type=alpha" in content
    assert "is_prerelease=true" in content

    # Branch is main, --channel beta promotes unreleased alpha cycle to beta.1
    out_file.write_text("", encoding="utf-8")
    result = runner.invoke(app, ["--branch", "main", "--channel", "beta", "--dry-run"])
    assert result.exit_code == 0
    content = out_file.read_text(encoding="utf-8")
    assert "mod_version=2.5.0-beta.1" in content
    assert "mod_version_type=beta" in content
    assert "is_prerelease=true" in content

    # Branch is mc/1.21.1, --channel release produces a stable release
    out_file.write_text("", encoding="utf-8")
    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.5.0"])
    monkeypatch.setattr("release.get_latest_tag_on_branch", lambda ref=None: "v2.5.0")
    monkeypatch.setattr(
        "release.get_commits_since_last_tag",
        lambda tag: [
            {
                "type": "fix",
                "scope": "lootball",
                "breaking": False,
                "is_mod": True,
                "description": "backport fix",
                "files": ["common/src/main/Item.java"],
            }
        ],
    )
    result = runner.invoke(
        app, ["--branch", "mc/1.21.1", "--channel", "release", "--dry-run"]
    )
    assert result.exit_code == 0
    content = out_file.read_text(encoding="utf-8")
    assert "mod_version=2.5.1" in content
    assert "mod_version_type=release" in content
    assert "is_prerelease=false" in content


def test_cli_invalid_channel_exits_nonzero():
    result = runner.invoke(app, ["--channel", "nightly", "--dry-run"])
    assert result.exit_code != 0
    assert "Invalid release channel" in result.output


def test_determine_next_version_ignores_old_prerelease_tags_after_stable_release():
    version, channel, tag = determine_next_version(
        "2.4.0", "minor", "beta", existing_tags=["v2.4.0-beta.1", "v2.4.0"]
    )
    assert version == "2.5.0-beta.1"
    assert channel == "beta"
    assert tag == "v2.5.0-beta.1"


def test_determine_next_version_increments_active_prerelease_cycle_without_stable_tag():
    version, channel, tag = determine_next_version(
        "2.4.0", "minor", "alpha", existing_tags=["v2.4.0-alpha.1"]
    )
    assert version == "2.4.0-alpha.2"
    assert channel == "alpha"
    assert tag == "v2.4.0-alpha.2"


def test_determine_next_version_increments_active_prerelease_cycle_with_multiple_tags():
    version, channel, tag = determine_next_version(
        "2.5.0", "minor", "alpha", existing_tags=["v2.4.0-alpha.1", "v2.5.0-alpha.1"]
    )
    assert version == "2.5.0-alpha.2"
    assert channel == "alpha"
    assert tag == "v2.5.0-alpha.2"


def test_determine_next_version_graduates_unreleased_prerelease_to_stable():
    version, channel, tag = determine_next_version(
        "2.3.0",
        "minor",
        "release",
        existing_tags=["v2.4.0-alpha.1", "v2.5.0-alpha.1", "v2.5.0-alpha.2"],
    )
    assert version == "2.5.0"
    assert channel == "release"
    assert tag == "v2.5.0"


def test_determine_next_version_promotes_unreleased_alpha_to_beta():
    version, channel, tag = determine_next_version(
        "2.3.0",
        "minor",
        "beta",
        existing_tags=["v2.3.0", "v2.4.0-alpha.1", "v2.5.0-alpha.1"],
    )
    assert version == "2.5.0-beta.1"
    assert channel == "beta"
    assert tag == "v2.5.0-beta.1"


def test_determine_next_version_advances_existing_beta_cycle():
    version, channel, tag = determine_next_version(
        "2.3.0",
        "minor",
        "beta",
        existing_tags=["v2.3.0", "v2.5.0-alpha.1", "v2.5.0-beta.1"],
    )
    assert version == "2.5.0-beta.2"
    assert channel == "beta"
    assert tag == "v2.5.0-beta.2"


def test_determine_next_version_alpha_during_active_beta_starts_next_cycle():
    tags = ["v2.3.0", "v2.5.0-alpha.4", "v2.5.0-beta.1"]

    # feat -> X.(Y+1).0-alpha.1
    assert determine_next_version("2.3.0", "minor", "alpha", existing_tags=tags) == (
        "2.6.0-alpha.1",
        "alpha",
        "v2.6.0-alpha.1",
    )
    # fix -> X.Y.(Z+1)-alpha.1
    assert determine_next_version("2.3.0", "patch", "alpha", existing_tags=tags) == (
        "2.5.1-alpha.1",
        "alpha",
        "v2.5.1-alpha.1",
    )
    # breaking -> (X+1).0.0-alpha.1
    assert determine_next_version("2.3.0", "major", "alpha", existing_tags=tags) == (
        "3.0.0-alpha.1",
        "alpha",
        "v3.0.0-alpha.1",
    )

    # Subsequent next-cycle alpha increments 2.6.0-alpha.1 -> 2.6.0-alpha.2
    tags_with_next_alpha = tags + ["v2.6.0-alpha.1"]
    assert determine_next_version(
        "2.3.0", "minor", "alpha", existing_tags=tags_with_next_alpha
    ) == ("2.6.0-alpha.2", "alpha", "v2.6.0-alpha.2")

    # Graduating active beta to stable must target v2.5.0, not next-cycle v2.6.0
    assert determine_next_version(
        "2.3.0", "minor", "release", existing_tags=tags_with_next_alpha
    ) == ("2.5.0", "release", "v2.5.0")


def test_determine_next_version_alpha_after_stable_release():
    tags = ["v2.3.0", "v2.5.0-alpha.4", "v2.5.0-beta.1", "v2.5.0"]

    assert determine_next_version("2.5.0", "minor", "alpha", existing_tags=tags) == (
        "2.6.0-alpha.1",
        "alpha",
        "v2.6.0-alpha.1",
    )
    assert determine_next_version("2.5.0", "patch", "alpha", existing_tags=tags) == (
        "2.5.1-alpha.1",
        "alpha",
        "v2.5.1-alpha.1",
    )
    assert determine_next_version("2.5.0", "major", "alpha", existing_tags=tags) == (
        "3.0.0-alpha.1",
        "alpha",
        "v3.0.0-alpha.1",
    )


def test_next_alpha_on_main_does_not_repeat_versions_when_stable_tag_unreachable(
    monkeypatch, tmp_path
):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    feat_commit = [
        {
            "hash": "abcdef1234567890",
            "subject": "feat(lootball): add next-cycle feature",
            "type": "feat",
            "scope": "lootball",
            "breaking": False,
            "is_mod": True,
            "description": "add next-cycle feature",
            "files": ["common/src/main/Item.java"],
        }
    ]

    # Case A: v2.5.0 was tagged off-branch from v2.5.0-beta.1; main already moved ahead
    # with v2.6.0-alpha.1, so v2.5.0 is not reachable from main's HEAD.
    monkeypatch.setattr(
        "release.get_git_tags",
        lambda: ["v2.3.0", "v2.5.0-alpha.4", "v2.5.0-beta.1", "v2.6.0-alpha.1", "v2.5.0"],
    )
    monkeypatch.setattr(
        "release.get_latest_tag_on_branch", lambda ref=None: "v2.6.0-alpha.1"
    )
    monkeypatch.setattr("release.get_commits_since_last_tag", lambda tag: feat_commit)

    result = runner.invoke(app, ["--branch", "main", "--channel", "alpha", "--dry-run"])
    assert result.exit_code == 0
    content = out_file.read_text(encoding="utf-8")
    assert "mod_version=2.6.0-alpha.2" in content
    assert "tag=v2.6.0-alpha.2" in content

    # Case B: v2.5.0 was tagged off-branch from v2.5.0-beta.1; main only had non-mod commits
    # so the latest reachable tag on main is still v2.5.0-beta.1.
    out_file.write_text("", encoding="utf-8")
    monkeypatch.setattr(
        "release.get_git_tags",
        lambda: ["v2.3.0", "v2.5.0-alpha.4", "v2.5.0-beta.1", "v2.5.0"],
    )
    monkeypatch.setattr(
        "release.get_latest_tag_on_branch", lambda ref=None: "v2.5.0-beta.1"
    )

    result = runner.invoke(app, ["--branch", "main", "--channel", "alpha", "--dry-run"])
    assert result.exit_code == 0
    content = out_file.read_text(encoding="utf-8")
    assert "mod_version=2.6.0-alpha.1" in content
    assert "tag=v2.6.0-alpha.1" in content


def test_graduation_on_main_uses_stable_base_tag(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0", "v2.4.0-beta.1"])
    monkeypatch.setattr(
        "release.get_latest_tag_on_branch", lambda ref=None: "v2.4.0-beta.1"
    )

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

    result = runner.invoke(
        app, ["--branch", "main", "--channel", "release", "--dry-run"]
    )
    assert result.exit_code == 0
    assert "Release calculated: v2.4.0" in result.output
    content = out_file.read_text(encoding="utf-8")
    assert "has_release=true" in content
    assert "mod_version=2.4.0" in content
    assert "tag=v2.4.0" in content
    assert "is_prerelease=false" in content


def test_beta_promotion_from_alpha_anchors_to_stable_base_tag(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0", "v2.5.0-alpha.4"])
    monkeypatch.setattr(
        "release.get_latest_tag_on_branch", lambda ref=None: "v2.5.0-alpha.4"
    )

    def mock_get_commits(anchor_tag):
        assert anchor_tag == "v2.3.0"
        return [
            {
                "type": "feat",
                "scope": "lootball",
                "breaking": False,
                "is_mod": True,
                "description": "alpha feature",
                "files": ["common/src/main/Item.java"],
            }
        ]

    monkeypatch.setattr("release.get_commits_since_last_tag", mock_get_commits)

    result = runner.invoke(app, ["--branch", "main", "--channel", "beta", "--dry-run"])
    assert result.exit_code == 0
    content = out_file.read_text(encoding="utf-8")
    assert "has_release=true" in content
    assert "mod_version=2.5.0-beta.1" in content
    assert "tag=v2.5.0-beta.1" in content


def test_resolve_channel_from_labels():
    assert resolve_channel_from_labels([]) == "alpha"
    assert resolve_channel_from_labels(["type: feature", "area: gameplay"]) == "alpha"
    assert resolve_channel_from_labels(["release:beta"]) == "beta"
    assert resolve_channel_from_labels(["type: feature", "release:beta"]) == "beta"
    assert resolve_channel_from_labels(["release:stable"]) == "release"
    assert resolve_channel_from_labels(["release:beta", "release:stable"]) == "release"
    assert resolve_channel_from_labels(["RELEASE:BETA"]) == "beta"


def test_resolve_channel_for_commit_with_mocked_github_api(monkeypatch):
    def mock_get(url, headers=None, timeout=None):
        assert url == "https://api.github.com/repos/ResistorCat/cobbleloots/commits/abc123/pulls"
        assert headers["Authorization"] == "Bearer test-token"
        resp = MagicMock()
        resp.status_code = 200
        resp.json.return_value = [
            {
                "number": 51,
                "merged_at": "2026-11-27T10:00:00Z",
                "labels": [{"name": "type: feature"}, {"name": "release:beta"}],
            }
        ]
        return resp

    monkeypatch.setattr("release.requests.get", mock_get)
    assert (
        resolve_channel_for_commit("ResistorCat/cobbleloots", "abc123", token="test-token")
        == "beta"
    )


def test_resolve_channel_for_commit_stable_label(monkeypatch):
    resp = MagicMock()
    resp.status_code = 200
    resp.json.return_value = [
        {
            "number": 52,
            "merged_at": "2026-11-27T12:00:00Z",
            "labels": [{"name": "release:stable"}],
        }
    ]
    monkeypatch.setattr("release.requests.get", lambda *args, **kwargs: resp)
    assert (
        resolve_channel_for_commit("ResistorCat/cobbleloots", "def456", token="tok")
        == "release"
    )


def test_resolve_channel_for_commit_unlabeled_pr_defaults_to_alpha(monkeypatch):
    resp = MagicMock()
    resp.status_code = 200
    resp.json.return_value = [
        {
            "number": 53,
            "merged_at": "2026-11-27T12:00:00Z",
            "labels": [{"name": "type: bug"}],
        }
    ]
    monkeypatch.setattr("release.requests.get", lambda *args, **kwargs: resp)
    assert (
        resolve_channel_for_commit("ResistorCat/cobbleloots", "ghi789", token="tok")
        == "alpha"
    )


def test_fetch_pr_labels_handles_api_failure_gracefully(monkeypatch):
    resp = MagicMock()
    resp.status_code = 403
    monkeypatch.setattr("release.requests.get", lambda *args, **kwargs: resp)
    assert fetch_pr_labels_for_commit("ResistorCat/cobbleloots", "abc123") == []

    def boom(*args, **kwargs):
        raise RuntimeError("network error")

    monkeypatch.setattr("release.requests.get", boom)
    assert resolve_channel_for_commit("ResistorCat/cobbleloots", "abc123") == "alpha"


def test_cli_detect_pr_channel_uses_github_api(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))
    monkeypatch.setenv("GITHUB_TOKEN", "fake-gh-token")

    resp = MagicMock()
    resp.status_code = 200
    resp.json.return_value = [
        {
            "number": 60,
            "merged_at": "2026-11-27T15:00:00Z",
            "labels": [{"name": "release:beta"}],
        }
    ]
    monkeypatch.setattr("release.requests.get", lambda *args, **kwargs: resp)
    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0", "v2.5.0-alpha.4"])
    monkeypatch.setattr(
        "release.get_latest_tag_on_branch", lambda ref=None: "v2.5.0-alpha.4"
    )
    monkeypatch.setattr(
        "release.get_commits_since_last_tag",
        lambda tag: [
            {
                "type": "feat",
                "scope": "lootball",
                "breaking": False,
                "is_mod": True,
                "description": "milestone final feature",
                "files": ["common/src/main/Item.java"],
            }
        ],
    )

    result = runner.invoke(
        app,
        [
            "--branch",
            "main",
            "--detect-pr-channel",
            "--repo",
            "ResistorCat/cobbleloots",
            "--commit",
            "deadbeef",
            "--dry-run",
        ],
    )
    assert result.exit_code == 0
    content = out_file.read_text(encoding="utf-8")
    assert "has_release=true" in content
    assert "mod_version=2.5.0-beta.1" in content
    assert "mod_version_type=beta" in content
    assert "tag=v2.5.0-beta.1" in content


def test_get_latest_unreleased_beta_tag():
    assert get_latest_unreleased_beta_tag(["v2.3.0", "v2.5.0-alpha.4"], "2.3.0") is None
    assert (
        get_latest_unreleased_beta_tag(["v2.3.0", "v2.5.0-beta.1", "v2.5.0"], "2.5.0")
        is None
    )
    assert (
        get_latest_unreleased_beta_tag(
            ["v2.3.0", "v2.5.0-beta.1", "v2.5.0-beta.2"], "2.3.0"
        )
        == "v2.5.0-beta.2"
    )
    assert (
        get_latest_unreleased_beta_tag(
            ["v2.3.0", "v2.4.0-beta.1", "v2.4.0", "v2.5.0-beta.1"], "2.4.0"
        )
        == "v2.5.0-beta.1"
    )


def test_evaluate_beta_promotion_seven_day_rule():
    now = datetime(2026, 12, 10, 12, 0, 0, tzinfo=timezone.utc)
    tags = ["v2.3.0", "v2.5.0-alpha.4", "v2.5.0-beta.1"]

    # 1. No unreleased beta tag
    res_no_beta = evaluate_beta_promotion(
        tags=["v2.3.0", "v2.5.0-alpha.4"],
        base_version="2.3.0",
        beta_timestamp=None,
        commits_since_beta=[],
        now=now,
    )
    assert res_no_beta["should_promote"] is False
    assert res_no_beta["reason"] == "no_unreleased_beta"

    # 2. Less than 7 days elapsed (6 days 23 hours) -> waiting
    res_too_soon = evaluate_beta_promotion(
        tags=tags,
        base_version="2.3.0",
        beta_timestamp=now - timedelta(days=6, hours=23),
        commits_since_beta=[],
        now=now,
    )
    assert res_too_soon["should_promote"] is False
    assert res_too_soon["reason"] == "waiting_soak_period"
    assert res_too_soon["beta_tag"] == "v2.5.0-beta.1"
    assert res_too_soon["target_tag"] == "v2.5.0"

    # 3. Exactly 7 days elapsed with no commits -> ready to promote
    res_ready = evaluate_beta_promotion(
        tags=tags,
        base_version="2.3.0",
        beta_timestamp=now - timedelta(days=7),
        commits_since_beta=[],
        now=now,
    )
    assert res_ready["should_promote"] is True
    assert res_ready["reason"] == "ready"
    assert res_ready["beta_tag"] == "v2.5.0-beta.1"
    assert res_ready["target_tag"] == "v2.5.0"

    # 4. >= 7 days with only non-mod commits (docs/build/fix(build)) or next-cycle feat -> still ready
    res_ready_non_fix = evaluate_beta_promotion(
        tags=tags,
        base_version="2.3.0",
        beta_timestamp=now - timedelta(days=8),
        commits_since_beta=[
            {"type": "docs", "is_mod": False},
            {"type": "fix", "scope": "build", "is_mod": False},
            {"type": "feat", "scope": "lootball", "is_mod": True},
        ],
        now=now,
    )
    assert res_ready_non_fix["should_promote"] is True
    assert res_ready_non_fix["reason"] == "ready"

    # 5. >= 7 days BUT a mod `fix` landed after the beta -> blocked with commit details
    res_blocked = evaluate_beta_promotion(
        tags=tags,
        base_version="2.3.0",
        beta_timestamp=now - timedelta(days=8),
        commits_since_beta=[
            {
                "hash": "abc1234567890",
                "subject": "fix(lootball): prevent crash on open",
                "type": "fix",
                "scope": "lootball",
                "is_mod": True,
            },
        ],
        now=now,
    )
    assert res_blocked["should_promote"] is False
    assert res_blocked["reason"] == "mod_fixes_since_beta"
    assert res_blocked["blocking_commits"] == [
        {
            "hash": "abc1234567890",
            "subject": "fix(lootball): prevent crash on open",
        }
    ]

    # 6. Once beta.2 is tagged for those fixes, timer resets from beta.2 timestamp
    res_reset = evaluate_beta_promotion(
        tags=tags + ["v2.5.0-beta.2"],
        base_version="2.3.0",
        beta_timestamp=now - timedelta(days=2),
        commits_since_beta=[],
        now=now,
    )
    assert res_reset["should_promote"] is False
    assert res_reset["reason"] == "waiting_soak_period"
    assert res_reset["beta_tag"] == "v2.5.0-beta.2"


def test_cli_check_stable_promotion_outputs(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    old_ts = datetime.now(timezone.utc) - timedelta(days=8)
    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0", "v2.5.0-beta.1"])
    monkeypatch.setattr("release.get_tag_timestamp", lambda tag: old_ts)
    monkeypatch.setattr("release.get_tag_commit_sha", lambda tag: "1122334455")
    monkeypatch.setattr("release.get_commits_since_last_tag", lambda tag: [])

    result = runner.invoke(app, ["--branch", "main", "--check-stable-promotion"])
    assert result.exit_code == 0
    content = out_file.read_text(encoding="utf-8")
    assert "should_promote=true" in content
    assert "reason=ready" in content
    assert "beta_tag=v2.5.0-beta.1" in content
    assert "beta_commit=1122334455" in content
    assert "target_tag=v2.5.0" in content


def test_cli_check_stable_promotion_logs_blocking_commits(monkeypatch, tmp_path):
    out_file = tmp_path / "github_output.txt"
    monkeypatch.setenv("GITHUB_OUTPUT", str(out_file))

    old_ts = datetime.now(timezone.utc) - timedelta(days=8)
    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0", "v2.5.0-beta.1"])
    monkeypatch.setattr("release.get_tag_timestamp", lambda tag: old_ts)
    monkeypatch.setattr("release.get_tag_commit_sha", lambda tag: "1122334455")
    monkeypatch.setattr(
        "release.get_commits_since_last_tag",
        lambda tag: [
            {
                "hash": "deadbeef12345678",
                "subject": "fix(fishing): avoid null drop table",
                "type": "fix",
                "scope": "fishing",
                "is_mod": True,
                "description": "avoid null drop table",
            },
            {
                "hash": "cafebabe87654321",
                "subject": "docs(readme): clarify install",
                "type": "docs",
                "scope": "readme",
                "is_mod": False,
                "description": "clarify install",
            },
        ],
    )

    result = runner.invoke(app, ["--branch", "main", "--check-stable-promotion"])
    assert result.exit_code == 0
    assert "reason=mod_fixes_since_beta" in result.output
    assert "deadbeef12345678" in result.output
    assert "fix(fishing): avoid null drop table" in result.output
    assert "cafebabe87654321" not in result.output
    assert "release:beta" in result.output
    assert "channel=beta" in result.output
    content = out_file.read_text(encoding="utf-8")
    assert "should_promote=false" in content
    assert "reason=mod_fixes_since_beta" in content


def test_get_changelog_fragments(tmp_path):
    changelog_dir = tmp_path / ".changelog"
    changelog_dir.mkdir()

    (changelog_dir / "README.md").write_text("# Readme", encoding="utf-8")
    (changelog_dir / ".gitkeep").write_text("", encoding="utf-8")
    (changelog_dir / "empty.md").write_text("   \n  ", encoding="utf-8")

    (changelog_dir / "DEV-1.md").write_text(
        "### Changes\n- Feature 1", encoding="utf-8"
    )
    (changelog_dir / "DEV-2.md").write_text(
        "### Bug Fixes\n- Fix 1", encoding="utf-8"
    )

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

    formatted = format_changelog_from_fragments(
        "2.4.0", [fragment_1, fragment_2, fragment_3]
    )

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

    monkeypatch.setattr("release.get_git_tags", lambda: ["v2.3.0"])
    monkeypatch.setattr("release.get_latest_tag_on_branch", lambda ref=None: "v2.3.0")
    monkeypatch.setattr(
        "release.get_commits_since_last_tag",
        lambda tag: [
            {
                "type": "feat",
                "scope": "lootball",
                "breaking": False,
                "is_mod": True,
                "description": "technical commit",
                "files": ["common/Item.java"],
            }
        ],
    )

    changelog_dir = tmp_path / ".changelog"
    changelog_dir.mkdir()
    frag_file = changelog_dir / "DEV-5.md"
    frag_file.write_text(
        "### Gameplay Changes\n- **Player Feature**: High-level explanation for players.",
        encoding="utf-8",
    )

    monkeypatch.setattr("release.ROOT_PATH", tmp_path)
    monkeypatch.setattr(
        "release.get_changelog_fragments",
        lambda: [(frag_file, frag_file.read_text(encoding="utf-8"))],
    )

    props = tmp_path / "gradle.properties"
    props.write_text("mod_version=2.3.0\nmod_version_type=release\n", encoding="utf-8")
    cl_file = tmp_path / "CHANGELOG.md"
    cl_file.write_text("# CHANGELOG\n\n## 2.3.0\n- Old", encoding="utf-8")

    result = runner.invoke(app, ["--branch", "main", "--channel", "release"])
    assert result.exit_code == 0
    assert "Release prepared: v2.4.0" in result.output

    notes_file = tmp_path / ".release_notes.md"
    assert notes_file.exists()
    notes_content = notes_file.read_text(encoding="utf-8")
    assert "### Gameplay Changes" in notes_content
    assert "**Player Feature**: High-level explanation for players." in notes_content
    assert "technical commit" not in notes_content

    assert not frag_file.exists()
