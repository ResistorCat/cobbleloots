package dev.ripio.cobbleloots.data.custom.filter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

/**
 * A biome filter that supports multiple biome entries with flexible matching
 * logic.
 * 
 * <p>
 * Matching logic:
 * <ul>
 * <li>All entries with {@code required=true} must match (AND logic)</li>
 * <li>If there are entries with {@code required=false}, at least one must match
 * (OR logic among optional entries)</li>
 * <li>Empty filter always passes</li>
 * </ul>
 * </p>
 *
 * @param entries The list of biome entries to check
 */
public record CobbleloootsBiomeFilter(List<CobbleloootsBiomeEntry> entries) {

    /**
     * Creates an empty biome filter that always passes.
     */
    public static final CobbleloootsBiomeFilter EMPTY = new CobbleloootsBiomeFilter(List.of());

    /**
     * Checks if the biome at the given position matches this filter.
     *
     * @param level The server level
     * @param pos   The block position to check
     * @return true if the biome matches the filter criteria, false otherwise
     */
    public boolean test(ServerLevel level, BlockPos pos) {
        if (entries == null || entries.isEmpty()) {
            return true; // Empty filter always passes
        }

        Holder<Biome> biomeHolder = level.getBiome(pos);
        boolean hasOptionalEntries = false;
        boolean anyOptionalMatched = false;

        for (CobbleloootsBiomeEntry entry : entries) {
            boolean matches = matchesBiome(level, biomeHolder, entry);

            if (entry.required()) {
                // Required entries must all match
                if (!matches) {
                    return false;
                }
            } else {
                // Track optional entries
                hasOptionalEntries = true;
                if (matches) {
                    anyOptionalMatched = true;
                }
            }
        }

        // If there are optional entries, at least one must match
        if (hasOptionalEntries && !anyOptionalMatched) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a biome holder matches a single biome entry.
     */
    private boolean matchesBiome(ServerLevel level, Holder<Biome> biomeHolder, CobbleloootsBiomeEntry entry) {
        if (entry.id() == null || entry.id().isEmpty()) {
            return true;
        }

        if (entry.isTag()) {
            // Parse as biome tag
            ResourceLocation tagLocation = ResourceLocation.parse(entry.getTagLocation());
            TagKey<Biome> biomeTag = TagKey.create(Registries.BIOME, tagLocation);
            return biomeHolder.is(biomeTag);
        } else {
            // Parse as biome key
            ResourceLocation biomeId = ResourceLocation.parse(entry.id());
            return biomeHolder.is(biomeId);
        }
    }

    /**
     * Checks if this filter is empty (has no entries).
     *
     * @return true if the filter has no entries
     */
    public boolean isEmpty() {
        return entries == null || entries.isEmpty();
    }
}
