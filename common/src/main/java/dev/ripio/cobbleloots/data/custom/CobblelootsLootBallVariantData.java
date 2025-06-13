package dev.ripio.cobbleloots.data.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CobblelootsLootBallVariantData {
    private final Component name;
    private final ResourceLocation lootTable;
    private final ResourceLocation texture;

    public CobblelootsLootBallVariantData(Component name, ResourceLocation lootTable, ResourceLocation texture) {
        this.name = name;
        this.lootTable = lootTable;
        this.texture = texture;
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
}
