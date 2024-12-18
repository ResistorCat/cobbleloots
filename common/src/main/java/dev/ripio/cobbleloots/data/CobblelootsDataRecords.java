package dev.ripio.cobbleloots.data;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;

public interface CobblelootsDataRecords {

    record LootBallRecord (
        String name,
        int weight,
        ResourceLocation table,
        ResourceLocation texture,
        ArrayList<LootBallSource> sources,
        ArrayList<LootBallRecord> variants
    ){}

    record LootBallSource(
        String type,
        ResourceLocation biome,
        LootBallHeight height,
        int weight
    ){}

    record LootBallHeight(
        String min,
        String max
    ){}

}
