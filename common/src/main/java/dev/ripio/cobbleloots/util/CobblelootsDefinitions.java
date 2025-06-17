package dev.ripio.cobbleloots.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsDefinitions {
    public static final ResourceLocation EMPTY_LOCATION = cobblelootsResource("empty");
    public static final TagKey<Biome> EMPTY_BIOME_TAG = TagKey.create(Registries.BIOME, EMPTY_LOCATION);
    public static final TagKey<Structure> EMPTY_STRUCTURE_TAG = TagKey.create(Registries.STRUCTURE, EMPTY_LOCATION);
    public static final TagKey<Block> EMPTY_BLOCK_TAG = TagKey.create(Registries.BLOCK, EMPTY_LOCATION);
    public static final TagKey<Fluid> EMPTY_FLUID_TAG = TagKey.create(Registries.FLUID, EMPTY_LOCATION);
    public static final String PATH_LOOT_BALLS = "loot_ball";
}
