package dev.ripio.cobbleloots.config;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.util.io.CobblelootsYamlParser;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CobblelootsConfig extends MidnightConfig {
    public static final String CATEGORY_XP = "xp";
    public static final String CATEGORY_BONUS = "bonus";
    public static final String CATEGORY_GENERATION = "generation";
    public static final String CATEGORY_SPAWNING = "spawning";
    public static final String CATEGORY_SURVIVAL = "survival";
    public static final String CATEGORY_DEFAULTS = "defaults";
    public static final String CATEGORY_DISABLED = "disabled";

    // XP
    @Entry(category = CATEGORY_XP)
    public static boolean xp_enabled = true;

    // Bonus
    @Entry(category = CATEGORY_BONUS)
    public static boolean bonus_enabled = true;
    @Entry(category = CATEGORY_BONUS)
    public static float bonus_chance = 0.1F;
    @Entry(category = CATEGORY_BONUS)
    public static float bonus_multiplier = 2F;
    @Entry(category = CATEGORY_BONUS)
    public static boolean bonus_invisible = true;

    // Generation
    @Entry(category = CATEGORY_GENERATION)
    public static boolean generation_enabled = true;
    @Entry(category = CATEGORY_GENERATION)
    public static float generation_chance = 0.0513F;
    @Entry(category = CATEGORY_GENERATION)
    public static int generation_attempts = 2;
    @Entry(category = CATEGORY_GENERATION)
    public static int generation_chunk_cap = 4;

    // Spawning
    @Entry(category = CATEGORY_SPAWNING)
    public static boolean spawning_enabled = true;
    @Entry(category = CATEGORY_SPAWNING)
    public static float spawning_chance = 0.25F;
    @Entry(category = CATEGORY_SPAWNING)
    public static int spawning_cooldown_min = 6000;
    @Entry(category = CATEGORY_SPAWNING)
    public static int spawning_cooldown_max = 36000;
    @Entry(category = CATEGORY_SPAWNING)
    public static boolean spawning_despawn_enabled = true;
    @Entry(category = CATEGORY_SPAWNING)
    public static int spawning_despawn_time = 24000;

    // Survival
    @Entry(category = CATEGORY_SURVIVAL)
    public static boolean survival_drop_enabled = true;
    @Entry(category = CATEGORY_SURVIVAL)
    public static boolean survival_drop_automatic = true;
    @Entry(category = CATEGORY_SURVIVAL)
    public static boolean survival_destroy_looted = false;

    // Defaults
    @Entry(category = CATEGORY_DEFAULTS)
    public static int defaults_uses = 1;
    @Entry(category = CATEGORY_DEFAULTS)
    public static float defaults_multiplier = 1.0F;
    @Entry(category = CATEGORY_DEFAULTS)
    public static int defaults_xp = 0;
    @Entry(category = CATEGORY_DEFAULTS)
    public static long defaults_player_timer = 0L;
    @Entry(category = CATEGORY_DEFAULTS)
    public static long defaults_despawn_tick = 0L;

    // Disabled Dimensions/Features (Lists)
    @Entry(category = CATEGORY_DISABLED)
    public static List<String> disabled_dimensions_generation = new ArrayList<>();
    @Entry(category = CATEGORY_DISABLED)
    public static List<String> disabled_dimensions_spawning = new ArrayList<>();
    @Entry(category = CATEGORY_DISABLED)
    public static List<String> disabled_dimensions_fishing = new ArrayList<>();
    @Entry(category = CATEGORY_DISABLED)
    public static List<String> disabled_dimensions_archaeology = new ArrayList<>();
    @Entry(category = CATEGORY_DISABLED)
    public static List<String> disabled_loot_balls = new ArrayList<>();

    public static void init() {
        init(Path.of("config/cobbleloots/cobbleloots.yaml"));
    }

    public static void init(Path legacyConfigPath) {
        MidnightConfig.init(Cobbleloots.MOD_ID, CobblelootsConfig.class);

        // Migration logic
        if (Files.exists(legacyConfigPath)) {
            Cobbleloots.LOGGER.info("Found legacy configuration file. Attempting migration...");
            try {
                Map<String, Object> legacyMap = CobblelootsYamlParser.parse(legacyConfigPath);
                Map<String, Object> fileMap = flatten(legacyMap, "");

                // Helper to safely get values
                // We manually map old keys to new static fields
                if (fileMap.containsKey("loot_ball.xp.enabled"))
                    xp_enabled = Boolean.parseBoolean(fileMap.get("loot_ball.xp.enabled").toString());

                if (fileMap.containsKey("loot_ball.bonus.enabled"))
                    bonus_enabled = Boolean.parseBoolean(fileMap.get("loot_ball.bonus.enabled").toString());
                if (fileMap.containsKey("loot_ball.bonus.chance"))
                    bonus_chance = Float.parseFloat(fileMap.get("loot_ball.bonus.chance").toString());
                if (fileMap.containsKey("loot_ball.bonus.multiplier"))
                    bonus_multiplier = Float.parseFloat(fileMap.get("loot_ball.bonus.multiplier").toString());
                if (fileMap.containsKey("loot_ball.bonus.invisible"))
                    bonus_invisible = Boolean.parseBoolean(fileMap.get("loot_ball.bonus.invisible").toString());

                if (fileMap.containsKey("loot_ball.generation.enabled"))
                    generation_enabled = Boolean.parseBoolean(fileMap.get("loot_ball.generation.enabled").toString());
                if (fileMap.containsKey("loot_ball.generation.chance"))
                    generation_chance = Float.parseFloat(fileMap.get("loot_ball.generation.chance").toString());
                if (fileMap.containsKey("loot_ball.generation.attempts"))
                    generation_attempts = Integer.parseInt(fileMap.get("loot_ball.generation.attempts").toString());
                if (fileMap.containsKey("loot_ball.generation.chunk_cap"))
                    generation_chunk_cap = Integer.parseInt(fileMap.get("loot_ball.generation.chunk_cap").toString());

                if (fileMap.containsKey("loot_ball.spawning.enabled"))
                    spawning_enabled = Boolean.parseBoolean(fileMap.get("loot_ball.spawning.enabled").toString());
                if (fileMap.containsKey("loot_ball.spawning.chance"))
                    spawning_chance = Float.parseFloat(fileMap.get("loot_ball.spawning.chance").toString());
                if (fileMap.containsKey("loot_ball.spawning.cooldown.min"))
                    spawning_cooldown_min = Integer.parseInt(fileMap.get("loot_ball.spawning.cooldown.min").toString());
                if (fileMap.containsKey("loot_ball.spawning.cooldown.max"))
                    spawning_cooldown_max = Integer.parseInt(fileMap.get("loot_ball.spawning.cooldown.max").toString());

                if (fileMap.containsKey("loot_ball.spawning.despawn.enabled"))
                    spawning_despawn_enabled = Boolean
                            .parseBoolean(fileMap.get("loot_ball.spawning.despawn.enabled").toString());
                if (fileMap.containsKey("loot_ball.spawning.despawn.time"))
                    spawning_despawn_time = Integer.parseInt(fileMap.get("loot_ball.spawning.despawn.time").toString());

                if (fileMap.containsKey("loot_ball.survival.drop.enabled"))
                    survival_drop_enabled = Boolean
                            .parseBoolean(fileMap.get("loot_ball.survival.drop.enabled").toString());
                if (fileMap.containsKey("loot_ball.survival.drop.automatic"))
                    survival_drop_automatic = Boolean
                            .parseBoolean(fileMap.get("loot_ball.survival.drop.automatic").toString());
                if (fileMap.containsKey("loot_ball.survival.destroy_looted"))
                    survival_destroy_looted = Boolean
                            .parseBoolean(fileMap.get("loot_ball.survival.destroy_looted").toString());

                if (fileMap.containsKey("loot_ball.defaults.uses"))
                    defaults_uses = Integer.parseInt(fileMap.get("loot_ball.defaults.uses").toString());
                if (fileMap.containsKey("loot_ball.defaults.multiplier"))
                    defaults_multiplier = Float.parseFloat(fileMap.get("loot_ball.defaults.multiplier").toString());
                if (fileMap.containsKey("loot_ball.defaults.xp"))
                    defaults_xp = Integer.parseInt(fileMap.get("loot_ball.defaults.xp").toString());
                if (fileMap.containsKey("loot_ball.defaults.player_timer"))
                    defaults_player_timer = Long.parseLong(fileMap.get("loot_ball.defaults.player_timer").toString());
                if (fileMap.containsKey("loot_ball.defaults.despawn_tick"))
                    defaults_despawn_tick = Long.parseLong(fileMap.get("loot_ball.defaults.despawn_tick").toString());

                // Handle Lists (converting ResourceLocation strings if needed, though they are
                // stored as Strings now)
                if (fileMap.containsKey("loot_ball.disabled.loot_balls"))
                    disabled_loot_balls = parseStringList(fileMap.get("loot_ball.disabled.loot_balls"));

                if (fileMap.containsKey("loot_ball.disabled.dimensions.generation"))
                    disabled_dimensions_generation = parseStringList(
                            fileMap.get("loot_ball.disabled.dimensions.generation"));

                if (fileMap.containsKey("loot_ball.disabled.dimensions.spawning"))
                    disabled_dimensions_spawning = parseStringList(
                            fileMap.get("loot_ball.disabled.dimensions.spawning"));

                if (fileMap.containsKey("loot_ball.disabled.dimensions.fishing"))
                    disabled_dimensions_fishing = parseStringList(fileMap.get("loot_ball.disabled.dimensions.fishing"));

                if (fileMap.containsKey("loot_ball.disabled.dimensions.archaeology"))
                    disabled_dimensions_archaeology = parseStringList(
                            fileMap.get("loot_ball.disabled.dimensions.archaeology"));

                Cobbleloots.LOGGER.info("Legacy config migrated successfully.");

                // Rename old file
                Files.move(legacyConfigPath, legacyConfigPath.resolveSibling(legacyConfigPath.getFileName() + ".old"));

                // Save new config
                MidnightConfig.write(Cobbleloots.MOD_ID);

            } catch (Exception e) {
                Cobbleloots.LOGGER.error("Error migrating legacy config: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static List<String> parseStringList(Object obj) {
        if (obj instanceof List) {
            List<String> list = new ArrayList<>();
            for (Object item : (List<?>) obj) {
                list.add(item.toString());
            }
            return list;
        }
        return new ArrayList<>();
    }

    // Helper from old class to flatten map for easier key access
    private static Map<String, Object> flatten(Map<String, Object> map, String prefix) {
        Map<String, Object> flat = new java.util.HashMap<>();
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

    // Adaptor methods for code compatibility - keeping old getters
    public static boolean getBooleanConfig(String key) {
        switch (key) {
            case "loot_ball.xp.enabled":
                return xp_enabled;
            case "loot_ball.bonus.enabled":
                return bonus_enabled;
            case "loot_ball.bonus.invisible":
                return bonus_invisible;
            case "loot_ball.generation.enabled":
                return generation_enabled;
            case "loot_ball.spawning.enabled":
                return spawning_enabled;
            case "loot_ball.spawning.despawn.enabled":
                return spawning_despawn_enabled;
            case "loot_ball.survival.drop.enabled":
                return survival_drop_enabled;
            case "loot_ball.survival.drop.automatic":
                return survival_drop_automatic;
            case "loot_ball.survival.destroy_looted":
                return survival_destroy_looted;
            default:
                throw new IllegalArgumentException("Unknown boolean config key: " + key);
        }
    }

    public static int getIntConfig(String key) {
        switch (key) {
            case "loot_ball.generation.attempts":
                return generation_attempts;
            case "loot_ball.generation.chunk_cap":
                return generation_chunk_cap;
            case "loot_ball.spawning.cooldown.min":
                return spawning_cooldown_min;
            case "loot_ball.spawning.cooldown.max":
                return spawning_cooldown_max;
            case "loot_ball.spawning.despawn.time":
                return spawning_despawn_time;
            case "loot_ball.defaults.uses":
                return defaults_uses;
            case "loot_ball.defaults.xp":
                return defaults_xp;
            default:
                throw new IllegalArgumentException("Unknown int config key: " + key);
        }
    }

    public static float getFloatConfig(String key) {
        switch (key) {
            case "loot_ball.bonus.chance":
                return bonus_chance;
            case "loot_ball.bonus.multiplier":
                return bonus_multiplier;
            case "loot_ball.generation.chance":
                return generation_chance;
            case "loot_ball.spawning.chance":
                return spawning_chance;
            case "loot_ball.defaults.multiplier":
                return defaults_multiplier;
            default:
                throw new IllegalArgumentException("Unknown float config key: " + key);
        }
    }

    public static long getLongConfig(String key) {
        switch (key) {
            case "loot_ball.defaults.player_timer":
                return defaults_player_timer;
            case "loot_ball.defaults.despawn_tick":
                return defaults_despawn_tick;
            default:
                throw new IllegalArgumentException("Unknown long config key: " + key);
        }
    }

    public static List<ResourceLocation> getResourceLocationList(String key) {
        List<String> stringList;
        switch (key) {
            case "loot_ball.disabled.dimensions.generation":
                stringList = disabled_dimensions_generation;
                break;
            case "loot_ball.disabled.dimensions.spawning":
                stringList = disabled_dimensions_spawning;
                break;
            case "loot_ball.disabled.dimensions.fishing":
                stringList = disabled_dimensions_fishing;
                break;
            case "loot_ball.disabled.dimensions.archaeology":
                stringList = disabled_dimensions_archaeology;
                break;
            default:
                throw new IllegalArgumentException("Unknown config key for ResourceLocation list: " + key);
        }
        List<ResourceLocation> list = new ArrayList<>();
        for (String s : stringList) {
            try {
                list.add(ResourceLocation.parse(s));
            } catch (Exception e) {
                Cobbleloots.LOGGER.warn("Invalid ResourceLocation in config {}: {}", key, s);
            }
        }
        return list;
    }

    public static List<String> getStringList(String key) {
        if ("loot_ball.disabled.loot_balls".equals(key)) {
            return disabled_loot_balls;
        }
        throw new IllegalArgumentException("Unknown config key for String list: " + key);
    }
}
