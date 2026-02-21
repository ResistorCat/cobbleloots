package dev.ripio.cobbleloots.data.custom.filter;

/**
 * Represents a single biome entry in a biome filter.
 * The id can be either a biome key (e.g., "minecraft:swamp") or a biome tag
 * (e.g., "#cobblemon:is_swamp").
 *
 * @param id       The biome identifier - either a ResourceLocation string or a
 *                 tag prefixed with #
 * @param required If true (default), loading will fail if this entry is not
 *                 found. Set to false for entries from mods that may not be
 *                 installed or tags defined in other datapacks.
 */
public record CobbleloootsBiomeEntry(String id, boolean required) {

    /**
     * Creates a required biome entry with the given id.
     *
     * @param id The biome identifier
     */
    public CobbleloootsBiomeEntry(String id) {
        this(id, true);
    }

    /**
     * Checks if this entry represents a biome tag (starts with #).
     *
     * @return true if the id starts with #, false otherwise
     */
    public boolean isTag() {
        return id != null && id.startsWith("#");
    }

    /**
     * Gets the tag location string without the # prefix.
     * Only valid if {@link #isTag()} returns true.
     *
     * @return The tag location without the # prefix
     */
    public String getTagLocation() {
        if (!isTag()) {
            throw new IllegalStateException("Not a tag: " + id);
        }
        return id.substring(1);
    }
}
