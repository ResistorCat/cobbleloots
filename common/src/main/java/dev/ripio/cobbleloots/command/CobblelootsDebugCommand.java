package dev.ripio.cobbleloots.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.ripio.cobbleloots.data.CobblelootsDataProvider;
import dev.ripio.cobbleloots.data.debug.CobblelootsDebugService;
import dev.ripio.cobbleloots.data.debug.FilterCheckResult;
import dev.ripio.cobbleloots.data.debug.LootBallCheckReport;
import dev.ripio.cobbleloots.data.debug.LootBallRuleReport;
import dev.ripio.cobbleloots.data.debug.LootBallWeightEntry;
import dev.ripio.cobbleloots.data.debug.WeightsReport;
import dev.ripio.cobbleloots.util.enums.CobblelootsSourceType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Command handler for {@code /cobbleloots debug} subcommands ({@code weights} and {@code check}).
 */
public class CobblelootsDebugCommand {

    private static final List<String> SOURCE_TYPE_NAMES = List.of("spawning", "generation", "fishing", "archaeology");

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_SOURCE_TYPE = (ctx, builder) ->
            SharedSuggestionProvider.suggest(SOURCE_TYPE_NAMES, builder);

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_LOOT_BALL_IDS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(CobblelootsDataProvider.getExistingLootBallIds(), builder);

    public static void register(LiteralArgumentBuilder<CommandSourceStack> debugNode) {
        register(debugNode, null);
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> debugNode, @Nullable CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> weightsNode = Commands.literal("weights")
                .executes(ctx -> executeWeights(ctx, CobblelootsSourceType.SPAWNING, false))
                .then(Commands.argument("source_type", StringArgumentType.word())
                        .suggests(SUGGEST_SOURCE_TYPE)
                        .executes(ctx -> executeWeights(ctx, parseSourceType(StringArgumentType.getString(ctx, "source_type")), false))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> executeWeights(ctx, parseSourceType(StringArgumentType.getString(ctx, "source_type")), true))
                        )
                );

        LiteralArgumentBuilder<CommandSourceStack> checkNode = Commands.literal("check")
                .then(Commands.argument("id", ResourceLocationArgument.id())
                        .suggests(SUGGEST_LOOT_BALL_IDS)
                        .executes(ctx -> executeCheck(ctx, CobblelootsSourceType.SPAWNING, false))
                        .then(Commands.argument("source_type", StringArgumentType.word())
                                .suggests(SUGGEST_SOURCE_TYPE)
                                .executes(ctx -> executeCheck(ctx, parseSourceType(StringArgumentType.getString(ctx, "source_type")), false))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> executeCheck(ctx, parseSourceType(StringArgumentType.getString(ctx, "source_type")), true))
                                )
                        )
                );

        debugNode.then(weightsNode);
        debugNode.then(checkNode);
    }

    private static int executeWeights(CommandContext<CommandSourceStack> ctx, @Nullable CobblelootsSourceType sourceType, boolean hasExplicitPos) {
        CommandSourceStack source = ctx.getSource();
        if (sourceType == null) {
            String rawType = StringArgumentType.getString(ctx, "source_type");
            source.sendFailure(Component.translatable("commands.cobbleloots.debug.invalid_source_type", rawType).withStyle(ChatFormatting.RED));
            return 0;
        }

        BlockPos pos = hasExplicitPos
                ? BlockPosArgument.getBlockPos(ctx, "pos")
                : BlockPos.containing(source.getPosition());

        ServerLevel level = source.getLevel();
        ServerPlayer player = source.getPlayer();

        WeightsReport report = CobblelootsDebugService.calculateWeights(level, pos, sourceType, player);

        source.sendSuccess(() -> Component.translatable(
                "commands.cobbleloots.debug.weights.header",
                report.sourceType().getName(),
                report.pos().getX(),
                report.pos().getY(),
                report.pos().getZ(),
                report.biome()
        ).withStyle(ChatFormatting.GOLD), false);

        if (report.entries().isEmpty()) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.cobbleloots.debug.weights.empty",
                    report.sourceType().getName()
            ).withStyle(ChatFormatting.YELLOW), false);
        } else {
            for (LootBallWeightEntry entry : report.entries()) {
                source.sendSuccess(() -> Component.translatable(
                        "commands.cobbleloots.debug.weights.entry",
                        entry.id(),
                        entry.weight(),
                        String.format(Locale.ROOT, "%.1f", entry.percentage())
                ).withStyle(ChatFormatting.WHITE), false);
            }
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.cobbleloots.debug.weights.total",
                report.entries().size(),
                report.totalWeight()
        ).withStyle(ChatFormatting.GRAY), false);

        return Math.max(1, report.entries().size());
    }

    private static int executeCheck(CommandContext<CommandSourceStack> ctx, @Nullable CobblelootsSourceType sourceType, boolean hasExplicitPos) {
        CommandSourceStack source = ctx.getSource();
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");

        if (CobblelootsDataProvider.getLootBallData(id) == null) {
            source.sendFailure(Component.translatable("commands.cobbleloots.debug.check.not_found", id).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (sourceType == null) {
            String rawType = StringArgumentType.getString(ctx, "source_type");
            source.sendFailure(Component.translatable("commands.cobbleloots.debug.invalid_source_type", rawType).withStyle(ChatFormatting.RED));
            return 0;
        }

        BlockPos pos = hasExplicitPos
                ? BlockPosArgument.getBlockPos(ctx, "pos")
                : BlockPos.containing(source.getPosition());

        ServerLevel level = source.getLevel();
        ServerPlayer player = source.getPlayer();

        source.sendSuccess(() -> Component.translatable(
                "commands.cobbleloots.debug.check.header",
                id,
                sourceType.getName(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        ).withStyle(ChatFormatting.GOLD), false);

        LootBallCheckReport report = CobblelootsDebugService.inspectLootBall(level, pos, id, sourceType, player);

        if (report.rules().isEmpty()) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.cobbleloots.debug.check.no_rules",
                    id,
                    sourceType.getName()
            ).withStyle(ChatFormatting.YELLOW), false);
            source.sendSuccess(() -> Component.translatable(
                    "commands.cobbleloots.debug.check.result_ineligible"
            ).withStyle(ChatFormatting.RED), false);
            return 0;
        }

        for (LootBallRuleReport ruleReport : report.rules()) {
            Component statusComponent = ruleReport.overallPassed()
                    ? Component.literal("[PASS]").withStyle(ChatFormatting.GREEN)
                    : Component.literal("[FAIL]").withStyle(ChatFormatting.RED);

            source.sendSuccess(() -> Component.translatable(
                    "commands.cobbleloots.debug.check.rule_header",
                    ruleReport.ruleIndex(),
                    ruleReport.weight(),
                    statusComponent
            ).withStyle(ChatFormatting.YELLOW), false);

            for (FilterCheckResult filterResult : ruleReport.filterResults()) {
                Component filterName = Component.translatable("commands.cobbleloots.debug.filter." + filterResult.filterKey());
                if (filterResult.passed()) {
                    source.sendSuccess(() -> Component.translatable(
                            "commands.cobbleloots.debug.check.filter_passed",
                            filterName,
                            filterResult.actual()
                    ).withStyle(ChatFormatting.GREEN), false);
                } else {
                    source.sendSuccess(() -> Component.translatable(
                            "commands.cobbleloots.debug.check.filter_failed",
                            filterName,
                            filterResult.actual(),
                            filterResult.expected()
                    ).withStyle(ChatFormatting.RED), false);
                }
            }
        }

        if (report.eligible()) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.cobbleloots.debug.check.result_eligible",
                    report.totalWeight()
            ).withStyle(ChatFormatting.GREEN), false);
            return Math.max(1, report.totalWeight());
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.cobbleloots.debug.check.result_ineligible"
        ).withStyle(ChatFormatting.RED), false);
        return 0;
    }

    @Nullable
    private static CobblelootsSourceType parseSourceType(String name) {
        if (name == null) {
            return null;
        }
        for (CobblelootsSourceType type : CobblelootsSourceType.values()) {
            if (type.getName().equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
