package dev.ripio.cobbleloots.data.custom.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ripio.cobbleloots.util.CobblelootsDefinitions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.List;

public class CobblelootsFilters {
	private static final int DEFAULT_WEIGHT = 1;
	private static final int MAX_ABSOLUTE_POSITION = Integer.MAX_VALUE;
	private static final int MIN_LIGHT = 0;
	private static final int MAX_LIGHT = 15;
	private static final int MIN_TICK = 0;
	private static final int MAX_TICK = Integer.MAX_VALUE;

	private static final CobblelootsMinMaxFilter DEFAULT_MIN_MAX_POSITION = new CobblelootsMinMaxFilter(
			-MAX_ABSOLUTE_POSITION, MAX_ABSOLUTE_POSITION);
	private static final CobblelootsMinMaxFilter DEFAULT_MIN_MAX_LIGHT = new CobblelootsMinMaxFilter(MIN_LIGHT,
			MAX_LIGHT);
	private static final CobblelootsMinMaxFilter DEFAULT_MIN_MAX_TIME = new CobblelootsMinMaxFilter(MIN_TICK,
			MAX_TICK);
	private static final CobblelootsPositionFilter DEFAULT_POSITION_FILTER = new CobblelootsPositionFilter(
			DEFAULT_MIN_MAX_POSITION, DEFAULT_MIN_MAX_POSITION, DEFAULT_MIN_MAX_POSITION);
	private static final CobblelootsLightFilter DEFAULT_LIGHT_FILTER = new CobblelootsLightFilter(
			DEFAULT_MIN_MAX_LIGHT,
			DEFAULT_MIN_MAX_LIGHT);
	private static final CobblelootsTimeFilter DEFAULT_TIME_FILTER = new CobblelootsTimeFilter(DEFAULT_MIN_MAX_TIME,
			0);
	private static final CobblelootsWeatherFilter DEFAULT_WEATHER_FILTER = new CobblelootsWeatherFilter(true,
			true,
			true);
	private static final CobblelootsBlockFilter DEFAULT_BLOCK_FILTER = new CobblelootsBlockFilter(
			CobblelootsDefinitions.EMPTY_BLOCK_TAG, CobblelootsDefinitions.EMPTY_BLOCK_TAG);
	private static final CobblelootsDateFilter DEFAULT_DATE_FILTER = new CobblelootsDateFilter("", "");
	private static final CobblelootsPokeRodFilter DEFAULT_POKE_ROD_FILTER = new CobblelootsPokeRodFilter(List.of());

	/**
	 * Codec for serializing and deserializing CobblelootsBlockFilter objects.
	 * This codec handles the block-based filtering for loot balls.
	 * 
	 * The codec creates a record with two fields:
	 * - "spawn": A TagKey for the block that can spawn loot balls, defaults to
	 * CobblelootsDefinitions.EMPTY_BLOCK_TAG if not specified
	 * - "base": A TagKey for the base block of the loot ball, defaults to
	 * CobblelootsDefinitions.EMPTY_BLOCK_TAG if not specified
	 * 
	 * @see CobblelootsBlockFilter
	 */
	public static final Codec<CobblelootsBlockFilter> BLOCK_FILTER_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					TagKey.codec(Registries.BLOCK)
							.optionalFieldOf("spawn",
									CobblelootsDefinitions.EMPTY_BLOCK_TAG)
							.forGetter(CobblelootsBlockFilter::getSpawn),
					TagKey.codec(Registries.BLOCK)
							.optionalFieldOf("base", CobblelootsDefinitions.EMPTY_BLOCK_TAG)
							.forGetter(CobblelootsBlockFilter::getBase))
					.apply(instance, CobblelootsBlockFilter::new));

	/**
	 * Codec for serializing and deserializing CobblelootsMinMaxFilter objects.
	 * This codec handles the min and max position values for position-based
	 * filtering.
	 * 
	 * The codec creates a record with two optional integer fields:
	 * - "min": The minimum position value, defaults to -MAX_ABSOLUTE_POSITION if
	 * not specified
	 * - "max": The maximum position value, defaults to MAX_ABSOLUTE_POSITION if not
	 * specified
	 * 
	 * @see CobblelootsMinMaxFilter
	 */
	public static final Codec<CobblelootsMinMaxFilter> MIN_MAX_POSITION_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.INT.optionalFieldOf("min", -MAX_ABSOLUTE_POSITION)
							.forGetter(CobblelootsMinMaxFilter::getMin),
					Codec.INT.optionalFieldOf("max", MAX_ABSOLUTE_POSITION)
							.forGetter(CobblelootsMinMaxFilter::getMax))
					.apply(instance, CobblelootsMinMaxFilter::new));

	/**
	 * Codec for serializing and deserializing CobblelootsMinMaxFilter objects for
	 * light values.
	 * This codec handles the min and max light values for light-based filtering.
	 * 
	 * The codec creates a record with two optional integer fields:
	 * - "min": The minimum light value, defaults to 0 if not specified
	 * - "max": The maximum light value, defaults to 15 if not specified
	 * 
	 * @see CobblelootsMinMaxFilter
	 */
	public static final Codec<CobblelootsMinMaxFilter> MIN_MAX_LIGHT_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.INT.optionalFieldOf("min", MIN_LIGHT)
							.forGetter(CobblelootsMinMaxFilter::getMin),
					Codec.INT.optionalFieldOf("max", MAX_LIGHT)
							.forGetter(CobblelootsMinMaxFilter::getMax))
					.apply(instance, CobblelootsMinMaxFilter::new));

	/**
	 * Codec for serializing and deserializing CobblelootsMinMaxFilter objects
	 * for time values.
	 * This codec handles the min and max time values for time-based filtering.
	 *
	 * The codec creates a record with two optional integer fields:
	 * - "min": The minimum time value, defaults to 0 if not specified
	 * - "max": The maximum time value, defaults to Integer.MAX_VALUE if not
	 * specified
	 *
	 * @see CobblelootsMinMaxFilter
	 */
	public static final Codec<CobblelootsMinMaxFilter> MIN_MAX_TIME_VALUE_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.INT.optionalFieldOf("min", MIN_TICK)
							.forGetter(CobblelootsMinMaxFilter::getMin),
					Codec.INT.optionalFieldOf("max", MAX_TICK)
							.forGetter(CobblelootsMinMaxFilter::getMax))
					.apply(instance, CobblelootsMinMaxFilter::new));

	/**
	 * Codec for serializing and deserializing CobblelootsPositionFilter objects.
	 * This codec handles the position-based filtering for loot balls.
	 *
	 * The codec creates a record with three optional fields:
	 * - "x": A CobblelootsMinMaxFilter for the x-coordinate, defaults to
	 * DEFAULT_MIN_MAX_POSITION if not specified
	 * - "y": A CobblelootsMinMaxFilter for the y-coordinate, defaults to
	 * DEFAULT_MIN_MAX_POSITION if not specified
	 * - "z": A CobblelootsMinMaxFilter for the z-coordinate, defaults to
	 * DEFAULT_MIN_MAX_POSITION if not specified
	 *
	 * @see CobblelootsPositionFilter
	 */
	public static final Codec<CobblelootsPositionFilter> POSITION_FILTER_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					MIN_MAX_POSITION_CODEC.optionalFieldOf("x", DEFAULT_MIN_MAX_POSITION)
							.forGetter(CobblelootsPositionFilter::getX),
					MIN_MAX_POSITION_CODEC.optionalFieldOf("y", DEFAULT_MIN_MAX_POSITION)
							.forGetter(CobblelootsPositionFilter::getY),
					MIN_MAX_POSITION_CODEC.optionalFieldOf("z", DEFAULT_MIN_MAX_POSITION)
							.forGetter(CobblelootsPositionFilter::getZ))
					.apply(instance, CobblelootsPositionFilter::new));

	/**
	 * Codec for serializing and deserializing CobblelootsLightFilter objects.
	 * This codec handles the light-based filtering for loot balls.
	 *
	 * The codec creates a record with two optional fields:
	 * - "block": A CobblelootsMinMaxFilter for block light, defaults to
	 * DEFAULT_MIN_MAX_LIGHT if not specified
	 * - "sky": A CobblelootsMinMaxFilter for sky light, defaults to
	 * DEFAULT_MIN_MAX_LIGHT if not specified
	 *
	 * @see CobblelootsLightFilter
	 */
	public static final Codec<CobblelootsLightFilter> LIGHT_FILTER_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					MIN_MAX_LIGHT_CODEC.optionalFieldOf("block", DEFAULT_MIN_MAX_LIGHT)
							.forGetter(CobblelootsLightFilter::getBlock),
					MIN_MAX_LIGHT_CODEC.optionalFieldOf("sky", DEFAULT_MIN_MAX_LIGHT)
							.forGetter(CobblelootsLightFilter::getSky))
					.apply(instance, CobblelootsLightFilter::new));

	/**
	 * Codec for serializing and deserializing CobblelootsTimeFilter objects.
	 * This codec handles the time-based filtering for loot balls.
	 *
	 * The codec creates a record with two optional fields:
	 * - "value": A CobblelootsMinMaxFilter for the time value, defaults to
	 * DEFAULT_MIN_MAX_TIME if not specified
	 * - "period": An integer representing the period, defaults to 0 if not
	 * specified
	 *
	 * @see CobblelootsTimeFilter
	 */
	public static final Codec<CobblelootsTimeFilter> TIME_FILTER_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					MIN_MAX_TIME_VALUE_CODEC.optionalFieldOf("value", DEFAULT_MIN_MAX_TIME)
							.forGetter(CobblelootsTimeFilter::getValue),
					Codec.INT.optionalFieldOf("period", 0)
							.forGetter(CobblelootsTimeFilter::getPeriod))
					.apply(instance, CobblelootsTimeFilter::new));

	/**
	 * Codec for serializing and deserializing CobblelootsWeatherFilter objects.
	 * This codec handles the weather-based filtering for loot balls.
	 *
	 * The codec creates a record with three optional boolean fields:
	 * - "rain": Indicates if rain is allowed, defaults to true if not specified
	 * - "thunder": Indicates if thunder is allowed, defaults to true if not
	 * specified
	 * - "clear": Indicates if clear weather is allowed, defaults to true if not
	 * specified
	 *
	 * @see CobblelootsWeatherFilter
	 */
	public static final Codec<CobblelootsWeatherFilter> WEATHER_FILTER_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.BOOL.optionalFieldOf("rain", true)
							.forGetter(CobblelootsWeatherFilter::getRain),
					Codec.BOOL.optionalFieldOf("thunder", true)
							.forGetter(CobblelootsWeatherFilter::getThunder),
					Codec.BOOL.optionalFieldOf("clear", true)
							.forGetter(CobblelootsWeatherFilter::getClear))
					.apply(instance, CobblelootsWeatherFilter::new));

	public static final Codec<CobblelootsDateFilter> DATE_FILTER_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.STRING.optionalFieldOf("from", "").forGetter(CobblelootsDateFilter::getFrom),
					Codec.STRING.optionalFieldOf("to", "").forGetter(CobblelootsDateFilter::getTo))
					.apply(instance, CobblelootsDateFilter::new));

	public static final Codec<CobblelootsPokeRodFilter> POKE_ROD_FILTER_CODEC = CobblelootsPokeRodFilter.CODEC;

	/**
	 * Codec for serializing and deserializing CobblelootsSourceFilter objects.
	 * This codec handles the various filters applied to loot balls, including
	 * structure, biome, dimension, block, fluid, position, light, time, and
	 * weather.
	 *
	 * The codec creates a record with the following fields:
	 * - "weight": An integer representing the weight of the filter, defaults to
	 * DEFAULT_WEIGHT if not specified
	 * - "structure": A TagKey for the structure, defaults to EMPTY_STRUCTURE_TAG if
	 * not specified
	 * - "biome": A TagKey for the biome, defaults to EMPTY_BIOME_TAG if not
	 * specified
	 * - "dimension": A ResourceLocation for the dimension, defaults to
	 * EMPTY_LOCATION if not specified
	 * - "block": A CobblelootsBlockFilter for block-based filtering, defaults to
	 * DEFAULT_BLOCK_FILTER if not specified
	 * - "fluid": A TagKey for the fluid, defaults to EMPTY_FLUID_TAG if not
	 * specified
	 * - "position": A CobblelootsPositionFilter for position-based filtering,
	 * defaults to DEFAULT_POSITION_FILTER if not specified
	 * - "light": A CobblelootsLightFilter for light-based filtering, defaults to
	 * DEFAULT_LIGHT_FILTER if not specified
	 * - "time": A CobblelootsTimeFilter for time-based filtering, defaults to
	 * DEFAULT_TIME_FILTER if not specified
	 * - "weather": A CobblelootsWeatherFilter for weather-based filtering, defaults
	 * to DEFAULT_WEATHER_FILTER if not specified
	 *
	 * @see CobblelootsSourceFilter
	 */
	public static final Codec<CobblelootsSourceFilter> LOOT_BALL_SOURCE_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.INT.optionalFieldOf("weight", DEFAULT_WEIGHT)
							.forGetter(CobblelootsSourceFilter::getWeight),
					TagKey.codec(Registries.STRUCTURE)
							.optionalFieldOf("structure",
									CobblelootsDefinitions.EMPTY_STRUCTURE_TAG)
							.forGetter(CobblelootsSourceFilter::getStructure),
					TagKey.codec(Registries.BIOME)
							.optionalFieldOf("biome",
									CobblelootsDefinitions.EMPTY_BIOME_TAG)
							.forGetter(CobblelootsSourceFilter::getBiome),
					ResourceLocation.CODEC.listOf().optionalFieldOf("dimension", List.of())
							.forGetter(CobblelootsSourceFilter::getDimension),
					BLOCK_FILTER_CODEC.optionalFieldOf("block", DEFAULT_BLOCK_FILTER)
							.forGetter(CobblelootsSourceFilter::getBlock),
					TagKey.codec(Registries.FLUID)
							.optionalFieldOf("fluid",
									CobblelootsDefinitions.EMPTY_FLUID_TAG)
							.forGetter(CobblelootsSourceFilter::getFluid),
					POSITION_FILTER_CODEC.optionalFieldOf("position", DEFAULT_POSITION_FILTER)
							.forGetter(CobblelootsSourceFilter::getPosition),
					LIGHT_FILTER_CODEC.optionalFieldOf("light", DEFAULT_LIGHT_FILTER)
							.forGetter(CobblelootsSourceFilter::getLight),
					TIME_FILTER_CODEC.optionalFieldOf("time", DEFAULT_TIME_FILTER)
							.forGetter(CobblelootsSourceFilter::getTime),
					WEATHER_FILTER_CODEC.optionalFieldOf("weather", DEFAULT_WEATHER_FILTER)
							.forGetter(CobblelootsSourceFilter::getWeather),
					DATE_FILTER_CODEC.optionalFieldOf("date", DEFAULT_DATE_FILTER)
							.forGetter(CobblelootsSourceFilter::getDate),
					POKE_ROD_FILTER_CODEC.optionalFieldOf("poke_rod", DEFAULT_POKE_ROD_FILTER)
							.forGetter(CobblelootsSourceFilter::getPokeRod))
					.apply(instance, CobblelootsSourceFilter::new));
}
