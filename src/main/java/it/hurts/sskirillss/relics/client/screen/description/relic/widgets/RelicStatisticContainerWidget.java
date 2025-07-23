package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.StatisticContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RelicStatisticContainerWidget extends StatisticContainerWidget {
    public RelicStatisticContainerWidget(DescriptionScreen screen) {
        super(screen);
    }

    @Override
    public List<FormattedCharSequence> getContent() {
        var sequences = new ArrayList<FormattedCharSequence>();
        var stack = this.getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();
        var player = this.minecraft.player;
        var font = this.minecraft.font;
        var maxWidth = 320;
        var dot = ". ";
        var dotWidth = font.width(dot);

        for (var metric : relic.getRelicStatisticTemplate(player, stack).getMetrics().values()) {
            if (!metric.getVisibilityCondition().test(player, stack, Optional.empty()))
                continue;

            var prefix = Component.literal("● ").append(metric.getComponent().apply(player, stack, Optional.empty())).append(Component.literal(" "));
            var suffix = Component.literal(" ").append(Component.literal(metric.getFormatValue().apply(relic.getRelicMetricComponent(player, stack, metric.getId()).getValue())).withStyle(ChatFormatting.BOLD));

            var availableWidth = maxWidth - font.width(prefix) - font.width(suffix);
            var repeatCount = availableWidth / dotWidth;
            var line = prefix.append(dot.repeat(repeatCount)).append(suffix);

            sequences.addAll(font.split(line, maxWidth));
        }

        return sequences;
    }
}