"""
API interactions with Modrinth.
"""

import requests
import json
import os
from pathlib import Path
from models import ModProperties


MODRINTH_API_URL = "https://api.modrinth.com/v2"


def upload_to_modrinth(
    file_path: Path, mod_properties: ModProperties, mod_loader: str, mod_changelog: str
) -> requests.Response:
    """
    Upload a file to Modrinth.

    Args:
        file_path (Path): Path to the file to upload.
        mod_properties (ModProperties): Mod properties including mod ID, version, etc.
        mod_loader (str): The mod loader type (e.g., "fabric", "neoforge").
        mod_changelog (str): The changelog for the version.

    Raises:
        Exception: If the upload fails or if the version already exists.

    Returns:
        Response object from the upload request.
    """

    # Get environment variables
    token = os.getenv("MODRINTH_TOKEN")
    project_id = os.getenv("MODRINTH_PROJECT_ID")
    if not token:
        raise Exception("MODRINTH_TOKEN environment variable not set.")
    if not project_id:
        raise Exception("MODRINTH_PROJECT_ID environment variable not set.")

    # Prepare request data
    metadata = {
        "name": f"{mod_properties.mod_id.title()} v{mod_properties.mod_version} ~ {mod_loader.title()}",
        "version_number": f"{mod_properties.mod_version}",
        "changelog": mod_changelog,
        "dependencies": [
            # Cobblemon
            {"project_id": "MdwFAVRL", "dependency_type": "required"}
        ],
        "game_versions": [f"{mod_properties.minecraft_version}"],
        "version_type": f"{mod_properties.mod_version_type}",
        "loaders": [mod_loader],
        "featured": True,
        "project_id": f"{project_id}",
        "file_parts": ["jarfile"],
        "primary_file": "jarfile",
    }

    # Add Fabric API dependency if the mod loader is fabric
    if mod_loader == "fabric":
        metadata["dependencies"].append(
            # Fabric API
            {"project_id": "P7dR8mSH", "dependency_type": "required"}
        )

    # Make the request
    url = f"{MODRINTH_API_URL}/version"
    headers = {"Authorization": f"Bearer {token}", "User-Agent": "cobbleloots"}
    files = {
        "data": (None, json.dumps(metadata), "application/json"),
        "jarfile": (file_path.name, open(file_path, "rb"), "application/java-archive"),
    }
    response = requests.post(url, headers=headers, files=files)

    if response.status_code != 200:
        raise Exception(f"Failed to upload file: {response.text}")

    return response


def upload_modinfo_to_modrinth(modinfo_content: str) -> requests.Response:
    """
    Upload MODINFO.md content to Modrinth.

    Args:
        modinfo_content (str): Content of the MODINFO.md file.

    Raises:
        Exception: If the upload fails.

    Returns:
        Response object from the upload request.
    """

    # Get environment variables
    token = os.getenv("MODRINTH_TOKEN")
    project_id = os.getenv("MODRINTH_PROJECT_ID")
    if not token:
        raise Exception("MODRINTH_TOKEN environment variable not set.")
    if not project_id:
        raise Exception("MODRINTH_PROJECT_ID environment variable not set.")

    # Prepare request data
    request_body = {"body": modinfo_content}

    # Make the request
    url = f"{MODRINTH_API_URL}/project/{project_id}"
    headers = {"Authorization": f"Bearer {token}", "User-Agent": "cobbleloots"}
    response = requests.patch(url, headers=headers, json=request_body)

    if response.status_code != 204:
        raise Exception(f"Failed to update MODINFO.md: {response.text}")

    return response


def get_version_info(version_number: str) -> requests.Response:
    """
    Get version information from Modrinth.

    Args:
        version_number (str): The version number to look up.

    Raises:
        Exception: If the request fails.

    Returns:
        Response object from the version info request.
    """

    # Get environment variables
    token = os.getenv("MODRINTH_TOKEN")
    project_id = os.getenv("MODRINTH_PROJECT_ID")
    if not token:
        raise Exception("MODRINTH_TOKEN environment variable not set.")
    if not project_id:
        raise Exception("MODRINTH_PROJECT_ID environment variable not set.")

    # Make the request
    url = f"{MODRINTH_API_URL}/project/{project_id}/version/{version_number}"
    headers = {"Authorization": f"Bearer {token}", "User-Agent": "cobbleloots"}
    response = requests.get(url, headers=headers)

    if response.status_code != 200 and response.status_code != 404:
        raise Exception(f"Failed to fetch versions: {response.text}")

    return response
