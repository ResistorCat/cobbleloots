package dev.ripio.cobbleloots.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ripio.cobbleloots.Cobbleloots;
import dev.ripio.cobbleloots.data.CobblelootsWorldData;
import dev.ripio.cobbleloots.entity.custom.CobblelootsLootBall;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class LootBallResetCommand {

    private enum TargetMode {
        SELF,
        ALL,
        PLAYERS
    }

    private static class TargetInfo {
        final boolean targetAll;
        final Collection<UUID> uuids;
        final String description;

        TargetInfo(boolean targetAll, Collection<UUID> uuids, String description) {
            this.targetAll = targetAll;
            this.uuids = uuids;
            this.description = description;
        }
    }

    @FunctionalInterface
    private interface ResetActionHandler {
        int run(CommandContext<CommandSourceStack> context, TargetMode mode, Collection<ServerPlayer> explicitPlayers, boolean restoreUses) throws CommandSyntaxException;
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> lootBallNode) {
        LiteralArgumentBuilder<CommandSourceStack> allNode = attachTargetAndRestoreArgs(
                Commands.literal("all"),
                LootBallResetCommand::executeAll
        );

        LiteralArgumentBuilder<CommandSourceStack> targetNode = attachTargetAndRestoreArgs(
                Commands.literal("target"),
                LootBallResetCommand::executeTarget
        );

        ArgumentBuilder<CommandSourceStack, ?> entitySelectorNode = attachTargetAndRestoreArgs(
                Commands.argument("entity_selector", EntityArgument.entities()),
                LootBallResetCommand::executeEntity
        );
        LiteralArgumentBuilder<CommandSourceStack> entityNode = Commands.literal("entity").then(entitySelectorNode);

        LiteralArgumentBuilder<CommandSourceStack> undoNode = Commands.literal("undo")
                .executes(ctx -> executeUndo(ctx, TargetMode.SELF, null))
                .then(Commands.literal("all")
                        .executes(ctx -> executeUndo(ctx, TargetMode.ALL, null)))
                .then(Commands.literal("*")
                        .executes(ctx -> executeUndo(ctx, TargetMode.ALL, null)))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> executeUndo(ctx, TargetMode.PLAYERS, EntityArgument.getPlayers(ctx, "targets"))));

        lootBallNode.then(allNode);
        lootBallNode.then(targetNode);
        lootBallNode.then(entityNode);
        lootBallNode.then(undoNode);
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T attachTargetAndRestoreArgs(T builder, ResetActionHandler handler) {
        builder.executes(ctx -> handler.run(ctx, TargetMode.SELF, null, true))
                .then(Commands.argument("restore_uses", BoolArgumentType.bool())
                        .executes(ctx -> handler.run(ctx, TargetMode.SELF, null, BoolArgumentType.getBool(ctx, "restore_uses"))))
                .then(Commands.literal("all")
                        .executes(ctx -> handler.run(ctx, TargetMode.ALL, null, true))
                        .then(Commands.argument("restore_uses", BoolArgumentType.bool())
                                .executes(ctx -> handler.run(ctx, TargetMode.ALL, null, BoolArgumentType.getBool(ctx, "restore_uses")))))
                .then(Commands.literal("*")
                        .executes(ctx -> handler.run(ctx, TargetMode.ALL, null, true))
                        .then(Commands.argument("restore_uses", BoolArgumentType.bool())
                                .executes(ctx -> handler.run(ctx, TargetMode.ALL, null, BoolArgumentType.getBool(ctx, "restore_uses")))))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> handler.run(ctx, TargetMode.PLAYERS, EntityArgument.getPlayers(ctx, "targets"), true))
                        .then(Commands.argument("restore_uses", BoolArgumentType.bool())
                                .executes(ctx -> handler.run(ctx, TargetMode.PLAYERS, EntityArgument.getPlayers(ctx, "targets"), BoolArgumentType.getBool(ctx, "restore_uses")))));
        return builder;
    }

    private static TargetInfo resolveTargets(CommandSourceStack source, TargetMode mode, Collection<ServerPlayer> explicitPlayers) {
        if (mode == TargetMode.ALL) {
            return new TargetInfo(true, null, "all players");
        }
        if (mode == TargetMode.PLAYERS) {
            if (explicitPlayers == null || explicitPlayers.isEmpty()) {
                return null;
            }
            List<UUID> uuids = explicitPlayers.stream().map(Entity::getUUID).toList();
            String desc = explicitPlayers.size() == 1
                    ? explicitPlayers.iterator().next().getScoreboardName()
                    : explicitPlayers.size() + " players";
            return new TargetInfo(false, uuids, desc);
        }
        // SELF
        if (source.getEntity() instanceof ServerPlayer player) {
            return new TargetInfo(false, Collections.singleton(player.getUUID()), player.getScoreboardName());
        }
        return null;
    }

    private static int executeAll(CommandContext<CommandSourceStack> context, TargetMode mode, Collection<ServerPlayer> explicitPlayers, boolean restoreUses) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        String executorName = source.getTextName();

        TargetInfo targets = resolveTargets(source, mode, explicitPlayers);
        if (targets == null) {
            source.sendFailure(Component.translatable("commands.cobbleloots.reset.loot_ball.player_required").withStyle(ChatFormatting.RED));
            return 0;
        }

        int affectedCount = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof CobblelootsLootBall) {
                    affectedCount++;
                }
            }
        }

        long gameTime = server.overworld().getGameTime();
        CobblelootsWorldData worldData = CobblelootsWorldData.get(server);
        if (targets.targetAll) {
            worldData.resetAll(gameTime, restoreUses);
        } else {
            worldData.resetPlayers(targets.uuids, gameTime, restoreUses);
        }

        Cobbleloots.LOGGER.info(
                "{} executed '/cobbleloots reset loot_ball all' for {} (restore_uses={}): {} loaded loot balls updated.",
                executorName, targets.description, restoreUses, affectedCount
        );

        final int finalAffectedCount = affectedCount;
        source.sendSuccess(
                () -> Component.translatable("commands.cobbleloots.reset.loot_ball.all.success", targets.description, finalAffectedCount)
                        .withStyle(ChatFormatting.GREEN),
                false
        );

        return Math.max(1, affectedCount);
    }

    private static int executeTarget(CommandContext<CommandSourceStack> context, TargetMode mode, Collection<ServerPlayer> explicitPlayers, boolean restoreUses) {
        CommandSourceStack source = context.getSource();
        String executorName = source.getTextName();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("commands.cobbleloots.reset.loot_ball.player_required").withStyle(ChatFormatting.RED));
            return 0;
        }

        TargetInfo targets = resolveTargets(source, mode, explicitPlayers);
        if (targets == null) {
            source.sendFailure(Component.translatable("commands.cobbleloots.reset.loot_ball.player_required").withStyle(ChatFormatting.RED));
            return 0;
        }

        Vec3 eye = player.getEyePosition();
        Vec3 view = player.getViewVector(1.0F);
        double reachDistance = 16.0;
        Vec3 reach = eye.add(view.scale(reachDistance));
        AABB searchBox = player.getBoundingBox().expandTowards(view.scale(reachDistance)).inflate(1.0);

        List<CobblelootsLootBall> candidates = player.level().getEntitiesOfClass(
                CobblelootsLootBall.class,
                searchBox,
                ball -> !ball.isRemoved()
        );

        CobblelootsLootBall closestBall = null;
        double closestDistanceSq = reachDistance * reachDistance;

        for (CobblelootsLootBall ball : candidates) {
            AABB box = ball.getBoundingBox().inflate(0.3);
            if (box.contains(eye)) {
                closestBall = ball;
                closestDistanceSq = 0.0;
                break;
            }
            Optional<Vec3> hit = box.clip(eye, reach);
            if (hit.isPresent()) {
                double distSq = eye.distanceToSqr(hit.get());
                if (distSq < closestDistanceSq) {
                    closestDistanceSq = distSq;
                    closestBall = ball;
                }
            }
        }

        if (closestBall == null) {
            source.sendFailure(Component.translatable("commands.cobbleloots.reset.loot_ball.target.not_found").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (targets.targetAll) {
            closestBall.resetForAll(restoreUses);
        } else {
            for (UUID uuid : targets.uuids) {
                closestBall.resetForPlayer(uuid, restoreUses);
            }
        }

        Cobbleloots.LOGGER.info(
                "{} executed '/cobbleloots reset loot_ball target' for {} (restore_uses={}): 1 loaded loot balls updated.",
                executorName, targets.description, restoreUses
        );

        source.sendSuccess(
                () -> Component.translatable("commands.cobbleloots.reset.loot_ball.target.success", targets.description)
                        .withStyle(ChatFormatting.GREEN),
                false
        );

        return 1;
    }

    private static int executeEntity(CommandContext<CommandSourceStack> context, TargetMode mode, Collection<ServerPlayer> explicitPlayers, boolean restoreUses) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String executorName = source.getTextName();

        TargetInfo targets = resolveTargets(source, mode, explicitPlayers);
        if (targets == null) {
            source.sendFailure(Component.translatable("commands.cobbleloots.reset.loot_ball.player_required").withStyle(ChatFormatting.RED));
            return 0;
        }

        Collection<? extends Entity> selectedEntities = EntityArgument.getEntities(context, "entity_selector");
        List<CobblelootsLootBall> matchedBalls = new ArrayList<>();
        for (Entity entity : selectedEntities) {
            if (entity instanceof CobblelootsLootBall ball) {
                matchedBalls.add(ball);
            }
        }

        if (matchedBalls.isEmpty()) {
            source.sendFailure(Component.translatable("commands.cobbleloots.reset.loot_ball.entity.none_matched").withStyle(ChatFormatting.RED));
            return 0;
        }

        for (CobblelootsLootBall ball : matchedBalls) {
            if (targets.targetAll) {
                ball.resetForAll(restoreUses);
            } else {
                for (UUID uuid : targets.uuids) {
                    ball.resetForPlayer(uuid, restoreUses);
                }
            }
        }

        Cobbleloots.LOGGER.info(
                "{} executed '/cobbleloots reset loot_ball entity' for {} (restore_uses={}): {} loaded loot balls updated.",
                executorName, targets.description, restoreUses, matchedBalls.size()
        );

        source.sendSuccess(
                () -> Component.translatable("commands.cobbleloots.reset.loot_ball.entity.success", matchedBalls.size(), targets.description)
                        .withStyle(ChatFormatting.GREEN),
                false
        );

        return matchedBalls.size();
    }

    private static int executeUndo(CommandContext<CommandSourceStack> context, TargetMode mode, Collection<ServerPlayer> explicitPlayers) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        String executorName = source.getTextName();

        TargetInfo targets = resolveTargets(source, mode, explicitPlayers);
        if (targets == null) {
            source.sendFailure(Component.translatable("commands.cobbleloots.reset.loot_ball.player_required").withStyle(ChatFormatting.RED));
            return 0;
        }

        CobblelootsWorldData worldData = CobblelootsWorldData.get(server);
        boolean changed = targets.targetAll ? worldData.undoAll() : worldData.undoPlayers(targets.uuids);

        if (!changed) {
            Cobbleloots.LOGGER.info(
                    "{} executed '/cobbleloots reset loot_ball undo' for {}: no change.",
                    executorName, targets.description
            );
            source.sendFailure(
                    Component.translatable("commands.cobbleloots.reset.loot_ball.undo.no_change", targets.description)
                            .withStyle(ChatFormatting.RED)
            );
            return 0;
        }

        Cobbleloots.LOGGER.info(
                "{} executed '/cobbleloots reset loot_ball undo' for {}: success.",
                executorName, targets.description
        );
        source.sendSuccess(
                () -> Component.translatable("commands.cobbleloots.reset.loot_ball.undo.success", targets.description)
                        .withStyle(ChatFormatting.GREEN),
                false
        );
        return 1;
    }
}
