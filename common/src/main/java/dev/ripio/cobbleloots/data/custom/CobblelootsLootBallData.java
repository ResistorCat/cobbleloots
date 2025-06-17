package dev.ripio.cobbleloots.data.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class CobblelootsLootBallData {
    private final Component name;
    private final ResourceLocation lootTable;
    private final ResourceLocation texture;
    private final CobblelootsLootBallSources sources;
    private final Map<String, CobblelootsLootBallVariantData> variants;
    private final int xp;

    public CobblelootsLootBallData(Component name, ResourceLocation lootTable, ResourceLocation texture, CobblelootsLootBallSources sources, Map<String, CobblelootsLootBallVariantData> variants, int xp) {
        this.name = name;
        this.lootTable = lootTable;
        this.texture = texture;
        this.sources = sources;
        this.variants = variants;
        this.xp = xp;
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

    public Map<String, CobblelootsLootBallVariantData> getVariants() {
        return this.variants;
    }

    public int getXp() {
        return this.xp;
    }

}
