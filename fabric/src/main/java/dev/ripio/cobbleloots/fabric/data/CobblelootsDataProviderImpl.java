package dev.ripio.cobbleloots.fabric.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.ripio.cobbleloots.Cobbleloots;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static dev.ripio.cobbleloots.data.CobblelootsDataProvider.*;
import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsDataProviderImpl {
    public static void registerDataProvider() {
        Cobbleloots.LOGGER.info("Registering data provider");
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return cobblelootsResource("custom_data");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        Cobbleloots.LOGGER.info("Reloading data pack");

                        // Cache data
                        List<ResourceLocation> cachedLootBalls = getExistingLootBallIds();

                        // Load loot balls
                        for (ResourceLocation id : resourceManager.listResources(PATH_LOOT_BALLS, path -> path.getPath().endsWith(".json")).keySet()) {
                            try (InputStream stream = resourceManager.getResourceOrThrow(id).open()) {
                                // Parse JSON
                                JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                                // Normalize id
                                id = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace(".json", ""));
                                // Load loot ball data
                                addLootBallRecord(id, jsonObject);
                                cachedLootBalls.remove(id);
                                Cobbleloots.LOGGER.info("Loaded loot ball data from data pack: {}", id);
                            } catch (IOException | NullPointerException | JsonSyntaxException e) {
                                Cobbleloots.LOGGER.error("Error loading loot ball data from data pack: {}", id, e);
                            }
                        }

                        // Remove deleted loot balls
                        removeLootBallRecord(cachedLootBalls);
                    }
                }
        );
    }
}
