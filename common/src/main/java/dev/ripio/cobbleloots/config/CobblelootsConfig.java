package dev.ripio.cobbleloots.config;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.util.io.CobblelootsYamlParser;

import java.io.IOException;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CobblelootsConfig {
    public static final Path CONFIG_PATH = Path.of("config/cobbleloots/cobbleloots.yaml");

    // Config keys
    public static final String LOOT_BALL_XP_ENABLED = "loot_ball.xp.enabled";
    public static final String LOOT_BALL_XP_AMOUNT = "loot_ball.xp.amount";
    public static final String LOOT_BALL_BONUS_ENABLED = "loot_ball.bonus.enabled";
    public static final String LOOT_BALL_BONUS_CHANCE = "loot_ball.bonus.chance";
    public static final String LOOT_BALL_BONUS_MULTIPLIER = "loot_ball.bonus.multiplier";
    public static final String LOOT_BALL_BONUS_INVISIBLE = "loot_ball.bonus.invisible";
    public static final String LOOT_BALL_GENERATION_ENABLED = "loot_ball.generation.enabled";
    public static final String LOOT_BALL_GENERATION_CHANCE = "loot_ball.generation.chance";
    public static final String LOOT_BALL_GENERATION_ATTEMPTS = "loot_ball.generation.attempts";
    public static final String LOOT_BALL_GENERATION_CHUNK_CAP = "loot_ball.generation.chunk_cap";
    public static final String LOOT_BALL_SPAWNING_ENABLED = "loot_ball.spawning.enabled";
    public static final String LOOT_BALL_SPAWNING_CHANCE = "loot_ball.spawning.chance";
    public static final String LOOT_BALL_SPAWNING_COOLDOWN_MIN = "loot_ball.spawning.cooldown.min";
    public static final String LOOT_BALL_SPAWNING_COOLDOWN_MAX = "loot_ball.spawning.cooldown.max";
    public static final String LOOT_BALL_SPAWNING_DESPAWN_ENABLED = "loot_ball.spawning.despawn.enabled";
    public static final String LOOT_BALL_SPAWNING_DESPAWN_TIME = "loot_ball.spawning.despawn.time";

    private static Map<String, Object> configMap = new HashMap<>();
    private static Map<String, Object> fileMap = new HashMap<>();

    private static Map<String, Object> getDefaultConfig() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put(LOOT_BALL_XP_ENABLED, true);
        defaults.put(LOOT_BALL_XP_AMOUNT, 5);
        defaults.put(LOOT_BALL_BONUS_ENABLED, true);
        defaults.put(LOOT_BALL_BONUS_CHANCE, 0.1F);
        defaults.put(LOOT_BALL_BONUS_MULTIPLIER, 2F);
        defaults.put(LOOT_BALL_BONUS_INVISIBLE, true);
        defaults.put(LOOT_BALL_GENERATION_ENABLED, true);
        defaults.put(LOOT_BALL_GENERATION_CHANCE, 0.0513F);
        defaults.put(LOOT_BALL_GENERATION_ATTEMPTS, 2);
        defaults.put(LOOT_BALL_GENERATION_CHUNK_CAP, 4);
        defaults.put(LOOT_BALL_SPAWNING_ENABLED, true);
        defaults.put(LOOT_BALL_SPAWNING_CHANCE, 0.25F);
        defaults.put(LOOT_BALL_SPAWNING_COOLDOWN_MIN, 6000);
        defaults.put(LOOT_BALL_SPAWNING_COOLDOWN_MAX, 36000);
        defaults.put(LOOT_BALL_SPAWNING_DESPAWN_ENABLED, true);
        defaults.put(LOOT_BALL_SPAWNING_DESPAWN_TIME, 24000);
        return defaults;
    }

    public static void initConfig() {
        boolean needsUpdate = false;
        configMap = getDefaultConfig();
        // Try to load config
        if (Files.exists(CONFIG_PATH)) {
            try {
                fileMap = flatten(CobblelootsYamlParser.parse(CONFIG_PATH), "");
            } catch (IOException e) {
                Cobbleloots.LOGGER.error("Invalid config file, generating new one with defaults.");
            }
        } else {
            // File does not exist, create with defaults
            Cobbleloots.LOGGER.info("Config file not found, creating new one with defaults.");
        }
        // Overwrite defaults with file values
        for (String key : fileMap.keySet()) {
            if (configMap.containsKey(key)) {
                // Check if the type is compatible
                Object defaultValue = configMap.get(key);
                Object fileValue = fileMap.get(key);
                // Try to cast the value to the default type
                try {
                  switch (defaultValue) {
                    case Integer i -> configMap.put(key, Integer.parseInt(fileValue.toString()));
                    case Float v -> configMap.put(key, Float.parseFloat(fileValue.toString()));
                    case Double v -> configMap.put(key, Double.parseDouble(fileValue.toString()));
                    case Long l -> configMap.put(key, Long.parseLong(fileValue.toString()));
                    case Boolean b -> configMap.put(key, Boolean.parseBoolean(fileValue.toString()));
                    case null, default -> configMap.put(key, fileValue);
                  }
                } catch (ClassCastException | NumberFormatException e) {
                    Cobbleloots.LOGGER.error("Config key {} has incompatible type (Expected: {}). Using default value.", key, defaultValue.getClass().getSimpleName());
                }
            }
        }
        // Save config if it was updated
        saveConfig();
        Cobbleloots.LOGGER.info("{} configurations loaded.", configMap.size());
    }

    public static int getIntConfig(String key) {
        Object value = configMap.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);
        throw new IllegalArgumentException("Config key not found or not an int: " + key);
    }

    public static float getFloatConfig(String key) {
        Object value = configMap.get(key);
        if (value instanceof Number) return ((Number) value).floatValue();
        if (value instanceof String) return Float.parseFloat((String) value);
        throw new IllegalArgumentException("Config key not found or not a float: " + key);
    }

    public static boolean getBooleanConfig(String key) {
        Object value = configMap.get(key);
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        throw new IllegalArgumentException("Config key not found or not a boolean: " + key);
    }

    private static void saveConfig() {
        // Write configMap as YAML
        try {
            if (!Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
                Map<String, Object> nested = unflatten(configMap);
                writeYaml(writer, nested, 0);
            }
        } catch (IOException | ClassCastException e) {
            Cobbleloots.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    // Flattens nested map to dot notation
    private static Map<String, Object> flatten(Map<String, Object> map, String prefix) {
        Map<String, Object> flat = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flat.putAll(flatten((Map<String, Object>) entry.getValue(), key));
            } else {
                flat.put(key, entry.getValue());
            }
        }
        return flat;
    }

    // Unflattens dot notation map to nested map
    static Map<String, Object> unflatten(Map<String, Object> flat) {
        Map<String, Object> nested = new HashMap<>();
        for (Map.Entry<String, Object> entry : flat.entrySet()) {
            String[] parts = entry.getKey().split("\\.");
            Map<String, Object> current = nested;
            for (int i = 0; i < parts.length - 1; i++) {
                current = (Map<String, Object>) current.computeIfAbsent(parts[i], k -> new HashMap<>());
            }
            current.put(parts[parts.length - 1], entry.getValue());
        }
        return nested;
    }

    // Writes a nested map as YAML
    private static void writeYaml(BufferedWriter writer, Map<String, Object> map, int indent) throws IOException {
        String indentStr = " ".repeat(indent);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                writer.write(indentStr + entry.getKey() + ":\n");
                writeYaml(writer, (Map<String, Object>) entry.getValue(), indent + 2);
            } else {
                writer.write(indentStr + entry.getKey() + ": " + entry.getValue() + "\n");
            }
        }
    }
}
