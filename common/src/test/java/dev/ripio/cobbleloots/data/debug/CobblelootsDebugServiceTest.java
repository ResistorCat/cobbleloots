package dev.ripio.cobbleloots.data.debug;

import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CobblelootsDebugServiceTest {

  @BeforeAll
  static void setup() {
    net.minecraft.SharedConstants.tryDetectVersion();
    net.minecraft.server.Bootstrap.bootStrap();
  }

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

  @Test
  @DisplayName("TranslatableContents only allows String, Number, or Boolean as primitive arguments, rejecting raw ResourceLocation")
  void testTranslatableContentsArgumentValidation() {
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath("cobbleloots", "poke");
    assertFalse(
        net.minecraft.network.chat.contents.TranslatableContents.isAllowedPrimitiveArgument(id),
        "Raw ResourceLocation must NOT be considered an allowed primitive argument"
    );
    assertTrue(
        net.minecraft.network.chat.contents.TranslatableContents.isAllowedPrimitiveArgument(id.toString()),
        "String representation of ResourceLocation must be an allowed primitive argument"
    );

    net.minecraft.network.chat.Component validComp = net.minecraft.network.chat.Component.translatable(
        "commands.cobbleloots.debug.weights.entry",
        id.toString(),
        10,
        "20.0"
    );
    io.netty.buffer.ByteBuf buf = io.netty.buffer.Unpooled.buffer();
    assertDoesNotThrow(() -> {
      net.minecraft.network.chat.ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, validComp);
    }, "Translatable component with String arguments should encode without throwing EncoderException");

    net.minecraft.network.chat.Component invalidComp = net.minecraft.network.chat.Component.translatable(
        "commands.cobbleloots.debug.weights.entry",
        id,
        10,
        "20.0"
    );
    io.netty.buffer.ByteBuf invalidBuf = io.netty.buffer.Unpooled.buffer();
    assertThrows(Exception.class, () -> {
      net.minecraft.network.chat.ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(invalidBuf, invalidComp);
    }, "Translatable component with raw ResourceLocation must fail encoding with EncoderException");
  }

  @Test
  @DisplayName("All debug command translatable components encode successfully into network ByteBuf")
  void testAllDebugChatComponentsCanBeEncodedStreamCodec() {
    List<net.minecraft.network.chat.Component> components = List.of(
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.weights.header",
            "spawning", 100, 64, 200, "minecraft:plains"
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.weights.empty",
            "spawning"
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.weights.entry",
            "cobbleloots:loot_ball/poke", 10, "25.0"
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.weights.total",
            1, 10
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.invalid_source_type",
            "invalid_source"
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.check.not_found",
            "cobbleloots:loot_ball/unknown"
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.check.header",
            "cobbleloots:loot_ball/poke", "spawning", 100, 64, 200
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.check.no_rules",
            "cobbleloots:loot_ball/poke", "spawning"
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.check.rule_header",
            1, 10, net.minecraft.network.chat.Component.literal("[PASS]")
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.check.filter_passed",
            net.minecraft.network.chat.Component.translatable("commands.cobbleloots.debug.filter.biome"),
            "minecraft:plains"
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.check.filter_failed",
            net.minecraft.network.chat.Component.translatable("commands.cobbleloots.debug.filter.biome"),
            "minecraft:desert",
            "minecraft:plains"
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.check.result_eligible",
            10
        ),
        net.minecraft.network.chat.Component.translatable(
            "commands.cobbleloots.debug.check.result_ineligible"
        )
    );

    for (net.minecraft.network.chat.Component comp : components) {
      io.netty.buffer.ByteBuf buf = io.netty.buffer.Unpooled.buffer();
      assertDoesNotThrow(
          () -> net.minecraft.network.chat.ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, comp),
          "Failed to encode component: " + comp
      );
    }
  }
}



