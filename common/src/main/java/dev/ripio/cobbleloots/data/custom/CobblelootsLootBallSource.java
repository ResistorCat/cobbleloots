package dev.ripio.cobbleloots.data.custom;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class CobblelootsLootBallSource {
    private final String type;
    private final TagKey<Biome> biome;
    private final CobblelootsLootBallHeight height;
    private final int weight;

    public CobblelootsLootBallSource(String type, TagKey<Biome> biome, CobblelootsLootBallHeight height, int weight) {
        this.type = type;
        this.biome = biome;
        this.height = height;
        this.weight = weight;
    }

    public String getType() {
        return this.type;
    }

    public TagKey<Biome> getBiome() {
        return this.biome;
    }

    public CobblelootsLootBallHeight getHeight() {
        return this.height;
    }

    public int getWeight() {
        return this.weight;
    }
}
