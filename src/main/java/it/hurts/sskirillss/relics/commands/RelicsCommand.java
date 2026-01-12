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

                                    var relicData = relic.getRelicData(player, stack);
                                    var levelingTemplate = relicData.getTemplate().getLeveling();

                                    relicData.getLevelingData().setRank(levelingTemplate.getMaxRank());
                                    relicData.getLevelingData().setLevel(relicData.calculateMaxLevel());

                                    var abilitiesData = relicData.getAbilitiesData();

                                    relicData.getTemplate().getAbilities().getAbilities().forEach((abilityId, abilityTemplate) -> {
                                        var abilityData = abilitiesData.getAbilityData(abilityId);

                                        abilityData.setLevel(abilityTemplate.getInitialMaxLevel());
                                        abilityData.getLockData().setUnlocks(abilityData.getLockData().getMaxUnlocks());
                                        abilityData.getResearchData().setResearched(true);

                                        abilityTemplate.getStats().keySet().forEach(statId -> abilityData.getStatData(statId).setInitialQuality(abilityData.getStatData(statId).getMaxQuality()));
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

                                    var relicData = relic.getRelicData(player, stack);

                                    relicData.getLevelingData().setRank(0);
                                    relicData.getLevelingData().setLevel(0);
                                    relicData.getLevelingData().setExperience(0);

                                    var abilitiesData = relicData.getAbilitiesData();

                                    relicData.getTemplate().getAbilities().getAbilities().forEach((abilityId, abilityTemplate) -> {
                                        var abilityData = abilitiesData.getAbilityData(abilityId);

                                        abilityData.getResearchData().setResearched(false);
                                        abilityData.setLevel(0);
                                        abilityData.getResearchData().setLinks(new HashMap<>());

                                        if (!abilityData.isEnoughLevel())
                                            abilityData.getLockData().setUnlocks(0);

                                        abilityTemplate.getStats().keySet().forEach(statId -> abilityData.getStatData(statId).setInitialQuality(0));
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

                                                    var levelingData = relic.getRelicData(player, stack).getLevelingData();

                                                    levelingData.setLevel(switch (action) {
                                                        case SET -> level;
                                                        case ADD -> levelingData.getLevel() + level;
                                                        case TAKE -> levelingData.getLevel() - level;
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

                                                    var levelingData = relic.getRelicData(player, stack).getLevelingData();

                                                    levelingData.setExperience(switch (action) {
                                                        case SET -> experience;
                                                        case ADD -> levelingData.getExperience() + experience;
                                                        case TAKE -> levelingData.getExperience() - experience;
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

                                                    var levelingData = relic.getRelicData(player, stack).getLevelingData();

                                                    levelingData.setPoints(switch (action) {
                                                        case SET -> points;
                                                        case ADD -> levelingData.getPoints() + points;
                                                        case TAKE -> levelingData.getPoints() - points;
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

                                                    var levelingData = relic.getRelicData(player, stack).getLevelingData();

                                                    levelingData.setRank(switch (action) {
                                                        case SET -> rank;
                                                        case ADD -> levelingData.getRank() + rank;
                                                        case TAKE -> levelingData.getRank() - rank;
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

                                                            var statisticData = relic.getRelicData(player, stack).getStatisticData();

                                                            Stream.of(metric.equals("all") ? statisticData.getTemplate().getMetrics().keySet().toArray(new String[0]) : new String[]{metric})
                                                                    .forEach(metricEntry -> statisticData.getMetricData(metricEntry).setValue(switch (action) {
                                                                        case SET -> value;
                                                                        case ADD -> statisticData.getMetricData(metricEntry).getValue() + value;
                                                                        case TAKE -> statisticData.getMetricData(metricEntry).getValue() - value;
                                                                    }));

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

                                                            var abilitiesData = relic.getRelicData(player, stack).getAbilitiesData();

                                                            (ability.equals("all") ? abilitiesData.getAbilityIds() : Set.of(ability)).forEach(entry -> {
                                                                var abilityData = abilitiesData.getAbilityData(entry);

                                                                abilityData.setLevel(switch (action) {
                                                                    case SET -> level;
                                                                    case ADD -> abilityData.getLevel() + level;
                                                                    case TAKE -> abilityData.getLevel() - level;
                                                                });
                                                            });

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

                                                                    var abilitiesData = relic.getRelicData(player, stack).getAbilitiesData();

                                                                    Stream.of(ability.equals("all") ? abilitiesData.getAbilityIds().toArray(new String[0]) : new String[]{ability})
                                                                            .forEach(abilityEntry -> {
                                                                                var abilityData = abilitiesData.getAbilityData(abilityEntry);

                                                                                (metric.equals("all") ? abilityData.getStatisticData().getTemplate().getMetrics().keySet().stream() : Stream.of(metric))
                                                                                        .forEach(metricEntry -> abilityData.getStatisticData().getMetricData(metricEntry).setValue(switch (action) {
                                                                                            case SET -> value;
                                                                                            case ADD -> abilityData.getStatisticData().getMetricData(metricEntry).getValue() + value;
                                                                                            case TAKE -> abilityData.getStatisticData().getMetricData(metricEntry).getValue() - value;
                                                                                        }));
                                                                            });

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
                                                        .then(Commands.argument("override", DoubleArgumentType.doubleArg())
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
                                                                    var override = DoubleArgumentType.getDouble(ctx, "override");

                                                                    var abilitiesData = relic.getRelicData(player, stack).getAbilitiesData();

                                                                    Stream.of(ability.equals("all") ? abilitiesData.getAbilityIds().toArray(new String[0]) : new String[]{ability})
                                                                            .forEach(abilityEntry -> {
                                                                                var abilityData = abilitiesData.getAbilityData(abilityEntry);

                                                                                (stat.equals("all") ? abilityData.getTemplate().getStats().keySet().stream() : Stream.of(stat))
                                                                                        .forEach(statEntry -> abilityData.getStatData(statEntry).setOverrideValue(switch (action) {
                                                                                            case SET -> override;
                                                                                            case ADD -> abilityData.getStatData(statEntry).getUnscaledValue() + override;
                                                                                            case TAKE -> abilityData.getStatData(statEntry).getUnscaledValue() - override;
                                                                                        }));
                                                                            });

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

                                                                    var abilitiesData = relic.getRelicData(player, stack).getAbilitiesData();

                                                                    Stream.of(ability.equals("all") ? abilitiesData.getAbilityIds().toArray(new String[0]) : new String[]{ability})
                                                                            .forEach(abilityEntry -> {
                                                                                var abilityData = abilitiesData.getAbilityData(abilityEntry);

                                                                                (stat.equals("all") ? abilityData.getTemplate().getStats().keySet().stream() : Stream.of(stat))
                                                                                        .forEach(statEntry -> abilityData.getStatData(statEntry).setInitialQuality(switch (action) {
                                                                                            case SET -> quality;
                                                                                            case ADD -> abilityData.getStatData(statEntry).getInitialQuality() + quality;
                                                                                            case TAKE -> abilityData.getStatData(statEntry).getInitialQuality() - quality;
                                                                                        }));
                                                                            });

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

                                                    var abilitiesData = relic.getRelicData(player, stack).getAbilitiesData();

                                                    Stream.of(ability.equals("all") ? abilitiesData.getAbilityIds().toArray(new String[0]) : new String[]{ability})
                                                            .forEach(abilityEntry -> {
                                                                var abilityData = abilitiesData.getAbilityData(abilityEntry);

                                                                (stat.equals("all") ? abilityData.getTemplate().getStats().keySet().stream() : Stream.of(stat))
                                                                        .forEach(statEntry -> abilityData.randomizeStat(statEntry));
                                                            });

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
