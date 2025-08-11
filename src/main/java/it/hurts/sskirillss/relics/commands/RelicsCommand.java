package it.hurts.sskirillss.relics.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.commands.arguments.AbilityArgument;
import it.hurts.sskirillss.relics.commands.arguments.AbilityStatArgument;
import it.hurts.sskirillss.relics.commands.arguments.RelicAbilityStatisticMetricArgument;
import it.hurts.sskirillss.relics.commands.arguments.RelicStatisticMetricArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class RelicsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("relics").requires(source -> source.hasPermission(2))
                .then(Commands.literal("relic")
                        .then(Commands.literal("maximize")
                                .executes(ctx -> {
                                    var source = ctx.getSource();
                                    var player = source.getPlayerOrException();
                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                        source.sendFailure(Component.translatable("command.relics.base.not_relic"));

                                        return 0;
                                    }

                                    var levelingTemplate = relic.getLevelingTemplate(player, stack);

                                    relic.setRelicRank(player, stack, levelingTemplate.getMaxRank());
                                    relic.setRelicLevel(player, stack, relic.calculateRelicMaxLevel(player, stack));

                                    relic.getAbilitiesTemplate(player, stack).getAbilities().forEach((abilityId, abilityTemplate) -> {
                                        relic.setAbilityLevel(player, stack, abilityId, abilityTemplate.getInitialMaxLevel());
                                        relic.setLockUnlocks(player, stack, abilityId, relic.getMaxLockUnlocks());
                                        relic.setAbilityResearched(player, stack, abilityId, true);

                                        abilityTemplate.getStats().keySet().forEach(statId -> relic.setStatInitialQuality(player, stack, abilityId, statId, relic.getStatMaxQuality(player, stack, abilityId, statId)));
                                    });

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("minimize")
                                .executes(ctx -> {
                                    var source = ctx.getSource();
                                    var player = source.getPlayerOrException();
                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                        source.sendFailure(Component.translatable("command.relics.base.not_relic"));

                                        return 0;
                                    }

                                    relic.setRelicRank(player, stack, 0);
                                    relic.setRelicLevel(player, stack, 0);
                                    relic.setRelicExperience(player, stack, 0);

                                    relic.getAbilitiesTemplate(player, stack).getAbilities().forEach((abilityId, abilityTemplate) -> {
                                        relic.setAbilityResearched(player, stack, abilityId, false);
                                        relic.setAbilityLevel(player, stack, abilityId, 0);
                                        relic.setResearchLinks(player, stack, abilityId, new HashMap<>());

                                        if (!relic.isEnoughLevel(player, stack, abilityId))
                                            relic.setLockUnlocks(player, stack, abilityId, 0);

                                        abilityTemplate.getStats().keySet().forEach(statId ->
                                                relic.setStatInitialQuality(player, stack, abilityId, statId, 0)
                                        );
                                    });

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("level")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("level", IntegerArgumentType.integer())
                                                .executes(ctx -> {
                                                    var player = ctx.getSource().getPlayerOrException();
                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                        ctx.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                        return 0;
                                                    }

                                                    var level = IntegerArgumentType.getInteger(ctx, "level");
                                                    var action = ctx.getArgument("action", CommandAction.class);

                                                    relic.setRelicLevel(player, stack, switch (action) {
                                                        case SET -> level;
                                                        case ADD -> relic.getRelicLevel(player, stack) + level;
                                                        case TAKE -> relic.getRelicLevel(player, stack) - level;
                                                    });

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("experience")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("experience", IntegerArgumentType.integer())
                                                .executes(ctx -> {
                                                    var player = ctx.getSource().getPlayerOrException();
                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                        ctx.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                        return 0;
                                                    }

                                                    var experience = IntegerArgumentType.getInteger(ctx, "experience");
                                                    var action = ctx.getArgument("action", CommandAction.class);

                                                    relic.setRelicExperience(null, stack, switch (action) {
                                                        case SET -> experience;
                                                        case ADD -> relic.getRelicExperience(player, stack) + experience;
                                                        case TAKE -> relic.getRelicExperience(player, stack) - experience;
                                                    });

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("points")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("points", IntegerArgumentType.integer())
                                                .executes(ctx -> {
                                                    var player = ctx.getSource().getPlayerOrException();
                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                        ctx.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                        return 0;
                                                    }

                                                    var points = IntegerArgumentType.getInteger(ctx, "points");
                                                    var action = ctx.getArgument("action", CommandAction.class);

                                                    relic.setRelicLevelingPoints(player, stack, switch (action) {
                                                        case SET -> points;
                                                        case ADD -> relic.getRelicLevelingPoints(player, stack) + points;
                                                        case TAKE -> relic.getRelicLevelingPoints(player, stack) - points;
                                                    });

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("rank")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("rank", IntegerArgumentType.integer())
                                                .executes(ctx -> {
                                                    var player = ctx.getSource().getPlayerOrException();
                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                        ctx.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                        return 0;
                                                    }

                                                    var rank = IntegerArgumentType.getInteger(ctx, "rank");
                                                    var action = ctx.getArgument("action", CommandAction.class);

                                                    relic.setRelicRank(player, stack, switch (action) {
                                                        case SET -> rank;
                                                        case ADD -> relic.getRelicRank(player, stack) + rank;
                                                        case TAKE -> relic.getRelicRank(player, stack) - rank;
                                                    });

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("statistic")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("metric", RelicStatisticMetricArgument.relicStatisticMetric())
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            var player = ctx.getSource().getPlayerOrException();
                                                            var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                            if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                                ctx.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                                return 0;
                                                            }

                                                            var action = ctx.getArgument("action", CommandAction.class);
                                                            var metric = RelicStatisticMetricArgument.getRelicStatisticMetric(ctx, "metric");
                                                            var value = DoubleArgumentType.getDouble(ctx, "value");

                                                            Stream.of(metric.equals("all") ? relic.getRelicStatisticTemplate(player, stack).getMetrics().keySet().toArray(new String[0]) : new String[]{metric})
                                                                    .forEach(metricEntry -> relic.setRelicMetricValue(player, stack, metricEntry, switch (action) {
                                                                                case SET -> value;
                                                                                case ADD -> relic.getRelicMetricValue(player, stack, metricEntry) + value;
                                                                                case TAKE -> relic.getRelicMetricValue(player, stack, metricEntry) - value;
                                                                            })
                                                                    );

                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("ability")
                        .then(Commands.literal("level")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("ability", AbilityArgument.ability())
                                                .then(Commands.argument("level", IntegerArgumentType.integer())
                                                        .executes(ctx -> {
                                                            var player = ctx.getSource().getPlayerOrException();
                                                            var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                            if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                                ctx.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                                return 0;
                                                            }

                                                            var action = ctx.getArgument("action", CommandAction.class);
                                                            var ability = AbilityArgument.getAbility(ctx, "ability");
                                                            var level = IntegerArgumentType.getInteger(ctx, "level");

                                                            (ability.equals("all") ? relic.getAbilitiesTemplate(player, stack).getAbilities().keySet() : Set.of(ability)).forEach(entry ->
                                                                    relic.setAbilityLevel(player, stack, entry, switch (action) {
                                                                        case SET -> level;
                                                                        case ADD -> relic.getAbilityLevel(player, stack, entry) + level;
                                                                        case TAKE -> relic.getAbilityLevel(player, stack, entry) - level;
                                                                    })
                                                            );

                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("statistic")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("ability", AbilityArgument.ability())
                                                .then(Commands.argument("metric", RelicAbilityStatisticMetricArgument.abilityStatisticMetric())
                                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                                .executes(ctx -> {
                                                                    var player = ctx.getSource().getPlayerOrException();
                                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                                        ctx.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                                        return 0;
                                                                    }

                                                                    var action = ctx.getArgument("action", CommandAction.class);
                                                                    var ability = AbilityArgument.getAbility(ctx, "ability");
                                                                    var metric = RelicAbilityStatisticMetricArgument.getAbilityStatisticMetric(ctx, "metric");
                                                                    var value = DoubleArgumentType.getDouble(ctx, "value");

                                                                    Stream.of(ability.equals("all") ? relic.getAbilitiesTemplate(player, stack).getAbilities().keySet().toArray(new String[0]) : new String[]{ability})
                                                                            .forEach(abilityEntry -> (metric.equals("all") ? relic.getAbilityTemplate(player, stack, abilityEntry).getStats().keySet().stream() : Stream.of(metric))
                                                                                    .forEach(metricEntry -> relic.setAbilityMetricValue(player, stack, abilityEntry, metricEntry, switch (action) {
                                                                                                case SET -> value;
                                                                                                case ADD -> relic.getAbilityMetricValue(player, stack, abilityEntry, metricEntry) + value;
                                                                                                case TAKE -> relic.getAbilityMetricValue(player, stack, abilityEntry, metricEntry) - value;
                                                                                            })
                                                                                    )
                                                                            );

                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("stat")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("ability", AbilityArgument.ability())
                                                .then(Commands.argument("stat", AbilityStatArgument.abilityStat())
                                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                                .executes(ctx -> {
                                                                    var player = ctx.getSource().getPlayerOrException();
                                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                                        ctx.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                                        return 0;
                                                                    }

                                                                    var action = ctx.getArgument("action", CommandAction.class);
                                                                    var ability = AbilityArgument.getAbility(ctx, "ability");
                                                                    var stat = AbilityStatArgument.getAbilityStat(ctx, "stat");
                                                                    var value = DoubleArgumentType.getDouble(ctx, "value");

                                                                    Stream.of(ability.equals("all") ? relic.getAbilitiesTemplate(player, stack).getAbilities().keySet().toArray(new String[0]) : new String[]{ability})
                                                                            .forEach(abilityEntry -> (stat.equals("all") ? relic.getAbilityTemplate(player, stack, abilityEntry).getStats().keySet().stream() : Stream.of(stat))
                                                                                    .forEach(statEntry -> relic.setStatOverrideValue(player, stack, abilityEntry, statEntry, switch (action) {
                                                                                                case SET -> value;
                                                                                                case ADD -> relic.getOrCalculateStatValue(player, stack, abilityEntry, statEntry) + value;
                                                                                                case TAKE -> relic.getOrCalculateStatValue(player, stack, abilityEntry, statEntry) - value;
                                                                                            })
                                                                                    )
                                                                            );

                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("quality")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("ability", AbilityArgument.ability())
                                                .then(Commands.argument("stat", AbilityStatArgument.abilityStat())
                                                        .then(Commands.argument("quality", IntegerArgumentType.integer())
                                                                .executes(ctx -> {
                                                                    var player = ctx.getSource().getPlayerOrException();
                                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                                        ctx.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                                        return 0;
                                                                    }

                                                                    var action = ctx.getArgument("action", CommandAction.class);
                                                                    var ability = AbilityArgument.getAbility(ctx, "ability");
                                                                    var stat = AbilityStatArgument.getAbilityStat(ctx, "stat");
                                                                    var quality = IntegerArgumentType.getInteger(ctx, "quality");

                                                                    Stream.of(ability.equals("all") ? relic.getAbilitiesTemplate(player, stack).getAbilities().keySet().toArray(new String[0]) : new String[]{ability})
                                                                            .forEach(abilityEntry -> (stat.equals("all") ? relic.getAbilityTemplate(player, stack, abilityEntry).getStats().keySet().stream() : Stream.of(stat))
                                                                                    .forEach(statEntry -> relic.setStatInitialQuality(player, stack, abilityEntry, statEntry, switch (action) {
                                                                                        case SET -> quality;
                                                                                        case ADD -> relic.getStatInitialQuality(player, stack, abilityEntry, statEntry) + quality;
                                                                                        case TAKE -> relic.getStatInitialQuality(player, stack, abilityEntry, statEntry) - quality;
                                                                                    }))
                                                                            );

                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("randomize")
                                .then(Commands.argument("ability", AbilityArgument.ability())
                                        .then(Commands.argument("stat", AbilityStatArgument.abilityStat())
                                                .executes(ctx -> {
                                                    var player = ctx.getSource().getPlayerOrException();
                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                        ctx.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                        return 0;
                                                    }

                                                    var ability = AbilityArgument.getAbility(ctx, "ability");
                                                    var stat = AbilityStatArgument.getAbilityStat(ctx, "stat");

                                                    Stream.of(ability.equals("all") ? relic.getAbilitiesTemplate(player, stack).getAbilities().keySet().toArray(new String[0]) : new String[]{ability})
                                                            .forEach(abilityEntry -> (stat.equals("all") ? relic.getAbilityTemplate(player, stack, abilityEntry).getStats().keySet().stream() : Stream.of(stat))
                                                                    .forEach(statEntry -> relic.randomizeStat(player, stack, abilityEntry, statEntry))
                                                            );

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                )
        );
    }

    public enum CommandAction {
        SET,
        ADD,
        TAKE
    }
}