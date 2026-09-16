import json
import os
from pathlib import Path
import sys
from unittest.mock import MagicMock
from typer.testing import CliRunner

sys.path.insert(0, str(Path(__file__).parent.parent))
from publish import app, get_artifact_path, should_auto_confirm
from modrinth import upload_to_modrinth
from curseforge import upload_to_curseforge
from models import ModProperties

runner = CliRunner()


def test_build_headless_flag_help():
    result = runner.invoke(app, ["build", "--help"])
    assert result.exit_code == 0
    assert "--yes" in result.output or "-y" in result.output


def test_publish_headless_flag_help():
    result = runner.invoke(app, ["publish", "--help"])
    assert result.exit_code == 0
    assert "--yes" in result.output or "-y" in result.output


def test_artifact_path_resolution():
    props = ModProperties(
        mod_id="cobbleloots",
        mod_version="2.3.0",
        mod_version_type="release",
        mod_name="Cobbleloots",
        mod_description="desc",
        mod_authors="author",
        mod_license="MIT",
        mod_logo="logo.png",
        mod_homepage="https://example.com",
        mod_source="https://example.com",
        mod_issues="https://example.com",
        mod_discord="https://example.com",
        mod_modrinth="https://example.com",
        mod_curseforge="https://example.com",
        maven_group="dev.ripio",
        archives_name="cobbleloots",
        enabled_platforms="fabric,neoforge",
        minecraft_version="1.21.1",
        cobblemon_target_version="1.7.0",
        fabric_loader_target_version="0.17.2",
        neoforge_target_version="21",
        cobblemon_version="1.7.3+1.21.1",
        fabric_loader_version="0.17.2",
        fabric_api_version="0.116.6+1.21.1",
        fabric_kotlin_version="1.13.6",
        neoforge_version="21.1.182",
        neoforge_kotlin_version="5.10.0",
    )
    fabric_path = get_artifact_path(props, "fabric")
    neoforge_path = get_artifact_path(props, "neoforge")

    assert fabric_path.name == "cobbleloots-fabric-1.21.1-2.3.0.jar"
    assert neoforge_path.name == "cobbleloots-neoforge-1.21.1-2.3.0.jar"


def test_should_auto_confirm(monkeypatch):
    # When yes flag is True, should always auto confirm
    assert should_auto_confirm(True) is True

    # When yes is False and no CI env vars, should return False
    monkeypatch.delenv("CI", raising=False)
    monkeypatch.delenv("GITHUB_ACTIONS", raising=False)
    assert should_auto_confirm(False) is False

    # When CI is true
    monkeypatch.setenv("CI", "true")
    assert should_auto_confirm(False) is True

    # When GITHUB_ACTIONS is true
    monkeypatch.delenv("CI", raising=False)
    monkeypatch.setenv("GITHUB_ACTIONS", "true")
    assert should_auto_confirm(False) is True


def test_modrinth_metadata_display_name_and_featured(monkeypatch, tmp_path):
    jar_file = tmp_path / "test.jar"
    jar_file.write_bytes(b"PK")

    monkeypatch.setenv("MODRINTH_TOKEN", "fake-token")
    monkeypatch.setenv("MODRINTH_PROJECT_ID", "fake-id")

    captured_metadata = None

    def mock_post(url, headers=None, files=None):
        nonlocal captured_metadata
        data_json = files["data"][1]
        captured_metadata = json.loads(data_json)
        mock_resp = MagicMock()
        mock_resp.status_code = 200
        return mock_resp

    monkeypatch.setattr("modrinth.requests.post", mock_post)

    props = ModProperties(
        mod_id="cobbleloots",
        mod_version="2.3.0",
        mod_version_type="release",
        mod_name="Cobbleloots",
        mod_description="desc",
        mod_authors="author",
        mod_license="MIT",
        mod_logo="logo.png",
        mod_homepage="https://example.com",
        mod_source="https://example.com",
        mod_issues="https://example.com",
        mod_discord="https://example.com",
        mod_modrinth="https://example.com",
        mod_curseforge="https://example.com",
        maven_group="dev.ripio",
        archives_name="cobbleloots",
        enabled_platforms="fabric,neoforge",
        minecraft_version="1.21.1",
        cobblemon_target_version="1.7.0",
        fabric_loader_target_version="0.17.2",
        neoforge_target_version="21",
        cobblemon_version="1.7.3+1.21.1",
        fabric_loader_version="0.17.2",
        fabric_api_version="0.116.6+1.21.1",
        fabric_kotlin_version="1.13.6",
        neoforge_version="21.1.182",
        neoforge_kotlin_version="5.10.0",
    )

    upload_to_modrinth(jar_file, props, "fabric", "changelog text")
    assert captured_metadata is not None
    assert captured_metadata["name"] == "Cobbleloots v2.3.0 [1.21.1] [Fabric]"
    assert captured_metadata["featured"] is True

    # Check non-release version
    props_beta = ModProperties(**{**props.model_dump(), "mod_version_type": "beta"})
    upload_to_modrinth(jar_file, props_beta, "neoforge", "changelog text")
    assert captured_metadata["name"] == "Cobbleloots v2.3.0 [1.21.1] [Neoforge]"
    assert captured_metadata["featured"] is False


def test_curseforge_metadata_display_name(monkeypatch, tmp_path):
    jar_file = tmp_path / "test.jar"
    jar_file.write_bytes(b"PK")

    monkeypatch.setenv("CURSEFORGE_TOKEN", "fake-token")
    monkeypatch.setenv("CURSEFORGE_PROJECT_ID", "12345")

    captured_metadata = None

    def mock_post(url, headers=None, files=None):
        nonlocal captured_metadata
        data_json = files["metadata"][1]
        captured_metadata = json.loads(data_json)
        mock_resp = MagicMock()
        mock_resp.status_code = 200
        return mock_resp

    monkeypatch.setattr("curseforge.requests.post", mock_post)

    props = ModProperties(
        mod_id="cobbleloots",
        mod_version="2.3.0",
        mod_version_type="release",
        mod_name="Cobbleloots",
        mod_description="desc",
        mod_authors="author",
        mod_license="MIT",
        mod_logo="logo.png",
        mod_homepage="https://example.com",
        mod_source="https://example.com",
        mod_issues="https://example.com",
        mod_discord="https://example.com",
        mod_modrinth="https://example.com",
        mod_curseforge="https://example.com",
        maven_group="dev.ripio",
        archives_name="cobbleloots",
        enabled_platforms="fabric,neoforge",
        minecraft_version="1.21.1",
        cobblemon_target_version="1.7.0",
        fabric_loader_target_version="0.17.2",
        neoforge_target_version="21",
        cobblemon_version="1.7.3+1.21.1",
        fabric_loader_version="0.17.2",
        fabric_api_version="0.116.6+1.21.1",
        fabric_kotlin_version="1.13.6",
        neoforge_version="21.1.182",
        neoforge_kotlin_version="5.10.0",
    )

    upload_to_curseforge(jar_file, props, "fabric", "changelog text")
    assert captured_metadata is not None
    assert captured_metadata["displayName"] == "Cobbleloots v2.3.0 [1.21.1] [Fabric]"
