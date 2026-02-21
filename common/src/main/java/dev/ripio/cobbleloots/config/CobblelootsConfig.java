package dev.ripio.cobbleloots.config;

import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.util.io.CobblelootsYamlParser;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CobblelootsConfig extends MidnightConfig {
    // Categories
    public static final String CATEGORY_GAMEPLAY = "gameplay";
    public static final String CATEGORY_CREATIVE = "creative";
    public static final String CATEGORY_SOURCES = "sources";

    ////////////////
    /// Gameplay ///
    ////////////////

    @Comment(category = CATEGORY_GAMEPLAY, centered = true)
    public static String gameplay_comment;

    // Loot Ball
    @Comment(category = CATEGORY_GAMEPLAY, centered = true)
    public static String loot_ball_comment;
    @Entry(category = CATEGORY_GAMEPLAY)
    public static CobblelootsLootBallEmptyBehavior loot_ball_empty_behavior = CobblelootsLootBallEmptyBehavior.DROP_AUTOMATIC;
    @Entry(category = CATEGORY_GAMEPLAY)
    public static float loot_ball_bonus_chance = 0.1F;
    @Entry(category = CATEGORY_GAMEPLAY)
    public static float loot_ball_bonus_multiplier = 2.0F;
    @Entry(category = CATEGORY_GAMEPLAY)
    public static boolean loot_ball_bonus_invisible = true;
    @Entry(category = CATEGORY_GAMEPLAY)
    public static boolean loot_ball_effects_enabled = true;
    @Entry(category = CATEGORY_GAMEPLAY)
    public static boolean loot_ball_xp_enabled = true;

    // TODO: Multiplayer
    @Comment(category = CATEGORY_GAMEPLAY, centered = true)
    public static String multiplayer_comment;

    // Data pack
    @Comment(category = CATEGORY_GAMEPLAY, centered = true)
    public static String data_pack_comment;
    @Entry(category = CATEGORY_GAMEPLAY)
    public static List<String> data_pack_disabled_loot_balls = new ArrayList<>();

    ////////////////
    /// Creative ///
    ////////////////

    @Comment(category = CATEGORY_CREATIVE, centered = true)
    public static String creative_comment;

    // Loot Ball
    @Comment(category = CATEGORY_CREATIVE, centered = true)
    public static String creative_loot_ball_comment;
    @Entry(category = CATEGORY_CREATIVE)
    public static int loot_ball_default_xp = 0;
    @Entry(category = CATEGORY_CREATIVE)
    public static int loot_ball_default_uses = 1;
    @Entry(category = CATEGORY_CREATIVE)
    public static float loot_ball_default_multiplier = 1.0F;
    @Entry(category = CATEGORY_CREATIVE)
    public static int loot_ball_default_player_cooldown = 0;
    @Entry(category = CATEGORY_CREATIVE)
    public static int loot_ball_default_despawn_tick = 0;

    ///////////////
    /// Sources ///
    ///////////////

    @Comment(category = CATEGORY_SOURCES, centered = true)
    public static String sources_comment;

    // Generation
    @Comment(category = CATEGORY_SOURCES, centered = true)
    public static String generation_comment;
    @Entry(category = CATEGORY_SOURCES)
    public static boolean generation_enabled = true;
    @Entry(category = CATEGORY_SOURCES)
    public static float generation_chance = 0.0513F;
    @Entry(category = CATEGORY_SOURCES)
    public static int generation_attempts_per_chunk = 2;
    @Entry(category = CATEGORY_SOURCES)
    public static int generation_chunk_cap = 4;
    @Entry(category = CATEGORY_SOURCES, idMode = 1)
    public static List<ResourceLocation> generation_disabled_dimensions = new ArrayList<>();

    // Spawning
    @Comment(category = CATEGORY_SOURCES, centered = true)
    public static String spawning_comment;
    @Entry(category = CATEGORY_SOURCES)
    public static boolean spawning_enabled = true;
    @Entry(category = CATEGORY_SOURCES)
    public static float spawning_chance = 0.25F;
    @Entry(category = CATEGORY_SOURCES)
    public static int spawning_cooldown_min = 6000;
    @Entry(category = CATEGORY_SOURCES)
    public static int spawning_cooldown_max = 36000;
    @Entry(category = CATEGORY_SOURCES)
    public static int spawning_despawn_time = 24000;
    @Entry(category = CATEGORY_SOURCES, idMode = 1)
    public static List<ResourceLocation> spawning_disabled_dimensions = new ArrayList<>();

    // Fishing
    @Comment(category = CATEGORY_SOURCES, centered = true)
    public static String fishing_comment;
    @Entry(category = CATEGORY_SOURCES)
    public static boolean fishing_enabled = true;
    @Entry(category = CATEGORY_SOURCES)
    public static float fishing_chance = 0.01F;
    @Entry(category = CATEGORY_SOURCES)
    public static float fishing_luck_of_the_sea_multiplier = 1.25F;
    @Entry(category = CATEGORY_SOURCES)
    public static int fishing_despawn_time = 24000;
    @Entry(category = CATEGORY_SOURCES, idMode = 1)
    public static List<ResourceLocation> fishing_disabled_dimensions = new ArrayList<>();

    // TODO: Archaeology (future)
    @Comment(category = CATEGORY_SOURCES, centered = true)
    public static String archaeology_comment;
    @Entry(category = CATEGORY_SOURCES, idMode = 1)
    public static List<ResourceLocation> archaeology_disabled_dimensions = new ArrayList<>();

    /////////////////
    /// Migration ///
    /////////////////

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
                    loot_ball_xp_enabled = Boolean.parseBoolean(fileMap.get("loot_ball.xp.enabled").toString());
                if (fileMap.containsKey("loot_ball.bonus.chance")) {
                    loot_ball_bonus_chance = Float.parseFloat(fileMap.get("loot_ball.bonus.chance").toString());
                    if (fileMap.containsKey("loot_ball.bonus.enabled"))
                        loot_ball_bonus_chance = Boolean.parseBoolean(fileMap.get("loot_ball.bonus.enabled").toString())
                                ? loot_ball_bonus_chance
                                : 0;
                }
                if (fileMap.containsKey("loot_ball.bonus.multiplier"))
                    loot_ball_bonus_multiplier = Float.parseFloat(fileMap.get("loot_ball.bonus.multiplier").toString());
                if (fileMap.containsKey("loot_ball.bonus.invisible"))
                    loot_ball_bonus_invisible = Boolean
                            .parseBoolean(fileMap.get("loot_ball.bonus.invisible").toString());

                if (fileMap.containsKey("loot_ball.generation.enabled"))
                    generation_enabled = Boolean.parseBoolean(fileMap.get("loot_ball.generation.enabled").toString());
                if (fileMap.containsKey("loot_ball.generation.chance"))
                    generation_chance = Float.parseFloat(fileMap.get("loot_ball.generation.chance").toString());
                if (fileMap.containsKey("loot_ball.generation.attempts"))
                    generation_attempts_per_chunk = Integer
                            .parseInt(fileMap.get("loot_ball.generation.attempts").toString());
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
                if (fileMap.containsKey("loot_ball.spawning.despawn.time")) {
                    spawning_despawn_time = Integer.parseInt(fileMap.get("loot_ball.spawning.despawn.time").toString());
                    if (fileMap.containsKey("loot_ball.spawning.despawn.enabled")) {
                        spawning_despawn_time = Boolean.parseBoolean(
                                fileMap.get("loot_ball.spawning.despawn.enabled").toString()) ? spawning_despawn_time
                                        : 0;
                    }
                }

                // Migrate old boolean keys to new enum
                {
                    boolean dropEnabled = true;
                    boolean dropAutomatic = true;
                    boolean destroyEmpty = false;
                    if (fileMap.containsKey("loot_ball.survival.drop.enabled"))
                        dropEnabled = Boolean.parseBoolean(fileMap.get("loot_ball.survival.drop.enabled").toString());
                    if (fileMap.containsKey("loot_ball.survival.drop.automatic"))
                        dropAutomatic = Boolean
                                .parseBoolean(fileMap.get("loot_ball.survival.drop.automatic").toString());
                    if (fileMap.containsKey("loot_ball.survival.destroy_looted"))
                        destroyEmpty = Boolean
                                .parseBoolean(fileMap.get("loot_ball.survival.destroy_looted").toString());

                    if (destroyEmpty) {
                        loot_ball_empty_behavior = CobblelootsLootBallEmptyBehavior.DESTROY;
                    } else if (dropEnabled && dropAutomatic) {
                        loot_ball_empty_behavior = CobblelootsLootBallEmptyBehavior.DROP_AUTOMATIC;
                    } else if (dropEnabled) {
                        loot_ball_empty_behavior = CobblelootsLootBallEmptyBehavior.DROP_MANUAL;
                    } else {
                        loot_ball_empty_behavior = CobblelootsLootBallEmptyBehavior.KEEP;
                    }
                }

                if (fileMap.containsKey("loot_ball.defaults.uses"))
                    loot_ball_default_uses = Integer.parseInt(fileMap.get("loot_ball.defaults.uses").toString());
                if (fileMap.containsKey("loot_ball.defaults.multiplier"))
                    loot_ball_default_multiplier = Float
                            .parseFloat(fileMap.get("loot_ball.defaults.multiplier").toString());
                if (fileMap.containsKey("loot_ball.defaults.xp"))
                    loot_ball_default_xp = Integer.parseInt(fileMap.get("loot_ball.defaults.xp").toString());
                if (fileMap.containsKey("loot_ball.defaults.player_timer"))
                    loot_ball_default_player_cooldown = Integer
                            .parseInt(fileMap.get("loot_ball.defaults.player_timer").toString());
                if (fileMap.containsKey("loot_ball.defaults.despawn_tick"))
                    loot_ball_default_despawn_tick = Integer
                            .parseInt(fileMap.get("loot_ball.defaults.despawn_tick").toString());
                if (fileMap.containsKey("loot_ball.defaults.effects_enabled"))
                    loot_ball_effects_enabled = Boolean
                            .parseBoolean(fileMap.get("loot_ball.defaults.effects_enabled").toString());

                // Handle Lists
                if (fileMap.containsKey("loot_ball.disabled.loot_balls"))
                    data_pack_disabled_loot_balls = parseStringList(
                            fileMap.get("loot_ball.disabled.loot_balls"));

                if (fileMap.containsKey("loot_ball.disabled.dimensions.generation"))
                    generation_disabled_dimensions = parseResourceLocationList(
                            fileMap.get("loot_ball.disabled.dimensions.generation"));

                if (fileMap.containsKey("loot_ball.disabled.dimensions.spawning"))
                    spawning_disabled_dimensions = parseResourceLocationList(
                            fileMap.get("loot_ball.disabled.dimensions.spawning"));

                if (fileMap.containsKey("loot_ball.disabled.dimensions.fishing"))
                    fishing_disabled_dimensions = parseResourceLocationList(
                            fileMap.get("loot_ball.disabled.dimensions.fishing"));

                if (fileMap.containsKey("loot_ball.disabled.dimensions.archaeology"))
                    archaeology_disabled_dimensions = parseResourceLocationList(
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

    private static List<ResourceLocation> parseResourceLocationList(Object obj) {
        if (obj instanceof List) {
            List<ResourceLocation> list = new ArrayList<>();
            for (Object item : (List<?>) obj) {
                list.add(ResourceLocation.parse(item.toString()));
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
}
