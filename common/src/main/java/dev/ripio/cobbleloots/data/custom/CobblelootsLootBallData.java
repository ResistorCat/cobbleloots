package dev.ripio.cobbleloots.data.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class CobblelootsLootBallData {
    private final Component name;
    private final ResourceLocation lootTable;
    private final ResourceLocation texture;
    private final CobblelootsLootBallSources sources;
    private final List<CobblelootsLootBallVariant> variants;

    public CobblelootsLootBallData(Component name, ResourceLocation lootTable, ResourceLocation texture, CobblelootsLootBallSources sources, List<CobblelootsLootBallVariant> variants) {
        this.name = name;
        this.lootTable = lootTable;
        this.texture = texture;
        this.sources = sources;
        this.variants = variants;
    }

    public Component getName() {
        return this.name;
    }

    public ResourceLocation getLootTable() {
        return this.lootTable;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public CobblelootsLootBallSources getSources() {
        return this.sources;
    }

    public List<CobblelootsLootBallVariant> getVariants() {
        return this.variants;
    }
}
