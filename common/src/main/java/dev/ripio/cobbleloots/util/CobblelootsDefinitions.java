package dev.ripio.cobbleloots.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsDefinitions {
    public static final ResourceLocation EMPTY_LOCATION = cobblelootsResource("empty");
    public static final TagKey<Biome> EMPTY_BIOME_TAG = TagKey.create(Registries.BIOME, EMPTY_LOCATION);
}
