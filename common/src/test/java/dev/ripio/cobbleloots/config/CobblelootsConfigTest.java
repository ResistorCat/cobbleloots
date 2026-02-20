package dev.ripio.cobbleloots.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CobblelootsConfigTest {

    private Path tempDir;
    private Path configDir;
    private Path legacyConfigFile;

    @BeforeEach
    public void setup() throws IOException {
        tempDir = Files.createTempDirectory("cobbleloots_test");
        configDir = tempDir.resolve("config/cobbleloots");
        Files.createDirectories(configDir);
        legacyConfigFile = configDir.resolve("cobbleloots.yaml");
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Cleanup
        deleteDirectory(tempDir);
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var entries = Files.list(path)) {
                for (Path entry : entries.toList()) {
                    deleteDirectory(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    // Helper to write a simplified legacy yaml
    private void writeLegacyConfig() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(legacyConfigFile)) {
            writer.write("loot_ball:\n");
            writer.write("  generation:\n");
            writer.write("    chance: 0.0513\n");
            writer.write("    enabled: true\n");
            writer.write("    chunk_cap: 4\n");
            writer.write("    attempts: 2\n");
            writer.write("  spawning:\n");
            writer.write("    chance: 0.25\n");
            writer.write("    cooldown:\n");
            writer.write("      min: 6000\n");
            writer.write("      max: 36000\n");
            writer.write("    despawn:\n");
            writer.write("      time: 24000\n");
            writer.write("      enabled: true\n");
            writer.write("    enabled: true\n");
            writer.write("  defaults:\n");
            writer.write("    multiplier: 1.0\n");
            writer.write("    xp: 0\n");
            writer.write("    despawn_tick: 0\n");
            writer.write("    player_timer: 0\n");
            writer.write("    uses: 1\n");
            writer.write("  bonus:\n");
            writer.write("    chance: 0.1\n");
            writer.write("    multiplier: 2.0\n");
            writer.write("    invisible: true\n");
            writer.write("    enabled: true\n");
            writer.write("  survival:\n");
            writer.write("    drop:\n");
            writer.write("      automatic: true\n");
            writer.write("      enabled: true\n");
            writer.write("    destroy_looted: false\n");
            writer.write("  xp:\n");
            writer.write("    enabled: true\n");
            writer.write("  disabled:\n");
            writer.write("    loot_balls: []\n");
            writer.write("    dimensions:\n");
            writer.write("      generation: []\n");
            writer.write("      spawning: []\n");
            writer.write("      fishing: []\n");
            writer.write("      archaeology: []\n");
        }
    }

    // Since we can't easily change the static CONFIG_PATH in CobblelootsConfig
    // without refactoring,
    // we will focus on testing the migration logic method if we extract it,
    // or we can refactor CobblelootsConfig to be more testable.
    // existing CobblelootsConfig.CONFIG_PATH is public static final.

    @Test
    public void testMigrationFromLegacyYaml() throws IOException {
        // Create a legacy config file with specific non-default values
        writeLegacyConfig();

        // Ensure the file exists
        Assertions.assertTrue(Files.exists(legacyConfigFile));

        // Call proper init with the test file
        CobblelootsConfig.init(legacyConfigFile);

        // Verify values were migrated
        Assertions.assertEquals(0.0513f, CobblelootsConfig.generation_chance, 0.0001f);
        Assertions.assertTrue(CobblelootsConfig.generation_enabled);
        Assertions.assertEquals(4, CobblelootsConfig.generation_chunk_cap);
        Assertions.assertEquals(2, CobblelootsConfig.generation_attempts_per_chunk);

        Assertions.assertEquals(0.25f, CobblelootsConfig.spawning_chance, 0.0001f);
        Assertions.assertEquals(6000, CobblelootsConfig.spawning_cooldown_min);
        Assertions.assertEquals(36000, CobblelootsConfig.spawning_cooldown_max);
        Assertions.assertEquals(24000, CobblelootsConfig.spawning_despawn_time);
        Assertions.assertTrue(CobblelootsConfig.spawning_enabled);

        Assertions.assertEquals(1.0f, CobblelootsConfig.loot_ball_default_multiplier, 0.0001f);
        Assertions.assertEquals(0, CobblelootsConfig.loot_ball_default_xp);
        Assertions.assertEquals(0, CobblelootsConfig.loot_ball_default_despawn_tick);
        Assertions.assertEquals(0, CobblelootsConfig.loot_ball_default_player_cooldown);
        Assertions.assertEquals(1, CobblelootsConfig.loot_ball_default_uses);

        Assertions.assertEquals(0.1f, CobblelootsConfig.loot_ball_bonus_chance, 0.0001f);
        Assertions.assertEquals(2.0f, CobblelootsConfig.loot_ball_bonus_multiplier, 0.0001f);
        Assertions.assertTrue(CobblelootsConfig.loot_ball_bonus_invisible);

        Assertions.assertEquals(CobblelootsLootBallEmptyBehavior.DROP_AUTOMATIC,
                CobblelootsConfig.loot_ball_empty_behavior);

        Assertions.assertTrue(CobblelootsConfig.loot_ball_xp_enabled);

        Assertions.assertTrue(CobblelootsConfig.data_pack_disabled_loot_balls.isEmpty());
        Assertions.assertTrue(CobblelootsConfig.generation_disabled_dimensions.isEmpty());
        Assertions.assertTrue(CobblelootsConfig.spawning_disabled_dimensions.isEmpty());
        Assertions.assertTrue(CobblelootsConfig.fishing_disabled_dimensions.isEmpty());
        Assertions.assertTrue(CobblelootsConfig.archaeology_disabled_dimensions.isEmpty());

        // Verify file was renamed
        Assertions.assertFalse(Files.exists(legacyConfigFile), "Legacy config file should be renamed");
        Assertions.assertTrue(Files.exists(legacyConfigFile.resolveSibling("cobbleloots.yaml.old")),
                "Renamed file should exist");
    }
}
