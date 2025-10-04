"""
Common configuration settings for the scripts.
"""

import os
from pathlib import Path
from models import ModProperties

ROOT_PATH = Path(__file__).parent.parent
ENV_PATH = ROOT_PATH / ".env"

CURSEFORGE_VERSION_IDS = {
    "1.21.1": 11779,
    "fabric": 7499,
    "neoforge": 10150,
    "server": 9639,
    "client": 9638,
    "java-21": 11135
}


def load_env() -> None:
    """
    Load environment variables from the .env file.
    """
    if not ENV_PATH.exists():
        raise FileNotFoundError(f"{ENV_PATH} does not exist.")

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


def load_changelog() -> str:
    """
    Load the changelog from the CHANGELOG.md file.
    """
    changelog_path = ROOT_PATH / "CHANGELOG.md"
    if not changelog_path.exists():
        raise FileNotFoundError(f"{changelog_path} does not exist.")

    with changelog_path.open() as f:
        return f.read()


def load_modinfo() -> str:
    """
    Load the MODINFO.md file content.
    """
    modinfo_path = ROOT_PATH / "MODINFO.md"
    if not modinfo_path.exists():
        raise FileNotFoundError(f"{modinfo_path} does not exist.")

    with modinfo_path.open() as f:
        return f.read()
