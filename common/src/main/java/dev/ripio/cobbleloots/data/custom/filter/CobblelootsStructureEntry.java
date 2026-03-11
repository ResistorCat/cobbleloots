package dev.ripio.cobbleloots.data.custom.filter;

/**
 * Represents a single structure entry in a structure filter.
 * The id can be either a structure key (e.g., "minecraft:village_plains") or a structure tag
 * (e.g., "#minecraft:village").
 *
 * @param id       The structure identifier - either a ResourceLocation string or a
 *                 tag prefixed with #
 * @param required If true (default), loading will fail if this entry is not
 *                 found. Set to false for entries from mods that may not be
 *                 installed or tags defined in other datapacks.
 */
public record CobblelootsStructureEntry(String id, boolean required) {

    /**
     * Creates a required structure entry with the given id.
     *
     * @param id The structure identifier
     */
    public CobblelootsStructureEntry(String id) {
        this(id, true);
    }

    /**
     * Checks if this entry represents a structure tag (starts with #).
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
