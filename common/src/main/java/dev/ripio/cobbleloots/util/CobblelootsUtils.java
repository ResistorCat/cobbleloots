package dev.ripio.cobbleloots.util;

import net.minecraft.resources.ResourceLocation;

public class CobblelootsUtils {
    public static ResourceLocation cobblelootsResource(String path) {
        return ResourceLocation.fromNamespaceAndPath("cobbleloots", path);
    }
}
