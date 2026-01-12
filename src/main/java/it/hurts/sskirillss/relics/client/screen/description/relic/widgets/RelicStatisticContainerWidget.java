package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class RelicStatisticContainerWidget extends SimpleDescriptionContainerWidget {
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
        var dot = ".";
        var dotWidth = Math.max(1, font.width(dot));

        var statisticData = relic.getRelicData(player, stack).getStatisticData();

        for (var metric : statisticData.getTemplate().getMetrics().values()) {
            var state = metric.getVisibilityState().apply(player, stack);

            if (state == VisibilityState.HIDDEN)
                continue;

            var prefix = Component.literal("● ").append(metric.getComponent().apply(player, stack)).append(Component.literal(" "));

            if (state == VisibilityState.OBFUSCATED)
                prefix = ScreenUtils.randomizeAllCharacters(prefix, this.hashCode()).withStyle(Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(DescriptionUtils.NEGATIVE_COLOR(true)));

            var suffix = Component.literal(" ").append(Component.literal(metric.getFormatValue().apply(statisticData.getMetricData(metric.getId()).getValue())).withStyle(ChatFormatting.BOLD));

            var suffixWidth = font.width(suffix);
            var limit = Math.max(0, maxWidth - suffixWidth);
            var lines = font.split(prefix, limit);

            for (var i = 0; i < lines.size(); i++) {
                var line = lines.get(i);

                if (i < lines.size() - 1)
                    sequences.add(line);
                else {
                    var avail = Math.max(0, maxWidth - font.width(line) - suffixWidth);
                    var dotsCount = Math.max(0, avail / dotWidth);
                    var dots = dotsCount > 0 ? FormattedCharSequence.forward(dot.repeat(dotsCount), Style.EMPTY) : FormattedCharSequence.EMPTY;
                    var seq = FormattedCharSequence.composite(line, dots, suffix.getVisualOrderText());

                    while (font.width(seq) > maxWidth && dotsCount > 0) {
                        dotsCount--;

                        dots = dotsCount > 0 ? FormattedCharSequence.forward(dot.repeat(dotsCount), Style.EMPTY) : FormattedCharSequence.EMPTY;
                        seq = FormattedCharSequence.composite(line, dots, suffix.getVisualOrderText());
                    }

                    sequences.add(seq);
                }
            }
        }

        return sequences;
    }
}
