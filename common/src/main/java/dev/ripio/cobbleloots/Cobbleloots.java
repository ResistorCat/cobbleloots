package dev.ripio.cobbleloots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.ripio.cobbleloots.config.CobblelootsConfig.initConfig;


public final class Cobbleloots {
    public static final String MOD_ID = "cobbleloots";
    public static final Logger LOGGER = LoggerFactory.getLogger("CobbleLoots");

    public static void init() {
        initConfig();
    }
}
