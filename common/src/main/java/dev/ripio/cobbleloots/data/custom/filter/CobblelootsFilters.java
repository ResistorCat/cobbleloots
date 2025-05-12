package dev.ripio.cobbleloots.data.custom.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class CobblelootsFilters {
  private static final int DEFAULT_WEIGHT = 1;
  private static final int MAX_ABSOLUTE_POSITION = Integer.MAX_VALUE;
  private static final int MIN_LIGHT = 0;
  private static final int MAX_LIGHT = 15;
  private static final int MIN_TICK = 0;
  private static final int MAX_TICK = Integer.MAX_VALUE;

  private static final CobblelootsMinMaxFilter DEFAULT_MIN_MAX_POSITION = new CobblelootsMinMaxFilter(-MAX_ABSOLUTE_POSITION, MAX_ABSOLUTE_POSITION);
  private static final CobblelootsMinMaxFilter DEFAULT_MIN_MAX_LIGHT = new CobblelootsMinMaxFilter(MIN_LIGHT, MAX_LIGHT);
  private static final CobblelootsMinMaxFilter DEFAULT_MIN_MAX_TIME = new CobblelootsMinMaxFilter(MIN_TICK, MAX_TICK);
  private static final CobblelootsPositionFilter DEFAULT_POSITION_FILTER = new CobblelootsPositionFilter(DEFAULT_MIN_MAX_POSITION, DEFAULT_MIN_MAX_POSITION, DEFAULT_MIN_MAX_POSITION);
  private static final CobblelootsLightFilter DEFAULT_LIGHT_FILTER = new CobblelootsLightFilter(DEFAULT_MIN_MAX_LIGHT, DEFAULT_MIN_MAX_LIGHT);
  private static final CobblelootsTimeFilter DEFAULT_TIME_FILTER = new CobblelootsTimeFilter(DEFAULT_MIN_MAX_TIME, 0);
  private static final CobblelootsWeatherFilter DEFAULT_WEATHER_FILTER = new CobblelootsWeatherFilter(false, false, false);

  public static final Codec<CobblelootsMinMaxFilter> MIN_MAX_POSITION_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          Codec.INT.optionalFieldOf("min", -MAX_ABSOLUTE_POSITION).forGetter(CobblelootsMinMaxFilter::getMin),
          Codec.INT.optionalFieldOf("max", MAX_ABSOLUTE_POSITION).forGetter(CobblelootsMinMaxFilter::getMax)
      ).apply(instance, CobblelootsMinMaxFilter::new)
  );

  public static final Codec<CobblelootsMinMaxFilter> MIN_MAX_LIGHT_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          Codec.INT.optionalFieldOf("min", MIN_LIGHT).forGetter(CobblelootsMinMaxFilter::getMin),
          Codec.INT.optionalFieldOf("max", MAX_LIGHT).forGetter(CobblelootsMinMaxFilter::getMax)
      ).apply(instance, CobblelootsMinMaxFilter::new)
  );

  public static final Codec<CobblelootsMinMaxFilter> MIN_MAX_TIME_VALUE_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          Codec.INT.optionalFieldOf("min", MIN_TICK).forGetter(CobblelootsMinMaxFilter::getMin),
          Codec.INT.optionalFieldOf("max", MAX_TICK).forGetter(CobblelootsMinMaxFilter::getMax)
      ).apply(instance, CobblelootsMinMaxFilter::new)
  );

  public static final Codec<CobblelootsPositionFilter> POSITION_FILTER_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          MIN_MAX_POSITION_CODEC.optionalFieldOf("x", DEFAULT_MIN_MAX_POSITION).forGetter(CobblelootsPositionFilter::getX),
          MIN_MAX_POSITION_CODEC.optionalFieldOf("y", DEFAULT_MIN_MAX_POSITION).forGetter(CobblelootsPositionFilter::getY),
          MIN_MAX_POSITION_CODEC.optionalFieldOf("z", DEFAULT_MIN_MAX_POSITION).forGetter(CobblelootsPositionFilter::getZ)
      ).apply(instance, CobblelootsPositionFilter::new)
  );

  public static final Codec<CobblelootsLightFilter> LIGHT_FILTER_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          MIN_MAX_LIGHT_CODEC.optionalFieldOf("block", DEFAULT_MIN_MAX_LIGHT).forGetter(CobblelootsLightFilter::getBlock),
          MIN_MAX_LIGHT_CODEC.optionalFieldOf("sky", DEFAULT_MIN_MAX_LIGHT).forGetter(CobblelootsLightFilter::getSky)
      ).apply(instance, CobblelootsLightFilter::new)
  );

  public static final Codec<CobblelootsTimeFilter> TIME_FILTER_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          MIN_MAX_TIME_VALUE_CODEC.optionalFieldOf("value", DEFAULT_MIN_MAX_TIME).forGetter(CobblelootsTimeFilter::getValue),
          Codec.INT.optionalFieldOf("period", 0).forGetter(CobblelootsTimeFilter::getPeriod)
      ).apply(instance, CobblelootsTimeFilter::new)
  );

  public static final Codec<CobblelootsWeatherFilter> WEATHER_FILTER_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          Codec.BOOL.optionalFieldOf("rain", false).forGetter(CobblelootsWeatherFilter::getRain),
          Codec.BOOL.optionalFieldOf("thunder", false).forGetter(CobblelootsWeatherFilter::getThunder),
          Codec.BOOL.optionalFieldOf("clear", false).forGetter(CobblelootsWeatherFilter::getClear)
      ).apply(instance, CobblelootsWeatherFilter::new)
  );

  public static final Codec<CobblelootsSourceFilter> LOOT_BALL_SOURCE_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
          Codec.INT.optionalFieldOf("weight", DEFAULT_WEIGHT).forGetter(CobblelootsSourceFilter::getWeight),
          TagKey.codec(Registries.STRUCTURE).optionalFieldOf("structure", CobblelootsDefinitions.EMPTY_STRUCTURE_TAG).forGetter(CobblelootsSourceFilter::getStructure),
          TagKey.codec(Registries.BIOME).optionalFieldOf("biome", CobblelootsDefinitions.EMPTY_BIOME_TAG).forGetter(CobblelootsSourceFilter::getBiome),
          ResourceLocation.CODEC.optionalFieldOf("dimension", CobblelootsDefinitions.EMPTY_LOCATION).forGetter(CobblelootsSourceFilter::getDimension),
          TagKey.codec(Registries.BLOCK).optionalFieldOf("block", CobblelootsDefinitions.EMPTY_BLOCK_TAG).forGetter(CobblelootsSourceFilter::getBlock),
          TagKey.codec(Registries.FLUID).optionalFieldOf("fluid", CobblelootsDefinitions.EMPTY_FLUID_TAG).forGetter(CobblelootsSourceFilter::getFluid),
          POSITION_FILTER_CODEC.optionalFieldOf("position", DEFAULT_POSITION_FILTER).forGetter(CobblelootsSourceFilter::getPosition),
          LIGHT_FILTER_CODEC.optionalFieldOf("light", DEFAULT_LIGHT_FILTER).forGetter(CobblelootsSourceFilter::getLight),
          TIME_FILTER_CODEC.optionalFieldOf("time", DEFAULT_TIME_FILTER).forGetter(CobblelootsSourceFilter::getTime),
          WEATHER_FILTER_CODEC.optionalFieldOf("weather", DEFAULT_WEATHER_FILTER).forGetter(CobblelootsSourceFilter::getWeather)
      ).apply(instance, CobblelootsSourceFilter::new)
  );
}
