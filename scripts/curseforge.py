"""
API interactions with CurseForge.
"""

import requests
import json
import os
from pathlib import Path
from models import ModProperties
from config import CURSEFORGE_VERSION_IDS


CURSEFORGE_API_URL = "https://minecraft.curseforge.com/api"


def get_curseforge_credentials() -> dict:
    """
    Retrieve CurseForge API credentials from environment variables.

    Raises:
        Exception: If required environment variables are not set.
    Returns:
        dict: A dictionary containing the API token and project ID.
    """
    token = os.getenv("CURSEFORGE_TOKEN")
    project_id = os.getenv("CURSEFORGE_PROJECT_ID")
    if not token:
        raise Exception("CURSEFORGE_TOKEN environment variable not set.")
    if not project_id:
        raise Exception("CURSEFORGE_PROJECT_ID environment variable not set.")
    return {"token": token, "project_id": project_id}


def upload_to_curseforge(
    file_path: Path, mod_properties: ModProperties, mod_loader: str, mod_changelog: str
) -> requests.Response:
    """
    Upload a file to CurseForge.

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
    credentials = get_curseforge_credentials()
    token = credentials["token"]
    project_id = credentials["project_id"]

    # Prepare request data
    metadata = {
        "changelog": mod_changelog,
        "changelogType": "markdown",
        "displayName": f"{mod_properties.mod_id.title()} v{mod_properties.mod_version} ~ {mod_loader.title()}",
        "gameVersions": [
            CURSEFORGE_VERSION_IDS[mod_properties.minecraft_version],
            CURSEFORGE_VERSION_IDS[mod_loader],
            CURSEFORGE_VERSION_IDS["java-21"],
            CURSEFORGE_VERSION_IDS["server"],
            CURSEFORGE_VERSION_IDS["client"],
        ],
        "releaseType": f"{mod_properties.mod_version_type}",
        "relations": {
            "projects": [
                {"slug": "cobblemon", "projectID": 687131, "type": "requiredDependency"}
            ]
        },
    }

    # Add Fabric API dependency if the mod loader is fabric
    if mod_loader == "fabric":
        metadata["relations"]["projects"].append(
            # Fabric API
            {"slug": "fabric-api", "projectID": 306612, "type": "requiredDependency"}
        )

    # Make the request
    url = f"{CURSEFORGE_API_URL}/projects/{project_id}/upload-file"
    headers = {"X-Api-Token": f"{token}", "User-Agent": "cobbleloots"}
    files = {
        "metadata": (None, json.dumps(metadata), "application/json"),
        "file": (file_path.name, open(file_path, "rb"), "application/java-archive"),
    }
    response = requests.post(url, headers=headers, files=files)

    # Check for errors
    if response.status_code != 200:
        raise Exception(f"Failed to upload to CurseForge: {response.text}")

    return response
