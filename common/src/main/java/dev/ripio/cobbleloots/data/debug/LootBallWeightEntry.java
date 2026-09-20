package dev.ripio.cobbleloots.data.debug;

import net.minecraft.resources.ResourceLocation;

public record LootBallWeightEntry(
    ResourceLocation id,
    int weight,
    double percentage
) {}
