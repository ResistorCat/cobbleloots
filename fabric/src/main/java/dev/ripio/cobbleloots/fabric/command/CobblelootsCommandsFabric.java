package dev.ripio.cobbleloots.fabric.command;

import dev.ripio.cobbleloots.command.CobblelootsCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CobblelootsCommandsFabric {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(CobblelootsCommands::register);
    }
}
