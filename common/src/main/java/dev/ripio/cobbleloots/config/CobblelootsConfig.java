package dev.ripio.cobbleloots.config;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.util.io.CobblelootsYamlParser;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public static final String LOOT_BALL_SURVIVAL_DROP_ENABLED = "loot_ball.survival.drop.enabled";
    public static final String LOOT_BALL_DEFAULTS_USES = "loot_ball.defaults.uses";
    public static final String LOOT_BALL_DEFAULTS_MULTIPLIER = "loot_ball.defaults.multiplier";
    public static final String LOOT_BALL_DEFAULTS_XP = "loot_ball.defaults.xp";
    public static final String LOOT_BALL_DEFAULTS_PLAYER_TIMER = "loot_ball.defaults.player_timer";
    public static final String LOOT_BALL_DEFAULTS_DESPAWN_TICK = "loot_ball.defaults.despawn_tick";

    // New config keys for disabled dimensions
    public static final String LOOT_BALL_DISABLED_DIMENSIONS_GENERATION = "loot_ball.disabled.dimensions.generation";
    public static final String LOOT_BALL_DISABLED_DIMENSIONS_SPAWNING = "loot_ball.disabled.dimensions.spawning";
    public static final String LOOT_BALL_DISABLED_DIMENSIONS_FISHING = "loot_ball.disabled.dimensions.fishing";
    public static final String LOOT_BALL_DISABLED_DIMENSIONS_ARCHAEOLOGY = "loot_ball.disabled.dimensions.archaeology";

    private static Map<String, Object> configMap = new HashMap<>();
    private static Map<String, Object> fileMap = new HashMap<>();

    private static boolean isResourceLocationListKey(String key) {
        return key.equals(LOOT_BALL_DISABLED_DIMENSIONS_GENERATION) ||
                key.equals(LOOT_BALL_DISABLED_DIMENSIONS_SPAWNING) ||
                key.equals(LOOT_BALL_DISABLED_DIMENSIONS_FISHING) ||
                key.equals(LOOT_BALL_DISABLED_DIMENSIONS_ARCHAEOLOGY);
    }

    private static Map<String, Object> getDefaultConfig() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put(LOOT_BALL_XP_ENABLED, true);
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
        defaults.put(LOOT_BALL_SURVIVAL_DROP_ENABLED, true);
        defaults.put(LOOT_BALL_DEFAULTS_USES, 1);
        defaults.put(LOOT_BALL_DEFAULTS_MULTIPLIER, 1.0F);
        defaults.put(LOOT_BALL_DEFAULTS_XP, 0);
        defaults.put(LOOT_BALL_DEFAULTS_PLAYER_TIMER, 0L);
        defaults.put(LOOT_BALL_DEFAULTS_DESPAWN_TICK, 0L);

        // Default disabled dimensions are empty lists
        defaults.put(LOOT_BALL_DISABLED_DIMENSIONS_GENERATION, new ArrayList<ResourceLocation>());
        defaults.put(LOOT_BALL_DISABLED_DIMENSIONS_SPAWNING, new ArrayList<ResourceLocation>());
        defaults.put(LOOT_BALL_DISABLED_DIMENSIONS_FISHING, new ArrayList<ResourceLocation>());
        defaults.put(LOOT_BALL_DISABLED_DIMENSIONS_ARCHAEOLOGY, new ArrayList<ResourceLocation>());

        return defaults;
    }

    public static void initConfig() {
        configMap = getDefaultConfig();
        // Try to load config
        if (Files.exists(CONFIG_PATH)) {
            try {
                fileMap = flatten(CobblelootsYamlParser.parse(CONFIG_PATH), "");
                Cobbleloots.LOGGER.info("Config file found, loading values.");
            } catch (IOException e) {
                Cobbleloots.LOGGER.error("Invalid config file, generating new one with defaults.");
            } catch (Exception e) {
                Cobbleloots.LOGGER.error("Error loading config file: {}", e.getMessage());
                Cobbleloots.LOGGER.info("Generating new config file with defaults.");
            }
        } else {
            // File does not exist, create with defaults
            Cobbleloots.LOGGER.info("Config file not found, creating new one with defaults.");
        }
        // Overwrite defaults with file values
        for (String key : fileMap.keySet()) {
            if (configMap.containsKey(key)) {
                Object defaultValue = configMap.get(key);
                Object fileValue = fileMap.get(key);
                try {
                    if (defaultValue instanceof Integer) {
                        configMap.put(key, Integer.parseInt(fileValue.toString()));
                    } else if (defaultValue instanceof Float) {
                        configMap.put(key, Float.parseFloat(fileValue.toString()));
                    } else if (defaultValue instanceof Double) {
                        configMap.put(key, Double.parseDouble(fileValue.toString()));
                    } else if (defaultValue instanceof Long) {
                        configMap.put(key, Long.parseLong(fileValue.toString()));
                    } else if (defaultValue instanceof Boolean) {
                        configMap.put(key, Boolean.parseBoolean(fileValue.toString()));
                    } else if (defaultValue instanceof String) {
                        configMap.put(key, fileValue.toString());
                    } else if (defaultValue instanceof List && isResourceLocationListKey(key)) {
                        if (fileValue instanceof List) {
                            List<ResourceLocation> locationList = new ArrayList<>();
                            for (Object item : (List<?>) fileValue) {
                                if (item instanceof String) {
                                    ResourceLocation location = ResourceLocation.tryParse((String) item);
                                    if (location != null) {
                                        locationList.add(location);
                                    } else {
                                        Cobbleloots.LOGGER.warn(
                                                "Invalid resource location string in list for key {}: {}", key, item);
                                    }
                                } else {
                                    Cobbleloots.LOGGER.warn("Item in list for key {} is not a string: {}", key, item);
                                }
                            }
                            configMap.put(key, locationList);
                        } else if (fileValue instanceof String) {
                            // If it's a single string, parse it as a ResourceLocation
                            ResourceLocation location = ResourceLocation.tryParse(fileValue.toString());
                            if (location != null) {
                                List<ResourceLocation> singleItemList = new ArrayList<>();
                                singleItemList.add(location);
                                configMap.put(key, singleItemList);
                            } else {
                                Cobbleloots.LOGGER.warn("Invalid resource location string for key {}: {}", key,
                                        fileValue);
                            }
                        } else if (fileValue != null) {
                            Cobbleloots.LOGGER.warn("Config key {} expected a list but got {}. Using default value.",
                                    key, fileValue.getClass().getSimpleName());
                        }
                        // If fileValue is null or not a list, the default (empty
                        // List<ResourceLocation>) is kept.
                    } else if (defaultValue != null) {
                        Cobbleloots.LOGGER.warn(
                                "Config key {} has an unexpected type (Expected: {}). Using default value.", key,
                                defaultValue.getClass().getSimpleName());
                    } else { // Should not happen if getDefaultConfig() is comprehensive
                        Cobbleloots.LOGGER.warn(
                                "Config key {} has a null default value. Using file value if available: {}", key,
                                fileValue);
                        if (fileValue != null) {
                            configMap.put(key, fileValue);
                        }
                    }
                } catch (NumberFormatException e) {
                    Cobbleloots.LOGGER.error(
                            "Config key {} has incompatible number format (Value: '{}'). Using default value.",
                            key, fileValue, e);
                } catch (Exception e) { // Catch any other unexpected error during value processing
                    Cobbleloots.LOGGER.error("Error processing config key {} (Value: '{}'). Using default value.", key,
                            fileValue, e);
                }
            }
        }
        // Save config if it was updated
        saveConfig();
        Cobbleloots.LOGGER.info("{} configurations loaded.", configMap.size());
    }

    public static int getIntConfig(String key) {
        Object value = configMap.get(key);
        if (value instanceof Integer)
            return (Integer) value;
        throw new IllegalArgumentException("Config key not found or not an int: " + key);
    }

    public static float getFloatConfig(String key) {
        Object value = configMap.get(key);
        if (value instanceof Float)
            return (Float) value;
        throw new IllegalArgumentException("Config key not found or not a float: " + key);
    }

    public static boolean getBooleanConfig(String key) {
        Object value = configMap.get(key);
        if (value instanceof Boolean)
            return (Boolean) value;
        throw new IllegalArgumentException("Config key not found or not a boolean: " + key);
    }

    public static long getLongConfig(String key) {
        Object value = configMap.get(key);
        if (value instanceof Long)
            return (Long) value;
        throw new IllegalArgumentException("Config key not found or not a long: " + key);
    }

    public static String getStringConfig(String key) {
        Object value = configMap.get(key);
        if (value instanceof String)
            return (String) value;
        throw new IllegalArgumentException("Config key not found or not a string: " + key);
    }

    @SuppressWarnings("unchecked")
    public static List<ResourceLocation> getResourceLocationList(String key) {
        Object value = configMap.get(key);
        if (value instanceof List) {
            // Assuming the list is of ResourceLocation, as per config structure
            // Add runtime check if necessary, though initConfig should ensure this.
            return (List<ResourceLocation>) value;
        }
        throw new IllegalArgumentException("Config key not found or not a resource location list: " + key);
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
            Object value = entry.getValue();
            if (value instanceof Map) {
                writer.write(indentStr + entry.getKey() + ":\n");
                writeYaml(writer, (Map<String, Object>) value, indent + 2);
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<?> listValue = (List<?>) value;
                if (listValue.isEmpty()) {
                    writer.write(indentStr + entry.getKey() + ": []\n");
                } else {
                    // Check if it's a list of ResourceLocation by inspecting the first element.
                    // This assumes homogeneous lists, which is true for ResourceLocation lists in
                    // this config.
                    if (listValue.get(0) instanceof ResourceLocation) {
                        writer.write(indentStr + entry.getKey() + ":\n");
                        for (Object item : listValue) {
                            writer.write(indentStr + "  - " + ((ResourceLocation) item).toString() + "\n");
                        }
                    } else {
                        // Generic list formatting (if other list types were supported)
                        // For this config, we only expect List<ResourceLocation>.
                        // If it's a list but not of ResourceLocation, it's an unexpected state or a
                        // different kind of list.
                        // Defaulting to writing elements' toString() for robustness if other list types
                        // appear.
                        writer.write(indentStr + entry.getKey() + ":\n");
                        for (Object item : listValue) {
                            writer.write(indentStr + "  - " + item.toString() + "\n");
                        }
                    }
                }
            } else {
                writer.write(indentStr + entry.getKey() + ": " + value + "\n");
            }
        }
    }
}
