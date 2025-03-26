package dev.ripio.cobbleloots.config;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.FileConfig;
import dev.ripio.cobbleloots.Cobbleloots;

public class CobblelootsConfigManager {
  // Config access
  public static final FileConfig CONFIG = FileConfig.of("config/cobbleloots/cobbleloots.yaml");
  // Loot Ball Bonus
  public static final String LOOT_BALL_BONUS_ENABLED = "loot_ball.bonus.enabled";
  public static final String LOOT_BALL_BONUS_CHANCE = "loot_ball.bonus.chance";
  public static final String LOOT_BALL_BONUS_MULTIPLIER = "loot_ball.bonus.multiplier";
  public static final String LOOT_BALL_BONUS_INVISIBLE = "loot_ball.bonus.invisible";
  // Loot Ball Generation
  public static final String LOOT_BALL_GENERATION_ENABLED = "loot_ball.generation.enabled";
  public static final String LOOT_BALL_GENERATION_CHANCE = "loot_ball.generation.chance";
  public static final String LOOT_BALL_GENERATION_ATTEMPTS = "loot_ball.generation.attempts";
  public static final String LOOT_BALL_GENERATION_CHUNK_CAP = "loot_ball.generation.chunk_cap";
  // Loot Ball Spawning
  public static final String LOOT_BALL_SPAWNING_ENABLED = "loot_ball.spawning.enabled";
  public static final String LOOT_BALL_SPAWN_CHANCE = "loot_ball.spawning.chance";
  public static final String LOOT_BALL_SPAWNING_COOLDOWN_MIN = "loot_ball.spawning.cooldown.min";
  public static final String LOOT_BALL_SPAWNING_COOLDOWN_MAX = "loot_ball.spawning.cooldown.max";
  // Loot Ball Despawn
  public static final String LOOT_BALL_DESPAWN_ENABLED = "loot_ball.despawn.enabled";
  public static final String LOOT_BALL_DESPAWN_TIME = "loot_ball.despawn.time";

  public static void initConfig() {
    // Load the config file
    if (!CONFIG.getFile().exists()) {
      // Create folder
      if (CONFIG.getFile().getParentFile().mkdirs()) {
        Cobbleloots.LOGGER.warn("Config folder does not exist, creating a new one");
      }
      // Create the config file
      Cobbleloots.LOGGER.warn("Config file does not exist, creating a new one");
      CONFIG.load();
      getConfigSpec().correct(CONFIG);
      CONFIG.save();
      return;
    }
    CONFIG.load();
    // Get the config spec
    ConfigSpec spec = getConfigSpec();
    // Check if the config file is correct
    ConfigSpec.CorrectionListener listener = (action, path, incorrectValue, correctedValue) -> {
      Cobbleloots.LOGGER.warn("Config value {}={} is incorrect, using default value {}", path, incorrectValue, correctedValue);
    };
    int numberOfErrors = spec.correct(CONFIG, listener);
    Cobbleloots.LOGGER.info("Config loaded with {} errors", numberOfErrors);
    // Save the config file
    CONFIG.save();
  }

  public static int getIntConfig(String key) {
    return CONFIG.get(key);
  }

  public static float getFloatConfig(String key) {
    double value = CONFIG.get(key);
    return (float) value;
  }

  public static boolean getBooleanConfig(String key) {
    return CONFIG.get(key);
  }

  private static ConfigSpec getConfigSpec() {
    ConfigSpec spec = new ConfigSpec();
    // Loot Ball Bonus
    spec.define(LOOT_BALL_BONUS_ENABLED, true);
    spec.defineInRange(LOOT_BALL_BONUS_CHANCE, 0.1F, 0F, 1F);
    spec.defineInRange(LOOT_BALL_BONUS_MULTIPLIER, 2F, 0F, Float.MAX_VALUE);
    spec.define(LOOT_BALL_BONUS_INVISIBLE, true);
    // Loot Ball Generation
    spec.define(LOOT_BALL_GENERATION_ENABLED, true);
    spec.defineInRange(LOOT_BALL_GENERATION_CHANCE, 0.0625F, 0F, 1F);
    spec.defineInRange(LOOT_BALL_GENERATION_ATTEMPTS, 2, 1, Integer.MAX_VALUE);
    spec.defineInRange(LOOT_BALL_GENERATION_CHUNK_CAP, 4, 1, Integer.MAX_VALUE);
    // Loot Ball Spawning
    spec.define(LOOT_BALL_SPAWNING_ENABLED, true);
    spec.defineInRange(LOOT_BALL_SPAWN_CHANCE, 0.25F, 0F, 1F);
    spec.defineInRange(LOOT_BALL_SPAWNING_COOLDOWN_MIN, 6000, 0, Integer.MAX_VALUE);
    spec.defineInRange(LOOT_BALL_SPAWNING_COOLDOWN_MAX, 36000, 0, Integer.MAX_VALUE);
    // Loot Ball Despawn
    spec.define(LOOT_BALL_DESPAWN_ENABLED, true);
    spec.defineInRange(LOOT_BALL_DESPAWN_TIME, 24000, 0, Integer.MAX_VALUE);
    return spec;
  }

}
