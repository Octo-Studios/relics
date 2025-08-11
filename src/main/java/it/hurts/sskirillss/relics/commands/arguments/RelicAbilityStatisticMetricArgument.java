package it.hurts.sskirillss.relics.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class RelicAbilityStatisticMetricArgument implements ArgumentType<String> {
    public static RelicAbilityStatisticMetricArgument abilityStatisticMetric() {
        return new RelicAbilityStatisticMetricArgument();
    }

    public static String getAbilityStatisticMetric(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    @SneakyThrows
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof CommandSourceStack sourceStack) || !(sourceStack.getEntity() instanceof ServerPlayer player))
            return Suggestions.empty();

        var stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return Suggestions.empty();

        var ability = StringArgumentType.getString(context, "ability");

        var result = new ArrayList<String>();

        if (ability.equals("all"))
            for (AbilityTemplate abilityEntry : relic.getAbilitiesTemplate(player, stack).getAbilities().values())
                result.addAll(abilityEntry.getStatistic().getMetrics().keySet());
        else
            result.addAll(relic.getAbilityStatisticTemplate(player, stack, ability).getMetrics().keySet());

        result.add("all");

        return SharedSuggestionProvider.suggest(result, builder);
    }
}