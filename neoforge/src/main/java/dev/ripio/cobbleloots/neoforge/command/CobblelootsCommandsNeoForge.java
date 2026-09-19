package dev.ripio.cobbleloots.neoforge.command;

import dev.ripio.cobbleloots.command.CobblelootsCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CobblelootsCommandsNeoForge {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CobblelootsCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }
}
