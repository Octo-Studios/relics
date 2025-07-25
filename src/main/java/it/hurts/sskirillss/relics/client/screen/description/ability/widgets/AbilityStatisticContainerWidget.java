package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.SimpleDescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbilityStatisticContainerWidget extends SimpleDescriptionContainerWidget {
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
            var prefix = Component.literal("● ").append(metric.getComponent().apply(player, stack, Optional.of(screen.getSelectedAbility()))).append(Component.literal(" "));

            if (!metric.getVisibilityCondition().test(player, stack, Optional.of(screen.getSelectedAbility())))
                prefix = ScreenUtils.randomizeAllCharacters(prefix, this.hashCode()).withStyle(Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(DescriptionUtils.NEGATIVE_COLOR(true)));

            var suffix = Component.literal(" ").append(Component.literal(metric.getFormatValue().apply(relic.getAbilityMetricComponent(player, stack, screen.getSelectedAbility(), metric.getId()).getValue())).withStyle(ChatFormatting.BOLD));

            var availableWidth = maxWidth - font.width(prefix) - font.width(suffix);
            var repeatCount = Math.max(0, availableWidth / dotWidth);
            var line = Component.empty().append(prefix).append(dot.repeat(repeatCount)).append(suffix);

            sequences.addAll(font.split(line, maxWidth));
        }

        return sequences;
    }
}