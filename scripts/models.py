from pydantic import BaseModel


class ModProperties(BaseModel):
    """
    Model for mod properties loaded from gradle.properties.
    """

    mod_id: str
    mod_version: str
    mod_version_type: str
    mod_name: str
    mod_description: str
    mod_authors: str
    mod_license: str
    mod_logo: str
    mod_homepage: str
    mod_source: str
    mod_issues: str
    mod_discord: str
    mod_modrinth: str
    mod_curseforge: str
    maven_group: str
    archives_name: str
    enabled_platforms: str
    minecraft_version: str
    cobblemon_target_version: str
    fabric_loader_target_version: str
    neoforge_target_version: str
    cobblemon_version: str
    fabric_loader_version: str
    fabric_api_version: str
    fabric_kotlin_version: str
    neoforge_version: str
    neoforge_kotlin_version: str
    modmenu_version: str
