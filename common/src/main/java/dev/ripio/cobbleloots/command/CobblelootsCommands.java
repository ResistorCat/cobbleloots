package dev.ripio.cobbleloots.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CobblelootsCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        LiteralArgumentBuilder<CommandSourceStack> cobbleloots = Commands.literal("cobbleloots")
                .requires(source -> source.hasPermission(2));

        LiteralArgumentBuilder<CommandSourceStack> reset = Commands.literal("reset");
        LiteralArgumentBuilder<CommandSourceStack> lootBall = Commands.literal("loot_ball");

        LootBallResetCommand.register(lootBall);

        reset.then(lootBall);
        cobbleloots.then(reset);

        dispatcher.register(cobbleloots);
    }
}
