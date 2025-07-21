package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.StatisticContainerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbilityExperienceContainerWidget extends StatisticContainerWidget {
    public AbilityExperienceContainerWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, screen);
    }

    @Override
    public List<FormattedCharSequence> getContent() {
        if (!(this.getScreen() instanceof AbilityDescriptionScreen screen))
            return new ArrayList<>();

        var sequences = new ArrayList<FormattedCharSequence>();

        var stack = this.getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();
        var player = this.minecraft.player;
        var font = this.minecraft.font;
        var maxWidth = 320;

        for (var source : relic.getAbilityTemplate(player, stack, screen.getSelectedAbility()).getExperienceSources()) {
            sequences.addAll(font.split(Component.literal("● ").append(Component.translatable("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "ability." + screen.getSelectedAbility() + ".experience_source." + source)), maxWidth));

            sequences.addAll(font.split(Component.literal(" "), maxWidth));
        }

        return sequences;
    }
}