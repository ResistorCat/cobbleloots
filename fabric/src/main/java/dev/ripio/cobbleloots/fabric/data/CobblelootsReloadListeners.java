package dev.ripio.cobbleloots.fabric.data;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import static dev.ripio.cobbleloots.data.CobblelootsDataProvider.onReload;
import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsReloadListeners {
    public static void registerReloadListeners() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                  @Override
                  public ResourceLocation getFabricId() {
                      return cobblelootsResource("custom_data");
                  }

                  @Override
                  public void onResourceManagerReload(ResourceManager resourceManager) {
                      onReload(resourceManager);
                  }
              }
        );
    }
}
