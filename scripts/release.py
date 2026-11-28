"""
Semantic Versioning & Changelog Engine for Cobbleloots.
Analyzes git commits, calculates SemVer bumps, manages multi-channel tags,
updates gradle.properties and CHANGELOG.md, and produces GitHub Actions outputs.
"""

from datetime import datetime, timedelta, timezone
import os
from pathlib import Path
import re
import subprocess
import sys
from typing import Optional
import requests
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
VALID_CHANNELS = ("alpha", "beta", "release")
DEFAULT_PROMOTION_DAYS = 7
GITHUB_API_URL = "https://api.github.com"

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
    "build",
    "gradle",
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


def _is_tooling_scope(scope: str | None) -> bool:
    """
    Returns True if all comma-separated scope tokens belong to TOOLING_SCOPES.
    """
    if not scope:
        return False
    parts = [part.strip().lower() for part in scope.split(",") if part.strip()]
    return bool(parts) and all(part in TOOLING_SCOPES for part in parts)


def _is_excluded_mod_path(normalized: str) -> bool:
    """
    Returns True for build, Gradle, or test paths that must not trigger mod releases.
    """
    filename = Path(normalized).name
    if filename == "gradle.properties":
        return True
    if normalized.endswith((".gradle", ".gradle.kts")):
        return True
    if normalized.startswith("gradle/") or "/gradle/" in normalized:
        return True
    if normalized.startswith("src/test/") or "/src/test/" in normalized:
        return True
    return False


def is_mod_commit(files: list[str]) -> bool:
    """
    Determines whether a commit touched mod paths rather than tooling, build, or test paths.
    """
    if not files:
        return False

    for f in files:
        normalized = f.replace("\\", "/").strip().lstrip("./")
        if not normalized:
            continue
        if _is_excluded_mod_path(normalized):
            continue
        if normalized.startswith(MOD_PATH_PREFIXES):
            return True

    return False


def normalize_channel(channel: str) -> str:
    """
    Validates and normalizes a release channel name ('alpha', 'beta', or 'release').
    Accepts 'stable' as an alias for 'release'.
    """
    normalized = channel.lower().strip()
    if normalized == "stable":
        return "release"
    if normalized not in VALID_CHANNELS:
        raise ValueError(
            f"Invalid release channel '{channel}'. Expected one of: {', '.join(VALID_CHANNELS)}."
        )
    return normalized


def resolve_channel_from_labels(labels: list[str]) -> str:
    """
    Maps GitHub PR label names to a release channel.
    Priority: release:stable -> release, release:beta -> beta, default -> alpha.
    """
    normalized = {label.strip().lower() for label in labels if label and label.strip()}
    if "release:stable" in normalized:
        return "release"
    if "release:beta" in normalized:
        return "beta"
    return "alpha"


def fetch_pr_labels_for_commit(
    repo: str,
    commit_sha: str,
    token: str | None = None,
    api_url: str = GITHUB_API_URL,
) -> list[str]:
    """
    Queries the GitHub REST API for Pull Requests associated with a commit SHA
    and returns the list of label names on the merged PR(s).
    """
    if not repo or not commit_sha:
        return []

    url = f"{api_url.rstrip('/')}/repos/{repo}/commits/{commit_sha}/pulls"
    headers = {
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "cobbleloots-release",
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"

    try:
        response = requests.get(url, headers=headers, timeout=15)
    except Exception as e:
        print(f"Warning: failed to query GitHub PRs for {commit_sha}: {e}")
        return []

    if response.status_code != 200:
        print(
            f"Warning: GitHub API returned {response.status_code} for {commit_sha} PR lookup."
        )
        return []

    prs = response.json()
    if not isinstance(prs, list) or not prs:
        return []

    merged_prs = [pr for pr in prs if isinstance(pr, dict) and pr.get("merged_at")]
    target_prs = merged_prs or [pr for pr in prs if isinstance(pr, dict)]

    labels: list[str] = []
    for pr in target_prs:
        for label in pr.get("labels", []):
            if isinstance(label, dict) and label.get("name"):
                labels.append(str(label["name"]))
            elif isinstance(label, str):
                labels.append(label)
    return labels


def resolve_channel_for_commit(
    repo: str,
    commit_sha: str,
    token: str | None = None,
    api_url: str = GITHUB_API_URL,
) -> str:
    """
    Determines the release channel for a commit from its associated GitHub PR labels.
    """
    labels = fetch_pr_labels_for_commit(repo, commit_sha, token=token, api_url=api_url)
    return resolve_channel_from_labels(labels)


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


def get_latest_tag_on_branch(ref: str | None = None) -> str | None:
    """
    Returns the most recent tag reachable from the specified ref (or HEAD).
    """
    cmd = ["git", "describe", "--tags", "--abbrev=0"]
    if ref:
        try:
            check_ref = subprocess.run(
                ["git", "rev-parse", "--verify", "--quiet", ref],
                cwd=ROOT_PATH,
                capture_output=True,
                text=True,
                check=False,
            )
            if check_ref.returncode == 0:
                cmd.append(ref)
        except Exception:
            pass
    try:
        res = subprocess.run(
            cmd,
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
        if _is_tooling_scope(scope):
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


def get_latest_unreleased_beta_tag(
    tags: list[str], base_version: str
) -> str | None:
    """
    Returns the highest vX.Y.Z-beta.N tag belonging to an active cycle that has
    not yet graduated to a stable vX.Y.Z tag.
    """
    core_version = base_version.lstrip("vba").split("-")[0]
    base_tuple = tuple(map(int, core_version.split(".")))

    parsed_existing = [_parse_semver(t) for t in tags]
    stable_cores = {
        (p[0], p[1], p[2])
        for p in parsed_existing
        if p is not None and p[3] == 1
    }

    beta_tags = []
    pattern = re.compile(r"^v?(\d+)\.(\d+)\.(\d+)-beta\.(\d+)$")
    for t in tags:
        m = pattern.match(t.strip())
        if not m:
            continue
        pre_major, pre_minor, pre_patch, pre_count = (
            int(m.group(1)),
            int(m.group(2)),
            int(m.group(3)),
            int(m.group(4)),
        )
        pre_core = (pre_major, pre_minor, pre_patch)
        if pre_core not in stable_cores and pre_core >= base_tuple:
            beta_tags.append((pre_major, pre_minor, pre_patch, pre_count, t.strip()))

    if not beta_tags:
        return None

    latest = max(beta_tags, key=lambda x: (x[0], x[1], x[2], x[3]))
    return latest[4]


def get_tag_timestamp(tag: str) -> datetime | None:
    """
    Returns the UTC timestamp of a git tag (tagger date for annotated tags,
    falling back to commit date). Uses local git metadata only.
    """
    try:
        res = subprocess.run(
            ["git", "for-each-ref", "--format=%(creatordate:unix)", f"refs/tags/{tag}"],
            cwd=ROOT_PATH,
            capture_output=True,
            text=True,
            check=False,
        )
        raw = res.stdout.strip()
        if not raw:
            res = subprocess.run(
                ["git", "log", "-1", "--format=%ct", f"{tag}^{{commit}}"],
                cwd=ROOT_PATH,
                capture_output=True,
                text=True,
                check=False,
            )
            raw = res.stdout.strip()
        if raw:
            return datetime.fromtimestamp(int(raw), tz=timezone.utc)
    except Exception:
        pass
    return None


def get_tag_commit_sha(tag: str) -> str | None:
    """
    Returns the commit SHA referenced by a git tag.
    """
    try:
        res = subprocess.run(
            ["git", "rev-list", "-n", "1", tag],
            cwd=ROOT_PATH,
            capture_output=True,
            text=True,
            check=False,
        )
        sha = res.stdout.strip()
        if sha and res.returncode == 0:
            return sha
    except Exception:
        pass
    return None


def evaluate_beta_promotion(
    tags: list[str],
    base_version: str,
    beta_timestamp: datetime | None,
    commits_since_beta: list[dict],
    now: datetime | None = None,
    min_days: int = DEFAULT_PROMOTION_DAYS,
) -> dict:
    """
    Evaluates whether the latest unreleased beta tag is eligible for automatic
    promotion to stable (at least `min_days` days old with no new mod `fix` commits).
    """
    beta_tag = get_latest_unreleased_beta_tag(tags, base_version)
    if not beta_tag:
        return {
            "should_promote": False,
            "reason": "no_unreleased_beta",
            "beta_tag": None,
            "target_tag": None,
            "days_elapsed": 0.0,
        }

    parsed = _parse_semver(beta_tag)
    target_tag = f"v{parsed[0]}.{parsed[1]}.{parsed[2]}" if parsed else None

    if beta_timestamp is None:
        return {
            "should_promote": False,
            "reason": "missing_beta_timestamp",
            "beta_tag": beta_tag,
            "target_tag": target_tag,
            "days_elapsed": 0.0,
        }

    current_time = now or datetime.now(timezone.utc)
    if current_time.tzinfo is None:
        current_time = current_time.replace(tzinfo=timezone.utc)
    if beta_timestamp.tzinfo is None:
        beta_timestamp = beta_timestamp.replace(tzinfo=timezone.utc)

    elapsed = current_time - beta_timestamp
    days_elapsed = elapsed.total_seconds() / 86400.0

    if elapsed < timedelta(days=min_days):
        return {
            "should_promote": False,
            "reason": "waiting_soak_period",
            "beta_tag": beta_tag,
            "target_tag": target_tag,
            "days_elapsed": days_elapsed,
        }

    has_mod_fixes = any(
        c.get("is_mod", False) and c.get("type") == "fix"
        for c in commits_since_beta
    )
    if has_mod_fixes:
        return {
            "should_promote": False,
            "reason": "mod_fixes_since_beta",
            "beta_tag": beta_tag,
            "target_tag": target_tag,
            "days_elapsed": days_elapsed,
        }

    return {
        "should_promote": True,
        "reason": "ready",
        "beta_tag": beta_tag,
        "target_tag": target_tag,
        "days_elapsed": days_elapsed,
    }


def determine_next_version(
    base_version: str, bump: str, channel: str, existing_tags: list[str]
) -> tuple[str, str, str]:
    """
    Calculates the next version string, channel name, and git tag.
    Returns: (version, channel, tag)
    """
    core_version = base_version.lstrip("vba").split("-")[0]
    base_major, base_minor, base_patch = map(int, core_version.split("."))

    channel = normalize_channel(channel)

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
        None,
        "--branch",
        "-b",
        help="Target branch ref for tag anchoring (does not determine release channel).",
    ),
    channel: Optional[str] = typer.Option(
        None,
        "--channel",
        "-c",
        help="Release channel (alpha, beta, release). Defaults to alpha unless --detect-pr-channel is set.",
    ),
    detect_pr_channel: bool = typer.Option(
        False,
        "--detect-pr-channel",
        help="Resolve release channel from GitHub PR labels (release:beta / release:stable) for the commit.",
    ),
    commit: Optional[str] = typer.Option(
        None,
        "--commit",
        help="Commit SHA for PR label lookup (defaults to $GITHUB_SHA).",
    ),
    repo: Optional[str] = typer.Option(
        None,
        "--repo",
        help="GitHub repository in owner/repo format (defaults to $GITHUB_REPOSITORY).",
    ),
    check_stable_promotion: bool = typer.Option(
        False,
        "--check-stable-promotion",
        help="Check whether the latest unreleased beta is ready for stable promotion (git-only, no secrets).",
    ),
    promotion_days: int = typer.Option(
        DEFAULT_PROMOTION_DAYS,
        "--promotion-days",
        help="Minimum days without mod fix commits before promoting beta to stable.",
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
    base_version, base_tag = get_base_version_and_tag(tags, base_default)

    if check_stable_promotion:
        beta_tag = get_latest_unreleased_beta_tag(tags, base_version)
        beta_timestamp = get_tag_timestamp(beta_tag) if beta_tag else None
        commits_since_beta = (
            get_commits_since_last_tag(beta_tag) if beta_tag else []
        )
        beta_commit = get_tag_commit_sha(beta_tag) if beta_tag else None

        result = evaluate_beta_promotion(
            tags=tags,
            base_version=base_version,
            beta_timestamp=beta_timestamp,
            commits_since_beta=commits_since_beta,
            min_days=promotion_days,
        )
        should_promote_str = "true" if result["should_promote"] else "false"
        print(
            f"Beta promotion check: should_promote={should_promote_str} "
            f"reason={result['reason']} beta_tag={result['beta_tag']} "
            f"target_tag={result['target_tag']} days_elapsed={result['days_elapsed']:.2f}"
        )
        write_github_output(
            {
                "should_promote": should_promote_str,
                "reason": result["reason"],
                "beta_tag": result["beta_tag"] or "",
                "beta_commit": beta_commit or "",
                "target_tag": result["target_tag"] or "",
            }
        )
        raise typer.Exit(code=0)

    # Determine release channel explicitly from --channel or PR labels (never from branch name)
    if channel is not None:
        try:
            resolved_channel = normalize_channel(channel)
        except ValueError as e:
            print(f"Error: {e}")
            raise typer.Exit(code=1)
    elif detect_pr_channel:
        target_repo = repo or os.getenv("GITHUB_REPOSITORY", "")
        target_commit = commit or os.getenv("GITHUB_SHA", "")
        token = os.getenv("GITHUB_TOKEN")
        resolved_channel = resolve_channel_for_commit(
            target_repo, target_commit, token=token
        )
    else:
        resolved_channel = "alpha"

    latest_branch_tag = get_latest_tag_on_branch()

    # Anchor tag selection:
    # - release: anchor to latest stable tag (base_tag) to evaluate the full cycle and allow graduation.
    # - beta: if an active beta cycle exists, anchor to latest reachable tag on the branch to prevent
    #   re-release loops on non-mod commits; when starting beta.1 from alpha, anchor to base_tag.
    # - alpha: anchor to latest reachable tag on the branch to prevent re-release loops.
    if resolved_channel == "release":
        anchor_tag = base_tag
    elif resolved_channel == "beta":
        active_beta_tag = get_latest_unreleased_beta_tag(tags, base_version)
        anchor_tag = (latest_branch_tag or base_tag) if active_beta_tag else base_tag
    else:
        anchor_tag = latest_branch_tag or base_tag

    commits = get_commits_since_last_tag(anchor_tag)
    bump = calculate_version_bump(commits)

    if bump is None:
        print("No mod changes detected. Release not required.")
        write_github_output({"has_release": "false"})
        raise typer.Exit(code=0)

    new_version, new_channel, new_tag = determine_next_version(
        base_version, bump, resolved_channel, tags
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
