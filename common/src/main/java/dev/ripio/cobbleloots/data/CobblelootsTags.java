package dev.ripio.cobbleloots.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsTags {
    public static TagKey<Biome> getBiomeTagKey(String path) {
        return TagKey.create(Registries.BIOME, cobblelootsResource(path));
    }

    public static TagKey<Block> getBlockTagKey(String path) {
        return TagKey.create(Registries.BLOCK, cobblelootsResource(path));
    }
}
