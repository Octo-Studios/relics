package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.SimpleDescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
        var dotWidth = Math.max(1, font.width(dot));

        var ability = screen.getSelectedAbility();
        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability);
        var template = abilityData.getTemplate();

        if (template == null)
            return new ArrayList<>();

        var group = new LinkedHashMap<String, List<List<FormattedCharSequence>>>();
        var conditions = new HashMap<String, MutableComponent>();

        for (var metric : template.getStatistic().getMetrics().values()) {
            var state = metric.getVisibilityState().apply(player, stack, ability);

            if (state == VisibilityState.HIDDEN)
                continue;

            var condition = metric.getConditionComponent()
                    .apply(player, stack, ability)
                    .withStyle(ChatFormatting.BOLD);
            var key = condition.getString().trim();

            var prefix = Component.literal("● ")
                    .append(metric.getDescriptionComponent().apply(player, stack, ability))
                    .append(Component.literal(" "));

            if (state == VisibilityState.OBFUSCATED)
                prefix = ScreenUtils.randomizeAllCharacters(prefix, this.hashCode())
                        .withStyle(Style.EMPTY
                                .withFont(ScreenUtils.ILLAGER_ALT_FONT)
                                .withColor(DescriptionUtils.NEGATIVE_COLOR(true)));

            var metricLines = new ArrayList<FormattedCharSequence>();

            if (state == VisibilityState.OBFUSCATED) {
                var lines = font.split(prefix, maxWidth);
                metricLines.addAll(lines);
            } else {
                var suffix = Component.literal(" ")
                        .append(Component.literal(metric.getFormatValue()
                                        .apply(abilityData.getStatisticData().getMetricData(metric.getId()).getValue()))
                                .withStyle(ChatFormatting.BOLD));

                var suffixWidth = font.width(suffix);
                var limit = Math.max(0, maxWidth - suffixWidth);
                var lines = font.split(prefix, limit);

                for (var i = 0; i < lines.size(); i++) {
                    var line = lines.get(i);

                    if (i < lines.size() - 1)
                        metricLines.add(line);
                    else {
                        var avail = Math.max(0, maxWidth - font.width(line) - suffixWidth);
                        var dotsCount = Math.max(0, avail / dotWidth);
                        var dots = dotsCount > 0
                                ? FormattedCharSequence.forward(dot.repeat(dotsCount), Style.EMPTY)
                                : FormattedCharSequence.EMPTY;

                        var seq = FormattedCharSequence.composite(line, dots, suffix.getVisualOrderText());

                        while (font.width(seq) > maxWidth && dotsCount > 0) {
                            dotsCount--;
                            dots = dotsCount > 0
                                    ? FormattedCharSequence.forward(dot.repeat(dotsCount), Style.EMPTY)
                                    : FormattedCharSequence.EMPTY;
                            seq = FormattedCharSequence.composite(line, dots, suffix.getVisualOrderText());
                        }

                        metricLines.add(seq);
                    }
                }
            }

            group.computeIfAbsent(key, k -> new ArrayList<>()).add(metricLines);

            if (!key.isBlank() && !conditions.containsKey(key))
                conditions.put(key, condition);
        }

        var firstGroup = true;

        for (var entry : group.entrySet()) {
            if (!firstGroup)
                sequences.addAll(font.split(Component.literal(" "), maxWidth));

            firstGroup = false;

            var key = entry.getKey();

            if (!key.isBlank() && conditions.containsKey(key) && !conditions.get(key).equals(Component.empty()))
                sequences.addAll(font.split(conditions.get(key), maxWidth));

            for (var metricLines : entry.getValue())
                sequences.addAll(metricLines);
        }

        return sequences;
    }
}
