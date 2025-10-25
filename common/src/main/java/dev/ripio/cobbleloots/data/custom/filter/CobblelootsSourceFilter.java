package dev.ripio.cobbleloots.data.custom.filter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public class CobblelootsSourceFilter {
    private final int weight;
    private final TagKey<Structure> structure;
    private final TagKey<Biome> biome;
    private final List<ResourceLocation> dimension;
    private final CobblelootsBlockFilter block;
    private final TagKey<Fluid> fluid;
    private final CobblelootsPositionFilter position;
    private final CobblelootsLightFilter light;
    private final CobblelootsTimeFilter time;
    private final CobblelootsWeatherFilter weather;
    private final CobblelootsDateFilter date;

  public CobblelootsSourceFilter(int weight, TagKey<Structure> structure, TagKey<Biome> biome, List<ResourceLocation> dimension, CobblelootsBlockFilter block, TagKey<Fluid> fluid, CobblelootsPositionFilter position, CobblelootsLightFilter light, CobblelootsTimeFilter time, CobblelootsWeatherFilter weather, CobblelootsDateFilter date) {
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
    this.date = date;
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

  public List<ResourceLocation> getDimension() {
    return dimension;
  }

  public CobblelootsBlockFilter getBlock() {
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

  public CobblelootsDateFilter getDate() {
    return date;
  }

}
