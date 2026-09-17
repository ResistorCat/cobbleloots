import os
import pytest
from pathlib import Path
import sys

# Add scripts directory to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from config import load_env
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


def test_constants_definitions():
    import constants
    assert constants.LOADER_NAMES == {"fabric": "Fabric", "neoforge": "NeoForge"}
    assert constants.CURSEFORGE_API_URL == "https://minecraft.curseforge.com/api"
    assert constants.MODRINTH_API_URL == "https://api.modrinth.com/v2"
    assert "1.21.1" in constants.CURSEFORGE_VERSION_IDS
    assert "release" in constants.FOOTERS
    assert constants.MODRINTH_COBBLEMON_PROJECT_ID == "MdwFAVRL"
    assert constants.CURSEFORGE_COBBLEMON_PROJECT_ID == 687131
