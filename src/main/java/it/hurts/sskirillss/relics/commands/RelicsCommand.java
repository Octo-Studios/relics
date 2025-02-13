package it.hurts.sskirillss.relics.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import it.hurts.sskirillss.relics.commands.arguments.RelicAbilityArgument;
import it.hurts.sskirillss.relics.commands.arguments.RelicAbilityStatArgument;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
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

                            var relicData = relic.getRelicData();

                            relic.setRelicLevel(stack, relicData.getLeveling().getMaxLevel());

                            for (var abilityEntry : relicData.getAbilities().getAbilities().entrySet()) {
                                var abilityId = abilityEntry.getKey();
                                var abilityData = abilityEntry.getValue();

                                relic.setAbilityLevel(stack, abilityId, relic.getAbilityMaxLevel(stack, abilityId));
                                relic.setLockUnlocks(stack, abilityId, relic.getMaxLockUnlocks());
                                relic.setAbilityResearched(stack, abilityId, true);

                                for (var statEntry : abilityData.getStats().entrySet())
                                    relic.setStatInitialValue(stack, abilityId, statEntry.getKey(), statEntry.getValue().getInitialValue().getValue());
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

                            var relicData = relic.getRelicData();

                            relic.setRelicLevel(stack, relicData.getLeveling().getMaxLevel());
                            relic.setRelicExperience(stack, 0);

                            for (var abilityEntry : relicData.getAbilities().getAbilities().entrySet()) {
                                var abilityId = abilityEntry.getKey();

                                relic.setAbilityResearched(stack, abilityId, false);
                                relic.setAbilityLevel(stack, abilityId, 0);

                                if (!relic.isEnoughLevel(stack, abilityId))
                                    relic.setLockUnlocks(stack, abilityId, 0);

                                for (var statEntry : abilityEntry.getValue().getStats().entrySet())
                                    relic.setStatInitialValue(stack, abilityId, statEntry.getKey(), statEntry.getValue().getInitialValue().getKey());
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
                                                case SET -> relic.setRelicLevel(stack, level);
                                                case ADD -> relic.addRelicLevel(stack, level);
                                                case TAKE -> relic.addRelicLevel(stack, -level);
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
                                                case SET -> relic.setRelicExperience(stack, experience);
                                                case ADD -> relic.addRelicExperience(stack, experience);
                                                case TAKE -> relic.addRelicExperience(stack, -experience);
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
                                                case SET -> relic.setRelicLevelingPoints(stack, points);
                                                case ADD -> relic.addRelicLevelingPoints(stack, points);
                                                case TAKE -> relic.addRelicLevelingPoints(stack, -points);
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
                                                                for (var entry : relic.getRelicData().getAbilities().getAbilities().keySet()) {
                                                                    switch (action) {
                                                                        case SET -> relic.setAbilityLevel(stack, entry, points);
                                                                        case ADD -> relic.addAbilityLevel(stack, entry, points);
                                                                        case TAKE -> relic.addAbilityLevel(stack, entry, -points);
                                                                    }
                                                                }
                                                            } else {
                                                                switch (action) {
                                                                    case SET -> relic.setAbilityLevel(stack, ability, points);
                                                                    case ADD -> relic.addAbilityLevel(stack, ability, points);
                                                                    case TAKE -> relic.addAbilityLevel(stack, ability, -points);
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
                                                                        for (var abilityEntry : relic.getRelicData().getAbilities().getAbilities().keySet()) {
                                                                            if (stat.equals("all")) {
                                                                                for (var statEntry : relic.getAbilityData(abilityEntry).getStats().keySet()) {
                                                                                    switch (action) {
                                                                                        case SET -> relic.setStatInitialValue(stack, abilityEntry, statEntry, value);
                                                                                        case ADD -> relic.addStatInitialValue(stack, abilityEntry, statEntry, value);
                                                                                        case TAKE -> relic.addStatInitialValue(stack, abilityEntry, statEntry, -value);
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                switch (action) {
                                                                                    case SET -> relic.setStatInitialValue(stack, abilityEntry, stat, value);
                                                                                    case ADD -> relic.addStatInitialValue(stack, abilityEntry, stat, value);
                                                                                    case TAKE -> relic.addStatInitialValue(stack, abilityEntry, stat, -value);
                                                                                }
                                                                            }
                                                                        }
                                                                    } else {
                                                                        if (stat.equals("all")) {
                                                                            for (var statEntry : relic.getAbilityData(ability).getStats().keySet()) {
                                                                                switch (action) {
                                                                                    case SET -> relic.setStatInitialValue(stack, ability, statEntry, value);
                                                                                    case ADD -> relic.addStatInitialValue(stack, ability, statEntry, value);
                                                                                    case TAKE -> relic.addStatInitialValue(stack, ability, statEntry, -value);
                                                                                }
                                                                            }
                                                                        } else {
                                                                            switch (action) {
                                                                                case SET -> relic.setStatInitialValue(stack, ability, stat, value);
                                                                                case ADD -> relic.addStatInitialValue(stack, ability, stat, value);
                                                                                case TAKE -> relic.addStatInitialValue(stack, ability, stat, -value);
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
                                                                        for (String abilityEntry : relic.getRelicData().getAbilities().getAbilities().keySet()) {
                                                                            if (stat.equals("all")) {
                                                                                for (String statEntry : relic.getAbilityData(abilityEntry).getStats().keySet()) {
                                                                                    double value = relic.getStatValueByQuality(abilityEntry, statEntry, quality);

                                                                                    switch (action) {
                                                                                        case SET -> relic.setStatInitialValue(stack, abilityEntry, statEntry, value);
                                                                                        case ADD -> relic.addStatInitialValue(stack, abilityEntry, statEntry, value);
                                                                                        case TAKE -> relic.addStatInitialValue(stack, abilityEntry, statEntry, -value);
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                double value = relic.getStatValueByQuality(abilityEntry, stat, quality);

                                                                                switch (action) {
                                                                                    case SET -> relic.setStatInitialValue(stack, abilityEntry, stat, value);
                                                                                    case ADD -> relic.addStatInitialValue(stack, abilityEntry, stat, value);
                                                                                    case TAKE -> relic.addStatInitialValue(stack, abilityEntry, stat, -value);
                                                                                }
                                                                            }
                                                                        }
                                                                    } else {
                                                                        if (stat.equals("all")) {
                                                                            for (String statEntry : relic.getAbilityData(ability).getStats().keySet()) {
                                                                                double value = relic.getStatValueByQuality(ability, statEntry, quality);

                                                                                switch (action) {
                                                                                    case SET -> relic.setStatInitialValue(stack, ability, statEntry, value);
                                                                                    case ADD -> relic.addStatInitialValue(stack, ability, statEntry, value);
                                                                                    case TAKE -> relic.addStatInitialValue(stack, ability, statEntry, -value);
                                                                                }
                                                                            }
                                                                        } else {
                                                                            double value = relic.getStatValueByQuality(ability, stat, quality);

                                                                            switch (action) {
                                                                                case SET -> relic.setStatInitialValue(stack, ability, stat, value);
                                                                                case ADD -> relic.addStatInitialValue(stack, ability, stat, value);
                                                                                case TAKE -> relic.addStatInitialValue(stack, ability, stat, -value);
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
                                                        for (var abilityEntry : relic.getRelicData().getAbilities().getAbilities().keySet()) {
                                                            if (stat.equals("all")) {
                                                                for (var statEntry : relic.getAbilityData(abilityEntry).getStats().keySet())
                                                                    relic.randomizeStat(stack, abilityEntry, statEntry);
                                                            } else {
                                                                relic.randomizeStat(stack, abilityEntry, stat);
                                                            }
                                                        }
                                                    } else {
                                                        if (stat.equals("all")) {
                                                            for (var statEntry : relic.getAbilityData(ability).getStats().keySet())
                                                                relic.randomizeStat(stack, ability, statEntry);
                                                        } else {
                                                            relic.randomizeStat(stack, ability, stat);
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