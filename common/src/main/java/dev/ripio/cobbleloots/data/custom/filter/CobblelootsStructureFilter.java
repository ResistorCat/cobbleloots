package dev.ripio.cobbleloots.data.custom.filter;

import dev.ripio.cobbleloots.Cobbleloots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A structure filter that supports multiple structure entries.
 *
 * <p>
 * Matching logic:
 * <ul>
 * <li>If the structure at the position matches <b>any</b> entry, the filter
 * passes.</li>
 * <li>Empty filter always passes.</li>
 * </ul>
 * </p>
 *
 * @param entries The list of structure entries to check
 */
public record CobblelootsStructureFilter(List<CobblelootsStructureEntry> entries) {

    private static final Set<String> warnedMissingEntries = new HashSet<>();

    public static void clearValidationWarnings() {
        warnedMissingEntries.clear();
    }

    public static final CobblelootsStructureFilter EMPTY = new CobblelootsStructureFilter(List.of());

    public boolean test(ServerLevel level, BlockPos pos) {
        if (entries == null || entries.isEmpty()) {
            return true;
        }

        for (CobblelootsStructureEntry entry : entries) {
            if (matchesStructure(level, pos, entry)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesStructure(ServerLevel level, BlockPos pos, CobblelootsStructureEntry entry) {
        if (entry.id() == null || entry.id().isEmpty()) {
            return true;
        }

        if (entry.required()) {
            if (entry.isTag()) {
                ResourceLocation tagLocation = ResourceLocation.parse(entry.getTagLocation());
                TagKey<Structure> structureTag = TagKey.create(Registries.STRUCTURE, tagLocation);
                if (level.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(structureTag).isEmpty()) {
                    if (warnedMissingEntries.add(entry.id())) {
                        Cobbleloots.LOGGER.warn("Missing required structure tag '{}'. Skipping entry.", entry.id());
                    }
                    return false;
                }
            } else {
                ResourceLocation structureId = ResourceLocation.parse(entry.id());
                if (!level.registryAccess().registryOrThrow(Registries.STRUCTURE).containsKey(structureId)) {
                    if (warnedMissingEntries.add(entry.id())) {
                        Cobbleloots.LOGGER.warn("Missing required structure '{}'. Skipping entry.", entry.id());
                    }
                    return false;
                }
            }
        }

        try {
            StructureStart structureStart;
            if (entry.isTag()) {
                ResourceLocation tagLocation = ResourceLocation.parse(entry.getTagLocation());
                TagKey<Structure> structureTag = TagKey.create(Registries.STRUCTURE, tagLocation);
                structureStart = level.structureManager().getStructureWithPieceAt(pos, holder -> holder.is(structureTag));
            } else {
                ResourceLocation structureId = ResourceLocation.parse(entry.id());
                structureStart = level.structureManager().getStructureWithPieceAt(pos, holder -> holder.is(structureId));
            }
            
            boolean result = structureStart != null && structureStart.isValid();
            return result;
        } catch (Exception e) {
            Cobbleloots.LOGGER.error("Structure filter check failed at {}: {}", pos, e.getMessage());
            return false;
        }
    }

    public boolean isEmpty() {
        return entries == null || entries.isEmpty();
    }
}
