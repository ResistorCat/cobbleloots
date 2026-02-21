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
 * A biome filter that supports multiple biome entries.
 *
 * <p>
 * Matching logic:
 * <ul>
 * <li>If the biome at the position matches <b>any</b> entry, the filter
 * passes.</li>
 * <li>Empty filter always passes.</li>
 * </ul>
 * </p>
 * <p>
 * The {@code required} field on each entry controls whether loading should fail
 * if the biome/tag is not found (e.g. from an uninstalled mod), following the
 * Minecraft tag convention. It does not affect runtime matching.
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
     * @return true if the biome matches any entry, false otherwise
     */
    public boolean test(ServerLevel level, BlockPos pos) {
        if (entries == null || entries.isEmpty()) {
            return true; // Empty filter always passes
        }

        Holder<Biome> biomeHolder = level.getBiome(pos);

        for (CobbleloootsBiomeEntry entry : entries) {
            if (matchesBiome(level, biomeHolder, entry)) {
                return true;
            }
        }

        return false;
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
