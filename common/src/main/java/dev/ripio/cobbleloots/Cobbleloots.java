package dev.ripio.cobbleloots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ripio.cobbleloots.config.CobblelootsConfig;
import dev.ripio.cobbleloots.event.compat.CobblemonCompatEvents;

public final class Cobbleloots {
    public static final String MOD_ID = "cobbleloots";
    public static final Logger LOGGER = LoggerFactory.getLogger("CobbleLoots");

    public static void init() {
        CobblelootsConfig.init();
        CobblemonCompatEvents.register();
    }
}
