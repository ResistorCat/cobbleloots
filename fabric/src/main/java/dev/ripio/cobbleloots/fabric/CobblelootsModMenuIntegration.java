package dev.ripio.cobbleloots.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.ripio.cobbleloots.config.CobblelootsConfig;
import eu.midnightdust.lib.config.MidnightConfig;

public class CobblelootsModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> MidnightConfig.getScreen(parent, "cobbleloots");
    }
}
