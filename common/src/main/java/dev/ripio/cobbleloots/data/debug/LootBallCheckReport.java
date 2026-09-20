package dev.ripio.cobbleloots.data.debug;

import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record LootBallCheckReport(
    ResourceLocation id,
    CobblelootsSourceType sourceType,
    BlockPos pos,
    ResourceLocation dimension,
    ResourceLocation biome,
    boolean eligible,
    int totalWeight,
    List<LootBallRuleReport> rules
) {}
