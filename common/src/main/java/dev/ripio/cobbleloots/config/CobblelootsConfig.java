package dev.ripio.cobbleloots.config;

import dev.ripio.cobbleloots.config.custom.CobblelootsConfigEntry;
import dev.ripio.cobbleloots.config.custom.CobblelootsMinMaxConfigEntry;

public class CobblelootsConfig {
  // Loot Ball Bonus
  public static final CobblelootsConfigEntry<Boolean> LOOT_BALL_BONUS_ENABLED = create(
      "loot_ball.bonus.enabled",
      true,
      "Whether loot balls should give bonus items");
  public static final CobblelootsMinMaxConfigEntry<Float> LOOT_BALL_BONUS_CHANCE = create(
      "loot_ball.bonus.chance",
      0.1F,
      0.0F,
      1.0F,
      "The chance that a loot ball will give a bonus item");
  public static final CobblelootsMinMaxConfigEntry<Float> LOOT_BALL_BONUS_MULTIPLIER = create(
      "loot_ball.bonus.multiplier",
      2F,
      0.0F,
      Float.MAX_VALUE,
      "The multiplier for the bonus item");
  public static final CobblelootsConfigEntry<Boolean> LOOT_BALL_BONUS_INVISIBLE = create(
      "loot_ball.bonus.invisible",
      true,
      "Whether the bonus loot ball should be invisible");

  // Loot Ball Generation
  public static final CobblelootsConfigEntry<Boolean> LOOT_BALL_GENERATION_ENABLED = create(
      "loot_ball.generation.enabled",
      true,
      "Whether loot balls should be generated in the world");
  public static final CobblelootsMinMaxConfigEntry<Float> LOOT_BALL_GENERATION_CHANCE = create(
      "loot_ball.generation.chance",
      0.0625F,
      0F,
      1F,
      "The chance that a loot ball will generate in a chunk");
  public static final CobblelootsMinMaxConfigEntry<Integer> LOOT_BALL_GENERATION_ATTEMPTS = create(
      "loot_ball.generation.attempts",
      2,
      1,
      Integer.MAX_VALUE,
      "The amount of attempts to generate a loot ball in a chunk");

  // Loot Ball Spawning
  public static final CobblelootsConfigEntry<Boolean> LOOT_BALL_SPAWNING_ENABLED = create(
      "loot_ball.spawning.enabled",
      true,
      "Whether loot balls should spawn near players");
  public static final CobblelootsMinMaxConfigEntry<Long> LOOT_BALL_SPAWN_COOLDOWN_MIN = create(
      "loot_ball.spawning.cooldown.min",
      6000L,
      0L,
      Long.MAX_VALUE,
      "The minimum amount of ticks between loot ball spawns");
  public static final CobblelootsMinMaxConfigEntry<Long> LOOT_BALL_SPAWN_COOLDOWN_MAX = create(
      "loot_ball.spawning.cooldown.max",
      36000L,
      0L,
      Long.MAX_VALUE,
      "The maximum amount of ticks between loot ball spawns");
  public static final CobblelootsConfigEntry<Long> LOOT_BALL_DESPAWN_TIME = create(
      "loot_ball.despawn_time",
      24000L,
      "The amount of ticks before a loot ball despawns");
  public static final CobblelootsMinMaxConfigEntry<Float> LOOT_BALL_SPAWN_CHANCE = create(
      "loot_ball.spawn_chance",
      0.25F,
      0.0F,
      1.0F,
      "The chance that a loot ball will spawn near a player");


  // Methods to generate config entries
  private static <T> CobblelootsConfigEntry<T> create(String key, T defaultValue, String comment) {
    return new CobblelootsConfigEntry<>(key, defaultValue, comment);
  }

  private static <T> CobblelootsMinMaxConfigEntry<T> create(String key, T defaultValue, T minValue, T maxValue, String comment) {
    if (defaultValue instanceof Number && minValue instanceof Number && maxValue instanceof Number) {
      return new CobblelootsMinMaxConfigEntry<>(key, defaultValue, minValue, maxValue, comment);
    } else {
      throw new IllegalArgumentException("Unsupported type for min/max config entry");
    }
  }

}
