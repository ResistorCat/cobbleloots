package dev.ripio.cobbleloots.data.custom.filter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

public class CobblelootsSourceFilter {
    private final int weight;
    private final TagKey<Structure> structure;
    private final TagKey<Biome> biome;
    private final ResourceLocation dimension;
    private final TagKey<Block> block;
    private final TagKey<Fluid> fluid;
    private final CobblelootsPositionFilter position;
    private final CobblelootsLightFilter light;
    private final CobblelootsTimeFilter time;
    private final CobblelootsWeatherFilter weather;

  public CobblelootsSourceFilter(int weight, TagKey<Structure> structure, TagKey<Biome> biome, ResourceLocation dimension, TagKey<Block> block, TagKey<Fluid> fluid, CobblelootsPositionFilter position, CobblelootsLightFilter light, CobblelootsTimeFilter time, CobblelootsWeatherFilter weather) {
    this.weight = weight;
    this.structure = structure;
    this.biome = biome;
    this.dimension = dimension;
    this.block = block;
    this.fluid = fluid;
    this.position = position;
    this.light = light;
    this.time = time;
    this.weather = weather;
  }

  public int getWeight() {
    return weight;
  }

  public TagKey<Structure> getStructure() {
    return structure;
  }

  public TagKey<Biome> getBiome() {
    return biome;
  }

  public ResourceLocation getDimension() {
    return dimension;
  }

  public TagKey<Block> getBlock() {
    return block;
  }

  public TagKey<Fluid> getFluid() {
    return fluid;
  }

  public CobblelootsPositionFilter getPosition() {
    return position;
  }

  public CobblelootsLightFilter getLight() {
    return light;
  }

  public CobblelootsTimeFilter getTime() {
    return time;
  }

  public CobblelootsWeatherFilter getWeather() {
    return weather;
  }

}
