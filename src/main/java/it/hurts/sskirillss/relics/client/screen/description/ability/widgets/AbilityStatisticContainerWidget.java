package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.StatisticContainerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbilityStatisticContainerWidget extends StatisticContainerWidget {
    public AbilityStatisticContainerWidget(DescriptionScreen screen) {
        super(screen);
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
        var dot = ".";
        var dotWidth = font.width(dot);

        for (var metric : relic.getAbilityStatisticTemplate(player, stack, screen.getSelectedAbility()).getMetrics().values()) {
            if (!metric.getVisibilityCondition().test(player, stack, Optional.of(screen.getSelectedAbility())))
                continue;

            var prefix = Component.literal("● ").append(metric.getComponent().apply(player, stack, Optional.of(screen.getSelectedAbility()))).append(Component.literal(" "));
            var suffix = Component.literal(" ").append(Component.literal(metric.getFormatValue().apply(relic.getAbilityMetricComponent(player, stack, screen.getSelectedAbility(), metric.getId()).getValue())).withStyle(ChatFormatting.BOLD));

            var availableWidth = maxWidth - font.width(prefix) - font.width(suffix);
            var repeatCount = availableWidth / dotWidth;
            var line = prefix.append(dot.repeat(repeatCount)).append(suffix);

            sequences.addAll(font.split(line, maxWidth));
        }

        return sequences;
    }
}