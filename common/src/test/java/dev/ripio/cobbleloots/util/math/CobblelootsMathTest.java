package dev.ripio.cobbleloots.util.math;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsMathTest {
  @Test
  void testWeightedRandomEntry() {
    Map<ResourceLocation, Integer> weights = Map.of(
        cobblelootsResource("test1"), 1,
        cobblelootsResource("test2"), 2,
        cobblelootsResource("test3"), 3
    );
    Map.Entry<ResourceLocation, Integer> entry = CobblelootsMath.weightedRandomEntry(weights);
    assert entry != null;
    assert weights.containsKey(entry.getKey());
    assert Objects.equals(weights.get(entry.getKey()), entry.getValue());
  }
}
