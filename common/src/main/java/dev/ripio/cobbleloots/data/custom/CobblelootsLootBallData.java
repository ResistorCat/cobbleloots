package dev.ripio.cobbleloots.data.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class CobblelootsLootBallData {
    private final Component name;
    private final boolean announce;
    private final int weight;
    private final ResourceLocation lootTable;
    private final ResourceLocation texture;
    private final List<CobblelootsLootBallSource> sources;
    private final List<CobblelootsLootBallData> variants;

    public CobblelootsLootBallData(Component name, boolean announce, int weight, ResourceLocation lootTable, ResourceLocation texture, List<CobblelootsLootBallSource> sources, List<CobblelootsLootBallData> variants) {
        this.name = name;
        this.announce = announce;
        this.weight = weight;
        this.lootTable = lootTable;
        this.texture = texture;
        this.sources = sources;
        this.variants = variants;
    }

    public Component getName() {
        return this.name;
    }

    public boolean getAnnounce() {
        return this.announce;
    }

    public int getWeight() {
        return this.weight;
    }

    public ResourceLocation getLootTable() {
        return this.lootTable;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public List<CobblelootsLootBallSource> getSources() {
        return this.sources;
    }

    public List<CobblelootsLootBallData> getVariants() {
        return this.variants;
    }
}
