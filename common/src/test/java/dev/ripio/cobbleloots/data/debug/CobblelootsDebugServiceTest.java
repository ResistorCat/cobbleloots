package dev.ripio.cobbleloots.data.debug;

import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CobblelootsDebugServiceTest {

  @Test
  @DisplayName("calculateWeights handles null parameters gracefully without throwing NPE")
  void testCalculateWeightsNullSafety() {
    WeightsReport report = CobblelootsDebugService.calculateWeights(null, null, null, null);
    assertNotNull(report);
    assertEquals(0, report.totalWeight());
    assertTrue(report.entries().isEmpty());
    assertEquals("minecraft", report.dimension().getNamespace());
    assertEquals("unknown", report.dimension().getPath());
    assertEquals("minecraft", report.biome().getNamespace());
    assertEquals("unknown", report.biome().getPath());
  }

  @Test
  @DisplayName("inspectLootBall handles null parameters gracefully without throwing NPE")
  void testInspectLootBallNullSafety() {
    LootBallCheckReport report = CobblelootsDebugService.inspectLootBall(null, null, null, null, null);
    assertNotNull(report);
    assertFalse(report.eligible());
    assertEquals(0, report.totalWeight());
    assertTrue(report.rules().isEmpty());
  }

  @Test
  @DisplayName("inspectLootBall returns ineligible for non-existent loot ball ID")
  void testInspectLootBallNonExistent() {
    ResourceLocation unknownId = ResourceLocation.fromNamespaceAndPath("cobbleloots", "non_existent_ball");
    LootBallCheckReport report = CobblelootsDebugService.inspectLootBall(null, null, unknownId, CobblelootsSourceType.SPAWNING, null);
    assertNotNull(report);
    assertEquals(unknownId, report.id());
    assertFalse(report.eligible());
    assertEquals(0, report.totalWeight());
    assertTrue(report.rules().isEmpty());
  }

  @Test
  @DisplayName("Weights entry sorting order: descending by weight, then ascending by ResourceLocation ID")
  void testWeightEntrySortingOrder() {
    ResourceLocation pokeBall = ResourceLocation.fromNamespaceAndPath("cobbleloots", "poke_ball");
    ResourceLocation greatBall = ResourceLocation.fromNamespaceAndPath("cobbleloots", "great_ball");
    ResourceLocation ultraBall = ResourceLocation.fromNamespaceAndPath("cobbleloots", "ultra_ball");
    ResourceLocation diveBall = ResourceLocation.fromNamespaceAndPath("cobbleloots", "dive_ball");

    List<LootBallWeightEntry> entries = new ArrayList<>();
    entries.add(new LootBallWeightEntry(pokeBall, 10, 20.0));
    entries.add(new LootBallWeightEntry(ultraBall, 30, 60.0));
    entries.add(new LootBallWeightEntry(greatBall, 10, 20.0));
    entries.add(new LootBallWeightEntry(diveBall, 10, 20.0));

    entries.sort(
        Comparator.comparingInt(LootBallWeightEntry::weight).reversed()
            .thenComparing(LootBallWeightEntry::id)
    );

    // Expected order:
    // 1. ultraBall (weight 30)
    // 2. diveBall (weight 10, 'd' comes before 'g')
    // 3. greatBall (weight 10, 'g' comes before 'p')
    // 4. pokeBall (weight 10, 'p')
    assertEquals(ultraBall, entries.get(0).id());
    assertEquals(diveBall, entries.get(1).id());
    assertEquals(greatBall, entries.get(2).id());
    assertEquals(pokeBall, entries.get(3).id());
  }
}
