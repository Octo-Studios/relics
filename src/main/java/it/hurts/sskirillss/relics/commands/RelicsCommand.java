package it.hurts.sskirillss.relics.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.commands.arguments.RelicAbilityArgument;
import it.hurts.sskirillss.relics.commands.arguments.RelicAbilityStatArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.server.command.EnumArgument;

public class RelicsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("relics").requires(sender -> sender.hasPermission(2))
                .then(Commands.literal("maximize")
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                            if (!(stack.getItem() instanceof IRelicItem relic)) {
                                context.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                return 0;
                            }

                            var relicData = relic.getRelicTemplate(player, stack);

                            relic.setRelicRank(player, stack, relicData.getLeveling().getMaxRank());
                            relic.setRelicLevel(player, stack, relicData.getLeveling().getMaxLevel());

                            for (var abilityEntry : relicData.getAbilities().getAbilities().entrySet()) {
                                var abilityId = abilityEntry.getKey();
                                var abilityData = abilityEntry.getValue();

                                relic.setAbilityLevel(player, stack, abilityId, relicData.getAbilities().getAbilities().get(abilityId).getMaxLevel());
                                relic.setLockUnlocks(player, stack, abilityId, relic.getMaxLockUnlocks());
                                relic.setAbilityResearched(player, stack, abilityId, true);

                                for (var statEntry : abilityData.getStats().entrySet())
                                    relic.setStatOverrideValue(player, stack, abilityId, statEntry.getKey(), statEntry.getValue().getInitialValue().getValue());
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("minimize")
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                            if (!(stack.getItem() instanceof IRelicItem relic)) {
                                context.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                return 0;
                            }

                            var relicData = relic.getRelicTemplate(player, stack);

                            relic.setRelicLevel(player, stack, relicData.getLeveling().getMaxLevel());
                            relic.setRelicExperience(player, stack, 0);

                            for (var abilityEntry : relicData.getAbilities().getAbilities().entrySet()) {
                                var abilityId = abilityEntry.getKey();

                                relic.setAbilityResearched(player, stack, abilityId, false);
                                relic.setAbilityLevel(player, stack, abilityId, 0);

                                if (!relic.isEnoughLevel(player, stack, abilityId))
                                    relic.setLockUnlocks(player, stack, abilityId, 0);

                                for (var statEntry : abilityEntry.getValue().getStats().entrySet())
                                    relic.setStatOverrideValue(player, stack, abilityId, statEntry.getKey(), statEntry.getValue().getInitialValue().getKey());
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("level")
                        .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                .then(Commands.argument("level", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            var player = context.getSource().getPlayerOrException();
                                            var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                            if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                context.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                return 0;
                                            }

                                            var level = IntegerArgumentType.getInteger(context, "level");

                                            switch (context.getArgument("action", CommandAction.class)) {
                                                case SET -> relic.setRelicLevel(player, stack, level);
                                                case ADD -> relic.addRelicLevel(player, stack, level);
                                                case TAKE -> relic.addRelicLevel(player, stack, -level);
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("experience")
                        .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                .then(Commands.argument("experience", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            var player = context.getSource().getPlayerOrException();
                                            var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                            if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                context.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                return 0;
                                            }

                                            var experience = IntegerArgumentType.getInteger(context, "experience");

                                            switch (context.getArgument("action", CommandAction.class)) {
                                                case SET -> relic.setRelicExperience(null, stack, experience);
                                                case ADD -> relic.addRelicExperience(null, stack, experience);
                                                case TAKE -> relic.addRelicExperience(null, stack, -experience);
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("points")
                        .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                .then(Commands.argument("points", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            var player = context.getSource().getPlayerOrException();
                                            var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                            if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                context.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                return 0;
                                            }

                                            var points = IntegerArgumentType.getInteger(context, "points");

                                            switch (context.getArgument("action", CommandAction.class)) {
                                                case SET -> relic.setRelicLevelingPoints(player, stack, points);
                                                case ADD -> relic.addRelicLevelingPoints(player, stack, points);
                                                case TAKE -> relic.addRelicLevelingPoints(player, stack, -points);
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("ability")
                        .then(Commands.literal("points")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("ability", RelicAbilityArgument.ability())
                                                .then(Commands.argument("points", IntegerArgumentType.integer())
                                                        .executes(context -> {
                                                            var player = context.getSource().getPlayerOrException();
                                                            var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                            if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                                context.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                                return 0;
                                                            }

                                                            var action = context.getArgument("action", CommandAction.class);

                                                            var ability = RelicAbilityArgument.getAbility(context, "ability");
                                                            var points = IntegerArgumentType.getInteger(context, "points");

                                                            if (ability.equals("all")) {
                                                                for (var entry : relic.getAbilitiesTemplate(player, stack).getAbilities().keySet()) {
                                                                    switch (action) {
                                                                        case SET -> relic.setAbilityLevel(player, stack, entry, points);
                                                                        case ADD -> relic.addAbilityLevel(player, stack, entry, points);
                                                                        case TAKE -> relic.addAbilityLevel(player, stack, entry, -points);
                                                                    }
                                                                }
                                                            } else {
                                                                switch (action) {
                                                                    case SET -> relic.setAbilityLevel(player, stack, ability, points);
                                                                    case ADD -> relic.addAbilityLevel(player, stack, ability, points);
                                                                    case TAKE -> relic.addAbilityLevel(player, stack, ability, -points);
                                                                }
                                                            }

                                                            return Command.SINGLE_SUCCESS;
                                                        })))))
                        .then(Commands.literal("value")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("ability", RelicAbilityArgument.ability())
                                                .then(Commands.argument("stat", RelicAbilityStatArgument.abilityStat())
                                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                                .executes(context -> {
                                                                    var player = context.getSource().getPlayerOrException();
                                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                                        context.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                                        return 0;
                                                                    }

                                                                    var action = context.getArgument("action", CommandAction.class);

                                                                    var ability = RelicAbilityArgument.getAbility(context, "ability");
                                                                    var stat = RelicAbilityStatArgument.getAbilityStat(context, "stat");
                                                                    var value = DoubleArgumentType.getDouble(context, "value");

                                                                    if (ability.equals("all")) {
                                                                        for (var abilityEntry : relic.getAbilitiesTemplate(player, stack).getAbilities().keySet()) {
                                                                            if (stat.equals("all")) {
                                                                                for (var statEntry : relic.getAbilityTemplate(player, stack, abilityEntry).getStats().keySet()) {
                                                                                    switch (action) {
                                                                                        case SET -> relic.setStatOverrideValue(player, stack, abilityEntry, statEntry, value);
                                                                                        case ADD -> relic.addStatOverrideValue(player, stack, abilityEntry, statEntry, value);
                                                                                        case TAKE -> relic.addStatOverrideValue(player, stack, abilityEntry, statEntry, -value);
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                switch (action) {
                                                                                    case SET -> relic.setStatOverrideValue(player, stack, abilityEntry, stat, value);
                                                                                    case ADD -> relic.addStatOverrideValue(player, stack, abilityEntry, stat, value);
                                                                                    case TAKE -> relic.addStatOverrideValue(player, stack, abilityEntry, stat, -value);
                                                                                }
                                                                            }
                                                                        }
                                                                    } else {
                                                                        if (stat.equals("all")) {
                                                                            for (var statEntry : relic.getAbilityTemplate(player, stack, ability).getStats().keySet()) {
                                                                                switch (action) {
                                                                                    case SET -> relic.setStatOverrideValue(player, stack, ability, statEntry, value);
                                                                                    case ADD -> relic.addStatOverrideValue(player, stack, ability, statEntry, value);
                                                                                    case TAKE -> relic.addStatOverrideValue(player, stack, ability, statEntry, -value);
                                                                                }
                                                                            }
                                                                        } else {
                                                                            switch (action) {
                                                                                case SET -> relic.setStatOverrideValue(player, stack, ability, stat, value);
                                                                                case ADD -> relic.addStatOverrideValue(player, stack, ability, stat, value);
                                                                                case TAKE -> relic.addStatOverrideValue(player, stack, ability, stat, -value);
                                                                            }
                                                                        }
                                                                    }

                                                                    return Command.SINGLE_SUCCESS;
                                                                }))))))
                        .then(Commands.literal("quality")
                                .then(Commands.argument("action", EnumArgument.enumArgument(CommandAction.class))
                                        .then(Commands.argument("ability", RelicAbilityArgument.ability())
                                                .then(Commands.argument("stat", RelicAbilityStatArgument.abilityStat())
                                                        .then(Commands.argument("quality", IntegerArgumentType.integer())
                                                                .executes(context -> {
                                                                    var player = context.getSource().getPlayerOrException();
                                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                                        context.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                                        return 0;
                                                                    }

                                                                    var action = context.getArgument("action", CommandAction.class);

                                                                    var ability = RelicAbilityArgument.getAbility(context, "ability");
                                                                    var stat = RelicAbilityStatArgument.getAbilityStat(context, "stat");
                                                                    var quality = IntegerArgumentType.getInteger(context, "quality");

                                                                    if (ability.equals("all")) {
                                                                        for (String abilityEntry : relic.getAbilitiesTemplate(player, stack).getAbilities().keySet()) {
                                                                            if (stat.equals("all")) {
                                                                                for (String statEntry : relic.getAbilityTemplate(player, stack, abilityEntry).getStats().keySet()) {
                                                                                    double value = relic.getStatValueFromQuality(player, stack, abilityEntry, statEntry, quality);

                                                                                    switch (action) {
                                                                                        case SET -> relic.setStatOverrideValue(player, stack, abilityEntry, statEntry, value);
                                                                                        case ADD -> relic.addStatOverrideValue(player, stack, abilityEntry, statEntry, value);
                                                                                        case TAKE -> relic.addStatOverrideValue(player, stack, abilityEntry, statEntry, -value);
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                double value = relic.getStatValueFromQuality(player, stack, abilityEntry, stat, quality);

                                                                                switch (action) {
                                                                                    case SET -> relic.setStatOverrideValue(player, stack, abilityEntry, stat, value);
                                                                                    case ADD -> relic.addStatOverrideValue(player, stack, abilityEntry, stat, value);
                                                                                    case TAKE -> relic.addStatOverrideValue(player, stack, abilityEntry, stat, -value);
                                                                                }
                                                                            }
                                                                        }
                                                                    } else {
                                                                        if (stat.equals("all")) {
                                                                            for (String statEntry : relic.getAbilityTemplate(player, stack, ability).getStats().keySet()) {
                                                                                double value = relic.getStatValueFromQuality(player, stack, ability, statEntry, quality);

                                                                                switch (action) {
                                                                                    case SET -> relic.setStatOverrideValue(player, stack, ability, statEntry, value);
                                                                                    case ADD -> relic.addStatOverrideValue(player, stack, ability, statEntry, value);
                                                                                    case TAKE -> relic.addStatOverrideValue(player, stack, ability, statEntry, -value);
                                                                                }
                                                                            }
                                                                        } else {
                                                                            double value = relic.getStatValueFromQuality(player, stack, ability, stat, quality);

                                                                            switch (action) {
                                                                                case SET -> relic.setStatOverrideValue(player, stack, ability, stat, value);
                                                                                case ADD -> relic.addStatOverrideValue(player, stack, ability, stat, value);
                                                                                case TAKE -> relic.addStatOverrideValue(player, stack, ability, stat, -value);
                                                                            }
                                                                        }
                                                                    }

                                                                    return Command.SINGLE_SUCCESS;
                                                                }))))))
                        .then(Commands.literal("randomize")
                                .then(Commands.argument("ability", RelicAbilityArgument.ability())
                                        .then(Commands.argument("stat", RelicAbilityStatArgument.abilityStat())
                                                .executes(context -> {
                                                    var player = context.getSource().getPlayerOrException();
                                                    var stack = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                    if (!(stack.getItem() instanceof IRelicItem relic)) {
                                                        context.getSource().sendFailure(Component.translatable("command.relics.base.not_relic"));

                                                        return 0;
                                                    }

                                                    var ability = RelicAbilityArgument.getAbility(context, "ability");
                                                    var stat = RelicAbilityStatArgument.getAbilityStat(context, "stat");

                                                    if (ability.equals("all")) {
                                                        for (var abilityEntry : relic.getAbilitiesTemplate(player, stack).getAbilities().keySet()) {
                                                            if (stat.equals("all")) {
                                                                for (var statEntry : relic.getAbilityTemplate(player, stack, abilityEntry).getStats().keySet())
                                                                    relic.randomizeStat(player, stack, abilityEntry, statEntry);
                                                            } else {
                                                                relic.randomizeStat(player, stack, abilityEntry, stat);
                                                            }
                                                        }
                                                    } else {
                                                        if (stat.equals("all")) {
                                                            for (var statEntry : relic.getAbilityTemplate(player, stack, ability).getStats().keySet())
                                                                relic.randomizeStat(player, stack, ability, statEntry);
                                                        } else {
                                                            relic.randomizeStat(player, stack, ability, stat);
                                                        }
                                                    }

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