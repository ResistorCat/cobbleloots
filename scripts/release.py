"""
Semantic Versioning & Changelog Engine for Cobbleloots.
Analyzes git commits, calculates SemVer bumps, manages multi-channel tags,
updates gradle.properties and CHANGELOG.md, and produces GitHub Actions outputs.
"""

import os
from pathlib import Path
import re
import subprocess
import sys
from typing import Optional
import typer

# Ensure scripts directory is on sys.path
SCRIPTS_DIR = Path(__file__).resolve().parent
ROOT_PATH = SCRIPTS_DIR.parent

if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

try:
    from config import load_mod_properties
except ImportError:
    load_mod_properties = None

app = typer.Typer(help="Semantic Versioning & Changelog Engine for Cobbleloots.")

MOD_PATH_PREFIXES = ("common/", "fabric/", "neoforge/")
MOD_EXACT_FILES = {"build.gradle", "settings.gradle", "gradle.properties"}
TOOLING_SCOPES = {
    "mcp",
    "relay",
    "agents",
    "factory",
    "harness",
    "templates",
    "ci",
    "github",
    "scripts",
    "release",
    "docs",
}


def parse_conventional_commit(message: str) -> dict | None:
    """
    Parses a conventional commit message.
    Returns a dict with keys: type, scope, breaking, description.
    Returns None if the message does not conform to conventional commits.
    """
    if not message:
        return None

    lines = [line.strip() for line in message.strip().splitlines() if line.strip()]
    if not lines:
        return None

    first_line = lines[0]
    match = re.match(
        r"^(?:\[[\w-]+\]\s*)?([a-zA-Z]+)(?:\(([^)]+)\))?(!)?:\s*(.+)$", first_line
    )
    if not match:
        return None

    ctype = match.group(1).lower()
    scope = match.group(2)
    has_exclamation = bool(match.group(3))
    description = match.group(4).strip()

    is_breaking = has_exclamation or bool(
        re.search(r"BREAKING[ -]CHANGE:\s*", message, re.IGNORECASE)
    )

    return {
        "type": ctype,
        "scope": scope,
        "breaking": is_breaking,
        "description": description,
    }


def is_mod_commit(files: list[str]) -> bool:
    """
    Determines whether a commit touched mod paths rather than tooling/repo paths.
    """
    if not files:
        return False

    for f in files:
        normalized = f.replace("\\", "/").strip().lstrip("./")
        if normalized.startswith(MOD_PATH_PREFIXES):
            return True
        if normalized in MOD_EXACT_FILES:
            return True

    return False


def get_git_tags() -> list[str]:
    """
    Retrieves all git tags in the repository.
    """
    try:
        res = subprocess.run(
            ["git", "tag", "-l"],
            cwd=ROOT_PATH,
            capture_output=True,
            text=True,
            check=False,
        )
        return [t.strip() for t in res.stdout.splitlines() if t.strip()]
    except Exception:
        return []


def _parse_semver(tag: str):
    """
    Parses a version tag into (major, minor, patch, is_stable, prerelease_num, raw_tag).
    """
    m = re.match(r"^v?(\d+)\.(\d+)\.(\d+)(?:-([a-zA-Z]+)\.(\d+))?$", tag)
    if not m:
        return None
    major = int(m.group(1))
    minor = int(m.group(2))
    patch = int(m.group(3))
    prerelease_type = m.group(4)
    prerelease_num = int(m.group(5)) if m.group(5) else 0
    is_stable = prerelease_type is None
    return (major, minor, patch, 1 if is_stable else 0, prerelease_num, tag)


def get_base_version_and_tag(
    tags: list[str], default_version: str
) -> tuple[str, str | None]:
    """
    Resolves the baseline version and its corresponding git tag.
    Prefers the latest stable tag; falls back to prerelease or default_version.
    """
    parsed_tags = [_parse_semver(t) for t in tags]
    valid_tags = [p for p in parsed_tags if p is not None]

    if not valid_tags:
        clean_default = default_version.lstrip("vba").split("-")[0]
        return clean_default, None

    stable_tags = [p for p in valid_tags if p[3] == 1]
    if stable_tags:
        highest = max(stable_tags, key=lambda x: (x[0], x[1], x[2]))
        return f"{highest[0]}.{highest[1]}.{highest[2]}", highest[5]

    highest = max(valid_tags, key=lambda x: (x[0], x[1], x[2], x[3], x[4]))
    return f"{highest[0]}.{highest[1]}.{highest[2]}", highest[5]


def _find_baseline_commit() -> str | None:
    """
    When no git tags exist, attempts to locate the commit of the baseline version
    from gradle.properties or PR merge commit.
    """
    try:
        props_version = "2.3.0"
        props_file = ROOT_PATH / "gradle.properties"
        if props_file.exists():
            for line in props_file.read_text(encoding="utf-8").splitlines():
                if line.startswith("mod_version="):
                    props_version = line.split("=", 1)[1].strip()
                    break

        # 1. Search for merge commit containing version
        res = subprocess.run(
            ["git", "log", "-n", "1", f"--grep={props_version}", "--merges", "--format=%H"],
            cwd=ROOT_PATH,
            capture_output=True,
            text=True,
            check=False,
        )
        commit = res.stdout.strip()
        if commit:
            return commit

        # 2. Search for commit setting mod_version in gradle.properties
        res = subprocess.run(
            [
                "git",
                "log",
                "-n",
                "1",
                f"-Smod_version={props_version}",
                "--format=%H",
                "--",
                "gradle.properties",
            ],
            cwd=ROOT_PATH,
            capture_output=True,
            text=True,
            check=False,
        )
        commit = res.stdout.strip()
        if commit:
            return commit
    except Exception:
        pass
    return None


def get_latest_tag_on_branch() -> str | None:
    """
    Returns the most recent tag reachable from HEAD on the current branch.
    """
    try:
        res = subprocess.run(
            ["git", "describe", "--tags", "--abbrev=0"],
            cwd=ROOT_PATH,
            capture_output=True,
            text=True,
            check=False,
        )
        tag = res.stdout.strip()
        if tag and res.returncode == 0:
            return tag
    except Exception:
        pass
    return None


def get_commits_since_last_tag(base_tag: str | None) -> list[dict]:
    """
    Extracts and parses all commits since the specified base tag or baseline commit.
    """
    if base_tag:
        git_range = f"{base_tag}..HEAD"
    else:
        latest_tag = get_latest_tag_on_branch()
        if latest_tag:
            git_range = f"{latest_tag}..HEAD"
        else:
            baseline = _find_baseline_commit()
            git_range = f"{baseline}..HEAD" if baseline else "HEAD"

    try:
        res = subprocess.run(
            ["git", "log", git_range, "--format=%x1e%H%x1f%s%x1f%b%x1f", "--name-only"],
            cwd=ROOT_PATH,
            capture_output=True,
            text=True,
            check=False,
        )
        output = res.stdout
    except Exception:
        return []

    commits: list[dict] = []
    blocks = output.split("\x1e")

    for block in blocks:
        if not block.strip():
            continue
        parts = block.split("\x1f")
        if len(parts) < 4:
            continue

        commit_hash = parts[0].strip()
        subject = parts[1].strip()
        body = parts[2].strip()
        file_lines = parts[3].strip().splitlines()
        files = [f.strip() for f in file_lines if f.strip()]

        message = f"{subject}\n\n{body}".strip()
        parsed = parse_conventional_commit(message)

        if parsed:
            ctype = parsed["type"]
            scope = parsed["scope"]
            breaking = parsed["breaking"]
            description = parsed["description"]
        else:
            ctype = "other"
            scope = None
            breaking = False
            description = subject

        # Check hybrid path and scope filtering
        if scope and scope.lower() in TOOLING_SCOPES:
            is_mod = False
        else:
            is_mod = is_mod_commit(files)

        commits.append(
            {
                "hash": commit_hash,
                "type": ctype,
                "scope": scope,
                "breaking": breaking,
                "description": description,
                "is_mod": is_mod,
                "files": files,
            }
        )

    return commits


def calculate_version_bump(commits: list[dict]) -> str | None:
    """
    Calculates the SemVer bump ('major', 'minor', 'patch', or None) based on mod commits.
    """
    has_major = False
    has_minor = False
    has_patch = False

    for c in commits:
        if not c.get("is_mod", True):
            continue
        if c.get("breaking"):
            has_major = True
        elif c.get("type") == "feat":
            has_minor = True
        elif c.get("type") == "fix":
            has_patch = True

    if has_major:
        return "major"
    if has_minor:
        return "minor"
    if has_patch:
        return "patch"
    return None


def determine_next_version(
    base_version: str, bump: str, branch: str, existing_tags: list[str]
) -> tuple[str, str, str]:
    """
    Calculates the next version string, channel name, and git tag.
    Returns: (version, channel, tag)
    """
    core_version = base_version.lstrip("vba").split("-")[0]
    base_major, base_minor, base_patch = map(int, core_version.split("."))

    branch_lower = branch.lower().strip()
    if branch_lower in ("main", "master", "release"):
        channel = "release"
    elif "beta" in branch_lower:
        channel = "beta"
    elif "alpha" in branch_lower:
        channel = "alpha"
    else:
        channel = "release"

    base_tuple = (base_major, base_minor, base_patch)

    parsed_existing = [_parse_semver(t) for t in existing_tags]
    stable_cores = {
        (p[0], p[1], p[2])
        for p in parsed_existing
        if p is not None and p[3] == 1
    }

    if channel == "release":
        # Check if we are graduating an ongoing, unreleased pre-release cycle
        unreleased_pres = []
        pattern = re.compile(r"^v?(\d+)\.(\d+)\.(\d+)-(?:alpha|beta)\.(\d+)$")
        for t in existing_tags:
            m = pattern.match(t.strip())
            if m:
                pre_core = (int(m.group(1)), int(m.group(2)), int(m.group(3)))
                if pre_core not in stable_cores and pre_core >= base_tuple:
                    unreleased_pres.append((pre_core[0], pre_core[1], pre_core[2], t.strip()))
        if unreleased_pres:
            latest_unreleased = max(unreleased_pres, key=lambda x: (x[0], x[1], x[2]))
            target_core = f"{latest_unreleased[0]}.{latest_unreleased[1]}.{latest_unreleased[2]}"
            return target_core, channel, f"v{target_core}"

    # Inspect existing pre-release tags for the active channel that belong to an ongoing cycle
    prerelease_tags = []
    if channel in ("beta", "alpha"):
        pattern = re.compile(rf"^v?(\d+)\.(\d+)\.(\d+)-{channel}\.(\d+)$")
        for t in existing_tags:
            m = pattern.match(t.strip())
            if m:
                pre_major, pre_minor, pre_patch, pre_count = (
                    int(m.group(1)),
                    int(m.group(2)),
                    int(m.group(3)),
                    int(m.group(4)),
                )
                pre_core = (pre_major, pre_minor, pre_patch)
                # An ongoing cycle must not have graduated to stable yet, and must be >= base_tuple
                if pre_core not in stable_cores and pre_core >= base_tuple:
                    prerelease_tags.append(
                        (pre_major, pre_minor, pre_patch, pre_count, t.strip())
                    )

    if prerelease_tags:
        latest_pre = max(prerelease_tags, key=lambda x: (x[0], x[1], x[2], x[3]))
        pre_core = (latest_pre[0], latest_pre[1], latest_pre[2])

        if bump == "major":
            major = max(base_major, pre_core[0]) + 1
            target_core = f"{major}.0.0"
            next_count = 1
        else:
            # For patch or minor, retain the core version of the current pre-release cycle
            target_core = f"{pre_core[0]}.{pre_core[1]}.{pre_core[2]}"
            counts = [x[3] for x in prerelease_tags if (x[0], x[1], x[2]) == pre_core]
            next_count = max(counts) + 1 if counts else 1

        version = f"{target_core}-{channel}.{next_count}"
        tag = f"v{version}"
    else:
        # Check if we are promoting an unreleased alpha cycle to beta
        if channel == "beta":
            unreleased_alphas = []
            pattern_alpha = re.compile(r"^v?(\d+)\.(\d+)\.(\d+)-alpha\.(\d+)$")
            for t in existing_tags:
                m = pattern_alpha.match(t.strip())
                if m:
                    alpha_core = (int(m.group(1)), int(m.group(2)), int(m.group(3)))
                    if alpha_core not in stable_cores and alpha_core >= base_tuple:
                        unreleased_alphas.append((alpha_core[0], alpha_core[1], alpha_core[2]))
            if unreleased_alphas:
                latest_alpha_core = max(unreleased_alphas, key=lambda x: (x[0], x[1], x[2]))
                target_core = f"{latest_alpha_core[0]}.{latest_alpha_core[1]}.{latest_alpha_core[2]}"
                version = f"{target_core}-beta.1"
                tag = f"v{version}"
                return version, channel, tag

        major = base_major
        minor = base_minor
        patch = base_patch

        if bump == "major":
            major += 1
            minor = 0
            patch = 0
        elif bump == "minor":
            minor += 1
            patch = 0
        elif bump == "patch":
            patch += 1

        target_core = f"{major}.{minor}.{patch}"

        if channel == "release":
            version = target_core
            tag = f"v{version}"
        else:
            version = f"{target_core}-{channel}.1"
            tag = f"v{version}"

    return version, channel, tag


def format_changelog_section(version: str, commits: list[dict]) -> str:
    """
    Formats the changelog Markdown section for the new release.
    """
    features: list[str] = []
    fixes: list[str] = []
    technical: list[str] = []

    for c in commits:
        if not c.get("is_mod", True):
            continue

        scope = c.get("scope")
        desc = c.get("description", "")
        entry = f"- **{scope}**: {desc}" if scope else f"- {desc}"

        ctype = c.get("type", "").lower()
        if ctype == "feat":
            features.append(entry)
        elif ctype == "fix":
            fixes.append(entry)
        else:
            technical.append(entry)

    sections: list[str] = [f"## {version}"]

    if features:
        sections.append("### Features\n" + "\n".join(features))
    if fixes:
        sections.append("### Bug Fixes\n" + "\n".join(fixes))
    if technical:
        sections.append("### Technical Changes\n" + "\n".join(technical))

    return "\n\n".join(sections) + "\n"


def get_changelog_fragments(
    changelog_dir: Path | None = None,
) -> list[tuple[Path, str]]:
    """
    Finds and reads all fragment markdown files in .changelog/,
    excluding README.md and hidden files.
    Returns a list of (path, content).
    """
    target_dir = changelog_dir or (ROOT_PATH / ".changelog")
    if not target_dir.exists():
        return []

    fragments = []
    for f in sorted(target_dir.glob("*.md")):
        if f.name.lower() == "readme.md" or f.name.startswith("."):
            continue
        content = f.read_text(encoding="utf-8").strip()
        if content:
            fragments.append((f, content))
    return fragments


def format_changelog_from_fragments(version: str, fragment_contents: list[str]) -> str:
    """
    Merges fragment contents into a single release section under ## {version}.
    Groups sections by '### <Header>' across fragments while preserving preferred order.
    """
    preferred_order = [
        "Gameplay Changes",
        "Changes",
        "Technical Changes",
        "Bug Fixes",
    ]
    sections: dict[str, list[str]] = {}

    for content in fragment_contents:
        lines = content.splitlines()
        current_header = "Changes"
        current_lines: list[str] = []

        for line in lines:
            if line.startswith("### "):
                if current_lines:
                    text = "\n".join(current_lines).strip()
                    if text:
                        sections.setdefault(current_header, []).append(text)
                    current_lines = []
                current_header = line.replace("### ", "").strip()
            else:
                current_lines.append(line)

        if current_lines:
            text = "\n".join(current_lines).strip()
            if text:
                sections.setdefault(current_header, []).append(text)

    result: list[str] = [f"## {version}"]

    seen_headers = set()
    for header in preferred_order:
        if header in sections:
            seen_headers.add(header)
            joined_content = "\n".join(sections[header]).strip()
            result.append(f"### {header}\n\n{joined_content}")

    for header, contents in sections.items():
        if header not in seen_headers:
            joined_content = "\n".join(contents).strip()
            result.append(f"### {header}\n\n{joined_content}")

    return "\n\n".join(result) + "\n"


def consume_changelog_fragments(fragment_paths: list[Path]) -> None:
    """
    Deletes the processed fragment files.
    """
    for p in fragment_paths:
        try:
            if p.exists():
                p.unlink()
        except Exception as e:
            print(f"Warning: could not delete fragment {p}: {e}")


def update_gradle_properties(
    new_version: str, new_channel: str, path: Path | None = None
) -> None:
    """
    Updates mod_version and mod_version_type in gradle.properties.
    """
    target = path or (ROOT_PATH / "gradle.properties")
    content = target.read_text(encoding="utf-8")
    content = re.sub(
        r"^mod_version=.*$", f"mod_version={new_version}", content, flags=re.MULTILINE
    )
    content = re.sub(
        r"^mod_version_type=.*$",
        f"mod_version_type={new_channel}",
        content,
        flags=re.MULTILINE,
    )
    target.write_text(content, encoding="utf-8")


def prepend_changelog(section: str, path: Path | None = None) -> None:
    """
    Prepends the new release section to CHANGELOG.md before the first release entry.
    """
    target = path or (ROOT_PATH / "CHANGELOG.md")
    if not target.exists():
        target.write_text(f"# CHANGELOG\n\n{section.strip()}\n", encoding="utf-8")
        return

    content = target.read_text(encoding="utf-8")
    match = re.search(r"^##\s+", content, flags=re.MULTILINE)
    if match:
        header = content[: match.start()]
        rest = content[match.start() :]
        updated = f"{header}{section.strip()}\n\n{rest}"
    else:
        updated = f"{content.rstrip()}\n\n{section.strip()}\n"

    target.write_text(updated, encoding="utf-8")


def write_release_notes(section: str, path: Path | None = None) -> Path:
    """
    Writes the isolated release notes to .release_notes.md.
    """
    target = path or (ROOT_PATH / ".release_notes.md")
    target.write_text(section.strip() + "\n", encoding="utf-8")
    return target


def write_github_output(outputs: dict[str, str]) -> None:
    """
    Appends key-value pairs to $GITHUB_OUTPUT if the environment variable is present.
    """
    output_file = os.getenv("GITHUB_OUTPUT")
    if not output_file:
        return

    target = Path(output_file)
    with target.open("a", encoding="utf-8") as f:
        for k, v in outputs.items():
            f.write(f"{k}={v}\n")


def get_current_branch(branch_opt: str | None = None) -> str:
    """
    Resolves the active branch name from option, environment, or git.
    """
    if branch_opt:
        return branch_opt.strip()
    if os.getenv("GITHUB_REF_NAME"):
        return os.environ["GITHUB_REF_NAME"].strip()
    try:
        res = subprocess.run(
            ["git", "branch", "--show-current"],
            cwd=ROOT_PATH,
            capture_output=True,
            text=True,
            check=False,
        )
        current = res.stdout.strip()
        if current:
            return current
    except Exception:
        pass
    return "main"


@app.command()
def main(
    branch: Optional[str] = typer.Option(
        None, "--branch", "-b", help="Target branch for release (main, beta, alpha)."
    ),
    dry_run: bool = typer.Option(
        False, "--dry-run", help="Dry-run mode without modifying files."
    ),
    check: bool = typer.Option(
        False, "--check", help="Check release requirement without modifying files."
    ),
) -> None:
    """
    Main entrypoint for Semantic Versioning analysis and changelog generation.
    """
    is_dry_run = dry_run or check
    active_branch = get_current_branch(branch)

    # Read base defaults from gradle.properties
    base_default = "2.3.0"
    minecraft_version = "1.21.1"
    if load_mod_properties is not None:
        try:
            props = load_mod_properties()
            base_default = props.mod_version
            minecraft_version = props.minecraft_version
        except Exception:
            pass

    tags = get_git_tags()
    latest_branch_tag = get_latest_tag_on_branch()
    base_version, base_tag = get_base_version_and_tag(tags, base_default)

    branch_lower = active_branch.lower().strip()
    is_stable_channel = branch_lower in ("main", "master", "release")

    # On stable branches (main), anchor to the latest stable tag (base_tag)
    # to evaluate all commits in the cycle and allow graduation.
    # On pre-release branches (beta/alpha), anchor to the latest reachable tag on the branch
    # to avoid re-release loops on non-mod commits.
    if is_stable_channel:
        anchor_tag = base_tag
    else:
        anchor_tag = latest_branch_tag or base_tag

    commits = get_commits_since_last_tag(anchor_tag)
    bump = calculate_version_bump(commits)

    if bump is None:
        print("No mod changes detected. Release not required.")
        write_github_output({"has_release": "false"})
        raise typer.Exit(code=0)

    new_version, new_channel, new_tag = determine_next_version(
        base_version, bump, active_branch, tags
    )

    fragments = get_changelog_fragments()
    if fragments:
        changelog_section = format_changelog_from_fragments(
            new_version, [content for _, content in fragments]
        )
    else:
        changelog_section = format_changelog_section(new_version, commits)

    if not is_dry_run:
        write_release_notes(changelog_section)
        update_gradle_properties(new_version, new_channel)
        prepend_changelog(changelog_section)
        if fragments:
            consume_changelog_fragments([path for path, _ in fragments])
        print(
            f"Release prepared: {new_tag} ({new_channel}) - updated gradle.properties and CHANGELOG.md"
        )
    else:
        print(
            f"[DRY-RUN] Release calculated: {new_tag} ({new_channel}) - no files modified."
        )

    is_prerelease_str = "true" if new_channel in ("beta", "alpha") else "false"
    write_github_output(
        {
            "has_release": "true",
            "mod_version": new_version,
            "mod_version_type": new_channel,
            "tag": new_tag,
            "is_prerelease": is_prerelease_str,
            "minecraft_version": minecraft_version,
            "notes_file": ".release_notes.md",
        }
    )
    raise typer.Exit(code=0)


if __name__ == "__main__":
    app()
