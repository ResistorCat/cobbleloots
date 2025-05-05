package dev.ripio.cobbleloots.util.math;

import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CobblelootsMath {
  public static final RandomSource randomSource = RandomSource.create();

  @Nullable
  public static <T> Map.Entry<T, Integer> weightedRandomEntry(Map<T, Integer> weights) {
      int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
      int randomValue = randomSource.nextInt(totalWeight);
      int cumulativeWeight = 0;
      for (Map.Entry<T, Integer> entry : weights.entrySet()) {
          cumulativeWeight += entry.getValue();
          if (randomValue < cumulativeWeight) {
              return entry;
          }
      }
      return null;
  }

}
