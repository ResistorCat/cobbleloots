package dev.ripio.cobbleloots.util.search;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.Nullable;

import static dev.ripio.cobbleloots.data.CobblelootsTags.LOOT_BALL_SPAWNABLE;

public class CobblelootsSearch {
  @Nullable
  public static BlockPos searchRandomValidLootBallSpawn(LevelChunkSection section, RandomSource randomSource) {
      // Choose random x and z positions in the chunk section
      int x = randomSource.nextInt(16);
      int z = randomSource.nextInt(16);
      // Search valid Y position in the section, randomly
      int[] y = new int[LevelChunkSection.SECTION_HEIGHT-1];
      for (int i = 0; i < y.length; i++) {
          y[i] = i+1;
      }
      for (int i = 0; i < y.length; i++) {
          int randomIndex = randomSource.nextIntBetweenInclusive(i, y.length - 1);
          BlockState state = section.getBlockState(x, y[randomIndex], z);
          BlockState underState = section.getBlockState(x, y[randomIndex]-1, z);
          if (state.is(LOOT_BALL_SPAWNABLE) && !underState.isAir()) {
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
      for (int i = 0; i < sectionIndexes.length; i++) {
          int randomIndex = randomSource.nextIntBetweenInclusive(i, sectionIndexes.length - 1);
          LevelChunkSection section = sectionsArray[sectionIndexes[randomIndex]];
          if (!section.hasOnlyAir()) {
              return sectionIndexes[randomIndex];
          }
          sectionIndexes[randomIndex] = sectionIndexes[i];
      }
      return -1;
  }
}
