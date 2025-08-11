package it.hurts.sskirillss.relics.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class AbilityStatArgument implements ArgumentType<String> {
    public static AbilityStatArgument abilityStat() {
        return new AbilityStatArgument();
    }

    public static String getAbilityStat(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof ClientSuggestionProvider ? this.constructSuggestions(context, builder) : Suggestions.empty();
    }

    @OnlyIn(Dist.CLIENT)
    public <S> CompletableFuture<Suggestions> constructSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var player = Minecraft.getInstance().player;

        if (player == null || !(player.getMainHandItem().getItem() instanceof IRelicItem relic))
            return Suggestions.empty();

        var ability = StringArgumentType.getString(context, "ability");

        var result = new ArrayList<String>();

        if (ability.equals("all")) {
            for (var abilityEntry : relic.getRelicTemplate(player, player.getMainHandItem()).getAbilities().getAbilities().values())
                result.addAll(abilityEntry.getStats().keySet());
        } else {
            var data = relic.getAbilityTemplate(player, player.getMainHandItem(), ability);

            if (data == null)
                return Suggestions.empty();

            result.addAll(data.getStats().keySet());
        }

        result.add("all");

        return SharedSuggestionProvider.suggest(result, builder);
    }
}