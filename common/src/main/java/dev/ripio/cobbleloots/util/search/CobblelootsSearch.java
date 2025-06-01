package dev.ripio.cobbleloots.util.search;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

import org.jetbrains.annotations.Nullable;

public class CobblelootsSearch {
    // Maximum number of attempts to search a valid loot ball spawn position in a
    // section to optimize performance
    public static final int MAX_SECTION_SEARCH_ATTEMPTS = 5;

    @Nullable
    public static BlockPos searchRandomValidLootBallSpawn(LevelChunkSection section, RandomSource randomSource) {
        // Choose random x and z positions in the chunk section
        int x = randomSource.nextInt(16);
        int z = randomSource.nextInt(16);
        // Search valid Y position in the section, randomly
        int[] y = new int[LevelChunkSection.SECTION_HEIGHT];
        for (int i = 0; i < y.length; i++) {
            y[i] = i;
        }
        for (int i = 0; i < y.length; i++) {
            int randomIndex = randomSource.nextIntBetweenInclusive(i, y.length - 1);
            BlockState blockState = section.getBlockState(x, y[randomIndex], z);
            // Check if the block is not solid
            if (!blockState.isSolid()) {
                return new BlockPos(x, y[randomIndex], z);
            }
            y[randomIndex] = y[i];
        }

        return null;
    }

    public static int searchRandomNonEmptySectionIndex(LevelChunkSection[] sectionsArray, RandomSource randomSource) {
        int[] sectionIndexes = new int[sectionsArray.length];
        for (int i = 0; i < sectionsArray.length; i++) {
            sectionIndexes[i] = i;
        }
        int tries = Math.min(MAX_SECTION_SEARCH_ATTEMPTS, sectionIndexes.length);
        for (int i = 0; i < tries; i++) {
            int randomIndex = randomSource.nextIntBetweenInclusive(i, sectionIndexes.length - 1);
            LevelChunkSection section = sectionsArray[sectionIndexes[randomIndex]];
            // Check if the section is not empty
            if (!section.hasOnlyAir()) {
                return sectionIndexes[randomIndex];
            }
            sectionIndexes[randomIndex] = sectionIndexes[i];
        }
        return -1;
    }
}
