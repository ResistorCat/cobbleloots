"""
Global constants for Cobbleloots publishing and release scripts.
"""

# Loader mappings
LOADER_NAMES = {
    "fabric": "Fabric",
    "neoforge": "NeoForge",
}

# API URLs
CURSEFORGE_API_URL = "https://minecraft.curseforge.com/api"
MODRINTH_API_URL = "https://api.modrinth.com/v2"

# CurseForge Game and Platform Version IDs
CURSEFORGE_VERSION_IDS = {
    "1.21.1": 11779,
    "fabric": 7499,
    "neoforge": 10150,
    "server": 9639,
    "client": 9638,
    "java-21": 11135,
}

# Modrinth Dependency Project IDs
MODRINTH_COBBLEMON_PROJECT_ID = "MdwFAVRL"
MODRINTH_FABRIC_API_PROJECT_ID = "P7dR8mSH"

# CurseForge Dependency Project IDs
CURSEFORGE_COBBLEMON_PROJECT_ID = 687131
CURSEFORGE_COBBLEMON_SLUG = "cobblemon"
CURSEFORGE_FABRIC_API_PROJECT_ID = 306612
CURSEFORGE_FABRIC_API_SLUG = "fabric-api"

# Changelog Footers
FOOTERS = {
    "alpha": "> Alpha versions may contain bugs and **unfinished features**. Use them at your own risk. If you find any issues, please report them on the Discord server.",
    "beta": "> Beta versions may contain bugs. Please report any issues you find on the Discord server.",
    "release": "> Please report any issues you find on the Discord server.",
}
