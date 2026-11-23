package dev.ripio.cobbleloots.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModIconTest {

    @Test
    public void testModIconResolvesInDevContainer() throws IOException {
        ModContainer modContainer = FabricLoader.getInstance()
                .getModContainer("cobbleloots")
                .orElseThrow(() -> new AssertionError("cobbleloots mod container not found"));

        String iconPath = modContainer.getMetadata()
                .getIconPath(128)
                .orElseThrow(() -> new AssertionError("cobbleloots icon path not declared in fabric.mod.json"));

        Path resolvedPath = modContainer.findPath(iconPath)
                .orElseThrow(() -> new AssertionError("Declared mod icon missing from mod container: " + iconPath));

        Assertions.assertTrue(Files.isRegularFile(resolvedPath), "Icon path must be a regular file: " + resolvedPath);

        try (InputStream stream = Files.newInputStream(resolvedPath)) {
            BufferedImage image = ImageIO.read(stream);
            Assertions.assertNotNull(image, "Icon must be a valid image: " + iconPath);
            Assertions.assertEquals(image.getWidth(), image.getHeight(), "Mod icon must be square");
            Assertions.assertTrue(image.getWidth() > 0, "Mod icon dimensions must be positive");
        }
    }
}
