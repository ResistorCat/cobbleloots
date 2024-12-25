package dev.ripio.cobbleloots.data.lootball;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CobblelootsLootBallSource {
    private final String type;
    private final ResourceLocation biome;
    private final CobblelootsLootBallHeight height;
    private final int weight;

    public CobblelootsLootBallSource(String type, ResourceLocation biome, CobblelootsLootBallHeight height, int weight) {
        this.type = type;
        this.biome = biome;
        this.height = height;
        this.weight = weight;
    }

    public String getType() {
        return this.type;
    }

    @Nullable
    public ResourceLocation getBiome() {
        return this.biome;
    }

    public CobblelootsLootBallHeight getHeight() {
        return this.height;
    }

    public int getWeight() {
        return this.weight;
    }
}
