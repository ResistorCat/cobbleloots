package dev.ripio.cobbleloots.util;

import net.minecraft.resources.ResourceLocation;

import static dev.ripio.cobbleloots.util.CobblelootsUtils.cobblelootsResource;

public class CobblelootsDefinitions {
    public static String[] SOURCE_TYPES = {
            "generation",
            "fishing",
            "archaeology"
    };
    public static final ResourceLocation EMPTY = cobblelootsResource("empty");
}
