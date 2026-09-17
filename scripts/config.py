"""
Common configuration settings for the scripts.
"""

import os
from pathlib import Path
from constants import CURSEFORGE_VERSION_IDS, FOOTERS
from models import ModProperties

ROOT_PATH = Path(__file__).parent.parent
ENV_PATH = ROOT_PATH / ".env"


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


def load_mod_properties() -> ModProperties:
    """
    Load mod properties from the gradle.properties file.
    """
    properties_path = ROOT_PATH / "gradle.properties"
    if not properties_path.exists():
        raise FileNotFoundError(f"{properties_path} does not exist.")

    mod_properties = {}
    with properties_path.open() as f:
        for line in f:
            if line.strip() and not line.startswith("#"):
                key, value = line.strip().split("=", 1)
                mod_properties[key] = value

    return ModProperties(**mod_properties)


def load_changelog(mod_version_type: str) -> str:
    """
    Load the latest changelog from the CHANGELOG.md file.
    """
    changelog_path = ROOT_PATH / "CHANGELOG.md"
    if not changelog_path.exists():
        raise FileNotFoundError(f"{changelog_path} does not exist.")

    contents = []
    with changelog_path.open() as f:
        recording = False
        for line in f:
            if line.startswith("## "):
                if recording:
                    break
                recording = True
                continue
            if recording:
                contents.append(line)
    contents.append(f"\n{FOOTERS.get(mod_version_type, '')}\n")
    return "".join(contents).strip()


def load_modinfo() -> str:
    """
    Load the MODINFO.md file content.
    """
    modinfo_path = ROOT_PATH / "MODINFO.md"
    if not modinfo_path.exists():
        raise FileNotFoundError(f"{modinfo_path} does not exist.")

    with modinfo_path.open() as f:
        return f.read()
