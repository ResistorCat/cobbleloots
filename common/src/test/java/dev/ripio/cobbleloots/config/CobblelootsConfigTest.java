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

        // Mock config path for testing
        // Note: In a real environment, we'd need to inject this path into the config
        // class
        // For this test, we might need to adjust CobblelootsConfig to accept a base
        // path or use a system property
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
            writer.write("  xp:\n");
            writer.write("    enabled: false\n"); // Changed from default true
            writer.write("  defaults:\n");
            writer.write("    uses: 5\n"); // Changed from default 1
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
        Assertions.assertFalse(CobblelootsConfig.xp_enabled, "XP enabled should be false");
        Assertions.assertEquals(5, CobblelootsConfig.defaults_uses, "Default uses should be 5");

        // Verify file was renamed
        Assertions.assertFalse(Files.exists(legacyConfigFile), "Legacy config file should be renamed");
        Assertions.assertTrue(Files.exists(legacyConfigFile.resolveSibling("cobbleloots.yaml.old")),
                "Renamed file should exist");
    }
}
