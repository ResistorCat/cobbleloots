package dev.ripio.cobbleloots.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class CobblelootsUtils {
    public static ResourceLocation cobblelootsResource(String path) {
        return ResourceLocation.fromNamespaceAndPath("cobbleloots", path);
    }

    public static MutableComponent cobblelootsText(String key, Object... args) {
        return Component.translatableWithFallback(key, "[ERROR] Please report this text error!", args);
    }

}
